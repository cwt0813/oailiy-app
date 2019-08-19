package com.yiliao.util;

import java.util.Arrays;
import java.util.List;

public class KeyWordUtil {

	/**
	 *  过滤字符串  出现关键字的地方已*号代替
	 * @param keyWords
	 * @param str
	 * @return
	 */
	public static String  filterKeyWord(String[] keyWords,String str) {
		
		for(String s : keyWords) {
			char[] charArray = s.toCharArray();
			int count = 0;
			for(char c : charArray) {
				 if(str.indexOf(c)>=0){
					 count = count+1;
				 }
			}
			if(count == charArray.length) {
				for(char c : charArray) {
					 str = str.replaceAll(String.valueOf(c), "*");
				}
				return str;
			}
		}
		System.out.println("循环完毕后返回");
		return str;
	}
	
	
	public static void main(String[] args) {
		 
		
	}
}
