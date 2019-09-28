package sybrix.easygsp2.framework;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.sql.Sql;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import sybrix.easygsp2.routing.Route;
import sybrix.easygsp2.security.JwtUtil;
import sybrix.easygsp2.security.UserPrincipal;
import sybrix.easygsp2.templates.RequestError;
import sybrix.easygsp2.templates.TemplateInfo;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ThreadVariables {

        private ServletContext app;
        private HttpServletRequest request;
        private HttpServletResponse response;
        private TemplateInfo templateInfo;
        private String viewFolder;
        private GroovyClassLoader groovyClassLoader;
        private Binding binding;
        private Map<String, Route> routes;
        private Sql sql;
        private RequestError requestError;
        private boolean isTemplateRequest;
        private String templateFilePath;
        private UserPrincipal userPrincipal;
        private Jws<Claims> claims;

        public ThreadVariables(ServletContext app) {
                this.app = app;
                this.requestError = new RequestError();
        }

        public ThreadVariables(ServletContext app, ServletRequest request, ServletResponse response, Map<String,Route> routes, Sql sql, TemplateInfo templateInfo, GroovyClassLoader groovyClassLoader) {
                this.app = app;
                this.request = (HttpServletRequest)request;
                this.response = (HttpServletResponse)response;
                this.templateInfo = templateInfo;
                this.binding = binding;
                this.groovyClassLoader = groovyClassLoader;
                this.routes = routes;
                this.sql = sql;
                this.requestError = new RequestError();
                try {
                        this.claims = JwtUtil.parseJwtClaims((HttpServletRequest) request);
                }catch (Exception e){
                }
        }

        public Jws<Claims> getClaims() {
                return claims;
        }

        public HttpServletRequest getRequest() {
                return request;
        }

        public HttpServletResponse getResponse() {
                return response;
        }

        public String getViewFolder() {
                return viewFolder;
        }

        public void setViewFolder(String viewFolder) {
                this.viewFolder = viewFolder;
        }

        public Binding getBinding() {
                return binding;

        }

        public TemplateInfo getTemplateInfo() {
                if (templateInfo == null){
                        templateInfo = new TemplateInfo();
                }

                return templateInfo;
        }

        public GroovyClassLoader getGroovyClassLoader() {
                return groovyClassLoader;
        }

        public ServletContext getApp() {
                return app;
        }

        public Map<String, Route> getRoutes() {
                return routes;
        }

        public Sql getSql() {
                return sql;
        }

        public void setSql(Sql sql){
                this.sql = sql;
        }

        public RequestError getRequestError() {
                return requestError;
        }

        public boolean isTemplateRequest() {
                return isTemplateRequest;
        }

        public void setTemplateRequest(boolean templateRequest) {
                isTemplateRequest = templateRequest;
        }

        public String getTemplateFilePath() {
                return templateFilePath;
        }

        public void setTemplateFilePath(String templateFilePath) {
                this.templateFilePath = templateFilePath;
        }

        public UserPrincipal getUserPrincipal() {
                return userPrincipal;
        }

        public void setUserPrincipal(UserPrincipal userPrincipal) {
                this.userPrincipal = userPrincipal;
        }
}

