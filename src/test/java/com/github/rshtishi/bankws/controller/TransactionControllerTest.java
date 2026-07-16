package com.github.rshtishi.bankws.controller;

import static org.mockito.Mockito.never;
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

	private Transaction transaction(int id, String client, double amount, ActionType actionType) {
		return new Transaction(id, client, LocalDateTime.now(), amount, actionType);
	}

	@Test
	public void testGetTransactionsReturnsAllWhenNoParam() throws Exception {
		when(transactionRepository.findAll()).thenReturn(Arrays.asList(
				transaction(1, "rshtishi", 500.00, ActionType.DEPOSIT),
				transaction(2, "jdoe", 250.00, ActionType.WITHDRAW)));

		mockMvc.perform(get("/api/transactions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].client").value("rshtishi"))
				.andExpect(jsonPath("$[0].amount").value(500.00))
				.andExpect(jsonPath("$[0].actionType").value("DEPOSIT"))
				.andExpect(jsonPath("$[1].client").value("jdoe"));

		verify(transactionRepository).findAll();
		verify(transactionRepository, never()).findByClient(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	public void testGetTransactionsReturnsEmptyWhenNoData() throws Exception {
		when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/api/transactions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));

		verify(transactionRepository).findAll();
	}

	@Test
	public void testGetTransactionsFiltersByClient() throws Exception {
		when(transactionRepository.findByClient("rshtishi")).thenReturn(Collections.singletonList(
				transaction(1, "rshtishi", 500.00, ActionType.DEPOSIT)));

		mockMvc.perform(get("/api/transactions").param("client", "rshtishi"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].client").value("rshtishi"));

		verify(transactionRepository).findByClient("rshtishi");
		verify(transactionRepository, never()).findAll();
	}

	@Test
	public void testGetTransactionsByClientWithNoMatchesReturnsEmpty() throws Exception {
		when(transactionRepository.findByClient("nobody")).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/api/transactions").param("client", "nobody"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));

		verify(transactionRepository).findByClient("nobody");
		verify(transactionRepository, never()).findAll();
	}

	@Test
	public void testGetTransactionsWithBlankClientReturnsAll() throws Exception {
		when(transactionRepository.findAll()).thenReturn(Collections.singletonList(
				transaction(1, "rshtishi", 500.00, ActionType.DEPOSIT)));

		mockMvc.perform(get("/api/transactions").param("client", ""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		verify(transactionRepository).findAll();
		verify(transactionRepository, never()).findByClient(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	public void testGetTransactionsWithWhitespaceClientReturnsAll() throws Exception {
		when(transactionRepository.findAll()).thenReturn(Collections.singletonList(
				transaction(1, "rshtishi", 500.00, ActionType.DEPOSIT)));

		mockMvc.perform(get("/api/transactions").param("client", "   "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		verify(transactionRepository).findAll();
		verify(transactionRepository, never()).findByClient(org.mockito.ArgumentMatchers.anyString());
	}

}
