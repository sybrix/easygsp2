package testapp.listener

import sybrix.easygsp2.framework.AppListener

import javax.servlet.ServletContext

/**
 * Created by dsmith on 8/14/16.
 */
class AppStart implements AppListener {
        @Override
        void onApplicationStart(ServletContext context) {
                println "app start"
        }
}
