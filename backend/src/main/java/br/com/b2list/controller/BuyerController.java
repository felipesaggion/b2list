package br.com.b2list.controller;

import br.com.b2list.domain.dto.BuyerDTO;
import br.com.b2list.service.BuyerService;
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
@RequestMapping("/buyer")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    @GetMapping
    public List<BuyerDTO> findAll() {
        return buyerService.findAll();
    }

    @GetMapping("/{id}")
    public BuyerDTO findById(@PathVariable UUID id) {
        return buyerService.findById(id);
    }

    @PostMapping
    public BuyerDTO save(@RequestBody BuyerDTO buyer) {
        return buyerService.save(buyer);
    }

    @PutMapping
    public BuyerDTO update(@RequestBody BuyerDTO buyer) {
        return buyerService.save(buyer);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        buyerService.deleteById(id);
    }
}
