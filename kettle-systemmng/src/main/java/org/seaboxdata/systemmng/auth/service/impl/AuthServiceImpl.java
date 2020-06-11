package org.seaboxdata.systemmng.auth.service.impl;


import org.apache.commons.lang.StringUtils;
import org.seaboxdata.systemmng.auth.service.AuthService;
import org.seaboxdata.systemmng.auth.utils.PropertiesUtil;
import org.seaboxdata.systemmng.auth.utils.RestTemplateUtils;
import org.seaboxdata.systemmng.auth.utils.UrlEnum;
import org.seaboxdata.systemmng.auth.vo.OauthUserDTO;
import org.seaboxdata.systemmng.auth.vo.OnlineUser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Service("authService")
public class AuthServiceImpl implements AuthService {

    /**
     * 鉴权模式
     */
    public static final String[] GRANT_TYPE = {"password", "refresh_token", "access_token"};

    private static RestTemplate restTemplate = RestTemplateUtils.restTemplate();
    private static String clientId = new PropertiesUtil("environment.properties").readProperty("client-id");
    private static String clientSecret = new PropertiesUtil("environment.properties").readProperty("client-secret");
    private static String authUrl = new PropertiesUtil("environment.properties").readProperty("auth-url");
    private static String authFrontierUrl = new PropertiesUtil("environment.properties").readProperty("auth-frontier-url");

    @Override
    public OnlineUser nowOnlineUser(String accessToken, HttpServletRequest request, HttpServletResponse response) {
        OnlineUser onlineUser = null;
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
        formData.add("token", accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthorizationHeader(clientId, clientSecret));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        UriComponents b = UriComponentsBuilder.fromUriString(authUrl + UrlEnum.CHECK_TOKEN_URL.getUrl()).build();

        try {
            Map<String, Object> map = restTemplate.exchange(b.toUri(), HttpMethod.POST, new HttpEntity<>(formData, headers), Map.class).getBody();
            /*if("invalid_token".equals(map.get("error"))){
                System.out.println("令牌一次失效");;
                map = frontierAuthFreshToken(request, response);
            }*/
            onlineUser = createOnlineUser(map);
        }catch (Exception e) {
            onlineUser = null;
            System.out.println("令牌一次校验异常");
            try {
                Map<String, Object> map = frontierAuthFreshToken(request, response);
                onlineUser = createOnlineUser(map);
            }catch (Exception e2){
                System.out.println("令牌二次失效");
            }
        }

        return onlineUser;
    }

    public Map<String, Object> frontierAuthFreshToken(HttpServletRequest request, HttpServletResponse response){

        Map<String, Object> resMap = new HashMap<>();

        if(null == request || null == response){
            try {
                request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            } catch (Exception e1) {

            }
        }

        Cookie[] cookieAuth = request.getCookies();
        String accessToken = "";
        String refreshToken = "";
        if (cookieAuth != null) {
            for (Cookie coo : cookieAuth) {
                if (GRANT_TYPE[1].equals(coo.getName())) {
                    refreshToken = coo.getValue();
                }
                if (GRANT_TYPE[2].equals(coo.getName())) {
                    accessToken = coo.getValue();
                }
            }
        }

        if(StringUtils.isBlank(accessToken) || StringUtils.isBlank(refreshToken)){
            return resMap;
        }

        Map<String, String> reqMap = new HashMap<>();
        reqMap.put("clientId", clientId);
        reqMap.put("clientSecret", clientSecret);
        reqMap.put("accessToken", accessToken);
        reqMap.put("refreshToken", refreshToken);

        String reqUrl = authFrontierUrl + UrlEnum.frontierAuthFreshToken.getUrl();
        resMap = restTemplate.exchange(reqUrl, HttpMethod.POST, new HttpEntity<>(reqMap), Map.class).getBody();

        if (resMap.containsKey(GRANT_TYPE[2])) {
            accessToken = (String) resMap.get("access_token");
        }
        if (resMap.containsKey(GRANT_TYPE[1])) {
            refreshToken = (String) resMap.get("refresh_token");
        }

        if(StringUtils.isNotBlank(accessToken)){
            Cookie accessTokenCookie = new Cookie("access_token", "Bearer" + accessToken);
            accessTokenCookie.setPath("/");
//            accessTokenCookie.setDomain(tokenDomain);
//            accessTokenCookie.setMaxAge(60 * 60 * 12 * 2 * 7);
//            accessTokenCookie.setHttpOnly(true);
            response.addCookie(accessTokenCookie);
        }
        if(StringUtils.isNotBlank(refreshToken)){
            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setPath("/");
//            refreshTokenCookie.setDomain(tokenDomain);
//            refreshTokenCookie.setMaxAge(60 * 60 * 12 * 2 * 7);
//            refreshTokenCookie.setHttpOnly(true);
            response.addCookie(refreshTokenCookie);
        }

        /**
         * 重新获取登录信息
         */
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthorizationHeader(clientId, clientSecret));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
        formData.add("token", accessToken);

