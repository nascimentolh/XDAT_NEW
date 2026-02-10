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
import acmi.l2.clientmod.l2resources.L2Resources;
import acmi.l2.clientmod.unreal.Environment;
import org.apache.commons.io.input.CountingInputStream;
import acmi.l2.clientmod.util.IOEntity;
import acmi.l2.clientmod.util.IOUtil;
import acmi.l2.clientmod.xdat.propertyeditor.SysstringPropertyEditor;
import acmi.l2.clientmod.xdat.propertyeditor.TexturePropertyEditor;
import acmi.l2.clientmod.xdat.util.RecentFilesManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.*;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

class FileOperationsManager {
    private static final Logger log = Logger.getLogger(FileOperationsManager.class.getName());

    private final XdatEditor editor;
    private final ResourceBundle resources;
    private final RecentFilesManager recentFilesManager;

    private final ObjectProperty<File> initialDirectory;
    private final ObjectProperty<File> xdatFile;
    private final ObjectProperty<Environment> environment;
    private final ObjectProperty<L2Resources> l2resources;

    private String currentVersionName;

    FileOperationsManager(XdatEditor editor, ResourceBundle resources) {
        this.editor = editor;
        this.resources = resources;
        this.recentFilesManager = new RecentFilesManager(XdatEditor.getPrefs());

        // Initialize properties
        this.initialDirectory = new SimpleObjectProperty<>(this, "initialDirectory",
                new File(XdatEditor.getPrefs().get("initialDirectory", System.getProperty("user.dir"))));
        this.xdatFile = new SimpleObjectProperty<>(this, "xdatFile");
        this.environment = new SimpleObjectProperty<>(this, "environment");
        this.l2resources = new SimpleObjectProperty<>(this, "l2resources");

        // Set up bindings
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

        // Set up xdatFile change listener for SysString and Texture loading
        xdatFile.addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                return;

            // Load SysString
            Collection<File> files = FileUtils.listFiles(newValue.getParentFile(),
                    new WildcardFileFilter("SysString-*.dat"), null);
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

            // Set up TexturePropertyEditor environment
            File file = new File(newValue.getParentFile(), "L2.ini");
            try {
                TexturePropertyEditor.environment = Environment.fromIni(file);
                TexturePropertyEditor.environment.getPaths().forEach(s -> log.info("environment path: " + s));
            } catch (Exception ignore) {
            }
        });
    }

    void open() {
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

    void openFileDirectly(File file) {
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

    void save() {
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

    void saveAs() {
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

    void openRecentFile(RecentFilesManager.RecentFile recentFile, ToggleGroup versionGroup) {
        if (!recentFile.exists()) {
            recentFilesManager.removeRecentFile(recentFile.getFile());
            Dialogs.showException(Alert.AlertType.WARNING, "File not found",
                    "The file no longer exists: " + recentFile.getPath(), null);
            return;
        }

        // Try to find and select the version
        if (recentFile.getVersionName() != null) {
            for (Toggle toggle : versionGroup.getToggles()) {
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

    void updateRecentFilesMenu(Menu recentFilesMenu) {
        recentFilesMenu.getItems().clear();

        if (recentFilesManager.getRecentFiles().isEmpty()) {
            MenuItem emptyItem = new MenuItem(resources.getString("file.recent.empty"));
            emptyItem.setDisable(true);
            recentFilesMenu.getItems().add(emptyItem);
        } else {
            for (RecentFilesManager.RecentFile recentFile : recentFilesManager.getRecentFiles()) {
                MenuItem item = new MenuItem(recentFile.toString());
                item.setOnAction(e -> openRecentFile(recentFile, null));  // ToggleGroup will be passed from Controller
                recentFilesMenu.getItems().add(item);
            }

            recentFilesMenu.getItems().add(new SeparatorMenuItem());

            MenuItem clearItem = new MenuItem(resources.getString("file.recent.clear"));
            clearItem.setOnAction(e -> recentFilesManager.clearRecentFiles());
            recentFilesMenu.getItems().add(clearItem);
        }
    }

    // Property accessors
    ObjectProperty<File> xdatFileProperty() {
        return xdatFile;
    }

    ObjectProperty<File> initialDirectoryProperty() {
        return initialDirectory;
    }

    ObjectProperty<Environment> environmentProperty() {
        return environment;
    }

    ObjectProperty<L2Resources> l2resourcesProperty() {
        return l2resources;
    }

    RecentFilesManager getRecentFilesManager() {
        return recentFilesManager;
    }

    String getCurrentVersionName() {
        return currentVersionName;
    }

    void setCurrentVersionName(String name) {
        this.currentVersionName = name;
    }
}
