package com.dg.jw.pgp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.util.io.Streams;


public class PGPEnDecrypt {
	
//	private static final int BUFFER_SIZE = 1 << 16; // should always be power of 2  
    private static final int KEY_FLAGS = 27;  
    private static final int[] MASTER_KEY_CERTIFICATION_TYPES = new int[]{  
            PGPSignature.POSITIVE_CERTIFICATION,  
            PGPSignature.CASUAL_CERTIFICATION,  
            PGPSignature.NO_CERTIFICATION,  
            PGPSignature.DEFAULT_CERTIFICATION  
    };

	public static void main(String[] args) throws Exception {

	  //输入待解密的加密文件
	  String inputFile ="E:\\myDocs\\medtronic\\Info_Hub_encrypt_1119009.xml";  
	  //密钥
      String privateKeyFile="E:\\myDocs\\medtronic\\prv-key.txt";  
      //输出解密后的明文
      String outputFile="E:\\myDocs\\medtronic\\Info_Hub_1119005.xml";
      String passwd="Medtronic1";
        
      decryptFile(inputFile, outputFile, privateKeyFile, passwd); 
        
//		String plainFileName="E:\\myDocs\\medtronic\\Info_Hub_1119004.xml";
//        String encryptedFileName="E:\\myDocs\\medtronic\\Info_Hub_encrypt_1119009.xml";
//        String publicKeyFileName="E:\\myDocs\\medtronic\\public-key.txt";
//        boolean isArmor=true;
//        boolean withIntegrityCheck=true;
        
//        encryptFile(plainFileName,encryptedFileName,publicKeyFileName,isArmor,withIntegrityCheck);

        
        System.exit(1);
		 
	}
	
	public static void decryptFile(String inputFileName,
						            String outputFileName,
						            String privatekeyFileName,
						            String passwd){
		Security.addProvider(new BouncyCastleProvider());  
		InputStream input = null;
		InputStream privatekeyIn =null;
		OutputStream output =null;

		try {
			input = new BufferedInputStream(new FileInputStream(inputFileName));
			
			privatekeyIn = new BufferedInputStream(new FileInputStream(privatekeyFileName));
			
			//输出解密后的明文
	        File outputFile =new File(outputFileName);
	        if(!outputFile.exists()){
	        	outputFile.createNewFile();
	        }
	        FileOutputStream fos = new FileOutputStream(outputFile); 
	        output = new BufferedOutputStream(fos);
	        
	        char[] passwdCharArr=passwd.toCharArray();
			
			decryptFile(input,output,privatekeyIn, passwdCharArr);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
				try {
					if(output!=null){
						output.close();
					}
					if(privatekeyIn!=null){
						privatekeyIn.close();
					}
					if(input!=null){
						input.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		
		}
	
	@SuppressWarnings("unchecked")  
    private static void decryptFile(InputStream in, OutputStream out, InputStream keyIn, char[] passwd)  
								throws Exception { 
        
        in =PGPUtil.getDecoderStream(in);
  
        PGPObjectFactory pgpF = new PGPObjectFactory(in,new BcKeyFingerprintCalculator());  
        PGPEncryptedDataList enc;  
        Object o = pgpF.nextObject();  
          
        if (o instanceof PGPEncryptedDataList) {  
            enc = (PGPEncryptedDataList) o;  
        } else {  
            enc = (PGPEncryptedDataList) pgpF.nextObject();  
        }  
        Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();  
        PGPPrivateKey sKey = null;  
        PGPPublicKeyEncryptedData pbe = null;  
  
        while (sKey == null && it.hasNext()) {  
            pbe = it.next();  
            sKey = findPrivateKey(keyIn, pbe.getKeyID(), passwd);  
        }  
  
        if (sKey == null) {  
            throw new IllegalArgumentException("Secret key for message not found.");  
        }  
  
        InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));  
        PGPObjectFactory plainFact = new PGPObjectFactory(clear, new BcKeyFingerprintCalculator());  
        Object message = plainFact.nextObject();  
  
        if (message instanceof PGPCompressedData) {  
            PGPCompressedData cData = (PGPCompressedData) message;  
            PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream(), new BcKeyFingerprintCalculator());  
            message = pgpFact.nextObject();  
        }  
  
        if (message instanceof PGPLiteralData) {  
            PGPLiteralData ld = (PGPLiteralData) message;  
            InputStream unc = ld.getInputStream();  
            int ch;  
            while ((ch = unc.read()) >= 0) {  
                out.write(ch);  
            }  
            Streams.pipeAll(unc,out);
            if(unc!=null){
            	unc.close();
            }
           
        } else if (message instanceof PGPOnePassSignatureList) {  
            throw new PGPException("Encrypted message contains a signed message - not literal data.");  
        } else {  
            throw new PGPException("Message is not a simple encrypted file - type unknown.");  
        }  
  
        if(clear!=null){
        	clear.close();
        }
        if (pbe.isIntegrityProtected()) {  
            if (!pbe.verify()) {  
                throw new PGPException("Message failed integrity check");  
            }  
        }  
    }
	
