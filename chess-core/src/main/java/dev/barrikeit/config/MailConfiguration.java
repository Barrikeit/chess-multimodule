package dev.barrikeit.config;

import java.util.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ConditionalOnProperty(name = "mail.host")
@EnableConfigurationProperties(ApplicationProperties.MailProperties.class)
public class MailConfiguration {

  @Bean
  public JavaMailSender javaMailSender(ApplicationProperties.MailProperties props) {
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(props.getHost());
    sender.setPort(props.getPort());
    sender.setUsername(props.getUser());
    sender.setPassword(props.getPass());

    Properties javaProps = sender.getJavaMailProperties();
    ApplicationProperties.MailProperties.Properties mp = props.getProperties();
    if (mp != null) {
      javaProps.put("mail.transport.protocol", nvl(mp.getProtocol(), "smtp"));
      javaProps.put("mail.smtp.auth", nvl(mp.getAuth(), "true"));
      javaProps.put("mail.smtp.starttls.enable", nvl(mp.getStarttls(), "true"));
      javaProps.put("mail.debug", nvl(mp.getDebug(), "false"));
    }
    return sender;
  }

  private static String nvl(String value, String fallback) {
    return (value != null && !value.isBlank()) ? value : fallback;
  }
}
