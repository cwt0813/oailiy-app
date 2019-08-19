package com.yiliao.util;

import org.springframework.util.StringUtils;

public class CharUtils {

	public static boolean contains(String[] strs, String str) {

		for (String s : strs) {
			if (str.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean exists(String[] strs, String str) {

		for (String s : strs) {
			if (str.contains(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @Title: clearBracket
	 * @author zhanweiwei
	 * @Description:清除字符串前后的中括号
	 * @param @param content
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String clearBracket(String content) {

		String con = content;
		if (con.startsWith("[")) {
			con = con.substring(1);
		}
		if (con.endsWith("]")) {
			con = con.substring(0, con.length() - 1);
		}
		if (con.startsWith("\"")) {

			con = con.substring(1);
		}
		if (con.endsWith("\"")) {

			con = con.substring(0, con.length() - 1);
		}
		return con;
	}

	public static String findNumberFromText(String text) {

		text = text.trim();
		String letter = "";
		if (StringUtils.isEmpty(text)) {
			return "";
		}
		boolean flag = false;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) >= 48 && text.charAt(i) <= 57) {
				flag = true;
				letter += text.charAt(i);
			} else {
				if (flag) {
					break;
				}
			}
		}
		return letter.trim();
	}

	
	public static boolean stringStartWith(String str , String[] starts){
		
		if(StringUtils.isEmpty(str)){
			return false ;
		}
		if(starts==null || starts.length==0){
			return false ;
		}
		for(String start : starts){
			
			if(str.startsWith(start)){
				
				return true ;
			}
		}
		return false ;
	}
	
}
