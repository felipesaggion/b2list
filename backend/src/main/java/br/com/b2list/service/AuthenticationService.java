package br.com.b2list.service;

import br.com.b2list.domain.dto.AuthenticationRequestDTO;
import br.com.b2list.domain.dto.AuthenticationResponseDTO;

public interface AuthenticationService {

    AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request);

    boolean validateToken(String token);
}
