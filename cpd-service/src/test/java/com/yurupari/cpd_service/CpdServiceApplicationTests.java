package com.yurupari.cpd_service;

import com.yurupari.cpd_service.support.PostgreSQLTestcontainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedKafka(partitions = 1, topics = {"cpd-notification"})
@ActiveProfiles("test")
class CpdServiceApplicationTests extends PostgreSQLTestcontainerBase {

	@Test
	void contextLoads() {
	}

}
