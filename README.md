# Clínica Essência Viva

## Sistema de gerenciamento para clínica estética

Projeto acadêmico desenvolvido para apoiar a rotina de atendimento da clínica estética **Essência Viva**. A aplicação reúne o frontend e o backend em uma solução integrada para organizar clientes, profissionais, procedimentos e agendamentos.

O sistema foi pensado para representar uma situação real de uso: a recepção precisa consultar informações com rapidez, os profissionais precisam ter seus horários organizados e os dados dos clientes devem ser tratados com cuidado. Por isso, além das funcionalidades de cadastro e consulta, o projeto também considera autenticação, controle de acesso, integridade dos dados e regras específicas para a agenda.

A aplicação está concluída e funcionando com o frontend integrado ao backend.

---

## Objetivo

O objetivo do projeto é desenvolver uma aplicação web para auxiliar no gerenciamento de uma clínica estética, centralizando as principais informações da operação em um único sistema.

A solução permite controlar:

- clientes;

- profissionais;

- procedimentos oferecidos pela clínica;

- horários de atendimento;

- situação dos agendamentos;

- usuários responsáveis pelo acesso ao sistema;

- valores dos procedimentos no momento do agendamento.

A proposta também busca reduzir erros comuns de operação, como agendar dois atendimentos para o mesmo profissional no mesmo horário ou permitir que o frontend altere valores que deveriam ser definidos pelo servidor.

---

## Funcionalidades principais

### Autenticação e acesso

- Login de usuários do sistema;

- controle de sessão por cookie;

- expiração da sessão por inatividade;

- logout com invalidação da sessão no servidor;

- perfis de acesso para administrador e recepcionista;

- bloqueio temporário após tentativas consecutivas de login inválidas.

### Clientes

- Cadastro e atualização de clientes;

- consulta de clientes;

- exibição de informações mascaradas nas listagens;

- proteção de dados pessoais e informações sensíveis;

- verificação de CPF duplicado por meio de hash seguro.

### Profissionais

- Cadastro de profissionais;

- registro profissional;

- especialidade;

- ativação e desativação de profissionais;

- associação dos profissionais aos agendamentos.

### Procedimentos

- Cadastro de procedimentos;

- descrição e duração;

- valor atual do procedimento;

- ativação e desativação de procedimentos;

- preservação do preço utilizado em agendamentos anteriores.

### Agendamentos

- Criação, consulta, atualização e cancelamento de agendamentos;

- associação entre cliente, profissional e procedimentos;

- cálculo automático da duração e do valor total;

- prevenção de conflitos de horário;

- controle da situação do atendimento;

- armazenamento do preço praticado no momento da criação do agendamento.

---

## Tecnologias utilizadas

### Backend

- Java 21;

- Spring Boot 3.4.5;

- Spring Web;

- Spring Data JPA;

- Hibernate;

- Spring Security;

- Bean Validation;

- Maven;

- Flyway;

- MySQL Connector/J;

- Springdoc OpenAPI.

### Banco de dados

- MySQL 8;

- migrations versionadas com Flyway;

- relacionamentos por chaves estrangeiras;

- índices para consultas frequentes;

- uso de transações para operações de negócio.

### Segurança

- AES-256-GCM para cifragem de dados sensíveis;

- HMAC-SHA256 para o CPF normalizado;

- armazenamento de senha com algoritmo apropriado de hash;

- token de sessão armazenado somente em formato hash no banco;

- cookies HttpOnly;

- controle de permissões no backend;

- respostas de erro sem exposição de informações internas.

### Frontend

O frontend consome a API REST do backend e é responsável pela interface de interação com os usuários. A comunicação ocorre por requisições HTTP usando JSON, respeitando os endpoints, os códigos de resposta e o formato de dados definidos pelo backend.

---

## Arquitetura da aplicação

A aplicação segue uma separação por responsabilidades:

```
Frontend
   ↓
API REST
   ↓
Controllers
   ↓
Services
   ↓
Repositories
   ↓
MySQL
```

### Controllers

