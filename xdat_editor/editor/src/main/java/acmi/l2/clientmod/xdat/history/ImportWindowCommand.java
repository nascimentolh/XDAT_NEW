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
package acmi.l2.clientmod.xdat.history;

import acmi.l2.clientmod.util.IOEntity;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * Command for import window operations.
 * Supports undo/redo of import actions.
 */
public class ImportWindowCommand implements Command {

    private final List<IOEntity> targetList;
    private final TreeItem<Object> parentTreeItem;
    private final IOEntity element;
    private final TreeItem<Object> elementTreeItem;
    private final String elementName;
    private final String fileName;
    private final long timestamp;

    public ImportWindowCommand(List<IOEntity> targetList, TreeItem<Object> parentTreeItem,
                               IOEntity element, TreeItem<Object> elementTreeItem, String fileName) {
        this.targetList = targetList;
        this.parentTreeItem = parentTreeItem;
        this.element = element;
        this.elementTreeItem = elementTreeItem;
        this.elementName = element.toString();
        this.fileName = fileName;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        targetList.add(element);
        parentTreeItem.getChildren().add(elementTreeItem);
    }

    @Override
    public void undo() {
        targetList.remove(element);
        parentTreeItem.getChildren().remove(elementTreeItem);
    }

    @Override
    public String getDescription() {
        return String.format("Import: %s (from %s)", elementName, fileName);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public IOEntity getElement() {
        return element;
    }

    public String getFileName() {
        return fileName;
    }
}
