package test.ktortemplate.containers

import org.testcontainers.containers.PostgreSQLContainer

class KPostgreSQLContainer(imgName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imgName)

object PgSQLContainerFactory {

    val instance by lazy {
        newInstance(
            databaseName = "ktortemplate",
            username = "ktor",
            password = "template"
        )
    }

    private fun newInstance(
        databaseName: String = "",
        username: String = "",
        password: String = "",
        reUse: Boolean = true,
        image: String = "postgres",
        version: String = "12"
    ): KPostgreSQLContainer {
        val instance = KPostgreSQLContainer("$image:$version")
            .withReuse(reUse)
            .withDatabaseName(databaseName)
            .withUsername(username)
            .withPassword(password)
            .withCommand("postgres -c max_connections=1000")

        instance.start()
        return instance
    }
}
