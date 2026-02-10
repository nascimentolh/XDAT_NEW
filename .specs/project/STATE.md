# State

**Last Updated:** 2026-02-10
**Current Work:** Java 25 Compatibility Upgrade — COMPLETED ✅
**Version:** 1.6.1

---

## Recent Decisions (Last 60 days)

### AD-001: No automated testing infrastructure (2026-02-10)

**Decision:** No plans to implement automated testing
**Reason:** Project complexity is manageable with manual testing; ROI of test infrastructure doesn't justify effort for a desktop GUI editor
**Trade-off:** No regression safety net; relies on manual verification
**Impact:** All validation remains manual; no CI test gates

### AD-002: Stay on Java/Ant build system (2026-02-10)

**Decision:** No migration to Gradle/Maven planned
**Reason:** Ant works well for the current multi-module structure; migration effort is high with little practical benefit
**Trade-off:** Manual JAR dependency management, no dependency resolution
**Impact:** New dependencies require manual download and lib/ placement

---

## Active Blockers

_None_

---

## Recent Changes

### v1.6.1 - Java 25 Compatibility (2026-02-10)

**Changes:**
- ✅ Upgraded Lombok: 1.18.30 → 1.18.42 (Java 25 support)
- ✅ Upgraded Groovy: 4.0.18 → 5.0.4 (Java 25 support)
- ✅ Upgraded ASM: 9.6 → 9.9.1 (Java 25 bytecode support)
- ✅ Fixed compilation errors in refactored code:
  - Added missing imports for `CountingInputStream` (Apache Commons IO)
  - Fixed `emptyProperty()` → `Bindings.isEmpty()` (JavaFX 21 compatibility)
  - Fixed `progressProperty()` → `workingProperty()`
  - Fixed `wrap(Pane)` → `wrap(Region)` for SplitPane compatibility
  - Added missing imports for `Hide` and `Description` annotations
- ✅ Fixed NullPointerException in `Controller.applyTheme()` - added null check for scene
- ✅ Created PowerShell launcher script (xdat-editor.ps1) to replace problematic .bat file
- ✅ Updated all build.xml files to reflect "Java 25 compatible" status
- ✅ Updated README.md with Java 25 compatibility information

**Build System:**
- Compiles with `-source 21 -target 21` for broad compatibility
- Runs on JDK 21-25+ without issues
- All dependencies support Java 25

---

## Lessons Learned

### LL-002: Java Version Compatibility Strategy (2026-02-10)

**Context:** Upgrading project from Java 21 to Java 25 compatibility
**Approach:**
- Keep compilation target at Java 21 for maximum compatibility
- Upgrade build-time dependencies to support latest JDK
- Use `--release 21` flag to ensure proper bytecode generation
- Test with latest JDK while maintaining backward compatibility

**Key Dependencies Updated:**
- Lombok: Needed 1.18.32+ for Java 21 support, 1.18.42 for Java 25
- Groovy: Version 4.x supports Java 21, 5.x needed for Java 25
- ASM: Version 9.6 supports Java 21, 9.7+ needed for Java 25

**Outcome:** Code compiles on Java 25, runs on Java 21-25+
**Lesson:** Always check dependency compatibility matrix before upgrading JDK

### LL-001: Successful Large-Scale Refactoring (2026-02-10)

**Context:** Controller.java refactoring from 1,944 lines to 619 lines
**Approach:**
- Created 8 new manager classes first (ListHolder, ClassHolder, DragDropHandler, PropertySheetManager, SearchReplaceManager, FileOperationsManager, TreeManager, ImportExportManager)
- Maintained FXML compatibility throughout
- Used callbacks (Function<TreeItem, String>) to avoid circular dependencies
- Phased approach: Foundation → Independent Managers → Dependent Managers → Integration

**Outcome:** 68% code reduction while maintaining full functionality
**Key Success Factor:** Complete architectural design before implementation

---

## Preferences

**Model Guidance Shown:** 2026-02-10
