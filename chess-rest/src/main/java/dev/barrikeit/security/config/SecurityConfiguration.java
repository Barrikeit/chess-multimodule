package dev.barrikeit.security.config;

import dev.barrikeit.config.ApplicationProperties;
import dev.barrikeit.security.filter.AppHeaderValidatorFilter;
import dev.barrikeit.security.filter.JwtFilter;
import dev.barrikeit.security.filter.RateLimitingFilter;
import dev.barrikeit.security.service.UserSessionService;
import dev.barrikeit.security.util.JwtUtil;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
@Import(SecurityExceptionHandler.class)
public class SecurityConfiguration {

  private final ApplicationProperties.ServerProperties serverProperties;
  private final SecurityProperties securityProperties;
  private final SecurityExceptionHandler exceptionHandler;
  private final JwtUtil jwtUtil;
  private final UserSessionService userSessionService;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.warn("Security Configuration active");
    String apiPath = serverProperties.getServlet().getApiPath();
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(
            headers ->
                headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                    .addHeaderWriter(
                        new StaticHeadersWriter(
                            "Referrer-Policy", "strict-origin-when-cross-origin"))
                    .addHeaderWriter(
                        new StaticHeadersWriter(
                            "Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
                    .addHeaderWriter(
                        new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin")))
        .sessionManagement(
            sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(exceptionHandler)
                    .accessDeniedHandler(exceptionHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // Public endpoints
                    .requestMatchers(
                        apiPath + "/public/**",
                        apiPath + "/error/**",
                        apiPath + "/error",
                        apiPath + "/version/**",
                        apiPath + "/version",
                        apiPath + "/auth/**")
                    .permitAll()
                    // Actuator — restricted to ADMIN role
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .hasAuthority("ADMIN")
                    // Domain endpoints
                    .requestMatchers(apiPath + "/watchdog/**")
                    .hasAuthority("WTC")
                    .requestMatchers(apiPath + "/users/**", apiPath + "/roles/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults());

    http.addFilterBefore(rateLimitingFilter(), UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(appHeaderValidatorFilter(), UsernamePasswordAuthenticationFilter.class);
    http.addFilterAfter(jwtFilter(), AppHeaderValidatorFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    SecurityProperties.CorsProperties cors = securityProperties.getCors();

    if (Boolean.TRUE.equals(cors.getEnabled())) {
      configuration.setAllowedOriginPatterns(splitTrimmed(cors.getAllowed().getOrigins()));
      configuration.setAllowedMethods(splitTrimmed(cors.getAllowed().getMethods()));
      configuration.setAllowedHeaders(splitTrimmed(cors.getAllowed().getHeaders()));
      configuration.setAllowCredentials(true);
    }

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration(cors.getPath().getPattern(), configuration);
    return source;
  }

  @Bean
  public RateLimitingFilter rateLimitingFilter() {
    SecurityProperties.RateLimitProperties rl = securityProperties.getRateLimit();
    if (rl == null) {
      return new RateLimitingFilter(
          false, 5, 5, 1, List.of("/auth/login", "/auth/register", "/auth/refresh"));
    }
    return new RateLimitingFilter(
        rl.isEnabled(),
        rl.getCapacity(),
        rl.getRefillTokens(),
        rl.getRefillPeriodMinutes(),
        List.of("/auth/login", "/auth/register", "/auth/refresh"));
  }

  @Bean
  public JwtFilter jwtFilter() {
    return new JwtFilter(jwtUtil, userSessionService);
  }

  @Bean
  public AppHeaderValidatorFilter appHeaderValidatorFilter() {
    return new AppHeaderValidatorFilter(
        serverProperties.getServlet().getApiPath(), securityProperties.getAppValidatorFilter());
  }

  private static List<String> splitTrimmed(String csv) {
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }
}
