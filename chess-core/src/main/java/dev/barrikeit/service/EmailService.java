package dev.barrikeit.service;

import dev.barrikeit.config.ApplicationProperties;
import dev.barrikeit.service.dto.UserDto;
import dev.barrikeit.util.enums.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.host")
public class EmailService {

  private final JavaMailSender mailSender;
  private final ApplicationProperties.MailProperties mailProperties;

  public void sendEmail(UserDto user, EmailType emailType) {
    sendEmail(user, emailType, subject(emailType), body(user, emailType));
  }

  public void sendEmail(UserDto user, EmailType emailType, String subject, String body) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(mailProperties.getFrom());
      message.setTo(user.getEmail());
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);
      log.info("Email [{}] sent to {}", emailType, user.getEmail());
    } catch (Exception e) {
      log.error("Failed to send email [{}] to {}: {}", emailType, user.getEmail(), e.getMessage());
    }
  }

  private String subject(EmailType type) {
    return switch (type) {
      case REGISTER_USER -> "Account Registration";
      case VERIFY_USER   -> "Account Verified";
      case UPDATED_USER  -> "Account Updated";
      case ENABLED_USER  -> "Account Enabled";
      case DISABLED_USER -> "Account Disabled";
      case BANNED_USER   -> "Account Suspended";
      case UNBANNED_USER -> "Account Reinstated";
    };
  }

  private String body(UserDto user, EmailType type) {
    String username = user.getUsername();
    String baseUrl = mailProperties.getActivacionUrl();
    String token = (user.getSecurity() != null) ? user.getSecurity().getVerificationToken() : "";
    return switch (type) {
      case REGISTER_USER -> String.format(
          "Hello %s,%n%nYour account has been created. Please verify your email:%n%s/auth/verify?token=%s%n%nRegards",
          username, baseUrl, token);
      case VERIFY_USER -> String.format(
          "Hello %s,%n%nYour account has been successfully verified.%n%nRegards", username);
      case UPDATED_USER -> String.format(
          "Hello %s,%n%nYour account details have been updated.%n%nRegards", username);
      case ENABLED_USER -> String.format(
          "Hello %s,%n%nYour account has been enabled.%n%nRegards", username);
      case DISABLED_USER -> String.format(
          "Hello %s,%n%nYour account has been disabled. Please contact support.%n%nRegards", username);
      case BANNED_USER -> String.format(
          "Hello %s,%n%nYour account has been suspended. Please contact support.%n%nRegards", username);
      case UNBANNED_USER -> String.format(
          "Hello %s,%n%nYour account suspension has been lifted.%n%nRegards", username);
    };
  }
}
