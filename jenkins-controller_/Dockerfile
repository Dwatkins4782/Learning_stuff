# lts-jdk11 v2.289.1
FROM docker.repo.ercot.com/jenkins/jenkins:lts-jdk11

LABEL maintainer="Brandon Johnson <brandon.johnson@ercot.com>"
LABEL projectId="SPSVC"

USER root:root

# The Jenkins/Jenkins image uses a tool to ensure that the system level trust store
# and the JVM's cacerts store are kept synchronized. Consequently any efforts to modify
# cacerts will not stick. Adding new trusted roots must be added at the system level.
RUN curl --insecure -fSsL -o /usr/local/share/ca-certificates/ERCOTROOTCA.crt "https://repo.ercot.com/artifactory/generic-ercot/certs/ca/ERCOTROOTCA.pem" && \
    curl --insecure -fSsL -o /usr/local/share/ca-certificates/ERCOTIssueCA01.crt "https://repo.ercot.com/artifactory/generic-ercot/certs/ca/ERCOTIssueCA01.pem" && \
    curl --insecure -fSsL -o /usr/local/share/ca-certificates/ERCOTIssueCA02.crt "https://repo.ercot.com/artifactory/generic-ercot/certs/ca/Ercotissueca02.pem" && \
    curl --insecure -fSsL -o /usr/local/share/ca-certificates/GlobalSign-Intermediate.crt "https://repo.ercot.com/artifactory/generic-thirdparty/globalsign/intermediate.pem" && \
    keytool -importcert -noprompt -alias ERCOTROOTCA -cacerts -storepass changeit -file /usr/local/share/ca-certificates/ERCOTROOTCA.crt && \
    keytool -importcert -noprompt -alias ERCOTIssueCA01 -cacerts -storepass changeit -file /usr/local/share/ca-certificates/ERCOTIssueCA01.crt && \
    keytool -importcert -noprompt -alias Ercotissueca02 -cacerts -storepass changeit -file /usr/local/share/ca-certificates/ERCOTIssueCA02.crt && \
    keytool -importcert -noprompt -alias GlobalSignIntermediate -cacerts -storepass changeit -file /usr/local/share/ca-certificates/GlobalSign-Intermediate.crt && \
    update-ca-certificates --verbose --fresh && \
    keytool -v --list --cacerts --storepass "changeit" | grep -i -e "ercot" -e "globalsign"

USER jenkins:jenkins

