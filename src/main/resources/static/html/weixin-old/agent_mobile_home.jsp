<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, user-scalable=0">
<title>欢迎来到：1111 ${nickname}</title>
<link rel="stylesheet" href="/css/weixinStyle.css">

<script type="text/javascript">
	function fileSelected() {
		var file = document.getElementById('fileToUpload').files[0];
		if (file) {
			var fileSize = 0;
			if (file.size > 1024 * 1024)
				fileSize = (Math.round(file.size * 100 / (1024 * 1024)) / 100)
						.toString()
						+ 'MB';
			else
				fileSize = (Math.round(file.size * 100 / 1024) / 100)
						.toString()
						+ 'KB';

			document.getElementById('fileName').innerHTML = 'Name: '
					+ file.name;
			document.getElementById('fileSize').innerHTML = 'Size: ' + fileSize;
			document.getElementById('fileType').innerHTML = 'Type: '
					+ file.type;
		}
	}
	
	function uploadFile_DO_NOT_WORK() {  //使用jquery的post方法不行，后台action没有响应。
		var fd = new FormData();
		fd.append("fileToUpload",
				document.getElementById('fileToUpload').files[0]);

		$.post('/weixin/agent/${appid}/uploadFilePost', fd, function(res) {
			alert("ok!!!");
		}, 'json');
	}

	function uploadFile() {
		var fd = new FormData();
		fd.append("fileToUpload",
				document.getElementById('fileToUpload').files[0]);
		var xhr = new XMLHttpRequest();
		xhr.upload.addEventListener("progress", uploadProgress, false);
		xhr.addEventListener("load", uploadComplete, false);
		xhr.addEventListener("error", uploadFailed, false);
		xhr.addEventListener("abort", uploadCanceled, false);
		xhr.open("POST", "/weixin/agent/${appid}/uploadFilePost");
		xhr.send(fd);
	}

	function uploadProgress(evt) {
		if (evt.lengthComputable) {
			var percentComplete = Math.round(evt.loaded * 100 / evt.total);
			document.getElementById('progressNumber').innerHTML = percentComplete
					.toString()
					+ '%';
		} else {
			document.getElementById('progressNumber').innerHTML = 'unable to compute';
		}
	}

	function uploadComplete(evt) {
		/* This event is raised when the server send back a response */
		var ret = JSON.parse(evt.target.responseText);
		window.location = ret.imgUrl;
	}

	function uploadFailed(evt) {
		alert("There was an error attempting to upload the file.");
	}

	function uploadCanceled(evt) {
		alert("The upload has been canceled by the user or the browser dropped the connection.");
	}
</script>
</head>
<body>

	<div class="wxapi_container">
		<div class="lbox_close wxapi_form">
			<h3 id="menu-basic">这里是 【${nickname}】 在第三方托管平台的首页</h3>
			<img src="${headimgurl}" alt="${nickname}" width="100" height="100" />
		</div>
		<div class="lbox_close wxapi_form">

			<h3 id="menu-image">素材管理</h3>
			<span class="desc">通过上传图片创建一个永久图文素材，包含：选取（拍摄）照片、上传至微信服务器、拉回到应用服务器、上传至永久图片素材库、创建图文并上传至永久图文素材库。</span>
			<button class="btn btn_primary" id="uploadImageFullVersion">uploadImageFullVersion</button>

			<h3 id="menu-basic">微信网页授权</h3>
			<span class="desc">静默授权并自动跳转到回调页</span>
			<button class="btn btn_primary"
				onclick="window.location='http://zhangkm.com/weixin/agent/${appid}/userinfo?state=0'">scope:
				snsapi_base</button>
			<span class="desc">用户需手动同意，无须关注，即可获取该用户基本信息</span>
			<button class="btn btn_primary"
				onclick="window.location='http://zhangkm.com/weixin/agent/${appid}/userinfo?state=1'">scope:
				snsapi_userinfo</button>

			<h3 id="menu-basic">基础接口</h3>
			<span class="desc">判断当前客户端是否支持指定JS接口</span>
			<button class="btn btn_primary" id="checkJsApi">checkJsApi</button>

			<h3 id="menu-image">图像接口</h3>
			<span class="desc">拍照或从手机相册中选图接口</span>
			<button class="btn btn_primary" id="chooseImage">chooseImage</button>
			<span class="desc">预览图片接口</span>
			<button class="btn btn_primary" id="previewImage">previewImage</button>
			<span class="desc">上传图片接口</span>
			<button class="btn btn_primary" id="uploadImage">uploadImage</button>
			<span class="desc">下载图片接口</span>
			<button class="btn btn_primary" id="downloadImage">downloadImage</button>

			<h3 id="menu-device">设备信息接口</h3>
			<span class="desc">获取网络状态接口</span>
			<button class="btn btn_primary" id="getNetworkType">getNetworkType</button>

		</div>
		<div class="lbox_close wxapi_form">
			<h3 id="menu-image">图片上传（非微信渠道）</h3>
			<span class="desc">使用fileupload方式，直接将手机本地文件上传到应用服务器。请选择一个文件...</span>
			<input class="btn btn_primary" type="file" name="fileToUpload" 
				id="fileToUpload" onchange="fileSelected();" /> 
			<span class="desc" id="fileName"></span> 
			<span class="desc" id="fileSize"></span> 
			<span class="desc" id="fileType"></span> 
			<button class="btn btn_primary" type="button" onclick="uploadFile()" >上传...</button> 
			<span class="desc" id="progressNumber"></span>
		</div>
	</div>

</body>
<script src="http://apps.bdimg.com/libs/jquery/1.8.0/jquery.js"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script src="/js/demo.js"></script>
<script>
	var appid = '${appid}';

	var signUrl = 'http://zhangkm.com/weixin/agent/' + appid + '/sign';
	var dragFileBackFromWeixinUrl = 'http://zhangkm.com/weixin/agent/' + appid
			+ '/dragFileBackFromWeixin';
	var addFixedNewsMediaUrl = 'http://zhangkm.com/weixin/agent/' + appid
			+ '/addFixedNewsMedia';

	function getsignature() {
		var temp = {};
		temp.server = signUrl;
		temp.param = {
			'url' : window.location.href,
			'appid' : appid
		};
		$.post(temp.server, temp.param, function(res) {
			temp.appid = res['appid'];
			temp.timestamp = res['timestamp'];
			temp.nonceStr = res['nonceStr'];
			temp.signature = res['signature'];
			startweixin(temp);
		}, 'json');
	}

	function startweixin(temp) {
		wx.config({
			debug : false,
			appId : temp.appid,
			timestamp : temp.timestamp,
			nonceStr : temp.nonceStr,
			signature : temp.signature,
			jsApiList : [ 'checkJsApi', 'chooseImage', 'previewImage',
					'uploadImage', 'downloadImage', 'onMenuShareAppMessage',
					'getNetworkType' ]
		});

	}
	getsignature();
</script>

</html>