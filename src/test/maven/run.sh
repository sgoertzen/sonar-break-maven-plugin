#!/usr/bin/env bash
#curl -L -O https://sonarsource.bintray.com/Distribution/sonarqube/sonarqube-5.2.zip
unzip sonarqube-5.2.zip
#sonarqube-5.2/bin/linux-x86-64/sonar.sh start
sonarqube-5.2/bin/macosx-universal-64/sonar.sh start
for i in {1..20}; do if (curl -I http://localhost:9000 2>/dev/null | grep -q 200) then echo "Sonar up and running"; break; fi; sleep 1; done

mvn clean install sonar:sonar com.sgoertzen.maven:sonarbreak:1.0:sonarBreak
#sonarqube-5.2/bin/linux-x86-64/sonar.sh stop


#sonarqube-5.2/bin/macosx-universal-64//sonar.sh stop
#rm -rf sonarqube-5.2