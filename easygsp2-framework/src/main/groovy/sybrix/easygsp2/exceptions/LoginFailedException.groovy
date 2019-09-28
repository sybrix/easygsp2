package sybrix.easygsp2.exceptions

class LoginFailedException extends HttpException {
        LoginFailedException() {
        }

        LoginFailedException(String var1) {
                super(var1, 401)
        }

        LoginFailedException(String var1, Throwable var2) {
                super(var1, var2, 401)
        }

        LoginFailedException(Throwable var1) {
                super(var1, 401)
        }

        LoginFailedException(String var1, Throwable var2, boolean var3, boolean var4) {
                super(var1, var2, var3, var4, 401)
        }
}
