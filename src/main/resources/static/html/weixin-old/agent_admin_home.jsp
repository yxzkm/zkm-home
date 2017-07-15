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
	</div>
	<div class="lbox_close wxapi_form">
		<h3 id="menu-basic">微信第三方托管授权</h3>
		<span class="desc">第三方运营商引导用户进入微信授权页面</span>
		<button 
			class="btn btn_primary" 
			onclick="window.location='http://zhangkm.com/weixin/agent/adminAuth'">授权给第三方运营商</button>
	</div>
	<div class="aaa" style="display:none;">
		<a href='http://sohu.com'>
			<h3 id="menu-basic"></h3>
			<img src=""  width="100" height="100"/>
		</a>
	</div>
</body>
<script src="http://apps.bdimg.com/libs/jquery/1.8.0/jquery.js"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script>

var getOwnerInfoListUrl = 'http://zhangkm.com/weixin/agent/getOwnerInfoList';

function getOwnerInfoList() {
	$.get(getOwnerInfoListUrl,'', function(res) {
		for (var i = 0; i < res.length; i++) {
			//alert(res[i].nickname + '  ' + res[i].headimgurl);
			var item = $('div.aaa').clone().css('display','');
			item.attr('class','lbox_close wxapi_form');
			item.children('a').children('h3').text(res[i].nickname);
			item.children('a').children('img').attr('src',res[i].headimgurl);
			item.children('a').attr('href','http://zhangkm.com/weixin/agent/'+res[i].ownerAppid +'/adminHome');
			$('.wxapi_container').append(item);
		}
	}, 'json');
}
getOwnerInfoList();
</script>
</html>
