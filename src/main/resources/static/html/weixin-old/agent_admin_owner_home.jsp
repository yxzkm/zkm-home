<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>微信第三方托管平台_管理后台</title>
<meta name="viewport"
	content="width=device-width, initial-scale=1, user-scalable=0">
<link rel="stylesheet" href="/css/weixinStyle.css">
</head>
<body>
	<div class="wxapi_container">
		<div class="lbox_close wxapi_form">
			<h3 id="menu-basic">欢迎： ${nickname}</h3>
			<img src="${headimgurl}" width="100" height="100"/>
		</div>
		<div class="lbox_close wxapi_form">
			<h3 id="menu-basic">第三方平台具备的能力</h3>
			<span class="desc">查看公众号资源列表</span>
			<button 
				class="btn btn_primary" 
				onclick="window.location='http://zhangkm.com/weixin/agent/${ownerAppid}/adminFixedMediaImgList'">查看公众号资源列表</button>
		</div>
	</div>
</body>
<script src="http://apps.bdimg.com/libs/jquery/1.8.0/jquery.js"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
</html>
