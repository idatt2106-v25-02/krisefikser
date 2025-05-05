package stud.ntnu.krisefikser.item.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.enums.ChecklistCategory;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * Service responsible for managing checklist items in the emergency preparedness system.
 *
 * <p>This service provides functionality for retrieving and toggling the completion status
 * of checklist items. It interacts with the {@link ChecklistItemRepository} for data access
 * operations.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistItemService {

  /**
   * Repository for ChecklistItem entity operations.
   */
  private final ChecklistItemRepository checklistItemRepository;
  private final UserService userService;

  /**
   * Toggles the checked status of a checklist item.
   *
   * <p>This method finds the checklist item by ID and inverts its current checked status.
   * If the item is currently checked, it will be unchecked, and vice versa.</p>
   *
   * @param id the unique identifier of the checklist item to toggle
   * @return a response DTO containing the updated checklist item details
   * @throws EntityNotFoundException if no checklist item exists with the given ID
   */
  @Transactional
  public ChecklistItemResponse toggleChecklistItem(UUID id) {
    return checklistItemRepository.findById(id)
        .map(item -> {
          if (!item.getHousehold().getId()
              .equals(userService.getCurrentUser().getActiveHousehold().getId())) {
            throw new EntityNotFoundException("Checklist item not found with id: " + id);
          }
          item.setChecked(!item.getChecked());
          return checklistItemRepository.save(item).toResponse();
        })
        .orElseThrow(() -> new EntityNotFoundException("Checklist item not found with id: " + id));
  }

  /**
   * Retrieves all checklist items for the active household.
   *
   * <p>This method fetches all checklist items for the active household from the repository and
   * converts them to response DTOs.</p>
   *
   * @return a list of checklist item response DTOs
   */
  public List<ChecklistItemResponse> getAllChecklistItems() {
    return checklistItemRepository.findByHousehold(
            userService.getCurrentUser().getActiveHousehold()).stream()
        .map(ChecklistItem::toResponse)
        .toList();
  }

  /**
   * Creates default checklist items for a household based on emergency preparedness guidelines.
   *
   * <p>This method creates a set of recommended checklist items organized by category
   * for the specified household, based on official emergency preparedness guidelines.</p>
   *
   * @param household the household to create checklist items for
   */
  @Transactional
  public void createDefaultChecklistItems(Household household) {
    List<ChecklistItem> defaultItems = List.of(
        // HEAT_LIGHT category items
        ChecklistItem.builder()
            .household(household)
            .name("Varme klær/Tepper")
            .description(
                "Ha tilgjengelig varme klær, pledd, dyner eller soveposer for å holde varmen ved"
                    + "strømbrudd.")
            .icon("shirt")
            .type(ChecklistCategory.HEAT_LIGHT)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Fyrstikker/Stearinlys")
            .description("Fyrstikker og stearinlys for belysning og varme ved strømbrudd.")
            .icon("flame")
            .type(ChecklistCategory.HEAT_LIGHT)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Ved (til ovn/peis)")
            .description("Hvis du har vedovn eller peis, sørg for å ha tilstrekkelig med ved.")
            .icon("flame")
            .type(ChecklistCategory.HEAT_LIGHT)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Varmeovn (gass/parafin)")
            .description(
                "En alternativ varmekilde som gass- eller parafinovn beregnet for innendørs bruk.")
            .icon("heater")
            .type(ChecklistCategory.HEAT_LIGHT)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Lommelykt/Hodelykt")
            .description(
                "Lommelykter eller hodelykter som går på batterier, sveiv eller solceller.")
            .icon("flashlight")
            .type(ChecklistCategory.HEAT_LIGHT)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Batterier/Nødlader")
            .description(
                "Ekstra batterier og oppladet batteribank til lommelykter og annet utstyr.")
            .icon("battery")
            .type(ChecklistCategory.HEAT_LIGHT)
            .build(),

        // INFORMATION category items
        ChecklistItem.builder()
            .household(household)
            .name("Radio (DAB)")
            .description(
                "En DAB-radio som går på batterier, sveiv eller solceller for å holde deg"
                    + "oppdatert.")
            .icon("radio")
            .type(ChecklistCategory.INFORMATION)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Viktige telefonnummer (papir)")
            .description(
                "Liste på papir med viktige telefonnummer som nødnummer, legevakt, veterinær,"
                    + "familie, venner og naboer.")
            .icon("list")
            .type(ChecklistCategory.INFORMATION)
            .build(),

        // HEALTH_HYGIENE category items
        ChecklistItem.builder()
            .household(household)
            .name("Medisiner/Førstehjelp")
            .description("Førstehjelpsutstyr og nødvendige medisiner for alle i husstanden.")
            .icon("first")
            .type(ChecklistCategory.HEALTH_HYGIENE)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Jodtabletter")
            .description(
                "Jodtabletter for barn og voksne under 40 år, gravide og ammende ved"
                    + "atomhendelser.")
            .icon("pill")
            .type(ChecklistCategory.HEALTH_HYGIENE)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Hygieneartikler")
            .description(
                "Hygieneartikler som våtservietter, håndsprit, bleier, toalettpapir, "
                    + "bind og tamponger.")
            .icon("bath")
            .type(ChecklistCategory.HEALTH_HYGIENE)
            .build(),

        // OTHER category items
        ChecklistItem.builder()
            .household(household)
            .name("Kokeapparat/Grill")
            .description("Grill, kokeapparat eller stormkjøkken for tilberedning av mat.")
            .icon("cooking")
            .type(ChecklistCategory.OTHER)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Ekstra brennstoff")
            .description("Ekstra gassbeholder eller brennstoff til kokeapparat.")
            .icon("fuel")
            .type(ChecklistCategory.OTHER)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Kontanter/Betalingskort")
            .description(
                "Litt kontanter og flere betalingskort i tilfelle elektroniske betalingssystemer "
                    + "ikke fungerer.")
            .icon("credit")
            .type(ChecklistCategory.OTHER)
            .build(),

        ChecklistItem.builder()
            .household(household)
            .name("Avtale om overnatting")
            .description(
                "Ha en avtale på plass om hvor du kan overnatte hvis du ikke kan bo hjemme.")
            .icon("hotel")
            .type(ChecklistCategory.OTHER)
            .build()
    );

    checklistItemRepository.saveAll(defaultItems);
  }
}
