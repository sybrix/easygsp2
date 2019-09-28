package sybrix.easygsp2.exceptions

class ForbiddedException extends HttpException {
        ForbiddedException() {
                super(403)
        }

        ForbiddedException(String message) {
                super(message, 403)
        }

        ForbiddedException(String message, Throwable throwable) {
                super(message, throwable, 403)
        }

        ForbiddedException(Throwable throwable) {
                super(throwable, 403)
        }

        ForbiddedException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
                super(message, throwable, enableSuppression, writableStackTrace, 403)
        }
}
