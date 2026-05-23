package com.autobots.automanager.controles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.autobots.automanager.dtos.LoginDTO;
import com.autobots.automanager.dtos.LoginResponseDTO;
import com.autobots.automanager.servicos.AutenticacaoServico;

@RestController
@RequestMapping("/auth")
public class AutenticacaoControle {

    @Autowired
    private AutenticacaoServico autenticacaoServico;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginResponseDTO response = autenticacaoServico.login(loginDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
