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
package acmi.l2.clientmod.xdat.search;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Encapsulates search criteria and matching logic.
 */
public class SearchCriteria {

    private final String searchText;
    private final String typeFilter;
    private final String propertyFilter;
    private final boolean useRegex;
    private Pattern regexPattern;

    public SearchCriteria(String searchText, String typeFilter, String propertyFilter, boolean useRegex) {
        this.searchText = searchText != null ? searchText : "";
        this.typeFilter = typeFilter;
        this.propertyFilter = propertyFilter;
        this.useRegex = useRegex;

        if (useRegex && !this.searchText.isEmpty()) {
            try {
                this.regexPattern = Pattern.compile(this.searchText, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                this.regexPattern = null;
            }
        }
    }

    public boolean isEmpty() {
        return searchText.isEmpty() && typeFilter == null && propertyFilter == null;
    }

    public boolean matches(Object item) {
        if (item == null) {
            return false;
        }

        // Check type filter
        if (typeFilter != null && !typeFilter.isEmpty()) {
            String itemType = item.getClass().getSimpleName();
            if (!itemType.equals(typeFilter)) {
                return false;
            }
        }

        // If no search text, type match is enough
        if (searchText.isEmpty()) {
            return true;
        }

        // Check property filter
        if (propertyFilter != null && !propertyFilter.isEmpty()) {
            return matchesProperty(item, propertyFilter);
        }

        // Search in name/toString and common properties
        return matchesName(Objects.toString(item)) || matchesCommonProperties(item);
    }

    private boolean matchesName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        if (useRegex && regexPattern != null) {
            Matcher matcher = regexPattern.matcher(name);
            return matcher.find();
        } else {
            return name.toLowerCase().contains(searchText.toLowerCase());
        }
    }

    private boolean matchesProperty(Object item, String propertyName) {
        try {
            Object value = getPropertyValue(item, propertyName);
            if (value != null) {
                return matchesName(value.toString());
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean matchesCommonProperties(Object item) {
        // Search in common properties: name, text, file, fontName, styleName
        String[] commonProps = {"name", "text", "buttonNameText", "titleText", "file", "normalTex", "backTex"};

        for (String prop : commonProps) {
            try {
                Object value = getPropertyValue(item, prop);
                if (value != null && matchesName(value.toString())) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private Object getPropertyValue(Object item, String propertyName) {
        Class<?> clazz = item.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(propertyName);
                field.setAccessible(true);
                return field.get(item);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    public String getSearchText() {
        return searchText;
    }

    public String getTypeFilter() {
        return typeFilter;
    }

    public String getPropertyFilter() {
        return propertyFilter;
    }

    public boolean isUseRegex() {
        return useRegex;
    }
}
