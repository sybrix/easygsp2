package sybrix.easygsp2.anno;
/**
 * Created by dsmith on 4/19/15.
 */

public @interface Mapping {
        String[] pattern() default {};
        String[] methods() default {};
}