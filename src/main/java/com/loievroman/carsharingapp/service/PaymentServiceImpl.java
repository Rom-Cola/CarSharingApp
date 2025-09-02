package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.mapper.PaymentMapper;
import com.loievroman.carsharingapp.repository.PaymentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public List<PaymentDto> getPayments(Long userId) {
        // TODO: finish implementation
        return List.of();
    }
}
