package com.smartcampus;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * User-scoped module endpoints exercised from each role:
 * notifications, leave, complaints, fees, timetable, exams.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testusermod;DB_CLOSE_DELAY=-1")
class UserScopedModuleTest extends IntegrationTestBase {

    @Test
    void notificationsWorkForAuthenticatedUsers() throws Exception {
        String student = login("student1@smartcampus.edu", "Student@123");

        mockMvc.perform(get("/api/notifications/me").header("Authorization", bearer(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/notifications/me/unread-count").header("Authorization", bearer(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").isNumber());

        mockMvc.perform(put("/api/notifications/me/read-all").header("Authorization", bearer(student)))
                .andExpect(status().isOk());
    }

    @Test
    void leavePendingIsScoped() throws Exception {
        String admin = login("admin@smartcampus.edu", "Admin@123");
        String faculty = login("faculty1@smartcampus.edu", "Faculty@123");
        String student = login("student1@smartcampus.edu", "Student@123");

        mockMvc.perform(get("/api/leave/pending").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/leave/pending").header("Authorization", bearer(faculty)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leave/pending").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void complaintsAreScopedPerRole() throws Exception {
        String admin = login("admin@smartcampus.edu", "Admin@123");
        String student = login("student1@smartcampus.edu", "Student@123");

        // Students can raise complaints and read their own
        MvcResult raised = mockMvc.perform(post("/api/complaints")
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Library timings\",\"description\":\"Extend evening hours\",\"category\":\"INFRASTRUCTURE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();
        long complaintId = dataOf(raised).path("id").asLong();

        mockMvc.perform(get("/api/complaints/me").header("Authorization", bearer(student)))
                .andExpect(status().isOk());

        // Students cannot list all complaints
        mockMvc.perform(get("/api/complaints").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());

        // Admin sees it and can resolve it (this also exercises the COMPLAINT notification)
        mockMvc.perform(get("/api/complaints").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(put("/api/complaints/" + complaintId + "/resolve")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\",\"resolutionNote\":\"Timings extended\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    void feesAreScopedPerRole() throws Exception {
        String admin = login("admin@smartcampus.edu", "Admin@123");
        String student = login("student1@smartcampus.edu", "Student@123");

        mockMvc.perform(get("/api/fees").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/fees/me").header("Authorization", bearer(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/fees").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void timetableMyIsAvailableToStudentFacultyAndAdmin() throws Exception {
        mockMvc.perform(get("/api/timetable/my").header("Authorization", bearer(login("student1@smartcampus.edu", "Student@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/timetable/my").header("Authorization", bearer(login("faculty1@smartcampus.edu", "Faculty@123"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/timetable/my").header("Authorization", bearer(login("admin@smartcampus.edu", "Admin@123"))))
                .andExpect(status().isOk());
    }

    @Test
    void examsAreScopedPerRole() throws Exception {
        String admin = login("admin@smartcampus.edu", "Admin@123");
        String faculty = login("faculty1@smartcampus.edu", "Faculty@123");
        String student = login("student1@smartcampus.edu", "Student@123");

        mockMvc.perform(get("/api/exams").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/exams").header("Authorization", bearer(faculty)))
                .andExpect(status().isOk());

        // Students use the scoped endpoints instead of the admin/faculty list
        mockMvc.perform(get("/api/exams").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/exams/my").header("Authorization", bearer(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/exams/next").header("Authorization", bearer(student)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/exams/results/me").header("Authorization", bearer(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void seededDepartmentsFacultyAndStudentsAreConsistent() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");
        JsonNode dashboard = getData("/api/admin/dashboard", token);
        assertThat(dashboard.path("totalStudents").asInt()).isEqualTo(3);
        assertThat(dashboard.path("totalFaculty").asInt()).isEqualTo(2);
        assertThat(dashboard.path("totalDepartments").asInt()).isPositive();
    }
}