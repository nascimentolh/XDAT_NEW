# Code Conventions

## Naming Conventions

**Files:**
Java: PascalCase matching class name (`UnrealPackage.java`, `XdatEditor.java`, `PropertyChangeCommand.java`)
Groovy: PascalCase per UI component (`DefaultProperty.groovy`, `Button.groovy`, `Window.groovy`)

**Packages:**
Reverse-domain hierarchy: `acmi.l2.clientmod.{module}.{submodule}`
Examples: `acmi.l2.clientmod.crypt`, `acmi.l2.clientmod.xdat.history`, `acmi.l2.clientmod.io.annotation`

**Classes:**
PascalCase with descriptive suffixes: `*Command`, `*Factory`, `*Exception`, `*Editor`, `*Manager`
Examples: `PropertyChangeCommand`, `ReflectionSerializerFactory`, `CryptoException`, `SysstringPropertyEditor`, `UndoManager`

**Methods:**
camelCase with verb prefixes: `readInt()`, `writeObject()`, `getPackage()`, `loadSchema()`, `executeCommand()`
Boolean getters: `isExecutingCommand()`, `isModified()`

**Fields/Variables:**
camelCase: `xdatObject`, `undoStack`, `fileCache`, `entriesCache`
Groovy schema fields: snake_case for multi-word (`size_absolute_values`, `anchor_parent`, `anchor_ctrl`)
Unknown/reverse-engineered: `unk2`, `unk3`, etc.

**Constants:**
UPPER_SNAKE_CASE: `BLOWFISH_KEY_211`, `NO_CRYPT`, `MAGIC`

## Code Organization

**Import ordering (observed):**
```java
// 1. Java standard library
import java.io.*;
import java.util.*;
import java.nio.*;

// 2. JavaFX
import javafx.application.*;
import javafx.beans.property.*;
import javafx.scene.control.*;

// 3. Third-party libraries
import org.apache.commons.io.*;
import org.controlsfx.control.*;

// 4. Project modules
import acmi.l2.clientmod.io.*;
import acmi.l2.clientmod.util.*;
```

**File structure:**
```java
// Package declaration
// Imports (grouped as above)
// Class declaration
//   Static fields/constants
//   Instance fields
//   Constructor(s)
//   Public methods
//   Private/helper methods
//   Inner classes/enums
```

## Type Safety

**Approach:** Standard Java types, no extensive generics. Lombok used in L2unreal for boilerplate (`@Getter`, `@Setter`). Groovy classes use `@CompileDynamic` for flexibility.

**Generics:** Used in serialization framework (`Serializer<T, C extends Context>`, `ObjectInput<C>`) and collections.

## Error Handling

**Pattern:** Checked-to-unchecked wrapping for cleaner APIs
```java
// Common pattern across modules
throw new UncheckedIOException(ioException);
throw new CryptoException("Unsupported crypt version: " + version);
throw new UnrealException("Entry not found: " + name);
```

**UI error display:** `Dialogs` utility shows JavaFX Alert dialogs
**Graceful degradation:** Missing resources return null, missing textures silently ignored
**Background tasks:** `Platform.runLater()` for UI thread safety

## Comments/Documentation

**Style:** Minimal inline comments. Code is self-documenting through descriptive naming. No Javadoc on most methods. Comments appear mainly for:
- Reverse-engineered protocol fields (`// version 120 uses file-based XOR key`)
- Build dependencies (`// depends on L2io`)
- Magic numbers (`// 0x9E2A83C1`)

## Groovy Schema Conventions

**Annotations:** `@Bindable` (JavaBeans property change support), `@CompileDynamic`, `@DefaultIO` (auto-serialization)
**Field defaults:** `'undefined'` for strings, `-9999` for sysstring IDs, `false` for booleans
**Custom annotations:** `@Tex` (texture preview), `@Sysstr` (sysstring autocomplete), `@Description` (property tooltip)
