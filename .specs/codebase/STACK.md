# Tech Stack

**Analyzed:** 2026-02-10

## Core

- Language: Java 21
- Secondary Language: Groovy 4.0.18 (schema definitions)
- Runtime: JVM (requires JavaFX-bundled JDK)
- Package Manager: Apache Ant (manual JAR dependency management)
- Build System: Apache Ant with per-module `build.xml` + root `build-all.xml`
- Dev Environment: mise.toml (Zulu JavaFX 21 + Ant)

## Frontend

- UI Framework: JavaFX 21 (FXML + CSS)
- Enhanced Controls: ControlsFX 11.2.1 (PropertySheet)
- Styling: CSS themes (dark-theme.css, light-theme.css, l2.css)
- State Management: JavaFX ObservableProperties + Bindings
- Icons: Custom PNG with @2x retina variants

## Core Libraries (In-House)

- L2crypt 1.3.3: Lineage 2 file encryption/decryption (Blowfish, RSA, XOR)
- L2io 2.2.6: Binary I/O abstraction (RandomAccess, DataInput/Output, Unreal Package)
- Serializer 1.2.3: Reflection + annotation-driven object serialization
- L2unreal 1.5.6: Unreal Engine object/bytecode handling

## External Dependencies

- Apache Commons IO 2.15.1
- Apache Commons Lang3 3.14.0
- Apache Commons CSV 1.10.0
- ASM 9.6 (bytecode manipulation, used by Groovy)
- Lombok 1.18.30 (compile-time, L2unreal only)
- jsquish (DXT texture compression)

## Testing

- Unit/Integration: JUnit (in L2io, Serializer, L2unreal)
- E2E: None (manual testing)
- Coverage: Not configured

## Development Tools

- Version Control: Git
- IDE Support: Standard Java IDE (IntelliJ/Eclipse compatible)
- i18n: Java ResourceBundle (EN, PT-BR, ES-AR, RU)
