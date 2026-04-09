@ECHO OFF
SETLOCAL

SET BASE_DIR=%~dp0
SET WRAPPER_DIR=%BASE_DIR%.mvn\wrapper
SET PROPERTIES_FILE=%WRAPPER_DIR%\maven-wrapper.properties

IF NOT EXIST "%PROPERTIES_FILE%" (
  ECHO Missing %PROPERTIES_FILE%
  EXIT /B 1
)

FOR /F "tokens=1,* delims==" %%A IN (%PROPERTIES_FILE%) DO (
  IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
)

SET MAVEN_VERSION=%DISTRIBUTION_URL:~-24,5%
SET MAVEN_HOME=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%
SET ARCHIVE_PATH=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%-bin.zip

IF NOT EXIST "%MAVEN_HOME%\bin\mvn.cmd" (
  IF NOT EXIST "%ARCHIVE_PATH%" (
    powershell -Command "Invoke-WebRequest -UseBasicParsing '%DISTRIBUTION_URL%' -OutFile '%ARCHIVE_PATH%'"
  )
  powershell -Command "Expand-Archive -Force '%ARCHIVE_PATH%' '%WRAPPER_DIR%'"
)

CALL "%MAVEN_HOME%\bin\mvn.cmd" %*
