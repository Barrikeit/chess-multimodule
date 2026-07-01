package dev.barrikeit.security.config;

import dev.barrikeit.config.ApplicationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Application WebSocket security configuration.
 *
 * <p>Extends the core {@link WebSocketSecurityConfiguration} base — authentication is performed at
 * the STOMP channel level by {@code JwtChannelInterceptor} (wired in {@code WebSocketConfiguration}),
 * so this only secures the HTTP surface (handshake, health, version), security headers and CORS.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize on @MessageMapping methods
@Import(SecurityExceptionHandler.class)
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfiguration extends WebSocketSecurityConfiguration {

  private final ApplicationProperties.ServerProperties serverProperties;

  public SecurityConfiguration(
      ApplicationProperties.ServerProperties serverProperties,
      SecurityProperties securityProperties,
      SecurityExceptionHandler exceptionHandler) {
    super(securityProperties, exceptionHandler);
    this.serverProperties = serverProperties;
  }

  @Override
  protected String apiPath() {
    return serverProperties.getServlet().getApiPath();
  }
}
