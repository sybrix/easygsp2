package testapp.controllers

class DefaultController {

        def index(request, page, int p){
                request.hello = 'aaa'
                println("hello default controller index")
                println("p=" + p)

                //render("<html><title>hey</title></html>")

                forward('index.jsp')

        }

//        def test(){
//                println 'hello from test'
//                println request.method
//
//                'default/test.gsp'
//        }

        def test(int x){
                println 'hello from test(x)'

                'default/test.gsp'
        }

}

