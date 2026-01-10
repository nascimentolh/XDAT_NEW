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

import java.io.UncheckedIOException;
import java.nio.charset.Charset;

public interface ObjectInput<C extends Context> extends DataInput {
    SerializerFactory<C> getSerializerFactory();

    C getContext();

    default <T> T readObject(Class<T> clazz) throws UncheckedIOException {
        if (getSerializerFactory() == null)
            throw new IllegalStateException("IOFactory is null");

        Serializer<T, C> serializer = getSerializerFactory().forClass(clazz);
        T obj = serializer.instantiate(this);
        if (obj != null) {
            Serializer s = getSerializerFactory().forClass(obj.getClass());
            //noinspection unchecked
            s.readObject(obj, this);
        }
        return obj;
    }

    static <C extends Context> ObjectInput<C> objectInput(DataInput dataInput, SerializerFactory<C> serializerFactory, C context) {
        return new ObjectInput<C>() {
            @Override
            public SerializerFactory<C> getSerializerFactory() {
                return serializerFactory;
            }

            @Override
            public C getContext() {
                return context;
            }

            @Override
            public int readUnsignedByte() throws UncheckedIOException {
                return dataInput.readUnsignedByte();
            }

            @Override
            public void readFully(byte[] b, int off, int len) throws UncheckedIOException {
                dataInput.readFully(b, off, len);
            }

            @Override
            public Charset getCharset() {
                return dataInput.getCharset();
            }

            @Override
            public int getPosition() throws UncheckedIOException {
                return dataInput.getPosition();
            }
        };
    }
}
