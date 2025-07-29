package de.ybm.restapi.variomedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.InputStream;
import java.util.Scanner;

@Service
public class VariomediaService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // API-URL und Token aus application.properties laden
    @Value("${variomedia.api.url}")
    private String baseUrl;

    @Value("${variomedia.api.token}")
    private String apiToken;

    /**
     * Erstellt die HTTP-Header mit der Autorisierung für API-Anfragen.
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + apiToken);
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * Ruft alle Kunden aus der Variomedia-API ab.
     */
    public JsonNode getAllCustomers() {
        String url = baseUrl + "/customers";
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        return response.getBody();
    }

    public JsonNode getDomainsForCustomer(String customerId) {
        String url = baseUrl + "/domains?filter[owner_id]=" + customerId;

        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        return response.getBody();
    }

    public JsonNode getDomainSettings(String domainId) {
        String url = baseUrl + "/dns-records?filter[domain]=" + domainId;

        HttpEntity<String> entity = new HttpEntity<>(getHeaders());

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class
        );

        return response.getBody();
    }
}