package stud.ntnu.krisefikser.household.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.household.dto.HouseholdItemDto;
import stud.ntnu.krisefikser.household.dto.ProductTypeDto;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdItem;
import stud.ntnu.krisefikser.household.entity.ProductType;
import stud.ntnu.krisefikser.household.repository.HouseholdItemRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdRepo;
import stud.ntnu.krisefikser.household.repository.ProductTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/households/{householdId}/items")
@RequiredArgsConstructor
public class HouseholdItemController {

    private final HouseholdItemRepository householdItemRepository;
    private final HouseholdRepo householdRepo;
    private final ProductTypeRepository productTypeRepository;

    @GetMapping
    public ResponseEntity<Page<HouseholdItemDto>> getHouseholdItems(
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

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<HouseholdItemDto>> getExpiringItems(
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
    public ResponseEntity<HouseholdItemDto> createHouseholdItem(
            @PathVariable UUID householdId,
            @RequestBody HouseholdItemDto itemDto) {
        Optional<Household> householdOpt = householdRepo.findById(householdId);
        if (householdOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<ProductType> productTypeOpt = productTypeRepository.findById(itemDto.getProductType().getId());
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
    public ResponseEntity<HouseholdItemDto> updateHouseholdItem(
            @PathVariable UUID householdId,
            @PathVariable UUID itemId,
            @RequestBody HouseholdItemDto itemDto) {
        Optional<HouseholdItem> itemOpt = householdItemRepository.findById(itemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getHousehold().getId().equals(householdId)) {
            return ResponseEntity.notFound().build();
        }

        Optional<ProductType> productTypeOpt = productTypeRepository.findById(itemDto.getProductType().getId());
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

    private HouseholdItemDto convertToDto(HouseholdItem item) {
        return HouseholdItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .amount(item.getAmount())
                .expiryDate(item.getExpiryDate())
                .householdId(item.getHousehold().getId())
                .productType(ProductTypeDto.builder()
                        .id(item.getProductType().getId())
                        .name(item.getProductType().getName())
                        .unit(item.getProductType().getUnit())
                        .build())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}