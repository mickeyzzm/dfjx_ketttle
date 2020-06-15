package org.flhy.platform.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.flhy.ext.utils.JsonUtils;
import org.seaboxdata.systemmng.auth.utils.PropertiesUtil;
import org.seaboxdata.systemmng.entity.UserGroupAttributeEntity;

public class GlobalFilter implements Filter {
	private static PropertiesUtil allowDomain = new PropertiesUtil("allowDomains.properties");
	
	private static PropertiesUtil environment = new PropertiesUtil("environment.properties");
    private boolean openAuth;
    private String gotoAuth;
    private String returnBack;
	
    private String loginUrl="";
	private String allowDomains;
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletResponse rep = (HttpServletResponse) response;
		JsonUtils.putResponse(rep);
		
		HttpServletRequest req = (HttpServletRequest) request;
		// 设置允许多个域名请求
		String[] allowDomainArr = this.allowDomains.split(";");
		Set allowOrigins = new HashSet(Arrays.asList(allowDomainArr));
		// if (Arrays.asList(Constants.ALLOW_DOMAIN).contains(originHeader)) {
		String originHeads = req.getHeader("Origin");
		if (allowOrigins.contains(originHeads)) {
			// 设置允许跨域的配置
			// 这里填写你允许进行跨域的主机ip（正式上线时可以动态配置具体允许的域名和IP）
			rep.setHeader("Access-Control-Allow-Origin", originHeads);
		}
		
		 UserGroupAttributeEntity attribute = (UserGroupAttributeEntity)req.getSession().getAttribute("userInfo");
		 if(null!=attribute){
			 String uri = req.getRequestURI();
			 String url = req.getRequestURL().toString();
			 String[] array = {"/user/getUsers.do", "/userGroup/getUserGroupOfThisPage.do", "/slave/slaveManager.do", "/slave/slaveQuatoByCondition.do"};
			 if(null !=attribute.getUserType()) {
				 int userType = attribute.getUserType();
				 if(userType!=1) {
					 if(Arrays.asList(array).contains(uri)) {
						 if(openAuth) {
							 rep.sendRedirect(this.goUrl(url));
						 } else {
							 rep.sendRedirect(req.getContextPath()+loginUrl);
						 }
					 }
				 }
			 }
		 }
		chain.doFilter(request, response);
	}

	private String goUrl(String url) throws UnsupportedEncodingException {
		String burl = returnBack + "?backUrl=" + URLEncoder.encode(url, "UTF-8");
		return gotoAuth + URLEncoder.encode(burl, "UTF-8");
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		allowDomains = allowDomain.readProperty("allowDomains");
		
		openAuth = Boolean.valueOf(environment.readProperty("openAuth"));
    	gotoAuth = environment.readProperty("gotoAuth");
    	returnBack = environment.readProperty("returnBack");
    	
        loginUrl=filterConfig.getInitParameter("login_url");
	}

}
