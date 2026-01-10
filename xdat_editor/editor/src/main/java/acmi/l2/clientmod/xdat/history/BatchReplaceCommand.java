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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Command for batch replace operations.
 * Stores all replacements to allow undo/redo of the entire batch.
 */
public class BatchReplaceCommand implements Command {

    private final List<ReplacementEntry> replacements;
    private final String searchText;
    private final String replaceText;
    private final long timestamp;

    public BatchReplaceCommand(String searchText, String replaceText) {
        this.replacements = new ArrayList<>();
        this.searchText = searchText;
        this.replaceText = replaceText;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Adds a replacement entry to the batch.
     */
    public void addReplacement(Object target, Field field, Object oldValue, Object newValue) {
        replacements.add(new ReplacementEntry(target, field, oldValue, newValue));
    }

    /**
     * Returns the number of replacements in the batch.
     */
    public int getReplacementCount() {
        return replacements.size();
    }

    /**
     * Checks if the batch has any replacements.
     */
    public boolean hasReplacements() {
        return !replacements.isEmpty();
    }

    @Override
    public void execute() {
        for (ReplacementEntry entry : replacements) {
            try {
                entry.field.setAccessible(true);
                entry.field.set(entry.target, entry.newValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void undo() {
        // Undo in reverse order
        for (int i = replacements.size() - 1; i >= 0; i--) {
            ReplacementEntry entry = replacements.get(i);
            try {
                entry.field.setAccessible(true);
                entry.field.set(entry.target, entry.oldValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getDescription() {
        return String.format("Replace '%s' with '%s' (%d occurrences)",
                searchText, replaceText, replacements.size());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public String getSearchText() {
        return searchText;
    }

    public String getReplaceText() {
        return replaceText;
    }

    /**
     * Internal class to store individual replacement information.
     */
    private static class ReplacementEntry {
        final Object target;
        final Field field;
        final Object oldValue;
        final Object newValue;

        ReplacementEntry(Object target, Field field, Object oldValue, Object newValue) {
            this.target = target;
            this.field = field;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
}
