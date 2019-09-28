package sybrix.easygsp2.exceptions;

/**
 * Created by dsmith on 8/16/15.
 */
public class InvokeControllerActionException extends HttpException {

        public InvokeControllerActionException() {
                super(500);
        }

        public InvokeControllerActionException(String message) {
                super(message,500);
        }

        public InvokeControllerActionException(String message, Throwable cause) {
                super(message, cause,500);
        }

        public InvokeControllerActionException(Throwable cause) {
                super(cause,500);
        }

        protected InvokeControllerActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace,500);
        }
}
