package stud.ntnu.krisefikser.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class E2eMailControllerTest {

  private MockMvc mockMvc;
  private E2eMailOutbox outbox;
  private E2eMailController controller;

  @BeforeEach
  void setUp() {
    outbox = new E2eMailOutbox();
    controller = new E2eMailController(outbox);
    ReflectionTestUtils.setField(controller, "hookSecret", "test-hook-secret");
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void latest_returnsCapturedHtmlWhenSecretMatches() throws Exception {
    outbox.record("user@testern.no", "sub", "<a href=\"http://127.0.0.1:5173/verify?token=abc-uuid\">x</a>");

    mockMvc.perform(
            get("/api/e2e/mail/latest")
                .param("email", "user@testern.no")
                .header(E2eMailController.HOOK_HEADER, "test-hook-secret")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.to").value("user@testern.no"))
        .andExpect(jsonPath("$.subject").value("sub"))
        .andExpect(jsonPath("$.html").value("<a href=\"http://127.0.0.1:5173/verify?token=abc-uuid\">x</a>"));
  }

  @Test
  void latest_returns401WhenSecretWrong() throws Exception {
    outbox.record("user@testern.no", "s", "h");
    mockMvc.perform(
            get("/api/e2e/mail/latest")
                .param("email", "user@testern.no")
                .header(E2eMailController.HOOK_HEADER, "wrong"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void latest_returns503WhenHookSecretNotConfigured() throws Exception {
    ReflectionTestUtils.setField(controller, "hookSecret", "");
    mockMvc.perform(
            get("/api/e2e/mail/latest")
                .param("email", "user@testern.no")
                .header(E2eMailController.HOOK_HEADER, "x"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void latest_returns404WhenNoMailForRecipient() throws Exception {
    mockMvc.perform(
            get("/api/e2e/mail/latest")
                .param("email", "missing@testern.no")
                .header(E2eMailController.HOOK_HEADER, "test-hook-secret"))
        .andExpect(status().isNotFound());
  }
}
