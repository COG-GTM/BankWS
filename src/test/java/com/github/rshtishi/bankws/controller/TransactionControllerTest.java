package com.github.rshtishi.bankws.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testGetAllTransactions() throws Exception {
		mockMvc.perform(get("/api/transactions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(5));
	}

	@Test
	public void testGetTransactionsForClient() throws Exception {
		mockMvc.perform(get("/api/transactions").param("client", "rshtishi"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].client").value("rshtishi"));
	}

}
