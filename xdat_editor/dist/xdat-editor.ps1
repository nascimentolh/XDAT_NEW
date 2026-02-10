# ============================================================
# XDAT Editor - Lineage 2 Interface Editor
# ============================================================
# PowerShell Script for Windows
#
# REQUISITOS:
# -----------
# Este programa requer Java 21+ com JavaFX (compatível com Java 25+).
#
# OPÇÃO 1 - RECOMENDADA (Mais fácil):
#   Baixe o Azul Zulu JDK 21 FX (já inclui JavaFX):
#   https://www.azul.com/downloads/?version=java-21-lts&package=jdk-fx
#   Escolha: Windows, x86 64-bit, JDK FX, .msi (installer)
#
# OPÇÃO 2 - Liberica JDK Full:
#   https://bell-sw.com/pages/downloads/#jdk-21-lts
#   Escolha: JDK 21, Windows, Full (inclui JavaFX)
#
# OPÇÃO 3 - Java padrão + JavaFX separado:
#   1. Baixe Java 21+: https://adoptium.net/
#   2. Baixe JavaFX SDK 21: https://gluonhq.com/products/javafx/
#   3. Extraia o JavaFX em: $env:USERPROFILE\javafx-sdk-21
#      OU defina a variável $env:JAVAFX_HOME apontando para a pasta
#
# CONFIGURAÇÃO MANUAL:
#   Se você tem um JDK com JavaFX instalado em outro local,
#   edite a linha abaixo e descomente:
#   $env:JAVA_FX_HOME = "C:\caminho\para\seu\jdk-com-javafx"
#
# ============================================================

# === CONFIGURAÇÃO MANUAL (descomente e edite se necessário) ===
# $env:JAVA_FX_HOME = "C:\Program Files\Zulu\zulu-21-fx"
# ==============================================================

# Muda para o diretório do script
Set-Location $PSScriptRoot

Write-Host ""
Write-Host "XDAT Editor - Iniciando..." -ForegroundColor Cyan
Write-Host ""

# Função para testar JavaFX
function Test-JavaFX {
    try {
        $modules = java --list-modules 2>$null | Out-String
        return $modules -match "javafx.controls"
    }
    catch {
        return $false
    }
}

# Função para mostrar erro e ajuda
function Show-Help {
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host "ERRO: JavaFX não encontrado!" -ForegroundColor Red
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "O XDAT Editor requer Java 21+ com JavaFX (compatível até Java 25+)." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "SOLUÇÕES:" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "[OPÇÃO 1 - RECOMENDADA - MAIS FÁCIL]" -ForegroundColor Green
    Write-Host "  Baixe o Azul Zulu JDK 21 FX (já vem com JavaFX incluso):"
    Write-Host "  https://www.azul.com/downloads/?version=java-21-lts&package=jdk-fx" -ForegroundColor Blue
    Write-Host "  - Escolha: Windows, x86 64-bit, JDK FX, .msi"
    Write-Host "  - Instale e reinicie o computador"
    Write-Host ""
    Write-Host "[OPÇÃO 2 - Liberica JDK Full]" -ForegroundColor Green
    Write-Host "  https://bell-sw.com/pages/downloads/#jdk-21-lts" -ForegroundColor Blue
    Write-Host "  - Escolha: JDK 21, Windows, Full"
    Write-Host ""
    Write-Host "[OPÇÃO 3 - Java + JavaFX separados]" -ForegroundColor Green
    Write-Host "  1. Instale Java 21+: https://adoptium.net/" -ForegroundColor Blue
    Write-Host "  2. Baixe JavaFX SDK 21: https://gluonhq.com/products/javafx/" -ForegroundColor Blue
    Write-Host "  3. Extraia em: $env:USERPROFILE\javafx-sdk-21"
    Write-Host ""
    Write-Host "[OPÇÃO 4 - Apontar manualmente para seu JDK]" -ForegroundColor Green
    Write-Host "  1. Abra este arquivo (xdat-editor.ps1) no Bloco de Notas"
    Write-Host "  2. Encontre a linha: # `$env:JAVA_FX_HOME = ..."
    Write-Host "  3. Remova o # e coloque o caminho do seu JDK com JavaFX"
    Write-Host "  Exemplo: `$env:JAVA_FX_HOME = 'C:\Program Files\Zulu\zulu-21-fx'"
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "PRECISA DE AJUDA? COPIE O TEXTO ABAIXO E COLE NO CHATGPT:" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "----------------------------------------------------------"
    Write-Host "Estou tentando rodar o XDAT Editor no Windows e apareceu"
    Write-Host "o erro 'JavaFX não encontrado'. Meu sistema é Windows."
    Write-Host "Como faço para instalar o Java 21+ com JavaFX da forma"
    Write-Host "mais simples possível? Preciso de um passo a passo."
    Write-Host "----------------------------------------------------------"
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host ""

    Read-Host "Pressione Enter para sair"
    exit 1
}

# Se JAVA_FX_HOME estiver definido, usa ele
if ($env:JAVA_FX_HOME) {
    $javaExe = Join-Path $env:JAVA_FX_HOME "bin\java.exe"
    if (Test-Path $javaExe) {
        Write-Host "Usando Java de: $env:JAVA_FX_HOME" -ForegroundColor Green
        & $javaExe -jar xdat-editor-1.6.1.jar $args
        exit $LASTEXITCODE
    }
    else {
        Write-Host "AVISO: JAVA_FX_HOME definido mas java.exe não encontrado em $env:JAVA_FX_HOME\bin" -ForegroundColor Yellow
        Write-Host "Tentando outras opções..." -ForegroundColor Yellow
        Write-Host ""
    }
}

# Tenta executar com JavaFX bundled primeiro (JDK padrão do sistema)
if (Test-JavaFX) {
    Write-Host "JavaFX encontrado no JDK do sistema" -ForegroundColor Green
    java -jar xdat-editor-1.6.1.jar $args
    exit $LASTEXITCODE
}

# Tenta com módulos JavaFX adicionados manualmente
try {
    java --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing -jar xdat-editor-1.6.1.jar $args 2>$null
    if ($LASTEXITCODE -eq 0) {
        exit 0
    }
}
catch {
    # Continua para tentar JavaFX externo
}

# Fall back para JavaFX SDK externo
$javafxPath = if ($env:JAVAFX_HOME) {
    Join-Path $env:JAVAFX_HOME "lib"
} else {
    Join-Path $env:USERPROFILE "javafx-sdk-21\lib"
}

if (Test-Path (Join-Path $javafxPath "javafx.controls.jar")) {
    Write-Host "Usando JavaFX de: $javafxPath" -ForegroundColor Green
    java --module-path "$javafxPath" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing -jar xdat-editor-1.6.1.jar $args
    exit $LASTEXITCODE
}
else {
    Show-Help
}
