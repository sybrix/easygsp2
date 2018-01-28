package testapp.controllers

import javax.servlet.http.HttpServletRequest


class TestController {


        def index(HttpServletRequest request){
                request.setAttribute("hello","aaa")
                println("hello")
        }
}
