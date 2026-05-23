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
class ServicoControleIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long criarEmpresa() throws Exception {
        String empresaJson = "{\"razaoSocial\":\"Empresa Servicos\",\"nomeFantasia\":\"ES\"}";
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String servicoJson(String nome, double valor, String desc, Long idEmpresa) {
        return String.format(java.util.Locale.US,
            "{\"nome\":\"%s\",\"valor\":%.2f,\"descricao\":\"%s\",\"idEmpresa\":%s}",
            nome, valor, desc, idEmpresa != null ? idEmpresa : "null"
        );
    }

    @Test
    @Order(1)
    void criar_comDadosValidos_deveRetornar201() throws Exception {
        Long empresaId = criarEmpresa();

        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson("Troca de Oleo", 80.0, "Troca completa", empresaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Troca de Oleo"))
                .andExpect(jsonPath("$.valor").value(80.0));
    }

    @Test
    @Order(2)
    void criar_semEmpresa_deveRetornar201() throws Exception {
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Alinhamento\",\"valor\":60.0,\"descricao\":\"Alinhamento de rodas\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Alinhamento"));
    }

    @Test
    @Order(3)
    void listar_deveRetornar200() throws Exception {
        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void obterPorId_comIdExistente_deveRetornarServico() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Balanceamento\",\"valor\":50.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/servicos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Balanceamento"));
    }

    @Test
    @Order(5)
    void obterPorId_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/servicos/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void atualizar_comDadosValidos_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Revisão\",\"valor\":150.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"Revisão Completa\",\"valor\":200.0}", id);

        mockMvc.perform(put("/servicos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Revisão Completa"))
                .andExpect(jsonPath("$.valor").value(200.0));
    }

    @Test
    @Order(7)
    void deletar_comIdExistente_deveRetornar204() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Servico Temp\",\"valor\":10.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/servicos/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/servicos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void deletar_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(delete("/servicos/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    void listarVendasDoServico_comServicoSemVendas_deveRetornarListaVazia() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Polimento\",\"valor\":120.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/servicos/" + id + "/vendas"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void criar_semNome_deveRetornar400() throws Exception {
        Long empresaId = criarEmpresa();
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":50.0,\"descricao\":\"Sem nome\",\"idEmpresa\":" + empresaId + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    void criar_comValorNegativo_deveRetornar400() throws Exception {
        Long empresaId = criarEmpresa();
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson("Serviço Inválido", -50.0, "Valor negativo", empresaId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(12)
    void criar_comValorZero_deveRetornar400() throws Exception {
        Long empresaId = criarEmpresa();
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson("Serviço Grátis", 0.0, "Valor zero", empresaId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(13)
    void deletar_deveRemoverCompletamente() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço Temporário\",\"valor\":75.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Confirmar existência
        mockMvc.perform(get("/servicos/" + id))
                .andExpect(status().isOk());

        // Deletar
        mockMvc.perform(delete("/servicos/" + id))
                .andExpect(status().isNoContent());

        // Confirmar remoção
        mockMvc.perform(get("/servicos/" + id))
                .andExpect(status().isNotFound());

        // Deletar novamente retorna 404
        mockMvc.perform(delete("/servicos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(14)
    void atualizar_comNomeEmBranco_deveRetornar400() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Original\",\"valor\":100.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"\",\"valor\":150.0}", id);
        mockMvc.perform(put("/servicos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(15)
    void atualizar_comValorNegativo_deveRetornar400() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Original\",\"valor\":100.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"Atualizado\",\"valor\":-50.0}", id);
        mockMvc.perform(put("/servicos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(16)
    void criar_comDescricaoLonga_deveRetornar201() throws Exception {
        String descricaoLonga = "Desc ".repeat(50);
        String json = servicoJson("Serviço Longo", 150.0, descricaoLonga, null);
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(17)
    void listar_multiplosSevicos_deveRetornarTodos() throws Exception {
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço 1\",\"valor\":100.0}"));
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço 2\",\"valor\":200.0}"));
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço 3\",\"valor\":300.0}"));

        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(18)
    void atualizar_comNomeComEspacos_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Original\",\"valor\":100.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"  Novo Nome  \",\"valor\":150.0}", id);
        mockMvc.perform(put("/servicos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(19)
    void criar_comValorGrande_deveRetornar201() throws Exception {
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson("Serviço Premium", 9999.99, "Premium service", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(9999.99));
    }

    @Test
    @Order(20)
    void deletar_eDepoisCriarComMesmoNome_deveRetornar201() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço Reutilizável\",\"valor\":75.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/servicos/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço Reutilizável\",\"valor\":75.0}"))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(21)
    void atualizar_comValorPrecisao_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Precisão\",\"valor\":99.99}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"Precisão\",\"valor\":123.45}", id);
        mockMvc.perform(put("/servicos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(123.45));
    }

    @Test
    @Order(22)
    void criar_semDescricao_deveRetornar201() throws Exception {
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Sem Descricao\",\"valor\":50.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Sem Descricao"));
    }

    @Test
    @Order(23)
    void criar_comNomeMuitoCurto_deveRetornar400() throws Exception {
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"X\",\"valor\":50.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    void atualizar_comValorZero_deveRetornar400() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço Original\",\"valor\":100.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"Serviço Atualizado\",\"valor\":0.0}", id);
        mockMvc.perform(put("/servicos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(25)
    void atualizar_comNomeVazio_deveRetornar400() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço Original\",\"valor\":100.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,"{\"id\":%d,\"nome\":\"\",\"valor\":150.0}", id);
        mockMvc.perform(put("/servicos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(26)
    void criar_comValorFracionado_deveAceitar() throws Exception {
        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson("Serviço Fracionado", 45.67, "Com centavos", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(45.67));
    }

    @Test
    @Order(27)
    void atualizar_comDadosCompletos_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Serviço Incompleto\",\"valor\":80.0}"))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"Serviço Completo\",\"valor\":120.0,\"descricao\":\"Descrição completa\"}", id);
        mockMvc.perform(put("/servicos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Descrição completa"));
    }

    @Test
    @Order(28)
    void listar_aposMultiplasCriacoes_deveRetornarTodos() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/servicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(servicoJson("Serviço Múltiplo " + i, 10.0 * i, "Desc " + i, null)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(29)
    void obterPorId_comDadosCompletos_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson("Serviço Detalhado", 199.99, "Descrição detalhada", null)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/servicos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Serviço Detalhado"))
                .andExpect(jsonPath("$.valor").value(199.99));
    }

    @Test
    @Order(30)
    void deletar_seguido_deVerificarAusencia_deveRetornar404() throws Exception {
        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson("Serviço Deletável", 75.50, "Para deletar", null)))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/servicos/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/servicos/" + id))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/servicos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void obter_servico_deveRetornarHATEOASLinks() throws Exception {
        String servicoJson = String.format(java.util.Locale.US,"{\"nome\":\"Serviço HATEOAS\",\"valor\":%.2f,\"descricao\":\"Desc\"}", 200.00);

        MvcResult result = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson))
                .andReturn();

        Long servicoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/servicos/" + servicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.servicos.href").exists())
                .andExpect(jsonPath("$._links.editar.href").exists())
                .andExpect(jsonPath("$._links.excluir.href").exists())
                .andExpect(jsonPath("$._links.vendas.href").exists())
                .andExpect(jsonPath("$._links.criar_venda_com_servico.href").exists());
    }
}
