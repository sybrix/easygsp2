
package sybrix.easygsp2.util;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * this is not mine - not completely
 */
public class Validator {

        private static final String sp = "\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~";
        private static final String atext = "[a-zA-Z0-9" + sp + "]";
        private static final String atom = atext + "+"; //one or more atext chars
        private static final String dotAtom = "\\." + atom;
        private static final String localPart = atom + "(" + dotAtom + ")*";

        //RFC 1035 tokens for domain names:
        private static final String letter = "[a-zA-Z]+$";
        private static final String domainLetter = "[a-zA-Z]+";
        private static final String letDig = "[a-zA-Z0-9]+$";
        private static final String letDigHyp = "[a-zA-Z0-9-]+$";
        private static final String digit = "[0-9]";

        public static final String rfcLabel = "[a-zA-Z0-9]+" + "[a-zA-Z0-9-]+" + "{0,61}" + "[a-zA-Z0-9]+";

        private static final String domain = rfcLabel + "(\\." + rfcLabel + ")*\\." + domainLetter + "{2,6}";
        //Combined together, these form the allowed email regexp allowed by RFC 2822:
        private static final String addrSpec = "^" + localPart + "@" + domain + "$";

        //now compile it:
        public static final Pattern EMAIL_PATTERN = Pattern.compile(addrSpec);

        public static final Pattern PHONE_PATTERN = Pattern.compile("(\\d-)?(\\d{3}-)?\\d{3}-\\d{4}");
        ;
        public static final Pattern ZIPCODE_PATTERN = Pattern.compile("\\d{5}(-\\d{4})?");

        public static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile(letDig);
        public static final Pattern LETTERS_PATTERN = Pattern.compile(letter);
        public static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+?)");
        public static final Pattern NUMERIC_PATTERN = Pattern.compile("[+-]?\\d*(\\.\\d+)?");

        public static final int INVALID = -1;
        public static final int VISA = 0;
        public static final int MASTERCARD = 1;
        public static final int AMERICAN_EXPRESS = 2;
        public static final int EN_ROUTE = 3;
        public static final int DINERS_CLUB = 4;

        private static final String[] cardNames =
                {"Visa",
                        "Mastercard",
                        "American Express",
                        "En Route",
                        "Diner's CLub/Carte Blanche",
                };


        public static boolean isEmailValid(String value) {
                if (StringUtil.isEmpty(value))
                        return false;
                return EMAIL_PATTERN.matcher(value).matches();
        }

        public static boolean isNumeric(Object value) {
                if (value instanceof Number)
                        return true;

                if (value == null || StringUtil.isEmpty(value.toString()))
                        return false;
                return NUMERIC_PATTERN.matcher(value.toString()).matches();
        }

        public static boolean isLettersOnly(String value) {
                if (value == null)
                        return false;
                return LETTERS_PATTERN.matcher(value).matches();
        }

        public static boolean isAlphaNumeric(String value) {
                if (value == null)
                        return false;
                return ALPHA_NUMERIC_PATTERN.matcher(value).matches();
        }

        public static boolean isValidPhone(String value) {
                return PHONE_PATTERN.matcher(value).matches();
        }

        public static boolean isZipCodeValid(String value) {
                return ZIPCODE_PATTERN.matcher(value).matches();
        }

        public static boolean matches(String value1, String value2) {
                try {
                        if (value1 == null || value2 == null) {
                                return false;
                        } else if (value1.trim().length() == 0 || value2.trim().length() == 0) {
                                return false;
                        } else {
                                return value1.equals(value2);
                        }
                } catch (Exception e) {
                        return false;
                }
        }

        /**
         * Returns -1 if too short, 1 if too long, else 0 if ok
         *
         * @param value url to measure
         * @param min   - Minimum string length(must be greater than)
         * @param max   - Maximum string length (cannot be greater than)
         * @return -1 if too short, 1 if too long, else 0 if ok
         */
        public static int lengthMinMax(String value, int min, int max) {
                if (null == value) {
                        return -1;
                } else if (value.trim().length() < min) {
                        return -1;
                } else if (value.trim().length() > max) {
                        return 1;
                } else {
                        return 0;
                }
        }

        public static boolean isTooShort(String value, int min) {
                if (null == value) {
                        return true;
                } else if (value.trim().length() < min) {
                        return true;
                } else {
                        return false;
                }
        }

        public static boolean isTooLong(String value, int max) {
                if (null == value) {
                        return false;
                } else if (value.trim().length() > max) {
                        return true;
                } else {
                        return false;
                }
        }

        public static boolean isCreditCardValid(String value) {
                if (getCardID(value) > -1) {
                        return validCCNumber(value);
                }
                return false;
        }
