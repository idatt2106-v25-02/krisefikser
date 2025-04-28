package stud.ntnu.krisefikser.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {

  @Value("${spring.mail.host}")
  private String mailHost;

  @Value("${spring.mail.port}")
  private int mailPort;

  @Value("${spring.mail.username}")
  private String mailUsername;

  @Value("${spring.mail.password}")
  private String mailPassword;

  @Value("${spring.mail.properties.mail.smtp.auth}")
  private boolean smtpAuth;

  @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
  private boolean smtpStarttlsEnable;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(mailHost);
    mailSender.setPort(mailPort);
    mailSender.setUsername(mailUsername);
    mailSender.setPassword(mailPassword);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", String.valueOf(smtpAuth));
    props.put("mail.smtp.starttls.enable", String.valueOf(smtpStarttlsEnable));
    props.put("mail.debug", "true");

    return mailSender;
  }
}