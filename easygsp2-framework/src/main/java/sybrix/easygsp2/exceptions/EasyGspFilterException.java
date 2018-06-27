package sybrix.easygsp2.exceptions;

public class EasyGspFilterException extends RuntimeException {
        private final long serialVersionUID = -1;

        public EasyGspFilterException(Exception e) {
        }

        public EasyGspFilterException() {
        }

        public EasyGspFilterException(String message) {
                super(message);
        }

        public EasyGspFilterException(String message, Throwable cause) {
                super(message, cause);
        }

        public EasyGspFilterException(Throwable cause) {
                super(cause);
        }

        public EasyGspFilterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace);
        }
}
