FROM debian:8

# Add the content needed to use Java8
#ADD ./java8/oracle-java.list /etc/apt/sources.list.d/
#ADD ./java8/get_java8.sh /opt/
#RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886

# Update the image
RUN apt-get update
RUN apt-get dist-upgrade -y

# Install all runtime tools from the debian repos
RUN apt-get install -y unzip openjdk-7-jre-headless curl git

# Set up the env for runtime
ENV JAVA_HOME /usr
ENV SDKMAN .sdkman/bin/sdkman-init.sh

# Install SDKman and groovy
RUN curl -s get.sdkman.io | bash
RUN /bin/bash -c 'source $HOME/$SDKMAN; sdk install groovy'

# Get Gart
RUN git clone http://github.com/deanydean/gart /opt/gart

# Run Gart on startup
CMD /bin/bash -c 'source $HOME/$SDKMAN; /opt/gart/gart'
