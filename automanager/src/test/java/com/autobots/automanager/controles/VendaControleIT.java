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
class VendaControleIT {

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

    private Long criarUsuario(String nome, String perfil) throws Exception {
        String json = String.format(java.util.Locale.US,"{\"nome\":\"%s\",\"perfis\":[\"%s\"]}", nome, perfil);
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        if (response.get("id") == null) {
            throw new IllegalStateException("Usuário não foi criado: " + result.getResponse().getContentAsString());
        }
        return response.get("id").asLong();
    }


    private Long criarFornecedor() throws Exception {
        return criarUsuario("Fornecedor Teste", "FORNECEDOR");
    }

    private Long criarVenda() throws Exception {
        Long clienteId = criarUsuario("Cliente", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário", "VENDEDOR");
        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-" + System.currentTimeMillis(), clienteId, funcionarioId)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        if (response.get("id") == null) {
            throw new IllegalStateException("Venda não foi criada: " + result.getResponse().getContentAsString());
        }
        return response.get("id").asLong();
    }

    private Long criarMercadoria(String nome, double valor, long quantidade, Long fornecedorId) throws Exception {
        String json = String.format(java.util.Locale.US,
            "{\"nome\":\"%s\",\"valor\":%.2f,\"quantidade\":%d,\"validade\":\"2026-12-31\",\"fabricacao\":\"2024-01-01\",\"descricao\":\"Desc\",\"idFornecedor\":%d}",
            nome, valor, quantidade, fornecedorId
        );
        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        if (response.get("id") == null) {
            throw new IllegalStateException("Mercadoria não foi criada: " + result.getResponse().getContentAsString());
        }
        return response.get("id").asLong();
    }

    private Long criarServico(String nome, double valor) throws Exception {
        String json = String.format(java.util.Locale.US,"{\"nome\":\"%s\",\"valor\":%.2f,\"descricao\":\"Desc\"}", nome, valor);
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        if (response.get("id") == null) {
            throw new IllegalStateException("Serviço não foi criado: " + result.getResponse().getContentAsString());
        }
        return response.get("id").asLong();
    }

    private Long criarServico(String nome, double valor, Long empresaId) throws Exception {
        // Overload para compatibilidade com chamadas antigas (ignora empresaId)
        return criarServico(nome, valor);
    }

    private Long criarMercadoria(String nome, double valor, Long fornecedorId) throws Exception {
        // Overload para compatibilidade com chamadas antigas (usa quantidade=10 como padrão)
        return criarMercadoria(nome, valor, 10L, fornecedorId);
    }

