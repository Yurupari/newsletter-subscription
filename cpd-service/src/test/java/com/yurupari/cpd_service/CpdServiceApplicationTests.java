package com.yurupari.cpd_service;

import com.yurupari.cpd_service.client.CPDClient;
import com.yurupari.cpd_service.model.enums.OutboxAggregateType;
import com.yurupari.cpd_service.model.enums.OutboxEventType;
import com.yurupari.cpd_service.repository.OutboxEventRepository;
import com.yurupari.cpd_service.support.PostgreSQLTestcontainerBase;
import com.yurupari.cpd_service.utils.TestModelFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedKafka(partitions = 1, topics = {"cpd-notification"})
@ActiveProfiles("test")
class CpdServiceApplicationTests extends PostgreSQLTestcontainerBase {

	@Autowired
	private OutboxEventRepository outboxEventRepository;

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private KafkaListenerEndpointRegistry registry;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoBean
	private CPDClient cpdClient;

	@BeforeEach
	void setUp() {
		outboxEventRepository.deleteAll();

		jdbcTemplate.execute("TRUNCATE TABLE outbox_event RESTART IDENTITY CASCADE");

		for (MessageListenerContainer container : registry.getListenerContainers()) {
			ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
		}
	}

	@Test
	void contextLoads() {
	}

	@Test
	void consumeCpdEvent_Success() {
		var outboxEvent = outboxEventRepository.save(TestModelFactory.buildOutboxEvent(
				null,
				OutboxAggregateType.SUBSCRIPTION,
				1L,
				OutboxEventType.NEWSLETTER_SUBSCRIBED,
				"{}",
				0,
				Instant.now(),
				null
		));

		var cpdEvent = TestModelFactory.buildCPDEvent(
				outboxEvent.getId(),
				"NEWSLETTER_SUBSCRIPTION",
				"subscription-service",
				1L,
				Map.of("key", "value")
		);
		kafkaTemplate.send("cpd-notification", cpdEvent);

		Awaitility.await()
				.atMost(Duration.ofSeconds(10))
				.untilAsserted(() -> {
					verify(cpdClient, times(1)).track(any());
				});
	}

}
