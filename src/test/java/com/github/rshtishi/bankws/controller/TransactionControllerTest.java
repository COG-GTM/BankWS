package com.github.rshtishi.bankws.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.rshtishi.bankws.entity.Transaction;
import com.github.rshtishi.bankws.enums.ActionType;
import com.github.rshtishi.bankws.repository.TransactionRepository;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TransactionRepository transactionRepository;

	@Test
	public void testGetTransactionsReturnsAll() throws Exception {
		Transaction first = new Transaction(1, "rshtishi", LocalDateTime.now(), 500.00, ActionType.DEPOSIT);
		Transaction second = new Transaction(2, "jdoe", LocalDateTime.now(), 250.00, ActionType.WITHDRAW);
		when(transactionRepository.findAll()).thenReturn(Arrays.asList(first, second));

		mockMvc.perform(get("/api/transactions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].client").value("rshtishi"));

		verify(transactionRepository).findAll();
	}

	@Test
	public void testGetTransactionsFiltersByClient() throws Exception {
		Transaction transaction = new Transaction(1, "rshtishi", LocalDateTime.now(), 500.00, ActionType.DEPOSIT);
		when(transactionRepository.findByClient("rshtishi")).thenReturn(Collections.singletonList(transaction));

		mockMvc.perform(get("/api/transactions").param("client", "rshtishi"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].client").value("rshtishi"));

		verify(transactionRepository).findByClient("rshtishi");
	}

}
