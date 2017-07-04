var filesAmount = 0;   // 等待上传的文件数量
var fileUploadCompleteCounter = 0;  // 文件上传完毕计数器
var batchCode = "";

//从本地加载图片文件，并预览
function loadImageFile() {
	
	var files = document.getElementById("fileToUpload").files;
	if (files.length === 0) { return; }
	
	var rFilter = /^(?:image\/bmp|image\/cis\-cod|image\/gif|image\/ief|image\/jpeg|image\/jpeg|image\/jpeg|image\/pipeg|image\/png|image\/svg\+xml|image\/tiff|image\/x\-cmu\-raster|image\/x\-cmx|image\/x\-icon|image\/x\-portable\-anymap|image\/x\-portable\-bitmap|image\/x\-portable\-graymap|image\/x\-portable\-pixmap|image\/x\-rgb|image\/x\-xbitmap|image\/x\-xpixmap|image\/x\-xwindowdump)$/i;
	
	// 首先把文件选择按钮以及问号logo图片隐藏起来
	var picSelectBox = document.getElementById("picSelectBox");
	picSelectBox.style.display = "none";
		
	// 然后把图片预览区域显示出来
	var picPreviewBox = document.getElementById("picPreviewBox");
	picPreviewBox.setAttribute("class", "vBox");

	// 循环加载用户选择的文件
	var icount = 0;
	for (var i=0;i<files.length;i++){
		var oFile = document.getElementById("fileToUpload").files[i];
		console.log(oFile);
		if (!rFilter.test(oFile.type)) { alert("You must select a valid image file!"); return; }

		var fileReader = new FileReader();

		fileReader.onload = function(file){
			return function (fileReaderEvent) {
						
				var imgBoxNode = document.createElement("div");
				imgBoxNode.setAttribute("id", "picPreviewImgBox_"+file.name);
				imgBoxNode.setAttribute("class", "picPreviewImgBox");
	
				var imgNode = document.createElement("img");
				imgNode.setAttribute("id", "picPreviewImg_"+file.name);
				imgNode.setAttribute("class", "picPreviewImg");
				imgNode.setAttribute("src", fileReaderEvent.target.result);
	
				imgBoxNode.appendChild(imgNode);
	
				var picPreviewDiv = document.getElementById("picPreviewDiv");
				//picPreviewDiv.appendChild(imgBoxNode);
				picPreviewDiv.insertBefore(imgBoxNode,picPreviewDiv.childNodes[0]);
							
				icount = icount + 1;
							
				if(icount===files.length){
					var picUploadButtonDiv = document.getElementById("picUploadButtonDiv");
					picUploadButtonDiv.setAttribute("class", "");
				}
			};
			
		}(oFile);

		fileReader.readAsDataURL(oFile);
	}
}

//图片上传
function uploadFile() {
	var files = document.getElementById('fileToUpload').files;
	if (files.length === 0) { return; }
	
	filesAmount = files.length;

	//获取批次代码
	batchCode = getBatchCodeAjax(filesAmount);
	if("-1"==batchCode){
		alert("发生错误");
	}
	
	// 获取热门标签数据
	getHotLabelListAjax();
	
	for (var i=0;i<files.length;i++){
		var fd = new FormData();
		fd.append("fileToUpload", files[i]);
		fd.append("batchCode", batchCode);
		fd.append("orderNum", i);
		var xhr = new XMLHttpRequest();
		xhr.upload.addEventListener("progress", uploadProgress, false);
		xhr.addEventListener("load", uploadComplete, false);
		xhr.addEventListener("error", uploadFailed, false);
		xhr.addEventListener("abort", uploadCanceled, false);
		xhr.open("POST", "/fileUpload/postFileToServer");
		xhr.send(fd);
	}
}

//图片上传结束的事件
function uploadComplete(evt) {
	fileUploadCompleteCounter++;
	var ret = JSON.parse(evt.target.responseText);
	var imgNode = document.getElementById("picPreviewImg_"+ret.dataBody.orgFileName);
	imgNode.setAttribute("class", "picPreviewImgUploadOK");
	
	console.log("图片上传完成： "+ret.dataBody.orgFileName);
	if(fileUploadCompleteCounter===filesAmount){
	}
}

//图片上传过程的百分比显示
function uploadProgress(evt) {
	
	if (evt.lengthComputable) {
		var percentComplete = Math.round(evt.loaded * 100 / evt.total);
		document.getElementById('progressNumber').innerHTML = percentComplete.toString()+ '%';
		console.log("图片上传中。。。： "+percentComplete.toString()+ '%');
	} else {
		document.getElementById('progressNumber').innerHTML = 'unable to compute';
	}
}


//获取批处理代码
function getBatchCodeAjax(filesAmount){
	var batchCodeFromServer = "";
	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange=function(){
		if (xhr.readyState==4 && xhr.status==200){
			var json = JSON.parse(xhr.responseText);
			if("0"==json.code){
				console.log("json.code:"+json.code);
				console.log("OK json.dataBody.batchCode:"+json.dataBody.batchCode);
				batchCodeFromServer = json.dataBody.batchCode; 
			}else{
				console.log("error json.code:"+json.code);
				batchCodeFromServer = "-1";
			}
		}
	}
	//注意：同步请求
	xhr.open("GET", "/fileUpload/getBatchCode?amount="+filesAmount,false);  
	xhr.send(null);  
	return batchCodeFromServer;
}

