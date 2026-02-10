# Controller Refactoring Tasks

**Design**: `.specs/features/controller-refactoring/design.md`
**Status**: Draft

---

## Execution Plan

### Phase 1: Foundation (Sequential)

Shared data classes that multiple managers depend on.

```
T1 → T2
```

### Phase 2: Independent Managers (Parallel)

Managers with no inter-manager dependencies. Can all be written simultaneously.

```
       ┌→ T3 (DragDropHandler) ──────────┐
       ├→ T4 (PropertySheetManager) ─────┤
T2 ──→ ├→ T5 (SearchReplaceManager) [P] ─┼──→ Phase 3
       └→ T6 (FileOperationsManager) [P] ┘
```

### Phase 3: Dependent Managers (After Phase 2)

Managers that depend on other managers existing.

```
T3 + T4 ──→ T7 (TreeManager)
T6 ────────→ T8 (ImportExportManager)
```

### Phase 4: Integration (Sequential)

Wire everything together and verify.

```
T7 + T8 ──→ T9 (Refactor Controller) ──→ T10 (Verify build)
```

---

## Task Breakdown

### T1: Create ListHolder.java

**What**: Extract `ListHolder` inner class from Controller into a package-private top-level class
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/ListHolder.java`
**Depends on**: None
**Reuses**: Exact code from Controller lines 1912-1929

**Done when**:

- [ ] File created with package `acmi.l2.clientmod.xdat`
- [ ] Class is package-private (no `public` modifier)
- [ ] Fields: `entity` (IOEntity), `list` (List<IOEntity>), `name` (String), `type` (Class<? extends IOEntity>)
- [ ] Constructor and `toString()` match original

**Verify**: `grep -r "class ListHolder" xdat_editor/editor/src/` shows the new file

---

### T2: Create ClassHolder.java

**What**: Extract `ClassHolder` inner class from Controller into a package-private top-level class
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/ClassHolder.java`
**Depends on**: None
**Reuses**: Exact code from Controller lines 1931-1942

**Done when**:

- [ ] File created with package `acmi.l2.clientmod.xdat`
- [ ] Class is package-private (no `public` modifier)
- [ ] Field: `clazz` (Class<? extends IOEntity>)
- [ ] Constructor and `toString()` match original

**Verify**: `grep -r "class ClassHolder" xdat_editor/editor/src/` shows the new file

---

### T3: Create DragDropHandler.java

**What**: Extract drag-and-drop logic into a standalone handler class
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/DragDropHandler.java`
**Depends on**: T1 (ListHolder)
**Reuses**: Controller lines 672-742 (event handlers), 748-857 (canDropOn, performDrop)

**Done when**:

- [ ] Constructor takes `UndoManager`
- [ ] `install(TreeCell<Object> cell, TreeView<Object> treeView)` method installs all 5 drag handlers
- [ ] `canDropOn()` logic moved verbatim
- [ ] `performDrop()` logic moved verbatim, creates MoveElementCommand, records via undoManager
- [ ] All drag style classes preserved: `drag-over-top`, `drag-over-bottom`, `drag-over`

**Verify**: File compiles with no references to Controller

---

### T4: Create PropertySheetManager.java

**What**: Extract property sheet creation and property loading into a manager class
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/PropertySheetManager.java`
**Depends on**: T1 (ListHolder)
**Reuses**: Controller lines 1082-1180 (createPropertySheet, loadProperties, property cache)

**Done when**:

- [ ] Constructor takes `XdatEditor editor`
- [ ] `createPropertySheet(TreeView<Object>, Function<TreeItem, String> scriptStringFn)` method defined
- [ ] Static `propertyCache` (Map<Class, List<PropertySheetItem>>) moved from Controller
- [ ] `loadProperties(Object)` static method moved verbatim
- [ ] Property change listener creates PropertyChangeCommand via `editor.getUndoManager()`
- [ ] Listener cleanup on selection change preserved (InvalidationListener pattern)
- [ ] `scriptStringFn` callback used instead of direct `treeItemToScriptString()` call
- [ ] UndoManager `isExecutingCommand()` check preserved

