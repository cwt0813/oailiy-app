package com.yiliao.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.yiliao.util.Utilities.MD5;

public class PayUtil {
	
	private static Logger logger = LoggerFactory.getLogger(PayUtil.class);
	
	/**
	 * 支付宝支付
	* @param orderNo 订单号
	 * @param payAmount 支付金额
	 * @param projectName 项目名称
	 * @param alipay_appid 支付宝appId
	 * @param alipay_private_key 支付宝秘钥
	 * @param alipay_public_key 支付宝公钥
	 * @return
	 * @throws Exception
	 */
	public static String alipayCreateOrder(String orderNo,BigDecimal payAmount,String projectName,String alipay_appid,String alipay_private_key,String alipay_public_key) throws Exception {
        
		//实例化客户端
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
				alipay_appid.trim(),
				alipay_private_key.trim(), "json", 
				"utf-8", 
				alipay_public_key.trim(),
				"RSA2");
		//实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		//SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setBody(SystemConfig.getValue("projectName")+"-"+projectName);
		model.setSubject(projectName);
		model.setOutTradeNo(orderNo);
		model.setTimeoutExpress("30m");
		model.setTotalAmount(payAmount.toString());
		model.setProductCode("QUICK_MSECURITY_PAY");
		request.setBizModel(model);
		request.setNotifyUrl(SystemConfig.getValue("alipayNotifyUrl"));
		try {
		        //这里和普通的接口调用不同，使用的是sdkExecute
		        AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
		        
		        return response.getBody();//就是orderString 可以直接给客户端请求，无需再做处理。
		    } catch (AlipayApiException e) {
		        e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 微信支付
	 * @param body  描述类容
	 * @param orderNo 订单号
	 * @param money 支付金额
	 * @return
	 */
	public static Map<String, String> wxPay(String body,String orderNo,Integer money,String appId,String mchId,String key) {
		
		WXPayConfigImpl config;
		try {
			config = WXPayConfigImpl.getInstance(appId,mchId,key);
			WXPay wxpay= new WXPay(config);
			
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("body", SystemConfig.getValue("projectName")+"-"+body);
			data.put("out_trade_no", orderNo);
			data.put("fee_type", "CNY");
			data.put("total_fee", money.toString());
			data.put("spbill_create_ip", SystemConfig.getValue("spbill_create_ip"));
			data.put("notify_url", SystemConfig.getValue("weixinNotifyUrl"));
			data.put("trade_type", "APP");
			
			Map<String, String> r = wxpay.unifiedOrder(data);
			
			System.out.println(r);
			
			if("SUCCESS".equals(r.get("result_code")) && "OK".equals(r.get("return_msg"))){
				Map<String, String> map = new HashMap<String, String>();
				map.put("appid", config.getAppID());
				map.put("partnerid", config.getMchID());
				map.put("prepayid", r.get("prepay_id"));
				map.put("package", "Sign=WXPay");
				map.put("noncestr", WXPayUtil.generateNonceStr());
				map.put("timestamp", (System.currentTimeMillis()/1000)+"");
				map.put("sign", WXPayUtil.generateSignature(map, config.getKey()));
				return  map;
			}else{
				return new HashMap<String, String>();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new HashMap<String, String>();
	}
	
	/**
	 * 闪电支付
	 * @param appid
	 * @param orderNo
	 * @param tradeName
	 * @param payCode
	 * @param orderAmount
	 * @param orderUid
	 * @param key
	 * @return
	 */
	public static Map<String, String> sdpay(String appid, String orderNo, String tradeName, String payCode, String orderAmount, String orderUid, String key) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("app_id", appid);
			map.put("order_no", orderNo);
			map.put("trade_name", tradeName);
			map.put("pay_type", payCode);
			map.put("order_amount", orderAmount);
			map.put("order_uid", orderUid);

			StringBuilder sb = new StringBuilder();
			sb.append("app_id").append("=").append(appid).append("&");
			sb.append("order_no").append("=").append(orderNo).append("&");
			sb.append("trade_name").append("=").append(tradeName).append("&");
			sb.append("pay_type").append("=").append(payCode).append("&");
			sb.append("order_amount").append("=").append(orderAmount).append("&");
			sb.append("order_uid").append("=").append(orderUid).append("&");

			sb.append(key);

			String sign = MD5.stringToMD5(sb.toString());
			
			logger.info("sdpay, orderNo={}, sign={}", orderNo, sign);

			map.put("sign", sign);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("sdpay error, e={}", e.getMessage());
		}
		return new HashMap<String, String>();
	}
	
	/**
	 * 天玑支付
	 * @param code
	 * @param key
	 * @param ip
	 * @param notifyUrl
	 * @param orderNo
	 * @param tradeName
	 * @param orderAmount
	 * @param payCode
	 * @return
	 */
	public static Map<String, String> tjpay(String code, String key, String ip, String notifyUrl, String orderNo, String tradeName, String orderAmount, String payCode) {
		try {
			SortedMap<String, String> smap = new TreeMap<>();
			smap.put("notifyUrl", notifyUrl);
			smap.put("outOrderNo", orderNo);
			smap.put("goodsClauses", tradeName);
			smap.put("tradeAmount", orderAmount);
			smap.put("code", code);
			smap.put("payCode", payCode);
			
			StringBuilder sb = new StringBuilder();
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			
			sb.append("key=");
			sb.append(key);

			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("tjpay, orderNo={}, sign={}", orderNo, sign);

			Map<String, String> map = new HashMap<>();
			map.putAll(smap);
			map.put("sign", sign);
			map.put("ip", ip);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("tjpay error, e={}", e.getMessage());
		}
		return new HashMap<String, String>();
	}
	
	/**
	 * 德汇支付
	 */
	public static Map<String, String> dhpay(String payMemberid, String orderid, String applydate, String bankcode, String notifyurl, String callbackurl, String amount, String productname, String key, String gateway) {
		try {
			SortedMap<String, String> smap = new TreeMap<>();
			smap.put("pay_memberid", payMemberid);
			smap.put("pay_orderid", orderid);
			smap.put("pay_applydate", applydate);
			smap.put("pay_bankcode", bankcode);
			smap.put("pay_notifyurl", notifyurl);
			smap.put("pay_callbackurl", callbackurl);
			smap.put("pay_amount", amount);
			
			StringBuilder sb = new StringBuilder();
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			
			sb.append("key=");
			sb.append(key);

			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("dhpay, orderid={}, sign={}", orderid, sign);

			Map<String, String> map = new HashMap<>();
			map.putAll(smap);
			map.put("pay_md5sign", sign);
			map.put("pay_productname", productname);
			map.put("gateway", gateway);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("tjpay error, e={}", e.getMessage());
		}
		return new HashMap<String, String>();
	}
	
	/**
	 * 云鼎支付
	 */
	public static Map<String, String> ydpay(String payMemberid, String orderid, String applydate, String bankcode, String notifyurl, String callbackurl, String amount, String productname, String key, String gateway) {
		try {
			SortedMap<String, String> smap = new TreeMap<>();
			smap.put("pay_memberid", payMemberid);
			smap.put("pay_orderid", orderid);
			smap.put("pay_applydate", applydate);
			smap.put("pay_bankcode", bankcode);
			smap.put("pay_notifyurl", notifyurl);
			smap.put("pay_callbackurl", callbackurl);
			smap.put("pay_amount", amount);
			
			StringBuilder sb = new StringBuilder();
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			sb.append(key);

			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("ydpay, orderid={}, sign={}", orderid, sign);

			Map<String, String> map = new HashMap<>();
			map.putAll(smap);
			map.put("pay_md5sign", sign);
			map.put("pay_productname", productname);
			map.put("gateway", gateway);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("ydpay error, e={}", e.getMessage());
		}
		return new HashMap<String, String>();
	}
	
	/**
	 * weipay
	 */
	public static Map<String, String> weipay(String appid, String appkey, String outTradeNo, String money, String notifyurl, String subject, String backType, String createPayType, String payCode, String gateway) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("appid", appid);
			map.put("outTradeNo", outTradeNo);
			map.put("money", money);
			map.put("notify_url", notifyurl);
			map.put("subject", subject);
			map.put("back_type", backType);
			map.put("create_pay_type", createPayType);
			map.put("pay_type", payCode);
			
			StringBuilder sb = new StringBuilder();
			sb.append(appid);
			sb.append(outTradeNo);
			sb.append(money);
			sb.append(appkey);

			String sign = MD5.stringToMD5(sb.toString());
			
			logger.info("weipay, outTradeNo={}, sign={}", outTradeNo, sign);
			map.put("sign", sign);
			map.put("gateway", gateway);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("weipay error, e={}", e.getMessage());
		}
		return new HashMap<String, String>();
	}
	
	/**
	 * 民付宝支付
	 */
	public static Map<String, String> mfbpay(String payMemberid, String orderid, String applydate, String bankcode, String notifyurl, String callbackurl, String amount, String productname, String key, String gateway) {
		try {
			SortedMap<String, String> smap = new TreeMap<>();
			smap.put("pay_memberid", payMemberid);
			smap.put("pay_orderid", orderid);
			smap.put("pay_applydate", applydate);
			smap.put("pay_bankcode", bankcode);
			smap.put("pay_notifyurl", notifyurl);
			smap.put("pay_callbackurl", callbackurl);
			smap.put("pay_amount", amount);
			
			StringBuilder sb = new StringBuilder();
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			sb.append(key);

			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("mfbpay, orderid={}, sign={}", orderid, sign);

			Map<String, String> map = new HashMap<>();
			map.putAll(smap);
			map.put("pay_md5sign", sign);
			map.put("pay_productname", productname);
			map.put("gateway", gateway);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("ydpay error, e={}", e.getMessage());
		}
		return new HashMap<String, String>();
	}
	
	/**
	 * 金钱汇支付
	 */
	public static Map<String, Object> jqhpay(String orderNo, String subject, String amount, String channel, String mchid, String returnUrl, String notifyUrl, String clientIp, String key) {
		try {
			SortedMap<String, Object> smap = new TreeMap<>();
			smap.put("out_trade_no", orderNo);
			smap.put("amount", amount);
			smap.put("currency", "CNY");
			smap.put("channel", channel);
			smap.put("mchid", mchid);
			smap.put("return_url", returnUrl);
			smap.put("notify_url", notifyUrl);
			
			StringBuilder sb = new StringBuilder();
			
			for(Entry<String, Object> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			sb.append(key);

			logger.info("jqhpay, orderNo={}, signsb={}", orderNo, sb.toString());
			
			String sign = MD5.stringToMD5(sb.toString());
			
			logger.info("jqhpay, orderNo={}, sign={}", orderNo, sign);

			Map<String, Object> map = new HashMap<>();
			map.putAll(smap);
			map.put("subject", subject);
			map.put("extparam", "xxx");
			map.put("sign_type", "2");
			map.put("body", subject);
			map.put("client_ip", clientIp);
			map.put("sign", sign);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("jqhpay error, e={}", e.getMessage());
		}
		return new HashMap<String, Object>();
	}
	
	/**
	 * 聚合支付
	 */
	public static Map<String, Object> juhepay(String orderNo, String service, String mchId, String paytype, String totalFee, String notifyUrl, String gateway, String key) {
		try {
			SortedMap<String, Object> smap = new TreeMap<>();
			smap.put("service", service);
			smap.put("mch_id", mchId);
			smap.put("paytype", paytype);
			smap.put("out_trade_no", orderNo);
			smap.put("total_fee", totalFee);
			smap.put("time", System.currentTimeMillis());
			smap.put("notify_url", notifyUrl);
			
			StringBuilder sb = new StringBuilder();
			
			for(Entry<String, Object> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			sb.append(key);

			logger.info("juhepay, orderNo={}, signsb={}", orderNo, sb.toString());
			
			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("juhepay, orderNo={}, sign={}", orderNo, sign);

			Map<String, Object> map = new HashMap<>();
			map.putAll(smap);
			map.put("gateway", gateway);
			map.put("sign", sign);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("juhepay error, e={}", e.getMessage());
		}
		return new HashMap<String, Object>();
	}
}
