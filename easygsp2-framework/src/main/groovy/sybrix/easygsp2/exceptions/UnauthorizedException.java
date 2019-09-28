package sybrix.easygsp2.exceptions;

public class UnauthorizedException extends HttpException {
        public UnauthorizedException() {
                super(401);
        }

        public UnauthorizedException(String message) {
                super(message, 401);
        }

        public UnauthorizedException(String message, Throwable cause) {
                super(message, cause, 401);
        }

        public UnauthorizedException(Throwable cause) {
                super(cause, 401);
        }

        protected UnauthorizedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace, 401);
        }
}
