package sybrix.easygsp2;

import org.slf4j.LoggerFactory;
import sybrix.easygsp2.security.EasyGspServletRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dsmith on 9/12/16.
 */
public class EasyGsp2Filter implements Filter {
        private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EasyGsp2Filter.class);

        private EasyGsp2 easyGsp2;

        public void init(FilterConfig filterConfig) throws ServletException {
                easyGsp2 = new EasyGsp2(filterConfig);
        }

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
                EasyGspServletRequest request = null;

                if (!(servletRequest instanceof EasyGspServletRequest)) {
                        request = new EasyGspServletRequest((HttpServletRequest) servletRequest,easyGsp2);
                } else {
                        request = (EasyGspServletRequest) servletRequest;
                }

                        boolean continueFilterChain = easyGsp2.doRequest(request, (HttpServletResponse) servletResponse);

                if (continueFilterChain) {
                        logger.debug("sending request to filter chain");
                        chain.doFilter(request, servletResponse);
                }else{
                        logger.debug("request complete");
                }
        }

        public void destroy() {

                easyGsp2.getAppListener().onApplicationEnd();
        }
}
