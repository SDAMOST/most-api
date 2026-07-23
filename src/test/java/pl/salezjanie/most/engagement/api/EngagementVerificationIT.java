package pl.salezjanie.most.engagement.api;

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
class EngagementVerificationIT {

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
        // Register & Login (or use existing)
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
    void shouldAwardPointsAndEnforceCapForNormalUnit() throws Exception {
        registerNewUser();
        // 1. Create a "Normal" unit (default cap = 4)
        String unitId = postJson("/api/structure/units", Map.of("name", "Formacyjne")).get("id").toString();

        // 2. Create an initiative
        String initiativeId = postJson("/api/initiatives", Map.of(
            "name", "Lectio",
            "description", "Test lectio",
            "ownerUnitId", unitId
        )).get("id").toString();

        // 3. Schedule weekly to get many occurrences in a few months
        postJson("/api/initiatives/" + initiativeId + "/schedule-rules", Map.of(
            "recurrenceType", "WEEKLY",
            "dayOfWeek", "MONDAY",
            "startTime", "10:00:00",
            "durationMinutes", 60,
            "effectiveFrom", "2027-01-01",
            "effectiveUntil", "2027-03-31"
        ));

        // 4. Generate
        List<Map<String, Object>> occurrences = postJsonList(
            "/api/initiatives/" + initiativeId + "/generate?from=2027-01-01&to=2027-02-28");
        assertThat(occurrences).hasSizeGreaterThanOrEqualTo(5);

        // 5. Complete and record attendance for 5 occurrences
        for (int i = 0; i < 5; i++) {
            String occurrenceId = occurrences.get(i).get("id").toString();
            putJson("/api/occurrences/" + occurrenceId + "/publish");
            putJson("/api/occurrences/" + occurrenceId + "/complete");
            
            // Record attendance -> triggers event -> awards point
            postJsonListWithBody("/api/occurrences/" + occurrenceId + "/attendance", 
                Map.of("memberIds", List.of(memberId)));
        }

        // 6. Check points
        Map<String, Object> pointsLedger = getJson("/api/engagement/me/points");
        
        // Since the cap is 4, total points should be exactly 4 despite 5 attendances
        assertThat(pointsLedger.get("totalPoints")).isEqualTo(4);

        // Transactions list should have 4 entries with 1 point
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) pointsLedger.get("transactions");
        assertThat(transactions).hasSize(4);
        assertThat(transactions.stream().mapToInt(t -> (int) t.get("points")).sum()).isEqualTo(4);
    }

    @Test
    void shouldAwardPointsWithoutLimitForUnlimitedUnit() throws Exception {
        registerNewUser();
        // 1. Create a unit
        String unitId = postJson("/api/structure/units", Map.of("name", "Gospodarcze")).get("id").toString();
        
        // Manually update the unit in DB to remove the cap (simulate unlimited unit)
        jdbcTemplate.update("UPDATE organization_units SET monthly_points_cap = NULL WHERE id = CAST(? AS uuid)", unitId);

        // 2. Create an initiative (defaults to 1 point)
        String initiativeId = postJson("/api/initiatives", Map.of(
            "name", "Obiady",
            "description", "Gotowanie",
            "ownerUnitId", unitId
        )).get("id").toString();

        // Update initiative to 2 points per occurrence
        jdbcTemplate.update("UPDATE initiatives SET default_points = 2 WHERE id = CAST(? AS uuid)", initiativeId);

        // 3. Schedule weekly
        postJson("/api/initiatives/" + initiativeId + "/schedule-rules", Map.of(
            "recurrenceType", "WEEKLY",
            "dayOfWeek", "TUESDAY",
            "startTime", "10:00:00",
            "durationMinutes", 60,
            "effectiveFrom", "2027-02-01",
            "effectiveUntil", "2027-04-30"
        ));

        // 4. Generate
        List<Map<String, Object>> occurrences = postJsonList(
            "/api/initiatives/" + initiativeId + "/generate?from=2027-02-01&to=2027-03-31");

        // 5. Complete and record attendance for 5 occurrences
        for (int i = 0; i < 5; i++) {
            String occurrenceId = occurrences.get(i).get("id").toString();
            putJson("/api/occurrences/" + occurrenceId + "/publish");
            putJson("/api/occurrences/" + occurrenceId + "/complete");
            postJsonListWithBody("/api/occurrences/" + occurrenceId + "/attendance", 
                Map.of("memberIds", List.of(memberId)));
        }

        // 6. Check points
        Map<String, Object> pointsLedger = getJson("/api/engagement/me/points");
        
        // Unlimited unit, 5 occurrences * 2 points = 10 points
        assertThat(pointsLedger.get("totalPoints")).isEqualTo(10);
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) pointsLedger.get("transactions");
        assertThat(transactions).hasSize(5);
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

    private List<Map<String, Object>> postJsonListWithBody(String path, Map<String, ?> body) throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("POST %s", path).isBetween(200, 299);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private Map<String, Object> getJson(String path) throws Exception {
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

    private Map<String, Object> putJson(String path) throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("PUT %s", path).isBetween(200, 299);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }
}
