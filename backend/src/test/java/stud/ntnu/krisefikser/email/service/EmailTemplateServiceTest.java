package stud.ntnu.krisefikser.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stud.ntnu.krisefikser.email.exception.EmailTemplateException;

class EmailTemplateServiceTest {

  private static final String TEST_TEMPLATE = "test-template.txt";
  private EmailTemplateService emailTemplateService;

  @BeforeEach
  void setUp() {
    emailTemplateService = new EmailTemplateService();
  }

  @Test
  void loadAndReplace_WithValidTemplateAndVariables_ShouldReplacePlaceholders() {
    // Arrange
    Map<String, String> variables = new HashMap<>();
    variables.put("name", "John");
    variables.put("code", "123456");

    // Act
    String result = emailTemplateService.loadAndReplace(TEST_TEMPLATE, variables);

    // Assert
    assertEquals("Hello John, your verification code is 123456. ", result);
  }

  @Test
  void loadAndReplace_WithEmptyVariables_ShouldReturnOriginalTemplate() {
    // Arrange
    Map<String, String> variables = new HashMap<>();

    // Act
    String result = emailTemplateService.loadAndReplace(TEST_TEMPLATE, variables);

    // Assert
    assertEquals("Hello {{name}}, your verification code is {{code}}. ", result);
  }

  @Test
  void loadAndReplace_WithSpecialCharacters_ShouldHandleCorrectly() {
    // Arrange
    Map<String, String> variables = new HashMap<>();
    variables.put("name", "John & Jane");
    variables.put("code", "123!@#");

    // Act
    String result = emailTemplateService.loadAndReplace(TEST_TEMPLATE, variables);

    // Assert
    assertEquals("Hello John & Jane, your verification code is 123!@#. ", result);
  }

  @Test
  void loadAndReplace_WithVariablesNotInTemplate_ShouldIgnoreUnusedVariables() {
    // Arrange
    Map<String, String> variables = new HashMap<>();
    variables.put("name", "John");
    variables.put("code", "123456");
    variables.put("unusedVar", "This won't be used");

    // Act
    String result = emailTemplateService.loadAndReplace(TEST_TEMPLATE, variables);

    // Assert
    assertEquals("Hello John, your verification code is 123456. ", result);
  }

  @Test
  void loadAndReplace_WithMultipleOccurrencesOfPlaceholder_ShouldReplaceAll() {
    // Arrange
    // Create a template with multiple occurrences of the same placeholder
    Map<String, String> variables = new HashMap<>();
    variables.put("name", "John");

    // Act
    String result = emailTemplateService.loadAndReplace(TEST_TEMPLATE, variables);

    // Assert - this assumes template has "Hello {{name}}, your verification code is {{code}}. "
    assertFalse(result.contains("{{name}}"));
    assertTrue(result.contains("John"));
  }

  @Test
  void loadAndReplace_WithEmptyPlaceholderValues_ShouldReplaceWithEmptyStrings() {
    // Arrange
    Map<String, String> variables = new HashMap<>();
    variables.put("name", "");
    variables.put("code", "");

    // Act
    String result = emailTemplateService.loadAndReplace(TEST_TEMPLATE, variables);

    // Assert
    assertEquals("Hello , your verification code is . ", result);
  }

  @Test
  void loadAndReplace_WithNonExistentTemplate_ShouldThrowEmailTemplateException() {
    // Arrange
    Map<String, String> variables = new HashMap<>();

    // Act & Assert
    EmailTemplateException exception = assertThrows(EmailTemplateException.class, () ->
        emailTemplateService.loadAndReplace("non-existent-template.txt", variables)
    );

    assertTrue(exception.getMessage().contains("Failed to load template"));
  }

  @Test
  void loadAndReplace_WithNullVariables_ShouldThrowEmailTemplateException() {
    // Act & Assert
    EmailTemplateException exception = assertThrows(EmailTemplateException.class, () ->
        emailTemplateService.loadAndReplace(TEST_TEMPLATE, null)
    );

    assertEquals("Variables map cannot be null", exception.getMessage());
  }

  @Test
  void loadAndReplace_WithNullTemplateName_ShouldThrowEmailTemplateException() {
    // Arrange
    Map<String, String> variables = new HashMap<>();

    // Act & Assert
    EmailTemplateException exception = assertThrows(EmailTemplateException.class, () ->
        emailTemplateService.loadAndReplace(null, variables)
    );

    assertEquals("Template name cannot be null", exception.getMessage());
  }
}