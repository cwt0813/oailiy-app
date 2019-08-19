package com.yiliao.consts;

public interface ConstantHtml {
	static final String NOT_ALLOW_HTML = "<html><head><meta charset='utf-8'></head><body><script> window.onload = function(){alert('该帐号没有这个模块的权限，请重新登录！');window.top.location.href='%s'; } </script></body></html>";
	static final String RETURN_LOGIN_HTML = "<html><head><meta charset='utf-8'></head><body><script> window.onload = function(){alert('该帐号登录超时，或者已在别处登录，请重新登录！');window.top.location.href='%s'; } </script></body></html>";
	static final String ILLEGAL_HTML = "<html><head><meta charset='utf-8'></head><body><script> window.onload = function(){alert('非法访问，请重新登录！');window.top.location.href='%s'; } </script></body></html>";
}
