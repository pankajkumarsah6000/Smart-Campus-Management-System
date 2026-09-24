package com.smartcampus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared helpers for the integration suite. Each concrete test class configures
 * its own H2 in-memory database (different mem URL) so the Spring TestContext
 * cache starts an isolated, freshly-seeded context per class.
 */
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /** Logs in with the given credentials and returns the bearer token. */
    protected String login(String email, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.path("data").path("accessToken").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected JsonNode dataOf(MvcResult res) throws Exception {
        return objectMapper.readTree(res.getResponse().getContentAsString()).path("data");
    }

    protected JsonNode getData(String url, String token) throws Exception {
        MvcResult res = mockMvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return dataOf(res);
    }
}