# Roadmap

**Current Milestone:** UX & Editing Tools
**Status:** Planning

---

## Milestone: Foundation (v1.0–v1.6) - COMPLETE

**Goal:** Fully functional XDAT editor with core editing capabilities

### Features

**Multi-Version Schema Support** - COMPLETE

- 25+ L2 client versions (ct0 through etoa5)
- Groovy-based schema definitions with automatic serialization
- Dynamic version loading from versions.csv

**Visual Tree Editor** - COMPLETE

- TreeView navigation of XDAT element hierarchy
- Property editing via ControlsFX PropertySheet
- Specialized editors (textures, sysstrings, colors, booleans)
- Custom node icons per component type

**Undo/Redo System** - COMPLETE

- Command pattern with dual-stack UndoManager
- Property change, create, delete, move, paste, import, batch commands
- Visual history timeline
- Max 100 history entries

**Search & Replace** - COMPLETE

- Regex-enabled search across all properties
- Batch replace with undo support
- Search highlighting in tree

**Themes & i18n** - COMPLETE

- Dark and Light CSS themes
- 4 languages: English, Portuguese (BR), Spanish (AR), Russian

**Productivity Features (v1.4–v1.6)** - COMPLETE

- Drag-and-drop element reordering
- Element export/import between XDAT files
- Clipboard management (copy, cut, paste, duplicate)
- Recent files manager

---

## Milestone: UX & Editing Tools

**Goal:** Professional-grade editing experience with advanced tools
**Target:** Next development cycle

### Features

**UX Improvements** - PLANNED

- Editing workflow optimizations
- Performance improvements for large XDAT files
- Better visual feedback and navigation

**Batch Operations & Templates** - PLANNED

- Batch property editing across multiple elements
- Element templates for common UI patterns
- Validation and error checking

**Scripting Support** - PLANNED

- Scriptable editing operations
- Macro recording/playback
- Custom transformation scripts

---

## Milestone: Architecture & Modularity

**Goal:** Improve code quality and maintainability for long-term growth

### Features

**Controller Refactoring** - PLANNED → [spec](.specs/features/controller-refactoring/spec.md)

- Extract TreeManager (tree view + context menu + cell rendering)
- Extract DragDropHandler (drag-drop logic from cell factory)
- Extract FileOperationsManager (open/save/recent files + state)
- Extract PropertySheetManager (property editing + undo integration)
- Extract SearchReplaceManager (find/replace + batch commands)
- Extract ImportExportManager (window export/import)
- Slim Controller to <500 line orchestrator

**Module Architecture** - PLANNED

- Better inter-module interfaces
- Reduce coupling between editor components
- Enable future plugin/extension support

---

## Milestone: Extended Version Support

**Goal:** Support newer L2 client versions beyond Salvation

### Features

**New Chronicle Schemas** - PLANNED

- Post-Salvation client version schemas
- Adapt to new UI component types
- Maintain backward compatibility

---

## Future Considerations

- Plugin/extension system for community contributions
- Visual XDAT diff tool (compare two files)
- Layout preview (approximate rendering of UI elements)
- Bulk file processing (batch convert/update multiple XDAT files)
