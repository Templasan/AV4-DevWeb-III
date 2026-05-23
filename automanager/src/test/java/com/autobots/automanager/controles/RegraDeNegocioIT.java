package com.autobots.automanager.controles;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Testa as regras de negócio validadas no service layer que o @PreAuthorize não cobre.
 *
 * Principais cenários:
 *  - VENDEDOR só pode criar/editar/excluir usuários com perfil CLIENTE
 *  - VENDEDOR não pode gerenciar dados de endereço/telefone vinculados a empresas
 *  - VENDEDOR pode editar dados de contato de usuários CLIENTE ou de si mesmo
 *
 * IMPORTANTE: usa UserDetailsImpl real para que ContextoSeguranca.getUsuario() funcione
 * e as restrições de service layer sejam aplicadas corretamente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegraDeNegocioIT {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    /** Entidade real do VENDEDOR criada no @BeforeEach para uso como principal. */
    private Usuario vendedorEntity;

    @BeforeEach
    void setupMockMvcEVendedor() throws Exception {
        this.mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .defaultRequest(get("/").with(user("admin").roles("ADMIN")))
                .build();

        // Cria um VENDEDOR real no banco para que ContextoSeguranca.getUsuario() retorne
        // uma entidade válida e as verificações de service layer sejam aplicadas
        String json = "{\"nome\":\"Vendedor Test\",\"perfis\":[\"VENDEDOR\"]}";
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();
        Long vendedorId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        this.vendedorEntity = usuarioRepositorio.findById(vendedorId).orElseThrow();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Executa uma requisição autenticada como o VENDEDOR real (com UserDetailsImpl). */
    private ResultActions comVendedor(MockHttpServletRequestBuilder req) throws Exception {
        UserDetailsImpl details = new UserDetailsImpl(vendedorEntity);
        return mockMvc.perform(req.with(user(details)));
    }

    private Long criarUsuarioComoAdmin(String nome, String perfil) throws Exception {
        String json = String.format("{\"nome\":\"%s\",\"perfis\":[\"%s\"]}", nome, perfil);
        MvcResult result = mockMvc.perform(post("/usuarios")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long criarEmpresaComoAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"razaoSocial\":\"Empresa Regra\",\"nomeFantasia\":\"EmpReg\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    // =========================================================================
    // VENDEDOR — CRUD de Usuários
    // =========================================================================

    @Test
    @Order(1)
    void vendedor_criarUsuarioCliente_deveRetornar201() throws Exception {
        comVendedor(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Cliente Novo\",\"perfis\":[\"CLIENTE\"]}"))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void vendedor_criarUsuarioGerente_deveRetornar403() throws Exception {
        comVendedor(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Gerente Ilegal\",\"perfis\":[\"GERENTE\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void vendedor_criarUsuarioAdmin_deveRetornar403() throws Exception {
        comVendedor(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Admin Ilegal\",\"perfis\":[\"ADMIN\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void vendedor_criarOutroVendedor_deveRetornar403() throws Exception {
        comVendedor(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Vendedor Ilegal\",\"perfis\":[\"VENDEDOR\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void vendedor_editarUsuarioCliente_deveRetornar200() throws Exception {
        Long clienteId = criarUsuarioComoAdmin("Cliente Para Editar", "CLIENTE");

        String updateJson = String.format(
            "{\"id\":%d,\"nome\":\"Cliente Editado\",\"perfis\":[\"CLIENTE\"]}", clienteId);
        comVendedor(put("/usuarios/" + clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void vendedor_editarUsuarioGerente_deveRetornar403() throws Exception {
        Long gerenteId = criarUsuarioComoAdmin("Gerente Existente", "GERENTE");

        String updateJson = String.format(
            "{\"id\":%d,\"nome\":\"Gerente Modificado\",\"perfis\":[\"GERENTE\"]}", gerenteId);
        comVendedor(put("/usuarios/" + gerenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void vendedor_excluirUsuarioCliente_deveRetornar204() throws Exception {
        Long clienteId = criarUsuarioComoAdmin("Cliente Para Excluir", "CLIENTE");

        comVendedor(delete("/usuarios/" + clienteId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(8)
    void vendedor_excluirUsuarioGerente_deveRetornar403() throws Exception {
        Long gerenteId = criarUsuarioComoAdmin("Gerente Para Excluir Tentativa", "GERENTE");

        comVendedor(delete("/usuarios/" + gerenteId))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // VENDEDOR — Telefone de Empresa é proibido
    // =========================================================================

    @Test
    @Order(20)
    void vendedor_criarTelefoneDeEmpresa_deveRetornar403() throws Exception {
        Long empresaId = criarEmpresaComoAdmin();

        String json = String.format(
            "{\"ddd\":\"11\",\"numero\":\"999990001\",\"tipoDono\":\"EMPRESA\",\"idDono\":%d}", empresaId);
        comVendedor(post("/telefones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(21)
    void vendedor_criarTelefoneDeClientePermitido() throws Exception {
        Long clienteId = criarUsuarioComoAdmin("Cliente Telefone", "CLIENTE");

        String json = String.format(
            "{\"ddd\":\"11\",\"numero\":\"999990002\",\"tipoDono\":\"USUARIO\",\"idDono\":%d}", clienteId);
        comVendedor(post("/telefones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    // =========================================================================
    // VENDEDOR — Endereço de Empresa é proibido
    // =========================================================================

    @Test
    @Order(30)
    void vendedor_criarEnderecoDeEmpresa_deveRetornar403() throws Exception {
        Long empresaId = criarEmpresaComoAdmin();

        String json = String.format(
            "{\"estado\":\"SP\",\"cidade\":\"SP\",\"bairro\":\"Centro\"," +
            "\"rua\":\"Rua A\",\"numero\":\"1\",\"codigoPostal\":\"01000-000\"," +
            "\"tipoDono\":\"EMPRESA\",\"idDono\":%d}", empresaId);
        comVendedor(post("/enderecos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(31)
    void vendedor_criarEnderecoDeClientePermitido() throws Exception {
        Long clienteId = criarUsuarioComoAdmin("Cliente Endereco", "CLIENTE");

        String json = String.format(
            "{\"estado\":\"SP\",\"cidade\":\"SP\",\"bairro\":\"Centro\"," +
            "\"rua\":\"Rua B\",\"numero\":\"2\",\"codigoPostal\":\"01001-000\"," +
            "\"tipoDono\":\"USUARIO\",\"idDono\":%d}", clienteId);
        comVendedor(post("/enderecos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    // =========================================================================
    // VENDEDOR — E-mail de usuário não-CLIENTE é proibido
    // =========================================================================

    @Test
    @Order(40)
    void vendedor_criarEmailDeGerente_deveRetornar403() throws Exception {
        Long gerenteId = criarUsuarioComoAdmin("Gerente Email", "GERENTE");

        String json = String.format(
            "{\"endereco\":\"gerente@test.com\",\"usuarioId\":%d}", gerenteId);
        comVendedor(post("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(41)
    void vendedor_criarEmailDeClientePermitido() throws Exception {
        Long clienteId = criarUsuarioComoAdmin("Cliente Email OK", "CLIENTE");

        String json = String.format(
            "{\"endereco\":\"cliente@test.com\",\"usuarioId\":%d}", clienteId);
        comVendedor(post("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    // =========================================================================
    // VENDEDOR — Documento de usuário não-CLIENTE é proibido
    // =========================================================================

    @Test
    @Order(50)
    void vendedor_criarDocumentoDeGerente_deveRetornar403() throws Exception {
        Long gerenteId = criarUsuarioComoAdmin("Gerente Doc", "GERENTE");

        String json = String.format(
            "{\"tipo\":\"CPF\",\"numero\":\"11122233300\",\"usuarioId\":%d,\"dataEmissao\":\"2020-01-01\"}", gerenteId);
        comVendedor(post("/documentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(51)
    void vendedor_criarDocumentoDeClientePermitido() throws Exception {
        Long clienteId = criarUsuarioComoAdmin("Cliente Doc OK", "CLIENTE");

        String json = String.format(
            "{\"tipo\":\"CPF\",\"numero\":\"44455566600\",\"usuarioId\":%d,\"dataEmissao\":\"2020-01-01\"}", clienteId);
        comVendedor(post("/documentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    // =========================================================================
    // GERENTE — sem restrições sobre CLIENTE (pode criar qualquer perfil exceto ADMIN)
    // =========================================================================

    @Test
    @Order(60)
    void gerente_criarUsuarioVendedor_deveRetornar201() throws Exception {
        mockMvc.perform(post("/usuarios")
                .with(user("gerente").roles("GERENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Vendedor Criado Por Gerente\",\"perfis\":[\"VENDEDOR\"]}"))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(61)
    void gerente_criarOutroGerente_deveRetornar201() throws Exception {
        mockMvc.perform(post("/usuarios")
                .with(user("gerente").roles("GERENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Gerente Novo\",\"perfis\":[\"GERENTE\"]}"))
                .andExpect(status().isCreated());
    }
}
