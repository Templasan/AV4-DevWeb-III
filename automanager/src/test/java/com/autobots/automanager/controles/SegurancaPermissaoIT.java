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

/**
 * Testa as regras de acesso por perfil conforme a tabela AV4.
 * Usa SecurityMockMvcRequestPostProcessors.user() para simular o perfil do usuário logado.
 * Cobre: acesso negado (403) e acesso permitido (2xx) por perfil.
 *
 * Observação: testes 403 para POST/PUT usam corpos válidos pois a validação de
 * @Valid @RequestBody ocorre antes do @PreAuthorize no ciclo Spring MVC.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SegurancaPermissaoIT {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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

    private Long criarMercadoriaComoAdmin(String nome) throws Exception {
        Long fornecedorId = criarUsuarioComoAdmin("Fornecedor Seg", "FORNECEDOR");
        String json = String.format(
            "{\"nome\":\"%s\",\"valor\":10.00,\"quantidade\":5,\"validade\":\"2026-12-31\"," +
            "\"fabricacao\":\"2024-01-01\",\"descricao\":\"Desc\",\"idFornecedor\":%d}",
            nome, fornecedorId);
        MvcResult result = mockMvc.perform(post("/mercadorias")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long criarServicoComoAdmin(String nome) throws Exception {
        String json = String.format("{\"nome\":\"%s\",\"valor\":50.00,\"descricao\":\"Desc\"}", nome);
        MvcResult result = mockMvc.perform(post("/servicos")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    // =========================================================================
    // SEM AUTENTICAÇÃO → 403
    // =========================================================================

    @Test
    @Order(1)
    void semAuth_getUsuarios_deveRetornar403() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(2)
    void semAuth_getMercadorias_deveRetornar403() throws Exception {
        mockMvc.perform(get("/mercadorias"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void semAuth_postVendas_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificacao\":\"TEST\",\"clienteId\":1,\"funcionarioId\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void semAuth_getVendas_deveRetornar403() throws Exception {
        mockMvc.perform(get("/vendas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void semAuth_postDocumentos_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(post("/documentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"CPF\",\"numero\":\"12345678900\",\"usuarioId\":1,\"dataEmissao\":\"2024-01-01\"}"))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // CLIENTE → acesso negado em operações não permitidas
    // =========================================================================

    @Test
    @Order(10)
    void cliente_getMercadorias_deveRetornar403() throws Exception {
        mockMvc.perform(get("/mercadorias")
                .with(user("cliente").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(11)
    void cliente_getListaUsuarios_deveRetornar403() throws Exception {
        mockMvc.perform(get("/usuarios")
                .with(user("cliente").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(12)
    void cliente_postUsuarios_deveRetornar403() throws Exception {
        mockMvc.perform(post("/usuarios")
                .with(user("cliente").roles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Teste\",\"perfis\":[\"CLIENTE\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    void cliente_postVendas_deveRetornar403() throws Exception {
        mockMvc.perform(post("/vendas")
                .with(user("cliente").roles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificacao\":\"X\",\"clienteId\":1,\"funcionarioId\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(14)
    void cliente_getListaVendas_deveRetornar403() throws Exception {
        mockMvc.perform(get("/vendas")
                .with(user("cliente").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(15)
    void cliente_postDocumentos_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(post("/documentos")
                .with(user("cliente").roles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"CPF\",\"numero\":\"12345678900\",\"usuarioId\":1,\"dataEmissao\":\"2024-01-01\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(16)
    void cliente_getListaDocumentos_deveRetornar403() throws Exception {
        mockMvc.perform(get("/documentos")
                .with(user("cliente").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(17)
    void cliente_postMercadorias_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(post("/mercadorias")
                .with(user("cliente").roles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Test\",\"validade\":\"2026-12-31\",\"fabricacao\":\"2024-01-01\",\"idFornecedor\":1,\"quantidade\":10,\"valor\":50.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(18)
    void cliente_putMercadoria_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(put("/mercadorias/1")
                .with(user("cliente").roles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"nome\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(19)
    void cliente_postVeiculos_deveRetornar403() throws Exception {
        mockMvc.perform(post("/veiculos")
                .with(user("cliente").roles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"placa\":\"ABC1D23\",\"modelo\":\"Test\",\"tipo\":\"SEDA\",\"proprietarioId\":1}"))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // VENDEDOR → acesso negado em operações de escrita de mercadoria/serviço
    // =========================================================================

    @Test
    @Order(20)
    void vendedor_getMercadorias_deveRetornar200() throws Exception {
        mockMvc.perform(get("/mercadorias")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(21)
    void vendedor_getServicos_deveRetornar200() throws Exception {
        mockMvc.perform(get("/servicos")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(22)
    void vendedor_postMercadorias_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(post("/mercadorias")
                .with(user("vendedor").roles("VENDEDOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Test\",\"validade\":\"2026-12-31\",\"fabricacao\":\"2024-01-01\",\"idFornecedor\":1,\"quantidade\":10,\"valor\":50.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(23)
    void vendedor_putMercadoria_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(put("/mercadorias/1")
                .with(user("vendedor").roles("VENDEDOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"nome\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(24)
    void vendedor_postServicos_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(post("/servicos")
                .with(user("vendedor").roles("VENDEDOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serv Test\",\"valor\":10.00,\"descricao\":\"Desc\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(25)
    void vendedor_putServico_deveRetornar403() throws Exception {
        // Corpo válido para que a validação passe e o @PreAuthorize possa bloquear
        mockMvc.perform(put("/servicos/1")
                .with(user("vendedor").roles("VENDEDOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serv Test\",\"valor\":50.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(26)
    void vendedor_getListaVendas_deveRetornar403() throws Exception {
        mockMvc.perform(get("/vendas")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(27)
    void vendedor_getVendasPorEmpresa_deveRetornar403() throws Exception {
        mockMvc.perform(get("/vendas/empresa/1")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(28)
    void vendedor_getListaDocumentos_deveRetornar403() throws Exception {
        mockMvc.perform(get("/documentos")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(29)
    void vendedor_getListaEmails_deveRetornar403() throws Exception {
        mockMvc.perform(get("/emails")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(30)
    void vendedor_getListaTelefones_deveRetornar403() throws Exception {
        mockMvc.perform(get("/telefones")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(31)
    void vendedor_getListaEnderecos_deveRetornar403() throws Exception {
        mockMvc.perform(get("/enderecos")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(32)
    void vendedor_getMercadoriaPorId_deveRetornar200QuandoExiste() throws Exception {
        Long mercadoriaId = criarMercadoriaComoAdmin("Merc Vendedor");

        mockMvc.perform(get("/mercadorias/" + mercadoriaId)
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(33)
    void vendedor_getServicoPorId_deveRetornar200QuandoExiste() throws Exception {
        Long servicoId = criarServicoComoAdmin("Serv Vendedor");

        mockMvc.perform(get("/servicos/" + servicoId)
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // GERENTE → acesso permitido em serviços, mercadorias e usuários
    // =========================================================================

    @Test
    @Order(40)
    void gerente_getUsuarios_deveRetornar200() throws Exception {
        mockMvc.perform(get("/usuarios")
                .with(user("gerente").roles("GERENTE")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(41)
    void gerente_postMercadorias_deveRetornar201() throws Exception {
        Long fornecedorId = criarUsuarioComoAdmin("Fornecedor Ger", "FORNECEDOR");
        String json = String.format(
            "{\"nome\":\"Mercadoria Gerente\",\"valor\":20.00,\"quantidade\":3," +
            "\"validade\":\"2026-12-31\",\"fabricacao\":\"2024-01-01\"," +
            "\"descricao\":\"Desc\",\"idFornecedor\":%d}", fornecedorId);

        mockMvc.perform(post("/mercadorias")
                .with(user("gerente").roles("GERENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(42)
    void gerente_postServicos_deveRetornar201() throws Exception {
        mockMvc.perform(post("/servicos")
                .with(user("gerente").roles("GERENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço Gerente\",\"valor\":100.00,\"descricao\":\"Desc\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(43)
    void gerente_getVendas_deveRetornar200() throws Exception {
        mockMvc.perform(get("/vendas")
                .with(user("gerente").roles("GERENTE")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(44)
    void gerente_deleteServico_deveRetornar204() throws Exception {
        Long servicoId = criarServicoComoAdmin("Servico Para Deletar Gerente");

        mockMvc.perform(delete("/servicos/" + servicoId)
                .with(user("gerente").roles("GERENTE")))
                .andExpect(status().isNoContent());
    }

    // =========================================================================
    // ADMIN → acesso total
    // =========================================================================

    @Test
    @Order(50)
    void admin_getUsuarios_deveRetornar200() throws Exception {
        mockMvc.perform(get("/usuarios")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(51)
    void admin_deleteUsuario_deveRetornar204() throws Exception {
        Long id = criarUsuarioComoAdmin("Usuario Para Deletar", "CLIENTE");

        mockMvc.perform(delete("/usuarios/" + id)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(52)
    void admin_getVendas_deveRetornar200() throws Exception {
        mockMvc.perform(get("/vendas")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(53)
    void admin_postMercadorias_deveRetornar201() throws Exception {
        Long fornecedorId = criarUsuarioComoAdmin("Fornecedor Admin", "FORNECEDOR");
        String json = String.format(
            "{\"nome\":\"Mercadoria Admin\",\"valor\":15.00,\"quantidade\":2," +
            "\"validade\":\"2026-12-31\",\"fabricacao\":\"2024-01-01\"," +
            "\"descricao\":\"Desc\",\"idFornecedor\":%d}", fornecedorId);

        mockMvc.perform(post("/mercadorias")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    // =========================================================================
    // DADOS DE CONTATO — lista completa bloqueada para VENDEDOR
    // =========================================================================

    @Test
    @Order(60)
    void vendedor_getDocumentoPorId_deveRetornar200() throws Exception {
        Long usuarioId = criarUsuarioComoAdmin("Usuario Doc", "CLIENTE");
        String docJson = String.format(
            "{\"tipo\":\"CPF\",\"numero\":\"12345678900\",\"usuarioId\":%d,\"dataEmissao\":\"2024-01-01\"}", usuarioId);
        MvcResult result = mockMvc.perform(post("/documentos")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(docJson))
                .andExpect(status().isCreated())
                .andReturn();
        Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/documentos/" + docId)
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(61)
    void admin_getListaDocumentos_deveRetornar200() throws Exception {
        mockMvc.perform(get("/documentos")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(62)
    void gerente_getListaEmails_deveRetornar200() throws Exception {
        mockMvc.perform(get("/emails")
                .with(user("gerente").roles("GERENTE")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(63)
    void vendedor_getEnderecoEmpresa_deveRetornar403() throws Exception {
        mockMvc.perform(get("/enderecos/empresa/1")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(64)
    void vendedor_getTelefoneEmpresa_deveRetornar403() throws Exception {
        mockMvc.perform(get("/telefones/empresa/1")
                .with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(65)
    void admin_getEnderecoEmpresa_deveRetornar200OuNotFound() throws Exception {
        int status = mockMvc.perform(get("/enderecos/empresa/1")
                .with(user("admin").roles("ADMIN")))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 404,
                "Esperado 200 ou 404, mas foi " + status);
    }
}
