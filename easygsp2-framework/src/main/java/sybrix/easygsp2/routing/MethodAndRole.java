package sybrix.easygsp2.routing;

import java.lang.reflect.Method;

public class MethodAndRole {
        private Method method;
        private String[] roles = new String[]{};


        public Method getMethod() {
                return method;
        }

        public void setMethod(Method method) {
                this.method = method;
        }

        public String[] getRoles() {
                return roles;
        }

        public void setRoles(String[] roles) {
                this.roles = roles;
        }
}
