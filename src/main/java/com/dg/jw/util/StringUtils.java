package com.dg.jw.util;

public class StringUtils {
	
	public static boolean isNotEmpty(String str){
		
		if(str!=null && ! str.equals("") && str.length()>0 && !str.isEmpty() && !"null".equals(str)){
			return true;
		}
		return false;
	}

}
