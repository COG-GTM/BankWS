package com.github.rshtishi.bankws.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class TransactionClientIndexTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void findByClientQueryUsesClientIndex() {
		List<String> plan = jdbcTemplate.queryForList(
				"EXPLAIN SELECT * FROM bank_transaction WHERE client = 'rshtishi'", String.class);
		String joined = String.join("\n", plan).toLowerCase(Locale.ROOT);
		assertTrue(joined.contains("idx_bank_transaction_client"), "expected client index in plan: " + joined);
	}

}
