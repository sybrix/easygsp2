package sybrix.easygsp2.exceptions


import sybrix.easygsp2.http.HttpStatus

class UnsupportedContentType extends HttpException{

        UnsupportedContentType() {
                super(HttpStatus.UNSUPPORTED_MEDIA_TYPE.val())
        }

        UnsupportedContentType(String message) {
                super(HttpStatus.UNSUPPORTED_MEDIA_TYPE.val(), message)
        }

        UnsupportedContentType(String message, Throwable throwable) {
                super(message, throwable, HttpStatus.UNSUPPORTED_MEDIA_TYPE.val())
        }

        UnsupportedContentType(Throwable throwable) {
                super(throwable, HttpStatus.UNSUPPORTED_MEDIA_TYPE.val())
        }

        UnsupportedContentType(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
                super(message, throwable, enableSuppression, writableStackTrace, HttpStatus.UNSUPPORTED_MEDIA_TYPE.val())
        }

}
