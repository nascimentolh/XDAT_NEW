# Project Structure

**Root:** `XDAT_NEW/`

## Directory Tree

```
XDAT_NEW/
├── build-all.xml                  # Master Ant build (orchestrates all modules)
├── mise.toml                      # Dev tooling (Java 21 + Ant)
├── README.md                      # Bilingual docs (EN/PT-BR)
│
├── L2crypt/                       # Encryption/decryption library (v1.3.3)
│   ├── build.xml
│   ├── dist/l2crypt-1.3.3.jar
│   └── src/main/java/acmi/l2/clientmod/crypt/
│       ├── L2Crypt.java           # Main dispatcher
│       ├── blowfish/              # Blowfish (v211/212)
│       ├── rsa/                   # RSA (v411-414)
│       ├── xor/                   # XOR (v120/1x1)
│       └── lame/                  # Legacy wrapper
│
├── L2io/                          # Binary I/O library (v2.2.6)
│   ├── build.xml
│   ├── dist/l2io-2.2.6.jar
│   └── src/
│       ├── main/java/acmi/l2/clientmod/io/
│       │   ├── UnrealPackage.java # Package format handler
│       │   ├── DataInput.java     # Read abstraction
│       │   ├── DataOutput.java    # Write abstraction
│       │   └── RandomAccess*.java # Bidirectional I/O
│       └── test/                  # JUnit tests
│
├── Serializer/                    # Reflection serialization (v1.2.3)
│   ├── build.xml
│   ├── dist/serializer-1.2.3.jar
│   └── src/
│       ├── main/java/acmi/l2/clientmod/io/
│       │   ├── Serializer.java    # Core interface
│       │   ├── ReflectionSerializerFactory.java
│       │   ├── ObjectInputStream.java
│       │   └── annotation/        # @Compact, @UTF, @Length, etc.
│       └── test/                  # JUnit tests
│
├── L2unreal/                      # Unreal Engine integration (v1.5.6)
│   ├── build.xml
│   ├── dist/l2unreal-1.5.6.jar
│   ├── lib/                       # commons-io, commons-lang3, lombok
│   └── src/
│       ├── main/java/acmi/l2/clientmod/unreal/
│       │   ├── Environment.java   # Package environment + caching
│       │   ├── UnrealSerializerFactory.java
│       │   ├── core/              # Core Unreal objects
│       │   ├── engine/            # Engine objects
│       │   ├── bytecode/          # Bytecode tokenization
│       │   ├── properties/        # Unreal properties
│       │   └── annotation/        # @NameRef, @ObjectRef, @Bytecode
│       └── test/                  # JUnit tests
│
└── xdat_editor/                   # Main editor application
    ├── build.xml                  # Multi-stage Ant build
    ├── mise.toml
    ├── lib/                       # External JARs (ControlsFX, Groovy, ASM, etc.)
    │
    ├── commons/
    │   ├── io/src/.../util/       # IOEntity, UIEntity, annotations, AST transforms
    │   └── l2resources/src/...    # L2Resources, L2Context, texture loading
    │
    ├── schema/src/main/groovy/    # Version-specific schemas
    │   ├── ct0/ ... ct26/         # Classic chronicles (Interlude→High Five)
    │   ├── god25/ god3/ god35/    # Goddess of Destruction
    │   └── etoa2/ ... etoa5/      # Ertheia→Salvation
    │       Each contains: DefaultProperty.groovy, Window.groovy,
    │       Button.groovy, CheckBox.groovy, ListCtrl.groovy, etc.
    │
    ├── editor/src/
    │   ├── main/java/acmi/l2/clientmod/xdat/
    │   │   ├── XdatEditor.java    # Application entry point
    │   │   ├── Controller.java    # Main controller (83KB)
    │   │   ├── History.java       # Change recording
    │   │   ├── history/           # Command pattern (Undo/Redo)
    │   │   ├── propertyeditor/    # PropertySheet customization
    │   │   ├── search/            # Find & Replace
    │   │   └── util/              # Clipboard, cloner, exporter, recent files
    │   └── main/resources/
    │       └── acmi/l2/clientmod/xdat/
    │           ├── main.fxml      # UI layout
    │           ├── *-theme.css    # Dark/Light themes
    │           ├── interface*.properties  # i18n (4 languages)
    │           └── nodeicons/     # Component type icons
    │
    └── dist/                      # Distribution output
        ├── xdat-editor-*.jar
        ├── xdat-editor.bat/.sh    # Launchers
        └── *.jar                  # All dependency JARs
```

## Where Things Live

**File encryption/decryption:** `L2crypt/src/.../crypt/`
**Binary I/O primitives:** `L2io/src/.../io/`
**Object serialization:** `Serializer/src/.../io/`
**Unreal package handling:** `L2unreal/src/.../unreal/`
**XDAT schema definitions:** `xdat_editor/schema/src/main/groovy/{version}/`
**Editor UI code:** `xdat_editor/editor/src/main/java/.../xdat/`
**UI resources (FXML, CSS, i18n):** `xdat_editor/editor/src/main/resources/.../xdat/`
**Shared entity interfaces:** `xdat_editor/commons/io/src/.../util/`
**Game resource loading:** `xdat_editor/commons/l2resources/src/.../l2resources/`

## Special Directories

**dist/ (in each module):** Pre-built JAR artifacts. Checked into git for convenience.
**lib/ (L2unreal, xdat_editor):** Third-party JARs managed manually (no Maven/Gradle).
**nodeicons/:** PNG icons for tree node types (Button, CheckBox, etc.) with @2x retina variants.
