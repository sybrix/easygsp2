package sybrix.easygsp2.exceptions;


public class ParameterDeserializationException extends RuntimeException {
        public ParameterDeserializationException() {
        }

        public ParameterDeserializationException(String message) {
                super(message);
        }

        public ParameterDeserializationException(String message, Throwable cause) {
                super(message, cause);
        }

        public ParameterDeserializationException(Throwable cause) {
                super(cause);
        }

        public ParameterDeserializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace);
        }
}
