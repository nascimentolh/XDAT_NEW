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

import acmi.l2.clientmod.crypt.L2Crypt;
import acmi.l2.clientmod.l2resources.L2Context;
import acmi.l2.clientmod.l2resources.L2Resources;
import acmi.l2.clientmod.l2resources.Sysstr;
import acmi.l2.clientmod.l2resources.Tex;
import acmi.l2.clientmod.unreal.Environment;
import acmi.l2.clientmod.util.*;
import acmi.l2.clientmod.xdat.history.*;
import acmi.l2.clientmod.xdat.propertyeditor.*;
import acmi.l2.clientmod.xdat.search.SearchCriteria;
import acmi.l2.clientmod.xdat.search.SearchPanel;
import acmi.l2.clientmod.xdat.util.ClipboardManager;
import acmi.l2.clientmod.xdat.util.ElementCloner;
import acmi.l2.clientmod.xdat.util.RecentFilesManager;
import groovy.lang.GroovyClassLoader;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.CountingInputStream;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.property.editor.PropertyEditor;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller implements Initializable {
    private static final Logger log = Logger.getLogger(Controller.class.getName());

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

    private XdatEditor editor;

    private ResourceBundle interfaceResources;

    @FXML
    private MenuItem open;
    @FXML
    private MenuItem save;
    @FXML
    private MenuItem saveAs;
    @FXML
    private Menu versionMenu;
    @FXML
    private Menu viewMenu;
    @FXML
    private Menu themeMenu;
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;
    @FXML
    private MenuItem historyMenuItem;
    @FXML
    private MenuItem copyMenuItem;
    @FXML
    private MenuItem cutMenuItem;
    @FXML
    private MenuItem pasteMenuItem;
    @FXML
    private MenuItem duplicateMenuItem;
    @FXML
    private Menu recentFilesMenu;
    @FXML
    private Menu languageMenu;
    private ToggleGroup version = new ToggleGroup();
    private ToggleGroup themeGroup = new ToggleGroup();
    private ToggleGroup languageGroup = new ToggleGroup();
    @FXML
    private TabPane tabs;
    @FXML
    private ProgressBar progressBar;

    // Clipboard and recent files managers
    private ClipboardManager clipboardManager;
    private RecentFilesManager recentFilesManager;

    // Current selected tree view (for copy/paste operations)
    private TreeView<Object> currentTreeView;
    private String currentVersionName;

    private ObjectProperty<File> initialDirectory = new SimpleObjectProperty<>(this, "initialDirectory", new File(XdatEditor.getPrefs().get("initialDirectory", System.getProperty("user.dir"))));
    private ObjectProperty<File> xdatFile = new SimpleObjectProperty<>(this, "xdatFile");
    private ObjectProperty<Environment> environment = new SimpleObjectProperty<>(this, "environment");
    private ObjectProperty<L2Resources> l2resources = new SimpleObjectProperty<>(this, "l2resources");

    private List<InvalidationListener> xdatListeners = new ArrayList<>();

    public Controller(XdatEditor editor) {
        this.editor = editor;

        environment.bind(Bindings.createObjectBinding(() -> {
            if (xdatFile.getValue() == null)
                return null;

            return Environment.fromIni(new File(xdatFile.getValue().getParentFile(), "L2.ini"));
        }, xdatFile));
        l2resources.bind(Bindings.createObjectBinding(() -> {
            if (environment.getValue() == null)
                return null;

            return new L2Resources(environment.get());
        }, environment));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        interfaceResources = resources;

        initializeThemeMenu();
        initializeLanguageMenu();
        initializeClipboard();
        initializeRecentFiles();

        Node scriptingTab = loadScriptTabContent();

        initialDirectory.addListener((observable, oldVal, newVal) -> {
            if (newVal != null)
                XdatEditor.getPrefs().put("initialDirectory", newVal.getPath());
        });
        editor.xdatClassProperty().addListener((ob, ov, nv) -> {
            log.log(Level.INFO, String.format("XDAT class selected: %s", nv.getName()));

            tabs.getTabs().clear();

            for (Iterator<InvalidationListener> it = xdatListeners.iterator(); it.hasNext(); ) {
                editor.xdatObjectProperty().removeListener(it.next());
                it.remove();
            }

            editor.setXdatObject(null);

            if (scriptingTab != null) {
                Tab tab = new Tab("script console");
                tab.setContent(scriptingTab);
                tabs.getTabs().add(tab);
            }

            Arrays.stream(nv.getDeclaredFields())
                    .filter(field -> List.class.isAssignableFrom(field.getType()))
                    .forEach(field -> {
                        field.setAccessible(true);
                        tabs.getTabs().add(createTab(field));
                    });
        });
        progressBar.visibleProperty().bind(editor.workingProperty());
        open.disableProperty().bind(Bindings.isNull(editor.xdatClassProperty()));
        BooleanBinding nullXdatObject = Bindings.isNull(editor.xdatObjectProperty());
        tabs.disableProperty().bind(nullXdatObject);
        save.disableProperty().bind(nullXdatObject);
        saveAs.disableProperty().bind(nullXdatObject);

        // Bind undo/redo menu items to UndoManager state
        UndoManager undoManager = editor.getUndoManager();
        undoMenuItem.disableProperty().bind(undoManager.canUndoProperty().not());
        redoMenuItem.disableProperty().bind(undoManager.canRedoProperty().not());

        // Update undo/redo text with descriptions
        undoManager.canUndoProperty().addListener((obs, oldVal, newVal) -> {
            String desc = undoManager.getUndoDescription();
            if (desc != null) {
                undoMenuItem.setText(interfaceResources.getString("edit.undo") + ": " + desc);
            } else {
                undoMenuItem.setText(interfaceResources.getString("edit.undo"));
            }
        });
        undoManager.canRedoProperty().addListener((obs, oldVal, newVal) -> {
            String desc = undoManager.getRedoDescription();
            if (desc != null) {
                redoMenuItem.setText(interfaceResources.getString("edit.redo") + ": " + desc);
            } else {
                redoMenuItem.setText(interfaceResources.getString("edit.redo"));
            }
        });

        xdatFile.addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                return;

            Collection<File> files = FileUtils.listFiles(newValue.getParentFile(), new WildcardFileFilter("SysString-*.dat"), null);
            if (!files.isEmpty()) {
                File file = files.iterator().next();
                log.info("sysstring file: " + file);
                try (InputStream is = L2Crypt.decrypt(new FileInputStream(file), file.getName())) {
                    SysstringPropertyEditor.strings.clear();
                    int count = IOUtil.readInt(is);
                    for (int i = 0; i < count; i++) {
                        SysstringPropertyEditor.strings.put(IOUtil.readInt(is), IOUtil.readString(is));
                    }
                } catch (Exception ignore) {
                }
            }

            File file = new File(newValue.getParentFile(), "L2.ini");
            try {
                TexturePropertyEditor.environment = Environment.fromIni(file);
                TexturePropertyEditor.environment.getPaths().forEach(s -> log.info("environment path: " + s));
            } catch (Exception ignore) {
            }
        });
    }

    private void initializeThemeMenu() {
        String savedTheme = XdatEditor.getPrefs().get("theme", "dark");

        RadioMenuItem darkTheme = new RadioMenuItem(interfaceResources.getString("view.theme.dark"));
        darkTheme.setToggleGroup(themeGroup);
        darkTheme.setUserData("dark");
        darkTheme.setOnAction(e -> applyTheme("dark"));

        RadioMenuItem lightTheme = new RadioMenuItem(interfaceResources.getString("view.theme.light"));
        lightTheme.setToggleGroup(themeGroup);
        lightTheme.setUserData("light");
        lightTheme.setOnAction(e -> applyTheme("light"));

        themeMenu.getItems().addAll(darkTheme, lightTheme);

        if ("light".equals(savedTheme)) {
            lightTheme.setSelected(true);
        } else {
            darkTheme.setSelected(true);
        }
    }

    private void applyTheme(String theme) {
        Scene scene = editor.getStage().getScene();
        scene.getStylesheets().clear();

        String cssPath;
        if ("light".equals(theme)) {
            cssPath = getClass().getResource("light-theme.css").toExternalForm();
        } else {
            cssPath = getClass().getResource("dark-theme.css").toExternalForm();
        }

        scene.getStylesheets().add(cssPath);
        XdatEditor.getPrefs().put("theme", theme);
    }

    private void initializeLanguageMenu() {
        String currentLanguage = XdatEditor.getPrefs().get("language", "en");

        // English
        RadioMenuItem enItem = new RadioMenuItem(interfaceResources.getString("view.language.en"));
        enItem.setToggleGroup(languageGroup);
        enItem.setUserData("en");
        enItem.setSelected("en".equals(currentLanguage));
        enItem.setOnAction(e -> changeLanguage("en"));

        // Portuguese (Brazil)
        RadioMenuItem ptBrItem = new RadioMenuItem(interfaceResources.getString("view.language.pt_BR"));
        ptBrItem.setToggleGroup(languageGroup);
        ptBrItem.setUserData("pt_BR");
        ptBrItem.setSelected("pt_BR".equals(currentLanguage));
        ptBrItem.setOnAction(e -> changeLanguage("pt_BR"));

        // Spanish (Argentina)
        RadioMenuItem esArItem = new RadioMenuItem(interfaceResources.getString("view.language.es_AR"));
        esArItem.setToggleGroup(languageGroup);
        esArItem.setUserData("es_AR");
        esArItem.setSelected("es_AR".equals(currentLanguage));
        esArItem.setOnAction(e -> changeLanguage("es_AR"));

        // Russian
        RadioMenuItem ruItem = new RadioMenuItem(interfaceResources.getString("view.language.ru"));
        ruItem.setToggleGroup(languageGroup);
        ruItem.setUserData("ru");
        ruItem.setSelected("ru".equals(currentLanguage));
        ruItem.setOnAction(e -> changeLanguage("ru"));

        languageMenu.getItems().addAll(enItem, ptBrItem, esArItem, ruItem);
    }

    private void changeLanguage(String language) {
        String currentLanguage = XdatEditor.getPrefs().get("language", "en");
        if (!language.equals(currentLanguage)) {
            XdatEditor.getPrefs().put("language", language);

            // Show restart message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(interfaceResources.getString("language.restart"));
            alert.setHeaderText(null);
            alert.setContentText(interfaceResources.getString("language.restart.message"));
            alert.showAndWait();
        }
    }

    private void initializeClipboard() {
        clipboardManager = ClipboardManager.getInstance();

        // Bind copy/cut/paste menu items
        BooleanBinding nullXdatObject = Bindings.isNull(editor.xdatObjectProperty());
        copyMenuItem.disableProperty().bind(nullXdatObject);
        cutMenuItem.disableProperty().bind(nullXdatObject);
        duplicateMenuItem.disableProperty().bind(nullXdatObject);
        pasteMenuItem.disableProperty().bind(
                nullXdatObject.or(clipboardManager.hasContentProperty().not())
        );
    }

    private void initializeRecentFiles() {
        recentFilesManager = new RecentFilesManager(XdatEditor.getPrefs());
        updateRecentFilesMenu();

        // Listen for changes in recent files
        recentFilesManager.getRecentFiles().addListener((javafx.collections.ListChangeListener<RecentFilesManager.RecentFile>) c -> {
            updateRecentFilesMenu();
        });
    }

    private void updateRecentFilesMenu() {
        recentFilesMenu.getItems().clear();

        if (recentFilesManager.getRecentFiles().isEmpty()) {
            MenuItem emptyItem = new MenuItem(interfaceResources.getString("file.recent.empty"));
            emptyItem.setDisable(true);
            recentFilesMenu.getItems().add(emptyItem);
        } else {
            for (RecentFilesManager.RecentFile recentFile : recentFilesManager.getRecentFiles()) {
                MenuItem item = new MenuItem(recentFile.toString());
                item.setOnAction(e -> openRecentFile(recentFile));
                recentFilesMenu.getItems().add(item);
            }

            recentFilesMenu.getItems().add(new SeparatorMenuItem());

            MenuItem clearItem = new MenuItem(interfaceResources.getString("file.recent.clear"));
            clearItem.setOnAction(e -> recentFilesManager.clearRecentFiles());
            recentFilesMenu.getItems().add(clearItem);
        }
    }

    private void openRecentFile(RecentFilesManager.RecentFile recentFile) {
        if (!recentFile.exists()) {
            recentFilesManager.removeRecentFile(recentFile.getFile());
            Dialogs.showException(Alert.AlertType.WARNING, "File not found",
                    "The file no longer exists: " + recentFile.getPath(), null);
            return;
        }

        // Try to find and select the version
        if (recentFile.getVersionName() != null) {
            for (Toggle toggle : version.getToggles()) {
                if (toggle instanceof RadioMenuItem) {
                    RadioMenuItem menuItem = (RadioMenuItem) toggle;
                    if (menuItem.getText().equals(recentFile.getVersionName())) {
                        menuItem.setSelected(true);
                        break;
                    }
                }
            }
        }

        // Wait for version to be selected, then open file
        Platform.runLater(() -> {
            if (editor.getXdatClass() != null) {
                openFileDirectly(recentFile.getFile());
            }
        });
    }

    private void openFileDirectly(File file) {
        xdatFile.setValue(file);
        initialDirectory.setValue(file.getParentFile());

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int i = Integer.reverseBytes(dis.readInt());
            if (i < 0 || i > 0xFFFF) {
                throw new IOException("File seems to be encrypted.");
            }
        } catch (IOException e) {
            Dialogs.showException(Alert.AlertType.ERROR, "Read error", e.getMessage(), e);
            return;
        }

        try {
            IOEntity xdat = editor.getXdatClass().getConstructor().newInstance();

            editor.execute(() -> {
                CountingInputStream cis = new CountingInputStream(new BufferedInputStream(new FileInputStream(file)));
                try (InputStream is = cis) {
                    xdat.read(is);
                    Platform.runLater(() -> editor.setXdatObject(xdat));
                } catch (Throwable e) {
                    String msg = String.format("Read error before offset 0x%x", cis.getCount());
                    log.log(Level.WARNING, msg, e);
                    throw new IOException(msg, e);
                }
                return null;
            }, e -> Dialogs.showException(Alert.AlertType.ERROR, "Read error", "Try to choose another version", e));
        } catch (ReflectiveOperationException e) {
            String msg = "XDAT class should have empty public constructor";
            log.log(Level.WARNING, msg, e);
            Dialogs.showException(Alert.AlertType.ERROR, "ReflectiveOperationException", msg, e);
        }
    }

    public void registerVersion(String name, String xdatClass) {
        RadioMenuItem menuItem = new RadioMenuItem(name);
        menuItem.setMnemonicParsing(false);
        menuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                currentVersionName = name;
                editor.execute(() -> {
                    Class<? extends IOEntity> clazz = Class.forName(xdatClass, true, new GroovyClassLoader(getClass().getClassLoader())).asSubclass(IOEntity.class);
                    Platform.runLater(() -> editor.setXdatClass(clazz));
                    return null;
                }, e -> {
                    String msg = String.format("%s: XDAT class load error", name);
                    log.log(Level.WARNING, msg, e);
                    Platform.runLater(() -> {
                        version.getToggles().remove(menuItem);
                        versionMenu.getItems().remove(menuItem);

                        Dialogs.showException(Alert.AlertType.ERROR, msg, e.getMessage(), e);
                    });
                });
            }
        });
        version.getToggles().add(menuItem);
        versionMenu.getItems().add(menuItem);
    }

    private Node loadScriptTabContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("scripting/main.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setControllerFactory(param -> new acmi.l2.clientmod.xdat.scripting.Controller(editor));
            return wrap(loader.load());
        } catch (IOException e) {
            log.log(Level.WARNING, "Couldn't load script console", e);
        }
        return null;
    }

    private static AnchorPane wrap(Node node) {
        AnchorPane anchorPane = new AnchorPane(node);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        return anchorPane;
    }

    private Tab createTab(Field listField) {
        Tab tab = new Tab(listField.getName());

        SplitPane pane = new SplitPane();

        // Create advanced search panel
        SearchPanel searchPanel = new SearchPanel(interfaceResources);
        searchPanel.setComponentTypes(new ArrayList<>(UI_NODE_ICONS.keySet()));
        searchPanel.setPropertyNames(Arrays.asList(
                "name", "text", "buttonNameText", "titleText", "file",
                "normalTex", "backTex", "fontName", "styleName"
        ));

        TreeView<Object> elements = createTreeView(listField, searchPanel);
        VBox.setVgrow(elements, Priority.ALWAYS);
        PropertySheet properties = createPropertySheet(elements);

        // Setup replace handlers
        searchPanel.setOnReplace(() -> replaceSelected(elements, searchPanel));
        searchPanel.setOnReplaceAll(() -> replaceAll(listField, searchPanel));

        pane.getItems().addAll(new VBox(searchPanel, elements), properties);
        pane.setDividerPositions(0.3);

        tab.setContent(wrap(pane));

        return tab;
    }

    private TreeView<Object> createTreeView(Field listField, SearchPanel searchPanel) {
        TreeView<Object> elements = new TreeView<>();

        // Setup cell factory with both highlight and drag-and-drop support
        setupTreeViewCellFactory(elements, searchPanel);

        elements.setShowRoot(false);
        elements.setContextMenu(createContextMenu(elements));

        // Track current tree view for copy/paste operations
        elements.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                currentTreeView = elements;
            }
        });
        elements.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentTreeView = elements;
        });

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

            // Drag detected - start drag
            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null || cell.getItem() instanceof ListHolder) {
                    return;
                }

                TreeItem<Object> draggedItem = cell.getTreeItem();
                if (draggedItem == null || draggedItem.getParent() == null ||
                        !(draggedItem.getParent().getValue() instanceof ListHolder)) {
                    return;
                }

                javafx.scene.input.Dragboard db = cell.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(String.valueOf(System.identityHashCode(draggedItem)));
                db.setContent(content);
                event.consume();
            });

            // Drag over - show drop indicator
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    TreeItem<Object> targetItem = cell.getTreeItem();
                    if (targetItem != null && canDropOn(treeView, targetItem)) {
                        event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);

                        // Show drop position indicator
                        cell.getStyleClass().removeAll("drag-over-top", "drag-over-bottom", "drag-over");
                        double cellHeight = cell.getHeight();
                        double y = event.getY();
                        if (y < cellHeight * 0.25) {
                            cell.getStyleClass().add("drag-over-top");
                        } else if (y > cellHeight * 0.75) {
                            cell.getStyleClass().add("drag-over-bottom");
                        } else {
                            cell.getStyleClass().add("drag-over");
                        }
                    }
                }
                event.consume();
            });

            // Drag exited - remove indicator
            cell.setOnDragExited(event -> {
                cell.getStyleClass().removeAll("drag-over-top", "drag-over-bottom", "drag-over");
                event.consume();
            });

            // Drop - perform the move
            cell.setOnDragDropped(event -> {
                javafx.scene.input.Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    TreeItem<Object> draggedItem = treeView.getSelectionModel().getSelectedItem();
                    TreeItem<Object> targetItem = cell.getTreeItem();

                    if (draggedItem != null && targetItem != null && draggedItem != targetItem) {
                        success = performDrop(treeView, draggedItem, targetItem, cell, event.getY());
                    }
                }

                event.setDropCompleted(success);
                event.consume();
            });

            // Drag done
            cell.setOnDragDone(event -> {
                cell.getStyleClass().removeAll("drag-over-top", "drag-over-bottom", "drag-over");
                event.consume();
            });

            return cell;
        });
    }

    private boolean canDropOn(TreeView<Object> treeView, TreeItem<Object> targetItem) {
        TreeItem<Object> draggedItem = treeView.getSelectionModel().getSelectedItem();
        if (draggedItem == null || targetItem == null) {
            return false;
        }

        // Cannot drop on itself
        if (draggedItem == targetItem) {
            return false;
        }

        // Cannot drop on descendants
        TreeItem<Object> parent = targetItem.getParent();
        while (parent != null) {
            if (parent == draggedItem) {
                return false;
            }
            parent = parent.getParent();
        }

        // Can drop if target is in same list or target is a compatible ListHolder
        if (targetItem.getValue() instanceof ListHolder) {
            ListHolder targetHolder = (ListHolder) targetItem.getValue();
            Object draggedValue = draggedItem.getValue();
            return targetHolder.type.isAssignableFrom(draggedValue.getClass());
        }

        if (targetItem.getParent() != null && targetItem.getParent().getValue() instanceof ListHolder) {
            // Same parent list - reordering
            if (draggedItem.getParent() == targetItem.getParent()) {
                return true;
            }
            // Different parent but compatible types
            ListHolder targetHolder = (ListHolder) targetItem.getParent().getValue();
            Object draggedValue = draggedItem.getValue();
            return targetHolder.type.isAssignableFrom(draggedValue.getClass());
        }

        return false;
    }

    private boolean performDrop(TreeView<Object> treeView, TreeItem<Object> draggedItem,
                                TreeItem<Object> targetItem, TreeCell<Object> cell, double y) {
        Object draggedValue = draggedItem.getValue();
        if (!(draggedValue instanceof IOEntity)) {
            return false;
        }

        IOEntity draggedEntity = (IOEntity) draggedValue;
        TreeItem<Object> sourceParent = draggedItem.getParent();

        if (!(sourceParent.getValue() instanceof ListHolder)) {
            return false;
        }

        ListHolder sourceHolder = (ListHolder) sourceParent.getValue();
        int sourceIndex = sourceHolder.list.indexOf(draggedEntity);

        // Determine target list and index
        TreeItem<Object> targetParent;
        ListHolder targetHolder;
        int targetIndex;

        if (targetItem.getValue() instanceof ListHolder) {
            // Dropping into a list
            targetParent = targetItem;
            targetHolder = (ListHolder) targetItem.getValue();
            targetIndex = targetHolder.list.size();
        } else if (targetItem.getParent() != null && targetItem.getParent().getValue() instanceof ListHolder) {
            // Dropping relative to a sibling
            targetParent = targetItem.getParent();
            targetHolder = (ListHolder) targetParent.getValue();

            double cellHeight = cell.getHeight();
            int siblingIndex = targetHolder.list.indexOf(targetItem.getValue());

            if (y < cellHeight * 0.5) {
                targetIndex = siblingIndex;
            } else {
                targetIndex = siblingIndex + 1;
            }
        } else {
            return false;
        }

        // Check type compatibility
        if (!targetHolder.type.isAssignableFrom(draggedEntity.getClass())) {
            return false;
        }

        // Create and execute move command
        MoveElementCommand command = new MoveElementCommand(
                sourceHolder.list, targetHolder.list,
                sourceParent, targetParent,
                draggedEntity, draggedItem,
                sourceIndex, targetIndex
        );

        // Execute the move
        command.execute();

        // Record for undo
        editor.getUndoManager().record(command);

        // Select the moved item
        treeView.getSelectionModel().select(draggedItem);

        log.info("Moved element: " + draggedEntity + " from index " + sourceIndex + " to " + targetIndex);
        return true;
    }

    private static int buildTree(IOEntity entity, Field listField, TreeView<Object> elements, SearchCriteria criteria) {
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
                        .map(Controller::createTreeItem)
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

    private static boolean checkTreeNode(TreeItem<Object> treeItem, SearchCriteria criteria, int[] resultCount) {
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
                        ((L2Context) value).setResources(l2resources.getValue());
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

    private static TreeItem<Object> createTreeItem(IOEntity o) {
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
                                .map(Controller::createTreeItem)
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

    private static Map<Class, List<PropertySheetItem>> map = new HashMap<>();

    private PropertySheet createPropertySheet(TreeView<Object> elements) {
        PropertySheet properties = new PropertySheet();
        properties.setSkin(new PropertySheetSkin(properties));

        elements.getSelectionModel().selectedItemProperty().addListener((selected, oldValue, newSelection) -> {
            properties.getItems().clear();

            if (newSelection == null)
                return;

            Object obj = newSelection.getValue();

            if (obj instanceof ListHolder)
                return;

            if (!map.containsKey(obj.getClass())) {
                map.put(obj.getClass(), loadProperties(obj));
            }
            List<PropertySheetItem> props = map.get(obj.getClass());
            props.forEach(property -> {
                property.setObject(obj);
                ChangeListener<Object> addToHistory = (observable1, oldValue1, newValue1) -> {
                    // Skip if UndoManager is executing a command (undo/redo in progress)
                    if (editor.getUndoManager().isExecutingCommand()) {
                        return;
                    }

                    String objName = treeItemToScriptString(newSelection);
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

    private String treeItemToScriptString(TreeItem item) {
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

    @FXML
    private void open() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open interface.xdat");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XDAT (*.xdat)", "*.xdat"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        if (initialDirectory.getValue() != null &&
                initialDirectory.getValue().exists() &&
                initialDirectory.getValue().isDirectory())
            fileChooser.setInitialDirectory(initialDirectory.getValue());

        File selected = fileChooser.showOpenDialog(editor.getStage());
        if (selected == null)
            return;

        xdatFile.setValue(selected);
        initialDirectory.setValue(selected.getParentFile());

        try (DataInputStream dis = new DataInputStream(new FileInputStream(selected))) {
            int i = Integer.reverseBytes(dis.readInt());

            if (i < 0 || i > 0xFFFF) {
                throw new IOException("File seems to be encrypted.");
            }
        } catch (IOException e) {
            Dialogs.showException(Alert.AlertType.ERROR, "Read error", e.getMessage(), e);
            return;
        }

        try {
            IOEntity xdat = editor.getXdatClass().getConstructor().newInstance();

            editor.execute(() -> {
                CountingInputStream cis = new CountingInputStream(new BufferedInputStream(new FileInputStream(selected)));
                try (InputStream is = cis) {
                    xdat.read(is);

                    Platform.runLater(() -> {
                        editor.setXdatObject(xdat);
                        // Add to recent files
                        recentFilesManager.addRecentFile(selected, currentVersionName);
                    });
                } catch (Throwable e) {
                    String msg = String.format("Read error before offset 0x%x", cis.getCount());
                    log.log(Level.WARNING, msg, e);
                    throw new IOException(msg, e);
                }
                return null;
            }, e -> Dialogs.showException(Alert.AlertType.ERROR, "Read error", "Try to choose another version", e));
        } catch (ReflectiveOperationException e) {
            String msg = "XDAT class should have empty public constructor";
            log.log(Level.WARNING, msg, e);
            Dialogs.showException(Alert.AlertType.ERROR, "ReflectiveOperationException", msg, e);
        }
    }

    @FXML
    private void save() {
        if (xdatFile.getValue() == null)
            return;

        editor.execute(() -> {
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(xdatFile.getValue()))) {
                editor.getXdatObject().write(os);
            }
            return null;
        }, e -> {
            String msg = "Write error";
            log.log(Level.WARNING, msg, e);
            Dialogs.showException(Alert.AlertType.ERROR, msg, e.getMessage(), e);
        });
    }

    @FXML
    private void undo() {
        editor.getUndoManager().undo();
    }

    @FXML
    private void redo() {
        editor.getUndoManager().redo();
    }

    @FXML
    private void copyElement() {
        TreeItem<Object> selected = getSelectedTreeItem();
        if (selected == null || selected.getValue() instanceof ListHolder) {
            return;
        }

        Object value = selected.getValue();
        if (value instanceof IOEntity) {
            clipboardManager.copy((IOEntity) value);
            log.info("Copied element: " + value);
        }
    }

    @FXML
    private void cutElement() {
        TreeItem<Object> selected = getSelectedTreeItem();
        if (selected == null || selected.getValue() instanceof ListHolder) {
            return;
        }

        Object value = selected.getValue();
        if (value instanceof IOEntity && selected.getParent() != null &&
                selected.getParent().getValue() instanceof ListHolder) {

            clipboardManager.cut((IOEntity) value);

            // Store info for deletion on paste
            log.info("Cut element: " + value);
        }
    }

    @FXML
    private void pasteElement() {
        if (!clipboardManager.hasContent()) {
            return;
        }

        TreeItem<Object> selected = getSelectedTreeItem();
        TreeItem<Object> targetParent = null;
        ListHolder listHolder = null;

        // Determine where to paste
        if (selected == null) {
            // Paste to root if nothing selected
            if (currentTreeView != null && currentTreeView.getRoot() != null) {
                targetParent = currentTreeView.getRoot();
                if (targetParent.getValue() instanceof ListHolder) {
                    listHolder = (ListHolder) targetParent.getValue();
                }
            }
        } else if (selected.getValue() instanceof ListHolder) {
            targetParent = selected;
            listHolder = (ListHolder) selected.getValue();
        } else if (selected.getParent() != null && selected.getParent().getValue() instanceof ListHolder) {
            targetParent = selected.getParent();
            listHolder = (ListHolder) targetParent.getValue();
        }

        if (listHolder == null || targetParent == null) {
            return;
        }

        // Check type compatibility
        Class<? extends IOEntity> clipboardClass = clipboardManager.getContentClass();
        if (clipboardClass != null && !listHolder.type.isAssignableFrom(clipboardClass)) {
            Dialogs.showException(Alert.AlertType.WARNING,
                    interfaceResources.getString("clipboard.incompatible"),
                    "Cannot paste " + clipboardClass.getSimpleName() + " into " + listHolder.type.getSimpleName(),
                    null);
            return;
        }

        // Perform paste
        IOEntity pastedElement = clipboardManager.paste();
        if (pastedElement != null) {
            // Create a fresh clone for paste
            IOEntity elementToAdd = ElementCloner.deepClone(pastedElement);
            if (elementToAdd == null) {
                elementToAdd = pastedElement;
            }

            int index = listHolder.list.size();
            listHolder.list.add(elementToAdd);
            TreeItem<Object> newTreeItem = createTreeItem(elementToAdd);
            targetParent.getChildren().add(newTreeItem);

            // Record command for undo
            PasteElementCommand command = new PasteElementCommand(
                    listHolder.list, targetParent, elementToAdd, newTreeItem, index);
            editor.getUndoManager().record(command);

            // Select the pasted element
            if (currentTreeView != null) {
                currentTreeView.getSelectionModel().select(newTreeItem);
                currentTreeView.scrollTo(currentTreeView.getSelectionModel().getSelectedIndex());
            }

            editor.getHistory().valueCreated(treeItemToScriptString(targetParent), elementToAdd.getClass());
            log.info("Pasted element: " + elementToAdd);
        }
    }

    @FXML
    private void duplicateElement() {
        TreeItem<Object> selected = getSelectedTreeItem();
        if (selected == null || selected.getValue() instanceof ListHolder) {
            return;
        }

        Object value = selected.getValue();
        if (value instanceof IOEntity && selected.getParent() != null &&
                selected.getParent().getValue() instanceof ListHolder) {

            ListHolder listHolder = (ListHolder) selected.getParent().getValue();

            // Clone the element
            IOEntity clone = ElementCloner.deepClone((IOEntity) value);
            if (clone == null) {
                return;
            }

            // Find index and insert after current element
            int currentIndex = listHolder.list.indexOf(value);
            int newIndex = currentIndex + 1;

            listHolder.list.add(newIndex, clone);
            TreeItem<Object> newTreeItem = createTreeItem(clone);
            selected.getParent().getChildren().add(newIndex, newTreeItem);

            // Record command for undo
            PasteElementCommand command = new PasteElementCommand(
                    listHolder.list, selected.getParent(), clone, newTreeItem, newIndex);
            editor.getUndoManager().record(command);

            // Select the duplicated element
            if (currentTreeView != null) {
                currentTreeView.getSelectionModel().select(newTreeItem);
                currentTreeView.scrollTo(currentTreeView.getSelectionModel().getSelectedIndex());
            }

            editor.getHistory().valueCreated(treeItemToScriptString(selected.getParent()), clone.getClass());
            log.info("Duplicated element: " + clone);
        }
    }

    private TreeItem<Object> getSelectedTreeItem() {
        if (currentTreeView == null) {
            return null;
        }
        return currentTreeView.getSelectionModel().getSelectedItem();
    }

    private void replaceSelected(TreeView<Object> treeView, SearchPanel searchPanel) {
        String searchText = searchPanel.getSearchText();
        String replaceText = searchPanel.getReplaceText();
        String propertyFilter = searchPanel.getSelectedProperty();

        if (searchText.isEmpty()) {
            searchPanel.setReplaceStatus(interfaceResources.getString("search.no_matches"));
            return;
        }

        TreeItem<Object> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() instanceof ListHolder) {
            searchPanel.setReplaceStatus(interfaceResources.getString("search.no_matches"));
            return;
        }

        Object item = selected.getValue();
        int count = replaceInObject(item, searchText, replaceText, propertyFilter, searchPanel.isUseRegex());

        if (count > 0) {
            searchPanel.setReplaceStatus(String.format(interfaceResources.getString("search.replaced"), count));
            // Refresh tree view
            treeView.refresh();
        } else {
            searchPanel.setReplaceStatus(interfaceResources.getString("search.no_matches"));
        }
    }

    private void replaceAll(Field listField, SearchPanel searchPanel) {
        String searchText = searchPanel.getSearchText();
        String replaceText = searchPanel.getReplaceText();
        String propertyFilter = searchPanel.getSelectedProperty();
        String typeFilter = searchPanel.getSelectedType();

        if (searchText.isEmpty()) {
            searchPanel.setReplaceStatus(interfaceResources.getString("search.no_matches"));
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
                        interfaceResources.getString("search.replaced"),
                        batchCommand.getReplacementCount()));

                // Refresh the current tree view
                if (currentTreeView != null) {
                    currentTreeView.refresh();
                }
            } else {
                searchPanel.setReplaceStatus(interfaceResources.getString("search.no_matches"));
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

    @FXML
    private void showHistory() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(interfaceResources.getString("history.title"));
        dialog.setResizable(true);

        UndoManager undoManager = editor.getUndoManager();

        ListView<Command> historyList = new ListView<>(undoManager.getHistoryList());
        historyList.setPrefSize(500, 400);
        historyList.setCellFactory(param -> new ListCell<Command>() {
            @Override
            protected void updateItem(Command item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getDescription());
                    // Format timestamp
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
                    String time = sdf.format(new java.util.Date(item.getTimestamp()));
                    setStyle("-fx-font-family: monospace;");
                    setText(String.format("[%s] %s", time, item.getDescription()));
                }
            }
        });

        if (undoManager.getHistoryList().isEmpty()) {
            historyList.setPlaceholder(new Label(interfaceResources.getString("history.no_changes")));
        }

        Button clearButton = new Button(interfaceResources.getString("history.clear"));
        clearButton.setOnAction(e -> {
            undoManager.clear();
        });

        VBox content = new VBox(10, historyList, clearButton);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    @FXML
    private void saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XDAT (*.xdat)", "*.xdat"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        fileChooser.setInitialFileName(xdatFile.getValue().getName());

        if (initialDirectory.getValue() != null &&
                initialDirectory.getValue().exists() &&
                initialDirectory.getValue().isDirectory())
            fileChooser.setInitialDirectory(initialDirectory.getValue());

        File file = fileChooser.showSaveDialog(editor.getStage());
        if (file == null)
            return;

        this.xdatFile.setValue(file);
        initialDirectory.setValue(file.getParentFile());

        save();
    }

    @FXML
    private void exit() {
        Platform.exit();
    }

    @FXML
    private void about() {
        Dialog dialog = new Dialog();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(interfaceResources.getString("about"));

        Label name = new Label("XDAT Editor");
        Label version = new Label("Version: " + editor.getApplicationVersion());
        Label jre = new Label("JRE: " + System.getProperty("java.version"));
        Label jvm = new Label("JVM: " + System.getProperty("java.vm.name") + " by " + System.getProperty("java.vendor"));
        Hyperlink link = new Hyperlink("GitHub");
        link.setOnAction(event -> editor.getHostServices().showDocument("https://github.com/acmi/xdat_editor"));

        Label license = new Label(interfaceResources.getString("help.open_source_licenses"));
        Hyperlink licenseApache = new Hyperlink("Apache Commons IO, Apache Commons CSV,\nApache Commons Lang, Groovy");
        licenseApache.setOnAction(event -> editor.getHostServices().showDocument("http://www.apache.org/licenses/LICENSE-2.0.txt"));
        Hyperlink licenseControlsFX = new Hyperlink("ControlsFX");
        licenseControlsFX.setOnAction(event -> editor.getHostServices().showDocument("https://bitbucket.org/controlsfx/controlsfx/raw/15b3171c215f00de751a37d14f6b678d6896f8a2/license.txt"));

        VBox content = new VBox(name, version, jre, jvm, link, license, licenseApache, licenseControlsFX);
        VBox.setMargin(jre, new Insets(10, 0, 0, 0));
        VBox.setMargin(link, new Insets(10, 0, 0, 0));
        VBox.setMargin(license, new Insets(15, 0, 0, 0));

        DialogPane pane = new DialogPane();
        pane.setContent(content);
        pane.getButtonTypes().addAll(ButtonType.OK);
        dialog.setDialogPane(pane);

        dialog.showAndWait();
    }

    private static class ListHolder {
        IOEntity entity;
        List<IOEntity> list;
        String name;
        Class<? extends IOEntity> type;

        ListHolder(IOEntity entity, List<IOEntity> list, String name, Class<? extends IOEntity> type) {
            this.entity = entity;
            this.list = list;
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class ClassHolder {
        Class<? extends IOEntity> clazz;

        private ClassHolder(Class<? extends IOEntity> clazz) {
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return clazz.getSimpleName();
        }
    }
}
