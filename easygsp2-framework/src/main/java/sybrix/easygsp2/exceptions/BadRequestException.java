package sybrix.easygsp2.exceptions;

import java.util.ArrayList;
import java.util.List;

public class BadRequestException extends Exception {
        List constraintErrors = new ArrayList();

        public BadRequestException() {

        }
        public BadRequestException(List constraintErrors) {
                this.constraintErrors = constraintErrors;

        }


        public BadRequestException(String message) {
                super(message);
        }

        public BadRequestException(String message, Throwable cause) {
                super(message, cause);
        }

        public BadRequestException(Throwable cause) {
                super(cause);
        }

        public BadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace);
        }

        public List getConstraintErrors() {
                return constraintErrors;
        }

        public void setConstraintErrors(List constraintErrors) {
                this.constraintErrors = constraintErrors;
        }
}
