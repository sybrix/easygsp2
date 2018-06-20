package sybrix.easygsp2.framework;

import sybrix.easygsp2.util.PropertiesFile;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

/**
 * Created by dsmith on 8/14/16.
 */
public interface AppListener {
        void onApplicationStart(ServletContext context);
        void onApplicationEnd();
        void onRequestEnd(ServletRequest request);
}
