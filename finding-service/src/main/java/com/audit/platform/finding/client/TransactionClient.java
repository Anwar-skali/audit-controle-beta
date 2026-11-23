package com.audit.platform.finding.client;

import com.audit.platform.finding.dto.external.TransactionDetailResponseDTO;
import com.audit.platform.finding.dto.external.TransactionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "transaction-service", path = "/api/transactions")
public interface TransactionClient {

    @GetMapping("/{id}")
    TransactionResponseDTO getTransactionById(@PathVariable("id") Long id);

    @GetMapping("/{id}/details")
    TransactionDetailResponseDTO getTransactionDetails(@PathVariable("id") Long id);
}

