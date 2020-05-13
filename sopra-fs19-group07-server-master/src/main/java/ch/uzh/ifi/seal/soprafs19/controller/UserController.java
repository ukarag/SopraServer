package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetUser;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetUserIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("MVCPathVariableInspection")
@RestController
public class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/users")
    ResponseEntity<Iterable<User>> all() {
        return ResponseEntity.status(HttpStatus.OK).body(service.getUsers());
    }

    @GetMapping("/users/{userId}")
    ResponseEntity<GetUser> one(
            @RequestHeader("userToken") String token, @PathVariable("userId") Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.getUser(id, token));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @GetMapping("/users/me")
    ResponseEntity<Long> me(
            @RequestHeader("userToken") String token) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.getUserId(token));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @PostMapping("/users")
    ResponseEntity<User> createUser(@RequestBody GetUserIn userData) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.service.createUserController(userData));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @PostMapping("/users/login")
    ResponseEntity<String> login(@RequestBody GetUserIn user) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(this.service.login(user));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @PostMapping("/users/logout")
    ResponseEntity<User> logout(@RequestHeader("userToken") String token) {
        try {
            this.service.logout(token);
            return ResponseEntity.noContent().build();
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/users")
    ResponseEntity<User> updateUser(@RequestBody User newUser, @RequestHeader("userToken") String token) {
        try {
            this.service.updateUser(token, newUser);
            return ResponseEntity.noContent().build();
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                ex.getException(), ex.toString(), ex);
        }
    }
}