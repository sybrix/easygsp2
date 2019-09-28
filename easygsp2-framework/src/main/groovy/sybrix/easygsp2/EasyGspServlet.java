package sybrix.easygsp2;


import sybrix.easygsp2.fileupload.FileUpload;
import sybrix.easygsp2.security.EasyGspServletRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;


public class EasyGspServlet implements Servlet {

        private static final Logger logger = Logger.getLogger(EasyGspServlet.class.getName());
        private EasyGsp2 easyGsp2;

        public void init(ServletConfig servletConfig) throws ServletException {
                easyGsp2 = new EasyGsp2(servletConfig);
        }

        public ServletConfig getServletConfig() {
                return null;
        }

        public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
                EasyGspServletRequest request = null;

                if (!(servletRequest instanceof EasyGspServletRequest)){
                        request =  new EasyGspServletRequest((HttpServletRequest)servletRequest,easyGsp2);
                } else {
                       request = (EasyGspServletRequest)servletRequest;
                }

                easyGsp2.doRequest(request, (HttpServletResponse)servletResponse);
        }

        public String getServletInfo() {
                return "easyGsp2 servlet v1 ";
        }

        public void destroy() {

        }

}
