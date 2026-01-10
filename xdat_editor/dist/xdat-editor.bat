@echo off
setlocal
cd /d "%~dp0"

REM Try running with bundled JavaFX first
java --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar xdat-editor-1.3.10.jar %* 2>nul
if %errorlevel%==0 goto :eof

REM Fall back to external JavaFX SDK
set JAVAFX_PATH=%JAVAFX_HOME%\lib
if "%JAVAFX_HOME%"=="" set JAVAFX_PATH=%USERPROFILE%\javafx-sdk-21\lib

if exist "%JAVAFX_PATH%\javafx.controls.jar" (
    java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar xdat-editor-1.3.10.jar %*
) else (
    echo JavaFX not found!
    echo.
    echo Options:
    echo 1. Use a JDK with JavaFX bundled (Azul Zulu FX, Liberica Full)
    echo 2. Download JavaFX SDK and set JAVAFX_HOME environment variable
    pause
)
