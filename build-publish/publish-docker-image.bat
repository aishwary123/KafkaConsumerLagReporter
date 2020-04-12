@echo off
SET VERSION=%1

:: Validate no arguments input
IF [%1]==[] goto noArgumentsError

# Validate HOME variable to resolve GitLab urls with 'go get'
if "%HOME%" == "" (
      echo "HOME env is empty"
      goto :eof
) else (
      echo "HOME env is set to %HOME%"
)

if not exist %HOME%\.ssh\id_rsa (
	echo "%HOME%\.ssh\id_rsa does not exist"
	goto :eof
)

SET LABEL="kafka-consumer-lag-reporter"
SET REPO_PATH="<REPO_URL>"
SET REPO_USERNAME="<REPO_USERNAME>"
SET REPO_PASSWORD="<REPO_PASSWORD>"
SET TAG="%LABEL%:%VERSION%"

:: Login to Artifactory docker repo
docker login %REPO_PATH% --username %REPO_USERNAME% --password %REPO_PASSWORD%

:: Build the image
docker build --label %LABEL% --tag %REPO_PATH%/%TAG% --file ..\Dockerfile ..\.

:: Push the image
docker push %REPO_PATH%/%TAG%

:: Remove the local image
docker rmi %REPO_PATH%/%TAG%

:: End the execution
@echo Done
goto :eof

:noArgumentsError
@echo "No arguments passed. Image version number is expected"
exit 1