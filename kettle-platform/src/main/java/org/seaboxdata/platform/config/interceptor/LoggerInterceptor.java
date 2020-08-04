package org.seaboxdata.platform.config.interceptor;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seaboxdata.systemmng.entity.UserEntity;
import org.seaboxdata.systemmng.utils.common.StringDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    	if(request != null && request.getSession()!= null) {
    		UserEntity loginUser=(UserEntity)request.getSession().getAttribute("login");
    		String user = loginUser ==null ?"":loginUser.getLogin();
    		String ip = getIpAddress(request);
    		String uri = request.getRequestURI();
    		String date=StringDateUtil.dateToString(new Date(),"yyyy-MM-dd HH:mm:ss");
    		String domain = getDomain(uri);
    		
    		logger.info("\r\n访问结束时间:{} \r\n访问IP:{} \r\n访问用户:{} \r\n访问模块:{} \r\n访问URI:{} ",date, ip, user, domain, uri);
    	}
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
		} else if (uri.indexOf("/userGroup/") != -1) {
			domain = "用户组管理";
		}
		
    	return domain;
    }
    
//    @Autowired  
//    private KafkaTemplate<Integer, String> kafkaTemplate;
//    /**
//      * 发送消息到Kafka
//	    * @param userId        登录用户id
//	    * @param optTime       用户操作时间 包括登录时间
//	    * @param clientIp      用户登录客户端Ip
//	    * @param visitModule   访问模块    //example: 用户认证 、业务模块
//	    * @param function      点击模块下的功能  //登录成功 登录失败 注册 注销 生成令牌
//	    * @param optContent    具体操作内容  //例如查看了【我的申请】模块  周畅登录平台！
//	    * @param requestPra    请求参数
//	    * @param servicePath   服务路径
//	    * @param executeSta    执行语句
//	    * @param logType       日志类型 0 代表接口访问日志 1 代表用户行为日志
//	    */
//    public void sendLoggerInfoToKafka(HttpServletRequest request) {
//    	UserActionLog userActionLog = new UserActionLog();
//    	userActionLog.setUserId(String.valueOf(userId));
//    	userActionLog.setOptTime(DateToolsUtils.dateToStr(beginDate));
//    	userActionLog.setClientIp(RemoteIpUtils.getCliectIp(request));
//    	userActionLog.setVisitModule(busilog.visitModule().getModule());
//    	userActionLog.setFunction(busilog.functionModule());
//    	userActionLog.setOptContent(busilog.optContent());
//    	userActionLog.setRequestPra(params);
//    	userActionLog.setServicePath(request.getRequestURL().toString());
//    	userActionLog.setExecuteSta("");
//    	userActionLog.setLogType("1");
//    	
//    	kafkaTemplate.sendDefault(userActionLog);  
//    }
}