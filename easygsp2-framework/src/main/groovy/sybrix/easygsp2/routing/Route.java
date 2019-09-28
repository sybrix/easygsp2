package sybrix.easygsp2.routing;

import jregex.Matcher;
import jregex.Pattern;
import sybrix.easygsp2.collections.CaseInsensitiveList;
import sybrix.easygsp2.exceptions.RoutingException;
import sybrix.easygsp2.util.StringUtil;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;


public class Route {
        private static final Logger logger = Logger.getLogger(Route.class.getName());

        // shamelessly taken from the play framework, these regex's are not mine
        public static final Pattern customRegexPattern = new Pattern("\\{([a-zA-Z_][a-zA-Z_0-9]*)\\}");
        public static final Pattern argsPattern = new Pattern("\\{<([^>]+)>([a-zA-Z_0-9]+)\\}");

        public static String[] HTTP_METHODS = new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS", "TRACE", "CONNECT", "HEAD"};

        private Pattern pattern;
        //private List<String> method = new ArrayList<String>();
        private String httpMethod;
        private String path;
        private Class controllerClass;
        private Method controllerMethod;
        private boolean secure;

        private Map<String, UrlParameter> parameters = new HashMap();
        private Class[] methodParameters;
        private boolean duplicate;
        private String[] consumes;
        private String[] produces;

        private List<MimeType> consumesMimeType = new ArrayList<>();
        private List<MimeType> producesMimeType = new ArrayList<>();

        private CaseInsensitiveList roles = new CaseInsensitiveList();


        public Route(String httpMethod, String path, Class controllerClass, Method controllerMethod, Class[] methodParameters, String[] roles, String accepts[], String returns[], boolean secure) throws RoutingException {
                try {

                        this.httpMethod = httpMethod.toUpperCase();
                        this.path = path;
                        this.controllerClass = controllerClass;
                        this.controllerMethod = controllerMethod;
                        this.methodParameters = methodParameters;
                        this.consumes = accepts;
                        this.produces = returns;
                        this.secure = secure;

                        for (String s : accepts)
                                consumesMimeType.add(new MimeType(s));

                        for (String s : returns)
                                producesMimeType.add(new MimeType(s));

                        String patternString = customRegexPattern.replacer("\\{<[^/]+>$1\\}").replace(path);
                        Matcher matcher = argsPattern.matcher(patternString);

                        while (matcher.find()) {
                                UrlParameter param = new UrlParameter();
                                param.name = matcher.group(2);
                                param.regexPattern = new Pattern(matcher.group(1));
                                parameters.put(param.name, param);
                        }

                        patternString = argsPattern.replacer("({$2}$1)").replace(patternString);
                        this.pattern = new Pattern(patternString);

                        if (roles != null) {
                                for (String role : roles) {
                                        this.roles.add(role.trim());
                                }
                        }

                } catch (Exception e) {
                        throw new RoutingException("Error initializing route httpMethod:" + httpMethod + ", path:" + path + ", " +
                                "controllers:" + controllerClass + ". " + e.getMessage(), e);
                }
        }

        public boolean matches(HttpServletRequest request) {
                String path = request.getRequestURI();

                Matcher matcher = pattern.matcher(path);

                if (matcher.matches()) {
                        for (UrlParameter parameter : parameters.values()) {
                                String value = matcher.group(parameter.name);
                                if (!parameter.getRegexPattern().matches(value)) {
                                        break;
                                } else {
                                        parameter.setValue(value);
                                }
                        }

                        return httpMethod.equalsIgnoreCase(request.getMethod());

                }

                return false;
        }


        public Class getControllerClass() {
                return controllerClass;
        }

        public void setControllerClass(Class controllerClass) {
                this.controllerClass = controllerClass;
        }

        public Method getControllerMethod() {
                return controllerMethod;
        }

        public void setControllerMethod(Method controllerMethod) {
                this.controllerMethod = controllerMethod;
        }

        public boolean isSecure() {
                return secure;
        }

        @Override
        public String toString() {
                return "Route {" +
                        "url=" + pattern +
                        ", httpMethod='" + httpMethod + '\'' +
                        ", path='" + path + '\'' +
                        ", controllerClass=" + controllerClass +
                        ", controllerMethod=" + controllerMethod +
                        ", secure=" + secure +
                        ", parameters=" + parameters +
                        ", methodParameters=" + Arrays.toString(methodParameters) +
                        ", duplicate=" + duplicate +
                        ", consumes='" + Arrays.toString(consumes) + '\'' +
                        ", produces='" + Arrays.toString(produces) + '\'' +
                        ", roles=" + roles +
                        '}';
        }


        public void setDuplicate(boolean duplicate) {
                this.duplicate = duplicate;

        }

        public boolean isDuplicate() {
                return duplicate;
        }

        public String getPath() {
                return path;
        }

        public Map<String, UrlParameter> getParameters() {
                return parameters;
        }

        public Class[] getMethodParameters() {
                return methodParameters;
        }

        public void setConsumes(String[] consumes) {
                this.consumes = consumes;
        }

        public void setProduces(String[] produces) {
                this.produces = produces;
        }

        public String[] getConsumes() {
                if (StringUtil.isEmpty(consumes)) {
                        return new String[]{};
                }
                return consumes;
        }

        public String[] getProduces() {
                if (StringUtil.isEmpty(produces)) {
                        return new String[]{};
                }
                return produces;
        }

        public List<String> getRoles() {
                return roles;
        }

        public void setRoles(CaseInsensitiveList roles) {
                this.roles = roles;
        }


        public void setSecure(boolean secure) {
                this.secure = secure;
        }

        public List<MimeType> getConsumesMimeType() {
                return consumesMimeType;
        }

        public List<MimeType> getProducesMimeType() {
                return producesMimeType;
        }


}
