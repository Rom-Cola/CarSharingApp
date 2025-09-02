package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import java.util.List;

public interface PaymentService {
    List<PaymentDto> getPayments(Long userId);
    // TODO: add methods: createPaymentSession(), handleSuccess(), handleCancel()
}
