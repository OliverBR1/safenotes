# SafeNotes

API REST de notas pessoais com autenticação **JWT (RS256)**, **refresh tokens rotativos** e controle de acesso por **roles e scopes**, construída com Spring Boot e PostgreSQL.

O projeto serve como referência de um fluxo de autenticação/autorização completo e seguro: cada usuário só enxerga as próprias notas, tokens podem ser revogados e o reuso de um refresh token já utilizado derruba toda a sessão.

## Sumário

- [Stack](#stack)
- [Início rápido](#início-rápido)
- [Configuração](#configuração)
- [Usuários de exemplo](#usuários-de-exemplo)
- [Referência da API](#referência-da-api)
- [Exemplo de uso com curl](#exemplo-de-uso-com-curl)
- [Como a segurança funciona](#como-a-segurança-funciona)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Decisões de design e trade-offs](#decisões-de-design-e-trade-offs)
- [Limitações conhecidas](#limitações-conhecidas)
- [Testes](#testes)
- [Contribuindo](#contribuindo)

## Stack

| Tecnologia | Versão / Uso |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 (Web MVC, Data JPA, Security) |
| Spring Security OAuth2 Resource Server | validação e emissão de JWT (Nimbus) |
| PostgreSQL | 17 (via Docker Compose) |
| Maven | via Maven Wrapper (`mvnw`) |
| BCrypt | hash de senhas |
| commons-codec | SHA-256 dos refresh tokens |

## Início rápido

**Pré-requisitos:** JDK 21, Docker e OpenSSL (no Windows, o OpenSSL já vem no Git Bash).

### 1. Suba o banco de dados

```bash
docker compose -f docker/docker-compose.yml up -d
```

Isso cria um PostgreSQL 17 em `localhost:5432` com banco `safenotesdb`, usuário `safenotes` e senha `safenotes`.

### 2. Gere o par de chaves RSA

O `.gitignore` do projeto **ignora a chave privada** (`app.key`) de propósito, então cada ambiente precisa gerar a sua. Na raiz do projeto:

```bash
openssl genrsa -out keypair.pem 2048
openssl rsa -in keypair.pem -pubout -out src/main/resources/app.pub
openssl pkcs8 -topk8 -inform PEM -nocrypt -in keypair.pem -out src/main/resources/app.key
rm keypair.pem
```

> A chave privada precisa estar no formato **PKCS#8** (`-----BEGIN PRIVATE KEY-----`) e a pública no formato **X.509** (`-----BEGIN PUBLIC KEY-----`). Os comandos acima já geram assim.

### 3. Rode a aplicação

```bash
./mvnw spring-boot:run        # Linux / macOS / Git Bash
mvnw.cmd spring-boot:run      # Windows (cmd / PowerShell)
```

A API sobe em `http://localhost:8080`. Na primeira execução, o `RBACSeed` cria scopes, roles e usuários de exemplo automaticamente.

### 4. Teste

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bruno","password":"senha"}'
```

Se voltar um JSON com `accessToken` e `refreshToken`, está tudo certo.

## Configuração

Arquivo: `src/main/resources/application.properties`

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/safenotesdb` | URL do banco |
| `spring.datasource.username` / `password` | `safenotes` / `safenotes` | Credenciais do banco |
| `spring.jpa.hibernate.ddl-auto` | `update` | Cria/atualiza as tabelas automaticamente |
| `spring.jpa.show-sql` | `true` | Loga as queries SQL |
| `jwt.public.key` | `classpath:app.pub` | Chave pública (valida os tokens) |
| `jwt.private.key` | `classpath:app.key` | Chave privada (assina os tokens) |
| `jwt.issuer` | `safenotes` | Claim `iss` dos tokens |
| `jwt.expires-in` | `300` | Validade do access token em segundos (5 min) |
| `jwt.refresh-expires-in` | `2592000` | Validade do refresh token em segundos (30 dias) |

Qualquer propriedade pode ser sobrescrita por variável de ambiente, por exemplo `SPRING_DATASOURCE_PASSWORD`.

## Usuários de exemplo

Criados (ou atualizados) a cada inicialização pelo `RBACSeed`:

| Usuário | Senha | Role | Scopes |
|---|---|---|---|
| `bruno` | `senha` | `USER` | `note:read`, `note:write`, `profile:read` |
| `joao` | `senha` | `USER` | `note:read`, `note:write`, `profile:read` |
| `ana` | `senha` | `VIEWER` | `profile:read` |

> ⚠️ **Somente para desenvolvimento.** Senhas fracas e fixas no código não devem ir para produção.

## Referência da API

Todas as rotas, exceto `/auth/login` e `/auth/refresh`, exigem o header `Authorization: Bearer <accessToken>`.

| Método | Rota | Auth | Scope necessário | Descrição |
|---|---|---|---|---|
| `POST` | `/auth/login` | pública | — | Autentica e retorna access + refresh token |
| `POST` | `/auth/refresh` | pública | — | Troca um refresh token por um novo par de tokens |
| `POST` | `/auth/logout` | Bearer | — | Revoga um refresh token do usuário logado |
| `POST` | `/auth/logout-all` | Bearer | — | Invalida todas as sessões do usuário |
| `GET` | `/me` | Bearer | `profile:read` | Dados do usuário autenticado |
| `GET` | `/notes` | Bearer | `note:read` | Lista as notas do usuário (id e título) |
| `POST` | `/notes` | Bearer | `note:write` | Cria uma nota |
| `GET` | `/notes/{id}` | Bearer | `note:read` + dono da nota | Lê uma nota completa |
| `PUT` | `/notes/{id}` | Bearer | `note:write` + dono da nota | Atualiza uma nota |
| `DELETE` | `/notes/{id}` | Bearer | `note:write` + dono da nota | Remove uma nota |

### Corpos de requisição e resposta

**`POST /auth/login`**

```json
// request
{ "username": "bruno", "password": "senha" }

// response 200
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "expiresIn": 300,
  "refreshToken": "q3J0m...",
  "refreshExpiresIn": 2592000,
  "scopes": ["note:read", "note:write", "profile:read"]
}
```

**`POST /auth/refresh`** e **`POST /auth/logout`**

```json
{ "refreshToken": "q3J0m..." }
```

O `/auth/refresh` responde com o mesmo formato do login. `/auth/logout` e `/auth/logout-all` respondem `204 No Content`.

**`GET /me`**

```json
{
  "userId": 1,
  "username": "bruno",
  "tokenVersion": 1,
  "scopes": ["note:read", "note:write", "profile:read"],
  "roles": ["USER"]
}
```

**`POST /notes`** e **`PUT /notes/{id}`**

```json
{ "title": "Lista de compras", "content": "Café, pão e leite" }
```

`POST` responde `201 Created` com o header `Location: /notes/{id}` (sem corpo). `PUT` e `DELETE` respondem `204 No Content`.

**`GET /notes`**

```json
[ { "id": 1, "title": "Lista de compras" } ]
```

**`GET /notes/{id}`**

```json
{ "id": 1, "title": "Lista de compras", "content": "Café, pão e leite" }
```

## Exemplo de uso com curl

Fluxo completo: login, criar nota, listar, renovar sessão e sair.

```bash
BASE=http://localhost:8080

# 1. Login (guarda os dois tokens)
RESP=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bruno","password":"senha"}')

ACCESS=$(echo "$RESP"  | jq -r .accessToken)
REFRESH=$(echo "$RESP" | jq -r .refreshToken)

# 2. Criar uma nota
curl -i -X POST $BASE/notes \
  -H "Authorization: Bearer $ACCESS" \
  -H "Content-Type: application/json" \
  -d '{"title":"Primeira nota","content":"Olá, SafeNotes!"}'

# 3. Listar minhas notas
curl -s $BASE/notes -H "Authorization: Bearer $ACCESS"

# 4. Quando o access token expirar (5 min), renove a sessão
RESP=$(curl -s -X POST $BASE/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}")

ACCESS=$(echo "$RESP"  | jq -r .accessToken)
REFRESH=$(echo "$RESP" | jq -r .refreshToken)   # o refresh token antigo deixa de valer

# 5. Encerrar a sessão em todos os dispositivos
curl -i -X POST $BASE/auth/logout-all -H "Authorization: Bearer $ACCESS"
```

> No **Insomnia/Postman**, use a aba *Auth → Bearer Token* e cole o `accessToken`. O `jq` é opcional, serve só para extrair campos do JSON.

## Como a segurança funciona

### Dois tipos de token

| | Access token | Refresh token |
|---|---|---|
| Formato | JWT assinado com RSA (RS256) | String aleatória opaca (32 bytes, Base64 URL-safe) |
| Validade | 5 minutos | 30 dias |
| Armazenado no banco? | Não (stateless) | Sim, **apenas o hash SHA-256** |
| Usado para | Chamar a API | Obter novos tokens |

O access token carrega as claims `sub` (id do usuário), `iss`, `iat`, `exp`, `jti`, `username`, `family_id`, `version`, `roles` e `scope`.

### Rotação de refresh token e detecção de reuso

Cada login cria uma **família** de tokens (`family_id`). A cada `/auth/refresh`, o refresh token usado é revogado e um novo é emitido na mesma família. Se alguém apresentar um refresh token **já revogado**, o sistema entende que ele pode ter vazado e **revoga a família inteira**, forçando novo login.

### Versão do token (`logout-all`)

Cada usuário tem um `token_version`. O `TokenVersionFilter` compara a claim `version` do JWT com o valor atual no banco; se diferirem, responde `401` com `{"error":"invalid_token_version"}`. O `/auth/logout-all` incrementa essa versão e revoga todos os refresh tokens, o que invalida na hora **todos os access tokens já emitidos**, sem precisar esperar os 5 minutos.

### Autorização em duas camadas

1. **Scope**: `@PreAuthorize("hasAuthority('SCOPE_note:read')")` valida se o token tem a permissão.
2. **Propriedade do recurso**: `@noteAuthz.hasAccess(#id, #jwt)` valida se a nota pertence ao usuário do token (`existsByIdAndOwnerId`).

Assim, o usuário `joao` não consegue ler, alterar ou apagar notas do `bruno`, mesmo tendo o scope correto.

## Estrutura do projeto

```
src/main/java/tech/oliver/safenotes/
├── config/        SecurityConfig, JwtConfig, TokenVersionFilter, NoteAuthz, OpaqueToken, RBACSeed
├── controller/    AuthController, NoteController, ProfileController
│   └── dto/       Records de request/response
├── entity/        User, Role, Scope, Note, RefreshToken
├── repository/    Repositórios Spring Data JPA
└── service/       Login, Refresh, Logout, AccessToken, User e CRUD de notas
docker/
└── docker-compose.yml   PostgreSQL 17
```

Modelo de dados: `User` ↔ `Role` ↔ `Scope` (RBAC), `User` 1—N `Note` (via `owner_id`) e `User` 1—N `RefreshToken`.

## Decisões de design e trade-offs

| Decisão | Prós | Contras |
|---|---|---|
| **JWT stateless** para o access token | Validação rápida, sem consulta de sessão | Não dá para revogar um token individual antes de expirar |
| **Checagem de `token_version` por requisição** | Permite revogar todos os tokens de um usuário instantaneamente | Uma query extra ao banco em cada requisição autenticada |
| **Refresh token opaco com hash no banco** | Vazamento do banco não expõe tokens utilizáveis; revogação simples | Exige armazenamento e consulta ao banco no refresh |
| **Rotação + detecção de reuso por família** | Limita o dano de um refresh token roubado | Clientes com requisições concorrentes de refresh podem ser deslogados por engano |
| **Chaves RSA (assimétricas)** | Outros serviços podem validar tokens só com a chave pública | Gestão de chaves é mais trabalhosa que um segredo HMAC |
| **Scopes + ownership** | Permissões granulares e isolamento entre usuários | Duas camadas de regra para manter e testar |

## Limitações conhecidas

Pontos a resolver antes de qualquer uso além de estudo/desenvolvimento:

- **Tratamento de erros:** falhas como senha inválida, refresh token expirado ou nota inexistente lançam `RuntimeException` e resultam em `500`, em vez de `401`/`404` com mensagens claras. Falta um `@RestControllerAdvice`.
- **Access token no logout:** o `/auth/logout` revoga só o refresh token. O access token continua válido até expirar (há um comentário no código sugerindo uma blocklist). Para derrubar tudo de imediato, use `/auth/logout-all`.
- **Sem validação de entrada:** os DTOs não usam Bean Validation (`@NotBlank` etc.).
- **Seed em todo startup:** o `RBACSeed` redefine a senha e as roles de `bruno`, `joao` e `ana` a cada inicialização.
- **Credenciais no `application.properties`:** o ideal é usar variáveis de ambiente ou um gerenciador de segredos.
- **`ddl-auto=update`:** conveniente em desenvolvimento, mas em produção o recomendado é usar migrações (Flyway/Liquibase).
- **CSRF e CORS desabilitados:** aceitável para uma API stateless consumida por clientes não-browser; revise se houver front-end web.

## Testes

```bash
./mvnw test
```

Existe hoje apenas o teste `SafenotesApplicationTests.contextLoads`, que sobe o contexto Spring completo. Ele **exige o PostgreSQL rodando** (passo 1) e as chaves RSA geradas (passo 2).

## Contribuindo

1. Crie uma branch a partir da `main`: `git checkout -b feature/minha-mudanca`
2. Faça as alterações e garanta que `./mvnw test` passa.
3. **Nunca commite chaves privadas** (`app.key`, `*.pem`), elas já estão no `.gitignore`.
4. Abra um Pull Request descrevendo o que mudou e por quê.