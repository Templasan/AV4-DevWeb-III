package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelo.Telefone.TelefoneAtualizador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TelefoneAtualizadorTest {

    private TelefoneAtualizador atualizador;

    @BeforeEach
    void setUp() {
        atualizador = new TelefoneAtualizador();
    }

    private Telefone criarTelefone(Long id, String ddd, String numero) {
        Telefone t = new Telefone();
        t.setId(id);
        t.setDdd(ddd);
        t.setNumero(numero);
        return t;
    }

    @Test
    void atualizar_comDadosValidos_deveAtualizarCampos() {
        Telefone original = criarTelefone(1L, "11", "99999-0000");
        Telefone atualizado = criarTelefone(1L, "21", "88888-1111");

        atualizador.atualizar(original, atualizado);

        assertEquals("21", original.getDdd());
        assertEquals("88888-1111", original.getNumero());
    }

    @Test
    void atualizar_comDddNulo_naoDeveAlterarDdd() {
        Telefone original = criarTelefone(1L, "11", "99999-0000");
        Telefone atualizado = criarTelefone(1L, null, "88888-1111");

        atualizador.atualizar(original, atualizado);

        assertEquals("11", original.getDdd());
        assertEquals("88888-1111", original.getNumero());
    }

    @Test
    void atualizar_comNumeroEmBranco_naoDeveAlterarNumero() {
        Telefone original = criarTelefone(1L, "11", "99999-0000");
        Telefone atualizado = criarTelefone(1L, "21", "");

        atualizador.atualizar(original, atualizado);

        assertEquals("21", original.getDdd());
        assertEquals("99999-0000", original.getNumero());
    }

    @Test
    void atualizar_comAtualizacaoNula_naoDeveAlterarNada() {
        Telefone original = criarTelefone(1L, "11", "99999-0000");

        atualizador.atualizar(original, null);

        assertEquals("11", original.getDdd());
        assertEquals("99999-0000", original.getNumero());
    }

    @Test
    void atualizar_lista_deveCombinarPorId() {
        Telefone t1 = criarTelefone(1L, "11", "11111-1111");
        Telefone t2 = criarTelefone(2L, "22", "22222-2222");
        List<Telefone> originais = Arrays.asList(t1, t2);

        Telefone atualizacao1 = criarTelefone(1L, "31", "33333-3333");
        List<Telefone> atualizacoes = Arrays.asList(atualizacao1);

        atualizador.atualizar(originais, atualizacoes);

        assertEquals("31", t1.getDdd());
        assertEquals("33333-3333", t1.getNumero());
        assertEquals("22", t2.getDdd());
        assertEquals("22222-2222", t2.getNumero());
    }

    @Test
    void atualizar_lista_comIdNull_naoDeveAtualizar() {
        Telefone original = criarTelefone(1L, "11", "11111-1111");
        List<Telefone> originais = Arrays.asList(original);

        Telefone atualizacao = criarTelefone(null, "99", "99999-9999");
        List<Telefone> atualizacoes = Arrays.asList(atualizacao);

        atualizador.atualizar(originais, atualizacoes);

        assertEquals("11", original.getDdd());
    }

    @Test
    void atualizar_lista_comListaVazia_naoFazNada() {
        Telefone original = criarTelefone(1L, "11", "11111-1111");
        List<Telefone> originais = Arrays.asList(original);

        atualizador.atualizar(originais, List.of());

        assertEquals("11", original.getDdd());
    }
}
