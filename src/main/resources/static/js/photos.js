(function() {window.loadImage = {
	
	load: function(json){
		var picPreviewDiv = document.getElementById("picPreviewDiv");
		for (var i=0;i<json.length;i++){
			console.log("json data:"+json[i].member);
			console.log("json data:"+json[i].score);

			var imgBoxNode = document.createElement("div");
			imgBoxNode.setAttribute("id", "picPreviewImgBox_"+i);
			imgBoxNode.setAttribute("class", "picPreviewImgBox");

			var imgNode = document.createElement("img");
			imgNode.setAttribute("id", "picPreviewImg_"+i);
			imgNode.setAttribute("class", "picPreviewImg");
			imgNode.setAttribute("src", "http://pic.zhangkm.com/"+json[i].member+"_thumb.jpg");
			imgBoxNode.appendChild(imgNode);
			
			picPreviewDiv.insertBefore(imgBoxNode, picPreviewDiv.childNodes[0]);
		}
		var picUploadButtonDiv = document.getElementById("picUploadButtonDiv");
		picUploadButtonDiv.setAttribute("class", "");
	},
	
	getImageData: function(){
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange=function(){
			if (xhr.readyState==4 && xhr.status==200){
				var json = JSON.parse(xhr.responseText);
				loadImage.load(json);
			}
		}
		xhr.open("GET", "/fileUpload/getLatestTop9Photos",true);  
		xhr.send(null);
	}
}})();

function dothis(){
	loadImage.getImageData();
}
