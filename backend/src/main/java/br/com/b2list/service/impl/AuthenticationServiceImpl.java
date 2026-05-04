package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.AuthenticationRequestDTO;
import br.com.b2list.domain.dto.AuthenticationResponseDTO;
import br.com.b2list.security.JwtProvider;
import br.com.b2list.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private JwtProvider jwtProvider;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    @Override
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        log.info("Autenticando usuário: {} com tenant: {}", request.getUsername(), request.getTenantCode());

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username não pode estar vazio");
        }

        if (request.getTenantCode() == null || request.getTenantCode().isBlank()) {
            throw new IllegalArgumentException("TenantCode não pode estar vazio");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            log.warn("Password vazio para usuário: {}", request.getUsername());
        }

        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("roles", "USER");
        additionalClaims.put("authenticated_at", System.currentTimeMillis());

        String token = jwtProvider.generateToken(request.getUsername(), additionalClaims);

        log.info("Token gerado com sucesso para usuário: {} com tenant: {}", request.getUsername(), request.getTenantCode());

        return AuthenticationResponseDTO.of(token, jwtExpiration, request.getUsername(), request.getTenantCode());
    }

    @Override
    public boolean validateToken(String token) {
        return jwtProvider.isTokenValid(token) && !jwtProvider.isTokenExpired(token);
    }

}

