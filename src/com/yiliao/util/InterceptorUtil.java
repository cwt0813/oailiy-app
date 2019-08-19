package com.yiliao.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

public class InterceptorUtil {
	
	/** 登录地址 **/
	private static String loginUrl;
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public static void jump(HttpServletRequest request,HttpServletResponse response,String loginHtml) throws IOException{
		String requestType = request.getHeader("X-Requested-With");
		if("XMLHttpRequest".equals(requestType)){
			response.getOutputStream().print("{'result':'"+getLoginUrl(request)+"'}");
			return;
		}
//		response.sendRedirect(getLoginUrl(request));
		response.setContentType("text/html;utf-8");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(String.format(loginHtml,getLoginUrl(request)));
		response.getWriter().close();
	}


	/**
	 * @return the loginUrl
	 */
	public static String getLoginUrl(HttpServletRequest request) {
		if(StringUtils.isEmpty(loginUrl)) {
			setLoginUrl(request.getContextPath() + "/web/login.action");
		}
		return loginUrl;
	}
	
	public static String getLoginUrl() {
		return loginUrl;
	}

	/**
	 * @param loginUrl the loginUrl to set
	 */
	public static void setLoginUrl(String url) {
		loginUrl = url;
	}
}