**Verify**: File compiles with no references to Controller

---

### T5: Create SearchReplaceManager.java [P]

**What**: Extract all search/replace logic into a manager class
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/SearchReplaceManager.java`
**Depends on**: T1 (ListHolder)
**Reuses**: Controller lines 1587-1802 (7 methods)

**Done when**:

- [ ] Constructor takes `XdatEditor editor, ResourceBundle resources`
- [ ] `replaceSelected(TreeView<Object>, SearchPanel)` method moved
- [ ] `replaceAll(Field, SearchPanel, TreeView<Object>)` method moved (added TreeView param for refresh)
- [ ] `replaceInObject()` moved verbatim
- [ ] `replaceInEntityRecursive()` moved verbatim
- [ ] `processChildEntities()` moved verbatim
- [ ] `findField()` moved verbatim
- [ ] `performReplace()` moved verbatim
- [ ] Default property names array preserved: `name, text, buttonNameText, titleText, file, normalTex, backTex, fontName, styleName`
- [ ] BatchReplaceCommand creation and UndoManager recording preserved

**Verify**: File compiles with no references to Controller

---

### T6: Create FileOperationsManager.java [P]

**What**: Extract file I/O operations and file-related state into a manager class
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/FileOperationsManager.java`
**Depends on**: None
**Reuses**: Controller lines 182-185 (properties), 189-204 (constructor bindings), 276-300 (sysstring/texture loading), 412-497 (recent files + file open), 1211-1284 (FXML open/save), 1851-1873 (saveAs)

**Done when**:

- [ ] Constructor takes `XdatEditor editor, ResourceBundle resources`
- [ ] Owns `xdatFile`, `initialDirectory`, `environment`, `l2resources` ObjectProperties
- [ ] Owns `recentFilesManager` instance (created in constructor)
- [ ] Owns `currentVersionName` state with getter/setter
- [ ] Environment binding from xdatFile (from Controller constructor lines 192-203)
- [ ] SysString loading on xdatFile change (from initialize() lines 276-292)
- [ ] TexturePropertyEditor.environment setup on xdatFile change (from initialize() lines 294-299)
- [ ] `open()` — file chooser + validation + read (from Controller.open() lines 1211-1267)
- [ ] `openFileDirectly(File)` — direct file read (from Controller lines 464-497)
- [ ] `save()` — write xdatObject (from Controller lines 1269-1284)
- [ ] `saveAs()` — file chooser + save (from Controller lines 1851-1873)
- [ ] `openRecentFile(RecentFile, ToggleGroup)` — version selection + open (from Controller lines 435-461)
- [ ] `updateRecentFilesMenu(Menu)` — rebuild menu items (from Controller lines 413-432)
- [ ] All properties exposed via getters: `xdatFileProperty()`, `initialDirectoryProperty()`, etc.
- [ ] Recent file addition happens inside open() on success (Platform.runLater)

**Verify**: File compiles with no references to Controller

---

### T7: Create TreeManager.java

