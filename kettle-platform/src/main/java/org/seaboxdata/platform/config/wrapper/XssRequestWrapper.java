package org.seaboxdata.platform.config.wrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class XssRequestWrapper extends HttpServletRequestWrapper {
    //白名单数组
    private static final String[] WHITE_LIST = {"content"};
    // 定义script的正则表达式
    private static final String REGEX_SCRIPT = "<((?i)script)[^>]?>[\\s\\S]?<\\/((?i)script)>";
    // 定义style的正则表达式
    private static final String REGEX_STYLE = "<((?i)style)[^>]?>[\\s\\S]?<\\/((?i)style)>";
    // 定义HTML标签的正则表达式
    private static final String REGEX_HTML = "<[^>]+>";////String regEx = "(?!<(img|p|span).*?>)<.*?>";
    // 定义空格回车换行符
    private static final String REGEX_SPACE = "\t|\r|\n";//\\s*|\t|\r|\n
    //定义所有w标签
    private static final String REGEX_W = "<w[^>]*?>[\\s\\S]*?<\\/w[^>]*?>";
    //定义SQL注入
    private static String reg = "(\\b(select|update|union|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }

        int count = values.length;

        String[] encodedValues = new String[count];

        for (int i = 0; i < count; i++) {
            //白名单放行的只有HTML标签，SQL标签还是要验证
            if (isWhitelist(parameter)) {
                if (sqlValidate(values[i])) {
                    encodedValues[i] = values[i];
                }
                encodedValues[i] = null;
            }
            encodedValues[i] = removeHtml(values[i]);
        }

        return encodedValues;

    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        if (value == null) {
            return null;
        }
        //白名单放行的只有HTML标签，SQL标签还是要验证
        if (isWhitelist(parameter)) {
            if (sqlValidate(value)) {
                return value;
            }
            return null;
        }
        return removeHtml(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value == null) {
            return null;
        }

        if (isWhitelist(name)) {
            if (sqlValidate(value)) {
                return value;
            }
            return null;
        }
        return removeHtml(value);
    }


    //\\b  表示 限定单词边界  比如  select 不通过   1select则是可以的
    private static Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);

    /**
     * SQL注入过滤器
     * @param str
     * @return
     */
    private static boolean sqlValidate(String str) {
        if (sqlPattern.matcher(str).find()) {
            System.out.println("未能通过过滤器：str=" + str);
            return false;
        }
        return true;
    }

    /**
     * 是否白名单，白名单的放行
     *
     * @param paramName
     * @return
     */
    private static boolean isWhitelist(String paramName) {
        String lowerParam = paramName.toLowerCase();
        String name = null;
        for (String y : WHITE_LIST) {
			if(y.toLowerCase().equals(lowerParam)) {
				name = y;
			}
		}
        return name != null;
    }

    /**
     * 移除HTML标签
     * @param htmlStr
     * @return
     */
    private static String removeHtml(String htmlStr){
    	if(htmlStr.indexOf("mxGraphModel") == -1) {
	        Pattern p_w = Pattern.compile(REGEX_W, Pattern.CASE_INSENSITIVE);
	        Matcher m_w = p_w.matcher(htmlStr);
	        htmlStr = m_w.replaceAll(""); // 过滤script标签
	
	        Pattern p_script = Pattern.compile(REGEX_SCRIPT, Pattern.CASE_INSENSITIVE);
	        Matcher m_script = p_script.matcher(htmlStr);
	        htmlStr = m_script.replaceAll(""); // 过滤script标签
	
	        Pattern p_style = Pattern.compile(REGEX_STYLE, Pattern.CASE_INSENSITIVE);
	        Matcher m_style = p_style.matcher(htmlStr);
	        htmlStr = m_style.replaceAll(""); // 过滤style标签
	        
	    	Pattern p_html = Pattern.compile(REGEX_HTML, Pattern.CASE_INSENSITIVE);
	    	Matcher m_html = p_html.matcher(htmlStr);
	    	htmlStr = m_html.replaceAll(""); // 过滤html标签
	
	        Pattern p_space = Pattern.compile(REGEX_SPACE, Pattern.CASE_INSENSITIVE);
	        Matcher m_space = p_space.matcher(htmlStr);
	        htmlStr = m_space.replaceAll(""); // 过滤空格回车标签
	        //htmlStr = htmlStr.replaceAll(" ", ""); //过滤
    	}
        return htmlStr.trim(); // 返回文本字符串
    }}
