package sybrix.easygsp2.security;

import sybrix.easygsp2.EasyGsp2;
import sybrix.easygsp2.routing.Route;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dsmith on 8/7/16.
 */
public class SecurityFilter implements Filter {

        public void init(FilterConfig filterConfig) throws ServletException {

        }

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//                HttpServletRequest request =  new EasyGspServletRequest((HttpServletRequest)servletRequest,);
//                HttpServletResponse response = (HttpServletResponse) servletResponse;
//
//                Route route = EasyGsp2.findRoute(request);
//                servletRequest.setAttribute(EasyGsp2.ROUTE_REQUEST_ATTR, route);
//
//                if (!onIgnoreList(request)) {
//                        authorize(route, request, response);
//                }
//
//                filterChain.doFilter(servletRequest, servletResponse);

        }

        public void destroy() {

        }

        protected boolean onIgnoreList(HttpServletRequest request) {
                return false;
        }

        protected void authorize(Route route, HttpServletRequest request, HttpServletResponse response) {

        }

}
