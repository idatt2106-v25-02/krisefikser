package stud.ntnu.krisefikser.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.user.entity.User;

import java.util.Collections;

@SpringBootTest(classes = {EmailService.class})
@ActiveProfiles("test")
public class SimplifiedEmailTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender javaMailSender;

    private final String targetEmail = "janjaboy14@gmail.com";

    @Test
    void sendDirectEmail() {
        // Create a simple user object without database involvement
        User user = new User();
        user.setEmail(targetEmail);
        user.setFirstName("Test");
        user.setLastName("User");

        // Create a simple UserDetails object
        UserBuilder builder = org.springframework.security.core.userdetails.User.builder();
        UserDetails userDetails = builder
            .username(targetEmail)
            .password("password")
            .authorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")))
            .build();

        // Send the email directly
        emailService.sendWelcomeEmail(user, userDetails);

        System.out.println("Email sent to " + targetEmail);
    }
}