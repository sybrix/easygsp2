package sybrix.easygsp2;

import groovy.lang.Binding;
import sybrix.easygsp2.templates.TemplateInfo;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestResponseObject {

        private HttpServletRequest request;
        private HttpServletResponse response;
        private TemplateInfo templateInfo;

        private Binding binding;

        public RequestResponseObject(ServletRequest request, ServletResponse response, TemplateInfo templateInfo) {
                this.request = (HttpServletRequest)request;
                this.response = (HttpServletResponse)response;
                this.templateInfo = templateInfo;
                this.binding = binding;
        }


        public HttpServletRequest getRequest() {
                return request;
        }

        public HttpServletResponse getResponse() {
                return response;
        }

        public Binding getBinding() {
                return binding;
        }

        public TemplateInfo getTemplateInfo() {
                return templateInfo;
        }
}

