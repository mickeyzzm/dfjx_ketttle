package org.seaboxdata.systemmng.utils.auth;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class CookieUtils {

    private static final String TOKEN_PRE = "bearer";

    public static String GetToken(HttpServletRequest request){
        /**
         * 从cookies中获取
         */
        Enumeration<String> headers = request.getHeaders("cookie");
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if (!value.contains("access_token")) {
                return null;
            }
            String accessToken = value.substring(value.indexOf("access_token="), value.indexOf("access_token=") + 55);
            String authorization = accessToken.substring(13);
            String authHeaderValue = handleToken(authorization);
            if (authHeaderValue != null) return authHeaderValue;
        }
        return null;
    }

    private static String handleToken(String authorization) {
        if ((authorization.toLowerCase().startsWith(TOKEN_PRE))) {
            String authHeaderValue = authorization.substring(TOKEN_PRE.length()).trim();
            int commaIndex = authHeaderValue.indexOf(',');
            if (commaIndex > 0) {
                authHeaderValue = authHeaderValue.substring(0, commaIndex);
            }
            return authHeaderValue;
        }
        return null;
    }
}
