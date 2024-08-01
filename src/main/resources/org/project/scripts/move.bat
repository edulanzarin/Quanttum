@echo off
REM Verifica se o script está sendo executado com privilégios administrativos
openfiles >nul 2>&1
if '%errorlevel%' NEQ '0' (
    powershell -Command "Start-Process cmd -ArgumentList '/c %~s0' -Verb RunAs"
    exit /b
)

REM Define o caminho para o arquivo JAR a ser movido
set "SOURCE_JAR=%USERPROFILE%\Downloads\quanttum.jar"

REM Define o caminho de destino para o arquivo JAR
set "TARGET_DIR=C:\Program Files\Quanttum"
set "TARGET_JAR=%TARGET_DIR%\quanttum.jar"

REM Verifica se o arquivo JAR de origem existe
if not exist "%SOURCE_JAR%" (
    exit /b
)

REM Cria o diretório de destino se não existir
if not exist "%TARGET_DIR%" (
    mkdir "%TARGET_DIR%"
)

REM Move e substitui o arquivo JAR no diretório de destino
move /Y "%SOURCE_JAR%" "%TARGET_JAR%" >nul 2>&1

REM Encerra o script e fecha a janela do console
exit
