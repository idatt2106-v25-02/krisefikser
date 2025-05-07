package stud.ntnu.krisefikser.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.notification.dto.NotificationResponse;
import stud.ntnu.krisefikser.notification.service.NotificationService;

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
public class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NotificationService notificationService;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @Autowired
  private ObjectMapper objectMapper;

  private UUID notificationId;
  private NotificationResponse testNotification;
  private List<NotificationResponse> notificationList;
  private Page<NotificationResponse> notificationsPage;

  @BeforeEach
  void setUp() {
    notificationId = UUID.randomUUID();

    testNotification = NotificationResponse.builder()
        .id(notificationId)
        .title("Test Notification")
        .message("This is a test notification")
        .read(false)
        .createdAt(LocalDateTime.now())
        .build();

    notificationList = List.of(testNotification);
    notificationsPage = new PageImpl<>(notificationList);
  }

  @Test
  @WithMockUser
  void getNotifications_ShouldReturnPage() throws Exception {
    when(notificationService.getNotifications(any(Pageable.class))).thenReturn(notificationsPage);

    mockMvc.perform(get("/api/notifications"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(notificationId.toString()))
        .andExpect(jsonPath("$.content[0].title").value("Test Notification"))
        .andExpect(jsonPath("$.content[0].message").value("This is a test notification"))
        .andExpect(jsonPath("$.content[0].read").value(false));
  }

  @Test
  @WithMockUser
  void getUnreadCount_ShouldReturnCount() throws Exception {
    when(notificationService.countUnreadNotifications()).thenReturn(5L);

    mockMvc.perform(get("/api/notifications/unread"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string("5"));
  }

  @Test
  @WithMockUser
  void readNotification_ShouldMarkAsRead() throws Exception {
    doNothing().when(notificationService).markNotificationAsRead(notificationId);

    mockMvc.perform(put("/api/notifications/read/{id}", notificationId)
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());

    verify(notificationService, times(1)).markNotificationAsRead(notificationId);
  }

  @Test
  @WithMockUser
  void readAll_ShouldMarkAllAsRead() throws Exception {
    doNothing().when(notificationService).markAllNotificationsAsRead();

    mockMvc.perform(put("/api/notifications/readAll")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());

    verify(notificationService, times(1)).markAllNotificationsAsRead();
  }

  @Test
  @WithMockUser
  void deleteNotification_ShouldDelete() throws Exception {
    doNothing().when(notificationService).deleteNotification(notificationId);

    mockMvc.perform(delete("/api/notifications/{id}", notificationId)
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());

    verify(notificationService, times(1)).deleteNotification(notificationId);
  }
}