package org.seaboxdata.systemmng.utils.auth;

public enum UrlEnum {

    //oauth2登录
    LOGIN_URL("/oauth/token"),
    CHECK_TOKEN_URL("/oauth/check_token"),
    LOGIN_OUT_RL("/server/logout/user"),
    SELECT_PERMISSION_CODE_BY_USERID("/role/get/permission/userid"),
    USER_QUERYUSERS_BY_IDS("/user/queryUsersByIds"),
    USER_SELECT_USERID("/user/select/userid"),
    selectPermissionsByUserIdAndSystem("/role/get/permission/userid/system"),
    LOGIN_OUT("/logout/user"),
    SELECTALLTENANT("/tenant/select/all"),
    frontierAuthFreshToken("/frontier/auth/freshToken")
    ;

    private String url;

    UrlEnum(String url) {
        this.url = url;

    }


    public String getUrl() {
        return url;
    }
}
