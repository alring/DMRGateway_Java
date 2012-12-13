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

   public jpcap.NetworkInterface netcard=null;
    
    StationPanel stationPanel;
    Gateway gateway;
    
    GetStatusThread status_thread;
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RadioStationPC.class);
    
    public Status status= new Status();
    
       public class Status
    {
    
    public boolean pttreplyOK=false;
    public boolean releasepttreplyOK=false;
    public boolean callreplyOK=false;
    public boolean connectreplyOK=false;
    public boolean rxtxreplyOK=false;
    public boolean killreplyOK=false;
    public boolean livereplyOK=false;
    
    public boolean remotemonitorOK=false;
    public boolean radiocheckOK=false;
    
    public boolean ReleasePTTInProcess=false;
    
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
    
//    synchronized private void GetRealAdress() 
//    {
//                jpcap.NetworkInterface[] devices = JpcapCaptor.getDeviceList();
//                if(devices==null)return;
//                jpcap.NetworkInterface device= null;
//                for(int i=0;i<devices.length;i++)
//                {
//                if(devices[i].description.equals("Hytera Virtual Rndis")){device=devices[i]; break;}
//                }
//                if(device==null) return;
//        jpcap.NetworkInterfaceAddress[] adresses= device.addresses;
////                    jpcap.NetworkInterfaceAddress[] adresses= devices[i].addresses;
////                    InetAddress srcipaa=null;
//                       
//        InetAddress srcip=null;
//        for(int j=0;j<adresses.length;j++)
//                {
//                if(adresses[j].address instanceof Inet4Address)
//                {
//                    srcip=adresses[j].address; break;
//                }
//                }
//
//        
//        try
//        {
//        
//               JpcapCaptor captor=null;
//               captor = JpcapCaptor.openDevice(device,1024,false,2000);
//               //captor = JpcapCaptor.openDevice(devices[i],1024,false,2000);
//
//        JpcapSender sender=captor.getJpcapSenderInstance();
//        byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
//        InetAddress ip=null;
//
//                ip = InetAddress.getByName(IPAdress);
//
//                ARPPacket arp=new ARPPacket();
//		arp.hardtype=ARPPacket.HARDTYPE_ETHER;
//		arp.prototype=ARPPacket.PROTOTYPE_IP;
//		arp.operation=ARPPacket.ARP_REQUEST;
//		arp.hlen=6;
//		arp.plen=4;
//		arp.sender_hardaddr=device.mac_address;
//		arp.sender_protoaddr=srcip.getAddress();
//		arp.target_hardaddr=broadcast;
//		arp.target_protoaddr=ip.getAddress();
//		
//		EthernetPacket ether=new EthernetPacket();
//		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
//		ether.src_mac=device.mac_address;
//		ether.dst_mac=broadcast;
//		arp.datalink=ether;
//		
//		sender.sendPacket(arp);
//       
//                captor.setFilter("arp",true);
//                
//
//		ARPPacket pack=(ARPPacket)captor.getPacket();
//                if(pack!=null)
//                {
//                RealIPAdress=String.valueOf(pack.sender_hardaddr[2])+"."+String.valueOf(pack.sender_hardaddr[3])+"."+String.valueOf(pack.sender_hardaddr[4])+"."+String.valueOf(pack.sender_hardaddr[5]);
//                ID=(pack.sender_hardaddr[3]<<16)|(pack.sender_hardaddr[4]<<8)|(pack.sender_hardaddr[5]);
//                Subnet=pack.sender_hardaddr[2];
//                }
//                
//                
//
//        }
//        catch(Exception ex)
//        {
//        logger.error(ex);
//        }
//        
//                
//        
//        
//    }
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
