package sybrix.easygsp2.routing;

import jregex.Matcher;
import jregex.Pattern;
import sybrix.easygsp2.exceptions.RoutingException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Route {
        // shamelessly taken from the play framework, these regex's are not mine
        public static final Pattern customRegexPattern = new Pattern("\\{([a-zA-Z_][a-zA-Z_0-9]*)\\}");
        public static final Pattern argsPattern = new Pattern("\\{<([^>]+)>([a-zA-Z_0-9]+)\\}");

        private Pattern pattern;
        private List<String> methods = new ArrayList<String>();
        private String path;
        private Class controllerClass;
        private String controllerMethod;

        private Map<String, UrlParameter> parameters = new HashMap();

        public Route(List<String> methods, String path, Class controllerClass, String controllerMethod) throws RoutingException {
                try {
                        for (String s : methods) {
                                this.methods.add(s.toUpperCase());
                        }

                        this.path = path;
                        this.controllerClass = controllerClass;
                        this.controllerMethod = controllerMethod;

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

                } catch (Exception e) {
                        throw new RoutingException("Error initializing route method:" + methods + ", path:" + path + ", " +
                                "controller:" + controllerClass + ". " + e.getMessage(), e);
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

                        return  methods.contains("*") ||  methods.contains(request.getMethod().toUpperCase());
                }

                return false;
        }

        public Class getControllerClass() {
                return controllerClass;
        }

        public void setControllerClass(Class controllerClass) {
                this.controllerClass = controllerClass;
        }

        public String getControllerMethod() {
                return controllerMethod;
        }

        public void setControllerMethod(String controllerMethod) {
                this.controllerMethod = controllerMethod;
        }

        @Override
        public String toString() {
                return "Route{" +
                        "pattern=" + pattern +
                        ", methods=" + methods +
                        ", path='" + path + '\'' +
                        ", controllerClass=" + controllerClass +
                        ", controllerMethod='" + controllerMethod + '\'' +
                        ", parameters=" + parameters +
                        '}';
        }
}