<h3 align="center">
  <img src=".github/assets/banner.png" alt="Trimly">
</h3>

<h4 align="center">
  <a href="https://github.com/jhotiori/trimly-backend/tree/main/" rel="noopener noreferrer">Source</a>
  ·
  <a href="https://github.com/jhotiori/trimly-backend/commits/main/" rel="noopener noreferrer">Commits</a>
  ·
  <a href="https://github.com/jhotiori/trimly-backend/pulls" rel="noopener noreferrer">PRs</a>
  ·
  <a href="https://github.com/jhotiori/trimly-backend/tree/main/LICENSE.md" rel="noopener noreferrer">License</a>
</h4>

<p align="center">
  <a href="https://github.com/jhotiori/trimly-backend/stargazers">
    <img src="https://img.shields.io/github/stars/jhotiori/trimly-backend?style=for-the-badge&labelColor=%231a1a1a&color=%23000000&logo=github&logoColor=ffffff" alt="GitHub Stars">
  </a>
  <a href="https://github.com/jhotiori/trimly-backend/network/members">
    <img src="https://img.shields.io/github/forks/jhotiori/trimly-backend?style=for-the-badge&labelColor=%231a1a1a&color=%23000000&logo=git&logoColor=ffffff" alt="GitHub Forks">
  </a>
  <a href="https://github.com/jhotiori/trimly-backend/issues">
    <img src="https://img.shields.io/github/issues/jhotiori/trimly-backend?style=for-the-badge&labelColor=%231a1a1a&color=%23000000&logo=github&logoColor=ffffff" alt="GitHub Issues">
  </a>
</p>

<blockquote>
    <em>
    Aplicação Backend do Trimly, API REST para gerenciamento de agendamentos em barbearias, construída com Spring Boot.
    </em>
    <br><br>
    <strong>
    Projeto acadêmico de uma aplicação voltada ao gerenciamento de agendamentos em barbearias, desenvolvida a partir de uma demanda real e focada na modernização do processo de contratação de serviços de cuidados masculinos.
    </strong>
</blockquote>

<hr>

## 🛠️ Técnologias
| Área                      | Tecnologias                                               |
| ------------------------- | ----------------------------------------------------------|
| **Linguagem & Framework** | Java (17) · Spring Boot (JPA, Security, Validation)        |
| **Banco de dados**        | PostgreSQL (produção) · H2 (desenvolvimento)               |
| **Migrações**             | Flyway                                                      |
| **Autenticação**          | JWT (auth0 java-jwt)                                        |
| **Build & Qualidade**     | Maven · Spotless (palantir-java-format)                     |
| **Infraestrutura**        | AWS Cloud                                                   |
| **Controle de versão**    | Git · GitHub                                                |

## 👥 Equipe
| Integrante             | Cargo           | Responsabilidade                |
| ---------------------- | --------------- | ------------------------------- |
| **João**               | Líder           | Frontend + Backend              |
| **Nicholas**           | Desenvolvedor   | Frontend                        |
| **Alexandre**          | Desenvolvedor   | Backend                         |
| **Evandro**            | Desenvolvedor   | Backend                         |

## 🧪 Desenvolvimento
Clone o repositório localmente e navegue até a raiz dele:
```bash
git clone https://github.com/jhotiori/trimly-backend
cd trimly-backend
```

Crie o arquivo `.env` a partir do exemplo e ajuste os valores (o `.env` não é versionado):
```bash
cp .env.example .env
```

| Variável                 | Uso                                                    | Padrão                  |
| ------------------------ | ------------------------------------------------------ | ----------------------- |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo                                           | `developing`            |
| `DB_URL`                 | URL do PostgreSQL (apenas `production`)                | sem padrão              |
| `DB_USER`                | Usuário do PostgreSQL (apenas `production`)            | sem padrão              |
| `DB_PASSWORD`            | Senha do PostgreSQL (apenas `production`)              | sem padrão              |
| `JWT_SECRET`             | Segredo HMAC dos tokens (obrigatório em `production`)  | segredo de desenvolvimento |
| `JWT_ISSUER`             | Emissor dos tokens                                     | `trimly-auth-api`       |
| `JWT_EXPIRY`             | Validade dos tokens, em segundos                       | `28800` (8 horas)       |
| `CORS_ORIGIN`            | Origem do frontend liberada no CORS                    | `http://localhost:4200` |

Perfis disponíveis:
- `developing`: H2 em memória, console em `/h2-console` e log de SQL.
- `debugging`: herda `developing` com logs mais detalhados.
- `production`: PostgreSQL; não inicia sem `JWT_SECRET`.
- `testing`: H2 em memória para os testes, sem serviços externos.

Variáveis do ambiente do processo têm prioridade sobre o `.env`.

Depois, rode o projeto usando o executavel do Maven `mvnw`

#### Linux/macOS
```bash
./mvnw spring-boot:run
```

#### Windows
```bash
mvnw.cmd spring-boot:run
```

#### Docker (PostgreSQL + API)
Com o `.env` preenchido, na raiz do projeto:
```bash
docker compose --env-file .env -f docker/compose.yaml up -d
```
