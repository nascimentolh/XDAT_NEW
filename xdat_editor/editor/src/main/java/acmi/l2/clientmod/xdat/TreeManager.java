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

import acmi.l2.clientmod.l2resources.L2Context;
import acmi.l2.clientmod.l2resources.L2Resources;
import acmi.l2.clientmod.util.*;
import acmi.l2.clientmod.xdat.search.SearchCriteria;
import acmi.l2.clientmod.xdat.search.SearchPanel;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.controlsfx.control.PropertySheet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TreeManager {
    private static final Logger log = Logger.getLogger(TreeManager.class.getName());

    private static final Map<String, String> UI_NODE_ICONS = new HashMap<>();
    private static final String UI_NODE_ICON_DEFAULT = "MissingIcon.png";

    static {
        UI_NODE_ICONS.put("BarCtrl", "ToggleButton.png");
        UI_NODE_ICONS.put("Button", "Button.png");
        UI_NODE_ICONS.put("CharacterViewportWindow", "MissingIcon.png");
        UI_NODE_ICONS.put("ChatWindow", "MissingIcon.png");
        UI_NODE_ICONS.put("CheckBox", "CheckBox.png");
        UI_NODE_ICONS.put("ComboBox", "ComboBox.png");
        UI_NODE_ICONS.put("DrawPanel", "Canvas.png");
        UI_NODE_ICONS.put("EditBox", "TextField.png");
        UI_NODE_ICONS.put("EffectButton", "Button.png");
        UI_NODE_ICONS.put("FishViewportWindow", "MissingIcon.png");
        UI_NODE_ICONS.put("FlashCtrl", "MissingIcon.png");
        UI_NODE_ICONS.put("HtmlCtrl", "HTMLEditor.png");
        UI_NODE_ICONS.put("InvenWeight", "ToggleButton.png");
        UI_NODE_ICONS.put("ItemWindow", "GridPane.png");
        UI_NODE_ICONS.put("ListBox", "MissingIcon.png");
        UI_NODE_ICONS.put("ListCtrl", "ListView.png");
        UI_NODE_ICONS.put("MinimapCtrl", "MissingIcon.png");
        UI_NODE_ICONS.put("MultiEdit", "MissingIcon.png");
        UI_NODE_ICONS.put("MultiSellItemInfo", "MissingIcon.png");
        UI_NODE_ICONS.put("MultiSellNeededItem", "MissingIcon.png");
        UI_NODE_ICONS.put("NameCtrl", "MissingIcon.png");
        UI_NODE_ICONS.put("Progress", "ProgressBar.png");
        UI_NODE_ICONS.put("PropertyController", "MissingIcon.png");
        UI_NODE_ICONS.put("Radar", "MissingIcon.png");
        UI_NODE_ICONS.put("RadarMapCtrl", "MissingIcon.png");
        UI_NODE_ICONS.put("RadioButton", "RadioButton.png");
        UI_NODE_ICONS.put("ScrollArea", "ScrollPane.png");
        UI_NODE_ICONS.put("ShortcutItemWindow", "MissingIcon.png");
        UI_NODE_ICONS.put("SliderCtrl", "Slider-h.png");
        UI_NODE_ICONS.put("StatusBar", "ToggleButton.png");
        UI_NODE_ICONS.put("StatusIconCtrl", "MissingIcon.png");
        UI_NODE_ICONS.put("Tab", "TabPane.png");
        UI_NODE_ICONS.put("TextBox", "Label.png");
        UI_NODE_ICONS.put("TextListBox", "Label.png");
        UI_NODE_ICONS.put("Texture", "Graphic.png");
        UI_NODE_ICONS.put("TreeCtrl", "TreeView.png");
        UI_NODE_ICONS.put("WebBrowserCtrl", "WebView.png");
        UI_NODE_ICONS.put("Window", "TitledPane.png");
    }

    private final XdatEditor editor;
    private final ResourceBundle resources;
    private final DragDropHandler dragDropHandler;
    private final PropertySheetManager propertySheetManager;
    private final List<InvalidationListener> xdatListeners = new ArrayList<>();
    private final ObjectProperty<L2Resources> l2resourcesProperty;

    TreeManager(XdatEditor editor, ResourceBundle resources, ObjectProperty<L2Resources> l2resourcesProperty) {
        this.editor = editor;
        this.resources = resources;
        this.l2resourcesProperty = l2resourcesProperty;
        this.dragDropHandler = new DragDropHandler(editor.getUndoManager());
        this.propertySheetManager = new PropertySheetManager(editor);
    }

    Tab createTab(Field listField, SearchPanel searchPanel, Runnable onReplaceSelected, Runnable onReplaceAll) {
        Tab tab = new Tab(listField.getName());

        SplitPane pane = new SplitPane();

        // Configure search panel
        searchPanel.setComponentTypes(new ArrayList<>(UI_NODE_ICONS.keySet()));
        searchPanel.setPropertyNames(Arrays.asList(
                "name", "text", "buttonNameText", "titleText", "file",
                "normalTex", "backTex", "fontName", "styleName"
        ));

        TreeView<Object> elements = createTreeView(listField, searchPanel);
        VBox.setVgrow(elements, Priority.ALWAYS);
        PropertySheet properties = propertySheetManager.createPropertySheet(elements, this::treeItemToScriptString);

        // Setup replace handlers
        searchPanel.setOnReplace(onReplaceSelected);
        searchPanel.setOnReplaceAll(onReplaceAll);

        pane.getItems().addAll(new VBox(searchPanel, elements), properties);
        pane.setDividerPositions(0.3);

        tab.setContent(wrap(pane));

        return tab;
    }

    void installXdatListener(InvalidationListener listener) {
        xdatListeners.add(listener);
    }

    static TreeItem<Object> createTreeItem(IOEntity o) {
        TreeItem<Object> item = new TreeItem<>(o);

        List<Field> fields = new ArrayList<>();
        Class<?> clazz = o.getClass();
        while (clazz != Object.class) {
            Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> !field.isSynthetic())
                    .filter(field -> List.class.isAssignableFrom(field.getType()) ||
                            IOEntity.class.isAssignableFrom(field.getType()))
                    .forEach(fields::add);
            clazz = clazz.getSuperclass();
        }
        fields.forEach(field -> {
            field.setAccessible(true);

            Optional<Object> obj = Optional.empty();
            try {
                obj = Optional.ofNullable(field.get(o));
            } catch (IllegalAccessException e) {
                String msg = String.format("%s.%s is not accessible", o.getClass(), field.getName());
                log.log(Level.WARNING, msg, e);
                Dialogs.showException(Alert.AlertType.ERROR, "ReflectiveOperationException", msg, e);
            }

            obj.ifPresent(val -> {
                if (List.class.isAssignableFrom(field.getType())) {
                    if (!field.isAnnotationPresent(Type.class)) {
                        String msg = String.format("%s.%s: @Type not defined", o.getClass().getName(), field.getName());
                        log.log(Level.WARNING, msg);
                        Dialogs.showException(Alert.AlertType.ERROR, "ReflectiveOperationException", msg, null);
                    } else {
                        @SuppressWarnings("unchecked")
                        List<IOEntity> list = (List<IOEntity>) val;
                        Class<? extends IOEntity> type = field.getAnnotation(Type.class).value().asSubclass(IOEntity.class);
                        TreeItem<Object> listItem = new TreeItem<>(new ListHolder(o, list, field.getName(), type));

                        item.getChildren().add(listItem);

                        listItem.getChildren().addAll(list.stream()
                                .map(TreeManager::createTreeItem)
                                .collect(Collectors.toList()));
                    }
                } else if (IOEntity.class.isAssignableFrom(field.getType())) {
                    IOEntity ioEntity = (IOEntity) val;

                    item.getChildren().add(createTreeItem(ioEntity));
                }
            });
        });
        return item;
    }

    static int buildTree(IOEntity entity, Field listField, TreeView<Object> elements, SearchCriteria criteria) {
        elements.setRoot(null);

        if (entity == null)
            return 0;

        int[] resultCount = {0};

        try {
            @SuppressWarnings("unchecked")
            List<IOEntity> list = (List<IOEntity>) listField.get(entity);
            if (!listField.isAnnotationPresent(Type.class)) {
                String msg = String.format("XDAT.%s: @Type not defined", listField.getName());
                log.log(Level.WARNING, msg);
                Dialogs.showException(Alert.AlertType.ERROR, "ReflectiveOperationException", msg, null);
            } else {
                Class<? extends IOEntity> type = listField.getAnnotation(Type.class).value().asSubclass(IOEntity.class);
                TreeItem<Object> rootItem = new TreeItem<>(new ListHolder(entity, list, listField.getName(), type));

                elements.setRoot(rootItem);

                List<TreeItem<Object>> filteredItems = list.stream()
                        .map(TreeManager::createTreeItem)
                        .filter(treeItem -> checkTreeNode(treeItem, criteria, resultCount))
                        .collect(Collectors.toList());

                rootItem.getChildren().addAll(filteredItems);
            }
        } catch (IllegalAccessException e) {
            String msg = String.format("%s.%s is not accessible", listField.getDeclaringClass().getSimpleName(), listField.getName());
            log.log(Level.WARNING, msg, e);
            Dialogs.showException(Alert.AlertType.ERROR, "ReflectiveOperationException", msg, e);
        }

        return resultCount[0];
    }

    static boolean checkTreeNode(TreeItem<Object> treeItem, SearchCriteria criteria, int[] resultCount) {
        Object value = treeItem.getValue();

        // If no active filters, show all
        if (criteria.isEmpty()) {
            return true;
        }

        // Check if this item matches
        if (criteria.matches(value)) {
            resultCount[0]++;
            return true;
        }

        // Check if any child matches
        for (TreeItem<Object> childItem : treeItem.getChildren()) {
            if (checkTreeNode(childItem, criteria, resultCount)) {
                return true;
            }
        }

        return false;
    }

    String treeItemToScriptString(TreeItem item) {
        List<TreeItem> list = new ArrayList<>();
        do {
            list.add(item);
        } while ((item = item.getParent()) != null);
        Collections.reverse(list);

        StringBuilder sb = new StringBuilder("xdat");
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i).getValue();
            if (value instanceof ListHolder) {
                ListHolder holder = (ListHolder) list.get(i).getValue();
                sb.append('.').append(holder.name);
                if (i + 1 < list.size()) {
                    sb.append('[');
                    Object obj = list.get(++i).getValue();
                    if (obj instanceof Named) {
                        sb.append('"').append(((Named) obj).getName()).append('"');
                    } else {
                        //noinspection SuspiciousMethodCalls
                        sb.append(holder.list.indexOf(obj));
                    }
                    sb.append(']');
                }
            }
        }
        return sb.toString();
    }

    private TreeView<Object> createTreeView(Field listField, SearchPanel searchPanel) {
        TreeView<Object> elements = new TreeView<>();

        // Setup cell factory with both highlight and drag-and-drop support
        setupTreeViewCellFactory(elements, searchPanel);

        elements.setShowRoot(false);
        elements.setContextMenu(createContextMenu(elements));

        // Create invalidation listener that rebuilds tree with search criteria
        InvalidationListener treeInvalidation = (observable) -> {
            SearchCriteria criteria = new SearchCriteria(
                    searchPanel.getSearchText(),
                    searchPanel.getSelectedType(),
                    searchPanel.getSelectedProperty(),
                    searchPanel.isUseRegex()
            );
            int resultCount = buildTree(editor.xdatObjectProperty().get(), listField, elements, criteria);
            searchPanel.setResultCount(resultCount);
        };

        editor.xdatObjectProperty().addListener(treeInvalidation);
        xdatListeners.add(treeInvalidation);

        // Listen to all search panel properties
        searchPanel.searchTextProperty().addListener(treeInvalidation);
        searchPanel.selectedTypeProperty().addListener(treeInvalidation);
        searchPanel.selectedPropertyProperty().addListener(treeInvalidation);
        searchPanel.useRegexProperty().addListener(treeInvalidation);

        return elements;
    }

    private void setupTreeViewCellFactory(TreeView<Object> treeView, SearchPanel searchPanel) {
        treeView.setCellFactory(tv -> {
            TreeCell<Object> cell = new TreeCell<Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);

                    // Remove all dynamic style classes
                    getStyleClass().removeAll("drag-over-top", "drag-over-bottom", "drag-over",
                            "search-match", "search-match-parent");

                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.toString());
                        if (UIEntity.class.isAssignableFrom(item.getClass())) {
                            try (InputStream is = getClass().getResourceAsStream("/acmi/l2/clientmod/xdat/nodeicons/" +
                                    UI_NODE_ICONS.getOrDefault(item.getClass().getSimpleName(), UI_NODE_ICON_DEFAULT))) {
                                setGraphic(new ImageView(new Image(is)));
                            } catch (IOException ignore) {}
                        }

                        // Apply highlight if item matches search criteria
                        if (searchPanel.hasActiveFilters()) {
                            SearchCriteria criteria = new SearchCriteria(
                                    searchPanel.getSearchText(),
                                    searchPanel.getSelectedType(),
                                    searchPanel.getSelectedProperty(),
                                    searchPanel.isUseRegex()
                            );
                            if (criteria.matches(item)) {
                                getStyleClass().add("search-match");
                            } else if (getTreeItem() != null && hasMatchingDescendant(getTreeItem(), criteria)) {
                                getStyleClass().add("search-match-parent");
                            }
                        }
                    }
                }

                private boolean hasMatchingDescendant(TreeItem<Object> treeItem, SearchCriteria criteria) {
                    for (TreeItem<Object> child : treeItem.getChildren()) {
                        if (criteria.matches(child.getValue())) {
                            return true;
                        }
                        if (hasMatchingDescendant(child, criteria)) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            // Install drag-and-drop handler
            dragDropHandler.install(cell, treeView);

            return cell;
        });
    }

    private ContextMenu createContextMenu(TreeView<Object> elements) {
        ContextMenu contextMenu = new ContextMenu();
        InvalidationListener il = observable1 -> updateContextMenu(contextMenu, elements);
        elements.rootProperty().addListener(il);
        elements.getSelectionModel().selectedItemProperty().addListener(il);
        return contextMenu;
    }

    private void updateContextMenu(ContextMenu contextMenu, TreeView<Object> elements) {
        contextMenu.getItems().clear();

        TreeItem<Object> root = elements.getRoot();
        TreeItem<Object> selected = elements.getSelectionModel().getSelectedItem();

        if (selected == null) {
            if (root != null)
                contextMenu.getItems().add(createAddMenu("Add ..", elements, root));
        } else {
            Object value = selected.getValue();
            if (value instanceof ListHolder) {
                contextMenu.getItems().add(createAddMenu("Add ..", elements, selected));
            } else if (selected.getParent() != null && selected.getParent().getValue() instanceof ListHolder) {
                MenuItem add = createAddMenu("Add to parent ..", elements, selected.getParent());

                MenuItem delete = new MenuItem("Delete");
                delete.setOnAction(event -> {
                    ListHolder parent = (ListHolder) selected.getParent().getValue();

                    @SuppressWarnings("SuspiciousMethodCalls")
                    int index = parent.list.indexOf(value);
                    if (value instanceof Named) {
                        editor.getHistory().valueRemoved(treeItemToScriptString(selected.getParent()), ((Named) value).getName());
                    } else {
                        editor.getHistory().valueRemoved(treeItemToScriptString(selected.getParent()), index);
                    }

                    parent.list.remove(index);
                    selected.getParent().getChildren().remove(selected);

                    elements.getSelectionModel().selectPrevious();
                    elements.getSelectionModel().selectNext();
                });
                contextMenu.getItems().addAll(add, delete);
            }
            if (value instanceof ComponentFactory) {
                MenuItem view = new MenuItem("View");
                view.setOnAction(event -> {
                    if (value instanceof L2Context)
                        ((L2Context) value).setResources(l2resourcesProperty.getValue());
                    Stage stage = new Stage();
                    stage.setTitle(value.toString());
                    Scene scene = new Scene(((ComponentFactory) value).getComponent());
                    String currentTheme = XdatEditor.getPrefs().get("theme", "dark");
                    String cssPath = "light".equals(currentTheme)
                            ? getClass().getResource("light-theme.css").toExternalForm()
                            : getClass().getResource("dark-theme.css").toExternalForm();
                    scene.getStylesheets().add(cssPath);
                    stage.setScene(scene);
                    stage.show();
                });
                contextMenu.getItems().add(view);
            }
        }
    }

    private MenuItem createAddMenu(String name, TreeView<Object> elements, TreeItem<Object> selected) {
        ListHolder listHolder = (ListHolder) selected.getValue();

        MenuItem add = new MenuItem(name);
        add.setOnAction(event -> {
            Stream<ClassHolder> st = SubclassManager.getInstance()
                    .getClassWithAllSubclasses(listHolder.type)
                    .stream()
                    .map(ClassHolder::new);
            List<ClassHolder> list = st
                    .collect(Collectors.toList());

            Optional<ClassHolder> choice;

            if (list.size() == 1) {
                choice = Optional.of(list.get(0));
            } else {
                ChoiceDialog<ClassHolder> cd = new ChoiceDialog<>(list.get(0), list);
                cd.setTitle("Select class");
                cd.setHeaderText(null);
                choice = cd.showAndWait();
            }
            choice.ifPresent(toCreate -> {
                try {
                    IOEntity obj = toCreate.clazz.newInstance();

                    listHolder.list.add(obj);
                    TreeItem<Object> treeItem = createTreeItem(obj);
                    selected.getChildren().add(treeItem);
                    elements.getSelectionModel().select(treeItem);
                    elements.scrollTo(elements.getSelectionModel().getSelectedIndex());

                    editor.getHistory().valueCreated(treeItemToScriptString(selected), toCreate.clazz);
                } catch (ReflectiveOperationException e) {
                    String msg = String.format("Couldn't instantiate %s", toCreate.clazz.getName());
                    log.log(Level.WARNING, msg, e);
                    Dialogs.showException(Alert.AlertType.ERROR, "ReflectiveOperationException", msg, e);
                }
            });
        });

        return add;
    }

    private ScrollPane wrap(Region region) {
        ScrollPane scrollPane = new ScrollPane(region);
        scrollPane.setPadding(new Insets(0));
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
}
