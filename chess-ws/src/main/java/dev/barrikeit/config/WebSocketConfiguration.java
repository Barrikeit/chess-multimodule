package dev.barrikeit.config;

import dev.barrikeit.security.interceptor.AppHeaderValidatorInterceptor;
import dev.barrikeit.security.interceptor.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

  private final ApplicationProperties.ServerProperties serverProperties;
  private final AppHeaderValidatorInterceptor appHeaderValidatorInterceptor;
  private final JwtChannelInterceptor jwtChannelInterceptor;

  @Bean
  public TaskScheduler heartbeatScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("ws-heartbeat-");
    scheduler.initialize();
    return scheduler;
  }

  /**
   * Register the STOMP endpoint. SockJS fallback is enabled for browsers that don't support native
   * WS. setAllowedOriginPatterns("*") — restrict this in production.
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    String apiPath = serverProperties.getServlet().getApiPath();
    registry.addEndpoint(apiPath + "/ws").setAllowedOriginPatterns("*");
  }

  /**
   * Configure the message broker. Simple in-memory broker; swap for RabbitMQ/ActiveMQ when you need
   * persistence.
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry
        .enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(new long[]{10000, 0})  // server sends every 10s, expects nothing from client
        .setTaskScheduler(heartbeatScheduler());
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
  }

  /**
   * Wire channel interceptors in order:
   *
   * <ul>
   *   <li>1. AppHeaderValidatorInterceptor — validates the custom app header on CONNECT
   *   <li>2. JwtChannelInterceptor — validates the Bearer JWT on CONNECT
   * </ul>
   *
   * <p>Both run only on the CONNECT frame; subsequent frames reuse the established principal.
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(appHeaderValidatorInterceptor, jwtChannelInterceptor);
  }
}