//
//        public static boolean isValidUrlValid(Object url) {
//                return true;
//        }


        public static Boolean isDate(String dateString, String format) {

                try {
                        SimpleDateFormat sdf = new SimpleDateFormat(format);
                        sdf.setLenient(false);
                        sdf.parse(dateString);
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public static boolean isNumber(String n) {
                try {
                        double d = Double.valueOf(n).doubleValue();
                        return true;
                }
                catch (NumberFormatException e) {
                        e.printStackTrace();
                        return false;
                }
        }

        public static String getCardName(int id) {
                return (id > -1 && id < cardNames.length ? cardNames[id] : "");
        }

        public static boolean validCCNumber(String n) {
                try {
                        /*
                        ** known as the LUHN Formula (mod10)
                        */
                        int j = n.length();

                        String[] s1 = new String[j];
                        for (int i = 0; i < n.length(); i++) s1[i] = "" + n.charAt(i);

                        int checksum = 0;

                        for (int i = s1.length - 1; i >= 0; i -= 2) {
                                int k = 0;

                                if (i > 0) {
                                        k = Integer.valueOf(s1[i - 1]).intValue() * 2;
                                        if (k > 9) {
                                                String s = "" + k;
                                                k = Integer.valueOf(s.substring(0, 1)).intValue() +
                                                        Integer.valueOf(s.substring(1)).intValue();
                                        }
                                        checksum += Integer.valueOf(s1[i]).intValue() + k;
                                } else
                                        checksum += Integer.valueOf(s1[0]).intValue();
                        }
                        return ((checksum % 10) == 0);
                }
                catch (Exception e) {
                        e.printStackTrace();
                        return false;
                }
        }

        /*
        ** For testing purpose
        **
        **   java CCUtils [credit card number] or java CCUtils
        **
        */



        /**
         * Valid a Credit Card number
         */
        public static boolean validCC(String number)
                throws Exception {
                int CardID;
                if ((CardID = getCardID(number)) != -1)
                        return validCCNumber(number);
                return false;
        }

        /**
         * Get the Card type
         * returns the credit card type
         * INVALID          = -1;
         * VISA             = 0;
         * MASTERCARD       = 1;
         * AMERICAN_EXPRESS = 2;
         * EN_ROUTE         = 3;
         * DINERS_CLUB      = 4;
         */
        public static int getCardID(String number) {
                int valid = INVALID;

                String digit1 = number.substring(0, 1);
                String digit2 = number.substring(0, 2);
                String digit3 = number.substring(0, 3);
                String digit4 = number.substring(0, 4);

                if (isNumber(number)) {
                        /* ----
                        ** VISA  prefix=4
                        ** ----  length=13 or 16  (can be 15 too!?! maybe)
                        */
                        if (digit1.equals("4")) {
                                if (number.length() == 13 || number.length() == 16)
                                        valid = VISA;
                        }
                        /* ----------
                        ** MASTERCARD  prefix= 51 ... 55
                        ** ----------  length= 16
                        */
                        else if (digit2.compareTo("51") >= 0 && digit2.compareTo("55") <= 0) {
                                if (number.length() == 16)
                                        valid = MASTERCARD;
                        }
                        /* ----
                        ** AMEX  prefix=34 or 37
                        ** ----  length=15
                        */
                        else if (digit2.equals("34") || digit2.equals("37")) {
                                if (number.length() == 15)
                                        valid = AMERICAN_EXPRESS;
                        }
                        /* -----
                        ** ENROU prefix=2014 or 2149
                        ** ----- length=15
                        */
                        else if (digit4.equals("2014") || digit4.equals("2149")) {
                                if (number.length() == 15)
                                        valid = EN_ROUTE;
                        }
                        /* -----
                        ** DCLUB prefix=300 ... 305 or 36 or 38
                        ** ----- length=14
                        */
                        else if (digit2.equals("36") || digit2.equals("38") ||
                                (digit3.compareTo("300") >= 0 && digit3.compareTo("305") <= 0)) {
                                if (number.length() == 14)
                                        valid = DINERS_CLUB;
                        }
                }
                return valid;

                /* ----
                ** DISCOVER card prefix = 60
                ** --------      lenght = 16
                **      left as an exercise ...
                */

        }

}
