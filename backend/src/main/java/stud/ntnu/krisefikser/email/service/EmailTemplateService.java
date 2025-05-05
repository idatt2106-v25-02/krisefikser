package stud.ntnu.krisefikser.email.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class EmailTemplateService {

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