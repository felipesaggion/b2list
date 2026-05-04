package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.Buyer;
import br.com.b2list.repository.BuyerRepository;
import br.com.b2list.service.BuyerService;
import br.com.b2list.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BuyerServiceImpl implements BuyerService {

    @Autowired
    private BuyerRepository buyerRepository;

    @Override
    public Buyer save(Buyer buyer) {
        buyer.setTenantCode(TenantContext.getTenant());
        if (buyer.getId() == null) {
            buyer.setCreatedAt(OffsetDateTime.now());
            buyer.setLastModified(buyer.getCreatedAt());
        } else {
            buyer.setLastModified(OffsetDateTime.now());
        }
        return buyerRepository.save(buyer);
    }

    @Override
    public List<Buyer> findAll() {
        return buyerRepository.findAll();
    }

    @Override
    public Buyer findById(UUID id) {
        return buyerRepository.findById(id).orElse(null);
    }

    @Override
    public Buyer findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode) {
        return buyerRepository.findByExternalReferenceAndEnabledTrueAndTenantCode(
                externalReference,
                tenantCode
        );
    }

    @Override
    public void deleteById(UUID id) {
        buyerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Buyer decrementCreditAtomically(UUID buyerId, BigDecimal amount) {
        log.info("Iniciando decremento atômico de crédito para buyerId: {} com amount: {}", buyerId, amount);

        Buyer buyer = buyerRepository.findByIdWithLock(buyerId)
                .orElseThrow(() -> new IllegalStateException("Comprador não encontrado: " + buyerId));

        BigDecimal currentCredit = buyer.getCreditLimit();
        if (currentCredit.compareTo(amount) < 0) {
            log.error("Crédito insuficiente. Crédito disponível: {}, Valor solicitado: {}", currentCredit, amount);
            throw new IllegalStateException(
                    String.format("Crédito insuficiente. Disponível: %s, Solicitado: %s", currentCredit, amount)
            );
        }

        BigDecimal newCredit = currentCredit.subtract(amount);
        buyer.setCreditLimit(newCredit);
        buyer.setLastModified(OffsetDateTime.now());

        Buyer updatedBuyer = buyerRepository.save(buyer);

        log.info("Crédito decrementado com sucesso. Buyer: {}, Crédito anterior: {}, Crédito novo: {}",
                buyerId, currentCredit, newCredit);

        return updatedBuyer;
    }
}
