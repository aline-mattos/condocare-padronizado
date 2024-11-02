## CondoCare
O CondoCare é um sistema desenvolvido em 2023 para auxiliar na gestão de condomínios, facilitando a reserva de áreas comuns e o controle das entregas na portaria. 
Ele é composto por um front-end em Angular e um back-end em Kotlin (usando o framework Ktor), que é nosso foco de melhoria, e foi desenvolvido para a disciplina de Tecnologias para Desenvolvimento Web.

### Funcionalidades Principais
#### 1. Reserva de Áreas Comuns:
Permite que os moradores reservem espaços compartilhados do condomínio, como salão de festas e lavanderia, separados como serviços. 
Inclui um sistema para verificar a disponibilidade das áreas, evitando conflitos e garantindo que cada área seja reservada apenas uma vez por período.

#### 2. Controle de Entregas na Portaria:
Gerencia as entregas que chegam ao condomínio e possibilita que o porteiro registre o recebimento de encomendas para os moradores.
Usa autenticação JWT para garantir que cada morador visualize apenas suas próprias informações e entregas.

## Melhorias Identificadas:
#### 1. Separação da Lógica de Negócio e Acesso ao Banco de Dados: 
A lógica de manipulação de dados e o acesso ao banco de dados estão misturados.

#### 2. Validação de Dados na Camada de Serviço: 
A validação de dados (por exemplo, checar se o id existe antes de atualizar ou deletar) está implementada diretamente nas rotas, misturando a lógica de negócios com a configuração das rotas.

#### 3. Tratamento de Exceções: 
Exceções não tratadas no acesso ao banco de dados podem fazer com que o código quebre ou retorne erros inesperados.


## Separação da Lógica de Negócio e Acesso ao Banco de Dados
### Padrão de Projeto: Repository
O Repository Pattern permite um encapsulamento da lógica de acesso a dados, impulsionando o uso da injeção de dependencia (DI) e proporcionando uma visão mais orientada a objetos das interações com a DAL.
#### Os grandes benefícos ao utilizar esse pattern são:
- Permitir a troca do banco de dados utilizado sem afetar o sistema como um todo.
- Código centralizado em um único ponto, evitando duplicidade.
- Facilita a implementação de testes unitários.
- Diminui o acoplamento entre classes.
- Padronização de códigos e serviços.

Mais informações [aqui](https://renicius-pagotto.medium.com/entendendo-o-repository-pattern-fcdd0c36b63b).

Vamos fazer a modificação para um Repository Pattern nos códigos relacionados à "Reservation", que são os que cuidam da reserva de áreas comuns.

![image](https://github.com/user-attachments/assets/5cef380a-6640-4a5f-846e-0e61f87b4920)
###### Figura 1. Estrutura do projeto antes das modificações.

#### Visão Geral da Estrutura

O código já está organizado em uma arquitetura baseada em camadas, separando as responsabilidades de cada código, porém adicionamos o "repositories":
- Modelo de Dados (models)
- Repositório (repositories)
- Serviço (services)
- Rotas (modules)

![image](https://github.com/user-attachments/assets/8d75bea5-11cb-4b0f-a9ff-4c2a34ce6abb)
###### Figura 2. Estrutura do projeto após as modificações.

![image](https://github.com/user-attachments/assets/c5af7ad8-7e68-409f-a321-c7eb412aaa94)
###### Figura 3. Repository Service.

Agora o Repository Service, antigo ReservationDB, utiliza a interface ReservationRepository para realizar operações, facilitando a substituição do repositório se necessário.

```package com.condocare.repositories

import com.condocare.models.Reservation
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class ReservationRepositoryImpl(private val database: Database) : ReservationRepository {
    object Reservations : Table() {
        val id = varchar("id", 255)
        val type = varchar("type", 255).nullable()
        val name = varchar("name", 255).nullable()
        val date = varchar("date", 255).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Reservations)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun create(newReservation: Reservation): Reservation {
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

    override suspend fun findAll(): List<Reservation> = dbQuery {
        Reservations.selectAll().map { row -> row.toReservation() }
    }

    override suspend fun findById(id: String): Reservation? = dbQuery {
        Reservations.select { Reservations.id eq id }
            .map { row -> row.toReservation() }
            .singleOrNull()
    }

    override suspend fun update(id: String, updatedReservation: Reservation): Reservation? {
        val affectedRows = dbQuery {
            Reservations.update({ Reservations.id eq id }) {
                it[type] = updatedReservation.type
                it[name] = updatedReservation.name
                it[date] = updatedReservation.date
            }
        }
        return if (affectedRows > 0) findById(id) else null
    }

    override suspend fun delete(id: String) {
        dbQuery { Reservations.deleteWhere { Reservations.id eq id } }
    }

    private fun ResultRow.toReservation() = Reservation(
        id = this[Reservations.id],
        type = this[Reservations.type],
        name = this[Reservations.name],
        date = this[Reservations.date]
    )
}
