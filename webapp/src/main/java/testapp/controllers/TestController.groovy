package testapp.controllers

import sybrix.easygsp2.anno.Controller
import sybrix.easygsp2.anno.Mapping

import javax.servlet.http.HttpServletRequest


@Controller
class TestController {

        @Mapping(methods = ["GET"], pattern = ["/"])
        def index(HttpServletRequest request){
                request.setAttribute("hello","aaa")
                println("hello")
        }
}
