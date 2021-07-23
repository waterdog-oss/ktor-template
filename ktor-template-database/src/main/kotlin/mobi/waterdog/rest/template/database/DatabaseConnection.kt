package mobi.waterdog.rest.template.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class DatabaseConnection(private val dataSource: DataSource) {
    private val database: Database by lazy {
        Database.connect(dataSource)
    }

    fun <T> query(block: Transaction.() -> T): T = transaction(database) {
        block()
    }

    suspend fun <T> suspendedQuery(
        context: CoroutineContext = MDCContext(),
        txIsolation: Int = Connection.TRANSACTION_REPEATABLE_READ,
        block: suspend Transaction.() -> T
    ): T = newSuspendedTransaction(Dispatchers.IO, database, txIsolation) {
        val newContext = coroutineContext + context
        withContext(newContext) {
            block()
        }
    }

    suspend fun <T : Any> executeRaw(
        rawSql: String,
        transform: (ResultSet) -> T
    ): T? = newSuspendedTransaction(Dispatchers.IO, database) {
        exec(rawSql, emptyList(), null, transform)
    }

    suspend fun ping(): Boolean =
        try {
            executeRaw("SELECT 1") {
                it.next()
            } ?: false
        } catch (e: Exception) {
            false
        }
}
