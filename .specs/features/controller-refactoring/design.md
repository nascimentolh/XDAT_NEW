# Controller Refactoring Design

**Spec**: `.specs/features/controller-refactoring/spec.md`
**Status**: Draft

---

## Architecture Overview

The Controller (1,944 lines) becomes a thin orchestrator (~450 lines) that owns FXML bindings and delegates to 6 focused manager classes. Managers receive dependencies via constructor — no circular references. Two shared data classes (`ListHolder`, `ClassHolder`) are promoted to package-private top-level classes since they're used across multiple managers.

```
┌─────────────────────────────────────────────────────────────┐
│                     main.fxml                                │
│  (binds @FXML methods and fields to Controller)              │
└──────────────────────────┬──────────────────────────────────┘
                           │
              ┌────────────▼─────────────┐
              │     Controller (~450)     │
              │ - FXML wiring + bindings  │
              │ - Manager creation        │
              │ - Clipboard @FXML ops     │
              │ - Theme/Language/About    │
              └─┬───┬───┬───┬───┬───┬────┘
                │   │   │   │   │   │
   ┌────────────┘   │   │   │   │   └────────────┐
   ▼                ▼   │   ▼   ▼                ▼
┌──────────┐ ┌─────────┐│┌──────────┐┌───────────┐┌──────────────┐
│TreeManager│ │FileOps  │││PropertySh││SearchRepl ││ImportExport  │
│  (~400)   │ │Manager  │││eetManager││aceManager ││Manager (~140)│
│           │ │ (~190)  │││  (~120)  ││  (~200)   ││              │
└─────┬─────┘ └─────────┘│└──────────┘└───────────┘└──────────────┘
      │                   │
      ▼                   │
┌──────────────┐          │
│DragDropHandler│◄─────────┘ (installed by TreeManager on cells)
│    (~120)    │
└──────────────┘

Communication: Controller → Managers (method calls)
              Managers → Controller (callbacks/functional interfaces)
```

---

## Code Reuse Analysis

### Existing Components to Leverage

| Component | Location | How to Use |
|-----------|----------|------------|
| ClipboardManager | `xdat/util/ClipboardManager.java` | Keep as-is. Used by Controller for copy/cut/paste |
| RecentFilesManager | `xdat/util/RecentFilesManager.java` | Keep as-is. Moves into FileOperationsManager |
| ElementCloner | `xdat/util/ElementCloner.java` | Keep as-is. Used by clipboard ops and ImportExportManager |
| WindowExporter | `xdat/util/WindowExporter.java` | Keep as-is. Used by ImportExportManager |
| SearchPanel | `xdat/search/SearchPanel.java` | Keep as-is. Passed to TreeManager and SearchReplaceManager |
| SearchCriteria | `xdat/search/SearchCriteria.java` | Keep as-is. Used by TreeManager for filtering |
| UndoManager | `xdat/history/UndoManager.java` | Keep as-is. Shared dependency for most managers |
| History | `xdat/History.java` | Keep as-is. Used for script-based change recording |
| All Command classes | `xdat/history/*.java` | Keep as-is. Each manager creates its own command types |
| PropertySheetItem, FieldProperty | `xdat/propertyeditor/*.java` | Keep as-is. Used by PropertySheetManager |

### Integration Points

| System | Integration Method |
|--------|-------------------|
| main.fxml | Keeps `fx:controller="acmi.l2.clientmod.xdat.Controller"` — no FXML changes |
| XdatEditor | Managers receive XdatEditor reference for `execute()`, properties, stage |
| Preferences | Accessed via static `XdatEditor.getPrefs()` — no change |

---

## Shared Data Classes (New Files)

### ListHolder

- **Purpose**: Data holder for tree nodes representing a List field — wraps the entity, list reference, field name, and element type
- **Location**: `xdat/ListHolder.java` (promoted from Controller inner class)
- **Visibility**: Package-private (used by TreeManager, DragDropHandler, Controller clipboard ops, ImportExportManager)

```java
class ListHolder {
    final IOEntity entity;
    final List<IOEntity> list;
    final String name;
    final Class<? extends IOEntity> type;
    // constructor + toString()
}
```

