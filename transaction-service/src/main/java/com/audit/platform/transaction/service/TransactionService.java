package com.audit.platform.transaction.service;

import com.audit.platform.transaction.dto.request.TransactionRequestDTO;
import com.audit.platform.transaction.dto.response.TransactionDetailResponseDTO;
import com.audit.platform.transaction.dto.response.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {

    TransactionResponseDTO createTransaction(TransactionRequestDTO request);

    TransactionResponseDTO getTransactionById(Long id);

    TransactionDetailResponseDTO getTransactionDetails(Long id);

    List<TransactionResponseDTO> getAllTransactions();

    List<TransactionResponseDTO> getTransactionsBySupplierId(Long supplierId);

    TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO request);

    void deleteTransaction(Long id);
}
