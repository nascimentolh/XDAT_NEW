@echo off
setlocal
cd /d "%~dp0"

REM ============================================================
REM XDAT Editor - Lineage 2 Interface Editor
REM ============================================================
REM
REM REQUISITOS:
REM -----------
REM Este programa requer Java 21+ com JavaFX.
REM
REM OPCAO 1 - RECOMENDADA (Mais facil):
REM   Baixe o Azul Zulu JDK FX (ja inclui JavaFX):
REM   https://www.azul.com/downloads/?version=java-21-lts&package=jdk-fx
REM   Escolha: Windows, x86 64-bit, .msi (installer)
REM
REM OPCAO 2 - Liberica JDK Full:
REM   https://bell-sw.com/pages/downloads/#jdk-21-lts
REM   Escolha: JDK 21, Windows, Full (inclui JavaFX)
REM
REM OPCAO 3 - Java padrao + JavaFX separado:
REM   1. Baixe Java 21: https://adoptium.net/
REM   2. Baixe JavaFX SDK 21: https://gluonhq.com/products/javafx/
REM   3. Extraia o JavaFX em: %USERPROFILE%\javafx-sdk-21
REM      OU defina a variavel JAVAFX_HOME apontando para a pasta
REM
REM CONFIGURACAO MANUAL:
REM   Se voce tem um JDK com JavaFX instalado em outro local,
REM   edite a linha abaixo e remova o REM:
REM   set JAVA_FX_HOME=C:\caminho\para\seu\jdk-com-javafx
REM
REM ============================================================

REM === CONFIGURACAO MANUAL (descomente e edite se necessario) ===
REM set JAVA_FX_HOME=C:\Program Files\Zulu\zulu-21-fx
REM ==============================================================

echo.
echo XDAT Editor - Iniciando...
echo.

REM Se JAVA_FX_HOME estiver definido, usa ele
if defined JAVA_FX_HOME (
    if exist "%JAVA_FX_HOME%\bin\java.exe" (
        echo Usando Java de: %JAVA_FX_HOME%
        "%JAVA_FX_HOME%\bin\java.exe" -jar xdat-editor-1.5.0.jar %*
        goto :eof
    ) else (
        echo AVISO: JAVA_FX_HOME definido mas java.exe nao encontrado em %JAVA_FX_HOME%\bin
        echo Tentando outras opcoes...
        echo.
    )
)

REM Try running with bundled JavaFX first (JDK padrao do sistema)
java --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing -jar xdat-editor-1.5.0.jar %* 2>nul
if %errorlevel%==0 goto :eof

REM Fall back to external JavaFX SDK
set JAVAFX_PATH=%JAVAFX_HOME%\lib
if "%JAVAFX_HOME%"=="" set JAVAFX_PATH=%USERPROFILE%\javafx-sdk-21\lib

if exist "%JAVAFX_PATH%\javafx.controls.jar" (
    echo Usando JavaFX de: %JAVAFX_PATH%
    java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing -jar xdat-editor-1.5.0.jar %*
) else (
    echo.
    echo ============================================================
    echo ERRO: JavaFX nao encontrado!
    echo ============================================================
    echo.
    echo O XDAT Editor requer Java 21+ com JavaFX.
    echo.
    echo ============================================================
    echo SOLUCOES:
    echo ============================================================
    echo.
    echo [OPCAO 1 - RECOMENDADA - MAIS FACIL]
    echo   Baixe o Azul Zulu JDK FX (ja vem com JavaFX incluso):
    echo   https://www.azul.com/downloads/?version=java-21-lts^&package=jdk-fx
    echo   - Escolha: Windows, x86 64-bit, .msi
    echo   - Instale e reinicie o computador
    echo.
    echo [OPCAO 2 - Liberica JDK Full]
    echo   https://bell-sw.com/pages/downloads/#jdk-21-lts
    echo   - Escolha: JDK 21, Windows, Full
    echo.
    echo [OPCAO 3 - Java + JavaFX separados]
    echo   1. Instale Java 21: https://adoptium.net/
    echo   2. Baixe JavaFX SDK: https://gluonhq.com/products/javafx/
    echo   3. Extraia em: %USERPROFILE%\javafx-sdk-21
    echo.
    echo [OPCAO 4 - Apontar manualmente para seu JDK]
    echo   1. Abra este arquivo (xdat-editor.bat) no Bloco de Notas
    echo   2. Encontre a linha: REM set JAVA_FX_HOME=C:\...
    echo   3. Remova o REM e coloque o caminho do seu JDK com JavaFX
    echo   Exemplo: set JAVA_FX_HOME=C:\Program Files\Zulu\zulu-21-fx
    echo.
    echo ============================================================
    echo PRECISA DE AJUDA? COPIE O TEXTO ABAIXO E COLE NO CHATGPT:
    echo ============================================================
    echo.
    echo ----------------------------------------------------------
    echo Estou tentando rodar o XDAT Editor no Windows e apareceu
    echo o erro "JavaFX nao encontrado". Meu sistema e Windows.
    echo Como faco para instalar o Java 21 com JavaFX da forma
    echo mais simples possivel? Preciso de um passo a passo.
    echo ----------------------------------------------------------
    echo.
    echo ============================================================
    echo.
    pause
)
