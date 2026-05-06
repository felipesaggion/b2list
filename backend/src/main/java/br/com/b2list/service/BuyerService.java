package br.com.b2list.service;

import br.com.b2list.domain.dto.BuyerDTO;
import br.com.b2list.domain.dto.TopBuyerDTO;
import br.com.b2list.domain.entity.Buyer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface BuyerService {
    BuyerDTO save(BuyerDTO buyer);

    List<BuyerDTO> findAll();

    BuyerDTO findById(UUID id);

    Buyer findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);

    void deleteById(UUID id);

    Buyer decrementCreditAtomically(UUID buyerId, BigDecimal amount);

    Buyer increaseCreditAtomically(UUID buyerId, BigDecimal amount);

    List<TopBuyerDTO> findTopBuyers(String tenant, OffsetDateTime from, OffsetDateTime to);
}
