package com.enc;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.apache.log4j.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Alexandr
 */
public class MyNative 
{
          static Logger logger = Logger.getLogger(MyNative.class);
          public interface CLibrary extends Library 
          {

          public void GetKey(byte[] data,int size);
          }
          
    
          public static void GetKey(byte[] b)
          {
             try
             {
           CLibrary lib = (CLibrary) Native.loadLibrary("dmrnative", CLibrary.class);
           lib.GetKey(b,b.length);
             }
             catch(Exception e)
             {
             logger.error(e.getMessage(),e.fillInStackTrace());
             }
                     
   
          }
          
}
