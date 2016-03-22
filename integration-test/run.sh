#!/usr/bin/env bash

mvn clean install

version=5.3

# Fetch Sonar
if [ ! -f "sonarqube-${version}.zip" ]; then
    curl -L -O https://sonarsource.bintray.com/Distribution/sonarqube/sonarqube-${version}.zip
    # and extract
    unzip sonarqube-${version}.zip
fi

# Start up sonar
if [ "$(uname)" == "Darwin" ]; then
    ./sonarqube-${version}/bin/macosx-universal-64/sonar.sh start
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    echo "Starting Sonar on linux"
    ./sonarqube-${version}/bin/linux-x86-64/sonar.sh console &

    # Travis has errors using the "sonar.sh start" command.  Instead we directly invoke the wroapper.
    #"$(pwd)/sonarqube-${version}/bin/linux-x86-64/./wrapper" "$(pwd)/sonarqube-${version}/conf/wrapper.conf" wrapper.syslog.ident=SonarQube wrapper.pidfile="$(pwd)/sonarqube-${version}/bin/linux-x86-64/./SonarQube.pid" wrapper.daemonize=TRUE
fi

# Wait for sonar to become available (up to one minute)
for i in {1..20}; do if (curl -I http://127.0.0.1:9000 2>/dev/null | grep -q 200); then echo "Sonar up and running"; break; fi; echo "Waiting for sonar to become available"; sleep 3; done

# Run our tests
mvn clean install sonar:sonar sonar-break:sonar-break

# Stop sonar
if [ "$(uname)" == "Darwin" ]; then
    ./sonarqube-${version}/bin/macosx-universal-64/sonar.sh stop
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    ./sonarqube-${version}/bin/linux-x86-64/sonar.sh stop
fi

# Clean up sonar
rm -rf sonarqube-${version}
# We leave the zip file around so it can be reused for the next run without downloading