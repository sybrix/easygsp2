package sybrix.easygsp2.exceptions;

/**
 * Created by dsmith on 8/16/15.
 */
public class InvokeControllerActionException extends Exception {
        public InvokeControllerActionException() {
                super();
        }

        public InvokeControllerActionException(String message) {
                super(message);
        }

        public InvokeControllerActionException(String message, Throwable cause) {
                super(message, cause);
        }

        public InvokeControllerActionException(Throwable cause) {
                super(cause);
        }

        protected InvokeControllerActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace);
        }
}
