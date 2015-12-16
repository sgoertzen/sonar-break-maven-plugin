#!/usr/bin/env bash


echo "Pinging localhost"
ping localhost
echo "Pinging 127.0.0.1"
ping 127.0.0.1

# Fetch Sonar
curl -L -O https://sonarsource.bintray.com/Distribution/sonarqube/sonarqube-5.2.zip
# and extract
unzip sonarqube-5.2.zip

# Start up sonar
# Travis has errors using the "sonar.sh start" command.  Instead we directly invoke the wroapper.
if [ "$(uname)" == "Darwin" ]; then
    "$(pwd)/sonarqube-5.2/bin/macosx-universal-64/./wrapper" "$(pwd)/sonarqube-5.2/conf/wrapper.conf" wrapper.syslog.ident=SonarQube wrapper.pidfile="$(pwd)/sonarqube-5.2/bin/macosx-universal-64/./SonarQube.pid" wrapper.daemonize=TRUE
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    echo "Starting Sonar on linux"
    "$(pwd)/sonarqube-5.2/bin/linux-x86-64/./wrapper" "$(pwd)/sonarqube-5.2/conf/wrapper.conf" wrapper.syslog.ident=SonarQube wrapper.pidfile="$(pwd)/sonarqube-5.2/bin/linux-x86-64/./SonarQube.pid" wrapper.daemonize=TRUE
fi

# Wait for sonar to become available
for i in {1..20}; do if (curl -I http://127.0.0.1:9000 2>/dev/null | grep -q 200); then echo "Sonar up and running"; break; fi; sleep 1; done


# Run our tests
#mvn clean install sonar:sonar com.sgoertzen.maven:sonarbreak:1.0:sonarBreak

# Stop sonar
#if [ "$(uname)" == "Darwin" ]; then
#    ./sonarqube-5.2/bin/macosx-universal-64//sonar.sh stop
#elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
#    ./sonarqube-5.2/bin/linux-x86-64/sonar.sh stop
#fi

# Clean up sonar
#rm -rf sonarqube-5.2
#rm -rf sonarqube-5.2.zip