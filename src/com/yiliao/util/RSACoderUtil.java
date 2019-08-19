package com.yiliao.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;

public class RSACoderUtil {

	public static final String CHARSET = "UTF-8";
	public static final String RSA_ALGORITHM = "RSA";

	public static Map<String, String> createKeys(int keySize) {
		// 为RSA算法创建一个KeyPairGenerator对象
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("No such algorithm-->[" + RSA_ALGORITHM + "]");
		}

		// 初始化KeyPairGenerator对象,密钥长度
		kpg.initialize(keySize);
		// 生成密匙对
		KeyPair keyPair = kpg.generateKeyPair();
		// 得到公钥
		Key publicKey = keyPair.getPublic();
		String publicKeyStr =Base64.getEncoder().encodeToString(publicKey.getEncoded());
//		String publicKeyStr = Base64.encodeBase64URLSafeString(publicKey.getEncoded());
//		String publicKeyStr = new String(publicKey.getEncoded());
		// 得到私钥
		Key privateKey = keyPair.getPrivate();
//		String privateKeyStr = Base64.encodeBase64URLSafeString(privateKey.getEncoded());
		String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		
		Map<String, String> keyPairMap = new HashMap<String, String>();
		keyPairMap.put("publicKey", publicKeyStr);
		keyPairMap.put("privateKey", privateKeyStr);

