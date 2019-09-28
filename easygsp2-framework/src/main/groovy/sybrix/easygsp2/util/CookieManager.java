package sybrix.easygsp2.util;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.net.URLDecoder;


/**
 * Created by IntelliJ IDEA.
 * User: dsmith
 * Date: Apr 12, 2007
 * Time: 6:12:40 PM
 */
public class CookieManager {
        //private static final Logger log = Logger.getLogger(CookieManager.class);
        public static final int DAY = 60 * 60 * 24;

        /**
         * Returns an instance to the specified cookie.
         *
         * @param cookieName String
         * @return Cookie - returns null if the cookie doesn't exists.
         */
        public static Cookie getCookie(String cookieName, HttpServletRequest request) {

                Cookie c[] = request.getCookies();
                if (c != null) {
                        for (int x = 0; x < c.length; x++) {
                                if (c[x].getName().equals(cookieName)) {
                                        return c[x];
                                }
                        }
                }
                return null;
        }

        public static Cookie createCookie(String cookieName, String value, int expiresInDays) {
                Cookie cookie = new Cookie(cookieName, value);
                cookie.setMaxAge(60 * 60 * 24 * expiresInDays);
                cookie.setPath("/");
                return cookie;

        }

        public static Map<String, String> getCookieMap(Cookie cookie, HttpServletRequest request) {
                if (request.getAttribute(cookie.getName() + "_cookie") == null) {
                        String _value =cookie.getValue();
                        String pair[] = _value.split(";");


                        Map map = new HashMap();
                        for (int i = 0; i < pair.length; i++) {
                                int index = pair[i].indexOf('=');
                                if (index > -1) {
                                        String name = pair[i].substring(0, index);
                                        String value = pair[i].substring(index + 1, pair[i].length());

                                        map.put(name, value);
                                }
                        }
                        request.setAttribute(cookie.getName() + "_cookie", map);
                }



                return (Map) request.getAttribute(cookie.getName() + "_cookie");
        }


//        public static String getCookieMapValue(Map map) {
//                Iterator i = map.keySet().iterator();
//                StringBuffer val = new StringBuffer(100);
//                while (i.hasNext()) {
//                        String key = i.next().toString();
//                        String value = map.get(key).toString();
//                        if (key.trim().length() > 0)
//                                val.append(key).append("=").append(value).append(";");
//                }
//
//                Encrypt enc = new Encrypt();
//                try {
//                        byte encrypted[] = enc.encrypt(val.toString().getBytes());
//                        return java.net.URLEncoder.encode(Utilities.base64Encode(encrypted),"UTF-8");
//                } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                }
//
//                return "";
//        }
//
//        public static String decryptValue(String val) {
//                Encrypt enc = new Encrypt();
//                byte[] decoded = null;
//                try {
//                        String urlDecoded = URLDecoder.decode(val,"UTF-8");
//                        decoded = enc.base64Decode(urlDecoded);
//                        byte[] decrypted = enc.decrypt(decoded);
//                        return new String(decrypted);
//                }catch(Exception e){
//                        log.error(e.getMessage(),e);
//                }
//
//                return "";
//        }


//        private static byte[] padBytes(byte value[]) {
//                int len = 8;
//                int originalLength = value.length;
//                if (value.length > len) {
//                        len = ((value.length / 8) + 1) * 8;
//                }
//
//                value = Utilities.increaseArrayLength(value, len);
//
//                return value;
//        }
//
//        private static String pad(String value) {
//                int len = 8;
//                if (value.length() > len) {
//                        len = ((value.length() / 8) + 1) * 8;
//                }
//
//                StringBuffer v = new StringBuffer(len);
//                v.append(value);
//
//                for (int i = 0; i < len - value.length(); i++) {
//                        v.append(" ");
//                }
//
//                return v.toString();
//        }


}
