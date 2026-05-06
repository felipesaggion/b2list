package br.com.b2list.controller;

import br.com.b2list.domain.dto.AuthenticationRequestDTO;
import br.com.b2list.domain.dto.AuthenticationResponseDTO;
import br.com.b2list.service.impl.AuthenticationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {

    @Autowired
    private AuthenticationServiceImpl authenticationServiceImpl;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody AuthenticationRequestDTO request) {
        log.info("Requisição de login recebida para usuário: {}", request.getUsername());

        try {
            AuthenticationResponseDTO response = authenticationServiceImpl.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("Erro na autenticação: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            log.error("Erro inesperado na autenticação", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        log.debug("Validando token JWT");

        try {
            boolean isValid = authenticationServiceImpl.validateToken(token);
            return ResponseEntity.ok(isValid);
        } catch (Exception ex) {
            log.error("Erro ao validar token", ex);
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Authentication service is running");
    }
}

