package stud.ntnu.krisefikser.item.enums;

/**
 * Categories for checklist items in the emergency preparedness system, based on the DSB checklist
 * structure.
 *
 * <p>This enum provides a more granular categorization compared to the initial
 * set, aligning better with common emergency preparedness guidelines.</p>
 */
public enum ChecklistCategory {
  /**
   * Items related to maintaining warmth and providing illumination. Includes clothing, blankets,
   * heating sources, matches, candles, flashlights, and power sources for them.
   */
  HEAT_LIGHT,

  /**
   * Items and methods for receiving and sharing critical information. Includes radios and important
   * contact information.
   */
  INFORMATION,

  /**
   * Items related to personal health, first aid, and hygiene. Includes medications, first-aid kits,
   * iodine tablets, and toiletries.
   */
  HEALTH_HYGIENE,

  /**
   * Other essential items for preparedness not covered in other categories. Includes cooking
   * equipment, fuel, cash, payment cards, and backup plans.
   */
  OTHER
}
