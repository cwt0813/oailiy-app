<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>小情人APP邀你分享百万红包</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<link href="https://res.cdn.openinstall.io/apk_icon/p3opbw/3593633452836111989-1568173799319.png" rel="apple-touch-icon-precomposed">

<link rel="stylesheet" href="https://res.cdn.openinstall.io/api_res/css/style.css">
<style type="text/css">
*{
    -webkit-tap-highlight-color:rgba(0,0,0,0);
}
</style>
<script src="../js/jquery-2.0.3.min.js"></script>
<script src="../js/mobile-detect.js"></script>
<script type="text/javascript">
	//获取初始信息
	var app = navigator.appVersion;
	//根据括号进行分割
	var left = app.indexOf('(');
	var right = app.indexOf(')');
	var str = app.substring(left + 1, right);

	var Str = str.split(";");
	//手机型号--苹果 iPhone
	var Mobile_Iphone = Str[0];
	//手机型号--安卓 
	var Mobile_Android = Str[2];
	// 红米手机等特殊型号处理 匹配字符
	var res = /Android/;
	var reslut = res.test(Mobile_Android);

	//设备
	var equipment = '';
	//系统
	var system_moble = '';

	//根据设备型号判断设备系统
	if (Mobile_Iphone.indexOf('iPhone') >= 0) {
		equipment = 'iPhone';
		system_moble = Str[1].substring(4, Str[1].indexOf('like'));
	} else if (Mobile_Iphone == 'Linux') {
		if (reslut) {
			equipment = 'Android';
			system_moble = Str[2];
			//alert('访问设备型号' + Str[4].substring(0, 9) + '|系统版本' + Str[2]+'|设备号:'+Str[2]);
		} else {
			equipment = 'Android';
			system_moble = Str[1];
			//alert('访问设备型号' + Mobile_Android.substring(0, 9) + '|系统版本' + Str[1]+'|设备号:'+Str[2]);
		}
	}
</script>
</head>
<body>
	 <input id="userId" type="hidden" value="${userId}">
	 <div class="udid-content channel-content">
      <img style="width: 72px; height: 72px" alt="" src="https://res.cdn.openinstall.io/apk_icon/p3opbw/3593633452836111989-1568173799319.png">
      <p style="font-size:1.6rem;margin:2px auto;">小情人</p>
    </div>
    <div class="channel-title" style="text-align:center">
	  <p>【小情人】美女众多的一对一视频交友平台</p>
	  <p>网红模特、同城妹子主动发起视频聊天。</p>
	</div>
</body>
<script type="text/javascript" id="_openinstall_banner" src="//openinstall.io/openinstall.js?id=7499843363934916724"></script>
<script type="text/javascript">
	$(function() {
		  $.ajax({
			type : 'POST',
			url : '../share/addShareInfo.html',
			data : {
				userId : $('#userId').val(),
				equipment : equipment,
				system_moble : system_moble
			},
			dataType : 'json',
			success : function(data) {
				//if (data.m_istatus == 1) {
				//	
				//	if (Mobile_Iphone.indexOf('iPhone') >= 0) {
				//		//苹果下载地址
				//		window.location.href = $('#t_ios_download').val();
 				//
				//	} else {
				//		window.location.href = $('#t_android_download').val();
				//	}
				//} else {
				//	window.location.href = projectPath + '/error.html';
				//}
			}
		});
	});
	
	function getQueryString(name) { 
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"); 
		var r = window.location.search.substr(1).match(reg); 
		if (r != null) return unescape(r[2]); return null; 
    } 
</script>
</html>

