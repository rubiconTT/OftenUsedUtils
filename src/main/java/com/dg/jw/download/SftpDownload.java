package com.dg.jw.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.dg.jw.util.StringUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpDownload {
	
	private static Logger log=Logger.getLogger(SftpDownload.class.getName());
	
//	private final static String PRE_FIX ="/root";
	
	public static File getFileFromRemoteServer(){
		
		String sftpHost="";
		String sftpPort="22";
		int sftpPortNo=Integer.parseInt(sftpPort);
		String sftpUserName="";
		String sftpPasswd="";
		
		ChannelSftp sftp =getSftpConnect (sftpHost, sftpPortNo, sftpUserName, sftpPasswd);  
		
        String remoteFilepathStr = "SFTP_FILE_ORIGINAL_PATH";
        String savePathStr="SFTP_FILE_DOWNLOAD_PATH";
        
        File downloadFile=download(remoteFilepathStr, savePathStr, sftp);
        
        return downloadFile;
	}

	public static ChannelSftp getSftpConnect(final String host, final int port, final String username,  
            									final String password)  {  
        ChannelSftp sftp = null;  
        JSch jsch = new JSch();  
        try {
			jsch.getSession(username, host, port);
			Session sshSession = jsch.getSession(username, host, port);  
	        sshSession.setPassword(password);  
	        Properties sshConfig = new Properties();  
	        sshConfig.put("StrictHostKeyChecking", "no");  
	        sshSession.setConfig(sshConfig);  
	        sshSession.connect();  
	        Channel channel = sshSession.openChannel("sftp");  
	        channel.connect();  
	        sftp = (ChannelSftp) channel; 
		} catch (JSchException e) {
			e.printStackTrace();
		}  
         
        return sftp;  
    } 
	
	public static File download(final String downloadFile, final String saveFile, final ChannelSftp sftp){  
        FileOutputStream os = null;  
        File file = new File(saveFile);  
        try {  
            if (!file.exists()) {  
                File parentFile = file.getParentFile();  
                if (!parentFile.exists()) {  
                    parentFile.mkdirs();  
                }  
                file.createNewFile();  
            }else{
            	//如果旧文件存在，则删除旧文件
            	boolean deleteOldFileRes=file.delete();
            	if(deleteOldFileRes){
            		File parentFile = file.getParentFile();  
                    if (!parentFile.exists()) {  
                        parentFile.mkdirs();  
                    }  
                    file.createNewFile();
            	}else{
            		//如果删除失败，返回Null
            		return null;
            	}
            }  
            os = new FileOutputStream(file);  
            List<String> list = formatPath(downloadFile);  
            //使用sftp下载文件
            if(sftp.isConnected()){
	            sftp.get(list.get(0) + list.get(1), os);  
            }
        } catch (Exception e) {  
            e.printStackTrace();
        } finally {  
            try {
            	if(os!=null){
            		os.close();
            	}
            	if(sftp.isConnected()){
            		sftp.disconnect();
            		exit(sftp);
            	}
            	if(!sftp.isClosed()){
            		exit(sftp);
            	}
			} catch (IOException e) {
				e.printStackTrace();
			}  
        }  
        return file;  
    }
	
	public static byte[] downloadAsByte(final String downloadFile, final ChannelSftp sftp){  
        ByteArrayOutputStream os = new ByteArrayOutputStream();  
        byte[] byteArr=null;
        try {  
            List<String> list = formatPath(downloadFile);  
            sftp.get(list.get(0) + list.get(1), os);  
            byteArr=os.toByteArray(); 
        } catch (Exception e) {
        	exit(sftp);
            e.printStackTrace();  
        } finally {  
            try {
            	if(os!=null){
            		os.close();
            	}
			} catch (IOException e) {
				e.printStackTrace();
			}  
        }  
        return  byteArr;
    } 
	
	public static void rmFile(final String pathString, final ChannelSftp sftp) throws Exception {  
		List<String> list = formatPath(pathString);  
        String dir = list.get(0);  
        String file = list.get(1);  
        if (dirExist(dir + file, sftp)) {  
            sftp.rm(list.get(0) + list.get(1));  
        }
        exit(sftp);
    }
	
	public static void rmDir(final String pathString, final ChannelSftp sftp,
			final boolean recursion) throws SftpException {  
		 
		String fp = formatPath(pathString).get(0);  
        if (dirExist(fp, sftp)) {  
            if (recursion)  
                exeRmRec(fp, sftp);  
            else  
                sftp.rmdir(fp);  
        } 
    }  
	
	public static void uploadFile(final String srcFile, final String dir, final String fileName, final ChannelSftp sftp)  
            throws Exception {
		
        mkdir(dir, sftp);  
        sftp.cd(dir);  
        sftp.put(srcFile, fileName);  
    } 
	
	private static void uploadFileToLinux(final String srcFile,final String fileName, final ChannelSftp sftp)  
            throws Exception {  
        sftp.put(srcFile, fileName);  
    } 
	
	public static void uploadFile(final String srcFile, final ChannelSftp sftp)  {  
        try {  
            File file = new File(srcFile);  
            if (file.exists()) {  
                List<String> list = formatPath(srcFile);
//                if(srcFile.contains("\\")){
//                	uploadFile(srcFile, list.get(0), list.get(1), sftp);
//                }else if(srcFile.contains("/")){
                	uploadFileToLinux(srcFile,list.get(1), sftp);
//                }
            }  
        } catch (Exception e) {  
            exit(sftp);  
            e.printStackTrace();
        }  
    } 
	
	 public static boolean mkdir(final String dir, final ChannelSftp sftp) throws Exception {  
	        try {  
	            if (!StringUtils.isNotEmpty(dir))  
	                return false;  
	            String md = dir.replaceAll("\\\\", "/");  
	            if (md.indexOf("/") != 0 || md.length() == 1)  
	                return false;  
	            return mkdirs(md, sftp);  
	        } catch (Exception e) {  
	            exit(sftp);  
	            throw e;  
	        }  
	    }  
	 
	 private static boolean mkdirs(final String dir, final ChannelSftp sftp) throws SftpException {  
	        String dirs = dir.substring(1, dir.length() - 1);  
	        String[] dirArr = dirs.split("/");  
	        String base = "";  
	        for (String d : dirArr) {  
	            base += "/" + d;  
	            if (dirExist(base + "/", sftp)) {  
	                continue;  
	            } else {  
	                sftp.mkdir(base + "/");  
	            }  
	        }  
	        return true;  
	    } 
	 
	 public static boolean dirExist(final String dir, final ChannelSftp sftp) {  
	        try {  
	            Vector<?> vector = sftp.ls(dir);  
	            if (null == vector)  
	                return false;  
	            else  
	                return true;  
	        } catch (SftpException e) {  
	            return false;  
	        }  
	  }
	 private static void exeRmRec(final String pathString, final ChannelSftp sftp) throws SftpException {  
	        @SuppressWarnings("unchecked")  
	        Vector<LsEntry> vector = sftp.ls(pathString);  
	        if (vector.size() == 1) { // 文件，直接删除  
	            sftp.rm(pathString);  
	        } else if (vector.size() == 2) { // 空文件夹，直接删除  
	            sftp.rmdir(pathString);  
	        } else {  
	            String fileName = "";  
	            // 删除文件夹下所有文件  
	            for (LsEntry en : vector) {  
	                fileName = en.getFilename();  
	                if (".".equals(fileName) || "..".equals(fileName)) {  
	                    continue;  
	                } else {  
	                    exeRmRec(pathString + "/" + fileName, sftp);  
	                }  
	            }  
	            // 删除文件夹  
	            sftp.rmdir(pathString);  
	     }  
	    }
	
	 public static List<String> formatPath(final String srcPath) {  
	        List<String> list = new ArrayList<String>(2);  
	        String repSrc = srcPath.replaceAll("\\\\", "/");  
	        int firstP = repSrc.indexOf("/");  
	        int lastP = repSrc.lastIndexOf("/");  
	        String fileName = lastP + 1 == repSrc.length() ? "" : repSrc.substring(lastP + 1);  
	        String dir = firstP == -1 ? "" : repSrc.substring(firstP, lastP);  
//	        dir = PRE_FIX + (dir.length() == 1 ? dir : (dir + "/"));  
	        dir = (dir.length() == 1 ? dir : (dir + "/"));  
	        list.add(dir);  
	        list.add(fileName);  
	        return list;  
	    }  
	 
	 public static void exit(final ChannelSftp sftp) {  
	        sftp.exit();  
     }
	 
	public static void main(String[] args)throws Exception {

		ChannelSftp sftp =getSftpConnect ("172.30.4.208", 22, "mysftpacc", "Joywok@123");  
//        String pathString = "E:\\myDocs\\medtronic\\Info_Hub_encrypt_1119009.xml";
        String originalFilePath= "/home/mysftpacc/Info_Hub.xml";
        String saveFilePath="E:\\myDocs\\medtronic\\Info_Hub_encrypt_11230010.xml";

//        sftp.cd("/home/mysftpacc/");
//        File file = new File(pathString);  
//        System.out.println("上传文件开始...");  
//        uploadFile(pathString, sftp);  
//        System.out.println("上传成功，开始删除本地文件...");  
//        file.delete();  
//        System.out.println("删除完成，开始校验本地文件...");  
//        if (!file.exists()) {  
        System.out.println("文件不存在，开始从远程服务器获取...");  
        download(originalFilePath, saveFilePath, sftp);  
        System.out.println("下载完成");  
//        } else {  
//            System.out.println("在本地找到文件");  
//        }  
//        exit(sftp);  
        System.exit(0); 
	}

}
