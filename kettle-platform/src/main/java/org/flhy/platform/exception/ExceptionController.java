package org.flhy.platform.exception;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
 
@ControllerAdvice
public class ExceptionController {
	
	@Autowired
	private HttpServletRequest request;    
	@Autowired
	private HttpServletResponse response;
	
	/*
	 * @ExceptionHandler(Exception.class)
	 * 
	 * @ResponseBody public ModelAndView customException(Exception e) { try {
	 * response.setContentType("text/html;charset=utf-8"); PrintWriter pw =
	 * response.getWriter(); pw.write("操作出现问题，请检查!"); pw.flush(); pw.close(); }
	 * catch (Exception ex) { ex.printStackTrace(); } return null; }
	 */
	
	@ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Map<String, String> errorHandler(Exception ex) {
		LoggerFactory.getLogger(this.getClass()).error("【异常拦截日志】>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", ex);
        Map<String, String> map = new HashMap<String, String>();
        map.put("code", "400");
        //判断异常的类型,返回不一样的返回值
        if(ex instanceof MissingServletRequestParameterException){
            map.put("msg","缺少必需参数："+((MissingServletRequestParameterException) ex).getParameterName());
        }
        else if(ex instanceof Exception){
            map.put("msg","这是自定义异常");
        }
        return map;
    }
	

}
