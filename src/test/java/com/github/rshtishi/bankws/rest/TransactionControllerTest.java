package com.github.rshtishi.bankws.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransactionControllerTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void testGetAllTransactions() throws Exception {
		mockMvc.perform(get("/api/v1/transactions").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(5)));
	}

	@Test
	public void testGetTransactionsByClient() throws Exception {
		mockMvc.perform(get("/api/v1/transactions").param("client", "rshtishi").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(3)))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].client").value("rshtishi"))
				.andExpect(jsonPath("$[0].amount").value(500.00));
	}

	@Test
	public void testGetTransactionsByClientNotFound() throws Exception {
		mockMvc.perform(get("/api/v1/transactions").param("client", "nonexistent").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(0)));
	}

}
