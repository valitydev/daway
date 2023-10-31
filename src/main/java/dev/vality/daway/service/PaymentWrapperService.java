package dev.vality.daway.service;

import dev.vality.daway.handler.wrapper.WrapperHandler;
import dev.vality.daway.model.PaymentWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWrapperService {

    private final List<WrapperHandler<PaymentWrapper>> paymentWrapperHandlers;

    public void save(List<PaymentWrapper> paymentWrappers) {
        log.info("Start saving of payment batch, size={}", paymentWrappers.size());
        paymentWrapperHandlers.stream()
                .filter(handler -> handler.accept(paymentWrappers))
                .forEach(handler -> handler.saveBatch(paymentWrappers));
        log.info("Saved payment batch");
    }

}