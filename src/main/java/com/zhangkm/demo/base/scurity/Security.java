package com.zhangkm.demo.base.scurity;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

public class Security {

    private static final String ENCODING = "UTF-8";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    
    public static final String KEY_ALGORITHM = "RSA";  
    public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";  
    public static final String PUBLIC_KEY = "publicKey";  
    public static final String PRIVATE_KEY = "privateKey";  
    public static final int KEY_SIZE = 2048;  

    
    public static Map<String, byte[]> generateKeyBytes() {  
        
        try {  
            KeyPairGenerator keyPairGenerator = KeyPairGenerator  
                    .getInstance(KEY_ALGORITHM);  
            keyPairGenerator.initialize(KEY_SIZE);  
            KeyPair keyPair = keyPairGenerator.generateKeyPair();  
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();  
            Map<String, byte[]> keyMap = new HashMap<String, byte[]>();  
            keyMap.put(PUBLIC_KEY, publicKey.getEncoded());  
            keyMap.put(PRIVATE_KEY, privateKey.getEncoded());  
            return keyMap;  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  
  
    /** 
     * 还原公钥 
     *  
     * @param keyBytes 
     * @return 
     */  
    public static PublicKey restorePublicKey(byte[] keyBytes) {  
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);  
        try {  
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);  
            PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);  
            return publicKey;  
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  
  
    /** 
     * 还原私钥 
     *  
     * @param keyBytes 
     * @return 
     */  
    public static PrivateKey restorePrivateKey(byte[] keyBytes) {  
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(  
                keyBytes);  
        try {  
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);  
            PrivateKey privateKey = factory  
                    .generatePrivate(pkcs8EncodedKeySpec);  
            return privateKey;  
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  
  


    public static byte[] sign256(String data, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, SignatureException,
            UnsupportedEncodingException {

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);

        signature.initSign(privateKey);

        signature.update(data.getBytes(ENCODING));

        return signature.sign();
    }

    public static boolean verify256(String data, byte[] sign,
            PublicKey publicKey) {
        if (data == null || sign == null || publicKey == null) {
            return false;
        }

        try {
            Signature signetcheck = Signature.getInstance(SIGNATURE_ALGORITHM);
            signetcheck.initVerify(publicKey);
            signetcheck.update(data.getBytes("UTF-8"));
            return signetcheck.verify(sign);
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * 二进制数据编码为BASE64字符串
     * 
     * @param data
     * @return
     */
    public static String encodeBase64(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }

    /**
     * BASE64解码
     * 
     * @param bytes
     * @return
     */
    public static byte[] decodeBase64(byte[] bytes) {
        byte[] result = null;
        try {
            result = Base64.decodeBase64(bytes);
        }
        catch (Exception e) {
            return null;
        }
        return result;
    }

    public static void main(String[] args){
        //sign256("zhangkm", );   
    }
}
