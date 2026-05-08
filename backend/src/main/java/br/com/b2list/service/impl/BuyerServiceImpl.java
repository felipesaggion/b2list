package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.BuyerDTO;
import br.com.b2list.domain.dto.TopBuyerDTO;
import br.com.b2list.domain.entity.Buyer;
import br.com.b2list.mapper.BuyerMapper;
import br.com.b2list.repository.BuyerRepository;
import br.com.b2list.service.BuyerService;
import br.com.b2list.tenant.TenantContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BuyerServiceImpl implements BuyerService {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private BuyerMapper buyerMapper;

    @Override
    public BuyerDTO save(BuyerDTO buyerDTO) {
        buyerDTO.setTenantCode(TenantContext.getTenant());
        Buyer buyer = buyerRepository.findByExternalReferenceAndEnabledTrueAndTenantCode(
                buyerDTO.getExternalReference(),
                buyerDTO.getTenantCode()
        );
        UUID id = buyer.getId();
        if (id == null) {
            buyerDTO.setCreatedAt(OffsetDateTime.now());
            buyerDTO.setLastModified(buyerDTO.getCreatedAt());
        } else {
            buyerDTO.setLastModified(OffsetDateTime.now());
        }
        buyer = buyerMapper.toEntity(buyerDTO);
        buyer.setId(id);
        return buyerMapper.toDto(buyerRepository.save(buyer));
    }

    @Override
    public List<BuyerDTO> findAllByTenantCodeAndEnabledTrue(String tenantCode) {
        return buyerRepository.findAllByTenantCodeAndEnabledTrue(tenantCode).stream().map(buyerMapper::toDto).toList();
    }

    @Override
    public BuyerDTO findById(UUID id) {
        return buyerMapper.toDto(buyerRepository.findById(id).orElse(null));
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

    @Transactional
    @Override
    public Buyer increaseCreditAtomically(UUID buyerId, BigDecimal amount) {
        log.info("Iniciando incremento atômico de crédito para buyerId: {} com amount: {}", buyerId, amount);

        Buyer buyer = buyerRepository.findByIdWithLock(buyerId)
                .orElseThrow(() -> new IllegalStateException("Comprador não encontrado: " + buyerId));

        BigDecimal currentCredit = buyer.getCreditLimit();
        BigDecimal newCredit = currentCredit.add(amount);
        buyer.setCreditLimit(newCredit);
        buyer.setLastModified(OffsetDateTime.now());

        Buyer updatedBuyer = buyerRepository.save(buyer);

        log.info("Crédito incrementado com sucesso. Buyer: {}, Crédito anterior: {}, Crédito novo: {}",
                buyerId, currentCredit, newCredit);

        return updatedBuyer;
    }

    @Override
    public List<TopBuyerDTO> findTopBuyers(String tenant, OffsetDateTime from, OffsetDateTime to) {
        return buyerRepository.findTopBuyers(tenant, from, to);
    }


}
