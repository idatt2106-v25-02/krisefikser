package stud.ntnu.krisefikser.email.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import stud.ntnu.krisefikser.email.exception.EmailTemplateException;

/**
 * Service class for loading and replacing variables in email templates.
 *
 * <p>This service provides functionality to load email templates from the classpath and replace
 * placeholders with actual values.
 */
@Service
public class EmailTemplateService {

  /**
   * Loads an email template by its name, replaces placeholders with the provided variable values,
   * and returns the processed content.
   *
   * @param templateName the name of the template file to load, expected to be located in the
   *                     "templates/" directory on the classpath
   * @param variables    a map of placeholder keys and their corresponding replacement values
   * @return the content of the template with placeholders replaced by provided values
   */
  public String loadAndReplace(String templateName, Map<String, String> variables) {
    // Add explicit null checks
    if (templateName == null) {
      throw new EmailTemplateException("Template name cannot be null");
    }
    if (variables == null) {
      throw new EmailTemplateException("Variables map cannot be null");
    }

    try {
      ClassPathResource resource = new ClassPathResource("templates/" + templateName);
      String templateContent;
      try (Reader reader = new InputStreamReader(resource.getInputStream(),
          StandardCharsets.UTF_8)) {
        templateContent = FileCopyUtils.copyToString(reader);
      }

      String finalContent = templateContent;
      for (Map.Entry<String, String> entry : variables.entrySet()) {
        finalContent = finalContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
      }

      return finalContent;
    } catch (IOException e) {
      throw new EmailTemplateException("Failed to load template: " + templateName, e);
    }
  }
} 