package sybrix.easygsp2.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import sybrix.easygsp2.EasyGsp2;
import sybrix.easygsp2.framework.ThreadBag;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;


public class EasyGspServletRequest implements HttpServletRequest {

        private HttpServletRequest request;
        private Map<String, String[]> parameters = new HashMap<String, String[]>();
        private Map<String, Object> uploads = new HashMap<String, Object>();
        private EasyGsp2 easyGsp;

        public EasyGspServletRequest(HttpServletRequest request, EasyGsp2 easyGsp) {
                this.request = request;
                Enumeration<String> en = request.getParameterNames();
                while (en.hasMoreElements()) {
                        String key = en.nextElement();
                        String[] val = request.getParameterValues(key);
                        parameters.put(key, val);
                }

                this.easyGsp = easyGsp;

        }

        public Map<String, Object> getUploads() {
                return uploads;
        }

        public void setUploads(Map<String, Object> uploads) {
                this.uploads = uploads;
        }

        public String getAuthType() {
                if (ThreadBag.get().getClaims() != null) {
                        UserPrincipal userPrincipal = JwtUtil.extractUserPrincipal(this);
                        return userPrincipal.getAuthType();
                } else {
                        UserPrincipal userPrincipal = (UserPrincipal) request.getSession(false).getAttribute(UserPrincipal.USER_PRINCIPAL_KEY);
                        if (userPrincipal != null) {
                                return userPrincipal.getAuthType();
                        }
                }

                return null;
        }

        public Cookie[] getCookies() {
                return request.getCookies();
        }

        public long getDateHeader(String name) {
                return request.getDateHeader(name);
        }


        public String getHeader(String name) {
                return request.getHeader(name);
        }

        public Enumeration<String> getHeaders(String name) {
                return request.getHeaders(name);
        }


        public Enumeration<String> getHeaderNames() {
                return request.getHeaderNames();
        }


        public int getIntHeader(String name) {
                return request.getIntHeader(name);
        }


        public String getMethod() {
                return request.getMethod();
        }


        public String getPathInfo() {
                return request.getPathInfo();
        }


        public String getPathTranslated() {
                return request.getPathTranslated();
        }


        public String getContextPath() {
                return request.getContextPath();
        }


        public String getQueryString() {
                return request.getQueryString();
        }

        public String getRemoteUser() {
                if (ThreadBag.get().getClaims() != null) {
                        UserPrincipal userPrincipal = JwtUtil.extractUserPrincipal(this);
                        return userPrincipal.getUsername();
                } else {
                        UserPrincipal userPrincipal = (UserPrincipal) request.getSession(false).getAttribute(UserPrincipal.USER_PRINCIPAL_KEY);
                        if (userPrincipal != null) {
                                return userPrincipal.getUsername();
                        }
                }
                return null;
        }


        public boolean isUserInRole(String role) {
                HttpSession session = request.getSession(false);
                Jws<io.jsonwebtoken.Claims> claims = ThreadBag.get().getClaims();

                if (claims != null) {
                        UserPrincipal userPrincipal = JwtUtil.extractUserPrincipal(this);
                        if (role.equals("*")) {
                                return true;
                        }
                        return userPrincipal.getRoles().contains(role);
                } else {
                        if (session != null) {
                                UserPrincipal userPrincipal = (UserPrincipal) session.getAttribute(UserPrincipal.USER_PRINCIPAL_KEY);
                                if (userPrincipal != null) {
                                        if (role.equals("*")) {
                                                return true;
                                        }
                                        return userPrincipal.getRoles().contains(role);
                                }
                        }
                }
                return false;
        }

        public Principal getUserPrincipal() {
                try {
                        if (ThreadBag.get() == null){
                                return  null;
                        }

                        if (ThreadBag.get().getClaims() != null) {
                                UserPrincipal userPrincipal = JwtUtil.extractUserPrincipal(this);
                                return userPrincipal;
                        } else {
                                HttpSession session = request.getSession(false);
                                if (session != null) {
                                        UserPrincipal userPrincipal = (UserPrincipal) session.getAttribute(UserPrincipal.USER_PRINCIPAL_KEY);
                                        if (userPrincipal != null) {
                                                return userPrincipal;
                                        }
                                }
                        }
                } catch (ExpiredJwtException e) {
                        return null;
                }

                return null;
        }


        public String getRequestedSessionId() {
                return request.getRequestedSessionId();
        }


        public String getRequestURI() {
                return request.getRequestURI();
        }


