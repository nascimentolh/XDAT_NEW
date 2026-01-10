/*
 * Copyright (c) 2016 acmi
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
package acmi.l2.clientmod.xdat.history;

import java.lang.reflect.Field;

/**
 * Command for property value changes.
 * Stores the old and new values to allow undo/redo.
 */
public class PropertyChangeCommand implements Command {

    private final Object target;
    private final Field field;
    private final String propertyName;
    private final Object oldValue;
    private final Object newValue;
    private final String targetName;
    private final long timestamp;

    public PropertyChangeCommand(Object target, Field field, String propertyName,
                                  Object oldValue, Object newValue, String targetName) {
        this.target = target;
        this.field = field;
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.targetName = targetName;
        this.timestamp = System.currentTimeMillis();

        field.setAccessible(true);
    }

    @Override
    public void execute() {
        try {
            field.set(target, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to execute property change", e);
        }
    }

    @Override
    public void undo() {
        try {
            field.set(target, oldValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to undo property change", e);
        }
    }

    @Override
    public String getDescription() {
        String oldStr = formatValue(oldValue);
        String newStr = formatValue(newValue);
        return String.format("%s.%s: %s → %s", targetName, propertyName, oldStr, newStr);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        String str = value.toString();
        if (str.length() > 30) {
            return str.substring(0, 27) + "...";
        }
        return str;
    }

    public Object getTarget() {
        return target;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
