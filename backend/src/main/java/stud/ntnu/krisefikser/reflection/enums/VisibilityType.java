package stud.ntnu.krisefikser.reflection.enums;

/**
 * Enumeration of visibility types for reflections.
 *
 * <p>Determines who can view a reflection:
 * </p>
 * <ul>
 * <li>PUBLIC: Visible to all users</li>
 * <li>HOUSEHOLD: Visible only to members of the author's household</li>
 * <li>PRIVATE: Visible only to the author</li>
 * </ul>
 */
public enum VisibilityType {
  PUBLIC,
  HOUSEHOLD,
  PRIVATE
}