		return keyPairMap;
	}

	/**
	 * 得到公钥
	 * 
	 * @param publicKey 密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 通过X509编码的Key指令获得公钥对象
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
//		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
		RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
		return key;
	}

	/**
	 * 得到私钥
	 * 
	 * @param privateKey 密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static RSAPrivateKey getPrivateKey(String privateKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 通过PKCS#8编码的Key指令获得私钥对象
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
		RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
		return key;
	}

	/**
	 * 公钥加密
	 * 
	 * @param data
	 * @param publicKey
	 * @return
	 */
	public static String publicEncrypt(String data, RSAPublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET),
					publicKey.getModulus().bitLength()));
		} catch (Exception e) {
			throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
		}
	}

	/**
	 * 私钥解密
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 */

	public static JSONObject privateDecrypt(HttpServletRequest req) {
		try {

			String cipher_param = "";
			
			BufferedReader br = req.getReader();
			String str = "";
			while ((str = br.readLine()) != null) {
				cipher_param += str;
			}
			String[] str1 = cipher_param.split("=");
			
			cipher_param =URLDecoder.decode(str1[1],"UTF-8") ;

			if (StringUtils.isBlank(cipher_param)) {
				return null;
			}

			RSAPrivateKey privateKey = RSACoderUtil.getPrivateKey(SystemConfig.getValue("encrypt_private"));

			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			return JSONObject.fromObject(new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE,
					java.util.Base64.getDecoder().decode(cipher_param), privateKey.getModulus().bitLength()), CHARSET));
		} catch (Exception e) {
			throw new RuntimeException("解密数据遇到异常", e);
		}
	}

	/**
	 * 私钥加密
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 */

	public static String privateEncrypt(String data, RSAPrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			return java.util.Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET),
					privateKey.getModulus().bitLength()));
		} catch (Exception e) {
			throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
		}
	}

	/**
	 * 公钥解密
	 * 
	 * @param data
	 * @param publicKey
	 * @return
	 */

	public static String publicDecrypt(String data, RSAPublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.getDecoder().decode(data),
					publicKey.getModulus().bitLength()), CHARSET);
		} catch (Exception e) {
			throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
		}
	}

	public static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) {
		int maxBlock = 0;
		if (opmode == Cipher.DECRYPT_MODE) {
			maxBlock = keySize / 8;
		} else {
			maxBlock = keySize / 8 - 11;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] buff;
		int i = 0;
		try {
			while (datas.length > offSet) {
				if (datas.length - offSet > maxBlock) {
					buff = cipher.doFinal(datas, offSet, maxBlock);
				} else {
					buff = cipher.doFinal(datas, offSet, datas.length - offSet);
				}
				out.write(buff, 0, buff.length);
				i++;
				offSet = i * maxBlock;
			}
		} catch (Exception e) {
			throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
		}
		byte[] resultDatas = out.toByteArray();
		IOUtils.closeQuietly(out);
		return resultDatas;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Map<String, String> keyMap = RSACoderUtil.createKeys(1024);
        String  publicKey = keyMap.get("publicKey");
        String  privateKey = keyMap.get("privateKey");
//		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1B5SkHGaNbhyb8viONwNWUEvIf/eEmrzBv3R35e6J0k5POTgePNcNJyZ7IXoXKoVIsNJgaxUDG5c/3mdhnsfiNxP6g4CJt4WZxDkEti/VoWODNhp/TBI4wcwUpistkETkAMA4ldqk2IwOQCJcwYy9lnNP+0vroKTUj07IjsBjzwIDAQAB";
//		String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBALUHlKQcZo1uHJvy+I43A1ZQS8h/94SavMG/dHfl7onSTk85OB481w0nJnshehcqhUiw0mBrFQMblz/eZ2Gex+I3E/qDgIm3hZnEOQS2L9WhY4M2Gn9MEjjBzBSmKy2QROQAwDiV2qTYjA5AIlzBjL2Wc0/7S+ugpNSPTsiOwGPPAgMBAAECgYEAln4w0aVSk+ygF/0LKJ8PRSgHeCz5xeElXiQdXLdncGfG9hFpV6nYVXTghqgz6SJOHznwCnu6whSjzlLDuuHW5AxK2kRWCC4/xXJHMAmDVBP/tj5o3QUHZupaIIv+oFz9ioJktMOyRqP3IBcm5AVyW2QpjrHrbFn2nfULvdojQNECQQD6Qncfx6N85Jd+VqLIb9ClS46VHmrgnF3B2Y9ky9cEaXNC4c0HOeRhp/32sVbbAsS/xinOcfZ9X7gmPhqhUqm1AkEAuS6YBWWr+NPB4WImm5cAOStoUA6Hqbx88ySWMttk44vwZiLzjquHfrrFMuH1N90vD27zp/pqZP4CVWqLX4A58wJBAMD7CfNGtmpzt+KjmiloGAOu7QdOimokCiSWyP2YNnlfe5NCniEJYnRv3+YgwHJogyHDBK/+r23kpxNOZqiqHtUCQG9K1Gxx9U/fHCdzUJazwpuZPeDXSqs1L7cFJwu7lT+4C6uml9AHwK/mWbvB1xlS4gSZfvBWbHjzu71QAiFMNAcCQQDRWOndDLyzVFnV1e77IUbN2FJ7D7RY53i7jyxMLUnMlF++oPlu31+VKXbGCjopitb1HK0J4HhWeZb0Vo4IsHbt";
		System.out.println("公钥: \n\r" + publicKey);
		System.out.println("私钥： \n\r" + privateKey);
//
//		System.out.println("公钥加密——私钥解密");
//
		JSONObject json = new JSONObject();
		json.put("userId", "653");

		System.out.println("\r明文：\r\n" + json.toString());
		System.out.println("\r明文大小：\r\n" + json.toString().getBytes().length);
		String encodedData = RSACoderUtil.publicEncrypt(json.toString(), RSACoderUtil.getPublicKey(publicKey));
		System.out.println("密文：\r\n" + encodedData);
		
		
		RSAPrivateKey privateKey1 = RSACoderUtil.getPrivateKey(privateKey);

		Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, privateKey1);
		
		String string = new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE,Base64.getDecoder().decode(encodedData), privateKey1.getModulus().bitLength()), CHARSET);
		
//        String decodedData = RSACoderUtil.privateDecrypt(encodedData, privateKey);
        System.out.println("解密后文字: \r\n" + string);
		
		
	 
		
		
//		String string = new String(bytes);
//		
//		System.out.println(string);

	}

}
