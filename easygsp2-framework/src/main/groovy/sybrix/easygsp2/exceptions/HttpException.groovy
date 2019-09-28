package sybrix.easygsp2.exceptions

class HttpException extends RuntimeException {
        protected Integer status
        String errorCode

        HttpException() {
        }

        HttpException(Integer status) {
                this.status = status
        }

        HttpException(String message) {
                super(message)
        }

        HttpException(String message, Integer status) {
                super(message)
                this.status = status
        }

        HttpException(String message, Throwable throwable) {
                super(message, throwable)
        }

        HttpException(String message, Throwable throwable, Integer status) {
                super(message, throwable)
                this.status = status
        }

        HttpException(Throwable throwable) {
                super(throwable)
        }

        HttpException(Throwable throwable, Integer status) {
                super(throwable)
                this.status = status
        }

        HttpException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
                super(message, throwable, enableSuppression, writableStackTrace)
        }

        HttpException(String mesasage, Throwable throwable, boolean enableSuppression, boolean writableStackTrace, Integer status) {
                super(mesasage, throwable, enableSuppression, writableStackTrace)
                this.status = status
        }

        Integer getStatus() {
                return status
        }

}
