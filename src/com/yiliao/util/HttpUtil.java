package com.yiliao.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import net.sf.json.JSONObject;

public class HttpUtil {

	/**
	 * @方法名 httpConnection
	 * @说明 (使用url获取网络数据)
	 * @param 参数
	 * @param URL
	 * @param 参数
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	public static JSONObject httpConnection(String URL) {
		URL url = null;
		HttpURLConnection connection = null;
		// 生成验证码
		try {
			url = new URL(URL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			// connection.setRequestProperty("Content-type", "text/html");
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.setUseCaches(false);
			connection.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "utf-8"));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			reader.close();
			return new JSONObject().fromObject(buffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public void text(String content) throws IOException {

		FileWriter fileWriter = new FileWriter("D:\\111\\123456.txt");
		fileWriter.write(content);
		fileWriter.flush();
		fileWriter.close();
	}

	/**
	 * @方法名 getSend
	 * @说明 (短信通道post方法)
	 * @param 参数
	 * @param strUrl
	 * @param 参数
	 * @param param
	 * @param 参数
	 * @return 设定文件
	 * @return String 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	public static String postSend(String strUrl, String param) {

		URL url = null;
		HttpURLConnection connection = null;

		try {
			url = new URL(strUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.connect();

			// POST����ʱʹ��
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			out.writeBytes(param);
			out.flush();
			out.close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "utf-8"));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			reader.close();
			return buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	/**
	 * @方法名 httpClent
	 * @说明 (传递参数的方法)
	 * @param 参数
	 *            @return 设定文件
	 * @return JSONObject 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	@SuppressWarnings("static-access")
	public static String httpClent(String httpUrl,String content){
		   try {
	            //创建连接
	            URL url = new URL(httpUrl);
	            HttpURLConnection connection = (HttpURLConnection) url
	                    .openConnection();
	            connection.setDoOutput(true);
	            connection.setDoInput(true);
	            connection.setRequestMethod("POST");
	            connection.setUseCaches(false);
	            connection.setRequestProperty("Accept-Charset", "utf-8");
	            connection.setRequestProperty("contentType", "utf-8");
	            connection.setInstanceFollowRedirects(true);
	            connection.setRequestProperty("Content-Type",
	                    "application/x-www-form-urlencoded");
	            connection.connect();
	            PrintWriter out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(),"utf-8"));  
	            out.println(content);  
	            out.close(); 

	            //读取响应
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
	                    connection.getInputStream()));
	            String lines;
	            StringBuffer sb = new StringBuffer("");
	            while ((lines = reader.readLine()) != null) {
	                lines = new String(lines.getBytes(), "utf-8");
	                sb.append(lines);
	            }
	            reader.close();
	            // 断开连接
	            connection.disconnect();
	            
	            return sb.toString() ;
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
 
	    return 	null;
	}

	/**
	 * 转为16进制方法
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String paraTo16(String str) throws UnsupportedEncodingException {
		String hs = "";

		byte[] byStr = str.getBytes("UTF-8");
		for (int i = 0; i < byStr.length; i++) {
			String temp = "";
			temp = (Integer.toHexString(byStr[i] & 0xFF));
			if (temp.length() == 1)
				temp = "%0" + temp;
			else
				temp = "%" + temp;
			hs = hs + temp;
		}
		return hs.toUpperCase();

	}
	 
	//  静态方法，类名可直接调用
    public static String doPost(String url, Map<String, Object> paramsMap) {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        //配置连接超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .setRedirectsEnabled(true)
                .build();
        HttpPost httpPost = new HttpPost(url);
        //设置超时时间
        httpPost.setConfig(requestConfig);

        //装配post请求参数
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        for (String key : paramsMap.keySet()) {
            list.add(new BasicNameValuePair(key, String.valueOf(paramsMap.get(key))));
        }

        try {
            //将参数进行编码为合适的格式,如将键值对编码为param1=value1&param2=value2
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(urlEncodedFormEntity);

            //执行 post请求
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            String strRequest = "";
            if (null != closeableHttpResponse && !"".equals(closeableHttpResponse)) {
                System.out.println(closeableHttpResponse.getStatusLine().getStatusCode());
                if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity httpEntity = closeableHttpResponse.getEntity();
                    strRequest = EntityUtils.toString(httpEntity);
                } else {
                    strRequest = "Error Response" + closeableHttpResponse.getStatusLine().getStatusCode();
                }
            }
            return strRequest;

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return "协议异常";
        } catch (ParseException e) {
            e.printStackTrace();
            return "解析异常";
        } catch (IOException e) {
            e.printStackTrace();
            return "传输异常";
        } finally {
            try {
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