**What**: Extract tree view construction, cell rendering, context menu, and tree item creation
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/TreeManager.java`
**Depends on**: T1 (ListHolder), T2 (ClassHolder), T3 (DragDropHandler), T4 (PropertySheetManager)
**Reuses**: Controller lines 84-126 (icons), 547-926 (createTab through createContextMenu), 928-1080 (updateContextMenu through createTreeItem), 1182-1209 (treeItemToScriptString)

**Done when**:

- [ ] Constructor takes `XdatEditor editor, ResourceBundle resources`
- [ ] `UI_NODE_ICONS` static map moved from Controller (all 35 entries)
- [ ] `UI_NODE_ICON_DEFAULT` constant moved
- [ ] `createTab(Field, SearchPanel, Runnable onReplaceSelected, Runnable onReplaceAll)` — full tab assembly
- [ ] Internal `createTreeView(Field, SearchPanel)` — tree factory with search listeners
- [ ] Internal `setupTreeViewCellFactory(TreeView, SearchPanel)` — cell rendering + search highlighting + DragDropHandler install
- [ ] Internal `createContextMenu(TreeView)` / `updateContextMenu(ContextMenu, TreeView)` — right-click menus
- [ ] Internal `createAddMenu(String, TreeView, TreeItem)` — add element submenu
- [ ] `static createTreeItem(IOEntity)` — public, recursive tree item builder
- [ ] `static buildTree(IOEntity, Field, TreeView, SearchCriteria)` — public, tree construction with filtering
- [ ] `static checkTreeNode(TreeItem, SearchCriteria, int[])` — search check
- [ ] `treeItemToScriptString(TreeItem)` — public, for History recording
- [ ] Creates PropertySheetManager internally, calls `createPropertySheet()` inside `createTab()`
- [ ] Creates DragDropHandler internally, installs via `handler.install(cell, treeView)` in cell factory
- [ ] `installXdatListener(InvalidationListener)` for tracking cleanup listeners
- [ ] L2Resources reference from FileOperationsManager passed through for context menu "View" action

**Verify**: File compiles, all Controller tree methods have corresponding TreeManager methods

---

### T8: Create ImportExportManager.java

**What**: Extract window export and import logic into a manager class
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/ImportExportManager.java`
**Depends on**: T1 (ListHolder), T6 (FileOperationsManager for initialDirectory)
**Reuses**: Controller lines 1442-1578 (exportWindow, importWindow)

**Done when**:

- [ ] Constructor takes `XdatEditor editor, ResourceBundle resources, FileOperationsManager fileOps`
- [ ] `exportWindow(TreeItem<Object> selected)` — file chooser + WindowExporter.exportWindow
- [ ] `importWindow(TreeView<Object> treeView)` — file chooser + WindowExporter.importWindow + type check + ImportWindowCommand
- [ ] Uses `fileOps.initialDirectoryProperty()` for file chooser default
- [ ] Uses `TreeManager.createTreeItem()` (static call) for building imported tree items
- [ ] Uses `fileOps.initialDirectoryProperty().setValue()` to update directory after export/import
- [ ] ImportWindowCommand creation and recording via `editor.getUndoManager()`
- [ ] Error dialogs use `Dialogs.showException()` as before

**Verify**: File compiles with no references to Controller

---

### T9: Refactor Controller.java

**What**: Gut Controller to a thin orchestrator that creates managers and delegates all @FXML methods
**Where**: `xdat_editor/editor/src/main/java/acmi/l2/clientmod/xdat/Controller.java` (modify)
**Depends on**: T3, T4, T5, T6, T7, T8 (all managers exist)
**Reuses**: Controller initialize() pattern, existing @FXML method signatures

**Done when**:

