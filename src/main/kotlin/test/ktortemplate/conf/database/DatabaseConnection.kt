package test.ktortemplate.conf.database

import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConnection(private val dataSource: DataSource) {
    private val database: Database by lazy {
        Database.connect(dataSource)
    }

    fun <T> query(block: () -> T): T = transaction(database) {
        val blockReturn = block()
        commit()
        blockReturn
    }
}
