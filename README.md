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

## Padrões de projeto utilizados e as melhorias identificadas:
### 1. Padrão de Projeto: Repository
**1. Separação da Lógica de Negócio e Acesso a Dados:** A lógica de acesso a dados é isolada da lógica de negócios do serviço utilizando o padrão Repository. Isso traz uma clara separação de responsabilidades, facilitando a leitura, manutenção e evolução do código. O serviço ("services") se concentra exclusivamente nas regras de negócios, enquanto o repositório ("repository") lida com o armazenamento de dados. Esse encapsulamento permite que mudanças no banco de dados ou nas consultas SQL afetem apenas o repositório, sem impactar a camada de serviço.

**2. Flexibilidade para Trocar ou Evoluir o Banco de Dados:** O Repository Pattern abstrai os detalhes do armazenamento de dados, permitindo que o ReservationService e outros serviços dependam apenas da interface do repositório, sem conhecer detalhes de implementação do banco de dados. Isso facilita mudanças no sistema de armazenamento, como trocar o banco relacional por um banco NoSQL (nesse projeto foi utilizado o MySQL) ou até mesmo modificar o tipo de persistência (por exemplo, de um banco de dados local para um serviço de armazenamento em nuvem). Essa flexibilidade é útil para evoluir a arquitetura do projeto conforme as necessidades.

### 2. Padrão de Projeto: Singleton
**1. Redução no Consumo de Recursos:** Criar um serviço como singleton garante que apenas uma instância desse serviço será criada durante toda a vida útil da aplicação. Isso evita a criação desnecessária de múltiplas instâncias, reduzindo o uso de memória e melhorando a eficiência geral do sistema. Como resultado, as operações de manipulação de dados (como no ReservationService) podem ser realizadas de maneira mais econômica, especialmente em serviços que são usados com frequência.

**2. Facilidade de Manutenção e Consistência de Estado:** Um singleton para o ReservationService e outros serviços evita inconsistências de estado, pois todas as chamadas para o serviço compartilham a mesma instância. Isso pode ser útil para gerenciar o estado compartilhado (por exemplo, configurações, conexão com o banco de dados ou cache de dados). Com uma única instância, é mais fácil aplicar atualizações e garantir que todas as partes da aplicação utilizem a versão mais recente e atualizada do serviço.

### 3. Padrão de Projeto: Builder
**1. Facilidade na Criação de Objetos Complexos:** O Builder Pattern é ideal para criar objetos com muitos parâmetros opcionais ou com configurações complexas, como o Reservation. Em vez de sobrecarregar os construtores com várias combinações de argumentos, o padrão permite criar objetos de forma clara e fluente. Cada campo pode ser definido separadamente, evitando a criação de construtores longos e melhorando a legibilidade do código. Isso é especialmente útil se a classe Reservation ou outras classes do projeto crescerem em complexidade.

**2. Imutabilidade e Segurança no Código:** O Builder Pattern pode ajudar a tornar os objetos imutáveis após a criação, promovendo um design mais seguro e estável. Em vez de definir todos os valores de uma só vez, o builder permite que todos os valores sejam configurados com precisão antes de chamar o método build(), que cria o objeto final imutável. Objetos imutáveis são menos propensos a erros e são mais fáceis de depurar.

## Padrão de Projeto: Repository
O Repository Pattern é um padrão de design que visa abstrair a lógica de acesso a dados, permitindo que a aplicação interaja com diferentes fontes de dados (como bancos de dados, arquivos, APIs, etc.) sem se preocupar com os detalhes de implementação. Isso promove uma separação clara entre a lógica de negócios e a lógica de persistência, facilitando a manutenção e os testes do código.

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

E assim fica a implementação de ReservationRepositoryImpl, que realiza as operações no banco de dados usando a biblioteca Exposed e o objeto Reservations que representa a tabela no banco.

```kotlin
package com.condocare.repositories

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
```

Serviço de Reservas (ReservationService.kt): Agora, a camada de serviço é responsável por gerenciar apenas a lógica de negócios.
Aqui, o ReservationService recebe uma instância de ReservationRepository e delega as operações para ele:

![image](https://github.com/user-attachments/assets/c5af7ad8-7e68-409f-a321-c7eb412aaa94)

###### Figura 3. Repository Service.

## Padrão de Projeto: Singleton
O Singleton é um padrão de projeto criacional que permite a você garantir que uma classe tenha apenas uma instância, enquanto provê um ponto de acesso global para essa instância.

Vamos transformar os arquivos de "service" em singleton para que possam ser instanciadas apenas uma vez:

![image](https://github.com/user-attachments/assets/6b499ce9-e0ff-4b8c-87e9-b65895426d75)
###### Figura 4. Singleton em RepositoryService.kt.

Chamando na Application.kt:

![image](https://github.com/user-attachments/assets/602ee308-571e-47a6-803b-eb468010c5a0)
###### Figura 5. Instanciação de ReservationService com Singleton e configuração da rota.

## Padrão de Projeto: Builder

O Builder é um padrão de projeto criacional que permite a você construir objetos complexos passo a passo. 

O padrão permite que você produza diferentes tipos e representações de um objeto usando o mesmo código de construção.

Vamos modificar nossas classes com o padrão Builder, abaixo o exemplo da "Reservation":

![image](https://github.com/user-attachments/assets/7e1f8ee9-e5de-46d8-b301-c539313e92e0)

###### Figura 6. Reservation como um objeto com a Classe Builder e métodos para definição de cada atributo.

A aplicação dos padrões trouxe maior modularidade, segurança e facilidade de manutenção ao projeto CondoCare. O Singleton garantiu uma única instância dos serviços, economizando recursos e aumentando o controle sobre dependências compartilhadas. O Builder proporcionou uma forma flexível e clara de construir objetos complexos como Reservation, garantindo consistência e segurança contra modificações inesperadas. Já o Repository separou a lógica de acesso ao banco de dados da lógica de negócios, tornando o código mais modular e testável, além de facilitar futuras mudanças de tecnologia de persistência sem impacto nos serviços e controladores. Esses padrões aprimoram o design do projeto, preparando-o para evoluir de forma escalável e sustentável.

##  Referências:
[Padrão Repository](https://dev.to/diariodeumacdf/padroes-dao-e-repository-13nj);[Padrão Singleton](https://refactoring.guru/pt-br/design-patterns/singleton);[Padrão Builder](https://refactoring.guru/pt-br/design-patterns/builder)
