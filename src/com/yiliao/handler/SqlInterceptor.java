package com.yiliao.handler;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.yiliao.consts.ConstantHtml;
import com.yiliao.util.CharUtils;
import com.yiliao.util.InterceptorUtil;

public class SqlInterceptor extends HandlerInterceptorAdapter {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SqlInterceptor.class);
	
	/***sql关键字***/
	private static final String[] SQL_KEY_WORD = { "and","exec","execute","insert","select","delete","update",
	"count","chr","mid","master","truncate"	,"char","declare","or","like",};
	/***特殊字符拦截***/
	private static final String[] SPECIAL_SYMBOL = { };
	
	/****拦截器，防止sql注入*****/
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		
		//String url = request.getRequestURL().toString();
		Enumeration<String> names = request.getParameterNames();
		while(names.hasMoreElements()){
			String str = names.nextElement();
			String param = request.getParameter(str);
			if(CharUtils.contains(SQL_KEY_WORD, param) ){
				logger.info("含有非法字段："+param);
				InterceptorUtil.jump(request,response,ConstantHtml.ILLEGAL_HTML);
				return false ;
			}
			if(CharUtils.exists(SPECIAL_SYMBOL, param) ){
				logger.info("含有非法符号："+param);
				InterceptorUtil.jump(request,response,ConstantHtml.ILLEGAL_HTML);
				return false ;
			}
			
		}
		return true;
	}

}
