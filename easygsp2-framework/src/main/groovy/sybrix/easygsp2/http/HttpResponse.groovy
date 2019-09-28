package sybrix.easygsp2.http

class HttpResponse {
        Integer code
        Object entity

        HttpResponse(HttpStatus httpStatus, Object obj) {
                code = httpStatus.val()
                this.entity = obj
        }

        HttpResponse(Integer statusCode, Object obj) {
                this.code = statusCode
                this.entity = obj
        }

        HttpResponse(Integer statusCode) {
                this.code = statusCode
        }

        HttpResponse(HttpStatus httpStatus) {
                this.code = httpStatus.val()
        }

        public static HttpResponse OK() {
                new HttpResponse(HttpStatus.OK)
        }

        public static HttpResponse NOT_FOUND() {
                new HttpResponse(HttpStatus.NOT_FOUND)
        }

        public static HttpResponse BAD_REQUEST() {
                new HttpResponse(HttpStatus.BAD_REQUEST)
        }

}
