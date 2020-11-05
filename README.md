# ktor-template

![Build and Test](https://github.com/waterdog-oss/ktor-template/workflows/Build%20and%20Test/badge.svg)
[![GitHub release](https://img.shields.io/github/release/aanciaes/ktor-template.svg)](https://github.com/aanciaes/ktor-template/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues/aanciaes/ktor-template.svg)](https://github.com/aanciaes/ktor-template/issues/)

## What you are getting
Ktor-template is a set of utilities built on top of [Ktor](https://ktor.io) to quickly bootstrap a production grade service.
This project offers the following features:

* JSON serialization utilities for kotlinx.serialization, namely around common data types like `java.time.Instant` and 
`java.util.UUID`;
* Error handling capable of producing errors in a friendly JSON format;
* Object validation using [Valiktor](https://github.com/valiktor/valiktor);
* Pagination utilities, using the format specified by [jsonapi.org](https://jsonapi.org);
* Semi structured logging that can easily be ingested by log aggregation tools;
* Kubernetes style readyness and liveness checks;
* Relational database connection setup using [Exposed](https://github.com/JetBrains/Exposed);

This is obviously somewhat opinionated, and that's why it falls outside the scope of something like [start.ktor.io](https://start.ktor.io).

## Getting started

The ktor-template offers two distinct artifacts: ktor-template-core and ktor-template-database.

The reason for this separation is due to the fact that not all projects use a relational database. 
The ktor-template-database requires ktor-template-core however. 

### Gradle dependencies
In your build.gradle, you'll need to:

1 - Add the maven repository:
```groovy
repositories {
    mavenCentral()
    jcenter()
    maven { url = "https://dl.bintray.com/waterdog/ktor-template" }
}
```

2 - Import the dependencies
```groovy
dependencies {
    implementation 'mobi.waterdog.ktor-template:ktor-template-core:1.0.0'
    // Optional, but useful if your projects uses a relational database 
    implementation 'mobi.waterdog.ktor-template:ktor-template-database:1.0.0' 
}
```

### Features:
In order to showcase how to use the various features, we'll refer to the ktor-template-example module:

#### Json support:
Json support can be added via the usual [ContentNegotiation](https://ktor.io/docs/serialization-converter.html#configuration)
feature of ktor. The template just offers a convenient json configuration via the `JsonSettings` object.

In your ktor module definition (e.g: [mobi.waterdog.rest.template.tests.Application](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/Application.kt))
```kotlin
install(ContentNegotiation) {
    json(
        contentType = ContentType.Application.Json,
        json = JsonSettings.mapper
    )
}
```

#### Error handling:
The default error handling strategy leverages the [StatusPages](https://ktor.io/docs/guides-api.html#statuspages) feature of ktor, and offers some basic building blocks to
deal with exceptions and convert them to a friendly JSON format that can be handled by a consumer.

In your ktor module definition (e.g: [mobi.waterdog.rest.template.tests.Application](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/Application.kt))
```kotlin
install(StatusPages) {
    appException()
    defaultStatusCodes()
}
```


#### Object validation:

#### Pagination utilities:

#### Logging:

#### Readyness and liveness checks:

#### Relational database support:

## Contributing

### Code structure
The ktor-template 

### Building and running the project locally
The project comes with the gradle wrapper, so in order to build the project you can easily use the `gradlew` command.

* `gradlew run` - run the project (note that the project may have dependencies to other systems like RDMSs). Check the 
required environment variables on `/src/main/resources/application.conf`.
* `gradlew test` - run the tests
* `gradlew clean build` - do a local build (this will run the compilation and verification tasks, i.e., linter and tests)
* `gradlew shadowJar` - Build the uber jar that will be used in production
