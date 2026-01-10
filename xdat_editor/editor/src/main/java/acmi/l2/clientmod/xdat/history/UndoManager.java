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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages undo/redo operations using the Command pattern.
 * Maintains two stacks: one for undo operations and one for redo operations.
 */
public class UndoManager {

    private static final int DEFAULT_MAX_HISTORY = 100;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private final ObservableList<Command> historyList = FXCollections.observableArrayList();

    private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private final BooleanProperty canRedo = new SimpleBooleanProperty(false);
    private final BooleanProperty modified = new SimpleBooleanProperty(false);

    private int maxHistory;
    private boolean isExecutingCommand = false;
    private Command savedStateCommand = null;

    public UndoManager() {
        this(DEFAULT_MAX_HISTORY);
    }

    public UndoManager(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    /**
     * Executes a command and adds it to the undo stack.
     * Clears the redo stack since a new action invalidates any redo history.
     */
    public void execute(Command command) {
        if (isExecutingCommand) {
            return;
        }

        isExecutingCommand = true;
        try {
            command.execute();
            undoStack.push(command);
            historyList.add(0, command);
            redoStack.clear();

            // Trim history if it exceeds max size
            while (undoStack.size() > maxHistory) {
                Command removed = ((ArrayDeque<Command>) undoStack).removeLast();
                historyList.remove(removed);
            }

            updateState();
        } finally {
            isExecutingCommand = false;
        }
    }

    /**
     * Records a command without executing it.
     * Useful when the action has already been performed.
     */
    public void record(Command command) {
        if (isExecutingCommand) {
            return;
        }

        undoStack.push(command);
        historyList.add(0, command);
        redoStack.clear();

        // Trim history if it exceeds max size
        while (undoStack.size() > maxHistory) {
            Command removed = ((ArrayDeque<Command>) undoStack).removeLast();
            historyList.remove(removed);
        }

        updateState();
    }

    /**
     * Undoes the last command.
     */
    public void undo() {
        if (!canUndo()) {
            return;
        }

        isExecutingCommand = true;
        try {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            updateState();
        } finally {
            isExecutingCommand = false;
        }
    }

    /**
     * Redoes the last undone command.
     */
    public void redo() {
        if (!canRedo()) {
            return;
        }

        isExecutingCommand = true;
        try {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            updateState();
        } finally {
            isExecutingCommand = false;
        }
    }

    /**
     * Clears all history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        historyList.clear();
        savedStateCommand = null;
        updateState();
    }

    /**
     * Marks the current state as saved.
     * Used to track if the document has been modified since last save.
     */
    public void markSaved() {
        savedStateCommand = undoStack.isEmpty() ? null : undoStack.peek();
        updateState();
    }

    /**
     * Checks if we can undo.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks if we can redo.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Checks if the document has been modified since last save.
     */
    public boolean isModified() {
        if (undoStack.isEmpty()) {
            return savedStateCommand != null;
        }
        return undoStack.peek() != savedStateCommand;
    }

    /**
     * Returns the description of the next undo operation.
     */
    public String getUndoDescription() {
        if (undoStack.isEmpty()) {
            return null;
        }
        return undoStack.peek().getDescription();
    }

    /**
     * Returns the description of the next redo operation.
     */
    public String getRedoDescription() {
        if (redoStack.isEmpty()) {
            return null;
        }
        return redoStack.peek().getDescription();
    }

    /**
     * Returns the observable list of all commands in history.
     * Most recent commands are at the beginning of the list.
     */
    public ObservableList<Command> getHistoryList() {
        return historyList;
    }

    /**
     * Returns the number of commands in the undo stack.
     */
    public int getUndoStackSize() {
        return undoStack.size();
    }

    /**
     * Returns the number of commands in the redo stack.
     */
    public int getRedoStackSize() {
        return redoStack.size();
    }

    public BooleanProperty canUndoProperty() {
        return canUndo;
    }

    public BooleanProperty canRedoProperty() {
        return canRedo;
    }

    public BooleanProperty modifiedProperty() {
        return modified;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
        // Trim if necessary
        while (undoStack.size() > maxHistory) {
            Command removed = ((ArrayDeque<Command>) undoStack).removeLast();
            historyList.remove(removed);
        }
    }

    /**
     * Checks if a command is currently being executed.
     * Used to prevent recursive command recording.
     */
    public boolean isExecutingCommand() {
        return isExecutingCommand;
    }

    private void updateState() {
        canUndo.set(canUndo());
        canRedo.set(canRedo());
        modified.set(isModified());
    }
}