//获取最热门标签列表
function getHotLabelListAjax(){
	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange=function(){
		if (xhr.readyState==4 && xhr.status==200){
			var json = JSON.parse(xhr.responseText);
			for (var i=0;i<json.length;i++){
				var mytemp = document.getElementById("mytemp").innerHTML;
				var linode = parseToDOM(mytemp);
				linode.innerHTML = json[i].member;
				linode.setAttribute("issubmit", "no");
				var picLabelNominateUL = document.getElementById("picLabelNominateUL");
				picLabelNominateUL.appendChild(linode);
			}
			//显示标签列表
			showLabelList();
		}
	}
	xhr.open("GET", "/fileUpload/getHotLabels",true);  
	xhr.send(null);  
}

//最终提交标签，后台要上传到七牛，因此比较慢
function submitLabelsAjax(){
	var picLabelButtonDiv = document.getElementById("picLabelButtonDiv");
	
	var labelNames = "";
	var picLabelSubmitLis = document.querySelectorAll("#picLabelSubmitUL>li"); 
	for (var i=0;i<picLabelSubmitLis.length;i++){
		labelNames = labelNames + picLabelSubmitLis[i].innerHTML + ","
	}

	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange=function(){
		if (xhr.readyState==4 && xhr.status==200){
			picLabelButtonDiv.innerHTML = "OK,照片处理完毕！\n"+xhr.responseText;
		}
	}
	xhr.open("POST","/fileUpload/submitLabels",true);
	xhr.send("batchCode="+batchCode+"&labelNames="+labelNames);
	
	picLabelButtonDiv.innerHTML = "照片正在处理中，请等待...";
}


//图片上传失败
function uploadFailed(evt) {
	alert("There was an error attempting to upload the file.");
}

// 图片上传取消
function uploadCanceled(evt) {
	alert("The upload has been canceled by the user or the browser dropped the connection.");
}
	

// 显示标签列表
function showLabelList() {
	var picUploadButtonDiv = document.getElementById("picUploadButtonDiv");
	picUploadButtonDiv.setAttribute("class", "hidden");
	var picLabelBox = document.getElementById("picLabelBox");
	picLabelBox.setAttribute("class", "vBox");
}

// 将推荐标签移动到待提交标签区域，或者将待提交标签从列表中删除
function moveLabel(thisLabel) {
	var picLabelNominateUL = document.getElementById("picLabelNominateUL");
	var picLabelSubmitUL = document.getElementById("picLabelSubmitUL");

	var isSubmit = thisLabel.getAttribute("issubmit");
	if("yes"==isSubmit){
		thisLabel.setAttribute("issubmit", "no");
		picLabelSubmitUL.removeChild(thisLabel);
	}else{
		thisLabel.setAttribute("issubmit", "yes");
		picLabelNominateUL.removeChild(thisLabel);
		picLabelSubmitUL.appendChild(thisLabel);
	}

	if(picLabelSubmitUL.hasChildNodes()){
		var picLabelSubmitBox = document.getElementById("picLabelSubmitBox");
		picLabelSubmitBox.setAttribute("class", "");
	}

}

function showLabelInput(){
	var picLabelInputDiv = document.getElementById("picLabelInputDiv");
	picLabelInputDiv.setAttribute("class", "");

	//console.log(picLabelButtonDiv);
}

var keyPressEnterFlag = 0;  //键盘输入回车标志位

// 响应键盘事件（keypress）
function checkInput() {
	// 获取用户敲击键盘的键的代码
	var x = event.which;
	// 如果是空格键或回车键
	if(x===32 || x===13){
		//将回车标志位置为1
		keyPressEnterFlag = 1;
		
		//获取当前用户在input中的全部输入
		var labelInput = document.getElementById("labelInput");
		var value = labelInput.value;
		
		if(value.length>4 || value=="" || value==" "){
			//如果长度大于4或input为空或空格,则什么也不做，交给keyup事件去处理
			return;
		}			
		
		var picLabelSubmitUL = document.getElementById("picLabelSubmitUL");
		if(picLabelSubmitUL.hasChildNodes()){
			if(picLabelSubmitUL.childNodes.length >= 5){
				console.log("too much label!");
				return;
			}
			for (var i=0;i<picLabelSubmitUL.childNodes.length;i++){
				var cnode = picLabelSubmitUL.childNodes.item(i);
				if("LI"===cnode.nodeName.toUpperCase()){
					var labelValue = cnode.innerHTML;
					if(value.toUpperCase()==labelValue.toUpperCase()){
						console.log("dup!");
						return;
					}
				}
			}

		}

		var mytemp = document.getElementById("mytemp").innerHTML;
		var linode = parseToDOM(mytemp);
		linode.innerHTML = value;
		picLabelSubmitUL.appendChild(linode);
			
		if(picLabelSubmitUL.hasChildNodes()){
			var picLabelSubmitBox = document.getElementById("picLabelSubmitBox");
			picLabelSubmitBox.setAttribute("class", "");
		}
	}
}

//响应键盘事件（keyup）
function keyup() {
	if(keyPressEnterFlag==1){
		labelInput.value = "";
		keyPressEnterFlag=0;
	}
}

// 将字符串转换成为dom对象
function parseToDOM(str){
   var div = document.createElement("div");
   div.innerHTML = str;	
   //console.log(div);
   return div.childNodes[1]; // 注意：这里必须是[1]，而不是[0]
}

