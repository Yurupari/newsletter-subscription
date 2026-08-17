package com.yurupari.user_service;

import com.yurupari.user_service.client.KeycloakClient;
import com.yurupari.user_service.model.enums.UserStatus;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.repository.UserRepository;
import com.yurupari.user_service.service.EncryptionService;
import com.yurupari.user_service.support.PostgreSQLTestcontainerBase;
import com.yurupari.user_service.utils.JsonTestUtils;
import com.yurupari.user_service.utils.TestModelFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

import static com.yurupari.user_service.utils.TestConstants.LOGIN_REQUEST_JSON;
import static com.yurupari.user_service.utils.TestConstants.UPDATE_USER_REQUEST_JSON;
import static com.yurupari.user_service.utils.TestConstants.USER_REQUEST_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedKafka(partitions = 1, topics = {"register-user", "delete-user"})
@ActiveProfiles("test")
class UserServiceApplicationTests extends PostgreSQLTestcontainerBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EncryptionService encryptionService;

	@MockitoBean
	private KeycloakClient keycloakClient;

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
		userRepository.deleteAll();

		jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");

		for (MessageListenerContainer container : registry.getListenerContainers()) {
			ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
		}
	}

	@Test
	void contextLoads() {
	}

	// --------------- UserControllerV1 ---------------

	@Test
	void registerUser_Success_Created_UserDoesNotExists() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(USER_REQUEST_JSON);
		var userRequest = jsonTestUtils.loadObject(USER_REQUEST_JSON, UserRequest.class);

		mockMvc.perform(post("/api/v1/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.email").value(userRequest.email()))
				.andExpect(jsonPath("$.firstName").value(userRequest.firstName()))
				.andExpect(jsonPath("$.lastName").value(userRequest.lastName()));
	}

	@Test
	void registerUser_Success_Created_UserExistsButDeleted() throws Exception {
		var savedUser = userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"testName",
				"testLastName",
				UserStatus.DELETED,
				null,
				1L,
				Instant.now(),
				Instant.now()
		));

		var requestJson = jsonTestUtils.loadRequest(USER_REQUEST_JSON);
		var userRequest = jsonTestUtils.loadObject(USER_REQUEST_JSON, UserRequest.class);

		mockMvc.perform(post("/api/v1/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.email").value(userRequest.email()))
				.andExpect(jsonPath("$.firstName").value(userRequest.firstName()))
				.andExpect(jsonPath("$.lastName").value(userRequest.lastName()));
	}

	@Test
	void registerUser_Fail_Conflict_UserExistsActive() throws Exception {
		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"testName",
				"testLastName",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));

		var requestJson = jsonTestUtils.loadRequest(USER_REQUEST_JSON);

		mockMvc.perform(post("/api/v1/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isConflict());
	}

	@Test
	void getUserById_Success_Found() throws Exception {
		var savedUser = userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));

		mockMvc.perform(get("/api/v1/user")
						.param("id", savedUser.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.email").value(savedUser.getEmail()))
				.andExpect(jsonPath("$.firstName").value(savedUser.getFirstName()))
				.andExpect(jsonPath("$.lastName").value(savedUser.getLastName()));
	}

	@Test
	void getUserByEmail_Success_Found() throws Exception {
		var savedUser = userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));

		mockMvc.perform(get("/api/v1/user")
						.param("email", savedUser.getEmail()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.email").value(savedUser.getEmail()))
				.andExpect(jsonPath("$.firstName").value(savedUser.getFirstName()))
				.andExpect(jsonPath("$.lastName").value(savedUser.getLastName()));
	}

	@Test
	void getUser_Fail_BadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/user"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getUser_Fail_NotFound() throws Exception {
		mockMvc.perform(get("/api/v1/user")
						.param("id", "999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void getUser_Fail_MultipleUsersFound() throws Exception {
		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));
		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"erika.doe@test.com",
				"encryptedPassword2",
				"Erika",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId2",
				1L,
				Instant.now(),
				Instant.now()
		));

		mockMvc.perform(get("/api/v1/user")
						.param("id", "2")
						.param("email", "john.doe@test.com"))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void updateUserById_Success_Updated() throws Exception {
		var savedUser = userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));
		var requestJson = jsonTestUtils.loadRequest(UPDATE_USER_REQUEST_JSON);

		mockMvc.perform(patch("/api/v1/user")
						.param("id", savedUser.getId().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("John-Updated"))
				.andExpect(jsonPath("$.lastName").value("Doe-Updated"));
	}

	@Test
	void updateUserByEmail_Success_Updated() throws Exception {
		var savedUser = userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));
		var requestJson = jsonTestUtils.loadRequest(UPDATE_USER_REQUEST_JSON);

		mockMvc.perform(patch("/api/v1/user")
						.param("email", savedUser.getEmail())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("John-Updated"))
				.andExpect(jsonPath("$.lastName").value("Doe-Updated"));
	}

	@Test
	void updateUser_Fail_BadRequest() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(UPDATE_USER_REQUEST_JSON);

		mockMvc.perform(patch("/api/v1/user")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateUser_Fail_NotFound() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(UPDATE_USER_REQUEST_JSON);

		mockMvc.perform(patch("/api/v1/user")
						.param("id", "999")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateUser_Fail_MultipleUsersFound() throws Exception {
		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));

		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"erika.doe@test.com",
				"encryptedPassword2",
				"Erika",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId2",
				1L,
				Instant.now(),
				Instant.now()
		));
		var requestJson = jsonTestUtils.loadRequest(UPDATE_USER_REQUEST_JSON);

		mockMvc.perform(patch("/api/v1/user")
						.param("id", "2")
						.param("email", "john.doe@test.com")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void deleteUserById_Success_NoContent() throws Exception {
		var savedUser = userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));

		mockMvc.perform(delete("/api/v1/user")
						.param("id", savedUser.getId().toString()))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteUserByEmail_Success_NoContent() throws Exception {
		var savedUser = userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));

		mockMvc.perform(delete("/api/v1/user")
						.param("email", savedUser.getEmail()))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteUser_Fail_BadRequest() throws Exception {
		mockMvc.perform(delete("/api/v1/user"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deleteUser_Fail_NotFound() throws Exception {
		mockMvc.perform(delete("/api/v1/user")
						.param("id", "999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteUser_Fail_MultipleUsersFound() throws Exception {
		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"john.doe@test.com",
				"encryptedPassword",
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		));
		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"erika.doe@test.com",
				"encryptedPassword2",
				"Erika",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId2",
				1L,
				Instant.now(),
				Instant.now()
		));

		mockMvc.perform(delete("/api/v1/user")
						.param("id", "2")
						.param("email", "john.doe@test.com"))
				.andExpect(status().isInternalServerError());
	}

	// --------------- AuthenticationControllerV1 ---------------

	@Test
	void login_Success() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(LOGIN_REQUEST_JSON);

		var loginRequest = jsonTestUtils.loadObject(LOGIN_REQUEST_JSON, LoginRequest.class);
		var user = TestModelFactory.createUser(
				null,
				loginRequest.email(),
				encryptionService.encrypt(loginRequest.password()),
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		);
		userRepository.saveAndFlush(user);

		var authenticationResponse = TestModelFactory.createAuthenticationResponse(
				"accessToken",
				"refreshToken",
				"Bearer",
				3600L
		);
		when(keycloakClient.authenticateUser(anyString(), any())).thenReturn(authenticationResponse);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").exists())
				.andExpect(jsonPath("$.refresh_token").exists())
				.andExpect(jsonPath("$.token_type").exists())
				.andExpect(jsonPath("$.expires_in").exists());
	}

	@Test
	void login_Fail_UserNotFound_Unauthorized() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(LOGIN_REQUEST_JSON);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_Fail_Unauthorized() throws Exception {
		var requestJson = jsonTestUtils.loadRequest(LOGIN_REQUEST_JSON);

		var loginRequest = jsonTestUtils.loadObject(LOGIN_REQUEST_JSON, LoginRequest.class);
		var user = TestModelFactory.createUser(
				null,
				loginRequest.email(),
				encryptionService.encrypt("different_password"),
				"John",
				"Doe",
				UserStatus.ACTIVE,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		);
		userRepository.saveAndFlush(user);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isUnauthorized());
	}

	// --------------- UserConsumer ---------------

	@Test
	void consumeRegisterUserEvent_Success() {
		var user = TestModelFactory.createUser(
				null,
				"john.doe@email.com",
				encryptionService.encrypt("password"),
				"John",
				"Doe",
				UserStatus.PENDING_ACTIVATION,
				null,
				1L,
				Instant.now(),
				Instant.now()
		);
		var savedUser = userRepository.saveAndFlush(user);

		var authenticationResponse = TestModelFactory.createAuthenticationResponse(
				"accessToken",
				"refreshToken",
				"Bearer",
				3600L
		);
		when(keycloakClient.authenticateClient(anyString(), anyString(), any()))
				.thenReturn(authenticationResponse);

		var locationUri = URI.create("http://localhost:8091/admin/realms/ns-security-realm/users/3d7f9b2a-8c11-4b36-921d-93e18a8f1011");
		ResponseEntity<Void> keycloakCreatedResponse = ResponseEntity.created(locationUri).build();
		when(keycloakClient.createUser(anyString(), anyString(), any())).thenReturn(keycloakCreatedResponse);

		var registerUserEvent = TestModelFactory.createRegisterUserEvent(
				savedUser.getId(),
				savedUser.getEmail(),
				savedUser.getPassword(),
				savedUser.getFirstName(),
				savedUser.getLastName()
		);
		kafkaTemplate.send("register-user", registerUserEvent);

		Awaitility.await()
				.atMost(Duration.ofSeconds(5))
				.untilAsserted(() -> {
					var updatedUser = userRepository.findById(savedUser.getId()).orElse(null);

					assertNotNull(updatedUser);
					assertEquals(UserStatus.ACTIVE, updatedUser.getStatus());
					assertNotNull(updatedUser.getAuthUserId());
				});

		verify(keycloakClient, times(1)).authenticateClient(anyString(), anyString(), any());
		verify(keycloakClient, times(1)).createUser(anyString(), anyString(), any());
	}

	@Test
	void consumeRegisterUserEvent_Fail_KeycloakClientThrowsException() {
		var user = TestModelFactory.createUser(
				null,
				"john.doe@email.com",
				encryptionService.encrypt("password"),
				"John",
				"Doe",
				UserStatus.PENDING_ACTIVATION,
				null,
				1L,
				Instant.now(),
				Instant.now()
		);
		var savedUser = userRepository.saveAndFlush(user);

		when(keycloakClient.authenticateClient(anyString(), anyString(), any()))
				.thenThrow(new RuntimeException("Keycloak client error"));

		var registerUserEvent = TestModelFactory.createRegisterUserEvent(
				savedUser.getId(),
				savedUser.getEmail(),
				savedUser.getPassword(),
				savedUser.getFirstName(),
				savedUser.getLastName()
		);
		kafkaTemplate.send("register-user", registerUserEvent);

		Awaitility.await()
				.atMost(Duration.ofSeconds(5))
				.untilAsserted(() -> {
					var updatedUser = userRepository.findById(savedUser.getId()).orElse(null);

					assertNotNull(updatedUser);
					assertEquals(UserStatus.FAILED_ACTIVATION, updatedUser.getStatus());
					assertNull(updatedUser.getAuthUserId());
				});

		verify(keycloakClient, times(1)).authenticateClient(anyString(), anyString(), any());
		verify(keycloakClient, never()).createUser(anyString(), anyString(), any());
	}

	@Test
	void consumeDeleteUserEvent_Success() {
		var user = TestModelFactory.createUser(
				null,
				"john.doe@email.com",
				encryptionService.encrypt("password"),
				"John",
				"Doe",
				UserStatus.PENDING_DELETION,
				"authUserId",
				1L,
				Instant.now(),
				Instant.now()
		);
		var savedUser = userRepository.saveAndFlush(user);

		var authenticationResponse = TestModelFactory.createAuthenticationResponse(
				"accessToken",
				"refreshToken",
				"Bearer",
				3600L
		);
		when(keycloakClient.authenticateClient(anyString(), anyString(), any()))
				.thenReturn(authenticationResponse);

		doNothing().when(keycloakClient).deleteUser(anyString(), anyString(), anyString());

		var deleteUserEvent = TestModelFactory.createDeleteUserEvent(
				savedUser.getId(),
				savedUser.getAuthUserId()
		);
		kafkaTemplate.send("delete-user", deleteUserEvent);

		Awaitility.await()
				.atMost(Duration.ofSeconds(5))
				.untilAsserted(() -> {
					var updatedUser = userRepository.findById(savedUser.getId()).orElse(null);

					assertNotNull(updatedUser);
					assertEquals(UserStatus.DELETED, updatedUser.getStatus());
					assertNull(updatedUser.getAuthUserId());
				});

		verify(keycloakClient, times(1)).authenticateClient(anyString(), anyString(), any());
		verify(keycloakClient, times(1)).deleteUser(anyString(), anyString(), anyString());
	}
}
