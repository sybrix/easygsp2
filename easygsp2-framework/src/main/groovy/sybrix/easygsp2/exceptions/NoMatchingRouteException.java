package sybrix.easygsp2.exceptions;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by dsmith on 8/15/15.
 */
public class NoMatchingRouteException extends HttpException{
        private static long serialVersionUID = 1L;

        public NoMatchingRouteException() {
                super(404);
        }

        public NoMatchingRouteException(String message) {
                super(message,404);

        }



        public NoMatchingRouteException(String message, Throwable throwable) {
                super(message, throwable,404);
        }


        public NoMatchingRouteException(Throwable throwable) {
                super(throwable,404);
        }


        public NoMatchingRouteException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
                super(message, throwable, enableSuppression, writableStackTrace,404);
        }

}
