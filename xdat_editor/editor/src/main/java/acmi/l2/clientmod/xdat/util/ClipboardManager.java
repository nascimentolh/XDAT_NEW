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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Manages clipboard operations for IOEntity elements.
 * Supports copy, cut, and paste operations with deep cloning.
 */
public class ClipboardManager {

    private static ClipboardManager instance;

    private IOEntity clipboardContent;
    private boolean isCut;
    private final BooleanProperty hasContent = new SimpleBooleanProperty(false);

    private ClipboardManager() {
    }

    public static synchronized ClipboardManager getInstance() {
        if (instance == null) {
            instance = new ClipboardManager();
        }
        return instance;
    }

    /**
     * Copies an element to the clipboard (deep clone).
     *
     * @param element the element to copy
     */
    public void copy(IOEntity element) {
        if (element != null) {
            this.clipboardContent = ElementCloner.deepClone(element);
            this.isCut = false;
            hasContent.set(true);
        }
    }

    /**
     * Cuts an element to the clipboard (marks for removal on paste).
     *
     * @param element the element to cut
     */
    public void cut(IOEntity element) {
        if (element != null) {
            this.clipboardContent = element;
            this.isCut = true;
            hasContent.set(true);
        }
    }

    /**
     * Pastes the clipboard content.
     * Returns a deep clone for paste operations.
     *
     * @return a clone of the clipboard content, or null if empty
     */
    public IOEntity paste() {
        if (clipboardContent == null) {
            return null;
        }

        if (isCut) {
            // For cut, return the original and clear clipboard
            IOEntity result = clipboardContent;
            clear();
            return result;
        } else {
            // For copy, return a new clone each time
            return ElementCloner.deepClone(clipboardContent);
        }
    }

    /**
     * Peeks at the clipboard content without removing it.
     *
     * @return the clipboard content type name, or null if empty
     */
    public String getContentTypeName() {
        return clipboardContent != null ? clipboardContent.getClass().getSimpleName() : null;
    }

    /**
     * Gets the clipboard content class.
     *
     * @return the class of clipboard content, or null if empty
     */
    public Class<? extends IOEntity> getContentClass() {
        return clipboardContent != null ? clipboardContent.getClass() : null;
    }

    /**
     * Checks if there is content in the clipboard.
     *
     * @return true if clipboard has content
     */
    public boolean hasContent() {
        return clipboardContent != null;
    }

    /**
     * Checks if the clipboard operation was a cut.
     *
     * @return true if content was cut, false if copied
     */
    public boolean isCut() {
        return isCut;
    }

    /**
     * Clears the clipboard.
     */
    public void clear() {
        clipboardContent = null;
        isCut = false;
        hasContent.set(false);
    }

    /**
     * Property indicating if clipboard has content.
     *
     * @return the hasContent property
     */
    public BooleanProperty hasContentProperty() {
        return hasContent;
    }
}
