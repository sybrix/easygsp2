package sybrix.easygsp2.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by dsmith on 4/19/15.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Api {

        String[] url();

        String[] method() default {"*"};

        String[] roles() default {"*"};

        String[] accepts() default {};

        String[] contentType() default {};
}