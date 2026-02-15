package edu.isu.crh.integrations;

import edu.isu.crh.config.AppProps;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class IntegrationClient {
  private final WebClient legacyClient;
  private final WebClient webhookClient;
  private final String webhookUrl;

  public IntegrationClient(AppProps props) {
    this.legacyClient = WebClient.builder()
        .baseUrl(props.legacy().baseUrl())
        .build();

    this.webhookClient = WebClient.builder().build();
    this.webhookUrl = props.webhook().url();
  }

  public Map<String, Object> fetchEmployee(String employeeId) {
    return legacyClient.get()
        .uri("/legacy/employee/{id}", employeeId)
        .retrieve()
        .bodyToMono(Map.class)
        .block();
  }

  public void syncToLegacy(Map<String, Object> payload) {
    legacyClient.post()
        .uri("/legacy/sync")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .retrieve()
        .bodyToMono(Void.class)
        .block();
  }

  public void sendWebhookEvent(Map<String, Object> event) {
    if (webhookUrl == null || webhookUrl.isBlank()) return;

    webhookClient.post()
        .uri(webhookUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(event)
        .retrieve()
        .bodyToMono(Void.class)
        .onErrorResume(e -> {
          // swallow webhook errors so core flow still works
          return reactor.core.publisher.Mono.empty();
        })
        .block();
  }
}
