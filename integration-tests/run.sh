#!/usr/bin/env bash

# version of sonarqube to use for the test
version=5.5

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

#cd .. && mvn clean install && cd integration-tests

# Fetch Sonar
if [ ! -f "sonarqube-${version}.zip" ]; then
    curl -L -O https://sonarsource.bintray.com/Distribution/sonarqube/sonarqube-${version}.zip
fi
# and extract
unzip sonarqube-${version}.zip

echo "Current directory"
pwd

#echo "Copy the config for test.rb"
#cp ./sonarqube-${version}/web/WEB-INF/config/environments/development.rb ./sonarqube-${version}/web/WEB-INF/config/environments/test.rb

echo "List of files in bin/linux64"
ls ./sonarqube-${version}/bin/linux-x86-64

#echo "List of files in web/config/envs"
#ls ./sonarqube-${version}/web/WEB-INF/config/environments/

unset RAILS_ENV

echo "env variables"
printenv


# Start up sonar
if [ "$(uname)" == "Darwin" ]; then
    ./sonarqube-${version}/bin/macosx-universal-64/sonar.sh start
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    echo "Starting Sonar on linux"
    ./sonarqube-${version}/bin/linux-x86-64/sonar.sh start

    # Travis has errors using the "sonar.sh start" command.  Instead we directly invoke the wroapper.
    #"./sonarqube-${version}/bin/linux-x86-64/wrapper" "$(pwd)/sonarqube-${version}/conf/wrapper.conf" wrapper.syslog.ident=SonarQube wrapper.pidfile="$(pwd)/sonarqube-${version}/bin/linux-x86-64/SonarQube.pid" wrapper.daemonize=TRUE
fi

# Wait for sonar to become available (up to one minute)
for i in {1..20}; do if (curl -I http://127.0.0.1:9000 2>/dev/null | grep -q 200); then echo "Sonar up and running"; break; fi; echo "Waiting for sonar to become available"; sleep 3; done

echo "Checking for sonar in processes"
ps -A | grep sonar

echo "Status of sonar"
./sonarqube-${version}/bin/linux-x86-32/sonar.sh status

cat ./sonarqube-${version}/logs/sonar.log

# Add a condition in for zero critical issues
#curl -X POST -H "Authorization: Basic YWRtaW46YWRtaW4=" -H "Cache-Control: no-cache" -H "Postman-Token: a1002e9f-7c26-b179-08de-e0066da5f318" -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" -F "gateId=1" -F "error=0" -F "metric=critical_violations" -F "op=GT" "http://localhost:9000/api/qualitygates/create_condition"

## Run our tests
#for path in ./*; do
#    [ -d "${path}" ] || continue # if not a directory, skip
#    dirname="$(basename "${path}")"
#    [ "${dirname}" = "sonarqube-${version}" ] && continue # if sonarqube, skip
#    cd ${dirname}
#    mvn clean install sonar:sonar sonar-break:sonar-break
#    returnValue=$?
#    cd ..
#    # Did we get an error and not expect it?
#    if [[ returnValue -eq 1 ]] && [[ ${dirname} != *error ]]; then
#        exitWithError "Got an error during build that was not expected."
#    fi
#    # Should we have encountered an error and we didn't?
#    if [[ returnValue -eq 0 ]] && [[ ${dirname} == *error ]]; then
#        exitWithError "Error expected but not encountered"
#    fi
#done
