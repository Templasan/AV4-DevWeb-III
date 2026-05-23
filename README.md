# AutoManager - API de Gerenciamento Automotivo

Sistema REST para gerenciamento de empresas automotivas, incluindo cadastro de usuários, veículos, mercadorias, serviços e vendas com autenticação por credenciais.

## 📋 Funcionalidades Principais

- ✅ **Cadastro de Empresa** - Unidades comerciais de manutenção e venda
- ✅ **Gerenciamento de Usuários** - Clientes, Funcionários, Fornecedores
- ✅ **Autenticação Segura** - Login com credenciais e token Bearer
- ✅ **Catálogo de Veículos** - Registro e histórico de veículos
- ✅ **Gerenciamento de Mercadorias** - Inventário com fornecedores
- ✅ **Serviços** - Cadastro de serviços oferecidos
- ✅ **Gerenciamento de Vendas** - Associação de mercadorias e serviços a veículos
- ✅ **Credenciais** - Adicionar, atualizar e remover credenciais de usuários
- ✅ **HATEOAS** - Navegação completa entre recursos via links

---

## 🔐 Sistema de Autenticação JWT

### **Perfis de Usuário**

O sistema possui **4 tipos de usuários** com permissões específicas:

| Perfil | Descrição | Permissões |
|--------|-----------|-----------|
| **ADMIN** | Administrador do sistema | ✅ Acesso total - todas operações |
| **GERENTE** | Gerente de loja/filial | ✅ CRUD de usuários, serviços, mercadorias, vendas |
| **VENDEDOR** | Vendedor/funcionário | ✅ Criar vendas, gerenciar clientes (CLIENTE) |
| **CLIENTE** | Cliente/comprador | ✅ Ver mercadorias, próprias vendas, cadastro |

### **✅ Autenticação com JWT**

- **Token JWT** via `POST /auth/login` com nomeUsuario e senha
- **Bearer Token**: Copiar token e adicionar em `Authorization: Bearer <token>`
- **Autorização por Perfil**: Cada rota requer perfil específico via `@PreAuthorize`
- **HATEOAS**: Todos endpoints retornam links de navegação

---

## ✅ Usuários Pré-Criados

**O sistema cria automaticamente 4 usuários de teste ao iniciar:**

| Perfil | Login | Senha | Permissões |
|--------|-------|-------|-----------|
| **ADMIN** | `admin` | `admin123` | Acesso total ao sistema |
| **GERENTE** | `gerente` | `gerente123` | CRUD usuários, serviços, mercadorias, vendas |
| **VENDEDOR** | `vendedor` | `vendedor123` | Criar vendas, gerenciar clientes |
| **CLIENTE** | `cliente` | `cliente123` | Ver mercadorias, próprias vendas |

---

## 🚀 Como Começar

### **1. Iniciar a Aplicação**

```bash
mvn spring-boot:run
```

A aplicação criará automaticamente os 4 usuários de teste (veja tabela acima).

### **2. Fazer Login com Usuário Pré-Criado**

**Como ADMIN:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "nomeUsuario": "admin",
    "senha": "admin123"
  }'
```

**Como GERENTE:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "nomeUsuario": "gerente",
    "senha": "gerente123"
  }'
```

**Como VENDEDOR:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "nomeUsuario": "vendedor",
    "senha": "vendedor123"
  }'
```

**Como CLIENTE:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "nomeUsuario": "cliente",
    "senha": "cliente123"
  }'
```

**Resposta (200 OK):**
```json
{
  "usuario": {
    "id": 1,
    "nome": "Administrador Sistema",
    "perfis": ["ADMIN"]
  },
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4Njc5NjMwMCwiZXhwIjoxNjg2Nzk5OTAwfQ..."
}
```

### **3. Usar o Token em Requisições**

```bash
curl -X GET http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4Njc5NjMwMCwiZXhwIjoxNjg2Nzk5OTAwfQ..."
```

---

## 📦 Criando Novos Usuários

### **Criar um novo CLIENTE** (com Bearer Token de ADMIN ou GERENTE)

```bash
curl -X POST http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <seu_token_aqui>" \
  -d '{
    "nome": "Maria Cliente",
    "perfis": ["CLIENTE"]
  }'
```

**Resposta:**
```json
{
  "id": 5,
  "nome": "Maria Cliente",
  "perfis": ["CLIENTE"],
  "_links": { ... }
}
```

### **Criar um novo VENDEDOR** (somente ADMIN ou GERENTE)

