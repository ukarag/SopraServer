package ch.uzh.ifi.seal.soprafs19.wrapper;

import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.User;

public class GetUser {
    private String username;
    private UserStatus status;
    private String creationDate;
    private String birthday;
    private Boolean isMe;

    public void setIsMe(Boolean isMe) {
        this.isMe = isMe;
    }

    public Boolean getIsMe() {
        return isMe;
    }

    public void setUser(User user) {
        this.username = user.getUsername();
        this.status = user.getStatus();
        this.creationDate = user.getCreationDate();
        this.birthday = user.getBirthday();

    }

    public String getUsername() {
        return username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getBirthday() {
        return birthday;
    }
}