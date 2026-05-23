package com.autobots.automanager.excecoes;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.autobots.automanager.dtos.ErroRespostaDTO;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@ControllerAdvice
public class ManipuladorGlobal {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroRespostaDTO> manipularValidacao(MethodArgumentNotValidException ex) {
        String mensagens = ex.getBindingResult().getAllErrors().stream()
            .map(erro -> erro.getDefaultMessage())
            .reduce((msg1, msg2) -> msg1 + "; " + msg2)
            .orElse("Erro de validação");
        
        ErroRespostaDTO erro = new ErroRespostaDTO(
            "Erro de validação", 
            mensagens
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroRespostaDTO> manipularViolacaoIntegridade(DataIntegrityViolationException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
            "Erro de integridade de dados.", 
            "Provavelmente este registro (CPF/RG) já existe ou faltam dados obrigatórios."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErroRespostaDTO> manipularNaoEncontrado(EntidadeNaoEncontradaException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(ex.getMessage(), "Recurso inexistente");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroRespostaDTO> manipularErroTipo(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        String tipoEsperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido";
        ErroRespostaDTO erro = new ErroRespostaDTO(
            "Tipo de dado inválido",
            String.format("O parâmetro '%s' deveria ser do tipo %s", ex.getName(), tipoEsperado)
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErroRespostaDTO> manipularJsonInvalido(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
            "Erro na leitura do JSON", 
            "O corpo da requisição possui erros de sintaxe ou caracteres inválidos."
        );
        return ResponseEntity.badRequest().body(erro);
    }

    /**
     * Acesso negado: usuário autenticado sem a permissão necessária.
     * Documentado: Spring Security lança AccessDeniedException que, sem este handler,
     * seria capturada pelo manipulador genérico e retornaria 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroRespostaDTO> manipularAcessoNegado(AccessDeniedException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
            "Acesso negado",
            "Você não tem permissão para acessar este recurso."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    /**
     * Sem autenticação: contexto de segurança vazio quando @PreAuthorize é avaliado.
     * Documentado: retorna 403 (não 401) para manter consistência com os testes.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroRespostaDTO> manipularNaoAutenticado(AuthenticationException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
            "Não autenticado",
            "É necessário se autenticar para acessar este recurso."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroRespostaDTO> manipularErroGenerico(Exception ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
            "Erro interno no servidor",
            "Ocorreu um erro inesperado. Verifique os dados enviados ou tente mais tarde."
        );
        System.err.println("Erro não tratado: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}

