# ktor-template

![Build and Test](https://github.com/aanciaes/ktor-template/workflows/Build%20and%20Test/badge.svg)
[![GitHub release](https://img.shields.io/github/release/aanciaes/ktor-template.svg)](https://github.com/aanciaes/ktor-template/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues/aanciaes/ktor-template.svg)](https://github.com/aanciaes/ktor-template/issues/)

## What you are getting
Ktor starter pack with gradle. Deploy a web server with docker 🐳 quickly, with dependency injection, database 
connection and unit tests with JUnit 5 (and more!).

The intended use of this repo is to serve as the base for other projects so feel free to clone this and adjust the project
name and version on `settings.gradle` and `build.gradle`. Optionally we also recommend you update the default values 
for the `APP_NAME` and `APP_VERSION` arguments in the docker file, otherwise you will need to provide docker with the
correct build arguments.
 
### Code structure
The Ktor template follows a simple structure with two source sets (main and test).

## Local development
### Building and running the project locally
The project comes with the gradle wrapper, so in order to build the project you can easily use the `gradlew` command.

* `gradlew run` - run the project
* `gradlew test` - run the tests
* `gradlew clean build` - do a local build (this will run the compilation and verification tasks, i.e., linter and tests)
* `gradlew shadowJar` - Build the uber jar that will be used in production

## Preparing a production build

In order to build the docker image you can simply run `docker build -t ktor-template .`. To run the application just use
the docker run command: `docker run -p 8080:8080 -it ktor-template:latest`.

If you cloned this repo and changed the project name and version you may need to pass specific build args if you haven't 
overridden the defaults on the dockerfile. For additional information regarding docker build arguments check the `--build-arg` 
flag on the official [docker build documentation](https://docs.docker.com/engine/reference/commandline/build/).

When running the image there are a number of environment variables that can be and should be used in production.

### Available build args

- __APP_NAME:__ Should match the root project name in `settings.gradle`
- __APP_VERSION:__ Should match the version on `build.gradle`

### Available environment variables:

- __KTOR_PORT:__ The port the server is listening on. Defaults to `8080`
- __KTOR_ENV:__ The environment that the application is running on. Can be `dev`, `staging` or `prod`. Defaults to `dev`
- __JAVA_OPTS:__ Runtime set of java opts used by the JVM.