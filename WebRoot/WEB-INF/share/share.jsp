<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%
	String userAgent = request.getHeader("user-agent").toLowerCase();
	if(userAgent.indexOf("micromessenger")!= -1&&userAgent.indexOf("android") != -1){
		response.setHeader("Content-Disposition", " attachment; filename=\"load.doc\"");
		response.setHeader("Content-Type", " application/vnd.ms-word;charset=utf-8");
		response.setHeader("Content-Type", "html/text;charset=utf-8");
	}
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

<style>
	body,html{width:100%;height:100%}
	*{margin:0;padding:0}
	body{background-color:#fff}
	.top-bar-guidance{font-size:15px;color:#fff;height:40%;line-height:1.8;padding-left:20px;padding-top:20px;background:url(//gw.alicdn.com/tfs/TB1eSZaNFXXXXb.XXXXXXXXXXXX-750-234.png) center top/contain no-repeat}
	.top-bar-guidance .icon-safari{width:25px;height:25px;vertical-align:middle;margin:0 .2em}
	.app-download-btn{display:block;width:214px;height:40px;line-height:40px;margin:18px auto 0 auto;text-align:center;font-size:18px;color:#2466f4;border-radius:20px;border:.5px #2466f4 solid;text-decoration:none}
</style>

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

	<div class="top-bar-guidance" style="display:none" id="hidediv">
		<p>
			点击右上角<img src="//gw.alicdn.com/tfs/TB1xwiUNpXXXXaIXXXXXXXXXXXX-55-55.png" class="icon-safari"/> Safari打开
		</p>
		<p>
			可以继续访问本站哦~
		</p>
	</div>

	<div id="origion">
		<input id="userId" type="hidden" value="${userId}">
		<div class="udid-content channel-content">
			<img style="width: 72px; height: 72px" alt=""
				src="https://res.cdn.openinstall.io/apk_icon/p3opbw/3593633452836111989-1568173799319.png">
			<p style="font-size: 1.6rem; margin: 2px auto;">小情人</p>
		</div>
		<div class="channel-title" style="text-align: center">
			<p>【小情人】跟附近的人视频聊天，遇见你心中的TA</p>
		</div>
		<div style="text-align: center; margin-top: 100px;">
			<button id="downloadButton" class="channel-title">点击下载</button>
		</div>
		<!--引入遮蔽层-->
		<!--<div id="Mask" style="">
		</div>-->

		<!--引入遮蔽层-->
		<!--<div id="Mask2" style="">
		</div> -->
	</div>
</body>
<script type="text/javascript" charset="UTF-8" src="//res.cdn.openinstall.io/openinstall.js"></script>
<script type="text/javascript">
	
	$(function() {

//		$("#Mask2").show();
//			
//		//判断是否在微信中打开
//		var ua = navigator.userAgent;
//		var isWeixin = !!/MicroMessenger/i.test(ua);
//		//如果使用的是微信自带浏览器，就打开蒙版
//		if (isWeixin) {
//			document.querySelector('body').addEventListener('touchmove', function(e) {
//				　　e.preventDefault();
//				});
//			var SHOW = 0;
//			document.getElementById('Mask').style.display = ++SHOW % 2 == 1 ? 'block'
//					: 'none';
//		}else{
//			window.location.href='../share/jumpRealShare.html?userId='+$('#userId').val();
//		}
//		
//		$("#Mask2").hide();

		/* $.ajax({
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
		}); */
	});

	function getQueryString(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		if (r != null)
			return unescape(r[2]);
		return null;
	}
	
	window.mobileUtil = (function(win, doc) {
		var UA = navigator.userAgent, isAndroid = /android|adr/gi.test(UA), isIOS = /iphone|ipod|ipad/gi
				.test(UA)
				&& !isAndroid, isBlackBerry = /BlackBerry/i.test(UA), isWindowPhone = /IEMobile/i
				.test(UA), isMobile = isAndroid || isIOS || isBlackBerry
				|| isWindowPhone;
		return {
			isAndroid : isAndroid,
			isIOS : isIOS,
			isMobile : isMobile,
			isWeixin : /MicroMessenger/gi.test(UA),
			isQQ : /QQ/gi.test(UA)
		};
	})(window, document);
	if (mobileUtil.isWeixin && mobileUtil.isIOS) {
		$('#hidediv').show();
		$('#origion').hide();
	}else{
		//openinstall初始化时将与openinstall服务器交互，应尽可能早的调用
	    /*web页面向app传递的json数据(json string/js Object)，应用被拉起或是首次安装时，通过相应的android/ios api可以获取此数据*/
	    var data = OpenInstall.parseUrlParams();//openinstall.js中提供的工具函数，解析url中的所有查询参数
	    new OpenInstall({
	        /*appKey必选参数，openinstall平台为每个应用分配的ID*/
	        appKey : "p3opbw",
	        /*可选参数，自定义android平台的apk下载文件名；个别andriod浏览器下载时，中文文件名显示乱码，请慎用中文文件名！*/
	        //apkFileName : 'com.fm.openinstalldemo-v2.2.0.apk',
	        /*可选参数，是否优先考虑拉起app，以牺牲下载体验为代价*/
	        //preferWakeup:true,
	        /*自定义遮罩的html*/
	        //mask:function(){
	        //  return "<div id='openinstall_shadow' style='position:fixed;left:0;top:0;background:rgba(0,255,0,0.5);filter:alpha(opacity=50);width:100%;height:100%;z-index:10000;'></div>"
	        //},
	        /*openinstall初始化完成的回调函数，可选*/
	        onready : function() {
	            var m = this, button = document.getElementById("downloadButton");
	            button.style.visibility = "visible";

	            /*在app已安装的情况尝试拉起app*/
	            m.schemeWakeup();
	            /*用户点击某个按钮时(假定按钮id为downloadButton)，安装app*/
	            button.onclick = function() {
	                m.wakeupOrInstall();
	                return false;
	            }
	        }
	    }, data);
	}
	
	
	
</script>
</html>

