# Controller Refactoring Specification

## Problem Statement

Controller.java is a 1,944-line "God Object" handling 10+ concerns: file operations, tree management, property editing, drag-and-drop, search/replace, clipboard, import/export, menu initialization, theme/language switching, and history display. Adding new features requires understanding the entire file and risks breaking unrelated functionality. This is the primary bottleneck for future editor development.

## Goals

- [ ] Reduce Controller.java from ~1,944 lines to ~400 lines (orchestrator + FXML wiring only)
- [ ] Extract 6 focused manager classes, each owning a single concern
- [ ] Zero behavior change — every existing feature works identically after refactoring
- [ ] Clear extension points for future features (scripting, batch tools, templates)

## Out of Scope

- Adding new features or changing existing behavior
- Modifying existing helper classes (ClipboardManager, RecentFilesManager, ElementCloner, WindowExporter, SearchPanel, etc.)
- Changing the FXML layout or CSS
- Changing the build system or project structure
- Refactoring other modules (L2crypt, L2io, Serializer, L2unreal)

---

## User Stories

### P1: Extract TreeManager ⭐ MVP

**User Story**: As a developer, I want tree view logic in a dedicated class so that I can modify tree behavior without reading 1,944 lines.

**Why P1**: Tree management is the largest concern (383 lines) with the highest coupling. It's the foundation other extractions depend on.

**Scope**: Methods `createTab`, `createTreeView`, `setupTreeViewCellFactory`, `buildTree`, `checkTreeNode`, `createTreeItem`, `createContextMenu`, `updateContextMenu`, `createAddMenu`. Inner classes `ListHolder`, `ClassHolder`. Static map `UI_NODE_ICONS`.

**Acceptance Criteria**:

1. WHEN the editor opens an XDAT file THEN TreeManager SHALL build and display the tree identically to current behavior
2. WHEN a user right-clicks a tree node THEN TreeManager SHALL show the same context menu with add/delete options
3. WHEN a tree node is selected THEN TreeManager SHALL notify the Controller (via callback/listener) to update the property sheet
4. WHEN search criteria change THEN TreeManager SHALL rebuild/filter the tree with highlights as before
5. WHEN TreeManager is instantiated THEN it SHALL receive only its dependencies (editor reference, undo manager, resource bundle) — no back-reference to the full Controller

**Independent Test**: Open an XDAT file, expand tree nodes, right-click for context menu, select nodes — all tree interactions work identically.

---

### P1: Extract DragDropHandler ⭐ MVP

**User Story**: As a developer, I want drag-and-drop logic isolated so that I can extend drop behavior (e.g., cross-tree drops) without touching tree rendering.

**Why P1**: Drag-drop is embedded inside `setupTreeViewCellFactory` (lines 672-742) creating a 127-line method. Extracting it is required to make TreeManager clean.

**Scope**: Methods `canDropOn`, `performDrop`, and the 5 drag event handlers currently embedded in `setupTreeViewCellFactory`.

**Acceptance Criteria**:

1. WHEN a user drags a tree element THEN DragDropHandler SHALL show the same visual feedback (drop indicators)
2. WHEN a user drops an element on a valid target THEN DragDropHandler SHALL execute a MoveElementCommand via the undo manager
3. WHEN a user drops on an invalid target THEN DragDropHandler SHALL reject the drop (same rules as current `canDropOn`)
4. WHEN drag-drop completes THEN the search highlight SHALL be preserved (regression from v1.4.1 fix)

**Independent Test**: Drag a Button element between Windows — it moves correctly with undo support.

---

### P1: Extract FileOperationsManager ⭐ MVP

**User Story**: As a developer, I want file operations centralized so that adding new file formats or import sources doesn't require modifying the controller.

**Why P1**: File ops (162 lines) manage critical state (`xdatFile`, `initialDirectory`, `environment`, `l2resources`) that cascades across the entire UI. Centralizing this state reduces coupling.

**Scope**: Methods `open`, `openFileDirectly`, `openRecentFile`, `save`, `saveAs`, `updateRecentFilesMenu`. Properties `xdatFile`, `initialDirectory`, `environment`, `l2resources`.

**Acceptance Criteria**:

1. WHEN a user opens a file via menu THEN FileOperationsManager SHALL decrypt, deserialize, and load identically to current behavior
2. WHEN a user saves a file THEN FileOperationsManager SHALL serialize and encrypt using the current version's schema
3. WHEN a user opens a recent file THEN FileOperationsManager SHALL load it with the correct version auto-selected
4. WHEN file operations are in progress THEN FileOperationsManager SHALL update the progress bar via a provided binding
5. WHEN a file open/save fails THEN FileOperationsManager SHALL show the same error dialog

**Independent Test**: Open, edit, save an XDAT file — file I/O works identically. Recent files menu updates.

---

### P1: Extract PropertySheetManager ⭐ MVP

