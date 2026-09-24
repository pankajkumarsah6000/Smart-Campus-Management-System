package com.smartcampus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Login/authentication flow and coarse role-based authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testauth;DB_CLOSE_DELAY=-1")
class AuthFlowTest extends IntegrationTestBase {

    @Test
    void allFourRolesCanLogIn() throws Exception {
        String[][] creds = {
                {"admin@smartcampus.edu", "Admin@123"},
                {"faculty1@smartcampus.edu", "Faculty@123"},
                {"student1@smartcampus.edu", "Student@123"},
                {"parent@smartcampus.edu", "Parent@123"},
        };
        for (String[] cred : creds) {
            String token = login(cred[0], cred[1]);
            assertThat(token).isNotBlank();
        }
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@smartcampus.edu\",\"password\":\"definitely-wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/students/me/dashboard"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotAccessAdminEndpoints() throws Exception {
        String token = login("student1@smartcampus.edu", "Student@123");
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminEndpoints() throws Exception {
        String token = login("admin@smartcampus.edu", "Admin@123");
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudents").value(3))
                .andExpect(jsonPath("$.data.totalFaculty").value(2));
    }

    @Test
    void parentCannotUseStudentEndpoints() throws Exception {
        String parentToken = login("parent@smartcampus.edu", "Parent@123");
        mockMvc.perform(get("/api/students/me/dashboard").header("Authorization", bearer(parentToken)))
                .andExpect(status().isForbidden());
    }
}