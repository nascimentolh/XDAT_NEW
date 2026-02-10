# XDAT Editor

[English](#english) | [Português](#português)

---

## English

### About

XDAT Editor is an open-source tool for editing Lineage 2 client interface files (`interface.xdat`). It provides a visual editor with support for multiple client versions, from Interlude to Salvation.

### Features

- **Multi-Version Support**: Supports 25+ Lineage 2 client versions
- **Visual Tree Editor**: Navigate and edit UI elements through an intuitive tree structure
- **Property Editors**: Specialized editors for textures, strings, colors, and more
- **Dark/Light Themes**: Choose your preferred visual style
- **Advanced Search**: Search across all properties with regex support
- **Undo/Redo System**: Full history management with visual timeline
- **Texture Preview**: View texture references directly in the editor
- **System Strings**: Edit sysstr references with autocomplete

### Supported Versions

| Version | Chronicles |
|---------|------------|
| ct0 - ct26 | Interlude through Epilogue |
| god25, god3, god35 | Goddess of Destruction |
| etoa2 - etoa5 | Ertheia, Helios, Grand Crusade, Salvation |

### Requirements

- **Java 21+** with JavaFX (fully compatible with Java 25)

> **Note:** The project is compiled with Java 21 target for broad compatibility but runs on any JDK from 21 through 25 and beyond. Build dependencies (Lombok 1.18.42, Groovy 5.0.4, ASM 9.9.1) fully support Java 25.

#### Recommended JDK Options (includes JavaFX):

1. **Azul Zulu JDK FX** (Recommended - Java 21 LTS)
   - Download: https://www.azul.com/downloads/?version=java-21-lts&package=jdk-fx
   - Stable LTS version with bundled JavaFX

2. **Liberica Full JDK** (Java 21 LTS)
   - Download: https://bell-sw.com/pages/downloads/#jdk-21-lts
   - Alternative LTS option with JavaFX included

3. **Standard JDK + JavaFX SDK**
   - Java 21+ (LTS or latest): https://adoptium.net/
   - JavaFX SDK 21: https://gluonhq.com/products/javafx/
   - Manual setup required

### Installation

#### From Release

1. Download the latest release from [Releases](../../releases)
2. Extract the ZIP file
3. Run:
   - **Windows**: `xdat-editor.bat`
   - **Linux/macOS**: `./xdat-editor.sh`

#### Building from Source

```bash
# Clone the repository
git clone https://github.com/your-username/xdat-editor.git
cd xdat-editor

# Build all modules (from root directory)
ant -f build-all.xml

# Or build only the editor (requires pre-built dependencies)
cd xdat_editor
ant dist
```

### Project Structure

```
XDAT_NEW/
├── L2crypt/          # Encryption/decryption library
├── L2io/             # Lineage 2 I/O operations
├── Serializer/       # Serialization library
├── L2unreal/         # UnrealScript object handling
└── xdat_editor/      # Main editor application
    ├── commons/      # Shared utilities
    ├── schema/       # Version-specific schemas (Groovy)
    └── editor/       # JavaFX UI application
```

### Usage

1. Launch the application
2. Select your Lineage 2 client version from the menu
3. Open an `interface.xdat` file (File → Open)
4. Navigate the tree structure to find UI elements
5. Edit properties using the property panel
6. Save changes (File → Save)

### Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl+O | Open file |
| Ctrl+S | Save file |
| Ctrl+Shift+S | Save As |
| Ctrl+Z | Undo |
| Ctrl+Y | Redo |
| Ctrl+F | Search |

### License

MIT License

---

## Português

### Sobre

XDAT Editor é uma ferramenta open-source para edição de arquivos de interface do cliente Lineage 2 (`interface.xdat`). Oferece um editor visual com suporte a múltiplas versões do cliente, desde Interlude até Salvation.

### Funcionalidades

- **Suporte Multi-Versão**: Suporta mais de 25 versões do cliente Lineage 2
- **Editor Visual em Árvore**: Navegue e edite elementos da UI através de uma estrutura em árvore intuitiva
- **Editores de Propriedades**: Editores especializados para texturas, strings, cores e mais
- **Temas Escuro/Claro**: Escolha seu estilo visual preferido
- **Busca Avançada**: Pesquise em todas as propriedades com suporte a regex
- **Sistema de Desfazer/Refazer**: Gerenciamento completo de histórico com linha do tempo visual
- **Preview de Texturas**: Visualize referências de texturas diretamente no editor
- **System Strings**: Edite referências sysstr com autocomplete

### Versões Suportadas

| Versão | Chronicles |
|--------|------------|
| ct0 - ct26 | Interlude até Epilogue |
| god25, god3, god35 | Goddess of Destruction |
| etoa2 - etoa5 | Ertheia, Helios, Grand Crusade, Salvation |

### Requisitos

- **Java 21+** com JavaFX (totalmente compatível com Java 25)

> **Nota:** O projeto é compilado com alvo Java 21 para ampla compatibilidade, mas roda em qualquer JDK da versão 21 até 25 e além. As dependências de build (Lombok 1.18.42, Groovy 5.0.4, ASM 9.9.1) suportam completamente Java 25.

#### Opções de JDK Recomendadas (inclui JavaFX):

1. **Azul Zulu JDK FX** (Recomendado - Java 21 LTS)
   - Download: https://www.azul.com/downloads/?version=java-21-lts&package=jdk-fx
   - Versão LTS estável com JavaFX incluído

2. **Liberica Full JDK** (Java 21 LTS)
   - Download: https://bell-sw.com/pages/downloads/#jdk-21-lts
   - Opção LTS alternativa com JavaFX incluído

3. **JDK Padrão + JavaFX SDK**
   - Java 21+ (LTS ou mais recente): https://adoptium.net/
   - JavaFX SDK 21: https://gluonhq.com/products/javafx/
   - Configuração manual necessária

### Instalação

#### A partir do Release

1. Baixe a versão mais recente em [Releases](../../releases)
2. Extraia o arquivo ZIP
3. Execute:
   - **Windows**: `xdat-editor.bat`
   - **Linux/macOS**: `./xdat-editor.sh`

#### Compilando do Código Fonte

```bash
# Clone o repositório
git clone https://github.com/your-username/xdat-editor.git
cd xdat-editor

# Compile todos os módulos (a partir do diretório raiz)
ant -f build-all.xml

# Ou compile apenas o editor (requer dependências pré-compiladas)
cd xdat_editor
ant dist
```

### Estrutura do Projeto

```
XDAT_NEW/
├── L2crypt/          # Biblioteca de criptografia/descriptografia
├── L2io/             # Operações de I/O do Lineage 2
├── Serializer/       # Biblioteca de serialização
├── L2unreal/         # Manipulação de objetos UnrealScript
└── xdat_editor/      # Aplicação principal do editor
    ├── commons/      # Utilitários compartilhados
    ├── schema/       # Schemas específicos por versão (Groovy)
    └── editor/       # Aplicação UI JavaFX
```

### Como Usar

1. Inicie a aplicação
2. Selecione a versão do seu cliente Lineage 2 no menu
3. Abra um arquivo `interface.xdat` (Arquivo → Abrir)
4. Navegue pela estrutura em árvore para encontrar elementos da UI
5. Edite as propriedades usando o painel de propriedades
6. Salve as alterações (Arquivo → Salvar)

### Atalhos de Teclado

| Atalho | Ação |
|--------|------|
| Ctrl+O | Abrir arquivo |
| Ctrl+S | Salvar arquivo |
| Ctrl+Shift+S | Salvar Como |
| Ctrl+Z | Desfazer |
| Ctrl+Y | Refazer |
| Ctrl+F | Buscar |

### Licença

Licença MIT

---

## Contributing / Contribuindo

Contributions are welcome! Feel free to open issues or submit pull requests.

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou enviar pull requests.