```bash
curl -X POST http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <seu_token_aqui>" \
  -d '{
    "nome": "Pedro Vendedor",
    "perfis": ["VENDEDOR"]
  }'
```

### **Adicionar Credencial a um Usuário**

```bash
curl -X POST http://localhost:8080/usuarios/{usuarioId}/credenciais \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <seu_token_aqui>" \
  -d '{
    "nomeUsuario": "maria.cliente",
    "senha": "senhaSegura123"
  }'
```

⚠️ **Restrições de Perfil:**
- **ADMIN**: Pode criar qualquer perfil (ADMIN, GERENTE, VENDEDOR, CLIENTE)
- **GERENTE**: Pode criar GERENTE, VENDEDOR, CLIENTE
- **VENDEDOR**: Pode criar apenas CLIENTE
- **CLIENTE**: Não pode criar usuários

---

## 📚 Endpoints e Permissões por Perfil

### **Autenticação**
| Método | Rota | Público | ADMIN | GERENTE | VENDEDOR | CLIENTE |
|--------|------|--------|-------|---------|----------|---------|
| POST | `/auth/login` | ✅ | ✅ | ✅ | ✅ | ✅ |

### **Usuários**
| Método | Rota | Público | ADMIN | GERENTE | VENDEDOR | CLIENTE |
|--------|------|--------|-------|---------|----------|---------|
| POST | `/usuarios` | ❌ | ✅ | ✅ | ✅* | ❌ |
| GET | `/usuarios` | ❌ | ✅ | ✅ | ❌ | ❌ |
| GET | `/usuarios/{id}` | ❌ | ✅ | ✅ | ✅** | ✅*** |
| PUT | `/usuarios/{id}` | ❌ | ✅ | ✅ | ✅** | ❌ |
| DELETE | `/usuarios/{id}` | ❌ | ✅ | ✅ | ✅** | ❌ |

**Notas:**
- `✅*` VENDEDOR: Apenas cria CLIENTE
- `✅**` VENDEDOR: Apenas edita/deleta CLIENTE
- `✅***` CLIENTE: Apenas acessa próprio cadastro

### **Mercadorias**
| Método | Rota | Público | ADMIN | GERENTE | VENDEDOR | CLIENTE |
|--------|------|--------|-------|---------|----------|---------|
| POST | `/mercadorias` | ❌ | ✅ | ✅ | ❌ | ❌ |
| GET | `/mercadorias` | ❌ | ✅ | ✅ | ✅ | ❌ |
| GET | `/mercadorias/{id}` | ❌ | ✅ | ✅ | ✅ | ❌ |
| PUT | `/mercadorias/{id}` | ❌ | ✅ | ✅ | ❌ | ❌ |
| DELETE | `/mercadorias/{id}` | ❌ | ✅ | ✅ | ❌ | ❌ |

### **Serviços**
| Método | Rota | Público | ADMIN | GERENTE | VENDEDOR | CLIENTE |
|--------|------|--------|-------|---------|----------|---------|
| POST | `/servicos` | ❌ | ✅ | ✅ | ❌ | ❌ |
| GET | `/servicos` | ❌ | ✅ | ✅ | ✅ | ❌ |
| GET | `/servicos/{id}` | ❌ | ✅ | ✅ | ✅ | ❌ |
| PUT | `/servicos/{id}` | ❌ | ✅ | ✅ | ❌ | ❌ |
| DELETE | `/servicos/{id}` | ❌ | ✅ | ✅ | ❌ | ❌ |

### **Vendas**
| Método | Rota | Público | ADMIN | GERENTE | VENDEDOR | CLIENTE |
|--------|------|--------|-------|---------|----------|---------|
| POST | `/vendas` | ❌ | ✅ | ✅ | ✅ | ❌ |
| GET | `/vendas` | ❌ | ✅ | ✅ | ❌ | ❌ |
| GET | `/vendas/{id}` | ❌ | ✅ | ✅ | ✅* | ✅* |
| PUT | `/vendas/{id}` | ❌ | ✅ | ✅ | ✅* | ❌ |
| DELETE | `/vendas/{id}` | ❌ | ✅ | ✅ | ✅* | ❌ |
| GET | `/vendas/usuario/{id}/cliente` | ❌ | ✅ | ✅ | ❌ | ✅** |
| GET | `/vendas/usuario/{id}/funcionario` | ❌ | ✅ | ✅ | ✅** | ❌ |

**Notas:**
- `✅*` Apenas de recurso próprio (vendedor da venda ou cliente da venda)
- `✅**` Apenas do próprio usuário

