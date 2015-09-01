package sybrix.easygsp2.util;

import java.math.BigDecimal;


public class NumberUtil {

        public static Double toDbl(Object val, Double defaultVal) {
                if (val == null) {
                        return defaultVal;
                }
                try {
                        return toDbl(val);
                } catch (Exception e) {
                        return defaultVal;
                }

        }

        public static Double toDbl(Object val) {
                if (val == null || StringUtil.isEmpty(val.toString())) {
                        return null;
                }

                return Double.parseDouble(val.toString());

        }

        public static BigDecimal toBD(Object val) {
                if (val == null || StringUtil.isEmpty(val.toString())) {
                        return null;
                }

                return new BigDecimal(val.toString());

        }

        public static BigDecimal toBD(Object val, BigDecimal defaultVal) {
                if (val == null) {
                        return defaultVal;
                }

                try {
                        return toBD(val);
                } catch (Exception e) {
                        return defaultVal;
                }
        }


        public static Integer toInt(Object val, Integer defaultVal) {
                if (val == null) {
                        return defaultVal;
                }

                try {
                        return toInt(val);
                } catch (Exception e) {
                        return defaultVal;
                }
        }

        public static Integer toInt(Object val) {
                if (val == null || StringUtil.isEmpty(val.toString())) {
                        return null;
                }
                return Integer.parseInt(val.toString());
        }

        public static Long toLong(Object val, Long defaultValue) {
                if (val == null) {
                        return defaultValue;
                }

                try {
                        return toLong(val);
                } catch (Exception e) {
                        return defaultValue;
                }
        }

        public static Long toLong(Object val) {

                if (val == null || StringUtil.isEmpty(val.toString())) {
                        return null;
                }

                if (val instanceof Number) {
                        return ((Number) val).longValue();
                } else {
                        return Long.parseLong(val.toString());
                }
        }

        public static BigDecimal round(BigDecimal d, Integer scale) {
                return d.setScale(scale, BigDecimal.ROUND_HALF_UP);
        }

}
