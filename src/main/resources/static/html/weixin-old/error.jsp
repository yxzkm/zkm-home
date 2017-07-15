<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>${errorTitle}</title>
<meta name="viewport"
	content="width=device-width, initial-scale=1, user-scalable=0">
<link rel="stylesheet" href="/css/weixinStyle.css">
</head>
<body>
	<div class="wxapi_container">
		<div class="lbox_close wxapi_form">
			<img src="/img/error.png"  alt="" width="100" height="100"/>
			<h3 id="menu-basic">${errorTitle}</h3>
			<span class="desc">${errorDetail}</span>
			<button 
				class="btn btn_primary" 
				onclick="window.close()">关闭</button>
		</div>
	</div>
</body>
</html>
