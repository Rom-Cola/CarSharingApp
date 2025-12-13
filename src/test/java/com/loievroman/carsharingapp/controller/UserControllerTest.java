package com.loievroman.carsharingapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loievroman.carsharingapp.dto.user.UserProfileResponseDto;
import com.loievroman.carsharingapp.dto.user.UserProfileUpdateRequestDto;
import com.loievroman.carsharingapp.dto.user.UserRoleUpdateRequestDto;
import com.loievroman.carsharingapp.model.Role;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Sql(
        scripts = {
                "classpath:database/remove-all-data.sql",
                "classpath:database/add-users-and-roles.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class UserControllerTest {

    @Autowired
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "manager@example.com", roles = {"MANAGER"})
    @DisplayName("updateUserRoles_ValidRequest_ReturnsOk")
    void updateUserRoles_ValidRequest_ReturnsOk() throws Exception {
        // Given
        Long userId = 1L; // customer user
        UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto();
        requestDto.setRoles(Set.of(Role.RoleName.MANAGER));

        // When & Then
        MvcResult result = mockMvc.perform(put("/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        UserProfileResponseDto expectedDto = new UserProfileResponseDto();
        expectedDto.setId(userId);
        expectedDto.setEmail("customer@example.com");
        expectedDto.setFirstName("John");
        expectedDto.setLastName("Doe");
        expectedDto.setRoles(Set.of(Role.RoleName.MANAGER));

        UserProfileResponseDto actualDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserProfileResponseDto.class
        );

        assertTrue(EqualsBuilder.reflectionEquals(expectedDto, actualDto, "id"));
    }

    @Test
    @WithUserDetails("customer@example.com")
    @DisplayName("getMyProfile_AuthenticatedUser_ReturnsOk")
    void getMyProfile_AuthenticatedUser_ReturnsOk() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserProfileResponseDto actualDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserProfileResponseDto.class
        );

        assertEquals(1L, actualDto.getId());
        assertEquals("customer@example.com", actualDto.getEmail());
        assertEquals("John", actualDto.getFirstName());
        assertEquals("Doe", actualDto.getLastName());
        assertEquals(1, actualDto.getRoles().size());
        assertTrue(actualDto.getRoles().contains(Role.RoleName.CUSTOMER));
    }

    @Test
    @WithUserDetails("customer@example.com")
    @DisplayName("updateMyProfile_ValidRequest_ReturnsOk")
    void updateMyProfile_ValidRequest_ReturnsOk() throws Exception {
        // Given
        UserProfileUpdateRequestDto requestDto = new UserProfileUpdateRequestDto();
        requestDto.setFirstName("Jane");

        // When & Then
        MvcResult result = mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        UserProfileResponseDto expectedDto = new UserProfileResponseDto();
        expectedDto.setId(1L);
        expectedDto.setEmail("customer@example.com");
        expectedDto.setFirstName("Jane");
        expectedDto.setLastName("Doe");
        expectedDto.setRoles(Set.of(Role.RoleName.CUSTOMER));

        UserProfileResponseDto actualDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserProfileResponseDto.class
        );

        assertTrue(EqualsBuilder.reflectionEquals(expectedDto, actualDto, "id"));
    }
}