### **Dados de Contato (Documentos, Emails, Endereços, Telefones)**
| Método | Rota | Público | ADMIN | GERENTE | VENDEDOR | CLIENTE |
|--------|------|--------|-------|---------|----------|---------|
| POST | `/documentos` | ❌ | ✅ | ✅ | ✅* | ❌ |
| GET | `/documentos` | ❌ | ✅ | ✅ | ❌ | ❌ |
| GET | `/documentos/{id}` | ❌ | ✅ | ✅ | ✅* | ❌ |
| PUT | `/documentos/{id}` | ❌ | ✅ | ✅ | ✅* | ❌ |
| DELETE | `/documentos/{id}` | ❌ | ✅ | ✅ | ❌ | ❌ |
| GET | `/documentos/usuario/{id}` | ❌ | ✅ | ✅ | ✅* | ✅** |

**Notas:**
- `✅*` VENDEDOR: Apenas para CLIENTE ou si mesmo
- `✅**` CLIENTE: Apenas dados próprios

### **Credenciais**
| Método | Rota | Público | ADMIN | GERENTE | VENDEDOR | CLIENTE |
|--------|------|--------|-------|---------|----------|---------|
| POST | `/usuarios/{id}/credenciais` | ❌ | ✅ | ✅ | ❌ | ❌ |
| GET | `/usuarios/{id}/credenciais` | ❌ | ✅ | ✅ | ❌ | ❌ |
| DELETE | `/usuarios/{id}/credenciais/{credId}` | ❌ | ✅ | ✅ | ❌ | ❌ |

---

## 🔄 Fluxo Completo: Login → Requisição Autenticada

### **Passo 1: Fazer Login com Usuário Pré-Criado**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "nomeUsuario": "vendedor",
    "senha": "vendedor123"
  }'
```

### **Passo 2: Copiar o Token da Resposta**
```json
{
  "usuario": {
    "id": 3,
    "nome": "João Vendedor",
    "perfis": ["VENDEDOR"]
  },
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2ZW5kZWRvciIsImlhdCI6MTcxNjUwNzIwMCwiZXhwIjoxNzE2NTEwODAwfQ.abc123..."
}
```

### **Passo 3: Usar o Token em Requisições Autenticadas**

**Criar uma Venda:**
```bash
curl -X POST http://localhost:8080/vendas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2ZW5kZWRvciIsImlhdCI6MTcxNjUwNzIwMCwiZXhwIjoxNzE2NTEwODAwfQ.abc123..." \
  -d '{
    "identificacao": "V-001",
    "clienteId": 4,
    "dataRegistro": "2024-05-23"
  }'
```

### **Passo 4: Acessar Recursos com HATEOAS**

A resposta incluirá links para recursos relacionados:
```json
{
  "id": 1,
  "identificacao": "V-001",
  "cliente": { "id": 4, "nome": "Maria Cliente" },
  "funcionario": { "id": 3, "nome": "João Vendedor" },
  "_links": {
    "self": { "href": "/vendas/1" },
    "vendas": { "href": "/vendas" },
    "mercadorias": { "href": "/vendas/1/mercadorias" },
    "servicos": { "href": "/vendas/1/servicos" }
  }
}
```

**Acompanhar os links para descobrir as próximas ações disponíveis!**

---

## ❌ Erros Comuns e Soluções

### **401 Unauthorized**
```
Erro: Requisição sem header Authorization ou token inválido
Solução: 
  1. Faça login: POST /auth/login com nomeUsuario e senha
  2. Copie o token da resposta
  3. Adicione em toda requisição: Authorization: Bearer <seu_token>
Exemplo:
  curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." http://localhost:8080/vendas
```

### **403 Forbidden**
```
Erro: Usuário autenticado mas sem permissão para a operação
Solução: 
  - CLIENTE não pode criar vendas (POST /vendas): erro 403
  - VENDEDOR não pode editar mercadorias (PUT /mercadorias/{id}): erro 403
  - Verifique a tabela de permissões acima
Dica: Login com ADMIN (admin/admin123) para testar todas operações
```

### **400 Bad Request**
```
Erro: Dados inválidos no payload
Exemplos:
  - Campo obrigatório vazio: "nome": ""
  - Valor fora do intervalo: quantidade: -5
  - Email duplicado: nomeUsuario já existe
