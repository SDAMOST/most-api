package pl.salezjanie.most.activities.api;

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
class ActivitiesVerificationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();
    private HttpClient httpClient = HttpClient.newHttpClient();

    private String token;
    
    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() throws Exception {
        // 1. Register User (Ignore 409 if exists)
        try {
            Map<String, String> registerRequest = Map.of(
                "email", "test@example.com",
                "password", "password",
                "displayName", "Test User"
            );
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(registerRequest)))
                .build();
            httpClient.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }

        // 2. Login to get token
        Map<String, String> loginRequest = Map.of(
            "email", "test@example.com",
            "password", "password"
        );
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + "/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginRequest)))
            .build();
        HttpResponse<String> loginResponse = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertThat(loginResponse.statusCode()).isBetween(200, 299);
        
        Map<String, Object> body = objectMapper.readValue(loginResponse.body(), new TypeReference<>() {});
        this.token = (String) body.get("token");
    }

    @Test
    void shouldCreateInitiativeGenerateOccurrencesAndReschedule() throws Exception {
        // 1. Create Unit
        Map<String, String> unitRequest = Map.of("name", "Test Unit");
        HttpRequest uReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + "/api/structure/units"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(unitRequest)))
            .build();
        HttpResponse<String> unitResponse = httpClient.send(uReq, HttpResponse.BodyHandlers.ofString());
        assertThat(unitResponse.statusCode()).isBetween(200, 299);
        Map<String, Object> uBody = objectMapper.readValue(unitResponse.body(), new TypeReference<>() {});
        String unitId = (String) uBody.get("id");

        // 2. Create Initiative
        Map<String, String> initRequest = Map.of(
            "name", "Test Init",
            "description", "Test desc",
            "ownerUnitId", unitId
        );
        HttpRequest iReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + "/api/initiatives"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(initRequest)))
            .build();
        HttpResponse<String> initResponse = httpClient.send(iReq, HttpResponse.BodyHandlers.ofString());
        assertThat(initResponse.statusCode()).isBetween(200, 299);
        Map<String, Object> iBody = objectMapper.readValue(initResponse.body(), new TypeReference<>() {});
        String initiativeId = (String) iBody.get("id");

        // 3. Add Schedule Rule (Weekly on Monday)
        Map<String, Object> ruleRequest = Map.of(
            "recurrenceType", "WEEKLY",
            "dayOfWeek", "MONDAY",
            "startTime", "10:00:00",
            "durationMinutes", 60,
            "effectiveFrom", "2026-08-01",
            "effectiveUntil", "2026-12-31"
        );
        HttpRequest rReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + "/api/initiatives/" + initiativeId + "/schedule-rules"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(ruleRequest)))
            .build();
        HttpResponse<String> ruleResponse = httpClient.send(rReq, HttpResponse.BodyHandlers.ofString());
        assertThat(ruleResponse.statusCode()).isBetween(200, 299);

        // 4. Generate Occurrences
        HttpRequest gReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + "/api/initiatives/" + initiativeId + "/generate?from=2026-08-01&to=2026-08-28"))
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<String> genResponse = httpClient.send(gReq, HttpResponse.BodyHandlers.ofString());
        assertThat(genResponse.statusCode()).isBetween(200, 299);
        
        List<Map<String, Object>> occurrences = objectMapper.readValue(genResponse.body(), new TypeReference<>() {});
        assertThat(occurrences).hasSize(4);
        String firstOccurrenceId = (String) occurrences.get(0).get("id");

        // 5. Reschedule the first occurrence
        Map<String, String> rescheduleRequest = Map.of(
            "newStart", "2026-08-03T11:00:00",
            "newEnd", "2026-08-03T12:00:00",
            "reason", "Test Reschedule Reason"
        );
        HttpRequest reschReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + "/api/occurrences/" + firstOccurrenceId + "/reschedule"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(rescheduleRequest)))
            .build();
        HttpResponse<String> reschResponse = httpClient.send(reschReq, HttpResponse.BodyHandlers.ofString());
        assertThat(reschResponse.statusCode()).isBetween(200, 299);

        // 6. Verify Log Entry via JDBC
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT * FROM occurrence_reschedule_log WHERE occurrence_id = CAST(? AS uuid)", firstOccurrenceId);
        
        assertThat(logs).hasSize(1);
        Map<String, Object> logEntry = logs.get(0);
        assertThat(logEntry.get("reason")).isEqualTo("Test Reschedule Reason");
    }
}
