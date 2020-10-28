package test.ktortemplate.conf.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

    suspend fun ping(): Boolean = newSuspendedTransaction(Dispatchers.IO, database) {
        try {
            exec("SELECT 1")
            true
        } catch (e: Exception) {
            false
        }
    }
}
