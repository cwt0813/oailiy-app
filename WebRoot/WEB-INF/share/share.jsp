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
<!-- <title>小情人APP邀你分享百万红包</title> -->
<title>小情人</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<link href="https://res.cdn.openinstall.io/apk_icon/p3opbw/3593633452836111989-1568173799319.png" rel="apple-touch-icon-precomposed">

<link rel="stylesheet" href="https://res.cdn.openinstall.io/api_res/css/style.css">
<style type="text/css">
* {
	-webkit-tap-highlight-color: rgba(0, 0, 0, 0);
}

#Mask {
	position: absolute;
	top: 0;
	left: 0;
	display: none;
	background-image: url(../img/shareMask.png);
	background-repeat: no-repeat;
	background-size: 100% 100%;
	-moz-background-size: 100% 100%;
	/* background-color: #000000; */
	width: 100%;
	height: 100%;
	z-index: 1000;
}

#Mask2 {
	position: absolute;
	top: 0;
	left: 0;
	display: none;
	background-color: #000000;
	background-size: cover;
	width: 100%;
	height: 100%;
	z-index: 1001;
}

.model-content {
	width: 100%;
	height: 100%;
	text-align: center;
	background: #ffffff;
	border-radius: 6px;
	margin: 100px auto;
	line-height: 30px;
	z-index: 10001;
}

.mask_span {
	width: 100%;
	height: 200%;
	text-align: center;
	color: #ffffff;
	border-radius: 6px;
	margin: 100px auto;
	line-height: 30px;
	z-index: 10002;
	font-size: 30px;
	font-family: "microsoft yahei", "Arial";
	line-height: 1;
	-webkit-user-select: none;
	-webkit-text-size-adjust: 100% !important;
	text-size-adjust: 100% !important;
	-moz-text-size-adjust: 100% !important;
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
	
	<!--引入遮蔽层-->
	<div id="Mask" style="">
	</div>
	
	<!--引入遮蔽层-->
	<div id="Mask2" style="">
	</div>
	
</body>
<script type="text/javascript">
	
	$(function() {

		$("#Mask2").show();
			
		//判断是否在微信中打开
		var ua = navigator.userAgent;
		var isWeixin = !!/MicroMessenger/i.test(ua);
		//如果使用的是微信自带浏览器，就打开蒙版
		if (isWeixin) {
			document.querySelector('body').addEventListener('touchmove', function(e) {
				　　e.preventDefault();
				});
			var SHOW = 0;
			document.getElementById('Mask').style.display = ++SHOW % 2 == 1 ? 'block'
					: 'none';
		}else{
			window.location.href='../share/jumpRealShare.html?userId='+$('#userId').val();
		}
		
		$("#Mask2").hide();

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
		if (r != null)
			return unescape(r[2]);
		return null;
	}
</script>
</html>

