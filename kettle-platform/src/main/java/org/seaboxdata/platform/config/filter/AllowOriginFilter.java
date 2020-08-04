package org.seaboxdata.platform.config.filter;

import java.io.IOException;
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

import org.seaboxdata.ext.utils.JsonUtils;
import org.seaboxdata.systemmng.entity.UserGroupAttributeEntity;
import org.seaboxdata.systemmng.utils.auth.PropertiesUtil;

public class AllowOriginFilter implements Filter {

	private static PropertiesUtil allowDomain = new PropertiesUtil("config/allowDomains.properties");
	
	private String allowDomains;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		allowDomains = allowDomain.readProperty("allowDomains");
	}	
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletResponse rep = (HttpServletResponse) response;
		HttpServletRequest req = (HttpServletRequest) request;
		// 设置允许多个域名请求
		String[] allowDomainArr = this.allowDomains.split(";");
		Set allowOrigins = new HashSet(Arrays.asList(allowDomainArr));
		String originHeads = req.getHeader("Origin");
		if (allowOrigins.contains(originHeads)) {
			// 设置允许跨域的配置
			// 这里填写你允许进行跨域的主机ip（正式上线时可以动态配置具体允许的域名和IP）
			rep.setHeader("Access-Control-Allow-Origin", originHeads);
		}
		
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}
}