Recebem as requisições HTTP, validam a entrada básica e devolvem as respostas da API. Os controllers não concentram as regras principais do sistema.

### Services

Contêm as regras de negócio, como cálculo de valores, verificação de conflitos de horário, controle de sessão e aplicação das permissões.

### Repositories

Responsáveis pelo acesso aos dados persistidos no banco por meio do Spring Data JPA.

### Migrations

As alterações estruturais do banco são controladas pelo Flyway. Isso permite reconstruir o banco de forma organizada e manter o histórico das alterações.

---

## Estrutura principal do backend

```
src
├── main
│   ├── java
│   │   └── br/com/essenciaviva/clinic
│   │       ├── ClinicEssenciaApplication.java
│   │       ├── controller
│   │       ├── config
│   │       ├── security
│   │       ├── crypto
│   │       ├── usuario
│   │       ├── cliente
│   │       ├── profissional
│   │       ├── procedimento
│   │       └── agendamento
│   └── resources
│       ├── application.yml
│       └── db
│           └── migration
│               └── V1__create_tables.sql
└── test
```

---

## Modelo de dados

As principais tabelas do banco são:

| Tabela | Finalidade |
| --- | --- |
| `usuario` | Usuários que acessam o sistema |
| `sessao` | Sessões ativas e seus prazos de validade |
| `cliente` | Dados dos clientes da clínica |
| `profissional` | Profissionais que realizam os atendimentos |
| `procedimento` | Catálogo de serviços e seus valores |
| `agendamento` | Horários e informações dos atendimentos |
| `agendamento_procedimento` | Procedimentos associados a cada agendamento |
| `profissional_agenda_lock` | Controle de concorrência na agenda |

Os relacionamentos principais são:

- um cliente pode possuir vários agendamentos;

- um profissional pode possuir vários agendamentos;

- um agendamento pode conter vários procedimentos;

- um procedimento pode aparecer em vários agendamentos;

- um usuário pode possuir várias sessões ao longo do tempo.

---

## Regras de negócio importantes

### Cálculo do agendamento

O valor total do atendimento não é recebido como fonte oficial pelo frontend. O backend busca os procedimentos no banco, copia seus valores atuais para os itens do agendamento e calcula o total no servidor.

Dessa forma, uma alteração posterior no preço de um procedimento não modifica o valor de um atendimento que já foi registrado.

### Conflito de horários

Antes de criar ou alterar um agendamento, o backend verifica se já existe outro atendimento ativo para o mesmo profissional no intervalo informado.

A operação é realizada dentro de uma transação com bloqueio apropriado para evitar que duas requisições simultâneas reservem o mesmo horário.

### Dados pessoais

Informações sensíveis não devem ser armazenadas em texto puro. O CPF também possui um identificador derivado por HMAC, utilizado para verificar unicidade sem precisar descriptografar todos os registros.

### Autorização

As permissões são determinadas pelo usuário autenticado no backend. O sistema não confia em um perfil ou autorização enviada pelo frontend.

---

## Principais endpoints

### Autenticação

```
POST /api/auth/login
POST /api/auth/renovar
POST /api/auth/logout
```

### Clientes

```
GET    /api/clientes
GET    /api/clientes/{id}
POST   /api/clientes
PUT    /api/clientes/{id}
DELETE /api/clientes/{id}
```

### Profissionais

```
GET    /api/profissionais
POST   /api/profissionais
PUT    /api/profissionais/{id}
DELETE /api/profissionais/{id}
```

### Procedimentos

```
GET    /api/procedimentos
POST   /api/procedimentos
PUT    /api/procedimentos/{id}
DELETE /api/procedimentos/{id}
```

### Agendamentos

```
GET    /api/agendamentos
POST   /api/agendamentos
PUT    /api/agendamentos/{id}
DELETE /api/agendamentos/{id}
```

---

## Respostas da API

A API utiliza códigos HTTP de acordo com o resultado da operação:

| Situação | Código |
| --- | --- |
| Operação realizada com sucesso | `200` |
| Registro criado | `201` |
| Operação concluída sem conteúdo | `204` |
| Usuário não autenticado ou sessão expirada | `401` |
| Usuário sem permissão | `403` |
| Recurso não encontrado | `404` |
| Dados inválidos | `422` |
| Conflito de CPF ou horário | `409` |

