package com.txznet.comm.util;

import android.support.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {
    public static Field getField(@NonNull Class<?> cls, @NonNull String name) throws NoSuchFieldException {
        Class<?> type = cls;
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException ignored) {
            }
            type = type.getSuperclass();
        }
        throw new NoSuchFieldException("cannot find field (" + cls.getCanonicalName() + "#" + name + ")");
    }

    public static Method getMethod(@NonNull Class<?> cls, @NonNull String name, Class<?>... paramTypes) throws NoSuchMethodException {
        Class<?> type = cls;
        try {
            Method declaredMethod = cls.getDeclaredMethod(name, paramTypes);
            if (declaredMethod != null) return declaredMethod;
        } catch (NoSuchMethodException ignored) {
        }
        while (type != null) {
            Method[] methods = type.getDeclaredMethods();
            for (Method method : methods) {
                if (matchesMethod(method, name, paramTypes)) {
                    return method;
                }
            }
            type = type.getSuperclass();
        }
        throw new NoSuchMethodException("cannot find method (" + cls.getCanonicalName() + "#" + name + ")");
    }

    private static boolean matchesMethod(@NonNull Method method, @NonNull String name, @NonNull Class<?>... argTypes) {
        return method.getName().equals(name) && matchesTypes(method.getParameterTypes(), argTypes);
    }

    private static boolean matchesTypes(@NonNull Class<?>[] paramTypes, @NonNull Class<?>[] argTypes) {
        if (paramTypes.length != argTypes.length) return false;
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Class<?> argType = argTypes[i];
            if (!paramType.isAssignableFrom(argType)) return false;
        }
        return true;
    }
}
