# Architecture

**Pattern:** Multi-module library ecosystem with monolithic desktop application

## High-Level Structure

```
┌──────────────────────────────────────────────────────┐
│                   xdat_editor (GUI)                  │
│  ┌─────────┐  ┌────────────┐  ┌───────────────────┐ │
│  │ editor/  │  │  commons/  │  │     schema/       │ │
│  │ (JavaFX) │  │ (io+res)   │  │ (Groovy per-ver)  │ │
│  └────┬─────┘  └─────┬──────┘  └────────┬──────────┘ │
└───────┼──────────────┼──────────────────┼────────────┘
        │              │                  │
   ┌────▼──────────────▼──────────────────▼────┐
   │              L2unreal                      │
   │   (Unreal object model + bytecode)         │
   └──────┬──────────┬──────────────┬──────────┘
          │          │              │
   ┌──────▼───┐ ┌───▼────┐ ┌──────▼──────┐
   │ L2crypt  │ │  L2io  │ │ Serializer  │
   │ (crypto) │ │ (I/O)  │ │ (reflect)   │
   └──────────┘ └────────┘ └─────────────┘
```

**Dependency chain:** L2crypt, L2io (standalone) → Serializer (→ L2io) → L2unreal (→ all) → xdat_editor (→ all)

## Identified Patterns

### Command Pattern (Undo/Redo)

**Location:** `xdat_editor/editor/src/.../xdat/history/`
**Purpose:** Full reversibility of all editing operations
**Implementation:** `Command` interface with `execute()`/`undo()`, managed by `UndoManager` (dual-stack: undo + redo, max 100 entries)
**Example:** `PropertyChangeCommand`, `CreateElementCommand`, `DeleteElementCommand`, `MoveElementCommand`, `BatchReplaceCommand`

### Reflection-Based Serialization

**Location:** `Serializer/src/.../io/ReflectionSerializerFactory.java`
**Purpose:** Automatic object read/write from annotations
**Implementation:** Inspects fields via reflection, dispatches based on `@Compact`, `@UShort`, `@UByte`, `@UTF`, `@Length`, `@Custom`, `@ReadMethod`/`@WriteMethod`
**Example:** Schema classes annotated with `@DefaultIO` get auto-generated read/write via Groovy AST transformation

### Strategy Pattern (Encryption)

**Location:** `L2crypt/src/.../crypt/`
**Purpose:** Version-specific encryption/decryption dispatch
**Implementation:** `L2Crypt.decrypt()`/`encrypt()` dispatches to `L2Ver{X}InputStream`/`OutputStream` based on header version number
**Example:** v211/212 → Blowfish, v411-414 → RSA, v120/1x1 → XOR

### Observer Pattern (JavaFX Properties)

**Location:** `xdat_editor/editor/src/.../xdat/XdatEditor.java`
**Purpose:** Reactive UI updates when data changes
**Implementation:** JavaFX `ObjectProperty`, `ReadOnlyBooleanWrapper`, property bindings throughout the editor
**Example:** `xdatClass`, `xdatObject`, `working` properties trigger UI state changes

### Factory Pattern

**Location:** `Serializer/` and `L2unreal/`
**Purpose:** Create type-appropriate serializers
**Implementation:** `SerializerFactory<C>` → `ReflectionSerializerFactory` → `UnrealSerializerFactory` (caches per-class)

## Data Flow

### Open XDAT File

```
User selects file → L2Crypt.readHeader() identifies version
→ L2Crypt.decrypt() wraps with version-appropriate InputStream
→ ObjectInputStream reads with SerializerFactory
→ Schema class (Groovy) deserializes fields via @DefaultIO / read()
→ IOEntity tree populated → TreeView renders in editor
```

### Save XDAT File

```
User saves → L2Crypt.encrypt() wraps OutputStream
→ ObjectOutputStream writes with SerializerFactory
→ Schema class write() serializes fields
→ Encrypted bytes written to file
```

### Property Edit

```
User edits property → PropertyChangeCommand created
→ UndoManager.execute(command) → command.execute()
→ Reflection sets field value on IOEntity
→ PropertySheet refreshes → History records change
```

## Code Organization

**Approach:** Module-based with layer separation within the editor

**Module boundaries:**
- **L2crypt:** Pure encryption (no external deps)
- **L2io:** Binary I/O primitives (no external deps)
- **Serializer:** Reflection serialization framework (depends: L2io)
- **L2unreal:** Unreal Engine object model (depends: all above + commons)
- **xdat_editor/commons/io:** Entity interfaces + annotations
- **xdat_editor/commons/l2resources:** Game resource loading (textures, sysstrings)
- **xdat_editor/schema:** Version-specific Groovy class definitions (25+ versions)
- **xdat_editor/editor:** JavaFX GUI application
