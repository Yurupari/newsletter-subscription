package com.yurupari.notification_service;

import com.yurupari.notification_service.client.UserServiceClient;
import com.yurupari.notification_service.utils.TestModelFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@EmbeddedKafka(partitions = 1, topics = {"confirm-subscription"})
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private KafkaListenerEndpointRegistry registry;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@MockitoBean
	private UserServiceClient userServiceClient;

	@MockitoBean
	private JavaMailSender javaMailSender;

	@BeforeEach
	void setUp() {
		for (MessageListenerContainer container : registry.getListenerContainers()) {
			ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
		}
	}

	@Test
	void contextLoads() {
	}

	@Test
	void consumeConfirmSubscriptionEvent_Success() {
		var user = TestModelFactory.buildUserResponse(
				1L,
				"john.doe@email.com",
				"John",
				"Doe"
		);
		when(userServiceClient.getUser(anyLong())).thenReturn(user);

		var confirmSubscriptionEvent = TestModelFactory.buildConfirmSubscriptionEvent(
				1L,
				2L,
				3L,
				"token"
		);
		kafkaTemplate.send("confirm-subscription", confirmSubscriptionEvent);

		Awaitility.await()
				.atMost(Duration.ofSeconds(10))
				.untilAsserted(() -> {
					verify(userServiceClient, times(1)).getUser(anyLong());
					//verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
				});
	}
}
