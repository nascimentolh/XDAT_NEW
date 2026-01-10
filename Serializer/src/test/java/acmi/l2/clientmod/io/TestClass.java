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

import acmi.l2.clientmod.io.annotation.Custom;
import acmi.l2.clientmod.io.annotation.Length;
import acmi.l2.clientmod.io.annotation.ReadMethod;
import acmi.l2.clientmod.io.annotation.UShort;

import java.io.UncheckedIOException;
import java.util.Arrays;

public class TestClass {
    @UShort
    @Length(Length.Type.COMPACT)
    public int[] foo;
    @Custom(StringSerializer.class)
    public String bar;
    @Custom(InnerClass.InnerClassSerializer.class)
    public InnerClass baz;

    public static class InnerClass{
        public int field1;

        public InnerClass() {
        }

        public InnerClass(int field1) {
            this.field1 = field1;
        }

        @ReadMethod
        public void read(ObjectInput input){
            field1 = input.readInt();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InnerClass that = (InnerClass) o;

            return field1 == that.field1;

        }

        @Override
        public int hashCode() {
            return field1;
        }

        public static class InnerClassSerializer implements Serializer<InnerClass, Context>{
            @Override
            public InnerClass instantiate(ObjectInput<Context> input) throws UncheckedIOException {
                switch (input.readUnsignedByte()){
                    case 1:
                        return new InnerClassExtends();
                    default:
                        return new InnerClass();
                }
            }

            @Override
            public <S extends InnerClass> void readObject(S obj, ObjectInput<Context> input) throws UncheckedIOException {
                Serializer serializer = input.getSerializerFactory().forClass(obj.getClass());
                serializer.readObject(obj, input);
            }

            @Override
            public <S extends InnerClass> void writeObject(S obj, ObjectOutput<Context> output) throws UncheckedIOException {
                if (obj instanceof InnerClassExtends){
                    output.writeByte(1);
                }else{
                    output.writeByte(0);
                }
                output.write(obj);
            }
        }
    }

    public static class InnerClassExtends extends InnerClass{
        public int field2;

        public InnerClassExtends() {
        }

        public InnerClassExtends(int field1, int field2) {
            super(field1);
            this.field2 = field2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InnerClassExtends)) return false;
            if (!super.equals(o)) return false;

            InnerClassExtends that = (InnerClassExtends) o;

            return field2 == that.field2;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + field2;
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestClass testClass = (TestClass) o;

        if (!Arrays.equals(foo, testClass.foo)) return false;
        if (bar != null ? !bar.equals(testClass.bar) : testClass.bar != null) return false;
        return baz != null ? baz.equals(testClass.baz) : testClass.baz == null;

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(foo);
        result = 31 * result + (bar != null ? bar.hashCode() : 0);
        result = 31 * result + (baz != null ? baz.hashCode() : 0);
        return result;
    }
}
