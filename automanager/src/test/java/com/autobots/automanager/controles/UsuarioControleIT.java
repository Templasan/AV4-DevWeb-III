package com.autobots.automanager.controles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioControleIT {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .defaultRequest(get("/").with(user("admin").roles("ADMIN")))
                .build();
    }

    private final String usuarioClienteJson = """
            {
              "nome": "João Silva",
              "nomeSocial": "João",
              "perfis": ["CLIENTE"]
            }
            """;

    private final String usuarioFuncionarioJson = """
            {
              "nome": "Ana Funcionária",
              "perfis": ["VENDEDOR"]
            }
            """;

    private final String usuarioSemNomeJson = """
            {
              "perfis": ["CLIENTE"]
            }
            """;

    private final String usuarioSemPerfisJson = """
            {
              "nome": "Sem Perfil"
            }
            """;

    @Test
    @Order(1)
    void criar_comDadosValidos_deveRetornar201() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @Order(2)
    void criar_semNome_deveRetornar400() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioSemNomeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void criar_semPerfis_deveRetornar400() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioSemPerfisJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void criar_comNomeCurto_deveRetornar400() throws Exception {
        String jsonNomeCurto = "{\"nome\":\"Ab\",\"perfis\":[\"CLIENTE\"]}";
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonNomeCurto))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void listar_deveRetornar200() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void obterPorId_comIdExistente_deveRetornarUsuario() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @Order(7)
    void obterPorId_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/usuarios/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void atualizar_comDadosValidos_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"João Atualizado\",\"perfis\":[\"CLIENTE\"]}", id);

        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    void deletar_comIdExistente_deveRetornar204() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/usuarios/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void deletar_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(delete("/usuarios/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    void criar_comEnderecoEmbutido_deveRetornar201() throws Exception {
        String comEndereco = """
                {
                  "nome": "Usuário Com Endereço",
                  "perfis": ["CLIENTE"],
                  "endereco": {
                    "estado": "SP",
                    "cidade": "São Paulo",
                    "bairro": "Centro",
                    "rua": "Av Paulista",
                    "numero": "1000",
                    "codigoPostal": "01310-100"
                  }
                }
                """;
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(comEndereco))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endereco.estado").value("SP"));
    }

    @Test
    @Order(12)
    void deletar_deveRemoverCompletamente() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Confirmar existência
        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isOk());

        // Deletar
        mockMvc.perform(delete("/usuarios/" + id))
                .andExpect(status().isNoContent());

        // Confirmar remoção
        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isNotFound());

        // Deletar novamente deve retornar 404
        mockMvc.perform(delete("/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(13)
    void criar_comNomeLongo_deveRetornar400() throws Exception {
        String nomeLongo = "A".repeat(101);
        String json = String.format(java.util.Locale.US,"{\"nome\":\"%s\",\"perfis\":[\"CLIENTE\"]}", nomeLongo);
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    void atualizar_comNomeSemAlterar_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Atualizar com mesmo nome
        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"João Silva\",\"perfis\":[\"CLIENTE\"]}", id);
        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @Order(15)
    void criar_multiplasPermissoes_deveRetornar201() throws Exception {
        String jsonMultiPerfis = """
                {
                  "nome": "Usuário Multi-Perfil",
                  "perfis": ["CLIENTE", "VENDEDOR"]
                }
                """;
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMultiPerfis))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Usuário Multi-Perfil"));
    }

    @Test
    @Order(16)
    void atualizar_comNomeCurto_deveRetornar4000() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"AB\",\"perfis\":[\"CLIENTE\"]}", id);
        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(17)
    void criar_comNomeSocial_deveRetornar201() throws Exception {
        String json = """
                {
                  "nome": "Maria da Silva",
                  "nomeSocial": "Maria",
                  "perfis": ["CLIENTE"]
                }
                """;
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeSocial").value("Maria"));
    }

    @Test
    @Order(18)
    void listar_deveRetornarMultiplosUsuarios() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson));
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioFuncionarioJson));
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(19)
    void atualizar_comPerfisAlterados_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"João Silva\",\"perfis\":[\"VENDEDOR\"]}", id);
        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(20)
    void criar_comEnderecoCompleto_deveRetornar201() throws Exception {
        String comEndereco = """
                {
                  "nome": "Carlos Endereço",
                  "perfis": ["CLIENTE"],
                  "endereco": {
                    "estado": "RJ",
                    "cidade": "Rio de Janeiro",
                    "bairro": "Copacabana",
                    "rua": "Av Atlântica",
                    "numero": "500",
                    "codigoPostal": "22010-020",
                    "informacoesAdicionais": "Apto 101"
                  }
                }
                """;
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(comEndereco))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endereco.cidade").value("Rio de Janeiro"))
                .andExpect(jsonPath("$.endereco.numero").value("500"));
    }

    @Test
    @Order(21)
    void atualizar_deveConsistenciaDeId() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"João Novo\",\"perfis\":[\"CLIENTE\"]}", id);
        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @Order(22)
    void deletar_emCascata_deveRemoverCompletamente() throws Exception {
        String comEndereco = """
                {
                  "nome": "Usuario para Deletar",
                  "perfis": ["CLIENTE"],
                  "endereco": {
                    "estado": "MG",
                    "cidade": "Belo Horizonte",
                    "bairro": "Centro",
                    "rua": "Rua A",
                    "numero": "123",
                    "codigoPostal": "30130-100"
                  }
                }
                """;
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(comEndereco))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/usuarios/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(23)
    void atualizar_comNomeCurto_deveRetornar400() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"AB\",\"perfis\":[\"CLIENTE\"]}", id);
        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    void atualizar_comNomeComEspacos_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"  João  \",\"perfis\":[\"CLIENTE\"]}", id);
        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(25)
    void criar_comNomeMuitoLongo_deveRetornar400() throws Exception {
        String nomeLongo = "A".repeat(101);
        String json = String.format(java.util.Locale.US,"{\"nome\":\"%s\",\"perfis\":[\"CLIENTE\"]}", nomeLongo);
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(26)
    void atualizar_comPerfisVazios_naoDeveAlterarPerfisAnteriores() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"João Novo\",\"perfis\":[\"CLIENTE\"]}", id);
        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(27)
    void criar_comMultiplosPerfis_diferentes_deveAceitar() throws Exception {
        String json = """
                {
                  "nome": "Usuário Multi-Perfil Completo",
                  "perfis": ["CLIENTE", "VENDEDOR"]
                }
                """;
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Usuário Multi-Perfil Completo"));
    }

    @Test
    @Order(28)
    void deletar_comEndereco_deveRemoverCascata() throws Exception {
        String comEndereco = """
                {
                  "nome": "Usuario com Endereco para Deletar",
                  "perfis": ["CLIENTE"],
                  "endereco": {
                    "estado": "RS",
                    "cidade": "Porto Alegre",
                    "bairro": "Centro",
                    "rua": "Rua Central",
                    "numero": "999",
                    "codigoPostal": "90010-150"
                  }
                }
                """;
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(comEndereco))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco.estado").value("RS"));

        mockMvc.perform(delete("/usuarios/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(29)
    void criar_comNomeSocial_completo_deveRetornar201() throws Exception {
        String json = """
                {
                  "nome": "Fernando da Silva Oliveira",
                  "nomeSocial": "Fernando",
                  "perfis": ["CLIENTE"]
                }
                """;
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeSocial").value("Fernando"));
    }

    @Test
    @Order(30)
    void atualizar_comNomeSocialNulo_deveAceitar() throws Exception {
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioClienteJson))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"João Silva\",\"perfis\":[\"CLIENTE\"]}", id);
        mockMvc.perform(put("/usuarios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void obter_usuario_deveRetornarHATEOASLinks() throws Exception {
        String usuarioJson = "{\"nome\":\"Usuario HATEOAS\",\"perfis\":[\"CLIENTE\"]}";

        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioJson))
                .andReturn();

        Long usuarioId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/usuarios/" + usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.usuarios.href").exists())
                .andExpect(jsonPath("$._links.editar.href").exists())
                .andExpect(jsonPath("$._links.excluir.href").exists())
                .andExpect(jsonPath("$._links.documentos.href").exists())
                .andExpect(jsonPath("$._links.telefones.href").exists())
                .andExpect(jsonPath("$._links.endereco.href").exists())
                .andExpect(jsonPath("$._links.veiculos.href").exists())
                .andExpect(jsonPath("$._links['vendas-como-cliente'].href").exists())
                .andExpect(jsonPath("$._links.mercadorias_fornecidas.href").exists());
    }

    @Test
    void obter_usuarioComEndereco_deveRetornarEnderecoModelado() throws Exception {
        String usuarioJson = "{\"nome\":\"Usuario com Endereco\",\"perfis\":[\"CLIENTE\"]," +
                "\"endereco\":{\"rua\":\"Rua Test\",\"numero\":\"123\",\"bairro\":\"Bairro\"," +
                "\"cidade\":\"Cidade\",\"estado\":\"SP\",\"codigoPostal\":\"01000-000\"}}";

        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioJson))
                .andReturn();

        Long usuarioId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/usuarios/" + usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco._links.self.href").exists())
                .andExpect(jsonPath("$.endereco._links.enderecos.href").exists());
    }
}
