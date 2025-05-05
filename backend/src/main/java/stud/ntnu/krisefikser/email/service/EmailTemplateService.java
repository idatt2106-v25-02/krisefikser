package stud.ntnu.krisefikser.email.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

/**
 * Service class for loading and replacing variables in email templates.
 *
 * <p>This service provides functionality to load email templates from the classpath and replace
 * placeholders with actual values.
 *
 * @since 1.0
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
   * @throws IOException if an error occurs while reading the template file
   */
  public String loadAndReplace(String templateName, Map<String, String> variables)
      throws IOException {
    ClassPathResource resource = new ClassPathResource("templates/" + templateName);
    String templateContent;
    try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
      templateContent = FileCopyUtils.copyToString(reader);
    }

    String finalContent = templateContent;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      finalContent = finalContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }

    return finalContent;
  }
} 