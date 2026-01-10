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
 * Command for move operations (drag and drop).
 * Supports moving elements within the same list or between different lists.
 */
public class MoveElementCommand implements Command {

    private final List<IOEntity> sourceList;
    private final List<IOEntity> targetList;
    private final TreeItem<Object> sourceParentTreeItem;
    private final TreeItem<Object> targetParentTreeItem;
    private final IOEntity element;
    private final TreeItem<Object> elementTreeItem;
    private final int sourceIndex;
    private final int targetIndex;
    private final String elementName;
    private final long timestamp;

    /**
     * Creates a move command for moving an element within the same list.
     */
    public MoveElementCommand(List<IOEntity> list, TreeItem<Object> parentTreeItem,
                              IOEntity element, TreeItem<Object> elementTreeItem,
                              int sourceIndex, int targetIndex) {
        this(list, list, parentTreeItem, parentTreeItem, element, elementTreeItem, sourceIndex, targetIndex);
    }

    /**
     * Creates a move command for moving an element between different lists.
     */
    public MoveElementCommand(List<IOEntity> sourceList, List<IOEntity> targetList,
                              TreeItem<Object> sourceParentTreeItem, TreeItem<Object> targetParentTreeItem,
                              IOEntity element, TreeItem<Object> elementTreeItem,
                              int sourceIndex, int targetIndex) {
        this.sourceList = sourceList;
        this.targetList = targetList;
        this.sourceParentTreeItem = sourceParentTreeItem;
        this.targetParentTreeItem = targetParentTreeItem;
        this.element = element;
        this.elementTreeItem = elementTreeItem;
        this.sourceIndex = sourceIndex;
        this.targetIndex = targetIndex;
        this.elementName = element.toString();
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        // Remove from source
        sourceList.remove(sourceIndex);
        sourceParentTreeItem.getChildren().remove(elementTreeItem);

        // Add to target
        int adjustedTargetIndex = targetIndex;
        if (sourceList == targetList && targetIndex > sourceIndex) {
            adjustedTargetIndex--;
        }

        if (adjustedTargetIndex >= 0 && adjustedTargetIndex <= targetList.size()) {
            targetList.add(adjustedTargetIndex, element);
            targetParentTreeItem.getChildren().add(adjustedTargetIndex, elementTreeItem);
        } else {
            targetList.add(element);
            targetParentTreeItem.getChildren().add(elementTreeItem);
        }
    }

    @Override
    public void undo() {
        // Remove from target
        targetList.remove(element);
        targetParentTreeItem.getChildren().remove(elementTreeItem);

        // Add back to source at original position
        if (sourceIndex >= 0 && sourceIndex <= sourceList.size()) {
            sourceList.add(sourceIndex, element);
            sourceParentTreeItem.getChildren().add(sourceIndex, elementTreeItem);
        } else {
            sourceList.add(element);
            sourceParentTreeItem.getChildren().add(elementTreeItem);
        }
    }

    @Override
    public String getDescription() {
        if (sourceList == targetList) {
            return String.format("Move: %s (%d -> %d)", elementName, sourceIndex, targetIndex);
        } else {
            return String.format("Move: %s to different list", elementName);
        }
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public IOEntity getElement() {
        return element;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public int getTargetIndex() {
        return targetIndex;
    }
}
