import com.condocare.models.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

class UserDB(private val database: Database) {
    object Users :  Table() {
        val id = varchar("id",255)
        val token = varchar("token",255).nullable()
        val type = varchar("type",255).nullable()
        val name = varchar("name",255).nullable()
        val apartment = varchar("apartment",255).nullable()
        val block = varchar("block",255).nullable()
        val email = varchar("email",255).nullable()
        val password = varchar("password",255).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(newUser: User): User {
        val id = UUID.randomUUID().toString()
        val token = generateToken(id)

        dbQuery {
            Users.insert { row ->
                row[Users.id] = id
                row[Users.token] = token
                row[Users.type] = newUser.type
                row[Users.name] = newUser.name
                row[Users.apartment] = newUser.apartment
                row[Users.block] = newUser.block
                row[Users.email] = newUser.email
                row[Users.password] = newUser.password
            }
        }

        return findById(id) ?: throw IllegalStateException("Falha ao criar o usuário")
    }

    suspend fun findAll(): List<User> = dbQuery {
        Users.selectAll()
            .map { row -> row.toUser() }
    }

    suspend fun findById(id: String): User? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { row -> row.toUser() }
                .singleOrNull()
        }
    }

    suspend fun findByEmail(email: String): User? {
        return dbQuery {
            Users.select { Users.email eq email }
                .map { row -> row.toUser() }
                .singleOrNull()
        }
    }

    suspend fun update(id: String, updatedUser: User): User? {
        val affectedRows = dbQuery {
            Users.update({ Users.id eq id }) {
                it[token] = updatedUser.token
                it[type] = updatedUser.type
                it[name] = updatedUser.name
                it[apartment] = updatedUser.apartment
                it[block] = updatedUser.block
                it[email] = updatedUser.email
                it[password] = updatedUser.password
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
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

    fun generateToken(userId: String): String {
        val secret = "test-secret" // Use seu segredo real
        val issuer = "test-issuer" // Defina seu emissor real

        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId)
            .sign(Algorithm.HMAC256(secret))
    }

    private fun ResultRow.toUser() = User(
        id = this[Users.id],
        token = this[Users.token],
        type = this[Users.type],
        name = this[Users.name],
        apartment = this[Users.apartment],
        block = this[Users.block],
        email = this[Users.email],
        password = this[Users.password],
    )
}