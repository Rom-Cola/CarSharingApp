package com.loievroman.carsharingapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RentalControllerTest {

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
    @DisplayName("Create a new rental")
    @WithUserDetails(value = "customer@example.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Sql(scripts = "classpath:database/rentals/add-rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void createRental_ValidRequest_Success() throws Exception {
        // given
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto();
        requestDto.setCarId(1L);
        requestDto.setReturnDate(LocalDate
                .now()
                .plusDays(5));

        // when
        MvcResult result = mockMvc
                .perform(post("/rentals")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        RentalDto actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), RentalDto.class);
        assertNotNull(actual.getId());
        assertEquals(requestDto.getCarId(), actual.getCarId());
    }

    @Test
    @DisplayName("Get rentals by user and status")
    @WithUserDetails(value = "customer@example.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Sql(scripts = "classpath:database/rentals/add-rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getRentalsByUserAndStatus_ReturnsRentals() throws Exception {
        // when
        MvcResult result = mockMvc
                .perform(get("/rentals/my").param("is_active", "true"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        PageWrapper<RentalDto> actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), objectMapper
                .getTypeFactory()
                .constructParametricType(PageWrapper.class, RentalDto.class));
        assertEquals(1, actual.getTotalElements());
    }

    @Test
    @DisplayName("Get rental by ID")
    @WithUserDetails(value = "customer@example.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Sql(scripts = "classpath:database/rentals/add-rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getRentalById_ValidId_ReturnsRental() throws Exception {
        // when
        MvcResult result = mockMvc
                .perform(get("/rentals/my/1"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        RentalDto actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), RentalDto.class);
        assertEquals(1L, actual.getId());
    }

    @Test
    @DisplayName("Set actual return date")
    @WithMockUser(roles = "MANAGER")
    @Sql(scripts = "classpath:database/rentals/add-rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void setActualReturnDate_ValidId_Success() throws Exception {
        // when
        MvcResult result = mockMvc
                .perform(post("/rentals/1/return"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        RentalDto actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), RentalDto.class);
        RentalDto updatedRental = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), RentalDto.class);
        assertNotNull(updatedRental.getActualReturnDate());
    }

    @Data
    private static class PageWrapper<T> {
        private List<T> content;
        private long totalElements;
    }
}
