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

import java.io.InputStream;
import java.nio.charset.Charset;

public class ObjectInputStream<T extends Context> extends DataInputStream implements ObjectInput<T> {
    private final SerializerFactory<T> serializerFactory;
    private final T context;

    public ObjectInputStream(InputStream in, Charset charset, SerializerFactory<T> serializerFactory, T context) {
        this(in, charset, 0, serializerFactory, context);
    }

    public ObjectInputStream(InputStream in, Charset charset, int position, SerializerFactory<T> serializerFactory, T context) {
        super(in, charset, position);
        this.serializerFactory = serializerFactory;
        this.context = context;
    }

    @Override
    public SerializerFactory<T> getSerializerFactory() {
        return serializerFactory;
    }

    @Override
    public T getContext() {
        return context;
    }
}
