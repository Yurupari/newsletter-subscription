package com.yurupari.cpd_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
//@EmbeddedKafka(partitions = 1, topics = {"confirm-subscription"})
@ActiveProfiles("test")
class CpdServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
