#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Check if JavaFX is bundled in JDK
if java --module-path . --list-modules 2>/dev/null | grep -q "javafx.controls"; then
    # JavaFX bundled - run directly
    java --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar xdat-editor-1.3.10.jar "$@"
else
    # External JavaFX SDK needed
    JAVAFX_PATH="${JAVAFX_HOME:-$HOME/javafx-sdk-21}/lib"
    if [ -d "$JAVAFX_PATH" ]; then
        java --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar xdat-editor-1.3.10.jar "$@"
    else
        echo "JavaFX not found!"
        echo ""
        echo "Options:"
        echo "1. Use a JDK with JavaFX bundled:"
        echo "   - Azul Zulu with JavaFX"
        echo "   - Liberica Full JDK"
        echo ""
        echo "2. Download JavaFX SDK and set JAVAFX_HOME:"
        echo "   export JAVAFX_HOME=/path/to/javafx-sdk-21"
        exit 1
    fi
fi
