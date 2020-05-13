package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetUser;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetUserIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Iterable<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User createUserController(GetUserIn userData) throws SantoriniException {
        User newUser = new User();
        newUser.setUsername(userData.getUsername());
        newUser.setPassword(userData.getPassword());
        return createUser(newUser);
    }

    public User createUser(User newUser) throws SantoriniException {
        if (this.userRepository.existsByUsername(newUser.getUsername())) {
            throw new SantoriniException(ExceptionEnum.USERNAME_EXISTS);
        }
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.OFFLINE);
        newUser.setCreationDate();
        newUser = userRepository.save(newUser);
        return newUser;
    }

    public String login(GetUserIn loginUser) throws SantoriniException {
        User user = userRepository.findByUsername(loginUser.getUsername());
        if (user == null) {
            throw new SantoriniException(ExceptionEnum.USER_NOT_FOUND);
        } else if (user.getStatus() == UserStatus.ONLINE) {
            throw new SantoriniException(ExceptionEnum.ALREADY_LOGGED_IN);
        } else if (user.getPassword().equals(loginUser.getPassword())) {
            user.setStatus(UserStatus.ONLINE);
            user = userRepository.save(user);
            return user.getToken();
        } else {
            throw new SantoriniException(ExceptionEnum.NO_MATCH);
        }
    }

    public void logout(String token) throws SantoriniException {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new SantoriniException(ExceptionEnum.USER_NOT_FOUND);
        } else {
            user.setStatus(UserStatus.OFFLINE);
        }
    }

    public GetUser getUser(Long id, String token) throws SantoriniException {
        User user = getUserById(id);
        GetUser result = new GetUser();
        result.setUser(user);
        result.setIsMe(user.getToken().equals(token));
        return result;
    }

    public User getUserById(Long id) throws SantoriniException {
        User user = this.userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new SantoriniException(ExceptionEnum.USER_NOT_FOUND);
        }
        return user;
    }

    public User getUserByToken(String token) throws SantoriniException {
        User user = this.userRepository.findByToken(token);
        if (user == null) {
            throw new SantoriniException(ExceptionEnum.USER_NOT_FOUND);
        }
        return user;
    }

    public Long getUserId(String token) throws SantoriniException {
        User user = getUserByToken(token);
        return user.getId();
    }

    public void updateUser(String token, User newUser) throws SantoriniException {
        User user = this.getUserByToken(token);
        if (newUser.getUsername() != null && !user.getUsername().equals(newUser.getUsername())) {
            if (this.userRepository.existsByUsername(newUser.getUsername())) {
                throw new SantoriniException(ExceptionEnum.USERNAME_EXISTS);
            } else {
                user.setUsername(newUser.getUsername());
            }
        }
        user.setBirthday(newUser.getBirthday());
        userRepository.save(user);
    }
}
