package ch.uzh.ifi.seal.soprafs19.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetUserIn;
import net.minidev.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class UserControllerTest {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private User testUser;
    private JSONObject userJson;
    private GetUserIn testUserIn;

    @Before
    public void setUp() throws Exception {
        testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        testUser = userService.createUser(testUser);

        userJson = new JSONObject();
        userJson.put("username", "testUsername");
        userJson.put("password", "testPassword");

        testUserIn = new GetUserIn();
        testUserIn.setUsername("testUsername");
        testUserIn.setPassword("testPassword");
    }

    @After
    public void end() {
        userRepository.deleteAll();
    }

    @Test
    public void allOk() throws Exception {
        this.mockMvc.perform(
                get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    public void oneIn() throws Exception {
        this.mockMvc.perform(
                get("/users/{userId}", testUser.getId())
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void oneNotIn() throws Exception {
        this.mockMvc.perform(
                get("/users/1000")
                        .header("userToken", "invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void meIn() throws Exception {
        this.mockMvc.perform(
                get("/users/me")
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void meNotIn() throws Exception {
        this.mockMvc.perform(
                get("/users/me")
                        .header("userToken", "invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createUserOk() throws Exception {
        JSONObject userJson3 = new JSONObject();
        userJson3.put("username", "testUsername3");
        userJson3.put("password", "testPassword3");

        this.mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson3.toJSONString()))
                .andExpect(status().isCreated());
    }

    @Test
    public void createUserNotOk() throws Exception {
        userJson.put("password", "testPassword2");

        this.mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isConflict());
    }

    @Test
    public void loginOk() throws Exception {
        this.mockMvc.perform(
                post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    public void loginNotOkForbidden() throws Exception {
        userJson.put("password", "testPassword2");

        this.mockMvc.perform(
                post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void loginNotOkNotFound() throws Exception {
        userJson.put("username", "testUsername2");

        this.mockMvc.perform(
                post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void loginNotOkConflict() throws Exception {
        userService.login(testUserIn);

        this.mockMvc.perform(
                post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isConflict());
    }

    @Test
    public void logoutOk() throws Exception {
        userService.login(testUserIn);

        this.mockMvc.perform(
                post("/users/logout")
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void logoutNotOk() throws Exception {
        this.mockMvc.perform(
                post("/users/logout")
                        .header("userToken","invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUserOk() throws Exception {
        JSONObject userJson2 = new JSONObject();
        userJson2.put("username", "testUsername3");
        userJson2.put("birthday", "01.01.2001");

        this.mockMvc.perform(
                put("/users")
                        .header("userToken", testUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson2.toJSONString())).
                andExpect(status().isNoContent());
    }

    @Test
    public void updateUserNotOkNotFound() throws Exception {
        JSONObject userJson2 = new JSONObject();
        userJson2.put("username", "testUsername");
        userJson2.put("birthday", "01.01.2001");

        this.mockMvc.perform(
                put("/users")
                        .header("userToken","Invalid-token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson2.toJSONString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUserNotOkConflict() throws Exception {
        User testUser2 = new User();
        testUser2.setUsername("testUsername2");
        testUser2.setPassword("testPassword2");
        userService.createUser(testUser2);

        JSONObject userJson2 = new JSONObject();
        userJson2.put("username", "testUsername2");
        userJson2.put("birthday", "01.01.2001");

        this.mockMvc.perform(
                put("/users")
                        .header("userToken", testUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson2.toJSONString()))
                .andExpect(status().isConflict());
    }
}