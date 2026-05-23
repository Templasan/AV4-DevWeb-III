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

// Documentado: @WithMockUser não funciona com SessionCreationPolicy via SecurityContextPersistenceFilter.
// A solução é reconstruir o MockMvc com o usuário admin como defaultRequest em @BeforeEach.
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CredencialControleIT {

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

    private Long criarUsuarioComCredencial(String nomeUsuario, String senha) throws Exception {
        String credencialJson = String.format(java.util.Locale.US,
            "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"%s\",\"senha\":\"%s\"}",
            nomeUsuario, senha
        );
        String usuarioJson = String.format(java.util.Locale.US,
            "{\"nome\":\"User Test\",\"perfis\":[\"CLIENTE\"],\"credenciais\":[%s]}",
            credencialJson
        );

        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long criarUsuarioSemCredencial() throws Exception {
        String usuarioJson = "{\"nome\":\"User Sem Cred\",\"perfis\":[\"CLIENTE\"]}";

        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(usuarioJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @Order(1)
    void adicionar_credencialUsuarioSenha_deveRetornar201() throws Exception {
        Long usuarioId = criarUsuarioSemCredencial();

        String json = "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"novo.user\",\"senha\":\"senha123\"}";

        mockMvc.perform(post("/usuarios/" + usuarioId + "/credenciais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipo").value("USUARIO_SENHA"))
                .andExpect(jsonPath("$.nomeUsuario").value("novo.user"))
                .andExpect(jsonPath("$.inativo").value(false));
    }

    @Test
    @Order(2)
    void adicionar_credencialCodigoBarra_deveRetornar201() throws Exception {
        Long usuarioId = criarUsuarioSemCredencial();

        String json = "{\"tipo\":\"CODIGO_BARRA\",\"codigo\":123456789}";

        mockMvc.perform(post("/usuarios/" + usuarioId + "/credenciais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipo").value("CODIGO_BARRA"))
                .andExpect(jsonPath("$.codigo").value(123456789))
                .andExpect(jsonPath("$.inativo").value(false));
    }

    @Test
    @Order(3)
    void adicionar_nomeUsuarioDuplicado_deveRetornar400() throws Exception {
        Long usuarioId = criarUsuarioSemCredencial();

        String json = "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"duplicado.user\",\"senha\":\"senha123\"}";

        // Primeira adição
        mockMvc.perform(post("/usuarios/" + usuarioId + "/credenciais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        // Segunda adição com mesmo nomeUsuario
        Long outroUsuarioId = criarUsuarioSemCredencial();
        mockMvc.perform(post("/usuarios/" + outroUsuarioId + "/credenciais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void listar_credenciais_deveRetornar200() throws Exception {
        Long usuarioId = criarUsuarioComCredencial("user.lista", "senha123");

        mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.credencialExibirDTOList[0].id").exists())
                .andExpect(jsonPath("$._embedded.credencialExibirDTOList[0].nomeUsuario").value("user.lista"));
    }

    @Test
    @Order(5)
    void atualizarSenha_deveRetornar200() throws Exception {
        Long usuarioId = criarUsuarioComCredencial("user.senha", "senha.antiga");

        // Obter ID da credencial
        MvcResult listResult = mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andReturn();
        Long credencialId = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .get("_embedded").get("credencialExibirDTOList").get(0).get("id").asLong();

        String json = "{\"novaSenha\":\"senha.nova\"}";

        mockMvc.perform(put("/usuarios/" + usuarioId + "/credenciais/" + credencialId + "/senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(credencialId));
    }

    @Test
    @Order(6)
    void desativarCredencial_deveRetornar204() throws Exception {
        Long usuarioId = criarUsuarioComCredencial("user.desativa", "senha123");

        // Obter ID da credencial
        MvcResult listResult = mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andReturn();
        Long credencialId = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .get("_embedded").get("credencialExibirDTOList").get(0).get("id").asLong();

        mockMvc.perform(put("/usuarios/" + usuarioId + "/credenciais/" + credencialId + "/desativar"))
                .andExpect(status().isNoContent());

        // Verifica se ficou inativo
        mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.credencialExibirDTOList[0].inativo").value(true));
    }

    @Test
    @Order(7)
    void ativarCredencial_deveRetornar204() throws Exception {
        Long usuarioId = criarUsuarioComCredencial("user.ativa", "senha123");

        // Obter ID da credencial
        MvcResult listResult = mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andReturn();
        Long credencialId = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .get("_embedded").get("credencialExibirDTOList").get(0).get("id").asLong();

        // Desativar
        mockMvc.perform(put("/usuarios/" + usuarioId + "/credenciais/" + credencialId + "/desativar"))
                .andExpect(status().isNoContent());

        // Ativar
        mockMvc.perform(put("/usuarios/" + usuarioId + "/credenciais/" + credencialId + "/ativar"))
                .andExpect(status().isNoContent());

        // Verifica se está ativo
        mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.credencialExibirDTOList[0].inativo").value(false));
    }

    @Test
    @Order(8)
    void removerCredencial_deveRetornar204() throws Exception {
        Long usuarioId = criarUsuarioComCredencial("user.remove", "senha123");

        // Obter ID da credencial
        MvcResult listResult = mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andReturn();
        Long credencialId = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .get("_embedded").get("credencialExibirDTOList").get(0).get("id").asLong();

        mockMvc.perform(delete("/usuarios/" + usuarioId + "/credenciais/" + credencialId))
                .andExpect(status().isNoContent());

        // Verifica se foi removido
        mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    void adicionarMultiplasCredenciais_deveRetornar201() throws Exception {
        Long usuarioId = criarUsuarioSemCredencial();

        // Adiciona credencial username/password
        String json1 = "{\"tipo\":\"USUARIO_SENHA\",\"nomeUsuario\":\"user.multi.1\",\"senha\":\"senha123\"}";
        mockMvc.perform(post("/usuarios/" + usuarioId + "/credenciais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json1))
                .andExpect(status().isCreated());

        // Adiciona credencial código de barra
        String json2 = "{\"tipo\":\"CODIGO_BARRA\",\"codigo\":987654321}";
        mockMvc.perform(post("/usuarios/" + usuarioId + "/credenciais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2))
                .andExpect(status().isCreated());

        // Verifica se listou ambas
        mockMvc.perform(get("/usuarios/" + usuarioId + "/credenciais"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void atualizarSenha_emCredencialInexistente_deveRetornar404() throws Exception {
        Long usuarioId = criarUsuarioSemCredencial();

        String json = "{\"novaSenha\":\"nova.senha\"}";

        mockMvc.perform(put("/usuarios/" + usuarioId + "/credenciais/9999/senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }
}
