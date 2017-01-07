#!/usr/bin/env bash

# version of sonarqube to use for the test
version=6.2

function finish {
    # Stop sonar
    if [ "$(uname)" == "Darwin" ]; then
        ./sonarqube-${version}/bin/macosx-universal-64/sonar.sh stop
    elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
        ./sonarqube-${version}/bin/linux-x86-64/sonar.sh stop
    fi

    # Clean up sonar
    rm -rf sonarqube-${version}
    # We leave the zip file around so it can be reused for the next run without downloading
}

function exitWithError {
    echo "**********************************************************************"
    echo " ERROR ENCOUNTERED"
    echo  "" $1
    echo "**********************************************************************"
    exit 1
}

trap finish EXIT

# Fetch Sonar
if [ ! -f "sonarqube-${version}.zip" ]; then
    curl -k -L -O https://sonarsource.bintray.com/Distribution/sonarqube/sonarqube-${version}.zip
fi
# and extract
unzip sonarqube-${version}.zip

# This is necessary so sonar actually starts successfully.  Travis is setting this value to "test" which causes the ruby
# runtime in sonar to not work correctly.
unset RAILS_ENV

# Start up sonar
echo "Starting sonar"
if [ "$(uname)" == "Darwin" ]; then
    ./sonarqube-${version}/bin/macosx-universal-64/sonar.sh start
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    echo "Starting Sonar on linux"
    ./sonarqube-${version}/bin/linux-x86-64/sonar.sh start
fi

# Wait for sonar to become available (up to one minute)
sonarfound=false
for i in {1..20}; do if (curl -I http://127.0.0.1:9000/coding_rules 2>/dev/null | grep -q 200); then echo "Sonar up and running"; sonarfound=true; break; fi; echo "Waiting for sonar to become available"; sleep 3; done

# If sonar didn't come up then print log and error
if [ "$sonarfound" = false ] ; then
    echo "Sonar did not start successfully, printing log files"
    tail -n 100 ./sonarqube-${version}/logs/sonar.log
    exitWithError "Sonar did not start"
fi;

# Add a condition in for zero critical issues
echo "Adding critical error check into sonar"
# Quality gate for versions 5.6 and below
curl -X POST -H "Authorization: Basic YWRtaW46YWRtaW4=" -H "Cache-Control: no-cache" -H "Content-Type: multipart/form-data" -F "gateId=1" -F "error=0" -F "metric=critical_violations" -F "op=GT" "http://localhost:9000/api/qualitygates/create_condition"
# Quality gate for version 6.0 and above (critical turned into bugs in 6.0)
curl -X POST -H "Authorization: Basic YWRtaW46YWRtaW4=" -H "Cache-Control: no-cache" -H "Content-Type: multipart/form-data" -F "gateId=1" -F "error=0" -F "metric=bugs" -F "op=GT" "http://localhost:9000/api/qualitygates/create_condition"

# Run our tests
for path in ./*; do
    [ -d "${path}" ] || continue # if not a directory, skip
    dirname="$(basename "${path}")"
    [ "${dirname}" = "sonarqube-${version}" ] && continue # if sonarqube, skip
    cd ${dirname}
    mvn versions:update-properties versions:commit
    mvn clean install sonar:sonar sonar-break:sonar-break
    returnValue=$?
    cd ..
    # Did we get an error and not expect it?
    if [[ returnValue -eq 1 ]] && [[ ${dirname} != *error ]]; then
        exitWithError "Got an error during build that was not expected."
    fi
    # Should we have encountered an error and we didn't?
    if [[ returnValue -eq 0 ]] && [[ ${dirname} == *error ]]; then
        exitWithError "Error expected but not encountered"
    fi
done
