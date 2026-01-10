/*
 * Copyright (c) 2024 TurtleLess - qesta.com.br
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
 *
 * @author TurtleLess
 * @website qesta.com.br
 */
package acmi.l2.clientmod.xdat.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Manages the list of recently opened files.
 * Persists the list to user preferences.
 */
public class RecentFilesManager {

    private static final String PREF_KEY_PREFIX = "recentFile_";
    private static final String PREF_KEY_VERSION_PREFIX = "recentVersion_";
    private static final int DEFAULT_MAX_RECENT = 10;

    private final Preferences prefs;
    private final int maxRecent;
    private final ObservableList<RecentFile> recentFiles;

    public RecentFilesManager(Preferences prefs) {
        this(prefs, DEFAULT_MAX_RECENT);
    }

    public RecentFilesManager(Preferences prefs, int maxRecent) {
        this.prefs = prefs;
        this.maxRecent = maxRecent;
        this.recentFiles = FXCollections.observableArrayList();
        loadFromPreferences();
    }

    /**
     * Adds a file to the recent files list.
     * If the file already exists, it is moved to the top.
     *
     * @param file        the file to add
     * @param versionName the XDAT version name used to open this file
     */
    public void addRecentFile(File file, String versionName) {
        if (file == null || !file.exists()) {
            return;
        }

        String path = file.getAbsolutePath();

        // Remove if already exists
        recentFiles.removeIf(rf -> rf.getPath().equals(path));

        // Add to the beginning
        recentFiles.add(0, new RecentFile(path, versionName, System.currentTimeMillis()));

        // Trim to max size
        while (recentFiles.size() > maxRecent) {
            recentFiles.remove(recentFiles.size() - 1);
        }

        saveToPreferences();
    }

    /**
     * Removes a file from the recent files list.
     *
     * @param file the file to remove
     */
    public void removeRecentFile(File file) {
        if (file == null) {
            return;
        }

        String path = file.getAbsolutePath();
        recentFiles.removeIf(rf -> rf.getPath().equals(path));
        saveToPreferences();
    }

    /**
     * Clears all recent files.
     */
    public void clearRecentFiles() {
        recentFiles.clear();
        saveToPreferences();
    }

    /**
     * Gets the list of recent files.
     *
     * @return observable list of recent files
     */
    public ObservableList<RecentFile> getRecentFiles() {
        return recentFiles;
    }

    /**
     * Gets the maximum number of recent files to store.
     *
     * @return max recent files count
     */
    public int getMaxRecent() {
        return maxRecent;
    }

    private void loadFromPreferences() {
        recentFiles.clear();

        for (int i = 0; i < maxRecent; i++) {
            String path = prefs.get(PREF_KEY_PREFIX + i, null);
            String version = prefs.get(PREF_KEY_VERSION_PREFIX + i, null);

            if (path != null && !path.isEmpty()) {
                File file = new File(path);
                if (file.exists()) {
                    recentFiles.add(new RecentFile(path, version, 0));
                }
            }
        }
    }

    private void saveToPreferences() {
        // Clear all existing entries
        for (int i = 0; i < maxRecent; i++) {
            prefs.remove(PREF_KEY_PREFIX + i);
            prefs.remove(PREF_KEY_VERSION_PREFIX + i);
        }

        // Save current list
        for (int i = 0; i < recentFiles.size(); i++) {
            RecentFile rf = recentFiles.get(i);
            prefs.put(PREF_KEY_PREFIX + i, rf.getPath());
            if (rf.getVersionName() != null) {
                prefs.put(PREF_KEY_VERSION_PREFIX + i, rf.getVersionName());
            }
        }
    }

    /**
     * Represents a recently opened file with metadata.
     */
    public static class RecentFile {
        private final String path;
        private final String versionName;
        private final long lastOpened;

        public RecentFile(String path, String versionName, long lastOpened) {
            this.path = path;
            this.versionName = versionName;
            this.lastOpened = lastOpened;
        }

        public String getPath() {
            return path;
        }

        public String getVersionName() {
            return versionName;
        }

        public long getLastOpened() {
            return lastOpened;
        }

        public File getFile() {
            return new File(path);
        }

        public String getFileName() {
            return new File(path).getName();
        }

        public boolean exists() {
            return new File(path).exists();
        }

        @Override
        public String toString() {
            String fileName = getFileName();
            if (versionName != null && !versionName.isEmpty()) {
                return String.format("%s [%s]", fileName, versionName);
            }
            return fileName;
        }
    }
}
