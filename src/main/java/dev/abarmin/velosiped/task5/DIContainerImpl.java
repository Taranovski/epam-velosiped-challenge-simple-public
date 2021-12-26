package dev.abarmin.velosiped.task5;

import dev.abarmin.velosiped.task7.Inject;
import dev.abarmin.velosiped.task7.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DIContainerImpl implements DIContainer {

    private Map<Class, Object> classObjectMap = new HashMap<>();
    private Map<Method, Object> postConstructClassObjectMap = new HashMap<>();
    private Map<Field, Map.Entry<Object, Class>> injectFieldClassObjectMap = new HashMap<>();

    @Override
    public void init() {
        String packageName = "dev.abarmin.velosiped";

        try {
            List<Class> classes = getClasses(packageName);

            for (Class aClass : classes) {
                String simpleName = aClass.getSimpleName();
                if (simpleName.endsWith("Impl") || simpleName.endsWith("Component")) {

                    Object newInstance = createInstance(aClass);

                    collectBeanObjectsByClass(aClass, newInstance);

                    collectDependencies(aClass, newInstance);

                    collectPostConstructs(aClass, newInstance);
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
        for (Map.Entry<Field, Map.Entry<Object, Class>> fieldEntryEntry : injectFieldClassObjectMap.entrySet()) {
            Field field = fieldEntryEntry.getKey();
            Map.Entry<Object, Class> value = fieldEntryEntry.getValue();

            Object object = value.getKey();
            Class aClass1 = value.getValue();

            field.setAccessible(true);
            field.set(object, classObjectMap.get(aClass1));
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

                injectFieldClassObjectMap.put(declaredField, new AbstractMap.SimpleEntry<>(newInstance, type));
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

    private List<Class> getClasses(String packageName) throws Exception {

        String path = packageName.replace('.', '/');

        List<File> dirs = collectDirs(path);

        List<Class> classes = collectClasses(packageName, dirs);

        return classes;
    }

    private List<Class> collectClasses(String packageName, List<File> dirs) throws Exception {
        List<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private List<File> collectDirs(String path) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        return dirs;
    }

    private List<Class> findClasses(File directory, String packageName) throws Exception {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
