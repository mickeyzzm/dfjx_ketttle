package org.flhy.platform.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 处理xss攻击的过滤器
 * @author Admin
 * 2020年5月23日16:12:26
 */
public class RequestFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 将request通过自定义的装饰类进行装饰
        XssRequestWrapper xssRequest = new XssRequestWrapper((HttpServletRequest) request);
        filterChain.doFilter(xssRequest, response);
    }
}
