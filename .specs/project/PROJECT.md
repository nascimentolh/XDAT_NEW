# XDAT Editor

**Vision:** A comprehensive, open-source visual editor for Lineage 2 client interface files (`interface.xdat`), enabling the modding community to customize game UI across all client versions with professional-grade editing tools.

**For:** Lineage 2 modding community (server admins, UI modders, client developers)
**Solves:** XDAT files are complex encrypted binaries with version-specific schemas — editing them requires understanding file formats, encryption, and Unreal Engine structures. This editor abstracts all complexity behind an intuitive visual tree editor.

## Goals

- Provide full read/write support for all major L2 client versions (Interlude through Salvation and beyond)
- Deliver a polished, efficient editing experience with modern UX (undo/redo, search, drag-drop, themes)
- Offer advanced editing tools (batch operations, element import/export, templates, scripting)
- Maintain clean, modular architecture as features grow (refactor monolithic components)

## Tech Stack

**Core:**

- Language: Java 21 + Groovy 4.0.18
- UI Framework: JavaFX 21
- Build System: Apache Ant

**Key dependencies:** ControlsFX 11.2.1, Apache Commons (IO, Lang3, CSV), ASM 9.6, jsquish

## Scope

**Current (v1.6.0) includes:**

- 25+ L2 client version support (Interlude → Salvation)
- Visual tree editor with property editing (textures, strings, colors)
- Dark/Light themes with i18n (EN, PT-BR, ES-AR, RU)
- Full undo/redo with visual history timeline
- Advanced search with regex support
- Drag-and-drop element reordering
- Element export/import between XDAT files
- Texture preview and system string autocomplete

**Active development focus:**

- UX improvements (editing workflows, performance, usability)
- New editing tools (batch operations, templates, validation, scripting)
- Code quality (refactor Controller.java monolith, improve modularity)

**Explicitly out of scope:**

- Automated testing infrastructure
- Migration to Gradle/Maven or other build systems
- Web version or cloud-based features
- Non-XDAT file format editing
