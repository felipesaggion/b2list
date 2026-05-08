package br.com.b2list.controller;

import br.com.b2list.domain.dto.PaymentConditionDTO;
import br.com.b2list.enums.Error;
import br.com.b2list.exception.TenantNotFoundException;
import br.com.b2list.service.PaymentConditionService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payment-condition")
public class PaymentConditionController {

    @Autowired
    private PaymentConditionService paymentConditionService;

    @GetMapping
    public List<PaymentConditionDTO> findAll() throws TenantNotFoundException {
        String tenant = TenantContext.getTenant();
        if(tenant == null) {
            throw new TenantNotFoundException("Tenant não encontrado nos headers", Error.ORD_VALIDATION_005);
        }
        return paymentConditionService.findByTenantCodeAndEnabledTrue(tenant);
    }

    @GetMapping("/{id}")
    public PaymentConditionDTO findById(@PathVariable UUID id) {
        return paymentConditionService.findById(id);
    }

    @PostMapping
    public PaymentConditionDTO save(@RequestBody PaymentConditionDTO paymentCondition) {
        return paymentConditionService.save(paymentCondition);
    }

    @PutMapping
    public PaymentConditionDTO update(@RequestBody PaymentConditionDTO paymentCondition) {
        return paymentConditionService.save(paymentCondition);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        paymentConditionService.deleteById(id);
    }
}
