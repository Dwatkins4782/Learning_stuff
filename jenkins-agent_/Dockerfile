FROM docker.repo.ercot.com/ercot/core/ubi/ubi8:latest

LABEL maintainer="Brandon Johnson <brandon.johnson@ercot.com>"
LABEL projectId="SPSVC"

USER root:root

RUN dnf -y install sudo which dos2unix file openssl && \
    dnf clean all && \
    rm -rf /var/log/dnf* && \
    groupadd -g 1000 jenkins && \
    useradd -m -u 1000 -g 1000 jenkins && \
    echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

USER jenkins:jenkins


CMD ["/bin/bash", "-l"]
