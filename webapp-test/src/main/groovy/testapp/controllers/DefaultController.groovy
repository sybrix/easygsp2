package testapp.controllers

import sybrix.easygsp2.anno.Api
import testapp.models.Game
import testapp.models.Marker
import static sybrix.easygsp2.db.firebird.Db.*;
import org.apache.commons.fileupload.FileItem

import javax.servlet.http.HttpServletRequest

class DefaultController {

        def index(request, page, int p){
                request.hello = 'aaa'
                println("hello default controller index")
                println("p=" + p)

                //render("<html><title>hey</title></html>")

                //forward('index.jsp')

                'default/index.jsp'
        }

        def index(){
                 request.hello = "testing hllo"
                //println  new Game().toString()

                //print Game.list()

                withTransaction {
                       def x = "Select * from game".executeQuery()
                }

                println 'hello from test'
                println request.method

                HttpServletRequest f;

                'default/test.gsp'
        }

        @Api(method = "POST", url = "/upload")
        def tese(List<FileItem> p) {
                println request
                println response
        }

        @Api(method = ["GET", "POST"], url = "/")
        def test1(){
                println request
                println response

                request.hello = "testing hello"
                // println  new Game().toString()
//                def sql = newSqlInstance()
//                sql.eachRow("SELECT * FROM GAME"){
//                        println it
//                }
                withTransaction {
                        println Game.list()[0]
                        println Game.find([gameId: 2])
                }

                println 'hello from test'
                println request.method

                'default/test.gsp'
        }

        @Api(method = ["GET", "POST"], url = "/")
        def test1(request, response, int x, Integer x1){
                println request
                println response

                request.hello = "testing hello"
                println  new Game().toString()
//                def sql = newSqlInstance()
//                sql.eachRow("SELECT * FROM GAME"){
//                        println it
//                }
                withTransaction {
                        println Game.list()[0]
                        println Game.find([gameId: 2])
                }

                println 'hello from test'
                println request.method

                'default/test.gsp'
        }

        @Api(method = "GET", url = "/x" )
        def test(int x){
                println 'hello from test(x)'
                request.setAttribute("hello", "it's here")
                'default/test.gsp'
        }

        @Api(method = ["POST"], url = "/x")
        def testx(List<Marker> m, HttpServletRequest request){
                println 'hello from test(x)'
                request.setAttribute("hello", "it's here")
                'default/test.gsp'
        }

}

