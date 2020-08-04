package org.seaboxdata.platform.config.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.seaboxdata.systemmng.utils.auth.PropertiesUtil;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

/**
 * Created by cRAZY on 2017/4/12.
 */
public class BeforeLoginFilter implements Filter{
	private static PropertiesUtil environment = new PropertiesUtil("config/environment.properties");
    private boolean openAuth;
	
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    	openAuth = Boolean.valueOf(environment.readProperty("openAuth"));
    }

    @Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain chain) throws IOException, ServletException {
		if (!openAuth) {
			HttpServletRequest request = (HttpServletRequest) arg0;
			HttpServletResponse response = (HttpServletResponse) arg1;
			HttpSession session = request.getSession();
			if (null != session.getAttribute("login")) {
				response.sendRedirect(request.getContextPath() + "/index.jsp");
				return;
			} else {
				chain.doFilter(arg0, arg1);
				return;
			}
		}
	}

    @Override
    public void destroy() {

    }
}