        public StringBuffer getRequestURL() {
                return request.getRequestURL();
        }


        public String getServletPath() {
                return request.getServletPath();
        }


        public HttpSession getSession(boolean create) {
                return request.getSession(create);
        }


        public HttpSession getSession() {
                return request.getSession();
        }


        public String changeSessionId() {
                return request.changeSessionId();
        }


        public boolean isRequestedSessionIdValid() {
                return request.isRequestedSessionIdValid();
        }


        public boolean isRequestedSessionIdFromCookie() {
                return request.isRequestedSessionIdFromCookie();
        }


        public boolean isRequestedSessionIdFromURL() {
                return request.isRequestedSessionIdFromURL();
        }


        @Deprecated
        public boolean isRequestedSessionIdFromUrl() {
                return request.isRequestedSessionIdFromUrl();
        }


        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
                return request.authenticate(response);
        }

        public void login(String username, String password) throws ServletException {
                //easyGsp.authenticationService authenticationUtil = (AuthenticationService) request.getServletContext().getAttribute("authenticationUtil");
                try {
                        easyGsp.getAuthenticationService().validateCredentials(username, password);

                } catch (Exception e) {
                        throw new ServletException(e);
                }
        }

        public void logout() throws ServletException {
                request.logout();
        }

        public Collection<Part> getParts() throws IOException, ServletException {
                return request.getParts();
        }


        public Part getPart(String name) throws IOException, ServletException {
                return request.getPart(name);
        }


        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
                return request.upgrade(handlerClass);
        }


        public Object getAttribute(String name) {
                return request.getAttribute(name);
        }


        public Enumeration<String> getAttributeNames() {
                return request.getAttributeNames();
        }


        public String getCharacterEncoding() {
                return request.getCharacterEncoding();
        }


        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
                request.setCharacterEncoding(env);
        }


        public int getContentLength() {
                return request.getContentLength();
        }


        public long getContentLengthLong() {
                return request.getContentLengthLong();
        }


        public String getContentType() {
                return request.getContentType();
        }


        public ServletInputStream getInputStream() throws IOException {
                return request.getInputStream();
        }


        public String getParameter(String name) {
                String[] val = parameters.get(name);
                if (val == null)
                        return null;
                else
                        return val[0];
        }


        public Enumeration<String> getParameterNames() {
                return java.util.Collections.enumeration(parameters.keySet());
        }


        public String[] getParameterValues(String name) {
                return parameters.get(name);
        }


        public Map<String, String[]> getParameterMap() {
                return parameters;
        }


        public String getProtocol() {
                return request.getProtocol();
        }


        public String getScheme() {
                return request.getScheme();
        }


        public String getServerName() {
                return request.getServerName();
        }


        public int getServerPort() {
                return request.getServerPort();
        }


        public BufferedReader getReader() throws IOException {
                return request.getReader();
        }


        public String getRemoteAddr() {
                return request.getRemoteAddr();
        }


        public String getRemoteHost() {
                return request.getRemoteHost();
        }


        public void setAttribute(String name, Object o) {
                request.setAttribute(name, o);
        }


        public void removeAttribute(String name) {
                request.removeAttribute(name);
        }


        public Locale getLocale() {
                return request.getLocale();
        }


        public Enumeration<Locale> getLocales() {
                return request.getLocales();
        }


        public boolean isSecure() {
                return request.isSecure();
        }


        public RequestDispatcher getRequestDispatcher(String path) {
                return request.getRequestDispatcher(path);
        }

        @Deprecated
        public String getRealPath(String path) {
                return request.getRealPath(path);
        }


        public int getRemotePort() {
                return request.getRemotePort();
        }


        public String getLocalName() {
                return request.getLocalName();
        }


        public String getLocalAddr() {
                return request.getLocalAddr();
        }


        public int getLocalPort() {
                return request.getLocalPort();
        }


        public ServletContext getServletContext() {
                return request.getServletContext();
        }


        public AsyncContext startAsync() throws IllegalStateException {
                return request.startAsync();
        }


        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
                return request.startAsync(servletRequest, servletResponse);
        }


        public boolean isAsyncStarted() {
                return request.isAsyncStarted();
        }


        public boolean isAsyncSupported() {
                return request.isAsyncSupported();
        }


        public AsyncContext getAsyncContext() {
                return request.getAsyncContext();
        }


        public DispatcherType getDispatcherType() {
                return request.getDispatcherType();
        }
}
