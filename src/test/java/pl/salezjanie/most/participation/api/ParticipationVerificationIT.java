package pl.salezjanie.most.participation.api;

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
class ParticipationVerificationIT {

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
        // 1. Register
        Map<String, String> registerRequest = Map.of(
            "email", "test@example.com",
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

        // 2. Login
        Map<String, String> loginRequest = Map.of("email", "test@example.com", "password", "password");
        HttpResponse<String> loginResponse = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginRequest)))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(loginResponse.statusCode()).isBetween(200, 299);
        Map<String, Object> loginBody = objectMapper.readValue(loginResponse.body(), new TypeReference<>() {});
        this.token = (String) loginBody.get("token");

        // Extract member ID from the JWT subject
        // The token is a JWT, the subject is the member's UUID
        String[] parts = this.token.split("\\.");
        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        Map<String, Object> claims = objectMapper.readValue(payload, new TypeReference<>() {});
        this.memberId = (String) claims.get("sub");
    }

    @Test
    void shouldEnrollWithdrawReEnrollAndRecordAttendance() throws Exception {
        // ── Setup: Create Unit → Initiative → Rule → Generate → Publish ──
        String unitId = postJson("/api/structure/units", Map.of("name", "Test Unit")).get("id").toString();

        String initiativeId = postJson("/api/initiatives", Map.of(
            "name", "Lectio",
            "description", "Test lectio",
            "ownerUnitId", unitId
        )).get("id").toString();

        postJson("/api/initiatives/" + initiativeId + "/schedule-rules", Map.of(
            "recurrenceType", "WEEKLY",
            "dayOfWeek", "MONDAY",
            "startTime", "10:00:00",
            "durationMinutes", 60,
            "effectiveFrom", "2027-01-01",
            "effectiveUntil", "2027-12-31"
        ));

        List<Map<String, Object>> occurrences = postJsonList(
            "/api/initiatives/" + initiativeId + "/generate?from=2027-01-01&to=2027-01-31");
        assertThat(occurrences).hasSizeGreaterThanOrEqualTo(4);

        String occurrenceId = occurrences.get(0).get("id").toString();

        // Publish the occurrence so enrollment is possible
        putJson("/api/occurrences/" + occurrenceId + "/publish");

        // ── 1. Enroll ──
        Map<String, Object> enrollment = postJsonNoBody("/api/occurrences/" + occurrenceId + "/enrollments");
        assertThat(enrollment.get("status")).isEqualTo("ENROLLED");
        assertThat(enrollment.get("memberId")).isEqualTo(memberId);
        String enrollmentId = enrollment.get("id").toString();

        // ── 2. List enrollments ──
        List<Map<String, Object>> enrollments = getJsonList("/api/occurrences/" + occurrenceId + "/enrollments");
        assertThat(enrollments).hasSize(1);

        // ── 3. Duplicate enroll → expect 409 ──
        HttpResponse<String> dupeResponse = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/occurrences/" + occurrenceId + "/enrollments"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(dupeResponse.statusCode()).isEqualTo(409);

        // ── 4. Withdraw ──
        Map<String, Object> withdrawn = deleteJson("/api/enrollments/" + enrollmentId);
        assertThat(withdrawn.get("status")).isEqualTo("WITHDRAWN");

        // ── 5. Re-enroll ──
        Map<String, Object> reEnrollment = postJsonNoBody("/api/occurrences/" + occurrenceId + "/enrollments");
        assertThat(reEnrollment.get("status")).isEqualTo("ENROLLED");

        // ── 6. Complete occurrence, then record attendance (self) ──
        putJson("/api/occurrences/" + occurrenceId + "/complete");

        Map<String, Object> attendance = postJsonNoBody("/api/occurrences/" + occurrenceId + "/attendance/me");
        assertThat(attendance.get("memberId")).isEqualTo(memberId);

        // ── 7. List attendance ──
        List<Map<String, Object>> attendances = getJsonList("/api/occurrences/" + occurrenceId + "/attendance");
        assertThat(attendances).hasSize(1);

        // ── 8. Verify via JDBC ──
        List<Map<String, Object>> dbAttendances = jdbcTemplate.queryForList(
            "SELECT * FROM attendances WHERE occurrence_id = CAST(? AS uuid)", occurrenceId);
        assertThat(dbAttendances).hasSize(1);
    }

    @Test
    void shouldRecordBulkAttendanceWithoutEnrollment() throws Exception {
        // Setup: same as above but we skip enrollment entirely
        String unitId = postJson("/api/structure/units", Map.of("name", "Unit Sprzatanie")).get("id").toString();

        String initiativeId = postJson("/api/initiatives", Map.of(
            "name", "Sprzatanie",
            "description", "Cleaning duty",
            "ownerUnitId", unitId
        )).get("id").toString();

        postJson("/api/initiatives/" + initiativeId + "/schedule-rules", Map.of(
            "recurrenceType", "WEEKLY",
            "dayOfWeek", "FRIDAY",
            "startTime", "18:00:00",
            "durationMinutes", 120,
            "effectiveFrom", "2027-01-01",
            "effectiveUntil", "2027-12-31"
        ));

        List<Map<String, Object>> occurrences = postJsonList(
            "/api/initiatives/" + initiativeId + "/generate?from=2027-01-01&to=2027-01-31");
        String occurrenceId = occurrences.get(0).get("id").toString();

        // Publish → Complete
        putJson("/api/occurrences/" + occurrenceId + "/publish");
        putJson("/api/occurrences/" + occurrenceId + "/complete");

        // Record bulk attendance without any prior enrollment
        Map<String, Object> body = Map.of("memberIds", List.of(memberId));
        List<Map<String, Object>> recorded = postJsonListWithBody(
            "/api/occurrences/" + occurrenceId + "/attendance", body);
        assertThat(recorded).hasSize(1);
        assertThat(recorded.get(0).get("memberId")).isEqualTo(memberId);
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
        assertThat(response.statusCode()).as("POST %s → %s", path, response.body()).isBetween(200, 299);
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
        assertThat(response.statusCode()).as("POST %s → %s", path, response.body()).isBetween(200, 299);
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
        assertThat(response.statusCode()).as("POST %s → %s", path, response.body()).isBetween(200, 299);
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
        assertThat(response.statusCode()).as("POST %s → %s", path, response.body()).isBetween(200, 299);
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
        assertThat(response.statusCode()).as("GET %s → %s", path, response.body()).isBetween(200, 299);
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
        assertThat(response.statusCode()).as("PUT %s → %s", path, response.body()).isBetween(200, 299);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private Map<String, Object> deleteJson(String path) throws Exception {
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("DELETE %s → %s", path, response.body()).isBetween(200, 299);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }
}
