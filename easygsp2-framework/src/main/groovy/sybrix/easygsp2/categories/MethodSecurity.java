package sybrix.easygsp2.categories;

public class MethodSecurity {
        boolean secured = false;
        String [] roles = {};

        public MethodSecurity(boolean secured, String[] roles) {
                this.secured = secured;
                this.roles = roles;
        }

        public boolean isSecured() {
                return secured;
        }

        public void setSecured(boolean secured) {
                this.secured = secured;
        }

        public String[] getRoles() {
                return roles;
        }

        public void setRoles(String[] roles) {
                this.roles = roles;
        }
}
