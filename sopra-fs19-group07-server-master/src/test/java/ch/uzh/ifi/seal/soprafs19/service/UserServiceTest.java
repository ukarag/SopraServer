package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.*;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetUser;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetUserIn;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
public class UserServiceTest {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User testUser;
    private User testUser2;
    private GetUserIn testUserData;

    @Before
    public void setUp() {
        try {
            testUser = new User();
            testUser.setUsername("testUsername");
            testUser.setPassword("testPassword");
            testUser = userService.createUser(testUser);

            testUser2 = new User();
            testUser2.setUsername("testUsername2");
            testUser2.setPassword("testPassword2");
            testUser2 = userService.createUser(testUser2);

            testUserData = new GetUserIn();
            testUserData.setUsername("testUsername");
            testUserData.setPassword("testPassword");
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @After
    public void end() {
        userRepository.deleteAll();
    }

    @Test
    public void createUserControllerOk() {
        testUserData.setUsername("testCreateControllerUsername");
        testUserData.setPassword("testCreateControllerPassword");

        Date today = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String todayStr = dateFormat.format(today);

        try {
            User createdUser = userService.createUserController(testUserData);
            Assert.assertNotNull(userRepository.findByUsername("testCreateControllerUsername"));
            Assert.assertEquals(createdUser.getPassword(),testUserData.getPassword());
            Assert.assertNotNull(createdUser.getToken());
            Assert.assertEquals(createdUser, userRepository.findByToken(createdUser.getToken()));
            Assert.assertEquals(createdUser.getStatus(),UserStatus.OFFLINE);
            Assert.assertEquals(createdUser.getCreationDate(), todayStr);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void createUserControllerNotOk() {
        try {
            userService.createUserController(testUserData);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USERNAME_EXISTS) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void createUserOk() {
        try {
            testUser.setUsername("testCreateUsername");
            testUser.setPassword("testCreatePassword");

            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String todayStr = dateFormat.format(today);

            User createdUser = userService.createUser(testUser);
            Assert.assertNotNull(userRepository.findByUsername("testCreateUsername"));
            Assert.assertEquals(createdUser.getPassword(),testUser.getPassword());
            Assert.assertNotNull(createdUser.getToken());
            Assert.assertEquals(createdUser, userRepository.findByToken(createdUser.getToken()));
            Assert.assertEquals(createdUser.getStatus(),UserStatus.OFFLINE);
            Assert.assertEquals(createdUser.getCreationDate(), todayStr);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void createUserNotOk() {
        try {
            userService.createUser(testUser);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USERNAME_EXISTS) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void loginOk() {
        try {
            String foundUserToken = userService.login(testUserData);
            User foundUser = userService.getUserByToken(foundUserToken);
            Assert.assertNotNull(userRepository.findByUsername("testUsername"));
            Assert.assertEquals(foundUser.getPassword(),testUserData.getPassword());
            Assert.assertEquals(foundUser.getStatus(),UserStatus.ONLINE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void loginNotOkForbidden() {
        try {
            testUserData.setPassword("testPassword3");

            userService.login(testUserData);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.NO_MATCH) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void loginNotOkNotFound() {
        try {
            testUserData.setUsername("testUsername3");
            testUserData.setPassword("testPassword3");

            userService.login(testUserData);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void loginNotOkConflict() {
        try {
            userService.login(testUserData);
            userService.login(testUserData);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.ALREADY_LOGGED_IN) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void logoutOk() {
        try {
            String foundUserToken = userService.login(testUserData);

            userService.logout(foundUserToken);
            User foundUser = userRepository.findByToken(foundUserToken);
            Assert.assertNotNull(userRepository.findByUsername("testUsername"));
            Assert.assertEquals(foundUser.getPassword(),testUser.getPassword());
            Assert.assertEquals(foundUser.getStatus(),UserStatus.OFFLINE);
            Assert.assertEquals(foundUser, userRepository.findByToken(foundUser.getToken()));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void logoutNotOk() {
        try {
            userService.logout("token");
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void getUserIsMeOk() {
        try {
            GetUser foundUser = userService.getUser(testUser.getId(), testUser.getToken());
            Assert.assertNotNull(foundUser);
            Assert.assertEquals(foundUser.getUsername(), testUser.getUsername());
            Assert.assertEquals(foundUser.getBirthday(), testUser.getBirthday());
            Assert.assertEquals(foundUser.getCreationDate(), testUser.getCreationDate());
            Assert.assertEquals(foundUser.getStatus(), testUser.getStatus());
            Assert.assertTrue(foundUser.getIsMe());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void getUserIsNotMeOk() {
        try {
            GetUser foundUser = userService.getUser(testUser.getId(), testUser2.getToken());
            Assert.assertNotNull(foundUser);
            Assert.assertEquals(foundUser.getUsername(), "testUsername");
            Assert.assertEquals(foundUser.getBirthday(), testUser.getBirthday());
            Assert.assertEquals(foundUser.getCreationDate(), testUser.getCreationDate());
            Assert.assertEquals(foundUser.getStatus(), testUser.getStatus());
            Assert.assertFalse(foundUser.getIsMe());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void getUserNotOk() {
        try {
            userService.getUserById(10000L);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void getUserByIdOk() {
        try {
            User foundUser = userService.getUserById(testUser.getId());
            Assert.assertNotNull(foundUser);
            Assert.assertEquals(foundUser.getUsername(), testUser.getUsername());
            Assert.assertEquals(foundUser.getPassword(), testUser.getPassword());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void getUserByIdNotOk() {
        try {
            userService.getUserById(10000L);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void getUserByTokenOk() {
        try {
            User foundUser = userService.getUserByToken(testUser.getToken());
            Assert.assertNotNull(foundUser);
            Assert.assertEquals(foundUser.getUsername(), testUser.getUsername());
            Assert.assertEquals(foundUser.getPassword(), testUser.getPassword());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void getUserByTokenNotOk() {
        try {
            userService.getUserByToken("token");
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void getUserIdOk() {
        try {
            Long foundUserId = userService.getUserId(testUser.getToken());
            Assert.assertNotNull(foundUserId);
            Assert.assertEquals(foundUserId, testUser.getId());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void getUserIdNotOk() {
        try {
            userService.getUserId("token");
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void updateUserNotNullOk() {
        try {
            User testUser3 = new User();
            testUser3.setUsername("testUsername3");
            testUser3.setBirthday("01.01.2001");

            userService.updateUser(testUser.getToken(), testUser3);
            User updatedUser = userService.getUserById(testUser.getId());
            Assert.assertEquals(testUser.getId(),testUser.getId());
            Assert.assertNotNull(userRepository.findByUsername("testUsername3"));
            Assert.assertEquals(updatedUser.getUsername(),"testUsername3");
            Assert.assertEquals(updatedUser.getBirthday(),"01.01.2001");
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void updateUserNullOk() {
        try {
            User testUser3 = new User();
            testUser3.setBirthday("01.01.2001");

            userService.updateUser(testUser.getToken(), testUser3);
            User updatedUser = userService.getUserById(testUser.getId());
            Assert.assertNotNull(userRepository.findByUsername("testUsername"));
            Assert.assertEquals(updatedUser.getUsername(),"testUsername");
            Assert.assertEquals(updatedUser.getBirthday(),"01.01.2001");
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void updateUserSameOk() {
        try {
            User testUser3 = new User();
            testUser3.setUsername("testUsername");
            testUser3.setBirthday("01.01.2001");

            userService.updateUser(testUser.getToken(), testUser3);
            User updatedUser = userService.getUserById(testUser.getId());
            Assert.assertNotNull(userRepository.findByUsername("testUsername"));
            Assert.assertEquals(updatedUser.getUsername(),"testUsername");
            Assert.assertEquals(updatedUser.getBirthday(),"01.01.2001");
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void updateBirthdayNullOk() {
        try {
            User testUser3 = new User();
            testUser3.setUsername("testUsername3");

            userService.updateUser(testUser.getToken(), testUser3);
            User updatedUser = userService.getUserById(testUser.getId());
            Assert.assertNotNull(userRepository.findByUsername("testUsername3"));
            Assert.assertEquals(updatedUser.getUsername(),"testUsername3");
            Assert.assertNull(updatedUser.getBirthday());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void updateBirthdayGetNullOk() {
        try {
            User testUser3 = new User();
            testUser3.setUsername("testUsername3");
            testUser.setPassword("01.01.2001");
            userService.updateUser(testUser.getToken(), testUser);

            userService.updateUser(testUser.getToken(), testUser3);
            User updatedUser = userService.getUserById(testUser.getId());
            Assert.assertNotNull(userRepository.findByUsername("testUsername3"));
            Assert.assertEquals(updatedUser.getUsername(),"testUsername3");
            Assert.assertNull(updatedUser.getBirthday());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void updateNotOkConflict() {
        try {
            User testUser3 = new User();
            testUser3.setUsername("testUsername2");

            userService.updateUser(testUser.getToken(), testUser3);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USERNAME_EXISTS) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void updateNotOkNotFound() {
        try {
            User testUser3 = new User();
            testUser.setUsername("testUsername3");
            testUser.setBirthday("01.01.2001");

            userService.updateUser("token", testUser3);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }
}