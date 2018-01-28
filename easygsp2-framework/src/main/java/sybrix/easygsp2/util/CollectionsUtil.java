package sybrix.easygsp2.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dsmith on 9/4/16.
 */
public class CollectionsUtil {

        public static <T> List<T> toList(T[] methods) {
                List<T> l = new ArrayList<T>();
                if (methods != null) {
                        for (T m : methods) {
                                l.add(m);
                        }
                }
                return l;
        }

        public static <T> T[] toArray(List<T> list) {
                T[] ary = (T[]) new Object[list.size()];
                list.toArray(ary);

                return ary;
        }

        public static <T> T[] add(T[] ary1, T[] ary2) {
                int i = ary1 != null ? ary1.length : 0;
                i = i + (ary2 != null ? ary2.length : 0);

                T[] ary = (T[])new Object[i];

                if (ary1!=null){
                        for(int x=0;x<ary1.length;x++){
                                ary[x] = ary1[x];
                        }
                }

                if (ary2!=null){
                        for(int x=0;x<ary2.length;x++){
                                ary[x] = ary2[x];
                        }
                }

                return ary;
        }
}
