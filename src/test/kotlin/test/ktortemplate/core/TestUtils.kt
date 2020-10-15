package test.ktortemplate.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.identity
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.serialization.json
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import javax.sql.DataSource
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SchemaUtils
import org.koin.dsl.module
import test.ktortemplate.conf.database.DatabaseConnection
import test.ktortemplate.core.httphandler.defaultRoutes
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.Test
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.persistance.sql.CarMappingsTable
import test.ktortemplate.core.persistance.sql.CarRepositoryImpl
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.service.CarServiceImpl
import test.ktortemplate.core.utils.json.JsonSettings

private fun bootstrapDatabase(dbc: DatabaseConnection) {
    dbc.query {
        SchemaUtils.create(CarMappingsTable)
    }
}

fun initServicesAndRepos() = module {
    single<CarRepository> { CarRepositoryImpl() }
    single<CarService> { CarServiceImpl() }
}

fun initDbCore() = module {
    val dataSource: DataSource = HikariDataSource(
        HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:test"
            maximumPoolSize = 5
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            leakDetectionThreshold = 10000
            poolName = "sat"
            validate()
        }
    )

    val dbc = DatabaseConnection(dataSource)
    bootstrapDatabase(dbc)
    single { dbc }
}

fun Application.testModule() {

    val car = Car(123, "asd", "asddd")
    val test = Test(listOf(car), null)

    val x = JsonSettings.toJson(test)
    println(x)

    val carr: Test<Car> = JsonSettings.fromJson(x)
    println(carr)

    install(DefaultHeaders)
    install(Compression) {
        gzip {
            priority = 100.0
        }
        identity {
            priority = 10.0
        }
        deflate {
            priority = 1.0
        }
    }

    install(CallLogging)
    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json,
            json = JsonSettings.mapper
        )
    }
    install(Routing) {
        defaultRoutes()
    }
}

fun <R> testApp(test: TestApplicationEngine.() -> R): R {
    return withTestApplication(
        {
            testModule()
        },
        test
    )
}
