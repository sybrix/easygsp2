package sybrix.easygsp2.exceptions


import sybrix.easygsp2.http.HttpStatus

class NotFoundException extends HttpException {

        NotFoundException() {
                super(HttpStatus.NOT_FOUND.val())
        }

        NotFoundException(String message) {
                super(message, HttpStatus.NOT_FOUND.val())
        }


        NotFoundException(String message, Throwable throwable) {
                super(message, throwable,HttpStatus.NOT_FOUND.val())
        }

        NotFoundException(Throwable throwable) {
                super(throwable, HttpStatus.NOT_FOUND.val())
        }

        NotFoundException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
                super(message, throwable, enableSuppression, writableStackTrace,HttpStatus.NOT_FOUND.val())
        }
}
