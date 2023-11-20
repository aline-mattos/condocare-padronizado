package com.condocare.services

import com.condocare.models.Reservation
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ReservationDB(database: Database) {
    object Reservations : Table() {
        val id = varchar("id", 255)
        val type = varchar("type", 255).nullable()
        val name = varchar("name", 255).nullable()
        val date = varchar("date", 255).nullable()

        override val primaryKey = PrimaryKey(ServiceDB.Services.id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Reservations)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(newReservation: Reservation): Reservation {
        val id = UUID.randomUUID().toString()

        dbQuery {
            Reservations.insert { row ->
                row[Reservations.id] = id
                row[Reservations.type] = newReservation.type
                row[Reservations.name] = newReservation.name
                row[Reservations.date] = newReservation.date
            }
        }

        return findById(id) ?: throw IllegalStateException("Falha ao criar a reserva")
    }

    suspend fun findAll(): List<Reservation> = dbQuery {
        Reservations.selectAll().map { row -> row.toReservation() }
    }

    suspend fun findById(id: String): Reservation? {
        return dbQuery {
            Reservations.select { Reservations.id eq id }
                .map { row -> row.toReservation() }
                .singleOrNull()
        }
    }

    suspend fun update(id: String, updatedReservation: Reservation): Reservation? {
        val affectedRows = dbQuery {
            Reservations.update({ Reservations.id eq id }) {
                it[type] = updatedReservation.type
                it[name] = updatedReservation.name
                it[date] = updatedReservation.date
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
            Reservations.deleteWhere { Reservations.id.eq(id) }
        }
    }


    private fun ResultRow.toReservation() = Reservation(
        id = this[Reservations.id],
        type = this[Reservations.type],
        name = this[Reservations.name],
        date = this[Reservations.date]
    )
}

