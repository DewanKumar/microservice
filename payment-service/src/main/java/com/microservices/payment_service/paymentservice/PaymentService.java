package com.microservices.payment_service.paymentservice;

import com.microservices.payment_service.entity.Payment;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface PaymentService {
    Payment createPayment(Payment payment);

    List<Payment> getAllPayments();

    Payment getPaymentById(Long id);
}
