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

## 🔐 Sistema de Autenticação

### **Perfis de Usuário**

O sistema possui **3 tipos de usuários**:

| Perfil | Descrição | POST/PUT/DELETE |
|--------|-----------|-----------------|
| **CLIENTE** | Compra serviços e mercadorias | ❌ Bloqueado |
| **FUNCIONARIO** | Trabalhador da empresa | ✅ Permitido |
| **FORNECEDOR** | Fornecedor de mercadorias | ❌ Bloqueado |

### **⚠️ Importante: Controle de Acesso**

- **POST, PUT, DELETE**: Requerem autenticação e perfil **FUNCIONÁRIO**
- **GET**: Públicos (sem autenticação necessária)
- **Cadastro de Usuário** (`POST /usuarios`): Público (sem autenticação)

---

## 🚀 Como Começar

### **1. Criar um Usuário FUNCIONÁRIO com Credencial**

```bash
curl -X POST http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Gerenciador",
    "perfis": ["FUNCIONARIO"],
    "credenciais": [{
      "tipo": "USUARIO_SENHA",
      "nomeUsuario": "joao.gerenciador",
      "senha": "senha123"
    }]
  }'
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "nome": "João Gerenciador",
  "perfis": ["FUNCIONARIO"]
}
```

### **2. Fazer Login**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "nomeUsuario": "joao.gerenciador",
    "senha": "senha123"
  }'
```

**Resposta (200 OK):**
```json
{
  "usuario": {
    "id": 1,
    "nome": "João Gerenciador",
    "perfis": ["FUNCIONARIO"]
  },
  "token": "am9hby5nZXJlbmNpYWRvcjozNjMyYjA2ZC05MzE5LTQ1NzItYWNmMS0xZWE3MmZkYjA1YzQ="
}
```

### **3. Usar o Token para Criar Recursos**

```bash
curl -X POST http://localhost:8080/empresas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer am9hby5nZXJlbmNpYWRvcjozNjMyYjA2ZC05MzE5LTQ1NzItYWNmMS0xZWE3MmZkYjA1YzQ=" \
  -d '{
    "razaoSocial": "AutoShop LTDA",
    "nomeFantasia": "AutoShop"
  }'
