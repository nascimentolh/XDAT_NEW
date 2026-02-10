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

import acmi.l2.clientmod.util.*;
import acmi.l2.clientmod.xdat.history.*;
import acmi.l2.clientmod.xdat.search.SearchPanel;
import acmi.l2.clientmod.xdat.util.ClipboardManager;
import acmi.l2.clientmod.xdat.util.ElementCloner;
import groovy.lang.GroovyClassLoader;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller implements Initializable {
    private static final Logger log = Logger.getLogger(Controller.class.getName());

    private XdatEditor editor;
    private ResourceBundle interfaceResources;

    // Managers
    private TreeManager treeManager;
    private FileOperationsManager fileOps;
    private SearchReplaceManager searchReplace;
    private ImportExportManager importExport;

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
    private MenuItem exportWindowMenuItem;
    @FXML
    private MenuItem importWindowMenuItem;
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

    // Clipboard manager
    private ClipboardManager clipboardManager;

    // Current selected tree view (for copy/paste operations)
    private TreeView<Object> currentTreeView;

    public Controller(XdatEditor editor) {
        this.editor = editor;
        this.fileOps = new FileOperationsManager(editor, interfaceResources);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        interfaceResources = resources;

        // Recreate FileOperationsManager with resources now available
        this.fileOps = new FileOperationsManager(editor, interfaceResources);
        this.treeManager = new TreeManager(editor, interfaceResources, fileOps.l2resourcesProperty());
        this.searchReplace = new SearchReplaceManager(editor, interfaceResources);
        this.importExport = new ImportExportManager(editor, interfaceResources, fileOps);

        initializeThemeMenu();
        initializeLanguageMenu();
        initializeClipboard();
        initializeRecentFiles();

        Node scriptingTab = loadScriptTabContent();

        fileOps.initialDirectoryProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal != null && newVal.exists())
                XdatEditor.getPrefs().put("initialDirectory", newVal.getAbsolutePath());
        });

        // Bind menu items to file state
        save.disableProperty().bind(Bindings.isNull(fileOps.xdatFileProperty()));
        saveAs.disableProperty().bind(Bindings.isNull(fileOps.xdatFileProperty()));

        // Bind undo/redo menu items
        undoMenuItem.disableProperty().bind(editor.getUndoManager().canUndoProperty().not());
        redoMenuItem.disableProperty().bind(editor.getUndoManager().canRedoProperty().not());
        historyMenuItem.disableProperty().bind(Bindings.isEmpty(editor.getUndoManager().getHistoryList()));

        // Listen to xdatClass changes and create tabs
        editor.xdatClassProperty().addListener((observable, oldValue, newValue) -> {
            tabs.getTabs().clear();

            if (newValue == null)
                return;

            List<Field> listFields = new ArrayList<>();
            Class<?> clazz = newValue;
            while (clazz != Object.class) {
                Arrays.stream(clazz.getDeclaredFields())
                        .filter(field -> !field.isSynthetic())
                        .filter(field -> List.class.isAssignableFrom(field.getType()))
                        .forEach(listFields::add);
                clazz = clazz.getSuperclass();
            }

            listFields.forEach(field -> {
                field.setAccessible(true);

                SearchPanel searchPanel = new SearchPanel(interfaceResources);

                Tab tab = treeManager.createTab(
                        field,
                        searchPanel,
                        () -> searchReplace.replaceSelected(currentTreeView, searchPanel),
                        () -> searchReplace.replaceAll(field, searchPanel, currentTreeView)
                );
                tabs.getTabs().add(tab);
            });

            if (scriptingTab != null) {
                Tab scriptTab = new Tab("Script");
                scriptTab.setContent(scriptingTab);
                tabs.getTabs().add(scriptTab);
            }

            // Track current tree view for clipboard operations
            tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null && newTab.getContent() instanceof AnchorPane) {
                    AnchorPane anchor = (AnchorPane) newTab.getContent();
                    if (!anchor.getChildren().isEmpty() && anchor.getChildren().get(0) instanceof SplitPane) {
                        SplitPane split = (SplitPane) anchor.getChildren().get(0);
                        if (!split.getItems().isEmpty() && split.getItems().get(0) instanceof VBox) {
                            VBox vbox = (VBox) split.getItems().get(0);
                            for (Node node : vbox.getChildren()) {
                                if (node instanceof TreeView) {
                                    currentTreeView = (TreeView<Object>) node;
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        });

        progressBar.visibleProperty().bind(editor.workingProperty());
    }

    private void initializeThemeMenu() {
        RadioMenuItem darkTheme = new RadioMenuItem("Dark");
        darkTheme.setToggleGroup(themeGroup);
        darkTheme.setOnAction(e -> applyTheme("dark"));

        RadioMenuItem lightTheme = new RadioMenuItem("Light");
        lightTheme.setToggleGroup(themeGroup);
        lightTheme.setOnAction(e -> applyTheme("light"));

        themeMenu.getItems().addAll(darkTheme, lightTheme);

        String currentTheme = XdatEditor.getPrefs().get("theme", "dark");
        if ("light".equals(currentTheme)) {
            lightTheme.setSelected(true);
        } else {
            darkTheme.setSelected(true);
        }
        applyTheme(currentTheme);
    }

    private void applyTheme(String theme) {
        XdatEditor.getPrefs().put("theme", theme);

        // Check if scene is available (it's null during FXML initialization)
        if (editor.getStage().getScene() == null) {
            return; // Theme will be applied by XdatEditor.applyInitialTheme()
        }

        String cssPath = "light".equals(theme)
                ? getClass().getResource("light-theme.css").toExternalForm()
                : getClass().getResource("dark-theme.css").toExternalForm();
        editor.getStage().getScene().getStylesheets().clear();
        editor.getStage().getScene().getStylesheets().add(cssPath);
    }

    private void initializeLanguageMenu() {
        Map<String, Locale> languages = new LinkedHashMap<>();
        languages.put("English", Locale.ENGLISH);
        languages.put("Русский", new Locale("ru"));
        languages.put("Português (Brasil)", new Locale("pt", "BR"));

        String currentLanguage = XdatEditor.getPrefs().get("language", "en");

        for (Map.Entry<String, Locale> entry : languages.entrySet()) {
            RadioMenuItem item = new RadioMenuItem(entry.getKey());
            item.setToggleGroup(languageGroup);
            String languageCode = entry.getValue().getLanguage();
            item.setOnAction(e -> changeLanguage(languageCode));
            languageMenu.getItems().add(item);

            if (currentLanguage.equals(languageCode)) {
                item.setSelected(true);
            }
        }
    }

    private void changeLanguage(String language) {
        XdatEditor.getPrefs().put("language", language);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(interfaceResources.getString("language.restart_title"));
        alert.setHeaderText(null);
        alert.setContentText(interfaceResources.getString("language.restart_message"));
        alert.showAndWait();
    }

    private void initializeClipboard() {
        clipboardManager = ClipboardManager.getInstance();

        // Bind copy/cut/paste menu items
        BooleanBinding nullXdatObject = Bindings.isNull(editor.xdatObjectProperty());
        copyMenuItem.disableProperty().bind(nullXdatObject);
        cutMenuItem.disableProperty().bind(nullXdatObject);
        duplicateMenuItem.disableProperty().bind(nullXdatObject);
        exportWindowMenuItem.disableProperty().bind(nullXdatObject);
        importWindowMenuItem.disableProperty().bind(nullXdatObject);
        pasteMenuItem.disableProperty().bind(
                nullXdatObject.or(clipboardManager.hasContentProperty().not())
        );
    }

    private void initializeRecentFiles() {
        fileOps.getRecentFilesManager().getRecentFiles().addListener(
                (javafx.collections.ListChangeListener<acmi.l2.clientmod.xdat.util.RecentFilesManager.RecentFile>) c -> {
                    fileOps.updateRecentFilesMenu(recentFilesMenu);
                });
        fileOps.updateRecentFilesMenu(recentFilesMenu);
    }

    public void registerVersion(String name, String xdatClass) {
        RadioMenuItem menuItem = new RadioMenuItem(name);
        menuItem.setMnemonicParsing(false);
        menuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                fileOps.setCurrentVersionName(name);
                editor.execute(() -> {
                    Class<? extends IOEntity> clazz = Class.forName(xdatClass, true,
                            new GroovyClassLoader(getClass().getClassLoader())).asSubclass(IOEntity.class);
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

    // ========== FXML Actions - File Operations ==========

    @FXML
    private void open() {
        fileOps.open();
    }

    @FXML
    private void save() {
        fileOps.save();
    }

    @FXML
    private void saveAs() {
        fileOps.saveAs();
    }

    // ========== FXML Actions - Edit Operations ==========

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
            TreeItem<Object> newTreeItem = TreeManager.createTreeItem(elementToAdd);
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

            editor.getHistory().valueCreated(treeManager.treeItemToScriptString(targetParent), elementToAdd.getClass());
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
            TreeItem<Object> newTreeItem = TreeManager.createTreeItem(clone);
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

            editor.getHistory().valueCreated(treeManager.treeItemToScriptString(selected.getParent()), clone.getClass());
            log.info("Duplicated element: " + clone);
        }
    }

    @FXML
    private void exportWindow() {
        importExport.exportWindow(getSelectedTreeItem());
    }

    @FXML
    private void importWindow() {
        importExport.importWindow(currentTreeView, treeManager::treeItemToScriptString);
    }

    // ========== FXML Actions - Other ==========

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
        VBox.setMargin(license, new Insets(20, 0, 0, 0));
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
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

    // ========== Utility Methods ==========

    private TreeItem<Object> getSelectedTreeItem() {
        if (currentTreeView == null) {
            return null;
        }
        return currentTreeView.getSelectionModel().getSelectedItem();
    }
}
