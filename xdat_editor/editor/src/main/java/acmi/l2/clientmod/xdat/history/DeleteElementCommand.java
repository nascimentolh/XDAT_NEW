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
package acmi.l2.clientmod.xdat.history;

import acmi.l2.clientmod.util.IOEntity;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * Command for deleting elements.
 * Stores the element and its position to allow undo/redo.
 */
public class DeleteElementCommand implements Command {

    private final List<IOEntity> list;
    private final TreeItem<Object> parentTreeItem;
    private final IOEntity element;
    private final TreeItem<Object> elementTreeItem;
    private final int index;
    private final String elementName;
    private final long timestamp;

    public DeleteElementCommand(List<IOEntity> list, TreeItem<Object> parentTreeItem,
                                 IOEntity element, TreeItem<Object> elementTreeItem, int index) {
        this.list = list;
        this.parentTreeItem = parentTreeItem;
        this.element = element;
        this.elementTreeItem = elementTreeItem;
        this.index = index;
        this.elementName = element.toString();
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        list.remove(element);
        parentTreeItem.getChildren().remove(elementTreeItem);
    }

    @Override
    public void undo() {
        if (index >= 0 && index <= list.size()) {
            list.add(index, element);
            parentTreeItem.getChildren().add(index, elementTreeItem);
        } else {
            list.add(element);
            parentTreeItem.getChildren().add(elementTreeItem);
        }
    }

    @Override
    public String getDescription() {
        return String.format("Delete: %s", elementName);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public IOEntity getElement() {
        return element;
    }

    public int getIndex() {
        return index;
    }
}
