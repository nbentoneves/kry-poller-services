package se.kry.codetest.domain;

import java.util.Objects;
import java.util.StringJoiner;

public final class Error {

    private final String message;

    private final int status;

    public Error(String message, int status) {
        this.message = Objects.requireNonNull(message, "message can not be null");
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Error.class.getSimpleName() + "[", "]")
                .add("message='" + message + "'")
                .add("status=" + status)
                .toString();
    }
}
