package com.dg.jw.httpclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpUtils {
	private static Logger log=Logger.getLogger(HttpUtils.class.getName());
	
	private static RequestConfig requestConfig = RequestConfig.custom()  
	        .setSocketTimeout(25000)  
	        .setConnectTimeout(25000)  
	        .setConnectionRequestTimeout(25000)
	        .build();
	
	public static String RequestByHttpClientWithPost(String params){
		
		CloseableHttpClient httpClient = HttpClients.createDefault(); 
        CloseableHttpResponse response = null;
        String httpUrl = "";
        HttpPost httpPost = new HttpPost(httpUrl);
        httpPost.setConfig(requestConfig); 
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        
        String responseContent =null;   
        try {    
       	 	StringEntity se=new StringEntity(params,"utf-8");
            httpPost.setEntity(se); 
            response = httpClient.execute(httpPost);
            HttpEntity  entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8"); 
            log.info(responseContent);
           
        } catch (Exception e) {    
            e.printStackTrace();    
        }finally{
       	 try {    
                // 关闭连接,释放资源    
                if (response != null) {    
                    response.close();    
                }    
                if (httpClient != null) {    
                    httpClient.close();    
                }    
            } catch (IOException e) {    
                e.printStackTrace();    
            } 
        }
        
        return responseContent;
	}
	
   public static String httpRequestByHttpClientPostWithJson(String httpUrl,Map<String, Object> paramsMap){
		
		CloseableHttpClient httpClient = HttpClients.createDefault(); 
        CloseableHttpResponse response = null;
        
        HttpPost httpPost = new HttpPost(httpUrl);
        httpPost.setConfig(requestConfig); 
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
        if(paramsMap!=null&&!paramsMap.isEmpty()){
        	 for (String key : paramsMap.keySet()) {    
                 nameValuePairs.add(new BasicNameValuePair(key, paramsMap.get(key).toString()));    
             }   
        }
        
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        
        String responseContent =null;   
        try {    
       	    StringEntity se=new UrlEncodedFormEntity(nameValuePairs,"UTF-8");
            httpPost.setEntity(se); 
            response = httpClient.execute(httpPost);
            HttpEntity  entity = response.getEntity();
            
            log.info("headers: "+response.getAllHeaders());
            log.info("locale: "+response.getLocale());
            log.info("status line: "+response.getStatusLine());
            responseContent = EntityUtils.toString(entity, "UTF-8"); 
            log.info("response content: "+responseContent);
           
        } catch (Exception e) {    
            e.printStackTrace();    
        }finally{
       	 try {    
                // 关闭连接,释放资源    
                if (response != null) {    
                    response.close();    
                }    
                if (httpClient != null) {    
                    httpClient.close();    
                }    
            } catch (IOException e) {    
                e.printStackTrace();    
            } 
        }
        
        return responseContent;
	}
   
   public static String httpRequestByHttpClientPostWithFormData(String httpUrl,Map<String, Object> paramsMap){
		
	   CloseableHttpClient httpClient = HttpClients.createDefault();
       CloseableHttpResponse response = null;
       
       HttpPost httpPost = new HttpPost(httpUrl);
       httpPost.setConfig(requestConfig); 
       
       List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
       if(paramsMap!=null&&!paramsMap.isEmpty()){
       	 for (String key : paramsMap.keySet()) {    
                nameValuePairs.add(new BasicNameValuePair(key, paramsMap.get(key).toString()));    
            }   
       }
       
       String responseContent =null;   
       try {    
       	   UrlEncodedFormEntity se=new UrlEncodedFormEntity(nameValuePairs,"UTF-8");
           httpPost.setEntity(se); 
           response = httpClient.execute(httpPost);
           int statusCode = response.getStatusLine().getStatusCode();
           if (statusCode == HttpStatus.SC_OK) {
           	
	           	HttpEntity  entity = response.getEntity();
	           	responseContent = EntityUtils.toString(entity, "UTF-8"); 
           }else{
	           	HttpEntity  entity = response.getEntity();
	           	StringBuilder builder = new StringBuilder();
	           	builder.append("status:" + response.getStatusLine());
	            builder.append("headers:");
	            HeaderIterator iterator = response.headerIterator();
	            while (iterator.hasNext()) {
	               builder.append("\t" + iterator.next());
	            }
	            // 判断响应实体是否为空
	            if (entity != null) {
	               String excepResContent = EntityUtils.toString(entity);
	               builder.append("response length:" + excepResContent.length());
	               builder.append("response content:" + excepResContent.replace("\r\n", ""));
	            }
	            log.info(builder.toString());
	            responseContent=null;
           }
           
           log.info("-------Response headers--------");
           for(Header header:response.getAllHeaders()){
           	 log.info(header);
           }
           log.info("-------------------------------");
           log.info("locale: "+response.getLocale());
           log.info("protocol version: "+response.getProtocolVersion());
           log.info("status line: "+response.getStatusLine());
           log.info("response content: "+responseContent);
          
       } catch (Exception e) {    
           e.printStackTrace();    
       }finally{
      	 try {    
               // 关闭连接,释放资源    
               if (response != null) {    
                   response.close();    
               }    
               if (httpClient != null) {    
                   httpClient.close();    
               }    
           } catch (IOException e) {    
               e.printStackTrace();    
           } 
       }
       return responseContent;
	}
	
}
