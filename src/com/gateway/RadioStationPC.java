/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import com.rtp.RtpMediaSession;
import com.sound.SoundManager;
import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;

/**
 *
 * @author Alexandr
 */
public class RadioStationPC 
{
    int ID;
    String Name;
    public String  IPAdress;//="192.168.10.60";
    public String  RealIPAdress;
    public  int Subnet;
    boolean Online;
    boolean Connected=false;  //установленно HTNP соединение    
    boolean lastState=false;
    public boolean IsBusy;
    int offline_count=0;
    public  RtpMediaSession rtpMediaSession;
    public int selected_mic=0;
    public int selected_speak=0;
    public boolean accepted_ptt = false;
    public boolean stopDeferred = false; // переменная, по которой определяется завершение опроса убийства
    
    
   public jpcap.NetworkInterface netcard=null;
    
    StationPanel stationPanel;
    Gateway gateway;
    
    GetStatusThread status_thread;
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RadioStationPC.class);
    
    public Status status= new Status();
    public typeOperation TypeOperation  = new typeOperation();
       public class Status
    {
    
     /*
    public boolean pttreplyOK=false;
    public boolean releasepttreplyOK=false;
    public boolean callreplyOK=false;
    */
    public boolean connectreplyOK=false;
    public boolean rxtxreplyOK=false;
    public boolean killreplyOK=false;
    public boolean livereplyOK=false;
    
    public boolean remotemonitorOK=false;
    public boolean radiocheckOK=false;
    
   // public boolean ReleasePTTInProcess=false;
           
    public boolean pttPressACK = false;
    public boolean pttReleaseACK = false;
    public boolean generationCallACK = false;
    public boolean txModeOnly = false;       
    public int currentButtonOperation = 0;       
    public boolean stopDeferredKill = false;       
    }
    
   
    public class typeOperation
    {
        public  int PRESS_PTT = 1;
        public  int RELEASE_PTT = 2;
        
        public typeOperation(){}
    }
    

    public RadioStationPC(String ip, Gateway gateway)
    {
        this.gateway=gateway;
        rtpMediaSession= new RtpMediaSession(this);
        RealIPAdress="";
        IPAdress=ip;
        status_thread= new GetStatusThread();
        status_thread.start();
        

        stationPanel= new StationPanel(this);
        IsBusy=false;
        
        
    }
    
        public StationPanel GetPanel() 
    {
      return stationPanel;
    }

        
    public boolean IsOnline()
    {
    
        return Online;
    }
        public String GetRealIP()
    {
    
        return RealIPAdress;
    }
          public String GetIP()
    {
    
        return IPAdress;
    }
        public long GetId()
    {
         if(!RealIPAdress.equals(""))
         {
         return ID;
         }
         return 0;
    }
        
     synchronized boolean getAcceptPtt(){return accepted_ptt;}
     synchronized void setAcceptPtt(boolean new_state){accepted_ptt = new_state;}
        
       public SoundManager GetSoundmanager()
    {
    
         return gateway.soundManager;
    }
       
    public RadioStationPC GetRadioStationPC()
    {
    
         return this;
    }
        
        
        
    
        public class GetStatusThread extends Thread
    {
    //  byte[] receiveData = new byte[64];
       
        @Override
        public void run() {
           while(!Thread.interrupted())
            {   
                try 
                {
                if(RealIPAdress.isEmpty())
                {
                  //GetRealAdress();
                   gateway.GetRealAdress(GetRadioStationPC());
                }
                else
                {
      
               // boolean state=InetAddress.getByName(RealIPAdress).isReachable(2000);
                
               //ping
                    boolean state=gateway.NetCardIsConnected(netcard);

     

                    
                if(state!=lastState && state==true)
                {
                    lastState=state;
                    Online=state;
                    stationPanel.Refresh();
                    gateway.rccService.EnableRXTX(RealIPAdress);
                    
                    gateway.client.SendRadioGatewayStateToServer(IPAdress, 1);
                    logger.info("Подключена базовая станция ID="+ID+" IP="+IPAdress); 
                    
              //      gateway.dataService.SendData("10.1.226.64", RealIPAdress, new String("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890").getBytes());
                }

                if(state==false)
                {
                offline_count++;
                if(offline_count>2)
                {
                    RealIPAdress="";
                    stationPanel.SetState("?");
                    lastState=state;
                    Online=state;
                    stationPanel.Refresh();
                    gateway.client.SendRadioGatewayStateToServer(IPAdress, 0);
                    logger.info("Базовая станция отключена ID="+ID+" IP="+IPAdress); 
                    offline_count=0;
                }
                }
                else
                {
                    offline_count=0;
                }
                }
                    
                    if(!RealIPAdress.isEmpty())
                    Thread.sleep(2000);
                    else 
                    Thread.sleep(2000);
                } 
                catch (InterruptedException ex) 
                {
                    break;
                }
                catch(Exception ex)
                {
                  logger.error(ex);
                }

            }
        }
    
    }


    
    
}
