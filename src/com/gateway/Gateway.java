/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import com.rtp.RtpMediaSession;
import com.sound.SoundManager;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexandr
 */
public class Gateway 
{
    MainWindow mainWindow;
    List<RadioStationPC>radioStationsPC;
    List<RadioStation> radioStations ;
    
            /* Max added */
    
    RRSService rrsService;
    RCCService rccService;
    MessageService messageService;
    LocationService locationService;
    DTService dataService;
    List<BusyGate> busyGate;
   
    SoundManager soundManager;
    
    ClientToServer client;
    static Logger logger = Logger.getLogger(Gateway.class);
    boolean ServerIsOnline=false;
    Thread state;

    long lastNetCheckTime=0;
    long soundCheckTimer=Calendar.getInstance().getTimeInMillis();
    public boolean serverHasLimit=false;

    public Gateway(MainWindow mainWindow) 
    {
        this.mainWindow= mainWindow;
        soundManager= new SoundManager();
        
        client= new ClientToServer(this);
       // client.Connect();
        //
        radioStationsPC= new ArrayList<RadioStationPC>();
        radioStations = new ArrayList<RadioStation>();
        
        //radioStationsPC.add(new RadioStationPC("192.168.10.60",this));
    
        

    }
//    public interface  CallProtocol extends Library
//    {
//       public void CCallInterface();
//       public void OpenSocket(int ip,short port,int id,byte subnet);
//       public void ConfigurateTXRX(byte tx,byte rx);
//    }
    
   public void Start()
   {

        messageService= new MessageService(3004,this);
        messageService.Start();
        rrsService= new RRSService(3002,this);
        rrsService.Start();
        rccService= new RCCService(3005,this);
        rccService.Start();
        locationService= new LocationService(3003, this);
        locationService.Start();
        dataService= new DTService(3007, this);
        dataService.Start();
        state= new Thread(new StateReader());
        state.start();
        
        lastNetCheckTime=Calendar.getInstance().getTimeInMillis();
   }
   
   public void RefreshGatewayPanels()
   {
       mainWindow.centerPanel.removeAll();
       mainWindow.centerPanel.validate();
           for(int i=0;i<radioStationsPC.size();i++)
        {
            mainWindow.centerPanel.add(radioStationsPC.get(i).GetPanel());
            radioStationsPC.get(i).GetPanel().CollapsPanel.RefreshSelect();
        } 
         mainWindow.validate();
   
   }


    
     public RadioStationPC GetRadiostatinPCBySubnet(int sn)
     {
          for(int i=0;i<radioStationsPC.size();i++)
     {
     if(radioStationsPC.get(i).Subnet==sn)return radioStationsPC.get(i);
     }
     return null;
     }
          public RadioStationPC GetRadiostatinPCByRealIP(String ip)
     {
          for(int i=0;i<radioStationsPC.size();i++)
     {
     if(radioStationsPC.get(i).RealIPAdress.equals(ip))return radioStationsPC.get(i);
     }
     return null;
     }
     
       public RadioStationPC GetRadiostatinPCByIP(String ip)
     {
          for(int i=0;i<radioStationsPC.size();i++)
     {
     if(radioStationsPC.get(i).IPAdress.equals(ip))return radioStationsPC.get(i);
     }
     return null;
     }
          
      public RadioStation GetRadiostatinByID(int id)
     {
     for(int i=0;i<radioStations.size();i++)
     {
     if(radioStations.get(i).ID==id)return radioStations.get(i);
     }
     return null;
     }
      
      synchronized public boolean NetCardIsConnected(jpcap.NetworkInterface device)
      {
                jpcap.NetworkInterface[] devices = JpcapCaptor.getDeviceList();
                if(devices==null)return false;
                
                for(int i=0;i<devices.length;i++)
                {
                    if(devices[i].name.equals(device.name)){return true;}
                }
                return false;
      }
      
