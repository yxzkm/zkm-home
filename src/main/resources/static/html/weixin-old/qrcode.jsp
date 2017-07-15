<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>请关注微信公众号</title>
<meta name="viewport"
	content="width=device-width, initial-scale=1, user-scalable=0">
<link rel="stylesheet" href="/css/weixinStyle.css">
</head>
<body>
	<div class="wxapi_container">
		<div class="lbox_close wxapi_form">
			<img src="https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${ticket}" 
				width="100%" />
		</div>
	</div>
</body>
</html>
