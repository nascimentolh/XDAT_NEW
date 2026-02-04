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

import acmi.l2.clientmod.util.IOEntity;
import acmi.l2.clientmod.util.IOUtil;
import acmi.l2.clientmod.util.UIEntity;

import java.io.*;

/**
 * Handles export/import of Window elements to/from standalone files.
 * This allows copying windows between different XDAT files.
 */
public class WindowExporter {

    private static final int MAGIC_NUMBER = 0x5857494E; // "XWIN"
    private static final int FILE_VERSION = 1;

    /**
     * Exports a window (IOEntity) to a standalone file.
     *
     * @param element The window/element to export
     * @param file    The destination file
     * @throws IOException If an I/O error occurs
     */
    public static void exportWindow(IOEntity element, File file) throws IOException {
        // Deep clone to avoid modifying the original
        IOEntity cloned = ElementCloner.deepClone(element);

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            // Write header
            IOUtil.writeInt(os, MAGIC_NUMBER);
            IOUtil.writeInt(os, FILE_VERSION);

            // Write element using UIEntity format (includes class name)
            if (cloned instanceof UIEntity) {
                IOUtil.writeUIEntity(os, (UIEntity) cloned);
            } else {
                // Fallback for non-UIEntity: write class name manually
                IOUtil.writeString(os, cloned.getClass().getSimpleName());
                cloned.write(os);
            }
        }
    }

    /**
     * Imports a window from a standalone file.
     *
     * @param file        The source file
     * @param classLoader The class loader to use for instantiating the window class
     * @param packageName The package name for UIEntity classes
     * @return The imported window
     * @throws IOException If an I/O error occurs or the file format is invalid
     */
    public static IOEntity importWindow(File file, ClassLoader classLoader, String packageName) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            // Validate header
            int magic = IOUtil.readInt(is);
            if (magic != MAGIC_NUMBER) {
                throw new IOException("Invalid window export file format. Expected XWIN header.");
            }

            int version = IOUtil.readInt(is);
            if (version > FILE_VERSION) {
                throw new IOException("File was created with a newer version (v" + version + "). Please update the editor.");
            }

            // Read element using UIEntity format
            IOEntity element = IOUtil.readUIEntity(is, packageName, classLoader);

            // Deep clone to ensure independence
            return ElementCloner.deepClone(element);
        }
    }

    /**
     * Checks if a file is a valid XDAT window export file.
     *
     * @param file The file to check
     * @return true if the file has a valid XWIN header
     */
    public static boolean isValidExportFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            int magic = IOUtil.readInt(is);
            return magic == MAGIC_NUMBER;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Gets the file extension filter description for export files.
     *
     * @return The filter description
     */
    public static String getFileExtension() {
        return "*.xdatwin";
    }

    /**
     * Gets the file extension filter name.
     *
     * @return The filter name
     */
    public static String getFileFilterName() {
        return "XDAT Window";
    }
}
