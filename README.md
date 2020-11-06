# ktor-template

![Build and Test](https://github.com/waterdog-oss/ktor-template/workflows/Build%20and%20Test/badge.svg)
[![GitHub release](https://img.shields.io/github/release/aanciaes/ktor-template.svg)](https://github.com/aanciaes/ktor-template/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues/aanciaes/ktor-template.svg)](https://github.com/aanciaes/ktor-template/issues/)

## What am I getting?
Ktor-template is a set of utilities built on top of [Ktor](https://ktor.io) to quickly bootstrap a production grade application/microservice.
This project offers the following features:

* JSON serialization utilities for kotlinx.serialization, namely around common data types like `java.time.Instant` and 
`java.util.UUID`;
* Error handling capable of producing errors in a friendly JSON format;
* Object validation using [Valiktor](https://github.com/valiktor/valiktor);
* Semi structured logging that can easily be ingested by log aggregation tools;
* Pagination utilities, using the format specified by [jsonapi.org](https://jsonapi.org);
* Kubernetes style readiness and liveness checks;
* Relational database connection setup using [Exposed](https://github.com/JetBrains/Exposed);

This is obviously somewhat opinionated, and that's why it falls outside the scope of something like [start.ktor.io](https://start.ktor.io).

## How do I get started?

The ktor-template offers two distinct artifacts: ktor-template-core and ktor-template-database.

The reason for this separation is due to the fact that not all projects use a relational database. 
The ktor-template-database requires ktor-template-core however. 

### Gradle dependencies
In your build.gradle, you'll need to:

1 - Add the maven repository:
```groovy
repositories {
    jcenter()
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
    defaultExceptionHandler()
    defaultStatusCodes()
}
```

The `defaultExceptionHandler` is responsible for the interception of exceptions, and their conversion to a more palatable format.
As part of the ktor-template-core an `AppException` class has been introduced, and can be used to surface things like validation errors to the consumer.

The `defaultStatusCodes` configures the default response for a 404 Not found.

#### Logging:
The logging functionality uses the ktor [CallLogging](https://ktor.io/docs/call-logging.html) and [CallId](https://ktor.io/docs/call-id.html) features to install a logger that does two things:
1 - it adds a unique request ID to each request (if none is present in the specified header);
2 - outputs the log in a semi-structured format that can easily be parsed by log aggregation tools;

In your ktor module definition (e.g: [mobi.waterdog.rest.template.tests.Application](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/Application.kt))
In your ktor module definition (e.g: [mobi.waterdog.rest.template.tests.Application](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/Application.kt))
```kotlin
val callIdHeader = SemiStructuredLogFormatter.REQUEST_ID_HEADER
install(CallLogging) {
    level = org.slf4j.event.Level.INFO
    callIdMdc(callIdHeader)
}
install(CallId) {
    generate { it.request.headers[callIdHeader] ?: UUID.randomUUID().toString() }
    replyToHeader(callIdHeader)
}
```

Also you'll need to configure `logback.xml`.
```xml

<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="ch.qos.logback.contrib.json.classic.JsonLayout">
                <timestampFormat>yyyy-MM-dd'T'HH:mm:ss.SSSX</timestampFormat>
                <timestampFormatTimezoneId>Etc/UTC</timestampFormatTimezoneId>
                <jsonFormatter class="mobi.waterdog.rest.template.log.SemiStructuredLogFormatter">
                    <jsonPrefix>JSON:</jsonPrefix>
                </jsonFormatter>
            </layout>
        </encoder>
    </appender>

    <logger name="Exposed" level="debug">
        <appender-ref ref="STDOUT"/>
    </logger>

    <root level="info">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```
Note the encoder setup on the appender and `<jsonFormatter>` and `<jsonPrefix>` in particular.
You can then use your usual slf4j logging, and the output of the logs should be something like:
```
2020-11-05T23:50:42.665Z INFO  de1126db-5314-49bd-ba2b-24c1e07d6f8e 200 OK: GET - /readiness                                 JSON:{"timestamp":"2020-11-05T23:50:42.665Z","level":"INFO","thread":"DefaultDispatcher-worker-3 @request#1","logger":"ktor.test","message":"200 OK: GET - /readiness","metadata":{"X-Request-Id":"de1126db-5314-49bd-ba2b-24c1e07d6f8e"}}
```

In order to 

#### Readiness and liveness checks:
The health check feature uses concepts from: [https://github.com/zensum/ktor-health-check]

In your ktor module definition (e.g: [mobi.waterdog.rest.template.tests.Application](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/Application.kt))
```kotlin
install(Health) {
    liveness()
    readiness()
}
```

The `liveness` and `readiness` functions are just plain kotlin extension functions, and you can find an example [here](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/core/utils/healthcheck/HealthCheck.kt)

#### Object validation:
The object validation uses [Valiktor](https://github.com/valiktor/valiktor). The utilities provide a `Validatable` class that
includes utility methods for declaring validation rules and a method to assert those rules.

1 - Declaring validation rules:
```kotlin
@Serializable
data class CarSaveCommand(val brand: String, val model: String, val wheels: List<Wheel>? = null) : Validatable<CarSaveCommand>() {
    override fun rules(validator: Validator<CarSaveCommand>) {
        validator
            .validate(CarSaveCommand::brand)
            .hasSize(3, 20)
            .isIn("porsche", "lamborghini", "koenigsegg")
        validator
            .validate(CarSaveCommand::wheels)
            .hasSize(3, 6)
            .validateForEach { it.applyRules(this) }
    }
}
```

2 - Validating an instance:
```kotlin
post("/$apiVersion/cars") {
    val newCar = call.receive<CarSaveCommand>()
    newCar.validate()

    val insertedCar = carService.insertNewCar(CarSaveCommand(newCar.brand, newCar.model))
    call.respond(insertedCar)
}
```

The call to `validate()` throws an `AppException` so it plays well with error handling.
You can find an example [here](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/core/httphandler/DefaultRoutes.kt)

#### Pagination utilities:
The ktor-template-core provides utility functions to parse the request parameters and respond with a page in the format specified  by [https://jsonapi.org] for
[filtering](https://jsonapi.org/recommendations/#filtering), [pagination](https://jsonapi.org/format/#fetching-pagination) and [sorting](https://jsonapi.org/format/#fetching-sorting)

```kotlin
get("/$apiVersion/cars") {
    val pageRequest = call.parsePageRequest()
    val totalElements = carService.count(pageRequest)
    val data = carService.list(pageRequest)
    call.respondPaged(
        PageResponse.from(
            pageRequest = pageRequest,
            totalElements = totalElements,
            data = data,
            path = call.request.path()
        )
    )
}
```

You can find an example [here](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/core/httphandler/DefaultRoutes.kt)

#### Relational database support:
Relational database support is optional and provided by ktor-template-database. It is a collection of utiliy methods over [Exposed](https://github.com/JetBrains/Exposed)
that make it simpler to use.

1 - Setting up a database connection:

In order to use these utilities, the first step is to setup the `DatabaseConnection` with a connection pool. An example of
setup using [HikariCP](https://github.com/brettwooldridge/HikariCP) can be found [here](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/conf/EnvironmentConfigurator.kt)
in the `initDbCore` method.

2 - Querying the database

After setting up the `DatabaseConnection` you can use the various methods this class exposes to query the database.
Examples can be found [here](https://github.com/waterdog-oss/ktor-template/blob/development/ktor-template-example/src/main/kotlin/mobi/waterdog/rest/template/tests/core/service/CarServiceImpl.kt)

## I want to contribute!

### Code structure
The ktor-template is divided into several modules:

* `ktor-template-core`: Module that implements features that are an important part of any production ready application/microservice (serialization, error handling, validation, logging and health checks)
* `ktor-template-database`: Database related utilities
* `ktor-template-example`: A sample project that provides usage examples as well as a testbed for the different features this project provides.

### Building and running the project locally
The project comes with the gradle wrapper, so in order to build the project you can easily use the `gradlew` command.

* `gradlew run` - run the project (note that the project may have dependencies to other systems like RDMSs). Check the 
required environment variables on `/src/main/resources/application.conf`.
* `gradlew test` - run the tests
* `gradlew clean build` - do a local build (this will run the compilation and verification tasks, i.e., linter and tests)