**User Story**: As a developer, I want property editing logic separated so that adding new property types (e.g., color pickers, enum dropdowns) is straightforward.

**Why P1**: Property editing (94 lines) is tightly coupled to undo/redo recording. Extracting it creates a clean extension point for new editor types.

**Scope**: Methods `createPropertySheet`, `loadProperties`. Static field `map` (property cache).

**Acceptance Criteria**:

1. WHEN a tree node is selected THEN PropertySheetManager SHALL display the correct properties with their current values
2. WHEN a user edits a property THEN PropertySheetManager SHALL create a PropertyChangeCommand and execute it via the undo manager
3. WHEN the undo manager is executing a command THEN PropertySheetManager SHALL NOT record duplicate history entries
4. WHEN properties are loaded for a class THEN PropertySheetManager SHALL cache them (same static map behavior)

**Independent Test**: Select a Button node, edit its `name` property, undo the change — property editing + undo works.

---

### P1: Extract SearchReplaceManager ⭐ MVP

**User Story**: As a developer, I want search/replace logic isolated so that I can add new search modes (e.g., search by type, search by value range) without touching the controller.

**Why P1**: Search/replace (194 lines) involves recursive traversal and batch command creation — complex logic that deserves its own class.

**Scope**: Methods `replaceSelected`, `replaceAll`, `replaceInObject`, `replaceInEntityRecursive`, `processChildEntities`, `findField`, `performReplace`.

**Acceptance Criteria**:

1. WHEN a user performs "Replace Selected" THEN SearchReplaceManager SHALL replace the value in the selected node's matching property
2. WHEN a user performs "Replace All" THEN SearchReplaceManager SHALL create a BatchReplaceCommand covering all matches
3. WHEN a replace operation completes THEN SearchReplaceManager SHALL execute the batch command via the undo manager (fully undoable)
4. WHEN no matches are found THEN SearchReplaceManager SHALL report zero replacements (no command created)

**Independent Test**: Search for a texture name, replace all occurrences, undo — batch replace works.

---

### P1: Extract ImportExportManager ⭐ MVP

**User Story**: As a developer, I want import/export logic separated so that adding new export formats (e.g., JSON, XML) is possible without modifying the controller.

**Why P1**: Import/export (134 lines) is a self-contained feature that already delegates to WindowExporter. Extracting the remaining orchestration completes the separation.

**Scope**: Methods `exportWindow`, `importWindow`.

**Acceptance Criteria**:

1. WHEN a user exports a window THEN ImportExportManager SHALL save it to a file using WindowExporter (same format)
2. WHEN a user imports a window from another XDAT THEN ImportExportManager SHALL load, present selection dialog, and create an ImportWindowCommand
3. WHEN import completes THEN the imported window SHALL appear in the tree and be fully undoable

**Independent Test**: Export a window from file A, import into file B — element appears in tree with undo support.

---

### P2: Slim Controller as Orchestrator

**User Story**: As a developer, I want the Controller to be a thin orchestrator so that I can understand the entire application flow by reading one short file.

**Why P2**: After all extractions, the Controller needs cleanup — wiring managers together, keeping only FXML bindings and initialization delegation.

**Scope**: Refactor `initialize()` to delegate to managers. Keep thin @FXML methods (`undo`, `redo`, `exit`, `about`, `showHistory`). Keep `registerVersion`. Wire manager dependencies.

**Acceptance Criteria**:

1. WHEN Controller is initialized THEN it SHALL create all managers and wire their dependencies
2. WHEN Controller.java is reviewed THEN it SHALL contain fewer than 500 lines
3. WHEN a new @FXML action is needed THEN the pattern SHALL be obvious: add 3-line method delegating to the appropriate manager
4. WHEN all managers are wired THEN there SHALL be no circular dependencies between them

**Independent Test**: Read Controller.java — every method either delegates to a manager or is a trivial FXML binding. Full application works identically.

---

## Edge Cases

- WHEN a manager needs to interact with another manager (e.g., SearchReplace triggers tree rebuild) THEN communication SHALL go through callbacks/listeners, not direct references between managers
- WHEN the Controller is initialized but no file is loaded THEN all managers SHALL handle the null/empty state gracefully (same as current behavior)
- WHEN a method is used by multiple concerns (e.g., `getSelectedTreeItem`) THEN it SHALL live in the most appropriate manager or remain in Controller as shared utility
- WHEN inner classes (`ListHolder`, `ClassHolder`) are moved THEN they SHALL become package-private in TreeManager (not public API)

---

## Success Criteria

- [ ] Controller.java reduced to <500 lines
- [ ] 6 new manager classes, each <400 lines with a single responsibility
- [ ] Zero behavior regression — every feature works identically
- [ ] No circular dependencies between managers
- [ ] Adding a new property editor type requires touching only PropertySheetManager
- [ ] Adding a new tree context menu action requires touching only TreeManager
- [ ] Adding a new file format requires touching only FileOperationsManager
