# External Integrations

## File Format: Lineage 2 XDAT

**Service:** Lineage 2 client interface files (`interface.xdat`)
**Purpose:** Read, edit, and write XDAT files containing UI element definitions
**Implementation:** `xdat_editor/editor/` + all library modules
**Configuration:** Schema selection via `versions.csv` (25+ client versions)
**Authentication:** N/A (local file processing)

### Data Format

**Header:** `Lineage2VerXXX` (UTF-16LE, 28 bytes) — identifies encryption version
**Body:** Encrypted binary stream containing serialized Groovy object tree
**Encryption versions:**
- v111/121: XOR
- v120: XOR (file-based key)
- v211/212: Blowfish (static keys)
- v411-414: RSA (configurable private key)
- v811/821/911/912: Legacy wrappers (LameCrypt → base version)

## File Format: Unreal Packages (.u, .utx)

**Service:** Unreal Engine package files
**Purpose:** Load textures, system strings, and class definitions from game assets
**Implementation:** `L2io/` (UnrealPackage.java) + `L2unreal/` (Environment.java)
**Configuration:** Game directory path (INI file parsing with wildcard support)

### Package Structure

- Name table, Import table, Export table, Object data
- Magic: `0x9E2A83C1`
- Supports L2-specific version extensions

## File Format: SysString-*.dat

**Service:** Lineage 2 localized string files
**Purpose:** Provide autocomplete for system string references in the editor
**Implementation:** `xdat_editor/commons/l2resources/L2Resources.java`
**Configuration:** Loaded from game directory, EUC-KR encoded, encrypted
**Key method:** `getSysString(int id)` — returns localized string by ID

## File Format: Textures (.utx)

**Service:** Lineage 2 texture packages
**Purpose:** Preview UI element textures directly in the editor
**Implementation:** `xdat_editor/commons/l2resources/` + `jsquish.jar`
**Supported formats:** DXT1, DXT3, DXT5, RGBA8, G16, P8
**Features:** Split9 border rendering, image caching, lazy loading

## UI Framework: JavaFX

**Service:** Java GUI toolkit
**Purpose:** Desktop application UI
**Implementation:** `xdat_editor/editor/`
**Configuration:** FXML layout (`main.fxml`), CSS themes, ResourceBundle i18n
**Key dependency:** ControlsFX 11.2.1 (PropertySheet for property editing)

## Scripting: Apache Groovy

**Service:** Groovy runtime 4.0.18
**Purpose:** Dynamic schema definitions per client version
**Implementation:** `xdat_editor/schema/` (30+ component types across 25+ versions)
**Key feature:** `@DefaultIO` AST transformation auto-generates serialization code at compile time
**Configuration:** Loaded at runtime via `GroovyClassLoader` from schema.jar

## Build System: Apache Ant

**Service:** Build automation
**Purpose:** Compile, package, and distribute the application
**Configuration:** `build-all.xml` (root) + per-module `build.xml`
**Dependency download:** Ant `<get>` tasks from Maven Central (with `skipexisting`)
**Distribution:** `xdat_editor/dist/` with launcher scripts (.bat/.sh)

## Persistence: Java Preferences API

**Service:** Java `java.util.prefs.Preferences`
**Purpose:** Store user settings between sessions
**Implementation:** `XdatEditor.java` → `Preferences.userRoot().node("l2clientmod").node("xdat_editor")`
**Stored data:** Window position/size, maximized state, selected theme, language preference, recent files

## Background Jobs

**Queue system:** `java.util.concurrent.Executor` (single-threaded)
**Location:** `XdatEditor.java` (application-level executor)
**Purpose:** File I/O operations run off the JavaFX application thread
**Pattern:** `execute(Callable task, Consumer onSuccess, Runnable onFinally)` with `Platform.runLater()` for UI updates

## No External Network Services

This is a fully offline desktop application. No APIs, webhooks, cloud services, or network calls. All data is processed locally from Lineage 2 client files.
