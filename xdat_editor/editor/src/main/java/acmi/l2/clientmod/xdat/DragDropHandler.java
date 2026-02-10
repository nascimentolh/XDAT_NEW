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
import acmi.l2.clientmod.xdat.history.MoveElementCommand;
import acmi.l2.clientmod.xdat.history.UndoManager;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.logging.Logger;

class DragDropHandler {
    private static final Logger log = Logger.getLogger(DragDropHandler.class.getName());

    private final UndoManager undoManager;

    DragDropHandler(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    void install(TreeCell<Object> cell, TreeView<Object> treeView) {
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

            Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(System.identityHashCode(draggedItem)));
            db.setContent(content);
            event.consume();
        });

        // Drag over - show drop indicator
        cell.setOnDragOver(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                TreeItem<Object> targetItem = cell.getTreeItem();
                if (targetItem != null && canDropOn(treeView, targetItem)) {
                    event.acceptTransferModes(TransferMode.MOVE);

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
            Dragboard db = event.getDragboard();
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
        undoManager.record(command);

        // Select the moved item
        treeView.getSelectionModel().select(draggedItem);

        log.info("Moved element: " + draggedEntity + " from index " + sourceIndex + " to " + targetIndex);
        return true;
    }
}
