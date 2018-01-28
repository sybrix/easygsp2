package sybrix.easygsp2.exceptions;

/**
 * Created by dsmith on 9/19/16.
 */
public class NoMatchingAcceptHeaderExcepton extends RuntimeException {
        public NoMatchingAcceptHeaderExcepton() {
        }

        public NoMatchingAcceptHeaderExcepton(String message) {
                super(message);
        }

        public NoMatchingAcceptHeaderExcepton(String message, Throwable cause) {
                super(message, cause);
        }

        public NoMatchingAcceptHeaderExcepton(Throwable cause) {
                super(cause);
        }

        public NoMatchingAcceptHeaderExcepton(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace);
        }
}
