package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.SellerDTO;
import br.com.b2list.domain.entity.Seller;
import br.com.b2list.mapper.SellerMapper;
import br.com.b2list.repository.SellerRepository;
import br.com.b2list.service.SellerService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SellerServiceImpl implements SellerService {
    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SellerMapper sellerMapper;

    @Override
    public SellerDTO save(SellerDTO sellerDTO) {
        sellerDTO.setTenantCode(TenantContext.getTenant());
        Seller seller = sellerRepository.findByExternalReferenceAndEnabledTrueAndTenantCode(
                sellerDTO.getExternalReference(),
                sellerDTO.getTenantCode()
        );
        UUID id = seller.getId();
        if (id == null) {
            sellerDTO.setCreatedAt(OffsetDateTime.now());
        }
        seller = sellerMapper.toEntity(sellerDTO);
        seller.setId(id);
        return sellerMapper.toDto(sellerRepository.save(seller));
    }

    @Override
    public List<SellerDTO> findAll() {
        return sellerRepository.findAll().stream().map(sellerMapper::toDto).toList();
    }

    @Override
    public SellerDTO findById(UUID id) {
        return sellerMapper.toDto(sellerRepository.findById(id).orElse(null));
    }

    @Override
    public Seller findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode) {
        return sellerRepository.findByExternalReferenceAndEnabledTrueAndTenantCode(
                externalReference,
                tenantCode
        );
    }

    @Override
    public void deleteById(UUID id) {
        sellerRepository.deleteById(id);
    }
}
