package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.modelo.Mercadoria.MercadoriaAtualizador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class MercadoriaAtualizadorTest {

    private MercadoriaAtualizador atualizador;

    @BeforeEach
    void setUp() {
        atualizador = new MercadoriaAtualizador();
    }

    private Mercadoria criarMercadoria(String nome, String descricao, long qtd, double valor, Date validade, Date fabricacao) {
        Mercadoria m = new Mercadoria();
        m.setNome(nome);
        m.setDescricao(descricao);
        m.setQuantidade(qtd);
        m.setValor(valor);
        m.setValidade(validade);
        m.setFabricacao(fabricacao);
        m.setCadastro(new Date());
        return m;
    }

    @Test
    void atualizar_comTodosOsCamposValidos_deveAtualizarTudo() {
        Mercadoria original = criarMercadoria("Oleo Original", "Desc original", 10, 50.0, new Date(1000), new Date(500));
        Mercadoria nova = criarMercadoria("Oleo Novo", "Desc nova", 20, 99.9, new Date(2000), new Date(1500));

        atualizador.atualizar(original, nova);

        assertEquals("Oleo Novo", original.getNome());
        assertEquals("Desc nova", original.getDescricao());
        assertEquals(20, original.getQuantidade());
        assertEquals(99.9, original.getValor());
        assertEquals(new Date(2000), original.getValidade());
        assertEquals(new Date(1500), original.getFabricacao());
    }

    @Test
    void atualizar_comNomeNulo_naoDeveAlterarNome() {
        Mercadoria original = criarMercadoria("Oleo", null, 10, 50.0, new Date(), new Date());
        Mercadoria nova = criarMercadoria(null, "Nova desc", 15, 60.0, new Date(), new Date());

        atualizador.atualizar(original, nova);

        assertEquals("Oleo", original.getNome());
        assertEquals(15, original.getQuantidade());
    }

    @Test
    void atualizar_comQuantidadeZero_naoDeveAlterarQuantidade() {
        Mercadoria original = criarMercadoria("Oleo", null, 10, 50.0, new Date(), new Date());
        Mercadoria nova = criarMercadoria("Oleo Novo", null, 0, 60.0, new Date(), new Date());

        atualizador.atualizar(original, nova);

        assertEquals(10, original.getQuantidade());
        assertEquals("Oleo Novo", original.getNome());
    }

    @Test
    void atualizar_comValorZero_naoDeveAlterarValor() {
        Mercadoria original = criarMercadoria("Oleo", null, 10, 50.0, new Date(), new Date());
        Mercadoria nova = criarMercadoria("Oleo Novo", null, 15, 0.0, new Date(), new Date());

        atualizador.atualizar(original, nova);

        assertEquals(50.0, original.getValor());
        assertEquals(15, original.getQuantidade());
    }

    @Test
    void atualizar_comValidadeNula_naoDeveAlterarValidade() {
        Date validadeOriginal = new Date(1000);
        Mercadoria original = criarMercadoria("Oleo", null, 10, 50.0, validadeOriginal, new Date());
        Mercadoria nova = criarMercadoria("Oleo Novo", null, 15, 60.0, null, new Date());

        atualizador.atualizar(original, nova);

        assertEquals(validadeOriginal, original.getValidade());
    }

    @Test
    void atualizar_comAtualizacaoNula_naoDeveAlterarNada() {
        Mercadoria original = criarMercadoria("Oleo", "Desc", 10, 50.0, new Date(), new Date());

        atualizador.atualizar(original, null);

        assertEquals("Oleo", original.getNome());
        assertEquals(10, original.getQuantidade());
        assertEquals(50.0, original.getValor());
    }
}
