package ch.uzh.ifi.seal.soprafs19.entity;

import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;

import org.junit.Assert;
import org.junit.Test;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserTest {

    private final User user = new User();

    @Test
    public void getAndSetId() {
        Assert.assertNull(user.getId());
        user.setId(1L);
        Assert.assertSame(user.getId(),1L);
    }

    @Test
    public void getAndSetUsername() {
        Assert.assertNull(user.getUsername());
        user.setUsername("testName");
        Assert.assertEquals(user.getUsername(),"testName");
    }

    @Test
    public void getAndSetPassword() {
        Assert.assertNull(user.getPassword());
        user.setPassword("testPassword");
        Assert.assertEquals(user.getPassword(),"testPassword");
    }

    @Test
    public void getAndSetToken() {
        Assert.assertNull(user.getToken());
        user.setToken("testToken");
        Assert.assertEquals(user.getToken(),"testToken");
    }

    @Test
    public void getAndSetStatus() {
        Assert.assertNull(user.getStatus());
        user.setStatus(UserStatus.ONLINE);
        Assert.assertEquals(user.getStatus(), UserStatus.ONLINE);
    }

    @Test
    public void getAndSetCreationDate() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String nowStr = dateFormat.format(now);
        Assert.assertNull(user.getCreationDate());
        user.setCreationDate();
        Assert.assertEquals(user.getCreationDate(), nowStr);
    }

    @Test
    public void getAndSetBirthday() {
        Assert.assertNull(user.getBirthday());
        user.setBirthday("02.02.2002");
        Assert.assertEquals(user.getBirthday(), "02.02.2002");
    }

    @Test
    public void equals() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(1L);
        Assert.assertEquals(user1, user2);
        Assert.assertEquals(user1.hashCode(), user2.hashCode());
        user2.setId(2L);
        Assert.assertNotEquals(user1, user2);
        Assert.assertNotEquals(user1, 1);
        Assert.assertNotEquals(user1, null);
    }
}
