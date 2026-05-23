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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MercadoriaControleIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long criarFornecedor() throws Exception {
        String json = "{\"nome\":\"Fornecedor Teste\",\"perfis\":[\"FUNCIONARIO\"]}";
        MvcResult result = mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String mercadoriaJson(String nome, double valor, long qtd, Long idFornecedor) {
        return String.format(java.util.Locale.US,
            "{\"nome\":\"%s\",\"valor\":%.2f,\"quantidade\":%d,\"validade\":\"2026-12-31\"," +
            "\"fabricacao\":\"2024-01-01\",\"descricao\":\"Desc\",\"idFornecedor\":%d}",
            nome, valor, qtd, idFornecedor
        );
    }

    @Test
    @Order(1)
    void criar_comDadosValidos_deveRetornar201() throws Exception {
        Long fornecedorId = criarFornecedor();

        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Oleo Motor 5W30", 45.90, 10, fornecedorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Oleo Motor 5W30"))
                .andExpect(jsonPath("$.valor").value(45.90))
                .andExpect(jsonPath("$.quantidade").value(10));
    }

    @Test
    @Order(2)
    void criar_semFornecedor_deveRetornar400() throws Exception {
        String json = "{\"nome\":\"Filtro\",\"valor\":20.0,\"quantidade\":5," +
                      "\"validade\":\"2026-12-31\",\"fabricacao\":\"2024-01-01\"}";
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void criar_semNome_deveRetornar400() throws Exception {
        Long fornecedorId = criarFornecedor();
        String json = String.format(java.util.Locale.US,
            "{\"valor\":20.0,\"quantidade\":5,\"validade\":\"2026-12-31\"," +
            "\"fabricacao\":\"2024-01-01\",\"idFornecedor\":%d}", fornecedorId);
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void criar_comValorNegativo_deveRetornar400() throws Exception {
        Long fornecedorId = criarFornecedor();
        String json = String.format(java.util.Locale.US,
            "{\"nome\":\"Produto\",\"valor\":-1.0,\"quantidade\":5,\"validade\":\"2026-12-31\"," +
            "\"fabricacao\":\"2024-01-01\",\"idFornecedor\":%d}", fornecedorId);
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void listar_deveRetornar200() throws Exception {
        mockMvc.perform(get("/mercadorias"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void obterPorId_comIdExistente_deveRetornarMercadoria() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Filtro Ar", 25.0, 5, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/mercadorias/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Filtro Ar"));
    }

    @Test
    @Order(7)
    void obterPorId_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/mercadorias/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void atualizar_comDadosValidos_deveRetornar200() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Produto Original", 30.0, 10, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Produto Atualizado\",\"valor\":35.0,\"quantidade\":20," +
            "\"validade\":\"2027-01-01\",\"fabricacao\":\"2024-06-01\"}", id);

        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto Atualizado"));
    }

    @Test
    @Order(9)
    void deletar_comIdExistente_deveRetornar204() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Para Deletar", 10.0, 1, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/mercadorias/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mercadorias/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void deletar_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(delete("/mercadorias/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    void listarVendasDaMercadoria_semVendas_deveRetornarLista() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Produto Sem Venda", 15.0, 3, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/mercadorias/" + id + "/vendas"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    void criar_comQuantidadeNegativa_deveRetornar400() throws Exception {
        Long fornecedorId = criarFornecedor();
        String json = String.format(java.util.Locale.US,
            "{\"nome\":\"Produto\",\"valor\":20.0,\"quantidade\":-5,\"validade\":\"2026-12-31\"," +
            "\"fabricacao\":\"2024-01-01\",\"idFornecedor\":%d}", fornecedorId);
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(13)
    void deletar_deveRemoverCompletamente() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria Temporária", 50.0, 5, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Confirmar existência
        mockMvc.perform(get("/mercadorias/" + id))
                .andExpect(status().isOk());

        // Deletar
        mockMvc.perform(delete("/mercadorias/" + id))
                .andExpect(status().isNoContent());

        // Confirmar remoção
        mockMvc.perform(get("/mercadorias/" + id))
                .andExpect(status().isNotFound());

        // Deletar novamente retorna 404
        mockMvc.perform(delete("/mercadorias/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(14)
    void criar_comQuantidadeZero_deveAceitar() throws Exception {
        Long fornecedorId = criarFornecedor();
        String json = String.format(java.util.Locale.US,
            "{\"nome\":\"Produto Esgotado\",\"valor\":25.0,\"quantidade\":0,\"validade\":\"2026-12-31\"," +
            "\"fabricacao\":\"2024-01-01\",\"descricao\":\"Esgotado\",\"idFornecedor\":%d}", fornecedorId);
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidade").value(0));
    }

    @Test
    @Order(15)
    void atualizar_aumentarQuantidade_deveRetornar200() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Produto Stock", 30.0, 10, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Aumentar quantidade para 50
        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Produto Stock\",\"valor\":30.0,\"quantidade\":50}", id);
        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(50));
    }

    @Test
    @Order(16)
    void atualizar_comValorZero_deveRetornar400() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Produto Original", 30.0, 10, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Produto\",\"valor\":0.0,\"quantidade\":10}", id);
        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(17)
    void criar_multiplos_deveAceitar() throws Exception {
        Long fornecedorId = criarFornecedor();

        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/mercadorias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mercadoriaJson("Produto " + i, 10.0 * i, i, fornecedorId)))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @Order(18)
    void listar_comMultiplasMercadorias_deveRetornarTodas() throws Exception {
        Long fornecedorId = criarFornecedor();

        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Merc 1", 50.0, 10, fornecedorId)));
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Merc 2", 60.0, 20, fornecedorId)));

        mockMvc.perform(get("/mercadorias"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(19)
    void atualizar_diminuirQuantidade_deveRetornar200() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Estoque Alto", 30.0, 100, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Estoque Alto\",\"valor\":30.0,\"quantidade\":50}", id);
        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(50));
    }

    @Test
    @Order(20)
    void criar_comValorMuitoAlto_deveAceitar() throws Exception {
        Long fornecedorId = criarFornecedor();

        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Produto Caro", 99999.99, 1, fornecedorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(99999.99));
    }

    @Test
    @Order(21)
    void deletar_eVerificarAusencia_comMultiplasTentativas() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Para Verificar", 20.0, 5, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/mercadorias/" + id))
                .andExpect(status().isNoContent());

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/mercadorias/" + id))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @Order(22)
    void atualizar_comValorMenorQueMinimoValido_deveRetornar400() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Original", 30.0, 10, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Atualizado\",\"valor\":0.01,\"quantidade\":10}", id);
        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(23)
    void criar_comDescricaoLonga_deveAceitar() throws Exception {
        Long fornecedorId = criarFornecedor();
        String descricaoLonga = "Descrição ".repeat(20);

        String json = String.format(java.util.Locale.US,
            "{\"nome\":\"Produto Long Desc\",\"valor\":40.0,\"quantidade\":15,\"validade\":\"2026-12-31\"," +
            "\"fabricacao\":\"2024-01-01\",\"descricao\":\"%s\",\"idFornecedor\":%d}",
            descricaoLonga, fornecedorId);

        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(24)
    void obterPorId_comMercadoriaValida_deveRetornarDados() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult createResult = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria Teste", 55.0, 25, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/mercadorias/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Mercadoria Teste"))
                .andExpect(jsonPath("$.valor").value(55.0))
                .andExpect(jsonPath("$.quantidade").value(25));
    }

    @Test
    @Order(26)
    void atualizar_comNomeEmBranco_deveRetornar400() throws Exception {
        Long fornecedorId = criarFornecedor();
        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria Original", 75.0, 30, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"   \",\"valor\":80.0,\"quantidade\":35}", id);
        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(27)
    void atualizar_comQuantidadeNegativaAposPositiva_deveRetornar400() throws Exception {
        Long fornecedorId = criarFornecedor();
        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria Estoque", 100.0, 50, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Mercadoria Estoque\",\"valor\":100.0,\"quantidade\":-5}", id);
        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(28)
    void criar_comDescricaoMuitoLonga_deveAceitar() throws Exception {
        Long fornecedorId = criarFornecedor();
        String descricaoLonga = "Descrição ".repeat(100);
        String json = String.format(java.util.Locale.US,
            "{\"nome\":\"Mercadoria Descritiva\",\"valor\":90.0,\"quantidade\":20,\"validade\":\"2026-12-31\",\"fabricacao\":\"2024-01-01\",\"descricao\":\"%s\",\"idFornecedor\":%d}",
            descricaoLonga, fornecedorId);
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(29)
    void atualizar_trocandoFornecedor_deveRetornar200() throws Exception {
        Long fornecedor1 = criarFornecedor();
        Long fornecedor2 = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria Transferivel", 60.0, 15, fornecedor1)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Mercadoria Transferivel\",\"valor\":60.0,\"quantidade\":15,\"idFornecedor\":%d}", id, fornecedor2);
        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    void deletar_multiplas_emSequencia_deveRetornar404NaSegunda() throws Exception {
        Long fornecedorId = criarFornecedor();
        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria Seq", 45.0, 10, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/mercadorias/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/mercadorias/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(31)
    void criar_comValorComMuitasCasasDecimais_deveAceitar() throws Exception {
        Long fornecedorId = criarFornecedor();
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Precisão", 123.456, 5, fornecedorId)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(32)
    void atualizar_alterandoTodosOsCampos_deveRetornar200() throws Exception {
        Long fornecedorId = criarFornecedor();
        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Original", 50.0, 10, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Atualizada\",\"valor\":150.0,\"quantidade\":50,\"descricao\":\"Nova descrição\"}", id);
        mockMvc.perform(put("/mercadorias/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Atualizada"))
                .andExpect(jsonPath("$.valor").value(150.0))
                .andExpect(jsonPath("$.quantidade").value(50));
    }

    @Test
    @Order(33)
    void listar_aposMultiplasCriacoesDeletacoes_deveRetornarApenas() throws Exception {
        Long fornecedor = criarFornecedor();
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/mercadorias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mercadoriaJson("Merc " + i, 10.0 * i, i * 10, fornecedor)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/mercadorias"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(34)
    void obterPorId_deveRetornarDadosCompletos_comValidity() throws Exception {
        Long fornecedorId = criarFornecedor();
        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria Completa", 199.99, 100, fornecedorId)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/mercadorias/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Mercadoria Completa"))
                .andExpect(jsonPath("$.valor").value(199.99))
                .andExpect(jsonPath("$.quantidade").value(100));
    }

    @Test
    @Order(35)
    void criar_comQuantidadeMaxima_deveAceitar() throws Exception {
        Long fornecedorId = criarFornecedor();
        mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria Volumosa", 25.0, 999999, fornecedorId)))
                .andExpect(status().isCreated());
    }

    @Test
    void obter_mercadoria_deveRetornarHATEOASLinks() throws Exception {
        Long fornecedorId = criarFornecedor();

        MvcResult result = mockMvc.perform(post("/mercadorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mercadoriaJson("Mercadoria HATEOAS", 50.0, 10, fornecedorId)))
                .andReturn();

        Long mercadoriaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/mercadorias/" + mercadoriaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.mercadorias.href").exists())
                .andExpect(jsonPath("$._links.editar.href").exists())
                .andExpect(jsonPath("$._links.excluir.href").exists())
                .andExpect(jsonPath("$._links.vendas.href").exists())
                .andExpect(jsonPath("$._links.fornecedor.href").exists())
                .andExpect(jsonPath("$._links.mercadorias_do_fornecedor.href").exists());
    }
}
