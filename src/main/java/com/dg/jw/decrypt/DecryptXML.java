package com.dg.jw.decrypt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

public class DecryptXML {
	
	public Logger log=Logger.getLogger(DecryptXML.class.getName());
	
	public void decryptXML(String commandStr){
		BufferedReader br = null;  
        try {  
            Process p = Runtime.getRuntime().exec(commandStr);  
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
            String line = null;  
            StringBuilder sb = new StringBuilder();  
            while ((line = br.readLine()) != null) {  
                sb.append(line + "\n");  
            }  
            log.info(sb.toString());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally{  
            if (br != null)  
            {  
                try {  
                    br.close();  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }
	}
	
	public void decryptXMLInLinux(){
		Runtime run = Runtime.getRuntime();
        File wd = new File("/bin");
        log.info("file: "+wd);
        Process proc = null;
	     try {
	         proc = run.exec("/bin/bash", null, wd);
	     } catch (IOException e) {
	          e.printStackTrace();
	     }
	    if (proc != null) {
	         BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	         PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
	         out.println("cd /usr/local/gpgnu");
	         out.println("pwd");
	         out.println("echo -n passphrase:");
	         out.println("read -s passphrase");
	         out.println("gpg --passphrase Medtronic1 -o med_decrypt_5 --decrypt med_RTG_Employee_0902.xml.gpg");
//	         out.println("echo Medtronic1|passwd --stdin");
	         out.println("exit");
	        try {
	            String line;
	            while ((line = in.readLine()) != null) {
	            	log.info("line: "+line);
	            }
	            proc.waitFor();
	             
	         } catch (Exception e) {
	            e.printStackTrace();
	         }finally{  
	        	 try {
	        		 if(in!=null){
	        			 in.close();
	        		 }
	        		 if(out!=null){
	        			 out.close();
	        		 }
	        		 if(proc!=null){
	        			 proc.destroy();
	        		 }
				} catch (IOException e) {
					e.printStackTrace();
				}  
	         }
	    }
	}

	public static void main(String[] args) {
		String commandStr="gpg --passphrase Medtronic1 --output E:\\myDocs\\medtronic\\DB\\medtronic_org_1102.xml --decrypt E:\\myDocs\\medtronic\\DB\\WD_INT397_GC_INFOHUB1102.xml";
		DecryptXML dx=new DecryptXML();
		dx.decryptXML(commandStr);


		System.exit(0);
	}

}
