package sybrix.easygsp2.exceptions;

import java.util.ArrayList;
import java.util.List;

public class BadRequestException extends HttpException {
        List constraintErrors = new ArrayList();

        public BadRequestException(List constraintErrors) {
                this.constraintErrors = constraintErrors;
        }

        public BadRequestException() {
                super(400);
        }

        public BadRequestException(String message) {
                super(message,400);
        }

        public BadRequestException(String message, Throwable throwable) {
                super(message, throwable,400);
        }


        public BadRequestException(Throwable throwable) {
                super(throwable,400);
        }

        public BadRequestException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
                super(message, throwable, enableSuppression, writableStackTrace,400);
        }

        public List getConstraintErrors() {
                return constraintErrors;
        }
}