        resMap = restTemplate.exchange(authUrl + UrlEnum.CHECK_TOKEN_URL.getUrl(), HttpMethod.POST, new HttpEntity<>(formData, headers), Map.class).getBody();

        return resMap;
    }

    @Override
    public Set<String> selectPermissionsByUserIdAndSystemToSet(Long userId){
        Set<String> permissionCodes = new HashSet<>();
        String reqUrl = authUrl + UrlEnum.selectPermissionsByUserIdAndSystem.getUrl() + "?userId="+ userId +"&appName=POPUPORTRAYAL";
        Map<Long, String> mapCodes = restTemplate.exchange(reqUrl, HttpMethod.GET, new HttpEntity<>(new LinkedMultiValueMap<>()), Map.class).getBody();
        for(String value : mapCodes.values()){
            permissionCodes.add(value);
        }
        return permissionCodes;
    }

    @Override
    public String queryUsersByIds(Long userId){
        String reqUrl = authUrl + UrlEnum.USER_QUERYUSERS_BY_IDS.getUrl();
        OauthUserDTO[] arrays = restTemplate.exchange(reqUrl, HttpMethod.POST, new HttpEntity<>(Arrays.asList(userId)), OauthUserDTO[].class).getBody();
        if(null != arrays && arrays.length > 0){
            return arrays[0].getUsername();
        }
        return "无";
    }

    @Override
    public Boolean loginOut(HttpServletRequest request){
        String reqUrl = authUrl + UrlEnum.LOGIN_OUT.getUrl();
        Boolean flag = restTemplate.exchange(reqUrl, HttpMethod.GET, new HttpEntity<>(new LinkedMultiValueMap<>(), this.createHttpHeaders(request)), Boolean.class).getBody();
        return flag;
    }

    private OnlineUser createOnlineUser(Map<String, Object> map){
        OnlineUser user = new OnlineUser();

        LinkedHashMap<String, LinkedHashMap<String, String>> principal = (LinkedHashMap)map.get("user_name");
        Set<Map.Entry<String, LinkedHashMap<String, String>>> entries = principal.entrySet();
        Iterator<Map.Entry<String, LinkedHashMap<String, String>>> iterator = entries.iterator();
        String userId = "";
        String tenantId = "";
        String username = "";

        while(iterator.hasNext()) {
            Map.Entry<String, LinkedHashMap<String, String>> next = (Map.Entry)iterator.next();
            if ("principal".equals(next.getKey())) {
                LinkedHashMap<String, String> value = (LinkedHashMap)next.getValue();
                if (value.containsKey("userId")) {
                    userId = String.valueOf(value.get("userId"));
                }

                if (value.containsKey("tenantId")) {
                    tenantId = String.valueOf(value.get("tenantId"));
                }

                if (value.containsKey("username")) {
                    username = String.valueOf(value.get("username"));
                }
            }
        }

        user.setUserId("".equals(userId) ? null : Long.valueOf(userId));
        user.setTenantId("".equals(tenantId) ? null : Long.valueOf(tenantId));
        user.setUsername("".equals(username) ? null : username);

        return user;
    }

    private HttpHeaders createHttpHeaders(HttpServletRequest request){
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", this.queryAuthorizationAddBearer(request));
        return headers;
    }

    private String getAuthorizationHeader(String clientId, String clientSecret) {

        if (clientId == null || clientSecret == null) {

        }

        String creds = String.format("%s:%s", clientId, clientSecret);
        try {
            BASE64Encoder base64Encoder = new BASE64Encoder();
            String basicToken = "Basic " + base64Encoder.encode(creds.getBytes("UTF-8"));
            return basicToken;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not convert String");
        }
    }

    private String handleToken(HttpServletRequest request, String authorization) {
        if ((authorization.toLowerCase().startsWith("bearer"))) {
            String authHeaderValue = authorization.substring("Bearer".length()).trim();
            int commaIndex = authHeaderValue.indexOf(',');
            if (commaIndex > 0) {
                authHeaderValue = authHeaderValue.substring(0, commaIndex);
            }
            return authHeaderValue;
        }
        return null;
    }

    public String extractHeaderToken(HttpServletRequest request){
        /**
         * 从head中获取token
         */
        Enumeration<String> authHeaders = request.getHeaders("Authorization");
        while (authHeaders.hasMoreElements()) {
            String authorization = authHeaders.nextElement();
            String authHeaderValue = handleToken(request, authorization);
            if (authHeaderValue != null) return authHeaderValue;
        }

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
            String authHeaderValue = handleToken(request, authorization);
            if (authHeaderValue != null) return authHeaderValue;
        }

        return null;
    }

    public String queryAuthorization(HttpServletRequest request){
        if(null == request){
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }
        return this.extractHeaderToken(request);
    }

    public String queryAuthorizationAddBearer(HttpServletRequest request){
        String authorization = this.queryAuthorization(request);
        if(!authorization.toLowerCase().startsWith("Bearer".toLowerCase())){
            authorization = "Bearer" + " " + authorization;
        }
        return authorization;
    }

}