- [ ] **Removed**: `UI_NODE_ICONS` map, `UI_NODE_ICON_DEFAULT`, `ListHolder` class, `ClassHolder` class
- [ ] **Removed**: `xdatFile`, `initialDirectory`, `environment`, `l2resources` properties (now in FileOperationsManager)
- [ ] **Removed**: `recentFilesManager` field (now in FileOperationsManager)
- [ ] **Removed**: `currentVersionName` field (now in FileOperationsManager)
- [ ] **Removed**: All extracted methods (createTab, createTreeView, setupTreeViewCellFactory, etc.)
- [ ] **Added**: Manager fields: `treeManager`, `fileOps`, `searchReplace`, `importExport`
- [ ] **Constructor**: Creates FileOperationsManager with environment/l2resources bindings
- [ ] **initialize()**: Creates remaining managers, wires bindings, delegates to managers
- [ ] **@FXML open()**: `fileOps.open()` (1 line)
- [ ] **@FXML save()**: `fileOps.save()` (1 line)
- [ ] **@FXML saveAs()**: `fileOps.saveAs()` (1 line)
- [ ] **@FXML exportWindow()**: `importExport.exportWindow(getSelectedTreeItem())` (1 line)
- [ ] **@FXML importWindow()**: `importExport.importWindow(currentTreeView)` (1 line)
- [ ] **@FXML copyElement()**: Kept, delegates to clipboardManager (12 lines)
- [ ] **@FXML cutElement()**: Kept, delegates to clipboardManager (16 lines)
- [ ] **@FXML pasteElement()**: Kept, uses TreeManager.createTreeItem + clipboardManager (~69 lines)
- [ ] **@FXML duplicateElement()**: Kept, uses TreeManager.createTreeItem + clipboardManager (~41 lines)
- [ ] **@FXML undo/redo/exit/about/showHistory**: Kept as-is (thin delegates)
- [ ] **initializeThemeMenu/applyTheme**: Kept as-is
- [ ] **initializeLanguageMenu/changeLanguage**: Kept as-is
- [ ] **registerVersion**: Kept, uses `fileOps.setCurrentVersionName(name)`
- [ ] **xdatClass listener in initialize()**: Adapted to use `treeManager.createTab()` and pass callbacks
- [ ] **xdatFile change listeners for SysString/Texture**: Moved to FileOperationsManager (verified removed)
- [ ] **Menu bindings**: Uses `fileOps.xdatFileProperty()` etc.
- [ ] `currentTreeView` tracking kept in Controller
- [ ] `getSelectedTreeItem()` kept in Controller
- [ ] File is under 500 lines

**Verify**:
- `wc -l Controller.java` shows <500 lines
- No references to removed methods remain
- All @FXML method names unchanged (FXML compatibility)

---

### T10: Build verification

**What**: Compile the entire project and verify no regressions
**Where**: Project root
**Depends on**: T9

**Done when**:

- [ ] `ant -f build-all.xml` completes without errors
- [ ] All JARs generated in `xdat_editor/dist/`
- [ ] No compilation warnings related to refactored classes
- [ ] `grep -r "Controller\." xdat_editor/editor/src/` shows no broken static references to moved methods
- [ ] `grep -rn "import.*Controller" xdat_editor/editor/src/` shows only main.fxml reference

**Verify**:
```bash
cd XDAT_NEW && ant -f build-all.xml
# Expected: "All projects built successfully!"
```

---

## Parallel Execution Map

```
Phase 1 (Sequential):
  T1 (ListHolder) ──→ T2 (ClassHolder)

Phase 2 (Parallel after T1+T2):
  ├── T3 (DragDropHandler) [P]
  ├── T4 (PropertySheetManager) [P]
  ├── T5 (SearchReplaceManager) [P]
  └── T6 (FileOperationsManager) [P]

Phase 3 (After Phase 2):
  T3 + T4 ──→ T7 (TreeManager)
  T6 ────────→ T8 (ImportExportManager)
  (T7 and T8 can be parallel)

Phase 4 (Sequential):
  T7 + T8 ──→ T9 (Refactor Controller) ──→ T10 (Build verify)
```

---

## Task Granularity Check

| Task | Scope | Status |
|------|-------|--------|
| T1: Create ListHolder.java | 1 class, ~20 lines | Granular |
| T2: Create ClassHolder.java | 1 class, ~15 lines | Granular |
| T3: Create DragDropHandler.java | 1 class, ~120 lines | Granular |
| T4: Create PropertySheetManager.java | 1 class, ~120 lines | Granular |
| T5: Create SearchReplaceManager.java | 1 class, ~200 lines | Granular |
| T6: Create FileOperationsManager.java | 1 class, ~190 lines | Granular |
| T7: Create TreeManager.java | 1 class, ~400 lines | Granular (large but single responsibility) |
| T8: Create ImportExportManager.java | 1 class, ~140 lines | Granular |
| T9: Refactor Controller.java | 1 file modify | Granular (removal + delegation) |
| T10: Build verification | Build + grep checks | Granular |
