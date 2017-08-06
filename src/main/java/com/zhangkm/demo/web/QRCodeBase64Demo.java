package com.zhangkm.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/demo/web", produces = "text/html;charset=UTF-8")
public class QRCodeBase64Demo {
//    @RequestMapping(
//            method = RequestMethod.GET, 
//            value="/qrcode")  
//    @ResponseBody
//    public String qrcode() {
//        Hashtable<Object, Object> hints = new Hashtable<Object, Object>();   
//        // 指定纠错等级   
//        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);   
//        // 指定编码格式   
//        try {   
//  
//            BitMatrix byteMatrix;  
//            byteMatrix = new MultiFormatWriter().encode(new String(contents.getBytes("UTF-8"),"iso-8859-1"),BarcodeFormat.QR_CODE, width, height);  
//            ByteArrayOutputStream bao = new ByteArrayOutputStream();  
//            MatrixToImageWriter.writeToStream(byteMatrix, "png", bao);  
//            System.out.println(bao.toByteArray());  
//            String ok = Base64Code(bao.toByteArray());  
//            System.out.println("-----------------------------------------------------------");  
//            System.out.println(ok);  
//            System.out.println("--------------------------------------------------------------");  
//            createImage(ok);  
//              
//        } catch (Exception e) {   
//            e.printStackTrace();   
//  
//        }   
//    }
}
