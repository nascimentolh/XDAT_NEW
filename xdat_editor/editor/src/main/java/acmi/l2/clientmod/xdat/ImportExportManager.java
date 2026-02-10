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
import acmi.l2.clientmod.xdat.history.ImportWindowCommand;
import acmi.l2.clientmod.xdat.util.WindowExporter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.logging.Logger;

class ImportExportManager {
    private static final Logger log = Logger.getLogger(ImportExportManager.class.getName());

    private final XdatEditor editor;
    private final ResourceBundle resources;
    private final FileOperationsManager fileOps;

    ImportExportManager(XdatEditor editor, ResourceBundle resources, FileOperationsManager fileOps) {
        this.editor = editor;
        this.resources = resources;
        this.fileOps = fileOps;
    }

    void exportWindow(TreeItem<Object> selected) {
        if (selected == null || selected.getValue() instanceof ListHolder) {
            Dialogs.showException(Alert.AlertType.WARNING,
                    resources.getString("export.select_element"),
                    resources.getString("export.select_element_message"), null);
            return;
        }

        Object value = selected.getValue();
        if (!(value instanceof IOEntity)) {
            return;
        }

        // Verify it's inside a list (valid for export)
        if (selected.getParent() == null ||
                !(selected.getParent().getValue() instanceof ListHolder)) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("export.title"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(WindowExporter.getFileFilterName() + " (" + WindowExporter.getFileExtension() + ")", WindowExporter.getFileExtension()),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        // Suggest filename based on element name
        String suggestedName = value.toString().replaceAll("[^a-zA-Z0-9_-]", "_");
        fileChooser.setInitialFileName(suggestedName + ".xdatwin");

        if (fileOps.initialDirectoryProperty().getValue() != null &&
                fileOps.initialDirectoryProperty().getValue().exists() &&
                fileOps.initialDirectoryProperty().getValue().isDirectory()) {
            fileChooser.setInitialDirectory(fileOps.initialDirectoryProperty().getValue());
        }

        File file = fileChooser.showSaveDialog(editor.getStage());
        if (file == null) {
            return;
        }

        fileOps.initialDirectoryProperty().setValue(file.getParentFile());

        editor.execute(() -> {
            WindowExporter.exportWindow((IOEntity) value, file);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(resources.getString("export.success"));
                alert.setHeaderText(null);
                alert.setContentText(String.format(
                        resources.getString("export.success_message"),
                        file.getName()));
                alert.showAndWait();
            });
            return null;
        }, e -> Dialogs.showException(Alert.AlertType.ERROR,
                resources.getString("export.error"), e.getMessage(), e));
    }

    void importWindow(TreeView<Object> treeView, Function<TreeItem, String> scriptStringFn) {
        // Ensure we have a valid target list
        if (treeView == null || treeView.getRoot() == null) {
            Dialogs.showException(Alert.AlertType.WARNING,
                    resources.getString("import.no_file"),
                    resources.getString("import.open_file_first"), null);
            return;
        }

        TreeItem<Object> rootItem = treeView.getRoot();
        if (!(rootItem.getValue() instanceof ListHolder)) {
            return;
        }

        ListHolder listHolder = (ListHolder) rootItem.getValue();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("import.title"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(WindowExporter.getFileFilterName() + " (" + WindowExporter.getFileExtension() + ")", WindowExporter.getFileExtension()),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        if (fileOps.initialDirectoryProperty().getValue() != null &&
                fileOps.initialDirectoryProperty().getValue().exists() &&
                fileOps.initialDirectoryProperty().getValue().isDirectory()) {
            fileChooser.setInitialDirectory(fileOps.initialDirectoryProperty().getValue());
        }

        File file = fileChooser.showOpenDialog(editor.getStage());
        if (file == null) {
            return;
        }

        fileOps.initialDirectoryProperty().setValue(file.getParentFile());

        editor.execute(() -> {
            // Import using the same classloader and package as the current schema
            ClassLoader classLoader = listHolder.type.getClassLoader();
            String packageName = listHolder.type.getPackage().getName();
            IOEntity importedElement = WindowExporter.importWindow(file, classLoader, packageName);

            // Verify type compatibility
            if (!listHolder.type.isAssignableFrom(importedElement.getClass())) {
                Platform.runLater(() -> {
                    Dialogs.showException(Alert.AlertType.ERROR,
                            resources.getString("import.incompatible"),
                            String.format(resources.getString("import.incompatible_message"),
                                    importedElement.getClass().getSimpleName(),
                                    listHolder.type.getSimpleName()),
                            null);
                });
                return null;
            }

            Platform.runLater(() -> {
                TreeItem<Object> newTreeItem = TreeManager.createTreeItem(importedElement);

                // Create and record command for undo support
                ImportWindowCommand command = new ImportWindowCommand(
                        listHolder.list, rootItem, importedElement, newTreeItem, file.getName());
                command.execute();
                editor.getUndoManager().record(command);

                // Select the imported element
                treeView.getSelectionModel().select(newTreeItem);
                treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex());

                editor.getHistory().valueCreated(scriptStringFn.apply(rootItem),
                        importedElement.getClass());
                log.info("Imported element: " + importedElement + " from " + file.getName());
            });

            return null;
        }, e -> Dialogs.showException(Alert.AlertType.ERROR,
                resources.getString("import.error"), e.getMessage(), e));
    }
}