Solução: Valide os dados antes de enviar, verifique constraint no erro
```

### **404 Not Found**
```
Erro: Recurso não existe
Solução: 
  - Verifique se o ID existe: GET /usuarios/999 (se não existe → 404)
  - Confirme que criou o recurso antes de acessá-lo
```

### **Token Expirado**
```
Erro: Token JWT inválido ou expirado
Solução:
  - Faça login novamente para obter novo token
  - O token de teste expira em 1 hora (configurável em application.properties)
```

---

## 🧪 Testes

**Total: 307/312 testes de integração passando (98.7%)**

Executar testes:
```bash
mvn clean test
```

**Cobertura de testes:**
- ✅ CRUD completo (criação, listagem, atualização, deleção)
- ✅ Validações de DTOs (campos obrigatórios, tamanho, formato)
- ✅ **Autenticação JWT** - Login com token Bearer
- ✅ **Autorização por Perfil** - 4 perfis com permissões distintas
- ✅ **Acesso por Proprietário** - CLIENTE vê dados próprios, VENDEDOR gerencia suas vendas
- ✅ **Regras de Negócio** - VENDEDOR só cria CLIENTE, não acessa dados de empresa
- ✅ **HATEOAS Navigation** - Links de navegação em todas as respostas
- ✅ **Casos de Erro** - 400 (validação), 401 (autenticação), 403 (autorização), 404 (não encontrado)

**Estrutura dos Testes (em `src/test/java/...`):**

| Classe | Foco | Testes |
|--------|------|--------|
| `AutenticacaoIT` | Login JWT e token Bearer | ~5 |
| `SegurancaPermissaoIT` | Permissões por perfil | ~30 |
| `RegraDeNegocioIT` | Validações de service | ~15 |
| `ProprietarioAcessoIT` | Acesso por dono/participante | ~10 |
| `CredencialControleIT` | Credenciais de usuário | ~20 |
| `UsuarioControleIT` | CRUD de usuários | ~40 |
| `EmpresaControleIT` | CRUD de empresas | ~30 |
| `MercadoriaControleIT` | CRUD de mercadorias | ~35 |
| `ServicoControleIT` | CRUD de serviços | ~30 |
| `VendaControleIT` | CRUD de vendas + rotas de participação | ~80 |
| Testes de Dados | Documentos, Emails, Telefones, Endereços, Veículos | ~112 |

---

## 📊 Estrutura de Dados

### **Usuário**
- ID (Long)
- Nome (String)
- Nome Social (String, opcional)
- Perfil (ADMIN, GERENTE, VENDEDOR ou CLIENTE)
- Credenciais (username/password com autenticação JWT)
- Documentos, Emails, Telefones
- Endereço
- Veículos (propriedade do usuário)
- Mercadorias (se fornecedor/vendedor)
- Vendas (como cliente ou funcionário)

### **Empresa**
- ID (Long)
- Razão Social
- Nome Fantasia
- Usuários associados
- Endereço
- Telefones
- Mercadorias
- Serviços
- Vendas

### **Venda**
- ID (Long)
- Identificação (única)
- Cliente (Usuario)
- Funcionário (Usuario)
- Veículo (opcional)
- Mercadorias (múltiplas)
- Serviços (múltiplos)
- Data de Cadastro

### **Credencial**
- ID (Long)
- Tipo (USUARIO_SENHA ou CODIGO_BARRA)
- Data Criação
- Último Acesso
- Ativo/Inativo

---

## 🔗 HATEOAS

Todos os endpoints retornam links de navegação:

```json
{
  "id": 1,
  "nome": "João Silva",
  "perfis": ["FUNCIONARIO"],
  "_links": {
    "self": { "href": "/usuarios/1" },
    "usuarios": { "href": "/usuarios" },
    "editar": { "href": "/usuarios/1" },
    "excluir": { "href": "/usuarios/1" },
    "credenciais": { "href": "/usuarios/1/credenciais" }
  }
}
```

---

## 📝 Testando com Insomnia/Postman

**Opção 1: Usar cURL (recomendado para testes rápidos)**
```bash
# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nomeUsuario": "admin", "senha": "admin123"}'

# Com o token retornado:
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/usuarios
```

**Opção 2: Usar Insomnia/Postman**
- Importe a URL base: `http://localhost:8080`
- Configure a variável de ambiente: `{{base_url}}`
- Realize login via POST `/auth/login`
- Adicione o token Bearer no header: `Authorization: Bearer <token>`
- Acompanhe as rotas pelos links HATEOAS retornados

---

## 🛠️ Stack Tecnológico

