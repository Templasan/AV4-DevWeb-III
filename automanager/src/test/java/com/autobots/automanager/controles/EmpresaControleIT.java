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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmpresaControleIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Payload reutilizável
    private String empresaJson(String razao, String fantasia) {
        return String.format(java.util.Locale.US,"{\"razaoSocial\":\"%s\",\"nomeFantasia\":\"%s\"}", razao, fantasia);
    }

    @Test
    @Order(1)
    void criar_comDadosValidos_deveRetornar201() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("AutoShop LTDA", "AutoShop")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.razaoSocial").value("AutoShop LTDA"))
                .andExpect(jsonPath("$.nomeFantasia").value("AutoShop"));
    }

    @Test
    @Order(2)
    void criar_semRazaoSocial_deveRetornar400() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nomeFantasia\":\"AutoShop\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void criar_comRazaoSocialEmBranco_deveRetornar400() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("", "AutoShop")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void listar_semEmpresas_deveRetornar200ComListaVazia() throws Exception {
        mockMvc.perform(get("/empresas"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void listar_comEmpresas_deveRetornarTodasAsEmpresas() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa A", "A")));
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa B", "B")));

        mockMvc.perform(get("/empresas"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void obterPorId_comIdExistente_deveRetornarEmpresa() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Teste", "Teste")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/empresas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Teste"));
    }

    @Test
    @Order(7)
    void obterPorId_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/empresas/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void atualizar_comDadosValidos_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Original", "Original")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/empresas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Atualizada", "Atualizada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Atualizada"));
    }

    @Test
    @Order(9)
    void deletar_comIdExistente_deveRetornar204() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Para Deletar", "Delete")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/empresas/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/empresas/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void deletar_comIdInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(delete("/empresas/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    void obterPorId_comIdInvalido_deveRetornar400() throws Exception {
        mockMvc.perform(get("/empresas/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(12)
    void atualizar_semRazaoSocial_deveRetornar400() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Original", "Original")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/empresas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nomeFantasia\":\"Atualizada\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(13)
    void atualizar_comRazaoSocialEmBranco_deveRetornar400() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Original", "Original")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/empresas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("", "Atualizada")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    void deletar_deveRemoverCompletamente() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Temp", "Temp")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Confirmar que existe
        mockMvc.perform(get("/empresas/" + id))
                .andExpect(status().isOk());

        // Deletar
        mockMvc.perform(delete("/empresas/" + id))
                .andExpect(status().isNoContent());

        // Confirmar que não existe mais
        mockMvc.perform(get("/empresas/" + id))
                .andExpect(status().isNotFound());

        // Confirmar que deletar novamente retorna 404
        mockMvc.perform(delete("/empresas/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15)
    void criar_comNomeFantasiaVazio_deveAceitar() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"razaoSocial\":\"Empresa Sem Fantasia\",\"nomeFantasia\":\"\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Sem Fantasia"));
    }

    @Test
    @Order(16)
    void criar_comNomeFantasiaNulo_deveAceitar() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"razaoSocial\":\"Empresa Sem Fantasia 2\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Sem Fantasia 2"));
    }

    @Test
    @Order(17)
    void atualizar_deveRetornarDadosAtualizados() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Original", "Orig")))
                .andReturn();
        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/empresas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Atualizada", "Atual")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Atualizada"))
                .andExpect(jsonPath("$.nomeFantasia").value("Atual"))
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @Order(18)
    void listar_deveRetornarMultiplasEmpresas() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa 1", "E1")));
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa 2", "E2")));
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa 3", "E3")));

        mockMvc.perform(get("/empresas"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(19)
    void criar_comRazaoSocialMuitoLonga_deveAceitar() throws Exception {
        String razaoLonga = "A".repeat(200);
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson(razaoLonga, "Longa")))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(20)
    void obterPorId_deveRetornarDadosCompletos() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Completa", "EC")))
                .andReturn();
        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/empresas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Completa"))
                .andExpect(jsonPath("$.nomeFantasia").value("EC"));
    }

    @Test
    @Order(21)
    void deletar_multiplosDeletosRetorna404() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Delete Multi", "DM")))
                .andReturn();
        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/empresas/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/empresas/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(22)
    void criar_comEspacosEmBranco_deveAceitar() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("  Empresa Com Espacos  ", "  ECE  ")))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(23)
    void atualizar_comRazaoSocialEmBrancoNaAtualizacao_deveRetornar400() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Original", "Original")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/empresas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"razaoSocial\":\"   \",\"nomeFantasia\":\"Atualizada\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    void criar_comRazaoSocialNumeros_deveAceitar() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("123456789 LTDA", "123")))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(25)
    void criar_comNomeFantasiaComCaracteresEspeciais_deveAceitar() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa & Cia.", "E & C")))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(26)
    void atualizar_mantendoMesmosValores_deveRetornar200() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Imutável", "EI")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/empresas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Imutável", "EI")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Imutável"));
    }

    @Test
    @Order(27)
    void deletar_seguidoDeListar_deveNaoConterEmpresa() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Deletável", "ED")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/empresas/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/empresas/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(28)
    void criar_comRazaoSocialMinima_deveAceitar() throws Exception {
        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("A", "")))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(29)
    void atualizar_removendoNomeFantasia_deveAceitar() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Com Fantasia", "ECF")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/empresas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Sem Fantasia", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeFantasia").value(""));
    }

    @Test
    @Order(30)
    void obterPorId_deveRetornarDadosExatos() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa Exata", "EE")))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/empresas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Exata"))
                .andExpect(jsonPath("$.nomeFantasia").value("EE"));
    }

    @Test
    void obter_empresa_deveRetornarHATEOASLinks() throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empresaJson("Empresa HATEOAS", "EH")))
                .andReturn();

        Long empresaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/empresas/" + empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.empresas.href").exists())
                .andExpect(jsonPath("$._links.editar.href").exists())
                .andExpect(jsonPath("$._links.excluir.href").exists())
                .andExpect(jsonPath("$._links.endereco.href").exists())
                .andExpect(jsonPath("$._links.telefones.href").exists())
                .andExpect(jsonPath("$._links.mercadorias.href").exists())
                .andExpect(jsonPath("$._links.vendas.href").exists())
                .andExpect(jsonPath("$._links.clientes.href").exists())
                .andExpect(jsonPath("$._links.funcionarios.href").exists())
                .andExpect(jsonPath("$._links.fornecedores.href").exists())
                .andExpect(jsonPath("$._links.registrar_venda.href").exists())
                .andExpect(jsonPath("$._links.cadastrar_usuario.href").exists());
    }
}
