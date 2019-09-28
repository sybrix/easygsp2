package sybrix.easygsp2.exceptions

class ApiException extends  HttpException{
        ApiException() {
        }

        ApiException(Integer status) {
                super(status)
        }

        ApiException(String message) {
                super(message)
        }

        ApiException(String message, Integer status) {
                super(message, status)
        }

        ApiException(String message, Throwable throwable) {
                super(message, throwable)
        }

        ApiException(String message, Throwable throwable, Integer status) {
                super(message, throwable, status)
        }

        ApiException(Throwable throwable) {
                super(throwable)
        }

        ApiException(Throwable throwable, Integer status) {
                super(throwable, status)
        }

        ApiException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
                super(message, throwable, enableSuppression, writableStackTrace)
        }

        ApiException(String mesasage, Throwable throwable, boolean enableSuppression, boolean writableStackTrace, Integer status) {
                super(mesasage, throwable, enableSuppression, writableStackTrace, status)
        }
}
