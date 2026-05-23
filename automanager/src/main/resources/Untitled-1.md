# File Tree: automanager

**Generated:** 5/9/2026, 5:20:55 PM
**Root Path:** `c:\Users\templ\Desktop\Facul\AV3-DevWeb-III\automanager`

```
├── .mvn
│   └── wrapper
│       ├── maven-wrapper.jar
│       └── maven-wrapper.properties
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── autobots
│   │   │           └── automanager
│   │   │               ├── controles
│   │   │               │   ├── CredencialControle.java
│   │   │               │   ├── DocumentoControle.java
│   │   │               │   ├── EmailControle.java
│   │   │               │   ├── EmpresaControle.java
│   │   │               │   ├── EnderecoControle.java
│   │   │               │   ├── MercadoriaControle.java
│   │   │               │   ├── ServicoControle.java
│   │   │               │   ├── TelefoneControle.java
│   │   │               │   ├── UsuarioControle.java
│   │   │               │   ├── VeiculoControle.java
│   │   │               │   └── VendaControle.java
│   │   │               ├── dtos
│   │   │               │   ├── Cliente
│   │   │               │   │   ├── ClienteAtualizarDTO.java
│   │   │               │   │   ├── ClienteCadastrarDTO.java
│   │   │               │   │   └── ClienteExibirDTO.java
│   │   │               │   ├── Credencial
│   │   │               │   ├── CredencialCodigoBarra
│   │   │               │   │   ├── CredencialCodigoBarraAtualizarDTO.java
│   │   │               │   │   ├── CredencialCodigoBarraCadastrarDTO.java
│   │   │               │   │   └── CredencialCodigoBarraExibirDTO.java
│   │   │               │   ├── CredencialUsuarioSenha
│   │   │               │   │   ├── CredencialUsuarioSenhaAtualizarDTO.java
│   │   │               │   │   └── CredencialUsuarioSenhaCadastrarDTO.java
│   │   │               │   ├── Documento
│   │   │               │   │   ├── DocumentoAtualizarDTO.java
│   │   │               │   │   ├── DocumentoCadastrarDTO.java
│   │   │               │   │   └── DocumentoExibirDTO.java
│   │   │               │   ├── Email
│   │   │               │   │   ├── EmailAtualizarDTO.java
│   │   │               │   │   ├── EmailCadastrarDTO.java
│   │   │               │   │   └── EmailExibirDTO.java
│   │   │               │   ├── Empresa
│   │   │               │   │   ├── EmpresaAtualizarDTO.java
│   │   │               │   │   ├── EmpresaCadastrarDTO.java
│   │   │               │   │   └── EmpresaExibirDTO.java
│   │   │               │   ├── Endereco
│   │   │               │   │   ├── EnderecoAtualizarDTO.java
│   │   │               │   │   ├── EnderecoCadastrarDTO.java
│   │   │               │   │   └── EnderecoExibirDTO.java
│   │   │               │   ├── Mercadoria
│   │   │               │   │   ├── MercadoriaAtualizarDTO.java
│   │   │               │   │   ├── MercadoriaCadastrarDTO.java
│   │   │               │   │   └── MercadoriaExibirDTO.java
│   │   │               │   ├── Servico
│   │   │               │   │   ├── ServicoAtualizarDTO.java
│   │   │               │   │   ├── ServicoCadastrarDTO.java
│   │   │               │   │   └── ServicoExibirDTO.java
│   │   │               │   ├── Telefone
│   │   │               │   │   ├── TelefoneAtualizarDTO.java
│   │   │               │   │   ├── TelefoneCadastrarDTO.java
│   │   │               │   │   └── TelefoneExibirDTO.java
│   │   │               │   ├── Usuario
│   │   │               │   │   ├── UsuarioAtualizarDTO.java
│   │   │               │   │   ├── UsuarioCadastrarDTO.java
│   │   │               │   │   └── UsuarioExibirDTO.java
│   │   │               │   ├── Veiculo
│   │   │               │   │   ├── VeiculoAtualizarDTO.java
│   │   │               │   │   ├── VeiculoCadastrarDTO.java
│   │   │               │   │   └── VeiculoExibirDTO.java
│   │   │               │   ├── Venda
│   │   │               │   │   ├── VendaAtualizarDTO.java
│   │   │               │   │   ├── VendaCadastrarDTO.java
│   │   │               │   │   └── VendaExibirDTO.java
│   │   │               │   └── ErroRespostaDTO.java
│   │   │               ├── entidades
│   │   │               │   ├── Credencial.java
│   │   │               │   ├── CredencialCodigoBarra.java
│   │   │               │   ├── CredencialUsuarioSenha.java
│   │   │               │   ├── Documento.java
│   │   │               │   ├── Email.java
│   │   │               │   ├── Empresa.java
│   │   │               │   ├── Endereco.java
│   │   │               │   ├── Mercadoria.java
│   │   │               │   ├── Servico.java
│   │   │               │   ├── Telefone.java
│   │   │               │   ├── Usuario.java
│   │   │               │   ├── Veiculo.java
│   │   │               │   └── Venda.java
│   │   │               ├── enumeracoes
│   │   │               │   ├── PerfilUsuario.java
│   │   │               │   ├── TipoDocumento.java
│   │   │               │   └── TipoVeiculo.java
│   │   │               ├── excecoes
│   │   │               │   ├── personalizado
│   │   │               │   │   └── EntidadeNaoEncontradaException.java
│   │   │               │   └── ManipuladorGlobal.java
│   │   │               ├── modeladores
│   │   │               │   ├── DocumentoModelador.java
│   │   │               │   ├── EmailModelador.java
│   │   │               │   ├── EmpresaModelador.java
│   │   │               │   ├── EnderecoModelador.java
│   │   │               │   ├── MercadoriaModelador.java
│   │   │               │   ├── ServicoModelador.java
│   │   │               │   ├── TelefoneModelador.java
│   │   │               │   ├── UsuarioModelador.java
│   │   │               │   ├── VeiculoModelador.java
│   │   │               │   └── VendaModelador.java
│   │   │               ├── modelo
│   │   │               │   ├── Credencial
│   │   │               │   ├── Documento
│   │   │               │   │   ├── DocumentoAtualizador.java
│   │   │               │   │   └── DocumentoSelecionador.java
│   │   │               │   ├── Email
│   │   │               │   │   ├── EmailAtualizador.java
│   │   │               │   │   └── EmailSelecionador.java
│   │   │               │   ├── Empresa
│   │   │               │   │   ├── EmpresaAtualizador.java
│   │   │               │   │   └── EmpresaSelecionador.java
│   │   │               │   ├── Endereco
│   │   │               │   │   ├── EnderecoAtualizador.java
│   │   │               │   │   └── EnderecoSelecionador.java
│   │   │               │   ├── Mercadoria
│   │   │               │   │   ├── MercadoriaAtualizador.java
│   │   │               │   │   └── MercadoriaSelecionador.java
│   │   │               │   ├── Servico
│   │   │               │   │   ├── ServicoAtualizador.java
│   │   │               │   │   └── ServicoSelecionador.java
│   │   │               │   ├── Telefone
│   │   │               │   │   ├── TelefoneAtualizador.java
│   │   │               │   │   └── TelefoneSelecionador.java
│   │   │               │   ├── Usuario
│   │   │               │   │   ├── UsuarioAtualizador.java
│   │   │               │   │   └── UsuarioSelecionador.java
│   │   │               │   ├── Veiculo
│   │   │               │   │   ├── VeiculoAtualizador.java
│   │   │               │   │   └── VeiculoSelecionador.java
│   │   │               │   ├── Venda
│   │   │               │   │   ├── VendaAtualizador.java
│   │   │               │   │   └── VendaSelecionador.java
│   │   │               │   └── StringVerificadorNulo.java
│   │   │               ├── repositorios
│   │   │               │   ├── CredencialRepositorio.java
│   │   │               │   ├── DocumentoRepositorio.java
│   │   │               │   ├── EmailRepositorio.java
│   │   │               │   ├── EmpresaRepositorio.java
│   │   │               │   ├── EnderecoRepositorio.java
│   │   │               │   ├── MercadoriaRepositorio.java
│   │   │               │   ├── RepositorioEmpresa.java
│   │   │               │   ├── ServicoRepositorio.java
│   │   │               │   ├── TelefoneRepositorio.java
│   │   │               │   ├── UsuarioRepositorio.java
│   │   │               │   ├── VeiculoRepositorio.java
│   │   │               │   └── VendaRepositorio.java
│   │   │               ├── servicos
│   │   │               │   ├── DocumentoServico.java
│   │   │               │   ├── EmailServico.java
│   │   │               │   ├── EmpresaServico.java
│   │   │               │   ├── EnderecoServico.java
│   │   │               │   ├── MercadoriaServico.java
│   │   │               │   ├── ServicoServico.java
│   │   │               │   ├── TelefoneServico.java
│   │   │               │   ├── UsuarioServico.java
│   │   │               │   ├── VeiculoServico.java
│   │   │               │   └── VendaServico.java
│   │   │               └── AutomanagerApplication.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── autobots
│                   └── automanager
│                       └── AutomanagerApplicationTests.java
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

---
*Generated by FileTree Pro Extension*