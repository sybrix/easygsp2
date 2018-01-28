package testapp.controllers;


import javax.servlet.http.HttpServletRequest;

/**
 * Created by dsmith on 4/17/15.
 */

public class JavaController {
        
        public void index(HttpServletRequest request){
                request.setAttribute("hello","aaa");

        }
}
