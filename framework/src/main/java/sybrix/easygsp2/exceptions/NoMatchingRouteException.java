package sybrix.easygsp2.exceptions;

/**
 * Created by dsmith on 8/15/15.
 */
public class NoMatchingRouteException extends RuntimeException{
        public NoMatchingRouteException(Exception e) {
                super(e);
        }
}
