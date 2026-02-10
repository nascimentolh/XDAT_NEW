# Changelog

All notable changes to XDAT Editor will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.1] - 2026-02-10

### Added
- PowerShell launcher script (xdat-editor.ps1) for Windows with better error handling and colored output
- Java 25 full compatibility while maintaining Java 21+ backward compatibility

### Changed
- **Build Dependencies:**
  - Upgraded Lombok from 1.18.30 to 1.18.42 (Java 25 support)
  - Upgraded Groovy from 4.0.18 to 5.0.4 (Java 25 support)
  - Upgraded ASM from 9.6 to 9.9.1 (Java 25 bytecode support)
- Updated README.md with detailed Java 25 compatibility information
- Improved launcher scripts with better JavaFX detection and error messages

### Fixed
- Fixed NullPointerException in theme initialization when scene is not yet created
- Fixed compilation errors from Controller refactoring:
  - Added missing Apache Commons IO import for `CountingInputStream`
  - Fixed JavaFX compatibility: `emptyProperty()` → `Bindings.isEmpty()`
  - Fixed property binding: `progressProperty()` → `workingProperty()`
  - Fixed type compatibility: `wrap(Pane)` → `wrap(Region)`
  - Added missing imports for `Hide` and `Description` annotations
- Fixed Windows .bat file issues with special characters in URLs

### Technical Details
- Compilation target remains Java 21 (`-source 21 -target 21`) for maximum compatibility
- Application runs on JDK 21, 22, 23, 24, 25, and future versions
- All build-time dependencies now support Java 25

## [1.6.0] - 2026-02-10

### Changed
- **Major Code Refactoring:** Refactored Controller.java from 1,944 lines to 619 lines (68% reduction)
  - Split into 8 specialized manager classes:
    - `FileOperationsManager` - File I/O operations
    - `TreeManager` - Tree view management
    - `PropertySheetManager` - Property editing
    - `SearchReplaceManager` - Search and replace functionality
    - `ImportExportManager` - Element import/export
    - `DragDropHandler` - Drag and drop operations
    - `ClassHolder` - Class reference management
    - `ListHolder` - List reference management
  - Improved code maintainability and testability
  - No functional changes to user interface

### Added
- Element import/export feature between different XDAT files
- Language selection menu with support for:
  - English
  - Русский (Russian)
  - Português (Brasil)
- Enhanced checkbox styling in property editors

### Fixed
- Restored search highlight functionality when using drag-and-drop operations

## [1.5.0] and earlier

See git history for older changes.
