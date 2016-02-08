FROM debian:8

# Add the content needed to use Java8
#ADD ./java8/oracle-java.list /etc/apt/sources.list.d/
#ADD ./java8/get_java8.sh /opt/
#RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886

# Update the image
RUN apt-get update
RUN apt-get dist-upgrade -y

# Install all runtime tools
RUN apt-get install -y groovy2 git

# Get Gart
RUN git clone http://github.com/deanydean/gart /opt/gart

# Run Gart on startup
CMD GROOVY_HOME=/usr JAVA_HOME=/usr /opt/gart/gart
