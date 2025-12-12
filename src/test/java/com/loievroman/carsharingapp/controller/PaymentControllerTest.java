package com.loievroman.carsharingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.dto.payment.PaymentStatusResponseDto;
import com.loievroman.carsharingapp.model.PaymentType;
import com.loievroman.carsharingapp.service.NotificationService;
import com.loievroman.carsharingapp.service.PaymentService;
import com.loievroman.carsharingapp.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
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
class PaymentControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired // Використовуємо реальний UserService для перевірки існування юзера
    private UserService userService;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Get my payments - Authenticated User - Returns Payments")
    @WithUserDetails(value = "customer@example.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getMyPayments_AuthenticatedUser_ReturnsPayments() throws Exception {
        // given
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(1L);
        paymentDto.setAmountToPay(java.math.BigDecimal.valueOf(100));

        when(paymentService.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(paymentDto)));

        // when & then
        mockMvc.perform(get("/payments/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Get all payments - Manager Role - Returns All Payments")
    @WithMockUser(username = "manager", roles = "MANAGER")
    void getAllPayments_Manager_ReturnsAllPayments() throws Exception {
        // given
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(2L);
        when(paymentService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(paymentDto)));

        // when & then
        mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2L));
    }

    @Test
    @DisplayName("Get all payments - Customer Role - Access Denied")
    @WithMockUser(username = "user", roles = "CUSTOMER")
    void getAllPayments_Customer_Forbidden() throws Exception {
        mockMvc.perform(get("/payments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create Payment Session - Valid Request - Returns URL")
    @WithMockUser(roles = "CUSTOMER")
    void createPaymentSession_ValidRequest_ReturnsUrl() throws Exception {
        // given
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto();
        requestDto.setRentalId(10L);
        requestDto.setType(PaymentType.PAYMENT);

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setSessionUrl("http://stripe.test/pay");
        responseDto.setStatus("PENDING");

        when(paymentService.createPaymentSession(any(), any()))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/payments")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionUrl")
                        .value("http://stripe.test/pay"));
    }

    @Test
    @DisplayName("Handle Successful Payment - Valid Session ID - Returns Success Message")
    void handleSuccessfulPayment_ValidSessionId_ReturnsMessage() throws Exception {
        // given
        String sessionId = "sess_123";
        PaymentStatusResponseDto response = new PaymentStatusResponseDto();
        response.setStatus("SUCCESS");
        response.setMessage("Paid");

        when(paymentService.handleSuccessfulPayment(sessionId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/payments/success")
                        .param("session_id", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("Handle Cancelled Payment - Valid Session ID - Returns Cancel Message")
    void handleCancelledPayment_ValidSessionId_ReturnsMessage() throws Exception {
        // given
        String sessionId = "sess_123";
        PaymentStatusResponseDto response = new PaymentStatusResponseDto();
        response.setStatus("CANCELLED");

        when(paymentService.handleCancelledPayment(sessionId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/payments/cancel")
                        .param("session_id", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
