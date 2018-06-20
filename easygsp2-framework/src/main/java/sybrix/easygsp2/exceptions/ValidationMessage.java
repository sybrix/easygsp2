package sybrix.easygsp2.exceptions;

public class ValidationMessage {

        private String property;

        public String getProperty() {
                return property;
        }

        public void setProperty(String property) {
                this.property = property;
        }

        public ValidationMessage(String property, String message) {
                this.message = message;
                this.property = property;
        }

        private String message;

        public String getMessage() {
                return message;
        }

        public void setMessage(String message) {
                this.message = message;
        }
}
