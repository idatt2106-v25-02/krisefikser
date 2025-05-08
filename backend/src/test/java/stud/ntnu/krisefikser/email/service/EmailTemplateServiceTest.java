package stud.ntnu.krisefikser.email.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

class EmailTemplateServiceTest {

    private EmailTemplateService emailTemplateService;
    private static final String TEST_TEMPLATE = "test-template.txt";

    @BeforeEach
    void setUp() {
        emailTemplateService = new EmailTemplateService();
    }

    @Test
    void loadAndReplace_WithValidTemplateAndVariables_ShouldReplacePlaceholders() throws IOException {
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
    void loadAndReplace_WithEmptyVariables_ShouldReturnOriginalTemplate() throws IOException {
        // Arrange
        Map<String, String> variables = new HashMap<>();

        // Act
        String result = emailTemplateService.loadAndReplace(TEST_TEMPLATE, variables);

        // Assert
        assertEquals("Hello {{name}}, your verification code is {{code}}. ", result);
    }

    @Test
    void loadAndReplace_WithSpecialCharacters_ShouldHandleCorrectly() throws IOException {
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
    void loadAndReplace_WithNonExistentTemplate_ShouldThrowIOException() {
        // Arrange
        Map<String, String> variables = new HashMap<>();

        // Act & Assert
        assertThrows(IOException.class, () -> 
            emailTemplateService.loadAndReplace("non-existent-template.txt", variables)
        );
    }

    private void createTestTemplateFile(String content) throws IOException {
        // Create the test template file in the test resources directory
        ClassPathResource resource = new ClassPathResource("templates/" + TEST_TEMPLATE);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            String existingContent = FileCopyUtils.copyToString(reader);
            if (!existingContent.equals(content)) {
                // If the content is different, we need to update it
                // In a real test environment, you would write to the file here
                // For this test, we'll just verify the content matches
                assertEquals(content, existingContent, "Test template content does not match expected content");
            }
        }
    }
} 