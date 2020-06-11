//package org.seaboxdata.systemmng.auth.controller;
//
//import org.apache.commons.lang.StringUtils;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
////@RestController
//public class LoginController {
//
//    @GetMapping("fromAuth")
//    public void loginAuth(@RequestParam("backUrl") String backUrl, @RequestParam(name = "access_token", required = false) String access_token, @RequestParam(name = "refresh_token", required = false) String refresh_token) throws IOException {
//        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
//        if(StringUtils.isNotBlank(access_token)){
//            Cookie accessTokenCookie = new Cookie("access_token", access_token);
//            accessTokenCookie.setPath("/");
////            accessTokenCookie.setDomain(tokenDomain);
//            accessTokenCookie.setMaxAge(60 * 60 * 12 * 2 * 7);
////            accessTokenCookie.setHttpOnly(true);
//            response.addCookie(accessTokenCookie);
//        }
//        if(StringUtils.isNotBlank(refresh_token)){
//            Cookie refreshTokenCookie = new Cookie("refresh_token", refresh_token);
//            refreshTokenCookie.setPath("/");
////            refreshTokenCookie.setDomain(tokenDomain);
//            refreshTokenCookie.setMaxAge(60 * 60 * 12 * 2 * 7);
////            refreshTokenCookie.setHttpOnly(true);
//            response.addCookie(refreshTokenCookie);
//        }
//        response.sendRedirect(backUrl);
//    }
//}
