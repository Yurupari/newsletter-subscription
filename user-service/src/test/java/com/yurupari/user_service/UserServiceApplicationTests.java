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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.yurupari.user_service.utils.TestConstants.LOGIN_REQUEST_JSON;
import static com.yurupari.user_service.utils.TestConstants.UPDATE_USER_REQUEST_JSON;
import static com.yurupari.user_service.utils.TestConstants.USER_REQUEST_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
	private JsonTestUtils jsonTestUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();

		jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
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
				null,
				null
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
				null,
				null,
				null
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
				null,
				null,
				null
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
				null,
				null,
				null
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
				null,
				null,
				null
		));
		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"erika.doe@test.com",
				"encryptedPassword2",
				"Erika",
				"Doe",
				UserStatus.ACTIVE,
				null,
				null,
				null
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
				null,
				null,
				null
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
				null,
				null,
				null
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
				null,
				null,
				null
		));

		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"erika.doe@test.com",
				"encryptedPassword2",
				"Erika",
				"Doe",
				UserStatus.ACTIVE,
				null,
				null,
				null
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
				null,
				null,
				null
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
				null,
				null,
				null
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
				null,
				null,
				null
		));
		userRepository.saveAndFlush(TestModelFactory.createUser(
				null,
				"erika.doe@test.com",
				"encryptedPassword2",
				"Erika",
				"Doe",
				UserStatus.ACTIVE,
				null,
				null,
				null
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
				null,
				null,
				null
		);
		userRepository.saveAndFlush(user);

		var authenticationResponse = TestModelFactory.createAuthenticationResponse(
				"accessToken",
				"refreshToken",
				"Bearer",
				3600L
		);
		when(keycloakClient.authenticate(anyString(), any())).thenReturn(authenticationResponse);

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
				null,
				null,
				null
		);
		userRepository.saveAndFlush(user);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isUnauthorized());
	}
}
