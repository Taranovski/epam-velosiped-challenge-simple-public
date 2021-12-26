package dev.abarmin.velosiped.task8;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class DIContainerImpl implements DIContainer {

    private Map<Class, Object> classObjectMap = new HashMap<>();
    private Map<Method, Object> postConstructClassObjectMap = new HashMap<>();
    private Map<Field, Map<Object, Class>> injectFieldClassObjectMap = new HashMap<>();
    private Map<HttpMethod, Map<String, MethodAndMethodParametersExtractor>> httpMethodMapMap = new HashMap<>();
    private ClassByPackageScanner classByPackageScanner = new ClassByPackageScanner();

    @Override
    public void init() {
        String packageName = "dev.abarmin.velosiped.task8";

        try {
            List<Class<?>> classes = classByPackageScanner.getClasses(packageName);

            for (Class aClass : classes) {
                String simpleName = aClass.getSimpleName();
                if (simpleName.endsWith("Impl")) {

                    Object newInstance = createInstance(aClass);

                    collectBeanObjectsByClass(aClass, newInstance);

                    collectDependencies(aClass, newInstance);

                    collectPostConstructs(aClass, newInstance);
                } else if (simpleName.endsWith("EndpointHandler")) {

                    Object newInstance = createInstance(aClass);

                    Method[] methods = aClass.getMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                            HttpMethod httpMethod = annotation.method();
                            httpMethodMapMap.putIfAbsent(httpMethod, new HashMap<>());

                            Map<String, MethodAndMethodParametersExtractor> stringMethodAndMethodParametersExtractorMap = httpMethodMapMap.get(httpMethod);
                            String path = annotation.path();

                            ArrayList<ParameterExtractor> parameterExtractorList = new ArrayList<>();
                            Parameter[] parameters = method.getParameters();
                            for (Parameter parameter : parameters) {
                                if (parameter.isAnnotationPresent(RequestBody.class)) {
                                    StringJsonBodyParameterExtractor stringJsonBodyParameterExtractor = new StringJsonBodyParameterExtractor(parameter.getType());
                                    collectDependencies(StringJsonBodyParameterExtractor.class, stringJsonBodyParameterExtractor);

                                    parameterExtractorList.add(stringJsonBodyParameterExtractor);
                                } else {
                                    //todo add query parameter
                                }
                            }
                            stringMethodAndMethodParametersExtractorMap.putIfAbsent(path, new MethodAndMethodParametersExtractor(method, newInstance, parameterExtractorList));
                        }
                    }
                    classObjectMap.put(RoutingHolder.class, new RoutingHolder() {
                        @Override
                        public Map<HttpMethod, Map<String, MethodAndMethodParametersExtractor>> getRoutings() {
                            return httpMethodMapMap;
                        }
                    });

                }
            }

            fillDependencies();

            runPostConstructs();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object createInstance(Class aClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor declaredConstructor = aClass.getDeclaredConstructor();
        Object newInstance = declaredConstructor.newInstance();
        return newInstance;
    }

    private void runPostConstructs() throws IllegalAccessException, InvocationTargetException {
        for (Map.Entry<Method, Object> entry : postConstructClassObjectMap.entrySet()) {
            Method method = entry.getKey();
            Object object = entry.getValue();
            method.invoke(object);
        }
    }

    private void fillDependencies() throws IllegalAccessException {
        for (Map.Entry<Field, Map<Object, Class>> fieldEntryEntry : injectFieldClassObjectMap.entrySet()) {
            Field field = fieldEntryEntry.getKey();
            Map<Object, Class> value = fieldEntryEntry.getValue();
            for (Map.Entry<Object, Class> objectClassEntry : value.entrySet()) {
                Object object = objectClassEntry.getKey();
                Class aClass1 = objectClassEntry.getValue();
                field.setAccessible(true);
                field.set(object, classObjectMap.get(aClass1));
            }

        }
    }

    private void collectPostConstructs(Class aClass, Object newInstance) {
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                postConstructClassObjectMap.put(method, newInstance);
            }
        }
    }

    private void collectDependencies(Class aClass, Object newInstance) {
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Inject.class)) {
                Class<?> type = declaredField.getType();

                injectFieldClassObjectMap.putIfAbsent(declaredField, new IdentityHashMap<>());
                Map<Object, Class> objectClassMap = injectFieldClassObjectMap.get(declaredField);
                objectClassMap.put(newInstance, type);
            }
        }
    }

    private void collectBeanObjectsByClass(Class aClass, Object newInstance) {
        Class[] interfaces = aClass.getInterfaces();

        for (Class aInterface : interfaces) {
            classObjectMap.put(aInterface, newInstance);
        }
        classObjectMap.put(aClass, newInstance);
    }

    @Override
    public <T> T getBean(Class<T> beanClass) {
        return (T) classObjectMap.get(beanClass);
    }

}
