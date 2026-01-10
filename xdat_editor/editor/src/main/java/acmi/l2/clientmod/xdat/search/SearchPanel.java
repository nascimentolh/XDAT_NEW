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

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Advanced search panel for filtering tree elements.
 */
public class SearchPanel extends VBox {

    private final TextField searchField;
    private final ComboBox<String> typeFilter;
    private final ComboBox<String> propertyFilter;
    private final CheckBox regexCheckBox;
    private final Button clearButton;
    private final Label statusLabel;

    private final StringProperty searchText = new SimpleStringProperty("");
    private final StringProperty selectedType = new SimpleStringProperty(null);
    private final StringProperty selectedProperty = new SimpleStringProperty(null);
    private final BooleanProperty useRegex = new SimpleBooleanProperty(false);
    private final IntegerProperty resultCount = new SimpleIntegerProperty(0);

    private final ObservableList<String> componentTypes = FXCollections.observableArrayList();
    private final ObservableList<String> propertyNames = FXCollections.observableArrayList();

    public SearchPanel(ResourceBundle resources) {
        setSpacing(8);
        setPadding(new Insets(8));
        getStyleClass().add("search-panel");

        // First row: Search field and type filter
        HBox row1 = new HBox(8);
        row1.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText(resources.getString("search.placeholder"));
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().bindBidirectional(searchText);

        Label typeLabel = new Label(resources.getString("search.type"));
        typeFilter = new ComboBox<>(componentTypes);
        typeFilter.setPromptText(resources.getString("search.all_types"));
        typeFilter.setPrefWidth(150);
        typeFilter.valueProperty().bindBidirectional(selectedType);

        row1.getChildren().addAll(searchField, typeLabel, typeFilter);

        // Second row: Property filter, regex checkbox, clear button
        HBox row2 = new HBox(8);
        row2.setAlignment(Pos.CENTER_LEFT);

        Label propLabel = new Label(resources.getString("search.property"));
        propertyFilter = new ComboBox<>(propertyNames);
        propertyFilter.setPromptText(resources.getString("search.all_properties"));
        propertyFilter.setPrefWidth(150);
        propertyFilter.valueProperty().bindBidirectional(selectedProperty);

        regexCheckBox = new CheckBox(resources.getString("search.regex"));
        regexCheckBox.selectedProperty().bindBidirectional(useRegex);
        regexCheckBox.setTooltip(new Tooltip(resources.getString("search.regex_tooltip")));

        clearButton = new Button(resources.getString("search.clear"));
        clearButton.getStyleClass().add("clear-button");
        clearButton.setOnAction(e -> clearFilters());

        statusLabel = new Label();
        statusLabel.getStyleClass().add("search-status");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        row2.getChildren().addAll(propLabel, propertyFilter, regexCheckBox, clearButton, statusLabel);

        getChildren().addAll(row1, row2);

        // Update status label when result count changes
        resultCount.addListener((obs, oldVal, newVal) -> {
            if (hasActiveFilters()) {
                statusLabel.setText(String.format(resources.getString("search.results"), newVal.intValue()));
            } else {
                statusLabel.setText("");
            }
        });

        // Validate regex when checkbox is toggled or text changes
        useRegex.addListener((obs, oldVal, newVal) -> validateRegex());
        searchText.addListener((obs, oldVal, newVal) -> validateRegex());
    }

    private void validateRegex() {
        if (useRegex.get() && !searchText.get().isEmpty()) {
            try {
                Pattern.compile(searchText.get());
                searchField.getStyleClass().remove("error");
            } catch (PatternSyntaxException e) {
                if (!searchField.getStyleClass().contains("error")) {
                    searchField.getStyleClass().add("error");
                }
            }
        } else {
            searchField.getStyleClass().remove("error");
        }
    }

    public void clearFilters() {
        searchText.set("");
        selectedType.set(null);
        selectedProperty.set(null);
        useRegex.set(false);
        typeFilter.getSelectionModel().clearSelection();
        propertyFilter.getSelectionModel().clearSelection();
    }

    public boolean hasActiveFilters() {
        return !searchText.get().isEmpty() ||
                selectedType.get() != null ||
                selectedProperty.get() != null;
    }

    public void setComponentTypes(java.util.List<String> types) {
        componentTypes.clear();
        componentTypes.addAll(types);
    }

    public void setPropertyNames(java.util.List<String> properties) {
        propertyNames.clear();
        propertyNames.addAll(properties);
    }

    // Property accessors
    public StringProperty searchTextProperty() {
        return searchText;
    }

    public String getSearchText() {
        return searchText.get();
    }

    public StringProperty selectedTypeProperty() {
        return selectedType;
    }

    public String getSelectedType() {
        return selectedType.get();
    }

    public StringProperty selectedPropertyProperty() {
        return selectedProperty;
    }

    public String getSelectedProperty() {
        return selectedProperty.get();
    }

    public BooleanProperty useRegexProperty() {
        return useRegex;
    }

    public boolean isUseRegex() {
        return useRegex.get();
    }

    public IntegerProperty resultCountProperty() {
        return resultCount;
    }

    public void setResultCount(int count) {
        resultCount.set(count);
    }

    public TextField getSearchField() {
        return searchField;
    }
}
