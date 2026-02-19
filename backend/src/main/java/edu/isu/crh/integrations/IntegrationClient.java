package edu.isu.crh.integrations;

import edu.isu.crh.config.AppProps;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class IntegrationClient {

  private final AppProps props;
  private final WebClient legacyClient;
  private final WebClient webhookClient;

  public IntegrationClient(AppProps props) {
    this.props = props;

    String legacyBaseUrl = props.getLegacyBaseUrl();
    if (legacyBaseUrl == null || legacyBaseUrl.isBlank()) {
      throw new IllegalArgumentException("Legacy base URL must be configured");
    }

    this.legacyClient = WebClient.builder()
        .baseUrl(legacyBaseUrl)
        .build();

    // generic client (we'll pass full URL to .uri(...) for webhook)
    this.webhookClient = WebClient.builder().build();
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> fetchEmployee(String employeeId) {
    return legacyClient.get()
        .uri("/legacy/employee/{id}", employeeId)
        .retrieve()
        .bodyToMono(Map.class)
        .block();
  }

  public void syncToLegacy(Map<String, Object> payload) {
    if (payload == null) {
      throw new IllegalArgumentException("Payload cannot be null");
    }
    legacyClient.post()
        .uri("/legacy/sync")
        .bodyValue(payload)
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  public void sendWebhookEvent(Map<String, Object> payload) {
    if (payload == null) {
      throw new IllegalArgumentException("Payload cannot be null");
    }
    String url = props.getWebhookUrl();
    if (url == null || url.isBlank()) return;

    webhookClient.post()
        .uri(url) // full URL
        .bodyValue(payload)
        .retrieve()
        .toBodilessEntity()
        .block();
  }
}