        synchronized public void GetRealAdress(RadioStationPC radioStationPC) 
    {
        boolean hasStation=false;
        try
        {
                jpcap.NetworkInterface[] devices = JpcapCaptor.getDeviceList();
                if(devices==null)return;
                
                for(int i=0;i<devices.length;i++)
                {
                    logger.warn("descr: " + devices[i].description);
                jpcap.NetworkInterface device= null;
                if(devices[i].description.indexOf("Hytera Virtual Rndis")!=-1){device=devices[i];}
                
                if(device==null) 
                {
 
                    continue;
                }
                hasStation=true;
                lastNetCheckTime=Calendar.getInstance().getTimeInMillis();
                jpcap.NetworkInterfaceAddress[] adresses= device.addresses;
//                    jpcap.NetworkInterfaceAddress[] adresses= devices[i].addresses;
//                    InetAddress srcip=null;
                       
        InetAddress srcip=null;
        for(int j=0;j<adresses.length;j++)
                {
                if(adresses[j].address instanceof Inet4Address)
                {
                    srcip=adresses[j].address; break;
                }
                }

               if(srcip==null)continue;
               byte[] host_ip_b= srcip.getAddress();
               String host_ip=(host_ip_b[0]&0xFF)+"."+(host_ip_b[1]&0xFF)+"."+(host_ip_b[2]&0xFF)+"."+((host_ip_b[3]&0xFF)-1);
               if(!host_ip.equals(radioStationPC.IPAdress))
                   continue;
        
        try
        {
        
               JpcapCaptor captor=null;
               captor = JpcapCaptor.openDevice(device,1024,false,2000);
               //captor = JpcapCaptor.openDevice(devices[i],1024,false,2000);

        JpcapSender sender=captor.getJpcapSenderInstance();
        byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
        InetAddress ip=null;

                ip = InetAddress.getByName(radioStationPC.IPAdress);

                ARPPacket arp=new ARPPacket();
		arp.hardtype=ARPPacket.HARDTYPE_ETHER;
		arp.prototype=ARPPacket.PROTOTYPE_IP;
		arp.operation=ARPPacket.ARP_REQUEST;
		arp.hlen=6;
		arp.plen=4;
		arp.sender_hardaddr=device.mac_address;
		arp.sender_protoaddr=srcip.getAddress();
		arp.target_hardaddr=broadcast;
		arp.target_protoaddr=ip.getAddress();
		
		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac=device.mac_address;
		ether.dst_mac=broadcast;
		arp.datalink=ether;
		
		sender.sendPacket(arp);
                captor.setFilter("arp",true);
                
//                for(int i=0;i<2;i++)
//                {
		ARPPacket pack=(ARPPacket)captor.getPacket();
                if(pack!=null)
                if((String.valueOf(pack.sender_protoaddr[0]&0xFF)+"."+String.valueOf(pack.sender_protoaddr[1]&0xFF)+"."+String.valueOf(pack.sender_protoaddr[2]&0xFF)+"."+String.valueOf(pack.sender_protoaddr[3]&0xFF)).equals(radioStationPC.IPAdress))       
                {
                radioStationPC.RealIPAdress=String.valueOf(pack.sender_hardaddr[2]&0xFF)+"."+String.valueOf(pack.sender_hardaddr[3]&0xFF)+"."+String.valueOf(pack.sender_hardaddr[4]&0xFF)+"."+String.valueOf(pack.sender_hardaddr[5]&0xFF);
//                if(!InetAddress.getByName(radioStationPC.RealIPAdress).isReachable(1000)) continue;
                radioStationPC.ID=(pack.sender_hardaddr[3]<<16)&0xFF0000|(pack.sender_hardaddr[4]<<8)&0xFF00|(pack.sender_hardaddr[5]&0xFF);
                radioStationPC.Subnet=pack.sender_hardaddr[2]&0xFF;
                radioStationPC.netcard=devices[i];
//                break;
                }
                else
                {
                radioStationPC.RealIPAdress="";
                radioStationPC.Subnet=-1;
                }
                
//                }
                captor.close();

        }
        catch(Exception ex)
        {
        logger.error(ex.getMessage(),ex.fillInStackTrace());
        }
                }
               }
        catch(UnsatisfiedLinkError er)
        {
        logger.error(er);
        new WarningWindow(mainWindow, "Не найдена библиотека Jpcap.dll");
        }
         catch(NoClassDefFoundError er)
        {
        logger.error(er);
    //    new ErrorWindow();
        }  
        if(!hasStation)
        {
                                if((Calendar.getInstance().getTimeInMillis()-lastNetCheckTime)/1000 >30)
                    {
                        if(mainWindow.isEnabled())new WarningWindow(mainWindow, "Не найдена сетевая карта станции.<br> Станция не подключена или после установки драйверов требуется перезагрузка компьютера.");
                        lastNetCheckTime=Calendar.getInstance().getTimeInMillis();
                    
                    }
        }
        
    }
      
 
      
     public class StateReader implements Runnable
    {

        @Override
        public void run() 
        {
            while(!Thread.interrupted())
            { 
              if(client.IsConnected)
              {
             //     locationService.StartReport(123456);
             //    locationService.StartReport(360013);
              if(!ServerIsOnline)
              {
                  
                  mainWindow.SetOnline(true);
                  ServerIsOnline=true;
                  client.SendMyTypeToServer(1);
                        try 
                        {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) 
                        {
                            
                        }
                  client.GetRadioGatewaysFromServer();
                  
              }
              }
              else
              {
                  
                if(ServerIsOnline)
              {
                  mainWindow.SetOnline(false);
                  ServerIsOnline=false;
                  if(serverHasLimit)
                        try {
                            Thread.sleep(10000);
                            serverHasLimit=false;
                        } catch (InterruptedException ex) {
                            logger.error(ex);
                        }
                  
              }
                client.Connect();
                                
              }
              
              
              
              for(int i=0;i<radioStations.size();i++)
              {
                  
                 long timenow=Calendar.getInstance().getTimeInMillis(); 
              if( (radioStations.get(i).IsOnline)  )
                  if(((timenow-radioStations.get(i).registerTime)/1000)>3000)
                  {
                      if( (radioStations.get(i).timeToLineBeforeOffline==0)) // Если истекло время опроса РC
                      {    
                       if(radioStations.get(i).timeToLive == 0) continue;                                         // отключаем ее на сервере, но продолжаем опрашивать,
                       client.SendMobileRadioStateToServer(radioStations.get(i).ID, 2, radioStations.get(i).PcRadioIPAdress); // пока не истечет время timeTolive
                       logger.info("Отключение объекта ID="+radioStations.get(i).ID+", превышено время ожидания регистрации.");
                       radioStations.get(i).needRefresh = false;
                       radioStations.get(i).timeToLineBeforeOffline = -1; 
                      }
                      else
                      {  
                       radioStations.get(i).registerTime = Calendar.getInstance().getTimeInMillis(); 
                       radioStations.get(i).needRefresh = true;
                       radioStations.get(i).timeToLive--; 
                       radioStations.get(i).timeToLineBeforeOffline--; 
                      }
                      if (radioStations.get(i).timeToLive == 0)       
                      {
                       radioStations.get(i).IsOnline=false;
                       logger.info("Прекращены запросы к объекту: " + radioStations.get(i).ID);
                       radioStations.get(i).needRefresh = false;
                      }
                  }
              }
              
              
              

            try 
            {
               Thread.sleep(1000);
            } catch (Exception ex) 
            {
             logger.error(ex);       
            }
           
        } 
   
        }
  }
    
    
}