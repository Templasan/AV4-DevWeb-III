package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelo.Venda.VendaAtualizador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VendaAtualizadorTest {

    private VendaAtualizador atualizador;

    @BeforeEach
    void setUp() {
        atualizador = new VendaAtualizador();
    }

    private Venda criarVenda(String identificacao, Date cadastro) {
        Venda v = new Venda();
        v.setIdentificacao(identificacao);
        v.setCadastro(cadastro);
        return v;
    }

    @Test
    void atualizar_comIdentificacaoValida_deveAtualizar() {
        Venda original = criarVenda("VENDA-001", new Date(1000));
        Venda atualizado = criarVenda("VENDA-002", null);

        atualizador.atualizar(original, atualizado);

        assertEquals("VENDA-002", original.getIdentificacao());
        assertEquals(new Date(1000), original.getCadastro());
    }

    @Test
    void atualizar_comIdentificacaoNula_naoDeveAlterarIdentificacao() {
        Venda original = criarVenda("VENDA-001", new Date(1000));
        Venda atualizado = criarVenda(null, null);

        atualizador.atualizar(original, atualizado);

        assertEquals("VENDA-001", original.getIdentificacao());
    }

    @Test
    void atualizar_comCadastroNaoNulo_deveAtualizarCadastro() {
        Date dataOriginal = new Date(1000);
        Date dataAtualizada = new Date(2000);
        Venda original = criarVenda("VENDA-001", dataOriginal);
        Venda atualizado = criarVenda(null, dataAtualizada);

        atualizador.atualizar(original, atualizado);

        assertEquals(dataAtualizada, original.getCadastro());
    }

    @Test
    void atualizar_comAtualizacaoNula_naoDeveAlterarNada() {
        Venda original = criarVenda("VENDA-001", new Date(1000));

        atualizador.atualizar(original, null);

        assertEquals("VENDA-001", original.getIdentificacao());
    }

    @Test
    void atualizar_comIdentificacaoEmBranco_naoDeveAlterarIdentificacao() {
        Venda original = criarVenda("VENDA-001", new Date(1000));
        Venda atualizado = criarVenda("   ", null);

        atualizador.atualizar(original, atualizado);

        assertEquals("VENDA-001", original.getIdentificacao());
    }
}
