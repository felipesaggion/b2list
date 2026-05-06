package br.com.b2list.controller;

import br.com.b2list.domain.dto.SellerDTO;
import br.com.b2list.service.SellerService;
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
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @GetMapping
    public List<SellerDTO> findAll() {
        return sellerService.findAll();
    }

    @GetMapping("/{id}")
    public SellerDTO findById(@PathVariable UUID id) {
        return sellerService.findById(id);
    }

    @PostMapping
    public SellerDTO save(@RequestBody SellerDTO seller) {
        return sellerService.save(seller);
    }

    @PutMapping
    public SellerDTO update(@RequestBody SellerDTO seller) {
        return sellerService.save(seller);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        sellerService.deleteById(id);
    }
}
