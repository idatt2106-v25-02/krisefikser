package stud.ntnu.krisefikser.household.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
import stud.ntnu.krisefikser.household.dto.HouseholdItemResponse;
import stud.ntnu.krisefikser.household.dto.ProductTypeResponse;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdItem;
import stud.ntnu.krisefikser.household.entity.ProductType;
import stud.ntnu.krisefikser.household.repository.HouseholdItemRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
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
@RequestMapping("/api/households/{householdId}/items")
@RequiredArgsConstructor
@Validated
@Tag(name = "Household Item", description = "Household Item management APIs")
public class HouseholdItemController {

  private final HouseholdItemRepository householdItemRepository;
  private final HouseholdRepository householdRepo;
  private final ProductTypeRepository productTypeRepository;

  @Operation(summary = "Get household items", description = "Retrieves a list of items for a household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved household items", content = @Content(mediaType = "application/json", array = @ArraySchema(items = @Schema(implementation = HouseholdItemResponse.class))))
  })
  @GetMapping
  public ResponseEntity<Page<HouseholdItemResponse>> getHouseholdItems(
      @PathVariable UUID householdId,
      @RequestParam(required = false) UUID productTypeId,
      Pageable pageable) {
    return householdRepo.findById(householdId)
        .map(household -> {
          if (productTypeId != null) {
            return productTypeRepository.findById(productTypeId)
                .map(productType -> ResponseEntity.ok(householdItemRepository
                    .findByHouseholdAndProductType(household, productType, pageable)
                    .map(this::convertToDto)))
                .orElse(ResponseEntity.notFound().build());
          }
          return ResponseEntity.ok(householdItemRepository
              .findByHousehold(household, pageable)
              .map(this::convertToDto));
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get expiring items", description = "Retrieves a list of items that will expire before a given date")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved expiring items", content = @Content(mediaType = "application/json", array = @ArraySchema(items = @Schema(implementation = HouseholdItemResponse.class))))
  })
  @GetMapping("/expiring-soon")
  public ResponseEntity<List<HouseholdItemResponse>> getExpiringItems(
      @PathVariable UUID householdId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
    return householdRepo.findById(householdId)
        .map(household -> ResponseEntity.ok(householdItemRepository
            .findByHouseholdAndExpiryDateBefore(household, before)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList())))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/summary")
  public ResponseEntity<Map<String, Double>> getHouseholdSummary(@PathVariable UUID householdId) {
    return householdRepo.findById(householdId)
        .map(household -> ResponseEntity.ok(householdItemRepository
            .findByHousehold(household)
            .stream()
            .collect(Collectors.groupingBy(
                item -> item.getProductType().getName(),
                Collectors.summingDouble(HouseholdItem::getAmount)))))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<HouseholdItemResponse> createHouseholdItem(
      @PathVariable UUID householdId,
      @RequestBody HouseholdItemResponse itemDto) {
    Optional<Household> householdOpt = householdRepo.findById(householdId);
    if (householdOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Optional<ProductType> productTypeOpt = productTypeRepository.findById(
        itemDto.getProductType().getId());
    if (productTypeOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    HouseholdItem item = HouseholdItem.builder()
        .name(itemDto.getName())
        .amount(itemDto.getAmount())
        .expiryDate(itemDto.getExpiryDate())
        .household(householdOpt.get())
        .productType(productTypeOpt.get())
        .build();

    return ResponseEntity.ok(convertToDto(householdItemRepository.save(item)));
  }

  @PutMapping("/{itemId}")
  public ResponseEntity<HouseholdItemResponse> updateHouseholdItem(
      @PathVariable UUID householdId,
      @PathVariable UUID itemId,
      @RequestBody HouseholdItemResponse itemDto) {
    Optional<HouseholdItem> itemOpt = householdItemRepository.findById(itemId);
    if (itemOpt.isEmpty() || !itemOpt.get().getHousehold().getId().equals(householdId)) {
      return ResponseEntity.notFound().build();
    }

    Optional<ProductType> productTypeOpt = productTypeRepository.findById(
        itemDto.getProductType().getId());
    if (productTypeOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    HouseholdItem item = itemOpt.get();
    item.setName(itemDto.getName());
    item.setAmount(itemDto.getAmount());
    item.setExpiryDate(itemDto.getExpiryDate());
    item.setProductType(productTypeOpt.get());

    return ResponseEntity.ok(convertToDto(householdItemRepository.save(item)));
  }

  @DeleteMapping("/{itemId}")
  public ResponseEntity<Void> deleteHouseholdItem(
      @PathVariable UUID householdId,
      @PathVariable UUID itemId) {
    return householdItemRepository.findById(itemId)
        .filter(item -> item.getHousehold().getId().equals(householdId))
        .map(item -> {
          householdItemRepository.delete(item);
          return ResponseEntity.ok().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
  }

  private HouseholdItemResponse convertToDto(HouseholdItem item) {
    return HouseholdItemResponse.builder()
        .id(item.getId())
        .name(item.getName())
        .amount(item.getAmount())
        .expiryDate(item.getExpiryDate())
        .householdId(item.getHousehold().getId())
        .productType(ProductTypeResponse.builder()
            .id(item.getProductType().getId())
            .name(item.getProductType().getName())
            .unit(item.getProductType().getUnit())
            .build())
        .createdAt(item.getCreatedAt())
        .updatedAt(item.getUpdatedAt())
        .build();
  }
}
