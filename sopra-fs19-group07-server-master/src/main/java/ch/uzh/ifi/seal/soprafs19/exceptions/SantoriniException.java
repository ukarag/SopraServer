package ch.uzh.ifi.seal.soprafs19.exceptions;

import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Game error")
public class SantoriniException extends Exception {
    private static final long serialVersionUID = 1L;
    private final ExceptionEnum ex;

    public SantoriniException(ExceptionEnum ex) {
        super(ex.getMessage());
        this.ex = ex;
    }

    @Override
    public String toString() {
        return ex.getMessage();
    }

    public HttpStatus getException() {
        return ex.getException();
    }

    public ExceptionEnum getEnum() {
        return ex;
    }
}
