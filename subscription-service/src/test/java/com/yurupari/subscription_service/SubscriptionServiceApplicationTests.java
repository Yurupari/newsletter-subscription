package com.yurupari.subscription_service;

import com.yurupari.subscription_service.client.UserServiceClient;
import com.yurupari.subscription_service.exception.UserServiceClientException;
import com.yurupari.subscription_service.model.enums.SubscriptionStatus;
import com.yurupari.subscription_service.repository.NewsletterRepository;
import com.yurupari.subscription_service.repository.OptInRepository;
import com.yurupari.subscription_service.repository.UserSubscriptionRepository;
import com.yurupari.subscription_service.support.PostgreSQLTestcontainerBase;
import com.yurupari.subscription_service.utils.JsonTestUtils;
import com.yurupari.subscription_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yurupari.subscription_service.utils.TestConstants.NEWSLETTER_REQUEST_JSON;
import static com.yurupari.subscription_service.utils.TestConstants.SUBSCRIPTION_REQUEST_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedKafka(partitions = 1, topics = {"confirm-subscription", "unsubscribe"})
@ActiveProfiles("test")
class SubscriptionServiceApplicationTests extends PostgreSQLTestcontainerBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserSubscriptionRepository userSubscriptionRepository;

	@Autowired
	private NewsletterRepository newsletterRepository;

	@Autowired
	private OptInRepository optInRepository;

	@MockitoBean
	private UserServiceClient userServiceClient;

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private KafkaListenerEndpointRegistry registry;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Autowired
	private JsonTestUtils jsonTestUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		userSubscriptionRepository.deleteAll();
		newsletterRepository.deleteAll();
		optInRepository.deleteAll();

		jdbcTemplate.execute("TRUNCATE TABLE user_subscription, newsletter, opt_in RESTART IDENTITY CASCADE");

		for (MessageListenerContainer container : registry.getListenerContainers()) {
			ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
		}
	}

	@Test
	void contextLoads() {
	}

	@Test
	void getNewsletters_Success() throws Exception {
		var newsletters = IntStream.rangeClosed(1, 10)
				.mapToObj(i -> TestModelFactory.buildNewsletter(
						null,
						"Newsletter " + i,
						"Description " + i,
						true,
						null,
						null
				))
				.collect(Collectors.toCollection(ArrayList::new));
		newsletters.add(TestModelFactory.buildNewsletter(
				null,
				"Newsletter 10",
				"Description 10",
				false,
				null,
				null
		));
		newsletterRepository.saveAllAndFlush(newsletters);

		mockMvc.perform(get("/api/v1/subscription/newsletters"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.size()").value(10))
				.andExpect(jsonPath("$.totalElements").value(10))
				.andExpect(jsonPath("$.totalPages").value(1));
	}

	@Test
	void getNewsletters_Success_WithPagination() throws Exception {
		var newsletters = IntStream.rangeClosed(1, 10)
				.mapToObj(i -> TestModelFactory.buildNewsletter(
						null,
						"Newsletter " + i,
						"Description " + i,
						true,
						null,
						null
				))
				.toList();
		newsletterRepository.saveAllAndFlush(newsletters);

		mockMvc.perform(get("/api/v1/subscription/newsletters?page=0&size=5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.size()").value(5))
				.andExpect(jsonPath("$.totalElements").value(10))
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void getNewsletters_Success_NoElements() throws Exception {
		mockMvc.perform(get("/api/v1/subscription/newsletters?page=0&size=5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.size()").value(0))
				.andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.totalPages").value(0));
	}

	@Test
	void registerNewsletter_Success() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(NEWSLETTER_REQUEST_JSON);

		mockMvc.perform(post("/api/v1/subscription/newsletter")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.title").value("New Newsletter"))
				.andExpect(jsonPath("$.description").value("A brand new newsletter"))
				.andExpect(jsonPath("$.isActive").value(true));
	}

	@Test
	void deleteNewsletter_Success() throws Exception {
		var newsletter = newsletterRepository.saveAndFlush(TestModelFactory.buildNewsletter(
				null,
				"Newsletter",
				"Description",
				true,
				null,
				null
		));

		mockMvc.perform(delete("/api/v1/subscription/newsletter/" + newsletter.getId()))
				.andExpect(status().isNoContent());

		var updatedNewsletter = newsletterRepository.findById(newsletter.getId());
		assertTrue(updatedNewsletter.isPresent());
		assertFalse(updatedNewsletter.get().getIsActive());
	}

	@Test
	void deleteNewsletter_Fail_NotFound() throws Exception {
		mockMvc.perform(delete("/api/v1/subscription/newsletter/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void getSubscriptions_Success() throws Exception {
		var newsletter = newsletterRepository.saveAndFlush(TestModelFactory.buildNewsletter(
				null,
				"Newsletter",
				"Description",
				true,
				null,
				null
		));

		var userSubscriptionPending = userSubscriptionRepository.saveAndFlush(TestModelFactory.buildUserSubscription(
				null,
				1L,
				newsletter.getId(),
				SubscriptionStatus.PENDING_CONFIRMATION,
				null,
				null
		));
		var userSubscriptionConfirmed = userSubscriptionRepository.saveAndFlush(TestModelFactory.buildUserSubscription(
				null,
				1L,
				newsletter.getId(),
				SubscriptionStatus.CONFIRMED,
				null,
				null
		));
		var userSubscriptionUnsubscribed = userSubscriptionRepository.saveAndFlush(TestModelFactory.buildUserSubscription(
				null,
				1L,
				newsletter.getId(),
				SubscriptionStatus.UNSUBSCRIBED,
				null,
				null
		));
		userSubscriptionRepository.saveAllAndFlush(List.of(
				userSubscriptionPending,
				userSubscriptionConfirmed,
				userSubscriptionUnsubscribed
		));

		mockMvc.perform(get("/api/v1/subscription/user/1/subscriptions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2));
	}

	@Test
	void subscribe_Success() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(SUBSCRIPTION_REQUEST_JSON);

		var user = TestModelFactory.buildUser(
				1L,
				"john.doe@email",
				"John",
				"Doe"
		);
		when(userServiceClient.getUser(anyLong())).thenReturn(user);

		var newsletter = newsletterRepository.saveAndFlush(TestModelFactory.buildNewsletter(
				null,
				"Newsletter",
				"Description",
				true,
				null,
				null
		));

		mockMvc.perform(post("/api/v1/subscription/user/1/subscription")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.userId").value(user.id()))
				.andExpect(jsonPath("$.newsletter.id").value(newsletter.getId()))
				.andExpect(jsonPath("$.newsletter.title").value(newsletter.getTitle()))
				.andExpect(jsonPath("$.newsletter.description").value(newsletter.getDescription()))
				.andExpect(jsonPath("$.newsletter.isActive").value(newsletter.getIsActive()))
				.andExpect(jsonPath("$.status").value(SubscriptionStatus.PENDING_CONFIRMATION.name()));
	}

	@Test
	void subscribe_Fail_AlreadySubscribed() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(SUBSCRIPTION_REQUEST_JSON);

		var user = TestModelFactory.buildUser(
				1L,
				"john.doe@email",
				"John",
				"Doe"
		);
		when(userServiceClient.getUser(anyLong())).thenReturn(user);

		var newsletter = newsletterRepository.saveAndFlush(TestModelFactory.buildNewsletter(
				null,
				"Newsletter",
				"Description",
				true,
				null,
				null
		));

		var userSubscription = userSubscriptionRepository.saveAndFlush(TestModelFactory.buildUserSubscription(
				null,
				1L,
				newsletter.getId(),
				SubscriptionStatus.CONFIRMED,
				null,
				null
		));

		mockMvc.perform(post("/api/v1/subscription/user/1/subscription")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isConflict());
	}

	@Test
	void subscribe_Fail_UserNotFound() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(SUBSCRIPTION_REQUEST_JSON);

		when(userServiceClient.getUser(anyLong()))
				.thenThrow(new UserServiceClientException(HttpStatusCode.valueOf(404), "1", "User not found"));

		mockMvc.perform(post("/api/v1/subscription/user/1/subscription")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isNotFound());
	}

	@Test
	void subscribe_Fail_NewsletterNotFound() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(SUBSCRIPTION_REQUEST_JSON);

		var user = TestModelFactory.buildUser(
				1L,
				"john.doe@email",
				"John",
				"Doe"
		);
		when(userServiceClient.getUser(anyLong())).thenReturn(user);

		mockMvc.perform(post("/api/v1/subscription/user/1/subscription")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isNotFound());
	}

	@Test
	void subscribe_Fail_UserClientError() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(SUBSCRIPTION_REQUEST_JSON);

		when(userServiceClient.getUser(anyLong())).thenThrow(new RestClientException("User client error"));

		mockMvc.perform(post("/api/v1/subscription/user/1/subscription")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void confirmSubscription_Success() throws Exception {
		var userSubscription = userSubscriptionRepository.saveAndFlush(TestModelFactory.buildUserSubscription(
				null,
				1L,
				1L,
				SubscriptionStatus.PENDING_CONFIRMATION,
				null,
				null
		));
		var optIn = optInRepository.saveAndFlush(TestModelFactory.buildOptIn(
				null,
				userSubscription.getId(),
				UUID.randomUUID().toString(),
				Instant.now().plusSeconds(3600)
		));

		mockMvc.perform(post("/api/v1/subscription/confirm")
						.param("token", optIn.getToken()))
				.andExpect(status().isNoContent());

		var updatedSubscription = userSubscriptionRepository.findById(userSubscription.getId());
		assertTrue(updatedSubscription.isPresent());
		assertEquals(SubscriptionStatus.CONFIRMED, updatedSubscription.get().getStatus());

		var updatedOptIn = optInRepository.findById(optIn.getId());
		assertTrue(updatedOptIn.isPresent());
		assertNotNull(updatedOptIn.get().getUsedAt());
	}

	@Test
	void confirmSubscription_Fail_TokenNotFound() throws Exception {
		mockMvc.perform(post("/api/v1/subscription/confirm")
						.param("token", UUID.randomUUID().toString()))
				.andExpect(status().isNoContent());
	}

	@Test
	void confirmSubscription_Fail_TokenExpired() throws Exception {
		var userSubscription = userSubscriptionRepository.saveAndFlush(TestModelFactory.buildUserSubscription(
				null,
				1L,
				1L,
				SubscriptionStatus.PENDING_CONFIRMATION,
				null,
				null
		));
		var optIn = optInRepository.saveAndFlush(TestModelFactory.buildOptIn(
				null,
				userSubscription.getId(),
				UUID.randomUUID().toString(),
				Instant.now().minusSeconds(3600)
		));

		mockMvc.perform(post("/api/v1/subscription/confirm")
						.param("token", optIn.getToken()))
				.andExpect(status().isNoContent());
	}

	@Test
	void confirmSubscription_Fail_TokenAlreadyUsed() throws Exception {
		var userSubscription = userSubscriptionRepository.saveAndFlush(TestModelFactory.buildUserSubscription(
				null,
				1L,
				1L,
				SubscriptionStatus.CONFIRMED,
				null,
				null
		));
		var optIn = optInRepository.saveAndFlush(TestModelFactory.buildOptIn(
				null,
				userSubscription.getId(),
				UUID.randomUUID().toString(),
				Instant.now().plusSeconds(3600)
		));
		optIn.setUsedAt(Instant.now());
		optInRepository.saveAndFlush(optIn);

		mockMvc.perform(post("/api/v1/subscription/confirm")
						.param("token", optIn.getToken()))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteSubscription_Success() throws Exception {
		var userSubscription = userSubscriptionRepository.saveAndFlush(TestModelFactory.buildUserSubscription(
				null,
				1L,
				1L,
				SubscriptionStatus.CONFIRMED,
				null,
				null
		));

		mockMvc.perform(delete("/api/v1/subscription/user/1/subscription/" + userSubscription.getId()))
				.andExpect(status().isNoContent());

		var updatedSubscription = userSubscriptionRepository.findById(userSubscription.getId());
		assertTrue(updatedSubscription.isPresent());
		assertEquals(SubscriptionStatus.UNSUBSCRIBED, updatedSubscription.get().getStatus());
	}

	@Test
	void deleteSubscription_Fail_NotFound() throws Exception {
		mockMvc.perform(delete("/api/v1/subscription/user/1/subscription/999"))
				.andExpect(status().isNotFound());
	}
}