Os erros seguem um formato padronizado, com código, mensagem e, quando necessário, indicação dos campos inválidos.

Exemplo:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Existem campos inválidos",
  "fields": {
    "email": "Informe um e-mail válido"
  }
}
```

---

## Como executar o projeto

### Pré-requisitos

Antes de iniciar, é necessário ter instalado:

- JDK 21;

- IntelliJ IDEA;

- Maven, ou utilizar o Maven Wrapper;

- MySQL 8;

- ferramenta para acessar o banco, como MySQL Workbench;

- frontend configurado para utilizar a URL da API.

### Banco de dados

Crie o banco no MySQL:

```sql
CREATE DATABASE essencia_viva
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Configure as credenciais no arquivo `application.yml` ou por variáveis de ambiente, conforme a configuração adotada no ambiente de execução.

### Migration

A migration principal está localizada em:

```
src/main/resources/db/migration/V1__create_tables.sql
```

O Flyway executa a migration automaticamente na inicialização da aplicação.

### Executar pelo IntelliJ

1. Abra o projeto no IntelliJ IDEA.

1. Aguarde o Maven concluir o carregamento das dependências.

1. Verifique se o MySQL está em execução.

1. Confira o `application.yml`.

1. Execute a classe `ClinicEssenciaApplication`.

1. Aguarde a mensagem:

```
Started ClinicEssenciaApplication
```

A API ficará disponível na porta configurada, normalmente:

```
http://localhost:8080
```

O endpoint de verificação pode ser acessado em:

```
GET http://localhost:8080/api/health
```

---

## Integração com o frontend

O frontend utiliza a API REST para realizar as operações do sistema. Para a integração funcionar corretamente, é necessário conferir:

- URL base da API;

- porta utilizada pelo backend;

- configuração de CORS;

- envio de cookies nas requisições autenticadas;

- formato dos campos de data e hora;

- valores dos enums de situação e perfil;

- tratamento dos códigos HTTP retornados pela API.

Em desenvolvimento, a origem autorizada do frontend deve ser configurada explicitamente. Não é recomendado liberar qualquer origem com `*` quando a aplicação utiliza cookies ou credenciais.

---

## Testes e validações realizados

Durante o desenvolvimento foram verificadas as seguintes partes:

- inicialização do Spring Boot;

- conexão com o MySQL;

- criação e execução das migrations pelo Flyway;

- funcionamento do pool de conexões Hikari;

- integração entre backend e frontend;

- validação de dados recebidos pela API;

- controle de sessão e autenticação;

- regras de permissão;

- prevenção de conflitos na agenda;

- cálculo do total dos agendamentos;

- preservação dos preços históricos.

---

## Considerações acadêmicas

O projeto permitiu aplicar conceitos estudados em desenvolvimento de sistemas, incluindo programação orientada a objetos, arquitetura em camadas, desenvolvimento de APIs REST, persistência de dados relacionais, autenticação, autorização e integração entre frontend e backend.

A escolha de separar o sistema em camadas facilita a manutenção e permite que cada parte tenha uma responsabilidade bem definida. A utilização de migrations também torna a estrutura do banco mais organizada, enquanto as regras de negócio concentradas no backend ajudam a manter a consistência das informações.

Um dos principais aprendizados foi perceber que uma aplicação não deve depender apenas da interface para garantir a segurança e a validade dos dados. Mesmo que o frontend faça validações, o backend precisa repetir e reforçar essas regras, pois é ele que controla o acesso ao banco e representa a fonte oficial das informações.

---

## Status do projeto

**Concluído e em funcionamento.**

O sistema possui backend conectado ao MySQL e integrado ao frontend, com as funcionalidades principais da clínica implementadas.

---

## Autoria

Projeto acadêmico desenvolvido para a clínica estética **Essência Viva**.

Responsável pelo backend: **Maina**.

Responsável pelo frontend: **Mateus**.
