/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enc;


import com.enc.MyNative;
import java.awt.List;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


/**
 *
 * @author Alexandr
 */
public final class Aes128 {
        
        private static Aes128 instance;
        static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MyNative.class);
        private SecretKeySpec skeySpec;
        private Cipher cipher;
        private final static byte[] key = new byte[]{0x01,(byte)0xBC,(byte)0xA1,0x12,(byte)0x99,(byte)0xF0, 0x5C, (byte)0xEE,(byte)0x88,(byte)0x22,(byte)0xF7,0x3D,(byte)0xA5,(byte)0x0C, (byte)0xA0, (byte)0x09};

    public Aes128() 
    {
                try {
        MyNative.GetKey(key);
        skeySpec=new SecretKeySpec(key, "AES");
        cipher=Cipher.getInstance("AES");
                }
                catch(Exception ex)
                {
                 logger.error(ex);
                }
    }
      
        
        
     public static synchronized Aes128 getInstance()
    {
     if(instance==null)
     {
     instance = new Aes128();
     }
     return instance;
    
    }
        
        public void GetKey()
        {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES"); 
            generator.init(128); 
            SecretKey key = generator.generateKey();
        } catch (NoSuchAlgorithmException ex) 
        {
            
        }
        }
         
    public  String encrypt(String text) 
    {
        try {
            byte[] clear= text.getBytes("UTF8");
            instance.cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(clear);
            String s= new String(new Base64().encode(encrypted));
            return s;
            
        } catch (Exception ex) 
        {
           return null;
        }
        
    }

    public  String decrypt(String text) 
    {
        try {
            byte[] encrypted= new Base64().decode(text);  
            instance.cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            String s= new String(decrypted);
            return s;
        } catch (Exception ex) 
        {
          return null;
        }
    }
    
};
