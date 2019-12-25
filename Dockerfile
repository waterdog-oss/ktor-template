FROM gradle:6.0.1-jdk8

# Default environment profile when running with docker
ENV KTOR_ENV dev

# Set the work dir and run the build
WORKDIR /home/gradle/project
COPY ./ /home/gradle/project
RUN gradle clean build

# Entrypoint
CMD gradle run
