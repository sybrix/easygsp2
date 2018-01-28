package sybrix.easygsp2.exceptions;

/**
 * Created by dsmith on 8/15/15.
 */
public class NoMatchingRouteException extends RuntimeException{
        public NoMatchingRouteException(Exception e) {
                super(e);
        }

        public NoMatchingRouteException() {
        }

        public NoMatchingRouteException(String message) {
                super(message);
        }

        public NoMatchingRouteException(String message, Throwable cause) {
                super(message, cause);
        }

        public NoMatchingRouteException(Throwable cause) {
                super(cause);
        }

        public NoMatchingRouteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace);
        }
}
