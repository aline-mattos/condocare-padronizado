package com.condocare.services

import com.condocare.models.Service
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ServiceDB(database: Database) {
    object Services : Table() {
        val id = varchar("id", 255)
        val type = varchar("type", 255).nullable()
        val name = varchar("name", 255).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Services)
        }
    }

        private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

        suspend fun create(newService: Service): Service {
            val id = UUID.randomUUID().toString()

            dbQuery {
                Services.insert { row ->
                    row[Services.id] = id
                    row[Services.type] = newService.type
                    row[Services.name] = newService.name
                }
            }

            return findById(id) ?: throw IllegalStateException("Falha ao criar o serviço")
        }


        suspend fun findAll(): List<Service> = dbQuery {
            Services.selectAll().map { row -> row.toService() }
        }

        suspend fun findById(id: String): Service? {
            return dbQuery {
                Services.select { Services.id eq id }
                    .map { row -> row.toService() }
                    .singleOrNull()
            }
        }

        suspend fun findByType(type: String): List<Service> = dbQuery {
                Services.select { Services.type eq type }
                    .map { row -> row.toService() }
        }


        suspend fun update(id: String, updatedService: Service): Service? {
            val affectedRows = dbQuery {
                Services.update({ Services.id eq id }) {
                    it[type] = updatedService.type
                    it[name] = updatedService.name
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
                Services.deleteWhere { Services.id.eq(id) }
            }
        }

        private fun ResultRow.toService() = Service(
            id = this[Services.id],
            type = this[Services.type],
            name = this[Services.name]
        )
}