package stud.ntnu.krisefikser.household.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.household.dto.ProductTypeResponse;
import stud.ntnu.krisefikser.household.entity.ProductType;
import stud.ntnu.krisefikser.household.repository.ProductTypeRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-types")
@RequiredArgsConstructor
public class ProductTypeController {

    private final ProductTypeRepository productTypeRepository;

    @GetMapping
    public ResponseEntity<Page<ProductType>> getAllProductTypes(Pageable pageable) {
        return ResponseEntity.ok(productTypeRepository.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductType>> searchProductTypes(@RequestParam String name) {
        return ResponseEntity.ok(productTypeRepository.findByNameContainingIgnoreCase(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductType> getProductType(@PathVariable UUID id) {
        return productTypeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductType> createProductType(@RequestBody ProductTypeResponse productTypeDto) {
        ProductType productType = ProductType.builder()
                .name(productTypeDto.getName())
                .unit(productTypeDto.getUnit())
                .build();
        return ResponseEntity.ok(productTypeRepository.save(productType));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductType> updateProductType(
            @PathVariable UUID id,
            @RequestBody ProductTypeResponse productTypeDto) {
        return productTypeRepository.findById(id)
                .map(existing -> {
                    existing.setName(productTypeDto.getName());
                    existing.setUnit(productTypeDto.getUnit());
                    return ResponseEntity.ok(productTypeRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductType(@PathVariable UUID id) {
        if (productTypeRepository.existsById(id)) {
            productTypeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}