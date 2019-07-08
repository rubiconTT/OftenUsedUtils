package com.dg.jw.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ParseXML {

	public static void main(String[] args) {
		parseXMLWithDom4j();
	}
	
	public static void parseXMLWithDom4j(){
		// 解析books.xml文件
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        Map<String,String> eleMap=new HashMap<String,String>();
        List<Object> jsonList=new ArrayList<Object>();
        try {
            // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
            Document document = reader.read(new File("src/main/resources/file/.xml"));
            // 通过document对象获取根节点bookstore
            
            Element userDetail = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator<Element> it = userDetail.elementIterator();
            // 遍历迭代器，获取根节点中的信息（书籍）
            while (it.hasNext()) {
                System.out.println("=====开始遍历某个人=====");
//                List<String> nodeAttrList=new ArrayList<String>();
                Element user = (Element) it.next();
                // 获取book的属性名以及 属性值
//                List<Attribute> userAttrs = user.attributes();
//                for (Attribute attr : userAttrs) {
//                    System.out.println("属性名：" + attr.getName() + " --属性值："
//                            + attr.getValue());
//                }
                Iterator<Element> itt = user.elementIterator();
                while (itt.hasNext()) {
                    Element userChild = (Element) itt.next();
                    if(userChild.elements().size()>0){
                    	
                    	System.out.println("节点名：" + userChild.getName());
                    	List<Attribute> userChildList=userChild.attributes();
                        
                        if(userChildList!=null&&userChildList.size()>0){
                        	for(Attribute userCChild:userChildList){
                        		System.out.println("----节点属性：" + userCChild.getName() + " --节点属性值：" + userCChild.getStringValue());
                        		eleMap.put(userChild.getName(), userCChild.getStringValue());
                        	}
                        }
                    	Iterator<Element> userElement=userChild.elements().iterator();
                    	int i=1;
                    	while(userElement.hasNext()){
                    		Element userEmt=userElement.next();
                    		
                    		System.out.println("--子节点" + (i++)+" :");
                    		List<Attribute> userEmtAttrList=userEmt.attributes();
                    		if(userEmtAttrList!=null&& userEmtAttrList.size()>0){
                    			for(Attribute userEmtAttr:userEmtAttrList){
                    				System.out.println("----子节点类型：" + userEmtAttr.getStringValue() + " --子节点类型值：" + userEmt.getStringValue());
                    				eleMap.put(userChild.getName()+"_"+userEmtAttr.getStringValue(), userEmt.getStringValue());
                    			}
                    		}
//                    		System.out.println("----节点值：" + userEmt.getStringValue());
                    	}
                    }else{
                    	 List<Attribute> userChildList=userChild.attributes();
                    	 System.out.println("节点名：" + userChild.getName());
                         if(userChildList!=null&&userChildList.size()>0){
                         	for(Attribute userCChild:userChildList){
                         		System.out.println("----属性：" + userCChild.getName() + " --属性值：" + userCChild.getStringValue());
                         		eleMap.put(userCChild.getName(), userCChild.getStringValue());
                         	}
                         }
                    	System.out.println(" --节点值：" + userChild.getStringValue());
                    	eleMap.put(userChild.getName(), userChild.getStringValue());
                    }
                    
                   
                }
                System.out.println("=====结束遍历某个人=====");
                JSONObject nodeJson=JSONObject.fromObject(eleMap);
                System.out.println(nodeJson.toString());
                jsonList.add(nodeJson);
                
            }
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JSONArray nodeArr=JSONArray.fromObject(jsonList.toArray());
        System.out.println(nodeArr.size());
        
	}
	
	
	public static void readXMLFileFromGivenPath(){
//		file = new FileInputStream("src/resources/med_oneuser.xml");
		File file=new File("src/resources/med_oneuser.xml");
        //字符流输出
        BufferedReader reader;
        String string=null;
        StringBuffer sb = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			 while((string = reader.readLine())!=null){
		            sb.append(string);
		        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        XMLSerializer xmlSerializer=new XMLSerializer();
        JSON json=xmlSerializer.read(sb.toString());
        JSONObject xmlJson=JSONObject.fromObject(json);
        String xmlStr=xmlJson.toString();
        //这一句的输出,也许你很快的就知道原理了,其实原理很简单的！
        System.out.println("重点处：\n"+xmlStr+"\n");
//			        
        JSONObject jsonObject=JSONObject.fromObject((json.toString()).substring(1, json.toString().length()-1));    
        System.out.println("截取后：\n"+jsonObject.toString(1)+"\n");
		
	}

}