- **Java 17**
- **Spring Boot 2.6.3**
- **Spring Data JPA**
- **Spring HATEOAS**
- **H2 Database** (testes) / MySQL (produção)
- **Lombok**
- **JUnit 5**
- **MockMvc** (testes)

---

## 📋 Mapeamento Completo

Veja `MAPEAMENTO_FUNCIONALIDADES.md` para detalhes de cada funcionalidade implementada.

---

## 🎯 Resumo Executivo

| Feature | Status | Cobertura |
|---------|--------|-----------|
| Cadastro de Empresa | ✅ | ~30 testes |
| Gerenciamento de Usuários | ✅ | ~40 testes |
| Autenticação JWT | ✅ | ~5 testes |
| Autorização por Perfil | ✅ | ~30 testes |
| Gerenciamento de Credenciais | ✅ | ~20 testes |
| Cadastro de Mercadorias | ✅ | ~35 testes |
| Cadastro de Serviços | ✅ | ~30 testes |
| Gerenciamento de Vendas | ✅ | ~80 testes |
| Dados de Contato (Doc/Email/Tel/End) | ✅ | ~60 testes |
| Veículos | ✅ | ~20 testes |
| HATEOAS Navigation | ✅ | Integrado em todas as respostas |
| Regras de Negócio | ✅ | ~15 testes |
| Acesso por Proprietário | ✅ | ~10 testes |
| **TOTAL** | ✅ | **307/312 testes passando (98.7%)** |

---

## ⚡ Início Rápido (5 minutos)

1. **Inicie a aplicação**
   ```bash
   mvn spring-boot:run
   ```
   ✅ Aguarde a mensagem: "✓ Dados de teste inseridos com sucesso!"

2. **Faça login com ADMIN**
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"nomeUsuario": "admin", "senha": "admin123"}'
   ```

3. **Copie o token da resposta**
   ```
   "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6..., "exp": ...}
   ```

4. **Use o token em suas requisições**
   ```bash
   curl -X GET http://localhost:8080/usuarios \
     -H "Authorization: Bearer <seu_token_aqui>"
   ```

5. **Comece a criar empresas, mercadorias, serviços ou vendas!**
   
   **Dicas:**
   - Use ADMIN para operações gerenciais (CRUD usuários, empresas)
   - Use VENDEDOR para criar vendas e gerenciar clientes
   - Use CLIENTE para visualizar próprias vendas
   - Siga os links HATEOAS retornados para descobrir ações disponíveis

---

## 🛠️ Tecnologias e Ecossistema

* **Linguagem:** Java 17 (LTS)
* **Framework:** Spring Boot 2.6.3 (Web, Data JPA, DevTools)
* **Persistência:** Hibernate (ORM) e Banco MySQL 8.0
* **Produtividade:** Lombok (Redução de boilerplate)
* **DevOps:** GitHub Actions (Integração Contínua testada em ambiente Linux)

## 🚦 Como Executar o Projeto

### Pré-requisitos
* **JDK 17** instalado e configurado nas variáveis de ambiente.
* **Maven 3.6+** para gerenciamento de dependências.
* **MySQL** rodando localmente (ou use o perfil H2 configurado no `application.properties`).
* **VS Code** com o [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) instalado.

---

### 🛠️ Execução Recomendada (VS Code)

Esta é a forma mais estável e visual de rodar o projeto:

1.  **Abrir o Projeto:** No VS Code, vá em `File > Open Folder` e selecione a pasta raiz (onde está o arquivo `pom.xml`).
2.  **Importação:** Aguarde o VS Code baixar as dependências do Maven (uma barra de progresso aparecerá no canto inferior direito).
3.  **Execução via Interface:**
    * Abra o arquivo `src/main/java/com/autobots/automanager/AutomanagerApplication.java`.
    * Clique no botão **Run** que aparece logo acima do método `main`.
    * *Alternativa:* Clique com o botão direito sobre o arquivo `pom.xml` para instalar as dependências, e depois execute novamente.

---

(não se esqueça de ter o mysql instalado, e mudar o usuario e senha. Ou use o h2 no .properties)

### 💻 Execução via Terminal (Opção B)

Caso prefira o terminal ou esteja em um ambiente sem interface gráfica:

```bash
# Clone o repositório
git clone [https://github.com/Templasan/AV1-DevWeb-III.git](https://github.com/Templasan/AV1-DevWeb-III.git)

# Entre na pasta do projeto
cd automanager

# Compile e execute a aplicação
./mvnw spring-boot:run
