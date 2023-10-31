package dev.vality.daway.service;

import dev.vality.daway.handler.wrapper.WrapperHandler;
import dev.vality.daway.model.InvoiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceWrapperService {

    private final List<WrapperHandler<InvoiceWrapper>> invoiceWrapperHandlers;

    public void save(List<InvoiceWrapper> invoiceWrappers) {
        log.info("Start saving of invoice batch, size={}", invoiceWrappers.size());
        invoiceWrapperHandlers.stream()
                .filter(handler -> handler.accept(invoiceWrappers))
                .forEach(handler -> handler.saveBatch(invoiceWrappers));
        log.info("Saved invoice batch");
    }

}
