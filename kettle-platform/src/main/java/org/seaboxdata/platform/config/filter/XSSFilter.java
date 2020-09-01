package org.seaboxdata.platform.config.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.seaboxdata.platform.config.wrapper.XssRequestWrapper;

/**
 * 处理xss攻击的过滤器
 * 
 * @author Admin 2020年5月23日16:12:26
 */
public class XSSFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
	}
}