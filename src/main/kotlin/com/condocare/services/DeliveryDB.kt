package com.condocare.services

import com.condocare.models.Delivery
import com.condocare.models.Service
import com.condocare.models.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DeliveryDB(database: Database) {
    object Deliveries: Table() {
        val id = varchar("id",255)
        val type = varchar("type",255).nullable()
        val apartment = varchar("apartment",255).nullable()
        val block = varchar("block",255).nullable()
        val receivedDate = varchar("receivedDate",255).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Deliveries)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(newDelivery: Delivery): Delivery {
        val id = UUID.randomUUID().toString()

        dbQuery {
            Deliveries.insert { row ->
                row[Deliveries.id] = id
                row[Deliveries.type] = newDelivery.type
                row[Deliveries.apartment] = newDelivery.apartment
                row[Deliveries.block] = newDelivery.block
                row[Deliveries.receivedDate] = newDelivery.receivedDate
            }
        }

        return findById(id) ?: throw IllegalStateException("Falha ao criar a encomenda")
    }

    suspend fun findAll(): List<Delivery> = dbQuery {
        Deliveries.selectAll()
            .map { row -> row.toDelivery() }
    }

    suspend fun findById(id: String): Delivery? {
        return dbQuery {
            Deliveries.select { Deliveries.id eq id }
                .map { row -> row.toDelivery() }
                .singleOrNull()
        }
    }

    suspend fun findFromResident(block: String, apartment: String) = dbQuery {
        Deliveries.select { Deliveries.block eq block and (Deliveries.apartment eq apartment) }
            .map { row -> row.toDelivery() }
    }

    suspend fun update(id: String, updateDelivery: Delivery): Delivery? {
        val affectedRows = dbQuery {
            Deliveries.update({ Deliveries.id eq id }) {
                it[type] = updateDelivery.type
                it[apartment] = updateDelivery.apartment
                it[block] = updateDelivery.block
                it[receivedDate] = updateDelivery.receivedDate
            }
        }

        return if (affectedRows > 0) {
            findById(id)
        } else {
            null
        }
    }

    suspend fun delete(id: String) {
        dbQuery {
            Deliveries.deleteWhere { Deliveries.id.eq(id) }
        }
    }

    private fun ResultRow.toDelivery() = Delivery(
        id = this[Deliveries.id],
        type = this[Deliveries.type],
        apartment = this[Deliveries.apartment],
        block = this[Deliveries.block],
        receivedDate = this[Deliveries.receivedDate],
    )
}