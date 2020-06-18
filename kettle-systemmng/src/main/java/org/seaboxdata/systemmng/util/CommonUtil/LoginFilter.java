package org.seaboxdata.systemmng.util.CommonUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.seaboxdata.systemmng.auth.service.AuthService;
import org.seaboxdata.systemmng.auth.utils.CookieUtils;
import org.seaboxdata.systemmng.auth.utils.PropertiesUtil;
import org.seaboxdata.systemmng.auth.utils.SpringContextHolder;
import org.seaboxdata.systemmng.auth.utils.UserThreadLocal;
import org.seaboxdata.systemmng.auth.vo.OnlineUser;
import org.seaboxdata.systemmng.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Set;

/**
 * Created by cRAZY on 2017/4/12.
 */

public class LoginFilter implements Filter{
    private final static Logger logger = LoggerFactory.getLogger(LoginFilter.class);
	private static PropertiesUtil environment = new PropertiesUtil("environment.properties");
    private boolean openAuth;
    private String gotoAuth;
    private String returnBack;

	
    private String loginUrl="";
    private String excludedPages="";
    private String[] excludedArray=null;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    	openAuth = Boolean.valueOf(environment.readProperty("openAuth"));
    	gotoAuth = environment.readProperty("gotoAuth");
    	returnBack = environment.readProperty("returnBack");
    	
        loginUrl=filterConfig.getInitParameter("login_url");
        excludedPages=filterConfig.getInitParameter("excludedPages");
        if(excludedPages.indexOf(",")!=-1){
            excludedArray=excludedPages.split(",");
        }else{
            excludedArray=new String[]{excludedPages};
        }

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    	try {
			if(openAuth) {
				doAuthFilter(servletRequest, servletResponse, chain);
			} else {
				doLocalFilter(servletRequest, servletResponse, chain);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void doLocalFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws Exception{
    	HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse)servletResponse;
        HttpSession session=request.getSession();
        String url = request.getRequestURL().toString();

        boolean isExclude=false;
        for(String excludePage:excludedArray){
            if(excludePage.equals(request.getServletPath())){
                isExclude=true;
            }
        }

        if (!isExclude) {
            Object user=session.getAttribute("login");
            if (user!=null) {
                chain.doFilter(servletRequest, servletResponse);
                return;
            }else {
                PrintWriter out=response.getWriter();
                //如果是异步请求
                if (request.getHeader("x-requested-with") != null && request.getHeader("x-requested-with").equals("XMLHttpRequest")) {
                    response.addHeader("sessionstatus", "timeout");
                    //chain.doFilter(request, response);
                    response.sendRedirect(request.getContextPath()+loginUrl);
                    return;
                }else {
                    response.sendRedirect(request.getContextPath()+loginUrl);
                    return;
                }
            }
        }else{
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
    }
    
    public void doAuthFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws Exception{
		System.out.println("---------------url-----------------------" + ((HttpServletRequest) servletRequest).getRequestURL());

		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		String uri = request.getRequestURI();
		String url = request.getRequestURL().toString();
		for(String page : excludedArray){
			if(uri.indexOf(page) > 0){
				chain.doFilter(servletRequest, servletResponse);
				return ;
			}
		}


		if (((HttpServletRequest) servletRequest).getRequestURL().indexOf("fromAuth") > 0) {
			chain.doFilter(servletRequest, servletResponse);
			return ;
		}

		String token = CookieUtils.GetToken(request);
		if(StringUtils.isBlank(token)){
			response.sendRedirect(this.goUrl(url));
			return ;
		}
		AuthService authService = SpringContextHolder.getBean("authService");

		OnlineUser user = authService.nowOnlineUser(token, request, response);
		if(null == user){
			response.sendRedirect(this.goUrl(url));
			return ;
		}
		user.setToken(token);

		Set<String> permissions = authService.selectPermissionsByUserIdAndSystemToSet(user.getUserId());
		user.setPermissions(permissions);

		UserThreadLocal.set(user);
        if(!checkAuth(url)){
            logger.info("用户没有权限，请联系管理员！");
            logger.info("用户没有权限，请联系管理员！");
            logger.info("用户没有权限，请联系管理员！");
            response.sendRedirect("/exception.jsp");
            return;
        }


        chain.doFilter(servletRequest, servletResponse);
    }


    /**
     * sdsjfzj_cqdc
     * 与非sdsjfzj_cqdc用户
     *
     * sdsjfzj_cqdc是全菜单
     * 非sdsjfzj_cqdc是普通用户
     * 不能看”用户“开头的菜单
     *
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     *
     * /userGroup/
     * /user/
     */
    private boolean checkAuth(String url){
        logger.info("checkAuth into url {}", url);
        logger.info("checkAuth into url {}", url);
        boolean bool = true;
        OnlineUser user = UserThreadLocal.get();
        String username = user.getUsername();
        logger.info("checkAuth into username {}", username);
        if("sdsjfzj_cqdc".equals(username)){
            bool = true;
        }else{
            if(url.indexOf("/user/getUsers")>-1 || url.indexOf("/userGroup/getUserGroupOfThisPage")>-1){
                bool = false;
            }
        }
        return bool;
    }
    

	private String goUrl(String url) throws UnsupportedEncodingException {
		String burl = returnBack + "?backUrl=" + URLEncoder.encode(url, "UTF-8");
		return gotoAuth + URLEncoder.encode(burl, "UTF-8");
	}
    
    @Override
    public void destroy() {
    	if(openAuth) {
    		UserThreadLocal.remove();
		}
    }
}
