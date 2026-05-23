package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.enumeracoes.TipoDocumento;
import com.autobots.automanager.modelo.Documento.DocumentoAtualizador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoAtualizadorTest {

    private DocumentoAtualizador atualizador;

    @BeforeEach
    void setUp() {
        atualizador = new DocumentoAtualizador();
    }

    private Documento criarDocumento(Long id, TipoDocumento tipo, String numero, Date dataEmissao) {
        Documento d = new Documento();
        d.setId(id);
        d.setTipo(tipo);
        d.setNumero(numero);
        d.setDataEmissao(dataEmissao);
        return d;
    }

    @Test
    void atualizar_comDadosValidos_deveAtualizarCampos() {
        Date dataOriginal = new Date(1000);
        Date dataAtualizada = new Date(2000);

        Documento original = criarDocumento(1L, TipoDocumento.CPF, "111.111.111-11", dataOriginal);
        Documento atualizado = criarDocumento(1L, TipoDocumento.RG, "99.999.999-9", dataAtualizada);

        atualizador.atualizar(original, atualizado);

        assertEquals(TipoDocumento.RG, original.getTipo());
        assertEquals("99.999.999-9", original.getNumero());
        assertEquals(dataAtualizada, original.getDataEmissao());
    }

    @Test
    void atualizar_comTipoNulo_naoDeveAlterarTipo() {
        Documento original = criarDocumento(1L, TipoDocumento.CPF, "111.111.111-11", new Date());
        Documento atualizado = criarDocumento(1L, null, "22.222.222-2", new Date());

        atualizador.atualizar(original, atualizado);

        assertEquals(TipoDocumento.CPF, original.getTipo());
        assertEquals("22.222.222-2", original.getNumero());
    }

    @Test
    void atualizar_comNumeroNulo_naoDeveAlterarNumero() {
        Documento original = criarDocumento(1L, TipoDocumento.CPF, "111.111.111-11", new Date());
        Documento atualizado = criarDocumento(1L, TipoDocumento.RG, null, null);

        atualizador.atualizar(original, atualizado);

        assertEquals("111.111.111-11", original.getNumero());
        assertEquals(TipoDocumento.RG, original.getTipo());
    }

    @Test
    void atualizar_comDataEmissaoNula_naoDeveAlterarData() {
        Date dataOriginal = new Date(1000);
        Documento original = criarDocumento(1L, TipoDocumento.CPF, "111.111.111-11", dataOriginal);
        Documento atualizado = criarDocumento(1L, null, null, null);

        atualizador.atualizar(original, atualizado);

        assertEquals(dataOriginal, original.getDataEmissao());
    }

    @Test
    void atualizar_comAtualizacaoNula_naoDeveAlterarNada() {
        Documento original = criarDocumento(1L, TipoDocumento.CPF, "111.111.111-11", new Date());

        atualizador.atualizar(original, null);

        assertEquals(TipoDocumento.CPF, original.getTipo());
        assertEquals("111.111.111-11", original.getNumero());
    }

    @Test
    void atualizar_lista_deveCombinarPorId() {
        Documento d1 = criarDocumento(1L, TipoDocumento.CPF, "111.111.111-11", new Date());
        Documento d2 = criarDocumento(2L, TipoDocumento.RG, "22.222.222-2", new Date());
        List<Documento> originais = Arrays.asList(d1, d2);

        Documento atualizacao = criarDocumento(1L, TipoDocumento.CNH, "12345678901", new Date());
        atualizador.atualizar(originais, Arrays.asList(atualizacao));

        assertEquals(TipoDocumento.CNH, d1.getTipo());
        assertEquals("12345678901", d1.getNumero());
        assertEquals(TipoDocumento.RG, d2.getTipo());
    }

    @Test
    void atualizar_lista_comListaNula_naoFazNada() {
        Documento original = criarDocumento(1L, TipoDocumento.CPF, "111.111.111-11", new Date());
        List<Documento> originais = Arrays.asList(original);

        atualizador.atualizar(originais, null);

        assertEquals(TipoDocumento.CPF, original.getTipo());
    }
}