### ClassHolder

- **Purpose**: Wrapper for class selection dialogs when adding new elements
- **Location**: `xdat/ClassHolder.java` (promoted from Controller inner class)
- **Visibility**: Package-private (used by TreeManager only, but extracted for consistency)

```java
class ClassHolder {
    final Class<? extends IOEntity> clazz;
    // constructor + toString()
}
```

---

## Components

### 1. TreeManager

- **Purpose**: Owns all tree view construction, cell rendering, context menu, and tree item creation
- **Location**: `xdat/TreeManager.java`
- **Lines**: ~400 (absorbs 383 from Controller + icons map + inner classes)

**Constructor dependencies**:
```java
TreeManager(XdatEditor editor, ResourceBundle resources)
```

**Public interface**:
```java
Tab createTab(Field listField, SearchPanel searchPanel,
              Runnable onReplaceSelected, Runnable onReplaceAll)
    // Creates a complete tab with tree + property sheet + search panel

void installXdatListener(InvalidationListener listener)
    // Tracks listeners for cleanup when xdatClass changes

static TreeItem<Object> createTreeItem(IOEntity entity)
    // Builds recursive TreeItem from IOEntity (used by paste/duplicate/import)

String treeItemToScriptString(TreeItem item)
    // Converts TreeItem path to Groovy-like script string for History

PropertySheet createPropertySheet(TreeView<Object> elements)
    // Delegates to PropertySheetManager internally
```

**Owns**:
- `UI_NODE_ICONS` static map (moved from Controller)
- `UI_NODE_ICON_DEFAULT` constant
- `setupTreeViewCellFactory()` — cell rendering + search highlighting (installs DragDropHandler)
- `createContextMenu()` / `updateContextMenu()` — right-click menu
- `createAddMenu()` — add element submenu
- `buildTree()` / `checkTreeNode()` — tree construction with search filtering
- `createTreeView()` — TreeView factory with search listeners

**Internal collaborators**:
- Creates `DragDropHandler` and installs it on tree cells
- Creates `PropertySheetManager` and wires it to tree selection

---

### 2. DragDropHandler

- **Purpose**: Encapsulates all drag-and-drop logic for tree elements
- **Location**: `xdat/DragDropHandler.java`
- **Lines**: ~120

**Constructor dependencies**:
```java
DragDropHandler(UndoManager undoManager)
```

**Public interface**:
```java
void install(TreeCell<Object> cell, TreeView<Object> treeView)
    // Installs all 5 drag event handlers on a tree cell
```

**Owns**:
- `canDropOn(TreeView, TreeItem)` — validates drop target
- `performDrop(TreeView, TreeItem, TreeItem, TreeCell, double)` — executes move + records MoveElementCommand
- All drag event handlers (onDragDetected, onDragOver, onDragExited, onDragDropped, onDragDone)

**Reuses**: `MoveElementCommand`, `ListHolder`

---

### 3. FileOperationsManager

- **Purpose**: Centralizes all file I/O operations and file-related state
- **Location**: `xdat/FileOperationsManager.java`
- **Lines**: ~190

**Constructor dependencies**:
```java
FileOperationsManager(XdatEditor editor, ResourceBundle resources)
```

**Public interface**:
```java
void open()
    // Shows file chooser, validates, reads XDAT file

void openFileDirectly(File file)
    // Opens without file chooser (for recent files)

void save()
    // Writes current xdatObject to xdatFile

void saveAs()
    // Shows save dialog, updates xdatFile, then saves

void openRecentFile(RecentFilesManager.RecentFile recentFile, ToggleGroup versionGroup)
    // Selects version and opens file

void updateRecentFilesMenu(Menu recentFilesMenu)
    // Rebuilds recent files menu items

// Observable state
ObjectProperty<File> xdatFileProperty()
ObjectProperty<File> initialDirectoryProperty()
ObjectProperty<Environment> environmentProperty()
ObjectProperty<L2Resources> l2resourcesProperty()
RecentFilesManager getRecentFilesManager()
String getCurrentVersionName()
void setCurrentVersionName(String name)
```

