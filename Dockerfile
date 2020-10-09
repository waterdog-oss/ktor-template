# Stage 1 - Download gradle
FROM openjdk:11-jdk as gradle
ENV GRADLE_VERSION 6.6.1
ENV GRADLE_SHA 7873ed5287f47ca03549ab8dcb6dc877ac7f0e3d7b1eb12685161d10080910ac
RUN cd /usr/lib \
	&& curl -fl https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle-bin.zip \
	&& echo "$GRADLE_SHA gradle-bin.zip" | sha256sum -c - \
	&& unzip "gradle-bin.zip" \
	&& ln -s "/usr/lib/gradle-${GRADLE_VERSION}/bin/gradle" /usr/bin/gradle \
	&& rm "gradle-bin.zip"

# Stage 2 assemble the ktor application by generating a fat jar
FROM openjdk:11-jdk as jar
ENV GRADLE_VERSION 6.6.1

COPY --from=gradle /usr/lib/gradle-${GRADLE_VERSION} /usr/lib/gradle-${GRADLE_VERSION}
RUN ln -s "/usr/lib/gradle-${GRADLE_VERSION}/bin/gradle" /usr/bin/gradle

# Set Appropriate Environmental Variables
ENV GRADLE_HOME /usr/lib/gradle
ENV PATH $PATH:$GRADLE_HOME/bin

# Set the work dir
WORKDIR /home/gradle/project
COPY ./ /home/gradle/project
RUN gradle clean shadowJar

# Stage 3 run the application
FROM openjdk:11-jre-slim-buster

ARG APP_NAME="ktor-template"
ENV JAR_NAME=${APP_NAME}

ARG APP_VERSION="0.1"
ENV JAR_VERSION=${APP_VERSION}

WORKDIR /home/app
COPY --from=jar /home/gradle/project/build/libs/${JAR_NAME}-${JAR_VERSION}-all.jar app.jar

# Setup jattach so that we have some of the tooling from the JDK in the JRE
COPY ./docker-resources/jattach /usr/local/bin/jattach
RUN chmod +x /usr/local/bin/jattach

# Provide some default jvm options, but these should be overriden
ENV JAVA_OPTS="-server -XX:+UseStringDeduplication"

# Entrypoint
CMD java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar