package br.com.b2list.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponseDTO {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String tenantCode;

    public static AuthenticationResponseDTO of(String accessToken, Long expiresIn, String username, String tenantCode) {
        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .username(username)
                .tenantCode(tenantCode)
                .build();
    }
}

