package com.loievroman.carsharingapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loievroman.carsharingapp.dto.car.CarDto;
import com.loievroman.carsharingapp.dto.car.CreateCarRequestDto;
import com.loievroman.carsharingapp.model.CarType;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Sql(
        scripts = {
                "classpath:database/remove-all-data.sql",
                "classpath:database/add-car.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class CarControllerTest {

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
    @DisplayName("Create a new car")
    @WithMockUser(roles = "MANAGER")
    void createCar_ValidRequestDto_Success() throws Exception {
        // given
        CreateCarRequestDto requestDto = new CreateCarRequestDto()
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(CarType.SEDAN)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(200.00));

        // when
        MvcResult result = mockMvc
                .perform(post("/cars")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        CarDto actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), CarDto.class);
        assertNotNull(actual.getId());
        assertEquals(requestDto.getModel(), actual.getModel());
    }

    @Test
    @DisplayName("Get all cars")
    @WithMockUser
    void getAll_ReturnsListOfCars() throws Exception {
        // when
        MvcResult result = mockMvc
                .perform(get("/cars"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        PageWrapper<CarDto> actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), objectMapper
                .getTypeFactory()
                .constructParametricType(PageWrapper.class, CarDto.class));
        assertEquals(1, actual.getTotalElements());
    }

    @Test
    @DisplayName("Get car by ID")
    @WithMockUser
    void getCarById_ValidId_ReturnsCar() throws Exception {
        // when
        MvcResult result = mockMvc
                .perform(get("/cars/1"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        CarDto actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), CarDto.class);
        assertEquals(1L, actual.getId());
    }

    @Test
    @DisplayName("Update a car")
    @WithMockUser(roles = "MANAGER")
    void updateCar_ValidRequest_Success() throws Exception {
        // given
        CreateCarRequestDto requestDto = new CreateCarRequestDto()
                .setModel("Q7")
                .setBrand("Audi")
                .setType(CarType.SUV)
                .setInventory(15)
                .setDailyFee(BigDecimal.valueOf(180.00));

        // when
        MvcResult result = mockMvc
                .perform(put("/cars/1")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        CarDto actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), CarDto.class);
        assertEquals("Q7", actual.getModel());
        assertEquals(15, actual.getInventory());
    }

    @Test
    @DisplayName("Delete a car")
    @WithMockUser(roles = "MANAGER")
    void deleteCar_ValidId_Success() throws Exception {
        // when & then
        mockMvc
                .perform(delete("/cars/1"))
                .andExpect(status().isNoContent());
    }

    @Data
    private static class PageWrapper<T> {
        private List<T> content;
        private long totalElements;
    }
}
