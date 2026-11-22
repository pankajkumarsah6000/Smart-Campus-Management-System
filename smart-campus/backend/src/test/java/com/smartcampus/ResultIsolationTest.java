package com.smartcampus;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Result isolation: students only ever see their own results, and faculty only
 * see results for exams on subjects they actually teach.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testresult;DB_CLOSE_DELAY=-1")
class ResultIsolationTest extends IntegrationTestBase {

    private long examId(JsonNode exams, String name) {
        for (JsonNode e : exams) {
            if (name.equals(e.path("name").asText())) {
                return e.path("id").asLong();
            }
        }
        throw new AssertionError("exam " + name + " not found");
    }

    @Test
    void studentSeesOnlyOwnResults() throws Exception {
        String token = login("student1@smartcampus.edu", "Student@123");
        JsonNode results = getData("/api/exams/results/me", token);
        assertThat(results.size()).isGreaterThanOrEqualTo(1);
        for (JsonNode r : results) {
            assertThat(r.path("rollNumber").asText()).isEqualTo("ROLL-2024-001");
            assertThat(r.path("examinationName").asText()).isNotEmpty();
        }
    }

    @Test
    void studentWithNoResultsSeesEmptyList() throws Exception {
        String token = login("student2@smartcampus.edu", "Student@123");
        JsonNode results = getData("/api/exams/results/me", token);
        assertThat(results.size()).isZero();
    }

    @Test
    void facultySeesResultsForOwnSubjectExam() throws Exception {
        String faculty1Token = login("faculty1@smartcampus.edu", "Faculty@123");
        JsonNode exams = getData("/api/exams", faculty1Token);
        long midTerm = examId(exams, "Mid-Term Examination");

        JsonNode results = getData("/api/exams/" + midTerm + "/results", faculty1Token);
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).path("rollNumber").asText()).isEqualTo("ROLL-2024-001");
    }

    @Test
    void facultyCannotViewResultsForForeignSubjectExam() throws Exception {
        String faculty1Token = login("faculty1@smartcampus.edu", "Faculty@123");
        String faculty2Token = login("faculty2@smartcampus.edu", "Faculty@123");

        JsonNode exams = getData("/api/exams", faculty1Token);
        long midTerm = examId(exams, "Mid-Term Examination"); // CS301 -> faculty1's subject

        mockMvc.perform(get("/api/exams/" + midTerm + "/results")
                        .header("Authorization", bearer(faculty2Token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void facultyCannotRecordResultForForeignSubjectExam() throws Exception {
        String faculty1Token = login("faculty1@smartcampus.edu", "Faculty@123");
        String faculty2Token = login("faculty2@smartcampus.edu", "Faculty@123");

        JsonNode exams = getData("/api/exams", faculty1Token);
        long midTerm = examId(exams, "Mid-Term Examination");
        JsonNode results = getData("/api/exams/" + midTerm + "/results", faculty1Token);
        long studentId = results.get(0).path("studentId").asLong();

        mockMvc.perform(post("/api/exams/" + midTerm + "/results")
                        .header("Authorization", bearer(faculty2Token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "studentId", studentId,
                                "marksObtained", 95.0))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRecordsResultUsingPathExamId() throws Exception {
        String student1Token = login("student1@smartcampus.edu", "Student@123");
        String adminToken = login("admin@smartcampus.edu", "Admin@123");

        JsonNode myExams = getData("/api/exams/my", student1Token);
        long unitTest = -1;
        for (JsonNode e : myExams) {
            if ("Unit Test 1".equals(e.path("name").asText())) {
                unitTest = e.path("id").asLong();
                break;
            }
        }
        assertThat(unitTest).isPositive();

        JsonNode own = getData("/api/exams/results/me", student1Token);
        long studentId = own.get(0).path("studentId").asLong();

        // The body intentionally omits examinationId: the path id must win.
        mockMvc.perform(post("/api/exams/" + unitTest + "/results")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "studentId", studentId,
                                "marksObtained", 90.0))))
                .andExpect(status().isOk());
    }
}