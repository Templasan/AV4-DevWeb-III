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
 * Testes de acesso baseado em dono (ownership).
 *
 * Verifica que as regras SpEL usando ContextoSeguranca funcionam corretamente
 * quando um usuário real (entidade Usuario do banco) é injetado como principal.
 *
 * Estratégia:
 *  1. Criar usuários reais via API (como ADMIN)
 *  2. Carregar a entidade Usuario do DB via UsuarioRepositorio
 *  3. Injetar o principal real com SecurityMockMvcRequestPostProcessors.user(new UserDetailsImpl(usuario))
 *  4. Verificar que o SpEL (@segurancaUtil.isProprioUsuario, isParticipanteVenda, isVendedorDaVenda) retorna
 *     o resultado correto baseado no ID do usuário logado.
 *
 * Mudanças documentadas:
 *  - UserDetailsImpl.getUsuario() adicionado para expor a entidade
 *  - ContextoSeguranca.getUsuario() atualizado com fallback para SecurityContextHolder
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProprietarioAcessoIT {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Cria usuário via API (como ADMIN) e retorna o ID gerado. */
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

    /** Busca a entidade Usuario do banco de dados pelo ID. */
    private Usuario carregarUsuario(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + id));
    }

    /** Cria uma venda (como ADMIN) com o cliente e funcionário especificados. */
    private Long criarVendaComoAdmin(Long clienteId, Long funcionarioId) throws Exception {
        String json = String.format(
            "{\"identificacao\":\"PROP-%d\",\"clienteId\":%d,\"funcionarioId\":%d}",
            System.currentTimeMillis(), clienteId, funcionarioId);
        MvcResult result = mockMvc.perform(post("/vendas")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    /**
     * Realiza a requisição autenticando como o usuário real fornecido.
     * O UserDetailsImpl expõe a entidade Usuario, que é então retornada por
     * ContextoSeguranca.getUsuario() via o fallback do SecurityContextHolder.
     */
    private ResultActions comAutenticacao(MockHttpServletRequestBuilder req, Usuario usuario) throws Exception {
        UserDetailsImpl details = new UserDetailsImpl(usuario);
        return mockMvc.perform(req.with(user(details)));
    }

    // =========================================================================
    // isProprioUsuario — GET /usuarios/{id}
    // =========================================================================

    @Test
    @Order(1)
    void cliente_acessaProprioUsuario_deveRetornar200() throws Exception {
        Long idA = criarUsuarioComoAdmin("Cliente A", "CLIENTE");
        Usuario usuarioA = carregarUsuario(idA);

        comAutenticacao(get("/usuarios/" + idA), usuarioA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idA));
    }

    @Test
    @Order(2)
    void cliente_acessaOutroUsuario_deveRetornar403() throws Exception {
        Long idA = criarUsuarioComoAdmin("Cliente A", "CLIENTE");
        Long idB = criarUsuarioComoAdmin("Cliente B", "CLIENTE");
        Usuario usuarioA = carregarUsuario(idA);

        // usuarioA tenta ver dados de usuarioB → não é o próprio → 403
        comAutenticacao(get("/usuarios/" + idB), usuarioA)
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void vendedor_acessaQualquerCliente_deveRetornar200() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Visivel", "CLIENTE");
        Long idVendedor = criarUsuarioComoAdmin("Vendedor Leitor", "VENDEDOR");
        Usuario vendedor = carregarUsuario(idVendedor);

        // VENDEDOR tem hasAnyRole('ADMIN','GERENTE','VENDEDOR') na rota GET /usuarios/{id}
        comAutenticacao(get("/usuarios/" + idCliente), vendedor)
                .andExpect(status().isOk());
    }

    // =========================================================================
    // isParticipanteVenda — GET /vendas/{id}
    // =========================================================================

    @Test
    @Order(10)
    void cliente_acessaPropriaVendaComoComprador_deveRetornar200() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Comprador", "CLIENTE");
        Long idFuncionario = criarUsuarioComoAdmin("Funcionario Venda", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idCliente, idFuncionario);

        Usuario cliente = carregarUsuario(idCliente);

        // cliente é o comprador da venda → isParticipanteVenda = true → 200
        comAutenticacao(get("/vendas/" + vendaId), cliente)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendaId));
    }

    @Test
    @Order(11)
    void cliente_acessaVendaDeOutroCliente_deveRetornar403() throws Exception {
        Long idClienteA = criarUsuarioComoAdmin("Comprador A", "CLIENTE");
        Long idClienteB = criarUsuarioComoAdmin("Comprador B", "CLIENTE");
        Long idFuncionario = criarUsuarioComoAdmin("Funcionario Venda B", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idClienteA, idFuncionario);

        Usuario clienteB = carregarUsuario(idClienteB);

        // clienteB não é participante da venda de clienteA → 403
        comAutenticacao(get("/vendas/" + vendaId), clienteB)
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(12)
    void vendedor_acessaPropriaVendaComoFuncionario_deveRetornar200() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Da Venda", "CLIENTE");
        Long idVendedor = criarUsuarioComoAdmin("Vendedor Proprio", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idCliente, idVendedor);

        Usuario vendedor = carregarUsuario(idVendedor);

        // vendedor é o funcionario da venda → isParticipanteVenda = true → 200
        comAutenticacao(get("/vendas/" + vendaId), vendedor)
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    void vendedor_acessaVendaDeOutroVendedor_deveRetornar403() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente", "CLIENTE");
        Long idVendedor1 = criarUsuarioComoAdmin("Vendedor 1", "VENDEDOR");
        Long idVendedor2 = criarUsuarioComoAdmin("Vendedor 2", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idCliente, idVendedor1);

        Usuario vendedor2 = carregarUsuario(idVendedor2);

        // vendedor2 não é participante da venda de vendedor1 → 403
        comAutenticacao(get("/vendas/" + vendaId), vendedor2)
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // isVendedorDaVenda — PUT /vendas/{id}
    // =========================================================================

    @Test
    @Order(20)
    void vendedor_editaPropriaVenda_deveRetornar200() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Edit", "CLIENTE");
        Long idVendedor = criarUsuarioComoAdmin("Vendedor Editor", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idCliente, idVendedor);

        Usuario vendedor = carregarUsuario(idVendedor);

        String updateJson = String.format("{\"id\":%d,\"identificacao\":\"PROP-EDITADA-%d\"}", vendaId, System.currentTimeMillis());

        // vendedor é o funcionario → isVendedorDaVenda = true → 200
        comAutenticacao(put("/vendas/" + vendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson), vendedor)
                .andExpect(status().isOk());
    }

    @Test
    @Order(21)
    void vendedor_editaVendaDeOutroVendedor_deveRetornar403() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Edit 2", "CLIENTE");
        Long idVendedor1 = criarUsuarioComoAdmin("Vendedor Original", "VENDEDOR");
        Long idVendedor2 = criarUsuarioComoAdmin("Vendedor Invasor", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idCliente, idVendedor1);

        Usuario vendedor2 = carregarUsuario(idVendedor2);

        String updateJson = String.format("{\"id\":%d,\"identificacao\":\"PROP-INVASAO\"}", vendaId);

        // vendedor2 não é o funcionario → isVendedorDaVenda = false → 403
        comAutenticacao(put("/vendas/" + vendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson), vendedor2)
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(22)
    void cliente_tentaEditarVenda_deveRetornar403() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Comprador Edit", "CLIENTE");
        Long idVendedor = criarUsuarioComoAdmin("Vendedor Da Venda", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idCliente, idVendedor);

        Usuario cliente = carregarUsuario(idCliente);

        String updateJson = String.format("{\"id\":%d,\"identificacao\":\"PROP-CLIENTE-EDIT\"}", vendaId);

        // CLIENTE não tem ADMIN/GERENTE e isVendedorDaVenda retorna false (é o comprador, não o funcionario)
        comAutenticacao(put("/vendas/" + vendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson), cliente)
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // isProprioUsuario — GET /vendas/usuario/{id}/cliente
    // =========================================================================

    @Test
    @Order(30)
    void usuario_listaPropriasPorParticipacaoComoCliente_deveRetornar200() throws Exception {
        Long idA = criarUsuarioComoAdmin("Cliente Próprio", "CLIENTE");
        Long idFuncionario = criarUsuarioComoAdmin("Funcionario Lista", "VENDEDOR");
        criarVendaComoAdmin(idA, idFuncionario);

        Usuario usuarioA = carregarUsuario(idA);

        comAutenticacao(get("/vendas/usuario/" + idA + "/cliente"), usuarioA)
                .andExpect(status().isOk());
    }

    @Test
    @Order(31)
    void usuario_listaVendasDeOutroComoCliente_deveRetornar403() throws Exception {
        Long idA = criarUsuarioComoAdmin("Cliente Lista A", "CLIENTE");
        Long idB = criarUsuarioComoAdmin("Cliente Lista B", "CLIENTE");
        Long idFuncionario = criarUsuarioComoAdmin("Funcionario Lista 2", "VENDEDOR");
        criarVendaComoAdmin(idA, idFuncionario);

        Usuario usuarioB = carregarUsuario(idB);

        // usuarioB tenta ver vendas de usuarioA como cliente → não é o próprio → 403
        comAutenticacao(get("/vendas/usuario/" + idA + "/cliente"), usuarioB)
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(32)
    void vendedor_listaPropriasPorParticipacaoComoFuncionario_deveRetornar200() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Func Lista", "CLIENTE");
        Long idVendedor = criarUsuarioComoAdmin("Vendedor Func Lista", "VENDEDOR");
        criarVendaComoAdmin(idCliente, idVendedor);

        Usuario vendedor = carregarUsuario(idVendedor);

        comAutenticacao(get("/vendas/usuario/" + idVendedor + "/funcionario"), vendedor)
                .andExpect(status().isOk());
    }

    @Test
    @Order(33)
    void vendedor_listaVendasDeOutroComoFuncionario_deveRetornar403() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Func Lista 2", "CLIENTE");
        Long idVendedor1 = criarUsuarioComoAdmin("Vendedor Func 1", "VENDEDOR");
        Long idVendedor2 = criarUsuarioComoAdmin("Vendedor Func 2", "VENDEDOR");
        criarVendaComoAdmin(idCliente, idVendedor1);

        Usuario vendedor2 = carregarUsuario(idVendedor2);

        // vendedor2 tenta ver as vendas de vendedor1 como funcionario → não é o próprio → 403
        comAutenticacao(get("/vendas/usuario/" + idVendedor1 + "/funcionario"), vendedor2)
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // ADMIN — acesso irrestrito (controle de regressão)
    // =========================================================================

    @Test
    @Order(40)
    void admin_acessaQualquerUsuario_deveRetornar200() throws Exception {
        Long idA = criarUsuarioComoAdmin("Qualquer Usuário", "CLIENTE");

        mockMvc.perform(get("/usuarios/" + idA)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idA));
    }

    @Test
    @Order(41)
    void admin_acessaQualquerVenda_deveRetornar200() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Admin", "CLIENTE");
        Long idFuncionario = criarUsuarioComoAdmin("Funcionario Admin", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idCliente, idFuncionario);

        mockMvc.perform(get("/vendas/" + vendaId)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendaId));
    }

    @Test
    @Order(42)
    void admin_editaQualquerVenda_deveRetornar200() throws Exception {
        Long idCliente = criarUsuarioComoAdmin("Cliente Admin Edit", "CLIENTE");
        Long idFuncionario = criarUsuarioComoAdmin("Funcionario Admin Edit", "VENDEDOR");
        Long vendaId = criarVendaComoAdmin(idCliente, idFuncionario);

        String updateJson = String.format("{\"id\":%d,\"identificacao\":\"PROP-ADMIN-EDIT\"}", vendaId);

        mockMvc.perform(put("/vendas/" + vendaId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(43)
    void admin_listaVendasDeQualquerUsuario_deveRetornar200() throws Exception {
        Long idA = criarUsuarioComoAdmin("Cliente Admin Lista", "CLIENTE");
        Long idFuncionario = criarUsuarioComoAdmin("Funcionario Admin Lista", "VENDEDOR");
        criarVendaComoAdmin(idA, idFuncionario);

        mockMvc.perform(get("/vendas/usuario/" + idA + "/cliente")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
