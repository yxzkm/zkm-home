<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>微信后台API接口测试</title>
<meta name="viewport"
	content="width=device-width, initial-scale=1, user-scalable=0">
<link rel="stylesheet" href="/css/weixinStyle.css">
</head>
<body>
	<div class="wxapi_container">
		<div class="lbox_close wxapi_form">
			<h3 id="menu-basic">微信网页授权</h3>
			<span class="desc">静默授权并自动跳转到回调页</span>
			<button 
				class="btn btn_primary" 
				onclick="window.location='http://zhangkm.com/weixin/biz/userinfo?state=0'">scope: snsapi_base</button>
			<span class="desc">用户需手动同意，无须关注，即可获取该用户基本信息</span>
			<button 
				class="btn btn_primary" 
				onclick="window.location='http://zhangkm.com/weixin/biz/userinfo?state=1'">scope: snsapi_userinfo</button>
		</div>
	</div>
</body>
<script src="http://apps.bdimg.com/libs/jquery/1.8.0/jquery.js"></script>
</html>
