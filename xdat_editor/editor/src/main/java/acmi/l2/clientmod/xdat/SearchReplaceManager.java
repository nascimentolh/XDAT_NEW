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

import acmi.l2.clientmod.util.IOEntity;
import acmi.l2.clientmod.xdat.history.BatchReplaceCommand;
import acmi.l2.clientmod.xdat.search.SearchPanel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

class SearchReplaceManager {
    private static final Logger log = Logger.getLogger(SearchReplaceManager.class.getName());

    private final XdatEditor editor;
    private final ResourceBundle resources;

    SearchReplaceManager(XdatEditor editor, ResourceBundle resources) {
        this.editor = editor;
        this.resources = resources;
    }

    void replaceSelected(TreeView<Object> treeView, SearchPanel searchPanel) {
        String searchText = searchPanel.getSearchText();
        String replaceText = searchPanel.getReplaceText();
        String propertyFilter = searchPanel.getSelectedProperty();

        if (searchText.isEmpty()) {
            searchPanel.setReplaceStatus(resources.getString("search.no_matches"));
            return;
        }

        TreeItem<Object> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() instanceof ListHolder) {
            searchPanel.setReplaceStatus(resources.getString("search.no_matches"));
            return;
        }

        Object item = selected.getValue();
        int count = replaceInObject(item, searchText, replaceText, propertyFilter, searchPanel.isUseRegex());

        if (count > 0) {
            searchPanel.setReplaceStatus(String.format(resources.getString("search.replaced"), count));
            // Refresh tree view
            treeView.refresh();
        } else {
            searchPanel.setReplaceStatus(resources.getString("search.no_matches"));
        }
    }

    void replaceAll(Field listField, SearchPanel searchPanel, TreeView<Object> treeView) {
        String searchText = searchPanel.getSearchText();
        String replaceText = searchPanel.getReplaceText();
        String propertyFilter = searchPanel.getSelectedProperty();
        String typeFilter = searchPanel.getSelectedType();

        if (searchText.isEmpty()) {
            searchPanel.setReplaceStatus(resources.getString("search.no_matches"));
            return;
        }

        IOEntity xdatObject = editor.xdatObjectProperty().get();
        if (xdatObject == null) {
            return;
        }

        BatchReplaceCommand batchCommand = new BatchReplaceCommand(searchText, replaceText);

        try {
            @SuppressWarnings("unchecked")
            List<IOEntity> list = (List<IOEntity>) listField.get(xdatObject);

            for (IOEntity entity : list) {
                replaceInEntityRecursive(entity, searchText, replaceText, propertyFilter, typeFilter,
                        searchPanel.isUseRegex(), batchCommand);
            }

            if (batchCommand.hasReplacements()) {
                // Execute and record for undo
                batchCommand.execute();
                editor.getUndoManager().record(batchCommand);

                searchPanel.setReplaceStatus(String.format(
                        resources.getString("search.replaced"),
                        batchCommand.getReplacementCount()));

                // Refresh the tree view
                if (treeView != null) {
                    treeView.refresh();
                }
            } else {
                searchPanel.setReplaceStatus(resources.getString("search.no_matches"));
            }

        } catch (IllegalAccessException e) {
            log.log(Level.WARNING, "Failed to access list field", e);
        }
    }

    private int replaceInObject(Object item, String searchText, String replaceText,
                                String propertyFilter, boolean useRegex) {
        int count = 0;
        String[] propsToCheck;

        if (propertyFilter != null && !propertyFilter.isEmpty()) {
            propsToCheck = new String[]{propertyFilter};
        } else {
            propsToCheck = new String[]{"name", "text", "buttonNameText", "titleText", "file",
                    "normalTex", "backTex", "fontName", "styleName"};
        }

        BatchReplaceCommand batchCommand = new BatchReplaceCommand(searchText, replaceText);

        for (String propName : propsToCheck) {
            try {
                Field field = findField(item.getClass(), propName);
                if (field != null && field.getType() == String.class) {
                    field.setAccessible(true);
                    String value = (String) field.get(item);
                    if (value != null) {
                        String newValue = performReplace(value, searchText, replaceText, useRegex);
                        if (!newValue.equals(value)) {
                            batchCommand.addReplacement(item, field, value, newValue);
                            count++;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (batchCommand.hasReplacements()) {
            batchCommand.execute();
            editor.getUndoManager().record(batchCommand);
        }

        return count;
    }

    private void replaceInEntityRecursive(Object entity, String searchText, String replaceText,
                                          String propertyFilter, String typeFilter, boolean useRegex,
                                          BatchReplaceCommand batchCommand) {
        if (entity == null) {
            return;
        }

        // Check type filter
        if (typeFilter != null && !typeFilter.isEmpty()) {
            if (!entity.getClass().getSimpleName().equals(typeFilter)) {
                // Still process children
                processChildEntities(entity, searchText, replaceText, propertyFilter, typeFilter, useRegex, batchCommand);
                return;
            }
        }

        // Process properties of this entity
        String[] propsToCheck;
        if (propertyFilter != null && !propertyFilter.isEmpty()) {
            propsToCheck = new String[]{propertyFilter};
        } else {
            propsToCheck = new String[]{"name", "text", "buttonNameText", "titleText", "file",
                    "normalTex", "backTex", "fontName", "styleName"};
        }

        for (String propName : propsToCheck) {
            try {
                Field field = findField(entity.getClass(), propName);
                if (field != null && field.getType() == String.class) {
                    field.setAccessible(true);
                    String value = (String) field.get(entity);
                    if (value != null) {
                        String newValue = performReplace(value, searchText, replaceText, useRegex);
                        if (!newValue.equals(value)) {
                            batchCommand.addReplacement(entity, field, value, newValue);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Process child entities
        processChildEntities(entity, searchText, replaceText, propertyFilter, typeFilter, useRegex, batchCommand);
    }

    private void processChildEntities(Object entity, String searchText, String replaceText,
                                      String propertyFilter, String typeFilter, boolean useRegex,
                                      BatchReplaceCommand batchCommand) {
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    if (value instanceof List) {
                        for (Object item : (List<?>) value) {
                            if (item instanceof IOEntity) {
                                replaceInEntityRecursive(item, searchText, replaceText,
                                        propertyFilter, typeFilter, useRegex, batchCommand);
                            }
                        }
                    } else if (value instanceof IOEntity) {
                        replaceInEntityRecursive(value, searchText, replaceText,
                                propertyFilter, typeFilter, useRegex, batchCommand);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private String performReplace(String value, String searchText, String replaceText, boolean useRegex) {
        if (useRegex) {
            try {
                return value.replaceAll(searchText, replaceText);
            } catch (Exception e) {
                return value;
            }
        } else {
            return value.replace(searchText, replaceText);
        }
    }
}
