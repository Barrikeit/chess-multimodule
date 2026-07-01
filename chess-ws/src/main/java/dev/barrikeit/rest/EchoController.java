package dev.barrikeit.rest;

import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class EchoController {

  @MessageMapping("/ping")
  @SendToUser("/queue/pong")
  public EchoMessage ping(Principal principal) {
    log.debug("Ping from user [{}]", principal.getName());
    return new EchoMessage(principal.getName(), "pong");
  }

  @MessageMapping("/hello")
  @SendTo("/topic/hello")
  public EchoMessage hello(@Payload HelloMessage payload, Principal principal) {
    log.debug("Hello from [{}]: {}", principal.getName(), payload.name());
    return new EchoMessage(principal.getName(), "Hello " + payload.name());
  }

  @MessageMapping("/echo")
  @SendTo("/topic/echo")
  public EchoMessage echo(@Payload String text, Principal principal) {
    log.debug("Echo from user [{}]: {}", principal.getName(), text);
    return new EchoMessage(principal.getName(), text);
  }

  public record HelloMessage(String name) {}

  public record EchoMessage(String sender, String content) {}
}
