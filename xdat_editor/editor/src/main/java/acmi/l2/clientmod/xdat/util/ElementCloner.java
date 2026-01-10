/*
 * Copyright (c) 2024 TurtleLess - qesta.com.br
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author TurtleLess
 * @website qesta.com.br
 */
package acmi.l2.clientmod.xdat.util;

import acmi.l2.clientmod.util.IOEntity;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for deep cloning IOEntity objects.
 */
public class ElementCloner {

    /**
     * Creates a deep clone of an IOEntity object.
     *
     * @param source the object to clone
     * @return a deep copy of the object, or null if cloning fails
     */
    @SuppressWarnings("unchecked")
    public static <T extends IOEntity> T deepClone(T source) {
        if (source == null) {
            return null;
        }

        try {
            // Create new instance
            T clone = (T) source.getClass().getDeclaredConstructor().newInstance();

            // Copy all fields
            copyFields(source, clone, source.getClass());

            return clone;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copyFields(Object source, Object target, Class<?> clazz) throws Exception {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        // Process superclass fields first
        copyFields(source, target, clazz.getSuperclass());

        // Process declared fields
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(source);

            if (value == null) {
                field.set(target, null);
            } else if (isPrimitiveOrWrapper(field.getType()) || field.getType() == String.class) {
                // Primitives and strings are immutable, direct copy
                field.set(target, value);
            } else if (field.getType().isArray()) {
                // Deep clone arrays
                field.set(target, cloneArray(value));
            } else if (List.class.isAssignableFrom(field.getType())) {
                // Deep clone lists
                field.set(target, cloneList((List<?>) value));
            } else if (IOEntity.class.isAssignableFrom(field.getType())) {
                // Deep clone nested IOEntity
                field.set(target, deepClone((IOEntity) value));
            } else {
                // For other types, try direct assignment
                field.set(target, value);
            }
        }
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type == Boolean.class ||
                type == Byte.class ||
                type == Character.class ||
                type == Short.class ||
                type == Integer.class ||
                type == Long.class ||
                type == Float.class ||
                type == Double.class;
    }

    private static Object cloneArray(Object array) throws Exception {
        int length = Array.getLength(array);
        Class<?> componentType = array.getClass().getComponentType();
        Object clone = Array.newInstance(componentType, length);

        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            if (element == null) {
                Array.set(clone, i, null);
            } else if (isPrimitiveOrWrapper(componentType) || componentType == String.class) {
                Array.set(clone, i, element);
            } else if (IOEntity.class.isAssignableFrom(componentType)) {
                Array.set(clone, i, deepClone((IOEntity) element));
            } else {
                Array.set(clone, i, element);
            }
        }

        return clone;
    }

    @SuppressWarnings("unchecked")
    private static List<?> cloneList(List<?> list) throws Exception {
        List<Object> clone = new ArrayList<>(list.size());

        for (Object element : list) {
            if (element == null) {
                clone.add(null);
            } else if (isPrimitiveOrWrapper(element.getClass()) || element instanceof String) {
                clone.add(element);
            } else if (element instanceof IOEntity) {
                clone.add(deepClone((IOEntity) element));
            } else {
                clone.add(element);
            }
        }

        return clone;
    }
}
