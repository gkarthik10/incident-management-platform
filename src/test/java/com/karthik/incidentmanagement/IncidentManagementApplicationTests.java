package com.karthik.incidentmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Runs against the H2 in-memory DB (see application-test.yml) instead of a
// live Postgres instance, so this — and `mvn test` in general — works on
// any machine or CI runner with zero external setup.
@SpringBootTest
@ActiveProfiles("test")
class IncidentManagementApplicationTests {

	@Test
	void contextLoads() {
		// Verifies the full Spring context (security, JPA, JWT, AI beans,
		// exception handlers) wires up correctly end to end.
	}
}