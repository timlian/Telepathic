package com.telepathic.finder.sdk.exception;

public class IllegalConsumerTypeException extends RuntimeException {

    public IllegalConsumerTypeException() {
        super();
    }

    public IllegalConsumerTypeException(String s) {
        super(s);
    }

    public IllegalConsumerTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalConsumerTypeException(Throwable cause) {
        super(cause);
    }

}
