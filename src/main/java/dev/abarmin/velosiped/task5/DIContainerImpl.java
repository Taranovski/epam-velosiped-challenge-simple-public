package dev.abarmin.velosiped.task5;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DIContainerImpl implements DIContainer {

    private Map<Class, Object> classObjectMap = new HashMap<>();

    @Override
    public void init() {
        String packageName = "dev.abarmin.velosiped.task4";

        try {
            List<Class> classes = getClasses(packageName);

            for (Class aClass : classes) {
                if (aClass.getSimpleName().endsWith("Impl")) {

                    Constructor declaredConstructor = aClass.getDeclaredConstructor();
                    Object newInstance = declaredConstructor.newInstance();

                    Class[] interfaces = aClass.getInterfaces();

                    for (Class aInterface : interfaces) {
                        classObjectMap.put(aInterface, newInstance);
                    }
                    classObjectMap.put(aClass, newInstance);
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
