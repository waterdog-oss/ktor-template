package test.ktortemplate.conf.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import javax.sql.DataSource

class DatabaseConnection(private val dataSource: DataSource) {
    private val database: Database by lazy {
        Database.connect(dataSource)
    }

    fun <T> query(block: () -> T): T = transaction(database) {
        block()
    }

    fun ping(): Boolean = try {
        transaction(database) { exec("SELECT 1") }
        true
    } catch (e: Exception) {
        false
    }
}
