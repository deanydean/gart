#!/bin/bash
# Get the runtime tools on debian

# Installing dev tools from repos
apt-get install -y unzip openjdk-7-jre-headless curl git

# Get SDKMAN and install groovy from it
curl -s get.sdkman.io | bash
JAVA_HOME=/usr source ${HOME}/.sdkman/bin/sdkman-init.sh
sgd install groovy
