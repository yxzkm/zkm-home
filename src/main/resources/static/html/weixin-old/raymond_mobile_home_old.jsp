<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=0">
<title>欢迎来到【雷蒙德】的微网站首页</title>
<link rel="stylesheet" href="/css/weixinStyle.css">
<link rel="stylesheet" href="/css/fileupload.css">

<script type="text/javascript">

	function uploadFile() {
		var files = document.getElementById('fileToUpload').files;
		if (files.length === 0) { return; }

		for (var i=0;i<files.length;i++){
			var fd = new FormData();
			fd.append("fileToUpload", files[i]);
			var xhr = new XMLHttpRequest();
			xhr.upload.addEventListener("progress", uploadProgress, false);
			xhr.addEventListener("load", uploadComplete, false);
			xhr.addEventListener("error", uploadFailed, false);
			xhr.addEventListener("abort", uploadCanceled, false);
			xhr.open("POST", "/raymond/uploadFilePost");
			xhr.send(fd);
		}
	}

	function uploadProgress(evt) {
		if (evt.lengthComputable) {
			var percentComplete = Math.round(evt.loaded * 100 / evt.total);
			document.getElementById('progressNumber').innerHTML = percentComplete.toString()+ '%';
		} else {
			document.getElementById('progressNumber').innerHTML = 'unable to compute';
		}
	}

	function uploadComplete(evt) {
		/* This event is raised when the server send back a response */
		var ret = JSON.parse(evt.target.responseText);
		alert(ret);
		//window.location = ret.imgUrl;
	}

	function uploadFailed(evt) {
		alert("There was an error attempting to upload the file.");
	}

	function uploadCanceled(evt) {
		alert("The upload has been canceled by the user or the browser dropped the connection.");
	}
	
	
	function loadImageFile() {
		var files = document.getElementById("fileToUpload").files;
		if (files.length === 0) { return; }
		
		var rFilter = /^(?:image\/bmp|image\/cis\-cod|image\/gif|image\/ief|image\/jpeg|image\/jpeg|image\/jpeg|image\/pipeg|image\/png|image\/svg\+xml|image\/tiff|image\/x\-cmu\-raster|image\/x\-cmx|image\/x\-icon|image\/x\-portable\-anymap|image\/x\-portable\-bitmap|image\/x\-portable\-graymap|image\/x\-portable\-pixmap|image\/x\-rgb|image\/x\-xbitmap|image\/x\-xpixmap|image\/x\-xwindowdump)$/i;
		
		var imgSelect = document.getElementById("imgSelect");
		imgSelect.style.display = "none";
		
		var icount = 0;
		
		for (var i=0;i<files.length;i++){
			var oFile = document.getElementById("fileToUpload").files[i];
			if (!rFilter.test(oFile.type)) { alert("You must select a valid image file!"); return; }

			var fileReader = new FileReader();
			fileReader.onload = function (fileReaderEvent) {
				
				console.log("this.id==="+this.id);
				console.log("fileReaderEvent.target.id: "+fileReaderEvent.target.id);
				
				var imgNode = document.createElement("img");
				imgNode.setAttribute("class", "uploadPreview");
				imgNode.setAttribute("src", fileReaderEvent.target.result);
				
				var boxNode = document.getElementById("imgBox");
				boxNode.appendChild(imgNode);
				
				icount = icount + 1;
				
				if(icount===files.length){
					var up = document.getElementById("up");
					up.style.display = "";
				}
			};
			fileReader.readAsDataURL(oFile);
		}
	}

</script>
</head>
<body>
	<div class="wxapi_container">
		<h3 id="menu-basic">请选择要上传的照片...</h3>
		<div id="imgBox">
			<div id="imgSelect" class="file-uploader-wrap">
				<input id="fileToUpload" class="file-uploader" type="file" name="fileToUpload" onchange="loadImageFile();" multiple="multiple" />
				<div class="file-uploader-wrap-fake">
					<img class="uploadImg" src="/img/wenhao.png" />
				</div>
			</div>
		</div>
		<div id="up" style="display:none;" class="lbox_close wxapi_form">
			<button class="btn btn_primary" type="button" onclick="uploadFile()" >上传...</button> 
			<span class="desc" id="progressNumber"></span>
		</div>
	</div>
</body>
</html>