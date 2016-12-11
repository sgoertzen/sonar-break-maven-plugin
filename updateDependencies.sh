#!/usr/bin/env bash

function exitWithError {
    echo "**********************************************************************"
    echo " ERROR ENCOUNTERED"
    echo  "" $1
    echo "**********************************************************************"
    exit 1
}

# Update our dependencies and plugins
mvn versions:use-latest-releases versions:update-properties versions:commit

# If there were no updates just exit
if [ -z "$(git status -s)" ]; then
    echo "No updates found"
    exit
fi

# Increment our version
mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion} versions:commit
returnValue=$?
if [[ returnValue -eq 1 ]]; then
    exitWithError "Error incrementing project version."
fi

# build the project
mvn clean install exec:exec -Dmaven.signing.skip=true
returnValue=$?
if [[ returnValue -eq 1 ]]; then
    exitWithError "Error while building the project after updates."
fi

#Create branch
datetime=$(date +%Y-%m-%d-%H-%M-%S)
branch="updates-$datetime"
echo "Creating branch $branch with updates."
git checkout -b $branch

git add .
git commit -m "Auto update of dependencies"

# push branch to github
git push origin $branch

# create pull request
hub pull-request -m "Auto updates of dependencies"

# todo: commit PR once all checks pass
# todo: run mvn deploy