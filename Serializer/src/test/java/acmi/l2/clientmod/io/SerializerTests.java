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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

public class SerializerTests {
    @Test
    public void deserializationTest() {
        TestClass testObject = new TestClass();
        testObject.foo = new int[]{0, 123};
        testObject.bar = "test";
        testObject.baz = new TestClass.InnerClassExtends(10, 20);

        Context context = null;
        SerializerFactory<Context> serializerFactory = new ReflectionSerializerFactory<>();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput objectOutput = new ObjectOutputStream<>(baos, UnrealPackage.getDefaultCharset(), serializerFactory, context);
        objectOutput.write(testObject);

        ObjectInput objectInput = new ObjectInputStream<>(new ByteArrayInputStream(baos.toByteArray()), UnrealPackage.getDefaultCharset(), serializerFactory, context);
        TestClass deserialized = (TestClass) objectInput.readObject(TestClass.class);

        assertEquals(testObject, deserialized);
    }
}
