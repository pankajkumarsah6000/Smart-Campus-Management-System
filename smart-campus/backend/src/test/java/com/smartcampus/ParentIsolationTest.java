package com.smartcampus;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Parent portal: a parent may only view data for their linked children.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testparent;DB_CLOSE_DELAY=-1")
class ParentIsolationTest extends IntegrationTestBase {

    @Test
    void parentSeesOwnChildrenOnly() throws Exception {
        String token = login("parent@smartcampus.edu", "Parent@123");

        JsonNode children = getData("/api/parents/me/children", token);
        assertThat(children.size()).isEqualTo(1);
        assertThat(children.get(0).path("rollNumber").asText()).isEqualTo("ROLL-2024-001");

        long childId = children.get(0).path("id").asLong();

        JsonNode dashboard = getData("/api/parents/children/" + childId + "/dashboard", token);
        assertThat(dashboard.path("profile").path("rollNumber").asText()).isEqualTo("ROLL-2024-001");

        JsonNode results = getData("/api/parents/children/" + childId + "/results", token);
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).path("examinationName").asText()).isEqualTo("Mid-Term Examination");

        JsonNode fees = getData("/api/parents/children/" + childId + "/fees", token);
        assertThat(fees.size()).isGreaterThanOrEqualTo(2);

        JsonNode timetable = getData("/api/parents/children/" + childId + "/timetable", token);
        assertThat(timetable.size()).isGreaterThanOrEqualTo(1);

        JsonNode attendance = getData("/api/parents/children/" + childId + "/attendance", token);
        assertThat(attendance.has("overallPercentage")).isTrue();
    }

    @Test
    void parentCannotAccessUnrelatedChild() throws Exception {
        String parentToken = login("parent@smartcampus.edu", "Parent@123");
        String adminToken = login("admin@smartcampus.edu", "Admin@123");

        JsonNode students = getData("/api/admin/students", adminToken);
        JsonNode student2 = null;
        for (JsonNode s : students) {
            if ("ROLL-2024-002".equals(s.path("rollNumber").asText())) {
                student2 = s;
                break;
            }
        }
        assertThat(student2).isNotNull();

        mockMvc.perform(get("/api/parents/children/" + student2.path("id").asLong() + "/dashboard")
                        .header("Authorization", bearer(parentToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentCannotAccessParentEndpoints() throws Exception {
        String studentToken = login("student1@smartcampus.edu", "Student@123");
        mockMvc.perform(get("/api/parents/me").header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void parentSeesOwnMidTermResultDetails() throws Exception {
        String token = login("parent@smartcampus.edu", "Parent@123");
        JsonNode children = getData("/api/parents/me/children", token);
        long childId = children.get(0).path("id").asLong();

        mockMvc.perform(get("/api/parents/children/" + childId + "/results")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rollNumber").value("ROLL-2024-001"))
                .andExpect(jsonPath("$.data[0].passed").value(true));
    }
}