	public static void encryptFile(String inputFileName,String outputFileName,String publicKeyFileName,
            						boolean armor, boolean withIntegrityCheck)
            							throws IOException, NoSuchProviderException, PGPException{
		Security.addProvider(new BouncyCastleProvider());  
		File outputFile =new File(outputFileName);
        if(!outputFile.exists()){
        	outputFile.createNewFile();
        }

	   OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));//加密后的文件
	
	   PGPPublicKey encKey =readPublicKey(publicKeyFileName);//公钥文件
	
	   encryptFile(inputFileName,out, encKey, armor, withIntegrityCheck);
	
	   
	   System.out.println("加密完成");

	}
	
	private static void encryptFile(String inputFileName,OutputStream out,PGPPublicKey encKey,
						            boolean armor,boolean withIntegrityCheck)
						            		throws IOException, NoSuchProviderException{
		if(armor){
				out= new ArmoredOutputStream(out);
		}
		try{
		
			byte[] bytes =compressFile(inputFileName, CompressionAlgorithmTags.ZIP);
			
			PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
			           					 new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
			           					 .setWithIntegrityPacket(withIntegrityCheck)
			                             .setSecureRandom(new SecureRandom())
			                             .setProvider("BC"));
			
			encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));
			
			OutputStream cOut = null;
			if(bytes!=null && bytes.length>0){
				cOut = 	encGen.open(out, bytes.length);
				cOut.write(bytes);
				cOut.flush();
				cOut.close();
			}
			if(out!=null){
			   out.close();
		   }
		} catch(PGPException e){
		
			System.err.println(e);
			
			if(e.getUnderlyingException() != null){
			     e.getUnderlyingException().printStackTrace();
			}
		}
	}	
	
	
	private static PGPPublicKey readPublicKey(String publicKeyFileName)  
            						throws IOException, PGPException {
		
		InputStream publicKeyIn = new BufferedInputStream(new FileInputStream(publicKeyFileName));
		
		PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKeyIn),new BcKeyFingerprintCalculator());  
        
        PGPPublicKey publicKey = null;  
  
        Iterator<PGPPublicKeyRing> rIt = keyRingCollection.getKeyRings();  
  
        while (publicKey == null && rIt.hasNext()) {  
            PGPPublicKeyRing kRing = rIt.next();  
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();  
            while (publicKey == null && kIt.hasNext()) {  
                PGPPublicKey key = kIt.next();  
                if (key.isEncryptionKey()) {  
                    publicKey = key;  
                }  
            }  
        }  
        if (publicKey == null) {  
            throw new IllegalArgumentException("Can't find public key in the key ring.");  
        }  
        if (!isForEncryption(publicKey)) {  
            throw new IllegalArgumentException("KeyID " + publicKey.getKeyID() + " not flagged for encryption.");  
        }  
        if(publicKeyIn!=null){
        	publicKeyIn.close();
        }
  
        return publicKey;  
    }
    
	
	private static PGPPrivateKey findPrivateKey(InputStream keyIn, long keyID, char[] pass)  
            throws IOException, PGPException, NoSuchProviderException  
    {  
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection( PGPUtil.getDecoderStream(keyIn),new BcKeyFingerprintCalculator());  
        return findPrivateKey(pgpSec.getSecretKey(keyID), pass);  
  
    } 
	
	private static PGPPrivateKey findPrivateKey(PGPSecretKey pgpSecKey, char[] pass)  
            throws PGPException {  
        if (pgpSecKey == null) return null;  
        PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pass);  
        return pgpSecKey.extractPrivateKey(decryptor);  
    }
	
	  
	
	 private static boolean isForEncryption(PGPPublicKey key) {  
	        if (key.getAlgorithm() == PublicKeyAlgorithmTags.RSA_SIGN  
	                || key.getAlgorithm() == PublicKeyAlgorithmTags.DSA  
	                || key.getAlgorithm() == PublicKeyAlgorithmTags.ECDH
	                || key.getAlgorithm() == PublicKeyAlgorithmTags.ECDSA) {  
	            return false;  
	        }  
	        return hasKeyFlags(key, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);  
    }
	 
	 @SuppressWarnings("unchecked")  
	 private static boolean hasKeyFlags(PGPPublicKey encKey, int keyUsage) {  
	        if (encKey.isMasterKey()) {  
	            for (int i = 0; i != PGPEnDecrypt.MASTER_KEY_CERTIFICATION_TYPES.length; i++) {  
	                for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(PGPEnDecrypt.MASTER_KEY_CERTIFICATION_TYPES[i]); eIt.hasNext(); ) {  
	                    PGPSignature sig = eIt.next();  
	                    if (!isMatchingUsage(sig, keyUsage)) {  
	                        return false;  
	                    }  
	                }  
	            }  
	        } else {  
	            for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(PGPSignature.SUBKEY_BINDING); eIt.hasNext(); ) {  
	                PGPSignature sig = eIt.next();  
	                if (!isMatchingUsage(sig, keyUsage)) {  
	                    return false;  
	                }  
	            }  
	        }  
	        return true;  
	  }
	 
	 
	 private static boolean isMatchingUsage(PGPSignature sig, int keyUsage) {  
	        if (sig.hasSubpackets()) {  
	            PGPSignatureSubpacketVector sv = sig.getHashedSubPackets();  
	            if (sv.hasSubpacket(PGPEnDecrypt.KEY_FLAGS)) {  
	                if ((sv.getKeyFlags() == 0 && keyUsage == 0)) {  
	                    return false;  
	                }  
	            }  
	        }  
	        return true;  
    }
	 
	 private static byte[] compressFile(String inputFileName, int algorithm) throws IOException{
		 
		   byte[] inputByteArr=null;
	       ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	 
	       PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(algorithm);
	 
	       PGPUtil.writeFileToLiteralData(comData.open(bOut),PGPLiteralData.BINARY,new File(inputFileName));
	       comData.close();
	       if(bOut!=null && bOut.size()>0){
	    	   inputByteArr=bOut.toByteArray();
	    	   bOut.close();
	       }
	       return inputByteArr;
	    }

}
