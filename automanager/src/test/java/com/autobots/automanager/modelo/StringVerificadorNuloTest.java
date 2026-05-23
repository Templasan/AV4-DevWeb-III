package com.autobots.automanager.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StringVerificadorNuloTest {

    private StringVerificadorNulo verificador;

    @BeforeEach
    void setUp() {
        verificador = new StringVerificadorNulo();
    }

    @Test
    void verificar_comNull_deveRetornarTrue() {
        assertTrue(verificador.verificar(null));
    }

    @Test
    void verificar_comStringVazia_deveRetornarTrue() {
        assertTrue(verificador.verificar(""));
    }

    @Test
    void verificar_comStringEmBranco_deveRetornarTrue() {
        assertTrue(verificador.verificar("   "));
    }

    @Test
    void verificar_comValorValido_deveRetornarFalse() {
        assertFalse(verificador.verificar("texto válido"));
    }

    @Test
    void verificar_comEspaco_deveRetornarFalse() {
        assertFalse(verificador.verificar(" texto "));
    }
}