**Owns**:
- `xdatFile`, `initialDirectory`, `environment`, `l2resources` properties (moved from Controller)
- `recentFilesManager` instance
- `currentVersionName` state
- Environment/L2Resources binding logic (from Controller constructor)
- SysString loading on file change (from initialize())
- TexturePropertyEditor.environment setup (from initialize())

**Reuses**: `RecentFilesManager`, `L2Crypt`, `SysstringPropertyEditor`, `TexturePropertyEditor`, `Dialogs`

---

### 4. PropertySheetManager

- **Purpose**: Handles property sheet creation, property loading, and undo integration
- **Location**: `xdat/PropertySheetManager.java`
- **Lines**: ~120

**Constructor dependencies**:
```java
PropertySheetManager(XdatEditor editor)
```

**Public interface**:
```java
PropertySheet createPropertySheet(TreeView<Object> elements,
                                   java.util.function.Function<TreeItem, String> scriptStringFn)
    // Creates PropertySheet bound to tree selection, wired to undo
```

**Owns**:
- `propertyCache` (static Map<Class, List<PropertySheetItem>>) — moved from Controller
- `loadProperties(Object)` — reflection-based property introspection
- Property change listener creation with UndoManager integration
- Listener cleanup on selection change

**Reuses**: `PropertySheetItem`, `FieldProperty`, `BooleanPropertyEditor`, `TexturePropertyEditor`, `SysstringPropertyEditor`, `PropertyChangeCommand`

**Key design note**: Receives `scriptStringFn` (a `Function<TreeItem, String>`) to convert tree items to script strings for History recording — avoids coupling to TreeManager.

---

### 5. SearchReplaceManager

- **Purpose**: Handles all find & replace operations with batch command support
- **Location**: `xdat/SearchReplaceManager.java`
- **Lines**: ~200

**Constructor dependencies**:
```java
SearchReplaceManager(XdatEditor editor, ResourceBundle resources)
```

**Public interface**:
```java
void replaceSelected(TreeView<Object> treeView, SearchPanel searchPanel)
    // Replaces in selected node only

void replaceAll(Field listField, SearchPanel searchPanel, TreeView<Object> treeView)
    // Replaces across all entities in a list
```

**Owns**:
- `replaceInObject()` — single-object replace with batch command
- `replaceInEntityRecursive()` — recursive traversal
- `processChildEntities()` — child entity traversal
- `findField()` — reflection field lookup
- `performReplace()` — regex/literal string replacement
- Default property list (`name`, `text`, `buttonNameText`, etc.)

**Reuses**: `BatchReplaceCommand`, `IOEntity`

---

### 6. ImportExportManager

- **Purpose**: Handles window export to file and import from file
- **Location**: `xdat/ImportExportManager.java`
- **Lines**: ~140

**Constructor dependencies**:
```java
ImportExportManager(XdatEditor editor, ResourceBundle resources,
                     FileOperationsManager fileOps)
```

**Public interface**:
```java
void exportWindow(TreeItem<Object> selected)
    // Exports selected element to .xdatwin file

void importWindow(TreeView<Object> treeView)
    // Imports element from .xdatwin file into current tree root
```

**Owns**:
- FileChooser setup for .xdatwin files
- WindowExporter/importWindow orchestration
- ImportWindowCommand creation and recording
- Type compatibility validation

**Reuses**: `WindowExporter`, `ImportWindowCommand`, `ElementCloner`, `ListHolder`, `TreeManager.createTreeItem()`

**Needs from FileOperationsManager**: `initialDirectoryProperty()` for file chooser default location

---

### 7. Controller (Refactored)

- **Purpose**: Thin FXML orchestrator — creates managers, wires bindings, delegates @FXML actions
- **Location**: `xdat/Controller.java` (modified in-place)
- **Lines**: ~450

