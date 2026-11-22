package com.smartcampus;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Attendance ownership: faculty may only mark attendance for the subjects they are
 * assigned to (and for the students actually enrolled in them).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testatt;DB_CLOSE_DELAY=-1")
class AttendanceOwnershipTest extends IntegrationTestBase {

    private long subjectId(JsonNode subjects, String code) {
        for (JsonNode s : subjects) {
            if (code.equals(s.path("code").asText())) {
                return s.path("id").asLong();
            }
        }
        throw new AssertionError("subject " + code + " not found");
    }

    @Test
    void facultyCanMarkAttendanceForOwnSubject() throws Exception {
        String facultyToken = login("faculty1@smartcampus.edu", "Faculty@123");
        JsonNode subjects = getData("/api/faculty/me/subjects", facultyToken);
        long cs301 = subjectId(subjects, "CS301");

        JsonNode students = getData("/api/faculty/subjects/" + cs301 + "/students", facultyToken);
        assertThat(students.size()).isEqualTo(3);
        long studentId = students.get(0).path("id").asLong();

        mockMvc.perform(post("/api/attendance/mark")
                        .header("Authorization", bearer(facultyToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "subjectId", cs301,
                                "attendanceDate", "2026-09-21",
                                "entries", List.of(Map.of(
                                        "studentId", studentId,
                                        "status", "PRESENT"))))))
                .andExpect(status().isOk());
    }

    @Test
    void facultyCannotMarkAttendanceForForeignSubject() throws Exception {
        String faculty1Token = login("faculty1@smartcampus.edu", "Faculty@123");
        String faculty2Token = login("faculty2@smartcampus.edu", "Faculty@123");

        // CS302 is taught by faculty2.
        JsonNode f2Subjects = getData("/api/faculty/me/subjects", faculty2Token);
        long cs302 = subjectId(f2Subjects, "CS302");

        long cs302Student = 0;
        for (JsonNode s : getData("/api/faculty/subjects/" + cs302 + "/students", faculty2Token)) {
            cs302Student = s.path("id").asLong();
            break;
        }

        mockMvc.perform(post("/api/attendance/mark")
                        .header("Authorization", bearer(faculty1Token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "subjectId", cs302,
                                "attendanceDate", "2026-09-21",
                                "entries", List.of(Map.of(
                                        "studentId", cs302Student,
                                        "status", "PRESENT"))))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanMarkAttendanceForAnySubject() throws Exception {
        String adminToken = login("admin@smartcampus.edu", "Admin@123");
        String faculty2Token = login("faculty2@smartcampus.edu", "Faculty@123");

        JsonNode f2Subjects = getData("/api/faculty/me/subjects", faculty2Token);
        long cs302 = subjectId(f2Subjects, "CS302");
        long cs302Student = getData("/api/faculty/subjects/" + cs302 + "/students", faculty2Token)
                .get(0).path("id").asLong();

        mockMvc.perform(post("/api/attendance/mark")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "subjectId", cs302,
                                "attendanceDate", "2026-09-21",
                                "entries", List.of(Map.of(
                                        "studentId", cs302Student,
                                        "status", "ABSENT"))))))
                .andExpect(status().isOk());
    }

    @Test
    void studentCannotMarkAttendance() throws Exception {
        String studentToken = login("student1@smartcampus.edu", "Student@123");
        mockMvc.perform(post("/api/attendance/mark")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":1,\"attendanceDate\":\"2026-09-21\",\"entries\":[]}"))
                .andExpect(status().isForbidden());
    }
}