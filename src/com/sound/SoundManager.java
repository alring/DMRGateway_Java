/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sound;

import com.gateway.Settings;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexandr
 */
public class SoundManager 
{
  //      AudioInputStream microphoneStream; 
  //      AudioInputStream speakerStream; 
        TargetDataLine microphoneLine; 
        SourceDataLine speakerLine; 
        //AudioFormat RtpAudioFormat; 
        AudioFormat audioFormat;
        static String MEDIA_DIR="Records";
        static Logger logger = Logger.getLogger(SoundManager.class);
        //
        public List<SoundManager.InLine> in_lines= new ArrayList<SoundManager.InLine>(); 
        public List<SoundManager.OutLine> out_lines= new ArrayList<SoundManager.OutLine>(); 
        public List<SoundDevice> sounddevices;


    
    public SoundManager()
    {
        //RtpAudioFormat = new AudioFormat(AudioFormat.Encoding.ULAW, 8000, 8, 1,1,8000,false);
        // linear PCM 8kHz, 16 bits signed, mono-channel, little endian
        audioFormat = new AudioFormat(8000, 16, 1, true, false);
      //  this.rtp=rtp;
           RefreshAudioDevices();
           
           microphoneLine=GetDefaultMicLine();
           speakerLine=GetDefaultSpeakLine();
           sounddevices= Settings.getInstance().sounddevices;
           
    }

            public void SaveSoundDevice(String ip, String name,int type)
        {
            boolean finded=false;
            for(int i=0;i<Settings.getInstance().sounddevices.size();i++)
            {
              if(Settings.getInstance().sounddevices.get(i).ip.equals(ip) && Settings.getInstance().sounddevices.get(i).Type==type){Settings.getInstance().sounddevices.get(i).Name=name;finded=true; break;};
                
            }
            if(!finded)
            {
             Settings.getInstance().sounddevices.add(new SoundDevice(ip, name, type));
            }
        
            Settings.Save();
        }
    
   synchronized public void RefreshAudioDevices()
    {
             //Info objects - the first for capturing microphone sound 
         DataLine.Info microphoneInfo = new DataLine.Info(TargetDataLine.class, audioFormat); 
         //and the second for listening to the other side in your speakers 
         DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat); 
         in_lines.clear();
         out_lines.clear();
       Mixer.Info[] infos=AudioSystem.getMixerInfo();
       for(int i=0;i<infos.length;i++)
        {
           Mixer mixer=AudioSystem.getMixer(infos[i]);        
            try {
                TargetDataLine in_line=(TargetDataLine)mixer.getLine(microphoneInfo);
                in_lines.add(new SoundManager.InLine(mixer, microphoneInfo));

            } 
            catch(Exception ex)
            {
            logger.debug(ex);
            }
           try {
                SourceDataLine out_line=(SourceDataLine)mixer.getLine(speakerInfo);
                out_lines.add(new SoundManager.OutLine(mixer, speakerInfo));
            } 
            catch(Exception ex)
            {
            logger.debug(ex);
            }

        }
    }
    
          synchronized  TargetDataLine GetMicLine(int index)
     {
            TargetDataLine microphoneline=null;
           try
         {
        microphoneline = (TargetDataLine) in_lines.get(index).mixer.getLine(in_lines.get(index).MicrophoneInfo); 
         }
              catch (Exception e) 
         { 
             logger.error(e);
         } 
         return microphoneline;
     
     }
       synchronized    SourceDataLine GetSpeakLine(int index)
     {
              SourceDataLine speakerline=null;
          try { 
         speakerline = (SourceDataLine) out_lines.get(index).mixer.getLine(out_lines.get(index).SpeakerInfo); 
          }
                   catch (Exception e) 
         { 
             logger.error(e);
         }
          return speakerline;
     }
            
    
        TargetDataLine GetDefaultMicLine()
     {
                  //Info objects - the first for capturing microphone sound 
         DataLine.Info microphoneInfo = new DataLine.Info(TargetDataLine.class, audioFormat); 
         TargetDataLine microphoneline=null;
         try
         {
        microphoneline = (TargetDataLine) AudioSystem.getLine(microphoneInfo); 
         }
              catch (Exception e) 
         { 
             logger.error(e);
         } 
         return microphoneline;
     }
     
           SourceDataLine GetDefaultSpeakLine()
     {

         DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat); 
         SourceDataLine speakerline=null;
          try { 
         speakerline = (SourceDataLine) AudioSystem.getLine(speakerInfo); 
          }
                   catch (Exception e) 
         { 
             logger.error(e);
         }
          return speakerline;
          
     }
           
           public int GetMicLineIndexByName(String name)
           {
               
               for(int i=0;i<in_lines.size();i++)
               {
               if(in_lines.get(i).Name.equals(name)) {return i;}
               }
               return -1;
           
           }
           
            public int GetSpeakLineIndexByName(String name)
           {
                 for(int i=0;i<out_lines.size();i++)
               {
               if(out_lines.get(i).Name.equals(name)) {return i;}
               
               }
                 return -1;
           }
            
           public void  SaveSelectedMic(String name)
            {
             
            }
           
           public void  SaveSelectedSpeak(String name)
            {
            
            }
            
          
//            public TargetDataLine GetMicLine()
//     {
//
//         return microphoneLine;
//     }
//     
//          public SourceDataLine GetSpeakLine()
//     {
//
//          return speakerLine;
//          
//     }
           
            synchronized  public String[] GetSreakerList()
     {
               String[] list= new String[out_lines.size()];
               for(int i=0;i<list.length;i++)
               {
               list[i]=out_lines.get(i).Name;
               }
               return list;
     }
              
            synchronized  public String[]  GetMicrophoneList()
     {
               String[] list= new String[in_lines.size()];
               for(int i=0;i<list.length;i++)
               {
               list[i]=in_lines.get(i).Name;
               }
               return list;
     }

    public int getMicrophonIndexByIP(String ip) {
                 for(int i=0;i<sounddevices.size();i++)
               {
               if(sounddevices.get(i).ip.equals(ip)&& sounddevices.get(i).Type==0) 
               {
                   return GetMicLineIndexByName(sounddevices.get(i).Name);
               }
               
               }
                 return -1;
    }

    public int getSpeakerIndexByIP(String ip) {
                 for(int i=0;i<sounddevices.size();i++)
               {
               if(sounddevices.get(i).ip.equals(ip)&& sounddevices.get(i).Type==1) 
               {
                   return GetSpeakLineIndexByName(sounddevices.get(i).Name);
               }
               
               }
                 return -1;
    }
              
          
          

        class InLine 
    {
            //public TargetDataLine Line;
            //public Mixer Mixer;
            public String Name;
            Mixer mixer;
            DataLine.Info MicrophoneInfo;
            
            public InLine(Mixer mixer,DataLine.Info info)
            {
                 this.mixer= mixer;
                 MicrophoneInfo= info;
                String name=mixer.getMixerInfo().getName();
            try {
                Name=new String(name.getBytes("cp1252"), "cp1251");
            } catch (UnsupportedEncodingException ex) {
                
            }
                
            }
    }
        class OutLine 
    {
            public String Name;
            Mixer mixer;
            DataLine.Info SpeakerInfo;
            
             public OutLine(Mixer mixer,DataLine.Info info)
            {
                this.mixer= mixer;
                 SpeakerInfo= info;
                String name=mixer.getMixerInfo().getName();
            try {
                Name=new String(name.getBytes("cp1252"), "cp1251");
            } catch (UnsupportedEncodingException ex) {
                
            }
            } 
    }
        
        
        
        
}