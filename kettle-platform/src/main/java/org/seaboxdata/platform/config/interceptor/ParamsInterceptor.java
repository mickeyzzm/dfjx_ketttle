package org.seaboxdata.platform.config.interceptor;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.seaboxdata.systemmng.utils.auth.PropertiesUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.InputSource;

public class ParamsInterceptor implements HandlerInterceptor {
	private static PropertiesUtil allowDomain = new PropertiesUtil("config/allowDomains.properties");
 
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	Map ParameterMap =  request.getParameterMap();
        Set<Map.Entry<String,String[]>> entry = ParameterMap.entrySet();
        Iterator<Map.Entry<String,String[]>> it = entry.iterator();
        int overLengthLimit = Integer.parseInt(allowDomain.readProperty("overLengthLimit")==null? "30":allowDomain.readProperty("overLengthLimit"));
        boolean overLength = false;
       
        Map<String, String> paramsMap = new HashMap<String, String>();
        
        while (it.hasNext()){
            Map.Entry<String,String[]>  me = it.next();
            String key = me.getKey();
            String value = me.getValue()[0];
            paramsMap.put(key, value);
            if(StringUtils.isNotEmpty(value)) {
            	if(value.indexOf("mxGraphModel") == -1) {
            		if(value.length()>overLengthLimit) {
            			overLength = true;
            		}
            	}
            }
        }
        
        System.out.println("=========参数：" + paramsMap);
        if(overLength) {
        	throw new RuntimeException("参数长度过长!");
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
 
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    	
    }
    
}