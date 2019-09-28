package sybrix.easygsp2.util;


public class Base64 {
        public static byte[] decode(String data) {
                return org.apache.commons.codec.binary.Base64.decodeBase64(data.getBytes());
        }

        public static byte[] decode(byte[] data) {
                return org.apache.commons.codec.binary.Base64.decodeBase64(data);
        }


        public static String encode(byte[] data) {
                return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
        }
}
