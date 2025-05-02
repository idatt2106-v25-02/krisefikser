package stud.ntnu.krisefikser.household.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.household.dto.ProductTypeResponse;
import stud.ntnu.krisefikser.household.entity.ProductType;
import stud.ntnu.krisefikser.household.repository.ProductTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/product-types")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Type", description = "Product Type management APIs")
public class ProductTypeController {

  private final ProductTypeRepository productTypeRepository;

  @Operation(summary = "Get all product types", description = "Retrieves a list of all product types in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved product types", content = @Content(mediaType = "application/json", array = @ArraySchema(items = @Schema(implementation = ProductType.class))))
  })
  @GetMapping
  public ResponseEntity<Page<ProductType>> getAllProductTypes(Pageable pageable) {
    return ResponseEntity.ok(productTypeRepository.findAll(pageable));
  }

  @Operation(summary = "Search product types", description = "Searches for product types by name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved product types", content = @Content(mediaType = "application/json", array = @ArraySchema(items = @Schema(implementation = ProductType.class))))
  })
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
  public ResponseEntity<ProductType> createProductType(
      @RequestBody ProductTypeResponse productTypeDto) {
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