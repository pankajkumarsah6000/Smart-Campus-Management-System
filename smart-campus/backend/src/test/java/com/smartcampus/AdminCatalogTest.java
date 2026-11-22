package com.smartcampus;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Admin catalog endpoints (departments, courses, notices, subject, faculty-by-id)
 * plus regression checks that unknown/missing paths return 404, not 500.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testcatalog;DB_CLOSE_DELAY=-1")
class AdminCatalogTest extends IntegrationTestBase {

    @Test
    void adminCanListDepartmentsAndCreateAndDelete() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");

        mockMvc.perform(get("/api/admin/departments").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        MvcResult created = mockMvc.perform(post("/api/admin/departments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"QA Dept\",\"code\":\"QA-01\",\"description\":\"ops\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("QA Dept"))
                .andReturn();
        long id = dataOf(created).path("id").asLong();
        assertThat(id).isPositive();

        mockMvc.perform(delete("/api/admin/departments/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanListCoursesAndCreateAndDelete() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");

        mockMvc.perform(get("/api/admin/courses").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        MvcResult created = mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Course\",\"code\":\"TC-001\",\"durationSemesters\":8,\"description\":\"tmp\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("TC-001"))
                .andReturn();
        long id = dataOf(created).path("id").asLong();
        assertThat(id).isPositive();

        mockMvc.perform(delete("/api/admin/courses/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanManageNotices() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");

        mockMvc.perform(get("/api/admin/notices").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        MvcResult created = mockMvc.perform(post("/api/admin/notices")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Mid-sem exams\",\"content\":\"All exams are scheduled\",\"targetAudience\":\"ALL\",\"priority\":\"NORMAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Mid-sem exams"))
                .andReturn();
        long id = dataOf(created).path("id").asLong();
        assertThat(id).isPositive();

        mockMvc.perform(delete("/api/admin/notices/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanListSubjects() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");
        mockMvc.perform(get("/api/admin/subjects").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void adminCanFetchFacultyById() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");
        mockMvc.perform(get("/api/admin/faculty/1").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void missingFacultyReturns404() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");
        mockMvc.perform(get("/api/admin/faculty/999999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownPathsReturn404Not500() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");

        // Unmapped sub-path under an existing controller base
        mockMvc.perform(get("/api/admin/faculty/1/unknown").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());

        // Totally unknown API path
        mockMvc.perform(get("/api/does-not-exist-xyz").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void facultyCannotUseAdminCatalogEndpoints() throws Exception {
        String token = login("faculty1@smartcampus.edu", "Faculty@123");
        mockMvc.perform(get("/api/admin/courses").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/departments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"code\":\"X-1\",\"description\":\"x\"}"))
                .andExpect(status().isForbidden());
    }
}