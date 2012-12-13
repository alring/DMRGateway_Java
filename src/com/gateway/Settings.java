/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import com.sound.SoundDevice;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexandr
 */
public class Settings 
{
    private static Settings instance;
    Properties properties = new Properties();
    static Logger logger = Logger.getLogger(Settings.class);
    public int serverPort;
    public String serverIP;
    public List<SoundDevice> sounddevices;
    
//    public String baseName;
//    public String baseIP;
//    public int basePort;
//    public String login;
//    public String password;
    
    private Settings()
    {
        properties= new Properties();
                try {
            InputStream is              = new FileInputStream("settings.ini");
            properties.load(is);
            is.close();
             } catch (IOException ex) {
            logger.error(ex);
        }
                
            serverPort= Integer.parseInt(properties.getProperty("ServerPort","6767"));
            serverIP   = properties.getProperty("ServerIp", "127.0.0.1");
            String speaker   = properties.getProperty("Speaker", "");
            String microphone   = properties.getProperty("Microphone", "");
            sounddevices= new ArrayList<SoundDevice>();
            //speaker
            String[] sp_dev= speaker.split(",");
            if(sp_dev.length>0)
            {
            for(int i=0;i<sp_dev.length;i++)    
            try
            {
            String name=sp_dev[i].split(":")[1];
            String ip=sp_dev[i].split(":")[0];
            sounddevices.add(new SoundDevice(ip, name, 1));
            }
            catch(Exception ex)
            {
            logger.error(ex);
            }
                    
            
            }
            //speaker
            String[] mic_dev= microphone.split(",");
            if(mic_dev.length>0)
            {
            for(int i=0;i<mic_dev.length;i++)    
            try
            {
            String name=mic_dev[i].split(":")[1];
            String ip=mic_dev[i].split(":")[0];
            sounddevices.add(new SoundDevice(ip, name, 0));
            }
            catch(Exception ex)
            {
            logger.error(ex);
            }
                    
            
            }
            
            
//            baseName  = properties.getProperty("BaseName", "dmrdb");
//            baseIP  = properties.getProperty("BaseIP", "127.0.0.1");
//            basePort= Integer.parseInt(properties.getProperty("BasePort","5432"));
//            login    = properties.getProperty("BaseLogin", "postgres");
//            password  = properties.getProperty("BasePassword", "postgres");
    }
    
    public static synchronized Settings getInstance()
    {
     if(instance==null)
     {
     instance = new Settings();
     }
     return instance;
    
    }
    
    
        public static synchronized void Save()
    {
        try {
            instance.properties.setProperty("ServerPort", String.valueOf(instance.serverPort));
            instance.properties.setProperty("ServerIp", instance.serverIP);
            
            String speaker   ="" ;       
            String microphone   = "";
            
                        for(int i=0;i<instance.sounddevices.size();i++)  
                        {
                        if(instance.sounddevices.get(i).Type==0)
                        {
                        if(!microphone.equals(""))microphone+=",";    
                        microphone+=instance.sounddevices.get(i).ip+":"+instance.sounddevices.get(i).Name;
                        
                        }
                        else
                        {
                        if(!speaker.equals(""))speaker+=",";    
                        speaker+=instance.sounddevices.get(i).ip+":"+instance.sounddevices.get(i).Name;
                        }
                        
                        }
            instance.properties.setProperty("Speaker",speaker);
            instance.properties.setProperty("Microphone", microphone);
            
            
//            instance.properties.setProperty("BaseName", instance.baseName);
//            instance.properties.setProperty("BaseIP", instance.baseIP);
//            instance.properties.setProperty("BasePort",String.valueOf(instance.basePort));
//            instance.properties.setProperty("BaseLogin", instance.login);
//            instance.properties.setProperty("BasePassword",instance.password);
            
            
            instance.properties.store(new FileOutputStream("settings.ini"), null);
        } catch (IOException ex) {
            logger.error(ex);
        }
    
    }
        

        
    
    
}
