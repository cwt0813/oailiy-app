package com.yiliao.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.github.wxpay.sdk.WXPayUtil;
import com.yiliao.service.CallBackService;
import com.yiliao.service.ConsumeService;
import com.yiliao.util.PrintUtil;
import com.yiliao.util.Utilities.MD5;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("pay")
public class PayCallbackControl {
	
	private static Logger logger = LoggerFactory
			.getLogger(PayCallbackControl.class);

	@Autowired
	private ConsumeService consumeService;
	
	
	@Autowired
	private CallBackService callBackService;
	
	
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	String FEATURE = null;
	/**
	 * 微信支付回调
	 * @param request
	 * @param response
	 */
	@RequestMapping("wxPayCallBack")
	@ResponseBody
	public void wxPayCallBack(HttpServletRequest request,
			HttpServletResponse response) {

		BufferedReader reader = null;

		try {
			
			FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
			dbf.setFeature(FEATURE, true);
			
			// If you can't completely disable DTDs, then at least do the following:
			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
			// JDK7+ - http://xml.org/sax/features/external-general-entities 
			FEATURE = "http://xml.org/sax/features/external-general-entities";
			dbf.setFeature(FEATURE, false);
			
			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
			// JDK7+ - http://xml.org/sax/features/external-parameter-entities 
			FEATURE = "http://xml.org/sax/features/external-parameter-entities";
			dbf.setFeature(FEATURE, false);
			
			// Disable external DTDs as well
			FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
			dbf.setFeature(FEATURE, false);
			
			// and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
			dbf.setXIncludeAware(false);
			dbf.setExpandEntityReferences(false);
			
			// 读取微信发送的数据
			reader = request.getReader();
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=UTF-8");
			response.setHeader("Access-Control-Allow-Origin", "*");

			String line = "";
			String xmlString = null;

			StringBuffer inputString = new StringBuffer();

			while ((line = reader.readLine()) != null) {
				inputString.append(line);
			}
			xmlString = inputString.toString();
			request.getReader().close();

//			System.out.println("回调通知->" + xmlString);
			
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
			out.flush();
			out.close();
			
			//字符串转为Map对象
			Map<String, String> xmlToMap = WXPayUtil.xmlToMap(xmlString);
			
			logger.info("return_code-->{},result_code-->{}",xmlToMap.get("return_code"),xmlToMap.get("result_code"));
			//支付成功!
			if("SUCCESS".equals(xmlToMap.get("return_code")) && "SUCCESS".equals(xmlToMap.get("result_code"))){
				
				this.consumeService.payNotify(xmlToMap.get("out_trade_no"), xmlToMap.get("transaction_id"));
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * <pre>
	 * 第一步:验证签名,签名通过后进行第二步
	 * 第二步:按一下步骤进行验证
	 * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
	 * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
	 * 3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
	 * 4、验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
	 * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
	 * 在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
	 * </pre>
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping("alipay_callback")
	@ResponseBody
	public  void  callback(HttpServletRequest request,HttpServletResponse response) {
		 
	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("支付宝回调，{}", params);
		try {
			
			String alipayPublicKey = this.consumeService.getAlipayPublicKey();
			logger.info("alipayPublicKey- >{}",alipayPublicKey);
			// 调用SDK验证签名
			boolean signVerified = AlipaySignature.rsaCheckV1(params,alipayPublicKey,
							"UTF-8", "RSA2");
			if (signVerified) {
				logger.info("支付宝回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.check(params);
				// 支付成功
				if ("TRADE_SUCCESS".equals(params.get("trade_status"))){
					// 处理支付成功逻辑
					try {
//						this.callBackService.alipayPaymentComplete(param.getOutTradeNo());
						this.consumeService.payNotify(params.get("out_trade_no"), params.get("trade_no"));
					} catch (Exception e) {
						logger.error("支付宝回调业务处理报错,params:" + params, e);
					}
				} else {
					logger.error("没有处理支付宝回调业务，支付宝交易状态：{},params:{}",params.get("trade_status"), params);
				}
				// 如果签名验证正确，立即返回success，后续业务另起线程单独处理
				// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
				PrintUtil.printWriStr("success", response);
			} else {
				logger.info("支付宝回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (AlipayApiException e) {
			logger.error("支付宝回调签名认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}

	/**
	 * 闪电支付回调
	 */
	@RequestMapping("sdpay_callback")
	@ResponseBody
	public void sdpayCallback(HttpServletRequest request,HttpServletResponse response) {
		 
	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("闪电支付回调，{}", params);
		try {
			
			String sdpay_map_sign = params.get("sign");
			params.remove("sign");
			StringBuilder sb = new StringBuilder();
			
			sb.append("no").append("=").append(params.get("no")).append("&");
			sb.append("order_no").append("=").append(params.get("order_no")).append("&");
			sb.append("trade_name").append("=").append(params.get("trade_name")).append("&");
			sb.append("pay_type").append("=").append(params.get("pay_type")).append("&");
			sb.append("order_amount").append("=").append(params.get("order_amount")).append("&");
			sb.append("pay_amount").append("=").append(params.get("pay_amount")).append("&");
			sb.append("order_uid").append("=").append(params.get("order_uid")).append("&");
			
			String key = this.consumeService.getSdpayKey();
			
			sb.append(key);
			
			String sign = MD5.stringToMD5(sb.toString());
			
			logger.info("sdpay_sign- >{}",sign);
			logger.info("sdpay_map_sign- >{}",sdpay_map_sign);
			// 验证签名
			boolean signVerified = sign.equals(sdpay_map_sign);
					
			if (signVerified) {
				logger.info("闪电支付回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.sdpayCheck(params);
				// 支付成功
				// 处理支付成功逻辑
				try {
					this.consumeService.payNotify(params.get("order_no"), params.get("no"));
					// 如果签名验证正确，立即返回success，后续业务另起线程单独处理
					PrintUtil.printWriStr("success", response);
				} catch (Exception e) {
					logger.error("闪电支付回调业务处理报错,params:" + params, e);
					PrintUtil.printWriStr("failure", response);
				}
			} else {
				logger.info("闪电支付回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (Exception e) {
			logger.error("闪电支付回调认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}
	
//	/**
//	 * 闪电支付回调
//	 */
//	@RequestMapping("sdpay_callback")
//	@ResponseBody
//	public String sdpayCallback(HttpServletRequest request,HttpServletResponse response) {
//		 
//	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
//		logger.info("闪电支付回调，{}", params);
//		try {
//			
//			String sdpay_map_sign = params.get("sign");
//			params.remove("sign");
//			StringBuilder sb = new StringBuilder();
//			
//			sb.append("no").append("=").append(params.get("no")).append("&");
//			sb.append("order_no").append("=").append(params.get("order_no")).append("&");
//			sb.append("trade_name").append("=").append(params.get("trade_name")).append("&");
//			sb.append("pay_type").append("=").append(params.get("pay_type")).append("&");
//			sb.append("order_amount").append("=").append(params.get("order_amount")).append("&");
//			sb.append("pay_amount").append("=").append(params.get("pay_amount")).append("&");
//			sb.append("order_uid").append("=").append(params.get("order_uid")).append("&");
//			
//			String key = this.consumeService.getSdpayKey();
//			
//			sb.append(key);
//			
//			String sign = MD5.stringToMD5(sb.toString());
//			
//			logger.info("sdpay_sign- >{}",sign);
//			logger.info("sdpay_map_sign- >{}",sdpay_map_sign);
//			// 验证签名
//			boolean signVerified = sign.equals(sdpay_map_sign);
//					
//			if (signVerified) {
//				logger.info("闪电支付回调签名认证成功");
//				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
//				this.sdpayCheck(params);
//				// 支付成功
//				// 处理支付成功逻辑
//				try {
//					this.consumeService.payNotify(params.get("order_no"), params.get("no"));
//					// 如果签名验证正确，立即返回success，后续业务另起线程单独处理
////					PrintUtil.printWriStr("success", response);
//					return "success";
//				} catch (Exception e) {
//					logger.error("闪电支付回调业务处理报错,params:" + params, e);
////					PrintUtil.printWriStr("failure", response);
//					return "failure";
//				}
//			} else {
//				logger.info("闪电支付回调签名认证失败，signVerified=false, paramsJson:{}",params);
////				PrintUtil.printWriStr("failure", response);
//				return "failure";
//			}
//		} catch (Exception e) {
//			logger.error("闪电支付回调认证失败,paramsJson:{},errorMsg:{}", params,
//					e.getMessage());
////			PrintUtil.printWriStr("failure", response);
//			return "failure";
//		}
//	}
	
	/**
	 * 天玑支付回调
	 */
	@RequestMapping("tjpay_callback")
	@ResponseBody
	public void tjpayCallback(HttpServletRequest request,HttpServletResponse response) {
		 
	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("天玑支付回调，{}", params);
		try {
			
			String tjpay_map_sign = params.get("sign");
			params.remove("sign");
			StringBuilder sb = new StringBuilder();
			
			SortedMap<String, String> smap = new TreeMap<>();
			smap.putAll(params);
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			String key = this.consumeService.getTjpayKey();
			sb.append(key);
			
			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("tjpay_sign- >{}",sign);
			logger.info("tjpay_map_sign- >{}",tjpay_map_sign);
			// 验证签名
			boolean signVerified = sign.equals(tjpay_map_sign);
					
			if (signVerified) {
				logger.info("天玑支付回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.tjpayCheck(params);
				// 支付成功
				if ("1".equals(params.get("payState"))){
					// 处理支付成功逻辑
					try {
						this.consumeService.payNotify(params.get("outOrderNo"), params.get("outOrderNo"));
					} catch (Exception e) {
						logger.error("天玑支付回调业务处理报错,params:" + params, e);
					}
				} else {
					logger.error("没有处理天玑支付回调业务，天玑支付交易状态：{},params:{}",params.get("payState"), params);
				}
				// 如果签名验证正确，立即返回success，后续业务另起线程单独处理
				// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
				PrintUtil.printWriStr("success", response);
			} else {
				logger.info("天玑支付回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (Exception e) {
			logger.error("天玑支付回调认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}
	
	/**
	 * 德汇支付回调
	 */
	@RequestMapping("dhpay_callback")
	@ResponseBody
	public void dhpayCallback(HttpServletRequest request,HttpServletResponse response) {
		 
	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("德汇支付回调，{}", params);
		try {
			
			String tjpay_map_sign = params.get("sign");
			params.remove("sign");
			params.remove("attach");
			StringBuilder sb = new StringBuilder();
			
			SortedMap<String, String> smap = new TreeMap<>();
			smap.putAll(params);
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			String key = this.consumeService.getDhpayKey();
			sb.append(key);
			
			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("dhpay_sign- >{}",sign);
			logger.info("dhpay_map_sign- >{}",tjpay_map_sign);
			// 验证签名
			boolean signVerified = sign.equals(tjpay_map_sign);
					
			if (signVerified) {
				logger.info("德汇支付回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.dhpayCheck(params);
				// 支付成功
				if ("00".equals(params.get("returncode"))){
					// 处理支付成功逻辑
					try {
						this.consumeService.payNotify(params.get("orderid"), params.get("transaction_id"));
					} catch (Exception e) {
						logger.error("德汇支付回调业务处理报错,params:" + params, e);
					}
				} else {
					logger.error("没有处理德汇支付回调业务，德汇支付交易状态：{},params:{}",params.get("payState"), params);
				}
				// 如果签名验证正确，立即返回success，后续业务另起线程单独处理
				// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
				PrintUtil.printWriStr("success", response);
			} else {
				logger.info("德汇支付回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (Exception e) {
			logger.error("德汇支付回调认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}
	
	/**
	 * 云鼎支付回调
	 */
	@RequestMapping("ydpay_callback")
	@ResponseBody
	public void ydpayCallback(HttpServletRequest request,HttpServletResponse response) {
		 
	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("云鼎支付回调，{}", params);
		try {
			
			String ydpay_map_sign = params.get("sign");
			params.remove("sign");
			StringBuilder sb = new StringBuilder();
			
			SortedMap<String, String> smap = new TreeMap<>();
			smap.putAll(params);
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			String key = this.consumeService.getYdpayKey();
			sb.append(key);
			
			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("ydpay_sign- >{}",sign);
			logger.info("ydpay_map_sign- >{}",ydpay_map_sign);
			// 验证签名
			boolean signVerified = sign.equals(ydpay_map_sign);
					
			if (signVerified) {
				logger.info("云鼎支付回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.ydpayCheck(params);
				// 支付成功
				if ("00".equals(params.get("returncode"))){
					// 处理支付成功逻辑
					try {
						this.consumeService.payNotify(params.get("orderid"), params.get("transaction_id"));
					} catch (Exception e) {
						logger.error("云鼎支付回调业务处理报错,params:" + params, e);
					}
				} else {
					logger.error("没有处理云鼎支付回调业务，云鼎支付交易状态：{},params:{}",params.get("returncode"), params);
				}
				// 如果签名验证正确，立即返回OK，后续业务另起线程单独处理
				// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
				PrintUtil.printWriStr("OK", response);
			} else {
				logger.info("云鼎支付回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (Exception e) {
			logger.error("云鼎支付回调认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}
	
	/**
	 * weipay回调
	 */
	@RequestMapping("weipay_callback")
	@ResponseBody
	public void weipayCallback(HttpServletRequest request,HttpServletResponse response) {
		 
		String out_trade_no = null;
		String total_amount = null;
		String trade_status = null;
		String sign2 = null;
		
		try {
			ServletInputStream ris = request.getInputStream();
			byte[] b = new byte[1024];
			int lens = -1;
			while ((lens = ris.readLine(b, 0, 1000)) > 0) {
				String str = new String(b, 0, lens);
				if(str.contains("\"out_trade_no\"")) {
					int lens2 = -1;
					lens2 = ris.readLine(b, 0, 1000);
					lens2 = ris.readLine(b, 0, 1000);
					out_trade_no = new String(b, 0, lens2);
				}else if(str.contains("\"total_amount\"")) {
					int lens2 = -1;
					lens2 = ris.readLine(b, 0, 1000);
					lens2 = ris.readLine(b, 0, 1000);
					total_amount = new String(b, 0, lens2);
				}else if(str.contains("\"trade_status\"")) {
					int lens2 = -1;
					lens2 = ris.readLine(b, 0, 1000);
					lens2 = ris.readLine(b, 0, 1000);
					trade_status = new String(b, 0, lens2);
				}else if(str.contains("\"sign2\"")) {
					int lens2 = -1;
					lens2 = ris.readLine(b, 0, 1000);
					lens2 = ris.readLine(b, 0, 1000);
					sign2 = new String(b, 0, lens2);
				}
			}

		}catch (Exception e) {
			logger.info("weipay回调转化失败，e={}", e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
		
		Map<String, String> params = new HashMap<>();
		params.put("out_trade_no", out_trade_no.replace("\r", "").replace("\n", ""));
		params.put("total_amount", total_amount.replace("\r", "").replace("\n", ""));
		params.put("trade_status", trade_status.replace("\r", "").replace("\n", ""));
		params.put("sign2", sign2.replace("\r", "").replace("\n", ""));
		
		logger.info("weipay回调，{}", params);
		try {
			
			String weipay_map_sign = params.get("sign2");
			params.remove("sign2");
			
			StringBuilder sb = new StringBuilder();
			String appid = this.consumeService.getWeipayAppid();
			sb.append(appid);
			sb.append(params.get("out_trade_no"));
			sb.append(params.get("total_amount"));
			String key = this.consumeService.getWeipayKey();
			sb.append(key);
			
			String sign = MD5.stringToMD5(sb.toString());
			
			logger.info("weipay_sign- >{}",sign);
			logger.info("weipay_map_sign- >{}",weipay_map_sign);
			// 验证签名
			boolean signVerified = sign.equals(weipay_map_sign);
					
			if (signVerified) {
				logger.info("weipay回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.weipayCheck(params);
				// 支付成功
				if ("TRADE_SUCCESS".equals(params.get("trade_status"))){
					// 处理支付成功逻辑
					try {
						this.consumeService.payNotify(params.get("out_trade_no"), params.get("out_trade_no"));
					} catch (Exception e) {
						logger.error("weipay回调业务处理报错,params:" + params, e);
					}
				} else {
					logger.error("没有处理weipay回调业务，weipay交易状态：{},params:{}",params.get("trade_status"), params);
				}
				// 如果签名验证正确，立即返回success，后续业务另起线程单独处理
				// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
				PrintUtil.printWriStr("success", response);
			} else {
				logger.info("weipay回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (Exception e) {
			logger.error("weipay回调认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}
	
	/**
	 * 民付宝支付回调
	 */
	@RequestMapping("mfbpay_callback")
	@ResponseBody
	public void mfbpayCallback(HttpServletRequest request,HttpServletResponse response) {
		 
	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("民付宝支付回调，{}", params);
		try {
			
			String mfbpay_map_sign = params.get("sign");
			params.remove("sign");
			StringBuilder sb = new StringBuilder();
			
			SortedMap<String, String> smap = new TreeMap<>();
			smap.putAll(params);
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			String key = this.consumeService.getMfbpayKey();
			sb.append(key);
			
			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("mfbpay_sign- >{}",sign);
			logger.info("mfbpay_map_sign- >{}",mfbpay_map_sign);
			// 验证签名
			boolean signVerified = sign.equals(mfbpay_map_sign);
					
			if (signVerified) {
				logger.info("民付宝支付回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.mfbpayCheck(params);
				// 支付成功
				if ("00".equals(params.get("returncode"))){
					// 处理支付成功逻辑
					try {
						this.consumeService.payNotify(params.get("orderid"), params.get("transaction_id"));
					} catch (Exception e) {
						logger.error("民付宝支付回调业务处理报错,params:" + params, e);
					}
				} else {
					logger.error("没有处理民付宝支付回调业务，民付宝支付交易状态：{},params:{}",params.get("returncode"), params);
				}
				// 如果签名验证正确，立即返回OK，后续业务另起线程单独处理
				// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
				PrintUtil.printWriStr("OK", response);
			} else {
				logger.info("民付宝支付回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (Exception e) {
			logger.error("民付宝支付回调认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}
	
	/**
	 * 金钱汇支付回调
	 * @throws IOException 
	 */
	@RequestMapping("jqhpay_callback")
	@ResponseBody
	public void jqhpayCallback(HttpServletRequest request,HttpServletResponse response){
		
		final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("金钱汇支付回调，{}", params);
		try {
			System.out.println(params.get("result_code"));
 			JSONObject charge = new JSONObject();
			charge.put("out_trade_no", params.get("charge[out_trade_no]"));
			charge.put("amount", params.get("charge[amount]"));
			charge.put("trade_no", params.get("charge[trade_no]"));
			charge.put("currency", params.get("charge[currency]"));
			charge.put("mchid", params.get("charge[mchid]"));
			charge.put("channel", params.get("charge[channel]"));
			charge.put("noncestr", params.get("charge[noncestr]"));
			charge.put("sign", params.get("charge[sign]"));
			String jqhpay_map_sign = charge.getString("sign");
			
			StringBuilder sb = new StringBuilder();
			
			SortedMap<String, String> smap = new TreeMap<>();
			smap.putAll((Map<String, String>)charge);
			smap.remove("sign");
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			String key = this.consumeService.getJqhpayKey();
			sb.append(key);
			
			String sign = MD5.stringToMD5(sb.toString());
			
			logger.info("jqhpay_sign- >{}",sign);
			logger.info("jqhpay_map_sign- >{}",jqhpay_map_sign);
			// 验证签名
			boolean signVerified = sign.equals(jqhpay_map_sign);
					
			if (signVerified) {
				logger.info("金钱汇支付回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.jqhpayCheck(charge);
				// 支付成功
				if ("OK".equals(params.get("result_code"))){
					// 处理支付成功逻辑
					try {
						this.consumeService.payNotify(charge.getString("out_trade_no"), charge.getString("trade_no"));
					} catch (Exception e) {
						logger.error("金钱汇支付回调业务处理报错,params:" + params, e);
					}
				} else {
					logger.error("没有处理金钱汇支付回调业务，金钱汇支付交易状态：{},params:{}",params.get("result_code"), params);
				}
				// 如果签名验证正确，立即返回OK，后续业务另起线程单独处理
				// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
				JSONObject respJso = new JSONObject();
				respJso.put("result_msg", "SUCCESS");
				respJso.put("result_code", "OK");
				PrintUtil.printWriStr(respJso.toString(), response);
			} else {
				logger.info("金钱汇支付回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (Exception e) {
			logger.error("金钱汇支付回调认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}
	
	/**
	 * 聚合支付回调
	 */
	@RequestMapping("juhepay_callback")
	@ResponseBody
	public void juhepayCallback(HttpServletRequest request,HttpServletResponse response) {
		 
	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("聚合支付回调，{}", params);
		try {
			
			String juhepay_map_sign = params.get("sign");
			params.remove("sign");
			StringBuilder sb = new StringBuilder();
			
			SortedMap<String, String> smap = new TreeMap<>();
			smap.putAll(params);
			
			for(Entry<String, String> e:smap.entrySet()) {
				sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			}
			sb.append("key=");
			String key = this.consumeService.getYdpayKey();
			sb.append(key);
			
			String sign = MD5.stringToMD5(sb.toString()).toUpperCase();
			
			logger.info("juhepay_sign- >{}",sign);
			logger.info("juhepay_map_sign- >{}",juhepay_map_sign);
			// 验证签名
			boolean signVerified = sign.equals(juhepay_map_sign);
					
			if (signVerified) {
				logger.info("云鼎支付回调签名认证成功");
				// 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
				this.juhepayCheck(params);
				// 支付成功
				if ("2".equals(params.get("returncode"))){
					// 处理支付成功逻辑
					try {
						this.consumeService.payNotify(params.get("out_biz_no"), params.get("out_biz_no"));
					} catch (Exception e) {
						logger.error("聚合支付回调业务处理报错,params:" + params, e);
					}
				} else {
					logger.error("没有处理聚合支付回调业务，聚合支付交易状态：{},params:{}",params.get("returncode"), params);
				}
				// 如果签名验证正确，立即返回OK，后续业务另起线程单独处理
				// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
				PrintUtil.printWriStr("success", response);
			} else {
				logger.info("聚合支付回调签名认证失败，signVerified=false, paramsJson:{}",params);
				PrintUtil.printWriStr("failure", response);
			}
		} catch (Exception e) {
			logger.error("聚合支付回调认证失败,paramsJson:{},errorMsg:{}", params,
					e.getMessage());
			PrintUtil.printWriStr("failure", response);
		}
	}
	
	// 将request中的参数转换成Map
	@SuppressWarnings("unchecked")
	private static Map<String, String> convertRequestParamsToMap(
			HttpServletRequest request) {
		Map<String, String> retMap = new HashMap<String, String>();

		Set<Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();

		for (Entry<String, String[]> entry : entrySet) {
			String name = entry.getKey();
			String[] values = entry.getValue();
			int valLen = values.length;

			if (valLen == 1) {
				retMap.put(name, values[0]);
			} else if (valLen > 1) {
				StringBuilder sb = new StringBuilder();
				for (String val : values) {
					sb.append(",").append(val);
				}
				retMap.put(name, sb.toString().substring(1));
			} else {
				retMap.put(name, "");
			}
		}

		return retMap;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
    public static String getRequestJsonString(HttpServletRequest request)
            throws IOException {
        String submitMehtod = request.getMethod();
        // GET
        if (submitMehtod.equals("GET")) {
            return new String(request.getQueryString().getBytes("iso-8859-1"),"utf-8").replaceAll("%22", "\"");
        // POST
        } else {
            return getRequestPostStr(request);
        }
    }
    

    /**      
     * 描述:获取 post 请求的 byte[] 数组
     * <pre>
     * 举例：
     * </pre>
     * @param request
     * @return
     * @throws IOException      
     */
    public static byte[] getRequestPostBytes(HttpServletRequest request)
            throws IOException {
        int contentLength = request.getContentLength();
        if(contentLength<0){
            return null;
        }
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength;) {
 
            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }
 
    /**      
     * 描述:获取 post 请求内容
     * <pre>
     * 举例：
     * </pre>
     * @param request
     * @return
     * @throws IOException      
     */
    public static String getRequestPostStr(HttpServletRequest request)
            throws IOException {
        byte buffer[] = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        return new String(buffer, charEncoding);
    }
    
	/**
	 * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
	 * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
	 * 3、校验通知中的seller_id（或者seller_email
	 * )是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
	 * 4、验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
	 * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
	 * 在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
	 * 
	 * @param params
	 * @throws AlipayApiException
	 */
	private void check(Map<String, String> params) throws AlipayApiException {
		
		String outTradeNo = params.get("out_trade_no");

		// 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(outTradeNo);
		if (null == dataMap) {
			 throw new AlipayApiException("out_trade_no错误");
		}

		// 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		BigDecimal payMoney = new BigDecimal(params.get("total_amount"));
		if (payMoney.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
		   throw new AlipayApiException("error total_amount");
		}
//		 4、验证app_id是否为该商户本身。
		 if (!params.get("app_id").equals(this.consumeService.getAlipayAppId())) {
			throw new AlipayApiException("app_id不一致");
		 }
	}
	
	/**
	 * 闪电支付回调校验
	 * @param params
	 * @throws AlipayApiException
	 */
	private void sdpayCheck(Map<String, String> params) throws AlipayApiException {
		
		String orderNo = params.get("order_no");

		// 1、商户需要验证该通知数据中的order_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(orderNo);
		if (null == dataMap) {
			throw new AlipayApiException("order_no错误");
		}

		// 2、判断order_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		logger.info("orderAmount", params.get("order_amount"));
		logger.info("t_recharge_money={}", dataMap.get("t_recharge_money").toString());
		BigDecimal orderAmount = new BigDecimal(params.get("order_amount"));
		if (orderAmount.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
			throw new AlipayApiException("error order_amount");
		}
		
		// 3、判断order_uid跟userId是否一致
		String orderUid = params.get("order_uid");
		logger.info("orderUid={}", orderUid);
		logger.info("t_user_id={}", dataMap.get("t_user_id").toString());
		if (!orderUid.equals(dataMap.get("t_user_id").toString())) {
			throw new AlipayApiException("error order_uid");
		}
	}
	
	/**
	 * 天玑支付回调校验
	 * @param params
	 * @throws AlipayApiException
	 */
	private void tjpayCheck(Map<String, String> params) throws AlipayApiException {
		
		String orderNo = params.get("outOrderNo");

		// 1、商户需要验证该通知数据中的order_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(orderNo);
		if (null == dataMap) {
			throw new AlipayApiException("outOrderNo错误");
		}

		// 2、判断order_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		logger.info("payAmount", params.get("payAmount"));
		logger.info("t_recharge_money={}", dataMap.get("t_recharge_money").toString());
		BigDecimal payAmount = new BigDecimal(params.get("payAmount"));
		if (payAmount.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
			throw new AlipayApiException("error payAmount");
		}
		
	}
	
	/**
	 * 德汇支付回调校验
	 * @param params
	 * @throws AlipayApiException
	 */
	private void dhpayCheck(Map<String, String> params) throws AlipayApiException {
		
		String orderNo = params.get("orderid");

		// 1、商户需要验证该通知数据中的order_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(orderNo);
		if (null == dataMap) {
			throw new AlipayApiException("orderid错误");
		}

		// 2、判断amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		logger.info("amount", params.get("amount"));
		logger.info("t_recharge_money={}", dataMap.get("t_recharge_money").toString());
		BigDecimal payAmount = new BigDecimal(params.get("amount"));
		if (payAmount.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
			throw new AlipayApiException("error amount");
		}
		
	}
	 
	/**
	 * 云鼎支付回调校验
	 * @param params
	 * @throws AlipayApiException
	 */
	private void ydpayCheck(Map<String, String> params) throws AlipayApiException {
		
		String orderNo = params.get("orderid");

		// 1、商户需要验证该通知数据中的order_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(orderNo);
		if (null == dataMap) {
			throw new AlipayApiException("orderid错误");
		}

		// 2、判断amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		logger.info("amount", params.get("amount"));
		logger.info("t_recharge_money={}", dataMap.get("t_recharge_money").toString());
		BigDecimal payAmount = new BigDecimal(params.get("amount"));
		if (payAmount.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
			throw new AlipayApiException("error amount");
		}
		
	}
	
	/**
	 * weipay回调校验
	 * @param params
	 * @throws AlipayApiException
	 */
	private void weipayCheck(Map<String, String> params) throws AlipayApiException {
		
		String orderNo = params.get("out_trade_no");

		// 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(orderNo);
		if (null == dataMap) {
			throw new AlipayApiException("out_trade_no错误");
		}

		// 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		logger.info("total_amount", params.get("total_amount"));
		logger.info("t_recharge_money={}", dataMap.get("t_recharge_money").toString());
		BigDecimal payAmount = new BigDecimal(params.get("total_amount"));
		if (payAmount.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
			throw new AlipayApiException("error total_amount");
		}
		
	}
	
	/**
	 * 民付宝支付回调校验
	 * @param params
	 * @throws AlipayApiException
	 */
	private void mfbpayCheck(Map<String, String> params) throws AlipayApiException {
		
		String orderNo = params.get("orderid");

		// 1、商户需要验证该通知数据中的order_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(orderNo);
		if (null == dataMap) {
			throw new AlipayApiException("orderid错误");
		}

		// 2、判断amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		logger.info("amount", params.get("amount"));
		logger.info("t_recharge_money={}", dataMap.get("t_recharge_money").toString());
		BigDecimal payAmount = new BigDecimal(params.get("amount"));
		if (payAmount.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
			throw new AlipayApiException("error amount");
		}
		
	}
	
	/**
	 * 金钱汇支付回调校验
	 * @param params
	 * @throws AlipayApiException
	 */
	private void jqhpayCheck(JSONObject charge) throws AlipayApiException {
		
		String orderNo = charge.getString("out_trade_no");

		// 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(orderNo);
		if (null == dataMap) {
			throw new AlipayApiException("out_trade_no错误");
		}

		// 2、判断amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		logger.info("amount", charge.getString("amount"));
		logger.info("t_recharge_money={}", dataMap.get("t_recharge_money").toString());
		BigDecimal payAmount = new BigDecimal(charge.getString("amount"));
		if (payAmount.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
			throw new AlipayApiException("error amount");
		}
		
	}
	
	/**
	 * 聚合支付回调校验
	 * @param params
	 * @throws AlipayApiException
	 */
	private void juhepayCheck(Map<String, String> params) throws AlipayApiException {
		
		String orderNo = params.get("out_biz_no");

		// 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
		Map<String, Object> dataMap = this.callBackService.getOrderByOrderNo(orderNo);
		if (null == dataMap) {
			throw new AlipayApiException("out_biz_no错误");
		}

		// 2、判断amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		logger.info("amount", params.get("amount"));
		logger.info("t_recharge_money={}", dataMap.get("t_recharge_money").toString());
		BigDecimal payAmount = new BigDecimal(params.get("amount"));
		if (payAmount.compareTo(new BigDecimal(dataMap.get("t_recharge_money").toString()))!= 0) {
			throw new AlipayApiException("error amount");
		}
		
	}

	public static Object mapToObject(Map<String, Object> map, Class<?> beanClass)
			throws Exception {
		if (map == null)
			return null;

		Object obj = beanClass.newInstance();

		org.apache.commons.beanutils.BeanUtils.populate(obj, map);

		return obj;
	}
	
	/**
	 * 支付成功页面
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("jumpPaySuccess")
	public ModelAndView jumpPaySuccess() {
		// 解密参数
		ModelAndView mv = new ModelAndView();
		mv.setViewName("paySuccess");
		return mv;
	}
	
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("WEPAY5315389xiaojun");
		sb.append("weipay_2737_1572772724934");
		sb.append("1.00");
		sb.append("skiapp5315389xiaojun");
		
		System.out.println(MD5.stringToMD5(sb.toString()));
		
		
	}

}
