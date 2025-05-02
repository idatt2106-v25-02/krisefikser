package stud.ntnu.krisefikser.item.enums;

/**
 * Categories for checklist items in the emergency preparedness system.
 * 
 * <p>This enum defines the various categories that checklist items can be assigned to,
 * enabling organization and filtering of checklist items by their purpose or domain.</p>
 */
public enum ChecklistType {
  /**
   * Medical and health-related items or tasks.
   */
  HEALTH, 
  
  /**
   * Electricity and power supply related items or tasks.
   */
  POWER, 
  
  /**
   * Communication devices, tools, and services.
   */
  COMMUNICATION, 
  
  /**
   * Miscellaneous items or tasks that don't fit other categories.
   */
  OTHER
}