```

---

## 📦 Criação de Usuários

### **Opção 1: Com Credencial (Recomendado para Funcionários)**

```json
POST /usuarios
{
  "nome": "Maria Vendedora",
  "perfis": ["FUNCIONARIO"],
  "credenciais": [{
    "tipo": "USUARIO_SENHA",
    "nomeUsuario": "maria.vendedora",
    "senha": "senha456"
  }]
}
```

### **Opção 2: Sem Credencial Inicial**

```json
POST /usuarios
{
  "nome": "Cliente João",
  "perfis": ["CLIENTE"]
}
```

Depois adicionar credencial:
```json
POST /usuarios/{usuarioId}/credenciais
{
  "tipo": "USUARIO_SENHA",
  "nomeUsuario": "cliente.joao",
  "senha": "senha789"
}
```

### **Opção 3: Múltiplas Credenciais**

```json
POST /usuarios
{
  "nome": "Admin Sistema",
  "perfis": ["FUNCIONARIO"],
  "credenciais": [
    {
      "tipo": "USUARIO_SENHA",
      "nomeUsuario": "admin.sistema",
      "senha": "admin123"
    },
    {
      "tipo": "CODIGO_BARRA",
      "codigo": 987654321
    }
  ]
}
```

---

## 📚 Endpoints Principais

### **Autenticação**
| Método | Rota | Requer Auth | Perfil |
|--------|------|------------|--------|
| POST | `/auth/login` | ❌ | - |

### **Usuários**
| Método | Rota | Requer Auth | Perfil |
|--------|------|------------|--------|
| POST | `/usuarios` | ❌ | - |
| GET | `/usuarios` | ❌ | - |
| GET | `/usuarios/{id}` | ❌ | - |
| PUT | `/usuarios/{id}` | ✅ | FUNCIONÁRIO |
| DELETE | `/usuarios/{id}` | ✅ | FUNCIONÁRIO |

### **Credenciais**
| Método | Rota | Requer Auth | Perfil |
|--------|------|------------|--------|
| POST | `/usuarios/{usuarioId}/credenciais` | ✅ | FUNCIONÁRIO |
| GET | `/usuarios/{usuarioId}/credenciais` | ❌ | - |
| PUT | `/usuarios/{usuarioId}/credenciais/{credencialId}/senha` | ✅ | FUNCIONÁRIO |
| PUT | `/usuarios/{usuarioId}/credenciais/{credencialId}/desativar` | ✅ | FUNCIONÁRIO |
| PUT | `/usuarios/{usuarioId}/credenciais/{credencialId}/ativar` | ✅ | FUNCIONÁRIO |
| DELETE | `/usuarios/{usuarioId}/credenciais/{credencialId}` | ✅ | FUNCIONÁRIO |

### **Empresas**
| Método | Rota | Requer Auth | Perfil |
|--------|------|------------|--------|
| POST | `/empresas` | ✅ | FUNCIONÁRIO |
| GET | `/empresas` | ❌ | - |
| GET | `/empresas/{id}` | ❌ | - |
| PUT | `/empresas/{id}` | ✅ | FUNCIONÁRIO |
| DELETE | `/empresas/{id}` | ✅ | FUNCIONÁRIO |

### **Mercadorias**
| Método | Rota | Requer Auth | Perfil |
|--------|------|------------|--------|
| POST | `/mercadorias` | ✅ | FUNCIONÁRIO |
| GET | `/mercadorias` | ❌ | - |
| GET | `/mercadorias/{id}` | ❌ | - |
| PUT | `/mercadorias/{id}` | ✅ | FUNCIONÁRIO |
| DELETE | `/mercadorias/{id}` | ✅ | FUNCIONÁRIO |

### **Serviços**
| Método | Rota | Requer Auth | Perfil |
|--------|------|------------|--------|
| POST | `/servicos` | ✅ | FUNCIONÁRIO |
| GET | `/servicos` | ❌ | - |
| GET | `/servicos/{id}` | ❌ | - |
| PUT | `/servicos/{id}` | ✅ | FUNCIONÁRIO |
| DELETE | `/servicos/{id}` | ✅ | FUNCIONÁRIO |

### **Veículos**
| Método | Rota | Requer Auth | Perfil |
|--------|------|------------|--------|
| POST | `/veiculos` | ✅ | FUNCIONÁRIO |
| GET | `/veiculos` | ❌ | - |
| GET | `/veiculos/{id}` | ❌ | - |
| PUT | `/veiculos/{id}` | ✅ | FUNCIONÁRIO |
| DELETE | `/veiculos/{id}` | ✅ | FUNCIONÁRIO |

### **Vendas**
| Método | Rota | Requer Auth | Perfil |
|--------|------|------------|--------|
| POST | `/vendas` | ✅ | FUNCIONÁRIO |
| GET | `/vendas` | ❌ | - |
| GET | `/vendas/{id}` | ❌ | - |
| POST | `/vendas/{id}/mercadorias/{mercadoriaId}` | ✅ | FUNCIONÁRIO |
| DELETE | `/vendas/{id}/mercadorias/{mercadoriaId}` | ✅ | FUNCIONÁRIO |
| POST | `/vendas/{id}/servicos/{servicoId}` | ✅ | FUNCIONÁRIO |
| DELETE | `/vendas/{id}/servicos/{servicoId}` | ✅ | FUNCIONÁRIO |
| POST | `/vendas/{id}/veiculo/{veiculoId}` | ✅ | FUNCIONÁRIO |
| DELETE | `/vendas/{id}/veiculo` | ✅ | FUNCIONÁRIO |
| PUT | `/vendas/{id}` | ✅ | FUNCIONÁRIO |
| DELETE | `/vendas/{id}` | ✅ | FUNCIONÁRIO |

---

## 🔄 Fluxo de Login Completo

### **Passo 1: Criar Usuário Funcionário**
```bash
POST /usuarios
{
  "nome": "Pedro Vendedor",
  "perfis": ["FUNCIONARIO"],
  "credenciais": [{
    "tipo": "USUARIO_SENHA",
    "nomeUsuario": "pedro.vendedor",
    "senha": "pedrosecuro123"
  }]
}
```

### **Passo 2: Fazer Login**
```bash
POST /auth/login
{
  "nomeUsuario": "pedro.vendedor",
  "senha": "pedrosecuro123"
}
# Resposta: { "usuario": {...}, "token": "..." }
```

### **Passo 3: Copiar o Token**
```
token = "am9hby5nZXJlbmNpYWRvcjozNjMyYjA2ZC05MzE5LTQ1NzItYWNmMS0xZWE3MmZkYjA1YzQ="
```

### **Passo 4: Usar Token em Requisições**
```bash
POST /empresas
Authorization: Bearer am9hby5nZXJlbmNpYWRvcjozNjMyYjA2ZC05MzE5LTQ1NzItYWNmMS0xZWE3MmZkYjA1YzQ=
Content-Type: application/json

