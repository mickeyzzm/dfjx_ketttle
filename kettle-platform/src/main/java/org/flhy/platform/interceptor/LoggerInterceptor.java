package org.flhy.platform.interceptor;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seaboxdata.systemmng.entity.UserEntity;
import org.seaboxdata.systemmng.util.CommonUtil.StringDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoggerInterceptor implements HandlerInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(LoggerInterceptor.class);
 
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	UserEntity loginUser=(UserEntity)request.getSession().getAttribute("login");
    	String user = loginUser ==null ?"":loginUser.getLogin();
    	String ip = getIpAddress(request);
    	String uri = request.getRequestURI();
    	String date=StringDateUtil.dateToString(new Date(),"yyyy-MM-dd HH:mm:ss");
    	String domain = getDomain(uri);
    	logger.info("\r\n访问开始时间:{} \r\n访问IP:{} \r\n访问用户:{} \r\n访问模块:{} \r\n访问URI:{}",date, ip, user,domain, uri);
        return true;
    }
 
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
       // logger.info("进入 postHandle 方法..." + request.getRequestURL().toString() + "," + request.getRequestURI());
    }
 
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    	UserEntity loginUser=(UserEntity)request.getSession().getAttribute("login");
    	String user = loginUser ==null ?"":loginUser.getLogin();
    	String ip = getIpAddress(request);
    	String uri = request.getRequestURI();
    	String date=StringDateUtil.dateToString(new Date(),"yyyy-MM-dd HH:mm:ss");
    	String domain = getDomain(uri);
    	
    	logger.info("\r\n访问结束时间:{} \r\n访问IP:{} \r\n访问用户:{} \r\n访问模块:{} \r\n访问URI:{} ",date, ip, user, domain, uri);
    }
    
    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
    }
    
    public String getDomain(String uri) {
    	String domain = "";
		if (uri.indexOf("/viewModule/") != -1) {
			domain = "平台-->平台概要";
		} else if (uri.indexOf("/user/captcha.do") != -1) {
			domain = "用户模块-->生产验证码";
		} else if (uri.indexOf("/viewModule/getData.do") != -1) {
			domain = "平台概要";
		} else if (uri.indexOf("/taskGroup/") != -1) {
			domain = "用户组管理";
		} else if (uri.indexOf("/repository/") != -1) {
			domain = "模型管理";
		} else if (uri.indexOf("/trans/") != -1) {
			domain = "转换管理";
		} else if (uri.indexOf("/job/") != -1) {
			domain = "作业管理";
		} else if (uri.indexOf("/task/") != -1) {
			domain = "任务管理";
		} else if (uri.indexOf("/task/") != -1) {
			domain = "任务管理";
		} else if (uri.indexOf("/scheduler/") != -1) {
			domain = "定时调度管理";
		} else if (uri.indexOf("/log/") != -1) {
			domain = "日志管理";
		} else if (uri.indexOf("/user/") != -1) {
			domain = "用户管理";
		}
		
    	return domain;
    }
}