**Keeps**:
- All `@FXML` field declarations (required by FXML binding)
- `initialize()` — slimmed to create managers and wire bindings
- `initializeThemeMenu()` / `applyTheme()` — 33 lines, UI-only
- `initializeLanguageMenu()` / `changeLanguage()` — 44 lines, UI-only
- `registerVersion()` — 24 lines, version menu wiring
- `loadScriptTabContent()` / `wrap()` — 17 lines, scripting tab
- `@FXML undo()` / `redo()` / `exit()` / `about()` / `showHistory()` — thin delegates (84 lines)
- `@FXML copyElement()` / `cutElement()` — thin delegates to ClipboardManager (28 lines)
- `@FXML pasteElement()` / `duplicateElement()` — delegates using TreeManager + ClipboardManager (~110 lines)
- `@FXML open()` / `save()` / `saveAs()` — delegates to FileOperationsManager (~10 lines)
- `@FXML exportWindow()` / `importWindow()` — delegates to ImportExportManager (~10 lines)
- `currentTreeView` tracking — cross-cutting state for clipboard ops
- `getSelectedTreeItem()` — utility for clipboard @FXML methods

**Removed**: All extracted methods, inner classes, static maps, file state properties

---

## Dependency Graph (No Cycles)

```
Controller
├── TreeManager
│   ├── DragDropHandler (created by TreeManager)
│   └── PropertySheetManager (created by TreeManager)
├── FileOperationsManager
├── SearchReplaceManager
└── ImportExportManager
    └── FileOperationsManager (for initialDirectory)

Shared (passed to multiple):
├── XdatEditor (read-only reference to app state)
├── UndoManager (from XdatEditor)
├── History (from XdatEditor)
└── ResourceBundle (i18n strings)
```

**No manager references another manager directly** except:
- ImportExportManager receives FileOperationsManager (for `initialDirectoryProperty` — read-only)
- TreeManager internally creates DragDropHandler and PropertySheetManager

---

## Error Handling Strategy

| Error Scenario | Handling | User Impact |
|----------------|----------|-------------|
| File read/write failure | Same as current: `Dialogs.showException()` in FileOperationsManager | Error dialog with message |
| Reflection error (field access) | Same as current: log + dialog in TreeManager/PropertySheetManager | Error dialog, operation skipped |
| Type incompatibility (paste/import) | Same as current: warning dialog in Controller/ImportExportManager | Warning dialog, operation cancelled |
| Invalid regex in replace | Same as current: silent fallback to original value in SearchReplaceManager | No crash, no replacement |

No error handling changes — every path preserved identically.

---

## Tech Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| FXML stays unchanged | Keep `fx:controller="Controller"` | Avoids risky FXML changes; all @FXML methods stay in Controller as thin delegates |
| ListHolder/ClassHolder become top-level | Package-private classes in `xdat/` package | Used across 4+ classes — inner class would require public or passing instances |
| PropertySheetManager receives scriptStringFn | `Function<TreeItem, String>` callback | Decouples from TreeManager; Controller wires `treeManager::treeItemToScriptString` |
| DragDropHandler created inside TreeManager | Not exposed to Controller | Only TreeManager needs it (installs on cells during factory setup) |
| Clipboard ops stay in Controller | paste/duplicate are ~110 lines in Controller | They wire together ClipboardManager + TreeManager + UndoManager — orchestration is Controller's job |
| Manager construction order | FileOps → TreeManager → SearchReplace → ImportExport | Respects dependency chain; no lazy initialization needed |

---

## File Summary

| Action | File | Lines |
|--------|------|-------|
| **New** | `xdat/ListHolder.java` | ~20 |
| **New** | `xdat/ClassHolder.java` | ~15 |
| **New** | `xdat/TreeManager.java` | ~400 |
| **New** | `xdat/DragDropHandler.java` | ~120 |
| **New** | `xdat/FileOperationsManager.java` | ~190 |
| **New** | `xdat/PropertySheetManager.java` | ~120 |
| **New** | `xdat/SearchReplaceManager.java` | ~200 |
| **New** | `xdat/ImportExportManager.java` | ~140 |
| **Modified** | `xdat/Controller.java` | 1,944 → ~450 |
| **Total new code** | 8 new files | ~1,205 lines |
| **Net change** | +8 files, -1 modified | ~1,655 total vs 1,944 original |