{
  "razaoSocial": "AutoRepair LTDA",
  "nomeFantasia": "AutoRepair"
}
```

---

## ❌ Erros Comuns

### **401 Unauthorized**
```
Requisição sem header Authorization
Solução: Copie o token do login e adicione: Authorization: Bearer <token>
```

### **403 Forbidden**
```
Usuário não é FUNCIONÁRIO
Solução: Login com usuário FUNCIONÁRIO, não CLIENTE ou FORNECEDOR
```

### **400 Bad Request**
```
Dados inválidos (ex: nomeUsuario duplicado)
Solução: Verifique validações no payload
```

### **404 Not Found**
```
Recurso não existe
Solução: Verifique se o ID existe no sistema
```

---

## 🧪 Testes

**Total: 175+ testes de integração**

Executar testes:
```bash
mvn clean test
```

Testes cobrem:
- ✅ Criação, listagem, atualização e deleção (CRUD)
- ✅ Validações de DTOs
- ✅ Autenticação e autorização
- ✅ HATEOAS links
- ✅ Edge cases e cenários de erro

---

## 📊 Estrutura de Dados

### **Usuário**
- ID (Long)
- Nome (String)
- Nome Social (String, opcional)
- Perfis (CLIENTE, FUNCIONÁRIO, FORNECEDOR)
- Credenciais (username/password ou código de barra)
- Documentos, Emails, Telefones
- Endereço
- Veículos (se proprietário)
- Mercadorias (se fornecedor)
- Vendas (se cliente ou funcionário)

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

## 📝 Postman Collection

Uma collection Postman com 175+ requisições pré-configuradas está disponível em:
```
AutoManager-Tests.postman_collection.json
```

Import no Postman e configure a variável `{{base_url}}` com `http://localhost:8080`

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

| Feature | Status | Testes |
|---------|--------|--------|
| Cadastro de Empresa | ✅ | 30 |
| Gerenciamento de Usuários | ✅ | 30 |
| Autenticação com Token | ✅ | Integrado |
| Gerenciamento de Credenciais | ✅ | 10 |
| Cadastro de Mercadorias | ✅ | 35 |
| Cadastro de Serviços | ✅ | 30 |
| Gerenciamento de Vendas | ✅ | 58 |
| HATEOAS Navigation | ✅ | Integrado |
| **TOTAL** | ✅ | **175+** |

---

## ⚡ Início Rápido (5 minutos)

1. **Compile o projeto**
   ```bash
   mvn clean compile
   ```

2. **Crie um usuário FUNCIONÁRIO**
   ```bash
   POST /usuarios
   { "nome": "Admin", "perfis": ["FUNCIONARIO"], "credenciais": [{"tipo": "USUARIO_SENHA", "nomeUsuario": "admin", "senha": "admin123"}] }
   ```

3. **Faça login**
   ```bash
   POST /auth/login
   { "nomeUsuario": "admin", "senha": "admin123" }
   ```

4. **Copie o token e use em suas requisições**
   ```bash
   Authorization: Bearer <token_aqui>
   ```

5. **Comece a criar empresas, mercadorias, serviços!**

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