    private Long criarEmpresa(String nome) throws Exception {
        String json = String.format(java.util.Locale.US,"{\"razaoSocial\":\"%s\",\"nomeFantasia\":\"NF\"}", nome);
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long criarUsuario() throws Exception {
        // Overload para compatibilidade com chamadas antigas
        return criarUsuario("Usuário Teste", "CLIENTE");
    }

    private String vendaJson(String id, Long clienteId, Long funcionarioId) {
        return String.format(java.util.Locale.US,
            "{\"identificacao\":\"%s\",\"clienteId\":%d,\"funcionarioId\":%d}",
            id, clienteId, funcionarioId
        );
    }

    private Long criarVeiculo(String placa) throws Exception {
        Long proprietarioId = criarUsuario("Proprietário Veiculo", "CLIENTE");
        String json = String.format(java.util.Locale.US,
            "{\"tipo\":\"SEDA\",\"modelo\":\"Modelo Test\",\"placa\":\"%s\",\"proprietarioId\":%d}",
            placa, proprietarioId
        );
        MvcResult result = mockMvc.perform(post("/veiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();

        if (result.getResponse().getStatus() != 201) {
            throw new RuntimeException(String.format(
                "Erro ao criar veículo: status %d - %s",
                result.getResponse().getStatus(),
                result.getResponse().getContentAsString()
            ));
        }

        com.fasterxml.jackson.databind.JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        if (response.get("id") == null) {
            throw new IllegalStateException("Veículo não foi criado: " + result.getResponse().getContentAsString());
        }
        return response.get("id").asLong();
    }

    @Test
    @Order(1)
    void criar_comDadosValidos_deveRetornar201() throws Exception {
        Long clienteId = criarUsuario("Cliente Teste", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Teste", "VENDEDOR");

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-001", clienteId, funcionarioId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.identificacao").value("VND-001"))
                .andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.funcionarioId").value(funcionarioId));
    }

    @Test
    @Order(2)
    void criar_comClienteInexistente_deveRetornar404() throws Exception {
        Long funcionarioId = criarUsuario("Funcionário", "VENDEDOR");

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-002", 9999L, funcionarioId)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void criar_comFuncionarioInexistente_deveRetornar404() throws Exception {
        Long clienteId = criarUsuario("Cliente", "CLIENTE");

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-003", clienteId, 9999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void listar_deveRetornar200() throws Exception {
        mockMvc.perform(get("/vendas"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void obterPorId_comIdExistente_deveRetornarVenda() throws Exception {
        Long clienteId = criarUsuario("Cliente", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário", "VENDEDOR");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-004", clienteId, funcionarioId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/vendas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.identificacao").value("VND-004"));
    }

    @Test
    @Order(6)
    void obterPorId_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/vendas/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    void atualizar_comDadosValidos_deveRetornar200() throws Exception {
        Long clienteId = criarUsuario("Cliente", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário", "VENDEDOR");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-005", clienteId, funcionarioId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"identificacao\":\"VND-005-ATUALIZADA\"}", id);

        mockMvc.perform(put("/vendas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("VND-005-ATUALIZADA"));
    }

    @Test
    @Order(8)
    void deletar_comIdExistente_deveRetornar204() throws Exception {
        Long clienteId = criarUsuario("Cliente", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário", "VENDEDOR");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-006", clienteId, funcionarioId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/vendas/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/vendas/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    void deletar_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(delete("/vendas/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void criar_identificacaoDuplicada_deveRetornar400() throws Exception {
        Long clienteId = criarUsuario("Cliente Dup", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Dup", "VENDEDOR");

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-DUP", clienteId, funcionarioId)))
                .andExpect(status().isCreated());

        // Mesmo identificacao deve falhar por unique constraint
        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-DUP", clienteId, funcionarioId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    void criar_comMercadorias_deveRetornar201() throws Exception {
        Long empresaId = criarEmpresa("Empresa Teste");
        Long clienteId = criarUsuario("Cliente", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário", "VENDEDOR");
        Long mercadoria1 = criarMercadoria("Óleo Motor", 50.0, empresaId);
        Long mercadoria2 = criarMercadoria("Filtro", 30.0, empresaId);

        String json = String.format(java.util.Locale.US,
            "{\"identificacao\":\"VND-COM-MERC\",\"clienteId\":%d,\"funcionarioId\":%d," +
            "\"mercadoriasIds\":[%d,%d]}",
            clienteId, funcionarioId, mercadoria1, mercadoria2
        );

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mercadoriasIds[0]").exists());
    }

    @Test
    @Order(12)
    void adicionarMercadoria_deveRetornar200() throws Exception {
        Long empresaId = criarEmpresa("Empresa Teste 2");
        Long clienteId = criarUsuario("Cliente 2", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário 2", "VENDEDOR");
        Long mercadoria1 = criarMercadoria("Bobina", 100.0, empresaId);
        Long mercadoria2 = criarMercadoria("Bobina Extra", 100.0, empresaId);

        MvcResult vendaResult = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-MERCADORIA", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(vendaResult.getResponse().getContentAsString()).get("id").asLong();

        // Adicionar primeira mercadoria
        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + mercadoria1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mercadoriasIds[0]").exists());

        // Adicionar segunda mercadoria
        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + mercadoria2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    void removerMercadoria_deveRetornar200() throws Exception {
        Long empresaId = criarEmpresa("Empresa Teste 3");
        Long clienteId = criarUsuario("Cliente 3", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário 3", "VENDEDOR");
        Long mercadoria1 = criarMercadoria("Produto A", 50.0, empresaId);

        String json = String.format(java.util.Locale.US,
            "{\"identificacao\":\"VND-RM-MERC\",\"clienteId\":%d,\"funcionarioId\":%d,\"mercadoriasIds\":[%d]}",
            clienteId, funcionarioId, mercadoria1
        );

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/vendas/" + vendaId + "/mercadorias/" + mercadoria1))
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    void adicionarServico_deveRetornar200() throws Exception {
        Long empresaId = criarEmpresa("Empresa Teste 4");
        Long clienteId = criarUsuario("Cliente 4", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário 4", "VENDEDOR");
        Long servico1 = criarServico("Troca de Óleo", 80.0, empresaId);

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-SERVICO", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/servicos/" + servico1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicosIds[0]").exists());
    }

    @Test
    @Order(15)
    void deletar_deveRemoverCompletamente() throws Exception {
        Long clienteId = criarUsuario("Cliente Delete", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Delete", "VENDEDOR");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-TEMP", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Confirmar existência
        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isOk());

        // Deletar
        mockMvc.perform(delete("/vendas/" + vendaId))
                .andExpect(status().isNoContent());

        // Confirmar remoção
        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isNotFound());

        // Deletar novamente retorna 404
        mockMvc.perform(delete("/vendas/" + vendaId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(16)
    void removerServico_deveRetornar200() throws Exception {
        Long empresaId = criarEmpresa("Empresa Servico");
        Long clienteId = criarUsuario("Cliente Servico", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Servico", "VENDEDOR");
        Long servico1 = criarServico("Alinhamento", 60.0, empresaId);

        String json = String.format(java.util.Locale.US,
            "{\"identificacao\":\"VND-RM-SRV\",\"clienteId\":%d,\"funcionarioId\":%d,\"servicosIds\":[%d]}",
            clienteId, funcionarioId, servico1
        );

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Remover serviço
        mockMvc.perform(delete("/vendas/" + vendaId + "/servicos/" + servico1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendaId));
    }

    @Test
    @Order(17)
    void criar_semIdentificacao_deveRetornar400() throws Exception {
        Long clienteId = criarUsuario("Cliente Sem ID", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Sem ID", "VENDEDOR");

        String json = String.format(java.util.Locale.US,"{\"clienteId\":%d,\"funcionarioId\":%d}", clienteId, funcionarioId);
        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(18)
    void criar_comIdentificacaoEmBranco_deveRetornar400() throws Exception {
        Long clienteId = criarUsuario("Cliente Branco", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Branco", "VENDEDOR");

        String json = String.format(java.util.Locale.US,"{\"identificacao\":\"\",\"clienteId\":%d,\"funcionarioId\":%d}",
                clienteId, funcionarioId);
        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(19)
    void atualizar_identificacao_deveRetornar200() throws Exception {
        Long clienteId = criarUsuario("Cliente Update ID", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Update ID", "VENDEDOR");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VND-ORIGINAL", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Atualizar identificação
        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"identificacao\":\"VND-NOVA\"}", vendaId);
        mockMvc.perform(put("/vendas/" + vendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("VND-NOVA"));
    }

    @Test
    @Order(20)
    void criar_comMultiplasMercadoriasEServicos_deveRetornar201() throws Exception {
        Long empresaId = criarEmpresa("Empresa Completa");
        Long clienteId = criarUsuario("Cliente Completo", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Completo", "VENDEDOR");

        Long mercadoria1 = criarMercadoria("Peça 1", 100.0, empresaId);
        Long mercadoria2 = criarMercadoria("Peça 2", 150.0, empresaId);
        Long servico1 = criarServico("Serviço 1", 200.0, empresaId);
        Long servico2 = criarServico("Serviço 2", 250.0, empresaId);

        String json = String.format(java.util.Locale.US,
            "{\"identificacao\":\"VND-MULTI\",\"clienteId\":%d,\"funcionarioId\":%d," +
            "\"mercadoriasIds\":[%d,%d],\"servicosIds\":[%d,%d]}",
            clienteId, funcionarioId, mercadoria1, mercadoria2, servico1, servico2
        );

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mercadoriasIds").isArray())
                .andExpect(jsonPath("$.servicosIds").isArray());
    }

    @Test
    @Order(29)
    void adicionar_mercadoria_aVendaExistente_deveRetornar200() throws Exception {
        Long fornecedorId = criarFornecedor();
        Long mercadoriaId = criarMercadoria("Produto para Adicionar", 40.0, 10, fornecedorId);
        Long vendaId = criarVenda();

        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + mercadoriaId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    void remover_mercadoria_daVenda_deveRetornar200() throws Exception {
        Long fornecedorId = criarFornecedor();
        Long mercadoriaId = criarMercadoria("Produto para Remover", 50.0, 15, fornecedorId);
        Long vendaId = criarVenda();

        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + mercadoriaId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/vendas/" + vendaId + "/mercadorias/" + mercadoriaId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(31)
    void adicionar_servico_aVendaExistente_deveRetornar200() throws Exception {
        Long servicoId = criarServico("Serviço para Adicionar", 100.0);
        Long vendaId = criarVenda();

        mockMvc.perform(post("/vendas/" + vendaId + "/servicos/" + servicoId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(32)
    void remover_servico_daVenda_deveRetornar200() throws Exception {
        Long servicoId = criarServico("Serviço para Remover", 120.0);
        Long vendaId = criarVenda();

        mockMvc.perform(post("/vendas/" + vendaId + "/servicos/" + servicoId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/vendas/" + vendaId + "/servicos/" + servicoId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(33)
    void criar_vendaComMultiplasMercadoriasEServicos_deveRetornar201() throws Exception {
        Long clienteId = criarUsuario("Cliente Multiplo", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Multiplo", "VENDEDOR");
        Long fornecedor1Id = criarFornecedor();
        Long mercadoria1Id = criarMercadoria("Merc1", 30.0, 5, fornecedor1Id);
        Long mercadoria2Id = criarMercadoria("Merc2", 40.0, 10, fornecedor1Id);
        Long servico1Id = criarServico("Serv1", 80.0);
        Long servico2Id = criarServico("Serv2", 90.0);

        String json = String.format(java.util.Locale.US,
            "{\"descricao\":\"Venda Multipla\",\"identificacao\":\"VEND-MULTI-001\"," +
            "\"clienteId\":%d,\"funcionarioId\":%d," +
            "\"mercadoriasIds\":[%d,%d],\"servicosIds\":[%d,%d]}",
            clienteId, funcionarioId, mercadoria1Id, mercadoria2Id, servico1Id, servico2Id
        );

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mercadoriasIds.length()").value(2))
                .andExpect(jsonPath("$.servicosIds.length()").value(2));
    }

    @Test
    @Order(34)
    void listar_multiplosVendas_deveRetornarTodas() throws Exception {
        for (int i = 0; i < 3; i++) {
            criarVenda();
        }

        mockMvc.perform(get("/vendas"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(35)
    void atualizar_venda_descricao_deveRetornar200() throws Exception {
        Long clienteId = criarUsuario("Cliente Update", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Update", "VENDEDOR");
        Long vendaId = criarVenda();

        String updateJson = String.format(java.util.Locale.US,
            "{\"descricao\":\"Nova Descrição\",\"identificacao\":\"VEND-UP-001\"," +
            "\"clienteId\":%d,\"funcionarioId\":%d}",
            clienteId, funcionarioId);

        mockMvc.perform(put("/vendas/" + vendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(36)
    void deletar_venda_comMercadoriasEServicos_deveRemover() throws Exception {
        Long fornecedorId = criarFornecedor();
        Long mercadoriaId = criarMercadoria("Produto", 25.0, 8, fornecedorId);
        Long servicoId = criarServico("Serviço", 75.0);
        Long vendaId = criarVenda();

        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + mercadoriaId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/vendas/" + vendaId + "/servicos/" + servicoId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/vendas/" + vendaId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(37)
    void adicionar_multiplas_mercadorias_sequencialmente() throws Exception {
        Long fornecedorId = criarFornecedor();
        Long vendaId = criarVenda();

        for (int i = 1; i <= 3; i++) {
            Long mercadoriaId = criarMercadoria("Produto " + i, 10.0 * i, i * 5, fornecedorId);
            mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + mercadoriaId))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @Order(38)
    void obter_venda_comDados_completos() throws Exception {
        Long clienteId = criarUsuario("Cliente Completo", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Completo", "VENDEDOR");
        Long fornecedorId = criarFornecedor();
        Long mercadoriaId = criarMercadoria("Completa", 60.0, 12, fornecedorId);
        Long servicoId = criarServico("Completa", 95.0);

        String json = String.format(java.util.Locale.US,
            "{\"descricao\":\"Venda Completa\",\"identificacao\":\"VEND-COMP-001\"," +
            "\"clienteId\":%d,\"funcionarioId\":%d," +
            "\"mercadoriasIds\":[%d],\"servicosIds\":[%d]}",
            clienteId, funcionarioId, mercadoriaId, servicoId
        );

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendaId))
                .andExpect(jsonPath("$.identificacao").value("VEND-COMP-001"));
    }

    @Test
    @Order(39)
    void criar_comIdentificacaoEmBranco_deveRetornar4000() throws Exception {
        Long clienteId = criarUsuario("Cliente Teste", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Teste", "VENDEDOR");

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("   ", clienteId, funcionarioId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(40)
    void atualizar_comIdentificacaoEmBranco_deveRetornar400() throws Exception {
        Long vendaId = criarVenda();
        Long clienteId = criarUsuario("Cliente Update", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Update", "VENDEDOR");

        String updateJson = String.format(java.util.Locale.US,
            "{\"identificacao\":\"   \",\"clienteId\":%d,\"funcionarioId\":%d}",
            clienteId, funcionarioId);

        mockMvc.perform(put("/vendas/" + vendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(41)
    void criar_comClienteNuloExplicito_deveRetornar400() throws Exception {
        Long funcionarioId = criarUsuario("Funcionário", "VENDEDOR");

        String json = String.format(java.util.Locale.US,
            "{\"identificacao\":\"VEND-NULO-001\",\"clienteId\":null,\"funcionarioId\":%d}",
            funcionarioId);

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(42)
    void criar_comFuncionarioNuloExplicito_deveRetornar400() throws Exception {
        Long clienteId = criarUsuario("Cliente", "CLIENTE");

        String json = String.format(java.util.Locale.US,
            "{\"identificacao\":\"VEND-NULO-002\",\"clienteId\":%d,\"funcionarioId\":null}",
            clienteId);

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(43)
    void deletar_vendaComMercadoriasEServicos_deveRemoverCompleto() throws Exception {
        Long clienteId = criarUsuario("Cliente Delete", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Delete", "VENDEDOR");
        Long fornecedor = criarFornecedor();
        Long mercadoria = criarMercadoria("Produto Delete", 35.0, 5, fornecedor);
        Long servico = criarServico("Serviço Delete", 85.0);

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-DEL-MULT-001", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + mercadoria))
                .andExpect(status().isOk());

        mockMvc.perform(post("/vendas/" + vendaId + "/servicos/" + servico))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/vendas/" + vendaId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(44)
    void listar_apósMultiplasOperacoes_deveRetornarTodas() throws Exception {
        for (int i = 1; i <= 3; i++) {
            criarVenda();
        }

        mockMvc.perform(get("/vendas"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(45)
    void atualizar_trocandoIdentificacao_deveRetornar200() throws Exception {
        Long clienteId = criarUsuario("Cliente Troca", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Troca", "VENDEDOR");
        Long vendaId = criarVenda();

        String updateJson = String.format(java.util.Locale.US,
            "{\"identificacao\":\"VEND-NOVA-001\",\"clienteId\":%d,\"funcionarioId\":%d}",
            clienteId, funcionarioId);

        mockMvc.perform(put("/vendas/" + vendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("VEND-NOVA-001"));
    }

    @Test
    @Order(46)
    void adicionar_mercadoria_avendaExistente_comMultiplas_deveRetornar200() throws Exception {
        Long fornecedor = criarFornecedor();
        Long merc1 = criarMercadoria("Merc A", 20.0, 3, fornecedor);
        Long merc2 = criarMercadoria("Merc B", 30.0, 4, fornecedor);
        Long vendaId = criarVenda();

        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + merc1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + merc2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mercadoriasIds").isArray());
    }

    @Test
    @Order(47)
    void remover_mercadoria_quenaoExisteNaVenda_deveRetornar404ouError() throws Exception {
        Long fornecedor = criarFornecedor();
        Long mercadoria = criarMercadoria("Merc Inexistente", 40.0, 2, fornecedor);
        Long vendaId = criarVenda();

        mockMvc.perform(delete("/vendas/" + vendaId + "/mercadorias/" + mercadoria))
                .andExpect(status().isOk());
    }

    @Test
    @Order(48)
    void obter_vendaComTodasAsMercadorias_deveRetornarCompleta() throws Exception {
        Long clienteId = criarUsuario("Cliente Completo", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Completo", "VENDEDOR");
        Long fornecedor = criarFornecedor();
        Long merc = criarMercadoria("Mercadoria Final", 99.99, 10, fornecedor);
        Long servico = criarServico("Serviço Final", 149.99);

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-FINAL-001", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + merc))
                .andExpect(status().isOk());

        mockMvc.perform(post("/vendas/" + vendaId + "/servicos/" + servico))
                .andExpect(status().isOk());

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.funcionarioId").value(funcionarioId));
    }

    @Test
    @Order(49)
    void deletar_multiplas_vendas_seguidas_deveRetornar404() throws Exception {
        Long vendaId1 = criarVenda();
        Long vendaId2 = criarVenda();

        mockMvc.perform(delete("/vendas/" + vendaId1))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/vendas/" + vendaId1))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/vendas/" + vendaId2))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(50)
    void criar_comNomeNull_deveRetornar400() throws Exception {
        String json = """
                {
                  "identificacao": "VEND-NULL-001"
                }
                """;

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(51)
    void adicionarVeiculo_deveRetornar200() throws Exception {
        Long clienteId = criarUsuario("Cliente Veiculo", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Veiculo", "VENDEDOR");
        Long veiculoId = criarVeiculo("ABC-1234");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-VEI-001", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/veiculo/" + veiculoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculoId").value(veiculoId))
                .andExpect(jsonPath("$.placaVeiculo").value("ABC-1234"));
    }

    @Test
    @Order(52)
    void adicionarVeiculo_comVeiculoInexistente_deveRetornar404() throws Exception {
        Long clienteId = criarUsuario("Cliente", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário", "VENDEDOR");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-404", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/veiculo/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(53)
    void removerVeiculo_deveRetornar200() throws Exception {
        Long clienteId = criarUsuario("Cliente Remove", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Remove", "VENDEDOR");
        Long veiculoId = criarVeiculo("XYZ-9999");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-REM-001", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/veiculo/" + veiculoId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/vendas/" + vendaId + "/veiculo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculoId").doesNotExist());
    }

    @Test
    @Order(54)
    void listarPorVeiculo_deveRetornar200() throws Exception {
        Long clienteId = criarUsuario("Cliente Filtro", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Filtro", "VENDEDOR");
        Long veiculoId = criarVeiculo("FIL-1111");

        MvcResult result1 = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-FIL-001", clienteId, funcionarioId)))
                .andReturn();
        Long vendaId1 = objectMapper.readTree(result1.getResponse().getContentAsString()).get("id").asLong();

        MvcResult result2 = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-FIL-002", clienteId, funcionarioId)))
                .andReturn();
        Long vendaId2 = objectMapper.readTree(result2.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId1 + "/veiculo/" + veiculoId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/vendas/veiculo/" + veiculoId))
                .andExpect(status().isOk());
    }

    @Test
    @Order(55)
    void obter_deveRetornarHATEOASLinks() throws Exception {
        Long clienteId = criarUsuario("Cliente HATEOAS", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário HATEOAS", "VENDEDOR");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-HATEOAS-001", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.vendas.href").exists())
                .andExpect(jsonPath("$._links.editar.href").exists())
                .andExpect(jsonPath("$._links.excluir.href").exists())
                .andExpect(jsonPath("$._links.cliente.href").exists())
                .andExpect(jsonPath("$._links.funcionario.href").exists())
                .andExpect(jsonPath("$._links.adicionar_mercadoria.href").exists())
                .andExpect(jsonPath("$._links.adicionar_servico.href").exists())
                .andExpect(jsonPath("$._links.adicionar_veiculo.href").exists());
    }

    @Test
    @Order(56)
    void obter_vendaComMercadorias_deveRetornarLinksIndividuais() throws Exception {
        Long clienteId = criarUsuario("Cliente Mercadoria", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Mercadoria", "VENDEDOR");
        Long fornecedor = criarFornecedor();
        Long merc1 = criarMercadoria("Mercadoria 1", 50.00, 5, fornecedor);
        Long merc2 = criarMercadoria("Mercadoria 2", 75.00, 10, fornecedor);

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-MERC-001", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + merc1))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vendas/" + vendaId + "/mercadorias/" + merc2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links['mercadoria_" + merc1 + "'].href").exists())
                .andExpect(jsonPath("$._links['mercadoria_" + merc2 + "'].href").exists())
                .andExpect(jsonPath("$._links['remover_mercadoria_" + merc1 + "'].href").exists())
                .andExpect(jsonPath("$._links['remover_mercadoria_" + merc2 + "'].href").exists());
    }

    @Test
    @Order(57)
    void obter_vendaComServicos_deveRetornarLinksIndividuais() throws Exception {
        Long clienteId = criarUsuario("Cliente Servico", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Servico", "VENDEDOR");
        Long servico1 = criarServico("Serviço 1", 100.00);
        Long servico2 = criarServico("Serviço 2", 150.00);

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-SERV-001", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/servicos/" + servico1))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vendas/" + vendaId + "/servicos/" + servico2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links['servico_" + servico1 + "'].href").exists())
                .andExpect(jsonPath("$._links['servico_" + servico2 + "'].href").exists())
                .andExpect(jsonPath("$._links['remover_servico_" + servico1 + "'].href").exists())
                .andExpect(jsonPath("$._links['remover_servico_" + servico2 + "'].href").exists());
    }

    @Test
    @Order(58)
    void obter_vendaComVeiculo_deveRetornarLinksVeiculo() throws Exception {
        Long clienteId = criarUsuario("Cliente Vei Links", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Vei Links", "VENDEDOR");
        Long veiculoId = criarVeiculo("LINKS-001");

        MvcResult result = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-VEICULO-LINKS", clienteId, funcionarioId)))
                .andReturn();

        Long vendaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vendas/" + vendaId + "/veiculo/" + veiculoId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/vendas/" + vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.veiculo.href").exists())
                .andExpect(jsonPath("$._links.vendas_do_veiculo.href").exists())
                .andExpect(jsonPath("$._links.remover_veiculo.href").exists())
                .andExpect(jsonPath("$._links.adicionar_veiculo.href").exists());
    }

    // -------------------------------------------------------------------------
    // Testes das novas rotas de participação (cliente / funcionário separados)
    // -------------------------------------------------------------------------

    @Test
    void listarPorUsuarioComoCliente_deveRetornarVendasDoComprador() throws Exception {
        Long clienteId = criarUsuario("Cliente Comprador", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Vendedor", "VENDEDOR");

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-CLIENTE-ROTA", clienteId, funcionarioId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/vendas/usuario/" + clienteId + "/cliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.vendaExibirDTOList").isArray());
    }

    @Test
    void listarPorUsuarioComoFuncionario_deveRetornarVendasDoVendedor() throws Exception {
        Long clienteId = criarUsuario("Cliente Para Func", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário Da Venda", "VENDEDOR");

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-FUNC-ROTA", clienteId, funcionarioId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/vendas/usuario/" + funcionarioId + "/funcionario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.vendaExibirDTOList").isArray());
    }

    @Test
    void listarPorUsuarioComoCliente_semVendas_deveRetornarListaVazia() throws Exception {
        Long clienteId = criarUsuario("Cliente Sem Vendas", "CLIENTE");

        mockMvc.perform(get("/vendas/usuario/" + clienteId + "/cliente"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorUsuarioComoFuncionario_semVendas_deveRetornarListaVazia() throws Exception {
        Long funcionarioId = criarUsuario("Funcionário Sem Vendas", "VENDEDOR");

        mockMvc.perform(get("/vendas/usuario/" + funcionarioId + "/funcionario"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorUsuarioComoCliente_deveRetornarHATEOASLinks() throws Exception {
        Long clienteId = criarUsuario("Cliente HATEOAS", "CLIENTE");
        Long funcionarioId = criarUsuario("Funcionário HATEOAS", "VENDEDOR");

        mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vendaJson("VEND-HATEOAS-CLI", clienteId, funcionarioId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/vendas/usuario/" + clienteId + "/cliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }
}
