## CondoCare
O CondoCare é um sistema desenvolvido para auxiliar na gestão de condomínios, facilitando a reserva de áreas comuns e o controle das entregas na portaria. 
Ele é composto por um front-end em Angular e um back-end em Kotlin (usando o framework Ktor), que é nosso foco de melhoria.

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
Exceções no acesso ao banco de dados podem fazer com que o código quebre ou retorne erros inesperados.



