package org.seaboxdata.systemmng.utils.task;

import java.security.MessageDigest;

/**
 * @author
 * @date 2020-03-12 10:37
 * MD5加密工具
 */
public class MD5Util {

    private static final String SALT = "tamboo";

    public static String encode(String password) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        char[] charArray = password.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }

            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static void main(String [] args){
        String pwd = "521125";
        String enPwd = encode(pwd);
        System.out.println(enPwd);
    }
}