package org.flhy.platform.filter;

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

import org.flhy.ext.utils.JsonUtils;
import org.seaboxdata.systemmng.auth.utils.PropertiesUtil;

public class GlobalFilter implements Filter {
	private static PropertiesUtil allowDomain = new PropertiesUtil("allowDomains.properties");
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

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		allowDomains = allowDomain.readProperty("allowDomains");
	}

}
