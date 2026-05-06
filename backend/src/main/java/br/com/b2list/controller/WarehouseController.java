package br.com.b2list.controller;

import br.com.b2list.domain.dto.WarehouseDTO;
import br.com.b2list.service.WarehouseService;
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
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public List<WarehouseDTO> findAll() {
        return warehouseService.findAll();
    }

    @GetMapping("/{id}")
    public WarehouseDTO findById(@PathVariable UUID id) {
        return warehouseService.findById(id);
    }

    @PostMapping
    public WarehouseDTO save(@RequestBody WarehouseDTO warehouse) {
        return warehouseService.save(warehouse);
    }

    @PutMapping
    public WarehouseDTO update(@RequestBody WarehouseDTO warehouse) {
        return warehouseService.save(warehouse);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        warehouseService.deleteById(id);
    }
}
