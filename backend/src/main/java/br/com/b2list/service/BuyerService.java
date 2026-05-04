package br.com.b2list.service;

import br.com.b2list.domain.entity.Buyer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BuyerService {
    Buyer save(Buyer buyer);
    List<Buyer> findAll();
    Buyer findById(UUID id);
    Buyer findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);
    void deleteById(UUID id);

    /**
     * Decrementa o crédito do comprador de forma atômica e segura
     * Usa PESSIMISTIC LOCK para garantir que apenas uma transação processe por vez
     * Essencial em ambiente de alta concorrência
     *
     * @param buyerId ID do comprador
     * @param amount Valor a ser decrementado
     * @return Buyer atualizado com crédito decrementado
     * @throws IllegalStateException se o credito for insuficiente
     */
    Buyer decrementCreditAtomically(UUID buyerId, BigDecimal amount);

}
