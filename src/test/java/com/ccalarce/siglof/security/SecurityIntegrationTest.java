package com.ccalarce.siglof.security;

import com.ccalarce.siglof.model.entity.User;
import com.ccalarce.siglof.model.enums.Role;
import com.ccalarce.siglof.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "admin", roles = { "SUPER_ADMIN" })
    public void createVehicle_WhenSuperAdmin_ShouldReturn200() throws Exception {
        String vehicleJson = "{\"plate\": \"ABC-123\", \"brand\": \"Toyota\", \"model\": \"Hilux\", \"capacity\": 50, \"active\": true}";

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleJson))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin_normal", roles = { "ADMIN" })
    public void createVehicle_WhenAdmin_ShouldReturn200() throws Exception {
        String vehicleJson = "{\"plate\": \"XYZ-999\", \"brand\": \"Nissan\", \"model\": \"Frontier\", \"capacity\": 40, \"active\": true}";

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleJson))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "driver", roles = { "REPARTIDOR" })
    public void createVehicle_WhenRepartidor_ShouldReturn403() throws Exception {
        String vehicleJson = "{\"plate\": \"FAIL-000\", \"brand\": \"Kia\", \"model\": \"K2700\", \"capacity\": 10, \"active\": true}";

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleJson))
                .andExpect(status().isForbidden());
    }
}
