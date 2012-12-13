/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sound;

import com.gateway.WarningWindow;
import com.rtp.RtpMediaSession;
import com.rtp.RtpPacket;
import java.io.*;
import java.util.Date;
import javax.sound.sampled.*;
import org.apache.log4j.Logger;


/**
 *
 * @author Alexandr
 */
public class SoundInOut 

{
        public TargetDataLine microphoneLine; 
        public SourceDataLine speakerLine; 
  //      RadioStationPC radioStationPC;
        static Logger logger = Logger.getLogger(SoundInOut.class);
        AudioFormat audioFormat = new AudioFormat(8000, 16, 1, true, false);
        Thread t;
       	    // аудиформат
        File RecordFile;
        ByteArrayOutputStream RecorderStream= new ByteArrayOutputStream();
        RtpMediaSession session;
        
                  public SoundInOut(RtpMediaSession session) 
    {
        this.session=session;
          //          this.radioStationPC=session.radioStationPC;
    }
                  

        
           public void PlayPacket(RtpPacket rtpPacket)
   {
          byte[] b = rtpPacket.getPayload(); 
          b=PcmuDecoder.process(b);
         if (speakerLine != null) 
         { 
             speakerLine.write(b, 0, b.length); 
         } 
         
         
   }
           
          public void OpenMicLine() 
    {
        if(speakerLine!=null|| microphoneLine!=null)closeLines();

        microphoneLine=session.radioStationPC.GetSoundmanager().GetMicLine(session.radioStationPC.selected_mic); 
        if(microphoneLine==null)return;
     //   speakerLine=session.soundManager.GetSpeakLine(session.soundManager.selected_speak); 
         try { 

             microphoneLine.open(audioFormat);       
            // microphone
             microphoneLine.start(); 
             //speaker line 
      //       speakerLine.open(audioFormat); 
      //       speakerLine.start(); 
  
         } 
         catch (Exception e) 
         { 
             logger.error(e);
             logger.error("Шлюз:Ошибка открытия устройства ввода");
             return;
            // new WarningWindow(null, "Шлюз:Ошибка открытия устройства ввода");
         } 
         
//       if(t!=null && !t.isInterrupted()) 
//       {
//       t.interrupt();
//       logger.info("Stop input audio device thread command");
//       }
         
        // SoundInOut.GetFromMicrophone catchmic= new SoundInOut.GetFromMicrophone(microphoneLine);
         
         t= new Thread(new SoundInOut.GetFromMicrophone(microphoneLine));
         t.start();
         
    }     
                    public void OpenSpeakLine() 
    {
        if(speakerLine!=null|| microphoneLine!=null)closeLines();

   //     microphoneLine=session.soundManager.GetMicLine(session.soundManager.selected_mic); 
        speakerLine=session.radioStationPC.GetSoundmanager().GetSpeakLine(session.radioStationPC.selected_speak); 
         if(speakerLine==null)return;
         try { 

    //         microphoneLine.open(audioFormat);       
            // microphone
    //         microphoneLine.start(); 
             //speaker line 
             speakerLine.open(audioFormat); 
             speakerLine.start(); 
  
         } 
         catch (Exception e) 
         { 
             logger.error(e);
             logger.error("Шлюз:Ошибка открытия устройства вывода");
            //new WarningWindow(null, "Шлюз:Ошибка открытия устройства вывода");
         } 
      //   GetFromMicrophone catchmic= new GetFromMicrophone(microphoneLine);
      //   t= new Thread(catchmic);
      //   t.start();
         
    }     
          
             public void closeLines() 
    {
       if(t!=null ) //&& !t.isInterrupted()
       {
       t.interrupt();
      // logger.info("Stop input audio device thread command");
       
       }
       else
       {
       int i=0;
       }
//        if (microphoneStream != null) {
//            try {
//                microphoneStream.close();
//            } catch (IOException e) {
//                System.out.println(e.toString());
//            }
//            microphoneStream = null;
//        }
//        if (speakerStream != null) {
//            try {
//                speakerStream.close();
//            } catch (IOException e) {
//                System.out.println(e.toString());
//            }
//            speakerStream = null;
//        }
       try
       {
        if (microphoneLine != null) {
            microphoneLine.stop();
            microphoneLine.close();
            microphoneLine = null;
        }
        if (speakerLine != null) {
            speakerLine.drain();
            speakerLine.stop();
            speakerLine.close();
            speakerLine = null;
        }
       }
       catch(Exception ex)
       {
       logger.error(ex);
       }
    }
           
           
      class GetFromMicrophone implements Runnable 
    {
     TargetDataLine line;    
     int numBytesRead;
     
    public GetFromMicrophone(TargetDataLine line)
    {
        this.line=line;

    }
    

        @Override
        public void run() 
        {
          //  logger.info("input audio device thread is start");
            while (!Thread.interrupted()) {
             
                     //Read from microphone
                        if(line==null)break;
                            //return;
                        if(Thread.interrupted())break;
                        int ready = line.available();
                        while (ready <320) 
                        {
                         if(Thread.interrupted()){
                         //    logger.warn("input audio device thread is closed");
                             return;}
                        try {
                            Thread.sleep(1);
                            ready = line.available();
                        } catch (InterruptedException e) {
                           // logger.warn("input audio device thread is closed");
                            return;
                        }
             
                    }
                    if(ready>1024)ready=(1024/audioFormat.getFrameSize())*audioFormat.getFrameSize();
                        
                    byte[] buffer = new byte[320];
                    numBytesRead= line.read(buffer, 0, buffer.length);
                    if(speakerLine!=null)speakerLine.write(buffer, 0, buffer.length); 
                    byte[] b=  PcmuEncoder.process(buffer);
                    if(microphoneLine!=null)session.Send(b); 
        }
     //logger.info("input audio device thread is closed");
    
}
}
    
}
