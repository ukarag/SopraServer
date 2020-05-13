package ch.uzh.ifi.seal.soprafs19.constant;

import org.springframework.http.HttpStatus;

public enum ExceptionEnum {
    ALREADY_LOGGED_IN("already logged in", HttpStatus.CONFLICT),
    GAME_FULL("Game full", HttpStatus.CONFLICT),
    GAME_NOT_FOUND("Game not found", HttpStatus.NOT_FOUND),
    GAME_NOT_FULL("Game not full", HttpStatus.FORBIDDEN),
    NO_MATCH("Username and Password don't match", HttpStatus.FORBIDDEN),
    PLAYER_ALREADY_IN_GAME("Player already in the game", HttpStatus.FORBIDDEN),
    TURN_NOT_ALLOWED("Turn not allowed", HttpStatus.FORBIDDEN),
    USERNAME_EXISTS("Username exists already", HttpStatus.CONFLICT),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus errorStatus;

    ExceptionEnum(String message, HttpStatus errorStatus) {
        this.message = message;
        this.errorStatus = errorStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getException() {
        return errorStatus;
    }
}
