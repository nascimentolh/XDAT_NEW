/*
 * Copyright (c) 2021 acmi
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
 */
package acmi.l2.clientmod.io;

import acmi.l2.clientmod.io.annotation.*;

import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReflectionSerializerFactory<C extends Context> implements SerializerFactory<C> {
    protected final Map<Class, Serializer> cache = new HashMap<>();

    @Override
    public <T> Serializer<T, C> forClass(Class<T> clazz) {
        if (!cache.containsKey(clazz)) {
            createForClass(clazz);
        }
        return cache.get(clazz);
    }

    protected void createForClass(Class<?> clazz) {
        List<BiConsumer<Object, ObjectInput<C>>> readActions = new ArrayList<>();
        List<BiConsumer<Object, ObjectOutput<C>>> writeActions = new ArrayList<>();

        Serializer<?, C> serializer = createSerializer(clazz, readActions, writeActions);

        cache.put(clazz, serializer);

        buildForClass(clazz, readActions, writeActions);
    }

    protected Serializer<?, C> createSerializer(Class<?> clazz, List<BiConsumer<Object, ObjectInput<C>>> readActions, List<BiConsumer<Object, ObjectOutput<C>>> writeActions) {
        return new SerializerImpl(clazz, readActions, writeActions);
    }

    protected Function<ObjectInput<C>, Object> createInstantiator(Class<?> clazz) {
        return input -> ReflectionUtil.instantiate(clazz);
    }

    protected BiConsumer<Object, ObjectInput<C>> createReader(Class<?> clazz, List<BiConsumer<Object, ObjectInput<C>>> readActions) {
        return (obj, input) -> readActions.forEach(action -> action.accept(obj, input));
    }

    protected BiConsumer<Object, ObjectOutput<C>> createWriter(Class<?> clazz, List<BiConsumer<Object, ObjectOutput<C>>> writeActions) {
        return (obj, output) -> writeActions.forEach(action -> action.accept(obj, output));
    }

    protected <T> void buildForClass(Class<?> clazz, List<BiConsumer<T, ObjectInput<C>>> read, List<BiConsumer<T, ObjectOutput<C>>> write) {
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            Serializer superIO = forClass(clazz.getSuperclass());
            read.add(superIO::readObject);
            write.add(superIO::writeObject);
        }

        List<BiConsumer<T, ObjectInput<C>>> read1 = new ArrayList<>();
        List<BiConsumer<T, ObjectOutput<C>>> write1 = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!validField(field)) {
                continue;
            }

            handleField(field, read1, write1);
        }

        boolean readMethod = false;
        boolean writeMethod = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ReadMethod.class)) {
                read.add((o, dataInput) -> ReflectionUtil.invokeMethod(method, o, dataInput));
                readMethod = true;
            }

            if (method.isAnnotationPresent(WriteMethod.class)) {
                write.add((o, dataOutput) -> ReflectionUtil.invokeMethod(method, o, dataOutput));
                writeMethod = true;
            }
        }
        if (!readMethod) {
            read.addAll(read1);
        }
        if (!writeMethod) {
            write.addAll(write1);
        }
    }

    protected boolean validField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) &&
                !Modifier.isTransient(field.getModifiers()) &&
                !field.isSynthetic();
    }

    protected <T> void handleField(Field field, List<BiConsumer<T, ObjectInput<C>>> readActions, List<BiConsumer<T, ObjectOutput<C>>> writeActions) {
        field.setAccessible(true);

        Custom custom = field.getAnnotation(Custom.class);
        if (custom != null) {
            if (!cache.containsKey(custom.value())) {
                cache.put(custom.value(), ReflectionUtil.instantiate(custom.value()));
            }
            Serializer customSerializer = cache.get(custom.value());
            readActions.add((object, input) -> {
                Object obj = customSerializer.instantiate(input);
                customSerializer.readObject(obj, input);
                ReflectionUtil.fieldSet(field, object, obj);
            });
            writeActions.add((object, output) -> customSerializer.writeObject(ReflectionUtil.fieldGet(field, object), output));
        } else {
            serializer(field.getType(),
                    object -> ReflectionUtil.fieldGet(field, object),
                    (obj, val) -> ReflectionUtil.fieldSet(field, obj, val.get()),
                    field::getAnnotation,
                    readActions,
                    writeActions);
        }
    }

    protected <T> void serializer(Class type,
                                  Function<T, Object> getter, BiConsumer<T, Supplier> setter,
                                  Function<Class<? extends Annotation>, Annotation> getAnnotation,
                                  List<BiConsumer<T, ObjectInput<C>>> read,
                                  List<BiConsumer<T, ObjectOutput<C>>> write) {
        if (type == Byte.TYPE || type == Byte.class) {
            read.add((object, dataInput) -> setter.accept(object, () -> (byte) dataInput.readUnsignedByte()));
            write.add((object, dataOutput) -> dataOutput.writeByte(((Byte) getter.apply(object))));
        } else if (type == Short.TYPE || type == Short.class) {
            read.add((object, dataInput) -> setter.accept(object, () -> (short) dataInput.readUnsignedShort()));
            write.add((object, dataOutput) -> dataOutput.writeShort(((Short) getter.apply(object))));
        } else if (type == Integer.TYPE || type == Integer.class) {
            if (getAnnotation.apply(Compact.class) != null) {
                read.add((object, dataInput) -> setter.accept(object, dataInput::readCompactInt));
                write.add((object, dataOutput) -> dataOutput.writeCompactInt(((Integer) getter.apply(object))));
            } else if (getAnnotation.apply(UShort.class) != null) {
                read.add((object, dataInput) -> setter.accept(object, dataInput::readUnsignedShort));
                write.add((object, dataOutput) -> dataOutput.writeShort(((Integer) getter.apply(object))));
            } else if (getAnnotation.apply(UByte.class) != null) {
                read.add((object, dataInput) -> setter.accept(object, dataInput::readUnsignedByte));
                write.add((object, dataOutput) -> dataOutput.writeByte(((Integer) getter.apply(object))));
            } else {
                read.add((object, dataInput) -> setter.accept(object, dataInput::readInt));
                write.add((object, dataOutput) -> dataOutput.writeInt(((Integer) getter.apply(object))));
            }
        } else if (type == Long.TYPE || type == Long.class) {
            read.add((object, dataInput) -> setter.accept(object, dataInput::readLong));
            write.add((object, dataOutput) -> dataOutput.writeLong(((Long) getter.apply(object))));
        } else if (type == Float.TYPE || type == Float.class) {
            read.add((object, dataInput) -> setter.accept(object, dataInput::readFloat));
            write.add((object, dataOutput) -> dataOutput.writeFloat(((Float) getter.apply(object))));
        } else if (type == String.class) {
            if (getAnnotation.apply(UTF.class) != null) {
                read.add((object, dataInput) -> setter.accept(object, dataInput::readUTF));
                write.add((object, dataOutput) -> dataOutput.writeUTF(((String) getter.apply(object))));
            } else {
                read.add((object, dataInput) -> setter.accept(object, dataInput::readLine));
                write.add((object, dataOutput) -> dataOutput.writeLine(((String) getter.apply(object))));
            }
        } else if (type.isArray()) {
            Class componentType = type.getComponentType();
            Length length = (Length) getAnnotation.apply(Length.class);
            Function<DataInput, Integer> lenReader;
            BiConsumer<DataOutput, Integer> lenWriter;
            if (length != null) {
                if (length.value() == Length.Type.BYTE) {
                    lenReader = DataInput::readUnsignedByte;
                    lenWriter = DataOutput::writeByte;
                } else if (length.value() == Length.Type.INT) {
                    lenReader = DataInput::readInt;
                    lenWriter = DataOutput::writeInt;
                } else if (length.value() == Length.Type.CONST) {
                    lenReader = input -> length.length();
                    lenWriter = (output, len) -> {};
                } else {
                    lenReader = DataInput::readCompactInt;
                    lenWriter = DataOutput::writeCompactInt;
                }
            } else {
                lenReader = DataInput::readCompactInt;
                lenWriter = DataOutput::writeCompactInt;
            }
            read.add((object, dataInput) -> {
                Object array = Array.newInstance(componentType, lenReader.apply(dataInput));
                for (int i = 0; i < Array.getLength(array); i++) {
                    int ind = i;
                    List<BiConsumer<Object, ObjectInput<C>>> arrayRead = new ArrayList<>();
                    List<BiConsumer<Object, ObjectOutput<C>>> arrayWrite = new ArrayList<>();
                    serializer(componentType, arr -> Array.get(arr, ind), (arr, val) -> Array.set(arr, ind, val.get()), getAnnotation, arrayRead, arrayWrite);
                    for (BiConsumer<Object, ObjectInput<C>> ra : arrayRead) {
                        ra.accept(array, dataInput);
                    }
                }
                setter.accept(object, () -> array);
            });
            write.add((object, dataOutput) -> {
                Object array = getter.apply(object);
                lenWriter.accept(dataOutput, Array.getLength(array));
                for (int i = 0; i < Array.getLength(array); i++) {
                    int ind = i;
                    List<BiConsumer<Object, ObjectInput<C>>> arrayRead = new ArrayList<>();
                    List<BiConsumer<Object, ObjectOutput<C>>> arrayWrite = new ArrayList<>();
                    serializer(componentType, arr -> Array.get(arr, ind), (arr, val) -> Array.set(arr, ind, val.get()), getAnnotation, arrayRead, arrayWrite);
                    for (BiConsumer<Object, ObjectOutput<C>> wa : arrayWrite) {
                        wa.accept(array, dataOutput);
                    }
                }
            });
        } else {
            read.add((object, dataInput) -> {
                Serializer typeSerializer = forClass(type);
                Object obj = typeSerializer.instantiate(dataInput);
                if (obj != null) {
                    Serializer realTypeSerializer = forClass(obj.getClass());
                    realTypeSerializer.readObject(obj, dataInput);
                }
                setter.accept(object, () -> obj);
            });
            write.add((object, dataOutput) -> {
                Object obj = getter.apply(object);
                Serializer realTypeSerializer = forClass(obj.getClass());
                realTypeSerializer.writeObject(obj, dataOutput);
            });
        }
    }

    protected class SerializerImpl implements Serializer<Object, C> {
        protected final Class<?> clazz;
        protected final List<BiConsumer<Object, ObjectInput<C>>> readActions;
        protected final List<BiConsumer<Object, ObjectOutput<C>>> writeActions;

        private Function<ObjectInput<C>, Object> instantiator;
        private BiConsumer<Object, ObjectInput<C>> reader;
        private BiConsumer<Object, ObjectOutput<C>> writer;

        public SerializerImpl(Class<?> clazz, List<BiConsumer<Object, ObjectInput<C>>> readActions, List<BiConsumer<Object, ObjectOutput<C>>> writeActions) {
            this.clazz = clazz;
            this.readActions = readActions;
            this.writeActions = writeActions;
        }

        @Override
        public Object instantiate(ObjectInput<C> input) throws UncheckedIOException {
            if (instantiator == null) {
                instantiator = createInstantiator(clazz);
            }
            return instantiator.apply(input);
        }

        @Override
        public <S> void readObject(S obj, ObjectInput<C> input) throws UncheckedIOException {
            if (obj == null) {
                return;
            }
            if (reader == null) {
                reader = createReader(clazz, readActions);
            }
            reader.accept(obj, input);
        }

        @Override
        public <S> void writeObject(S obj, ObjectOutput<C> output) throws UncheckedIOException {
            if (writer == null) {
                writer = createWriter(clazz, writeActions);
            }
            writer.accept(obj, output);
        }

        @Override
        public String toString() {
            return "Serializer[" + clazz + "]";
        }
    }
}
