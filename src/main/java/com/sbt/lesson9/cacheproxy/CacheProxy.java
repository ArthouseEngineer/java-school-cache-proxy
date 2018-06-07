package com.sbt.lesson9.cacheproxy;


import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sbt.lesson9.serialization.*;

import static java.lang.ClassLoader.getSystemClassLoader;

public class CacheProxy implements InvocationHandler {

    private final Map<String, Object> resByArgs = new HashMap<>();
    private final Object delegate;
    private final String rootCacheDirectory;

    public CacheProxy(Object delegate, String rootCacheDirectory) {
        this.delegate = delegate;
        this.rootCacheDirectory = rootCacheDirectory;
    }

    public static <T> T cache(Object delegate, String rootCacheDirectory) {
        return (T) Proxy.newProxyInstance(getSystemClassLoader(),
                delegate.getClass().getInterfaces(),
                new CacheProxy(delegate, rootCacheDirectory)
        );
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!method.isAnnotationPresent(Cache.class)) {
            return this.invoke(method, args);
        }

        String key = key(method, args);
        Cache cacheSettings = method.getDeclaredAnnotation(Cache.class);
        if (cacheSettings.cacheType() == CacheType.IN_MEMORY) {
            if (!resByArgs.containsKey(key)) {
                Object result = getResult(method, args);
                resByArgs.put(key, result);
            }
            return resByArgs.get(key);
        } else {
            File cachedResultFile = Paths.get(rootCacheDirectory, key + ".ser").toFile();
            if (!cachedResultFile.exists()) {
                Object result = getResult(method, args);
                SerializationUtils.serialize((Serializable) result,cachedResultFile,cacheSettings.zip());
            }
            return SerializationUtils.deserialize(cachedResultFile,cacheSettings.zip());
        }
    }

    private Object invoke(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Не возможно вызывать метод : " + method.getName());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Object getResult(Method method, Object[] args) throws Throwable {
        Object result = this.invoke(method, args);
        Class<?> returnType = method.getReturnType();

        /* Если метод возвращает список элементов, то возвращаем только последние элементы списка
         *  Количество элеметов берем из анотации
         */
        if (returnType == List.class) {
            int listSize = method.getDeclaredAnnotation(Cache.class).listSize();
            if (listSize != -1) {
                List<?> listResult = (List) result;
                return listResult.subList(listResult.size() - listSize, listResult.size());
            }
        }
        return result;
    }

    private String key(Method method, Object[] args) {
        Cache annotationSettings = method.getDeclaredAnnotation(Cache.class);
        Class<?>[] parameterTypes = method.getParameterTypes();

        byte[] identityBy = annotationSettings.identityBy();

        StringBuilder builder = new StringBuilder();

        if (annotationSettings.fileNamePrefix().equals("")) {
            builder.append(method.getName());
        } else {
            builder.append(annotationSettings.fileNamePrefix());
        }
        if (identityBy.length == 0) {
            for (int i = 0; i < parameterTypes.length; i++) {
                builder.append("-" + parameterTypes[i].getName() + args[i].hashCode());
            }
        } else {
            for (int i = 0; i < identityBy.length; i++) {
                builder.append("-" + parameterTypes[identityBy[i]] + args[identityBy[i]].hashCode());
            }
        }
        return builder.toString();
    }

}
