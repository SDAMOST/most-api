package pl.salezjanie.most.communication.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CommunicationVerificationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String token;
    private String memberId;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() throws Exception {
        registerNewUser();
    }

    private void registerNewUser() throws Exception {
        String email = "test" + System.currentTimeMillis() + "@example.com";
        Map<String, String> registerRequest = Map.of(
            "email", email,
            "password", "password",
            "displayName", "Test User"
        );
        httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(registerRequest)))
                .build(),
            HttpResponse.BodyHandlers.discarding());

        Map<String, String> loginRequest = Map.of("email", email, "password", "password");
        HttpResponse<String> loginResponse = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginRequest)))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        Map<String, Object> loginBody = objectMapper.readValue(loginResponse.body(), new TypeReference<>() {});
        this.token = (String) loginBody.get("token");

        String[] parts = this.token.split("\\.");
        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        Map<String, Object> claims = objectMapper.readValue(payload, new TypeReference<>() {});
        this.memberId = (String) claims.get("sub");
    }

    @Test
    void shouldNotifySubscribersOnPublish() throws Exception {
        // 1. Register device token
        postJson("/api/communication/device-tokens", Map.of("token", "fcm-token-" + memberId));

        // 2. Create unit and initiative
        String unitId = postJson("/api/structure/units", Map.of("name", "Unit_" + System.currentTimeMillis())).get("id").toString();
        String initiativeId = postJson("/api/initiatives", Map.of(
                "name", "Lectio",
                "description", "Opis",
                "ownerUnitId", unitId
        )).get("id").toString();

        // 3. Subscribe to initiative
        postJson("/api/communication/subscriptions/" + initiativeId, Map.of());

        // 4. Create schedule rule and generate occurrences
        postJson("/api/initiatives/" + initiativeId + "/schedule-rules", Map.of(
            "recurrenceType", "WEEKLY",
            "dayOfWeek", "MONDAY",
            "startTime", "10:00:00",
            "durationMinutes", 60,
            "effectiveFrom", "2027-01-01",
            "effectiveUntil", "2027-12-31"
        ));

        List<Map<String, Object>> occurrences = postJsonList("/api/initiatives/" + initiativeId + "/generate?from=2027-01-01&to=2027-01-31");
        String occurrenceId = occurrences.get(0).get("id").toString();

        // 5. Publish occurrence (this should trigger notification)
        putJson("/api/occurrences/" + occurrenceId + "/publish");

        // 6. Check notifications
        List<Map<String, Object>> notifications = getJsonList("/api/communication/notifications");
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).get("title")).isEqualTo("New Occurrence Published");
    }

    @Test
    void shouldNotifyEnrolledOnCancel() throws Exception {
        // 1. Create unit and initiative
        String unitId = postJson("/api/structure/units", Map.of("name", "Unit_" + System.currentTimeMillis())).get("id").toString();
        String initiativeId = postJson("/api/initiatives", Map.of(
                "name", "Rajd",
                "description", "Opis",
                "ownerUnitId", unitId
        )).get("id").toString();

        // 2. Create schedule rule and generate occurrences
        postJson("/api/initiatives/" + initiativeId + "/schedule-rules", Map.of(
            "recurrenceType", "WEEKLY",
            "dayOfWeek", "FRIDAY",
            "startTime", "18:00:00",
            "durationMinutes", 120,
            "effectiveFrom", "2027-02-01",
            "effectiveUntil", "2027-12-31"
        ));

        List<Map<String, Object>> occurrences = postJsonList("/api/initiatives/" + initiativeId + "/generate?from=2027-02-01&to=2027-02-28");
        String occurrenceId = occurrences.get(0).get("id").toString();
        putJson("/api/occurrences/" + occurrenceId + "/publish");

        // (We expect no notifications for Publish because we didn't subscribe!)
        List<Map<String, Object>> notifsAfterPublish = getJsonList("/api/communication/notifications");
        assertThat(notifsAfterPublish).isEmpty();

        // 3. Enroll in occurrence
        postJsonNoBody("/api/occurrences/" + occurrenceId + "/enrollments");

        // 4. Cancel occurrence (this should trigger notification)
        putJson("/api/occurrences/" + occurrenceId + "/cancel");

        // 5. Check notifications
        List<Map<String, Object>> notifsAfterCancel = getJsonList("/api/communication/notifications");
        assertThat(notifsAfterCancel).hasSize(1);
        assertThat(notifsAfterCancel.get(0).get("title")).isEqualTo("Occurrence Cancelled");
    }

    // ── HTTP helpers ──

    private Map<String, Object> postJson(String path, Map<String, ?> body) throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("POST %s", path).isBetween(200, 299);
        if (response.body().isBlank()) {
            return Map.of();
        }
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private List<Map<String, Object>> postJsonList(String path) throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("POST %s", path).isBetween(200, 299);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private Map<String, Object> postJsonNoBody(String path) throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("POST %s", path).isBetween(200, 299);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private Map<String, Object> putJson(String path) throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("PUT %s", path).isBetween(200, 299);
        if (response.body().isBlank()) {
            return Map.of();
        }
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private List<Map<String, Object>> getJsonList(String path) throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("GET %s", path).isBetween(200, 299);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }
}
