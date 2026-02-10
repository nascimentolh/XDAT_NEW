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
package acmi.l2.clientmod.xdat;

import acmi.l2.clientmod.l2resources.Sysstr;
import acmi.l2.clientmod.l2resources.Tex;
import acmi.l2.clientmod.util.Description;
import acmi.l2.clientmod.util.Hide;
import acmi.l2.clientmod.xdat.history.PropertyChangeCommand;
import acmi.l2.clientmod.xdat.propertyeditor.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

class PropertySheetManager {
    private static Map<Class, List<PropertySheetItem>> propertyCache = new HashMap<>();

    private final XdatEditor editor;

    PropertySheetManager(XdatEditor editor) {
        this.editor = editor;
    }

    PropertySheet createPropertySheet(TreeView<Object> elements, Function<TreeItem, String> scriptStringFn) {
        PropertySheet properties = new PropertySheet();
        properties.setSkin(new PropertySheetSkin(properties));

        elements.getSelectionModel().selectedItemProperty().addListener((selected, oldValue, newSelection) -> {
            properties.getItems().clear();

            if (newSelection == null)
                return;

            Object obj = newSelection.getValue();

            if (obj instanceof ListHolder)
                return;

            if (!propertyCache.containsKey(obj.getClass())) {
                propertyCache.put(obj.getClass(), loadProperties(obj));
            }
            List<PropertySheetItem> props = propertyCache.get(obj.getClass());
            props.forEach(property -> {
                property.setObject(obj);
                ChangeListener<Object> addToHistory = (observable1, oldValue1, newValue1) -> {
                    // Skip if UndoManager is executing a command (undo/redo in progress)
                    if (editor.getUndoManager().isExecutingCommand()) {
                        return;
                    }

                    String objName = scriptStringFn.apply(newSelection);
                    String propName = ((PropertySheetItem)observable1).getName();
                    if ("name".equals(propName) || "wnd".equals(propName)){
                        StringBuilder b = new StringBuilder(objName);
                        String nv = String.valueOf(newValue1);
                        int ind = objName.lastIndexOf(nv);
                        b.replace(ind, ind + nv.length(), String.valueOf(oldValue1) );
                        objName = b.toString();
                    }
                    editor.getHistory().valueChanged(objName, property.getName(), newValue1, property.hashCode());

                    // Record property change command for undo/redo
                    if (property instanceof FieldProperty) {
                        FieldProperty fieldProperty = (FieldProperty) property;
                        String elementName = obj.toString();
                        PropertyChangeCommand command = new PropertyChangeCommand(
                                obj, fieldProperty.getField(), propName, oldValue1, newValue1, elementName);
                        editor.getUndoManager().record(command);
                    }
                };
                property.addListener(addToHistory);

                selected.addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        property.removeListener(addToHistory);
                        observable.removeListener(this);
                    }
                });
            });
            properties.getItems().setAll(props);
        });

        return properties;
    }

    private static List<PropertySheetItem> loadProperties(Object obj) {
        Class<?> objClass = obj.getClass();
        List<PropertySheetItem> list = new ArrayList<>();
        while (objClass != Object.class) {
            for (Field field : objClass.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers()))
                    continue;

                if (Collection.class.isAssignableFrom(field.getType()))
                    continue;

                if (field.isAnnotationPresent(Hide.class))
                    continue;

                String description = "";
                if (field.isAnnotationPresent(Description.class))
                    description = field.getAnnotation(Description.class).value();
                Class<? extends PropertyEditor<?>> propertyEditorClass = null;
                if (field.getType() == Boolean.class ||
                        field.getType() == Boolean.TYPE) {
                    propertyEditorClass = BooleanPropertyEditor.class;
                } else if (field.isAnnotationPresent(Tex.class)) {
                    propertyEditorClass = TexturePropertyEditor.class;
                } else if (field.isAnnotationPresent(Sysstr.class)) {
                    propertyEditorClass = SysstringPropertyEditor.class;
                }
                field.setAccessible(true);
                PropertySheetItem property = new FieldProperty(field, objClass.getSimpleName(), description, propertyEditorClass);
                list.add(property);
            }
            objClass = objClass.getSuperclass();
        }
        return list;
    }
}
