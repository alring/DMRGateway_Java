/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import sun.awt.windows.ThemeReader;

/**
 *
 * @author Alexandr
 */
public class RCCService {
    int Port;
    DatagramSocket socket;
    DatagramPacket receivePacket;
    DataReceiver dataReceiver;
    byte[] receiveData = new byte[64];
    byte[] sendData = new byte[1024];
    Gateway gateway;
    static Logger logger = Logger.getLogger(RCCService.class);
    boolean needReleasePTT=false;
    
//    boolean isbusy =false;
//    List<Command> commands;
//    java.util.Timer timer1 ;
    
    
//    boolean pttreplyOK=false;
//    boolean releasepttreplyOK=false;
//    boolean callreplyOK=false;
//    boolean connectreplyOK=false;
//    boolean rxtxreplyOK=false;
//    boolean killreplyOK=false;
//    boolean livereplyOK=false;
//    
//    boolean remotemonitorOK=false;
//    boolean radiocheckOK=false;
//    
//    boolean ReleasePTTInProcess=false;
    

    
    
    

    
    public RCCService(int Port, Gateway gateway) 
    {
        this.gateway=gateway;
        this.Port = Port; 
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
    }
    

    
        public void Start()
    {
        try {
                socket = new DatagramSocket(Port);

            dataReceiver= new DataReceiver();
            dataReceiver.start();
            //socket.setSoTimeout(100);
            
//            timer1.schedule( task,1000, 1000 );
            
        } catch (SocketException ex) {
            System.out.println(ex);
        }
        
    }
        

           synchronized public void EnableRXTX(String ip)
        {         
            
             RadioStationPC statioPC= gateway.GetRadiostatinPCByRealIP(ip);
            
            if(statioPC!=null) 
            for(int i=0;i<2;i++)                           
             {
            
                RCCPacket packet = new RCCPacket();
                byte[] pack= packet.GenerateConnect();
            try {
                DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(ip),Port);
                socket.send(sendPacket);
            }
               catch(Exception ex)
               {
                logger.error(ex);
               }
            
            try 
            {
            Thread.sleep(1000);
             } catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(statioPC.status.rxtxreplyOK){statioPC.status.rxtxreplyOK=false;break;}

            
         }
        

        }
           
//        public void MakeFunctionStatusCheckToRadio(String gatewayIP,int toid)  
//        {
//
//    
//           RCCPacket packet = new RCCPacket();
//           byte[] pack =packet.GenerateFunctionStatusCheck(0);
//           try
//           {
//           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
//           socket.send(sendPacket);
//           }
//           catch(Exception ex)
//           {
//            logger.error(ex);
//           }
//
//             }      
                     
    
           
           
//                             public void MakeRadioCheckToRadio(String gatewayIP,int toid)  
//        {
//                 for(int i=0;i<3;i++)                           
//             {
//    
//           RCCPacket packet = new RCCPacket();
//           byte[] pack =packet.GenerateRadioCheck(toid);
//           try
//           {
//           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
//           socket.send(sendPacket);
//           }
//           catch(Exception ex)
//           {
//            logger.error(ex);
//           }
//               try 
//            {
//            Thread.sleep(1000);
//             } catch (InterruptedException ex) 
//             {
//                logger.error(ex);
//             }
//            if(radiocheckOK){radiocheckOK=false;return;} 
//             }      
//                     
//        }  
           
           
                  public void MakeRemoteMonitorToRadio(String gatewayIP,int toid)    //прослушка
        {
                 RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(gatewayIP);
                 for(int i=0;i<3;i++)                           
             {
    
           RCCPacket packet = new RCCPacket();
           byte[] pack =packet.GenerateRemoteMonitor(toid);
           try
           {
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
               try 
            {
            Thread.sleep(1000);
             } catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(statioPC.status.remotemonitorOK){statioPC.status.remotemonitorOK=false;return;} 
             }      
                     
        }  
           
           
            public void MakeKillToRadio(String gatewayIP,int toid)  
        {
            RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(gatewayIP);
                 for(int i=0;i<3;i++)                           
             {
    
           RCCPacket packet = new RCCPacket();
           byte[] pack =packet.GenerateKill(toid);
           try
           {
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
                       try 
            {
            Thread.sleep(1000);
             } catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(statioPC.status.killreplyOK){statioPC.status.killreplyOK=false;return;} 
             }      
                     
        }  
            
              public void MakeLiveRadio(String gatewayIP,int toid)  
        {
            RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(gatewayIP);
                 for(int i=0;i<3;i++)                           
             {
           
                 
           RCCPacket packet = new RCCPacket();
           byte[] pack =packet.GenerateLive(toid);
           try
           {
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
                       try 
            {
            Thread.sleep(1000);
             } catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(statioPC.status.livereplyOK){statioPC.status.livereplyOK=false;return;} 
             }      
                     
        }  
                
                
         public void MakeCallToRadio(String fromip,String gatewayIP,int toid,int type)    //00
        {

//           if(type==0)
//           {
//           RadioStation station =gateway.GetRadiostatinByID(toid);
//           if(station==null)return ;
//           RadioStationPC stationPC=gateway.GetRadiostatinPCBySubnet(station.Subnet);
//           if(stationPC==null)return ;
//           }
             RadioStationPC radioStationPC = gateway.GetRadiostatinPCByIP(gatewayIP);

//            if(radioStationPC.IsBusy)
//            {
//             //   gateway.client.SendStopCallToServer(-1, toid, type, gatewayIP);
//                gateway.client.SendIsBusyToServer(fromip, toid, type, gatewayIP);
//                return;
//            
//            }
             radioStationPC.status.callreplyOK=false; 
             
//             if(radioStationPC.status.ReleasePTTInProcess)
//             {
//             gateway.client.SendIsBusyToServer(1,fromip, toid, type, gatewayIP);
//             return;
//             }
                 radioStationPC.status.ReleasePTTInProcess=false;
              for(int i=0;i<2;i++)                           
             {
                 
           RCCPacket packet = new RCCPacket();
           byte[] pack =packet.GenerateMakeCall(toid,type);
           try
           {
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
            for(int j=0;j<15;j++)  
            {
                       try 
            {
            Thread.sleep(100);
             } catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(radioStationPC.status.callreplyOK){
                radioStationPC.status.callreplyOK=false;
                radioStationPC.status.ReleasePTTInProcess=false;
                gateway.client.SendIsBusyToServer(0,fromip, toid, type, gatewayIP);
                return;} 
            if(radioStationPC.status.ReleasePTTInProcess)
                break;
            }
            if(radioStationPC.status.ReleasePTTInProcess)
                break;
             } 

              gateway.client.SendIsBusyToServer(1, fromip, toid, type, gatewayIP);
             
              if(!radioStationPC.status.ReleasePTTInProcess)MakeReleasePTT(gatewayIP);
        
        }
         
         public class MakePTT implements Runnable
         {
             String adr;

        public MakePTT(String adr) {
            this.adr = adr;
        }


        @Override
        public void run() {
             RadioStationPC statioPC= gateway.GetRadiostatinPCByRealIP(adr);
            
             statioPC.status.pttreplyOK=false;
             for(int i=0;i<2;i++)                           
             {
            
           RCCPacket packet = new RCCPacket();
           byte[] pack =packet.GeneratePressPTT();
           try
           {
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(adr),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
           
                       try 
            {
            Thread.sleep(1000);
             } catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(statioPC.status.pttreplyOK || statioPC.status.ReleasePTTInProcess){
            //pttreplyOK=false;
            return;}
             }
             if(!statioPC.status.pttreplyOK && !statioPC.status.ReleasePTTInProcess )  MakeReleasePTT(statioPC.IPAdress);
        } 
             
             
         }
         
//                public void MakePTT(String adr)
//        {
//            
//            RadioStationPC statioPC= gateway.GetRadiostatinPCByRealIP(adr);
//            
//             statioPC.status.pttreplyOK=false;
//             for(int i=0;i<2;i++)                           
//             {
//            
//           RCCPacket packet = new RCCPacket();
//           byte[] pack =packet.GeneratePressPTT();
//           try
//           {
//           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(adr),Port);
//           socket.send(sendPacket);
//           }
//           catch(Exception ex)
//           {
//            logger.error(ex);
//           }
//           
//                       try 
//            {
//            Thread.sleep(1000);
//             } catch (InterruptedException ex) 
//             {
//                logger.error(ex);
//             }
//            if(statioPC.status.pttreplyOK || statioPC.status.ReleasePTTInProcess){
//            //pttreplyOK=false;
//            return;}
//             }
//             if(!statioPC.status.pttreplyOK && !statioPC.status.ReleasePTTInProcess )MakeReleasePTT(statioPC.IPAdress);
//            
//        }
         
//         public class MakeReleasePTT implements Runnable
//         {
//         String adr;

//        public MakeReleasePTT(String adr) {
//            this.adr = adr;
//        }
//
//        @Override
//        public void run() {
//            
//           RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(adr);
//            
//           
//            if(!statioPC.status.ReleasePTTInProcess)statioPC.status.ReleasePTTInProcess=true; else return;
//            for(int i=0;i<5;i++)                           
//             { 
//           
//           RCCPacket packet = new RCCPacket();
//           byte[] pack =packet.GenerateReleasePTT();
//           try
//           {
//           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(adr).RealIPAdress),Port);
//           socket.send(sendPacket);
//           }
//           catch(Exception ex)
//           {
//            logger.error(ex);
//           }
//           
//                                  try 
//            {
//            Thread.sleep(1000);
//             } catch (InterruptedException ex) 
//             {
//                logger.error(ex);
//             }
//            if(statioPC.status.releasepttreplyOK){statioPC.status.releasepttreplyOK=false;break;}
//             }
//            statioPC.status.ReleasePTTInProcess=false;       
//        }
//         
//         
//         
//         }
//         
//             public void MakeReleasePTT(String adr)
//        {
//            new Thread(new MakeReleasePTT(adr)).start();
//        }
         
              public void MakeReleasePTT(String adr)
        {
             RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(adr);
            if(statioPC.status.ReleasePTTInProcess)return;
            
//                                new Thread(new Runnable() {
//	        @Override
//	        public void run() {
//            
            

            statioPC.status.ReleasePTTInProcess=true; 
            statioPC.status.releasepttreplyOK=false;
            for(int i=0;i<2;i++)                           
             { 
           
           RCCPacket packet = new RCCPacket();
           byte[] pack =packet.GenerateReleasePTT();
           try
           {
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(adr).RealIPAdress),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
           for(int j=0;j<15;j++)  
            {
           
                                  try 
            {
            Thread.sleep(100);
             } catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(statioPC.status.releasepttreplyOK){
                statioPC.status.releasepttreplyOK=false;
                statioPC.status.ReleasePTTInProcess=false;
                return;}
            }
             }
            statioPC.status.ReleasePTTInProcess=false;                       
             
            
            
//                       	        }
//	    }).start();  
            
        }
        
       
           public class DataReceiver extends Thread
    {
    //  byte[] receiveData = new byte[64];
       
        @Override
        public void run() {
           while(!Thread.interrupted())
            {   
                try {
                receivePacket = new DatagramPacket(new byte[128], 128);
                socket.receive(receivePacket);   
    
                RCCPacket packet = new RCCPacket(receivePacket.getData());
                if(packet.ChecksumOk())
                {
                    
                         if(packet.GetHRNPOperation()!=RCCPacket.Operation.DATA_ACK && packet.GetHRNPOperation()!=RCCPacket.Operation.CONNECTION_REJECT && packet.GetHRNPOperation()!=RCCPacket.Operation.MASTER_BROADCAST)
                    {
                        
                        if(packet.GetHRNPOperation()==RCCPacket.Operation.MASTER_BROADCAST)
                        {
                        int i=0;
                        }
                        
             //ack
            byte[] ack= packet.GenerateHRNPACK();
        try {
            DatagramPacket sendPacket_= new DatagramPacket(ack, ack.length,receivePacket.getAddress(),Port);
            socket.send(sendPacket_);
        }
           catch(Exception ex)
           {
            logger.error(ex);
           }
                    
           Thread.sleep(50);
           
                    }
                  RadioStationPC statioPC= gateway.GetRadiostatinPCByRealIP(receivePacket.getAddress().toString().replace("/", ""));   
                  if(statioPC==null)continue; 
                if(packet.IsHRNP())
                {

                     if(packet.GetHRNPOperation()==RCCPacket.Operation.CONNECTION_ACCEPT)
                    {
                        statioPC.status.connectreplyOK=true;
                        RCCPacket packet_ = new RCCPacket();
                        
                        byte[] pack= packet.GenerateEnableRXTX();
                        try {
                        DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,receivePacket.getAddress(),Port);
                        socket.send(sendPacket);
                        }
                        catch(Exception ex)
                        {
                        logger.error(ex);
                        }
                        
                    }
                     if(packet.GetHRNPOperation()==RCCPacket.Operation.CONNECTION_CLOSE)
                    {
                     int i=0;
                     EnableRXTX(receivePacket.getAddress().toString().replace("/", ""));
                    }
                         if(packet.GetHRNPOperation()==RCCPacket.Operation.CONNECTION_REJECT)
                    {
                     int i=0;
                     EnableRXTX(receivePacket.getAddress().toString().replace("/", ""));
                     
                    }
                     
                     
                    
                }
                else if(packet.IsRCCPacket())
                {
                    
                        if(packet.GetOperation()==RCCPacket.Operation.STATUS_CHECK_REPLY)
                    {
                            int i=0;
                    }
                                                if(packet.GetOperation()==RCCPacket.Operation.RADIO_CHECK_REQUEST_ACK)
                    {
                                statioPC.status.radiocheckOK=true;
                                int i=0;
                    }

                            if(packet.GetOperation()==RCCPacket.Operation.REMOTE_MONITOR_REPLY)
                    {
                        
                                                if(packet.GetRemoteMonitorResult()==RCCPacket.CallReplyStatus.SUCCES)
                                                {
                                                    statioPC.status.remotemonitorOK=true;
                                                     int j=packet.GetRemoteMonitorTargetID();
                                                }

                    }
                        
                    
                          if(packet.GetOperation()==RCCPacket.Operation.LIVEREPLY)
                    {
                        if(packet.GetCallStatus()==RCCPacket.CallReplyStatus.SUCCES)
                        {
                        statioPC.status.livereplyOK=true;
                        int id=packet.GetKillRadioID();
                       // String radioip=receivePacket.getAddress().toString().replace("/", "");
                        gateway.client.SendMobileRadioLiveStateToServer(id, 1);
                        }
                        if(packet.GetCallStatus()==RCCPacket.CallReplyStatus.FAILURE)
                        {
                        statioPC.status.livereplyOK=false;
                        }
             
                        
                    }
                    
                      if(packet.GetOperation()==RCCPacket.Operation.KILLREPLY)
                    {
                        if(packet.GetCallStatus()==RCCPacket.CallReplyStatus.SUCCES)
                        {
                        statioPC.status.killreplyOK=true;
                        int id=packet.GetKillRadioID();
                       // String radioip=receivePacket.getAddress().toString().replace("/", "");
                        gateway.client.SendMobileRadioLiveStateToServer(id, 0);
                        }
                        if(packet.GetCallStatus()==RCCPacket.CallReplyStatus.FAILURE)
                        {
                       statioPC.status.killreplyOK=false;
                        }
                    }
                    
                    
                     if(packet.GetOperation()==RCCPacket.Operation.CALLREPLY)
                    {
                        if(packet.GetMakeCallReply()==RCCPacket.CallReplyStatus.SUCCES)
                        {
                        statioPC.status.callreplyOK=true;
                       // MakePTT(receivePacket.getAddress().toString().replace("/", ""));
                        new Thread(new MakePTT(receivePacket.getAddress().toString().replace("/", ""))).start();
                        
                        
                        }
                          if(packet.GetMakeCallReply()==RCCPacket.CallReplyStatus.FAILURE)
                        {
                            int i=0;
                        }
                        
                        
                      
                    }
                
                    if(packet.GetOperation()==RCCPacket.Operation.RECEIVE_STATUS)
                    {
                        int type=packet.GetCallType();
                        int TargetID=packet.GetCallTarget();
                        int SenderID=packet.GetCallSender();
                        
                        
                        RadioStationPC radioStationPC = gateway.GetRadiostatinPCByRealIP(receivePacket.getAddress().toString().replace("/", ""));
                        if(radioStationPC!=null)
                        {
                        if(packet.GetCallStatus()==RCCPacket.CallStatus.START_CALL)
                            
                        {
                            gateway.client.SendCallToServer(SenderID, TargetID, type, radioStationPC.IPAdress,0);
                            radioStationPC.stationPanel.SetState("Входяший вызов");
                            radioStationPC.IsBusy=true;
                            
                        }
                        
                        if(packet.GetCallStatus()==RCCPacket.CallStatus.END_CALL)
                        {
                            //radioStationPC.rtpMediaSession.StopSession();
                            type=1;         //звонок полностью завершен
                            gateway.client.SendStopCallToServer(SenderID, TargetID, type, radioStationPC.IPAdress);  //type указываеьт на тип завершения звонка
                            radioStationPC.stationPanel.SetState("Свободен");
                            radioStationPC.IsBusy=false;
                            
                        }
                             if(packet.GetCallStatus()==RCCPacket.CallStatus.HANGTIME)
                        {
                            radioStationPC.rtpMediaSession.StopSession();
                            type=0;         //звонок в состоянии ожидания
                            radioStationPC.stationPanel.SetState("Входяший вызов (ожидание ответа)");
                            gateway.client.SendStopCallToServer(SenderID, TargetID, type, radioStationPC.IPAdress); //type указываеьт на тип завершения звонка
                        }
                        }
                      
                    }
                      if(packet.GetOperation()==RCCPacket.Operation.TRANSMIT_STATUS)
                    {
                      //  int type=packet.GetCallType();
                        RadioStationPC radioStationPC = gateway.GetRadiostatinPCByRealIP(receivePacket.getAddress().toString().replace("/", ""));
                         if(radioStationPC!=null)
                        {
                        if(packet.GetCallStatus()==RCCPacket.CallStatus.START_CALL)
                        {
                            radioStationPC.stationPanel.SetState("Исходящий вызов");
                            radioStationPC.IsBusy =true;
                            if(!statioPC.status.pttreplyOK)  //если вызов с тангеты
                            {
                             int target=packet.GetCallTargetExtPTT();  
                             int type=packet.GetCallTypeExtPTT();
                     
                             gateway.client.SendCallToServer(radioStationPC.ID, target, type, radioStationPC.IPAdress,2);
                            }
                           
                        }
                        if(packet.GetCallStatus()==RCCPacket.CallStatus.END_CALL)
                        {
                            radioStationPC.rtpMediaSession.StopSession();
                            radioStationPC.stationPanel.SetState("Свободен");
                            radioStationPC.IsBusy =false;
                            if(!statioPC.status.releasepttreplyOK)statioPC.status.releasepttreplyOK=true;
                            statioPC.status.pttreplyOK=false;
                        }
                            if(packet.GetCallStatus()==RCCPacket.CallStatus.HANGTIME)
                        {
                            statioPC.status.releasepttreplyOK=true;
                            radioStationPC.IsBusy =false;
                            radioStationPC.stationPanel.SetState("Исходящий вызов (ожидание ответа)");
                                if(!statioPC.status.pttreplyOK)  //если вызов с тангеты
                            {
                             int target=packet.GetCallTargetExtPTT();  
                             int type=packet.GetCallTypeExtPTT();
                      
                             //gateway.client.SendCallToServer(radioStationPC.ID, target, type, radioStationPC.IPAdress,1);
                             gateway.client.SendStopCallToServer(radioStationPC.ID, target, type, radioStationPC.IPAdress);
                            }
                            
                        }
                        }
                        
                    }
                        if(packet.GetOperation()==RCCPacket.Operation.BROADCAST_STATUS_CONF_REPLY)
                    {
                        statioPC.status.rxtxreplyOK=true;
                        
                    }
                      if(packet.GetOperation()==RCCPacket.Operation.KEY_OPER_REPLY)
                    {
                        int reply=packet.GetMakeCallReply();
                        int key =packet.GetKey();
                        if(key==RCCPacket.Key.PTT)
                        {
                            if(reply==RCCPacket.CallReplyStatus.SUCCES)
                            {
                                 statioPC.status.pttreplyOK=true;
                            }
                            
                                if(reply==RCCPacket.CallReplyStatus.FAILURE)
                            {
                                statioPC.status.pttreplyOK=false;
                            }
                        
                        }
                    }
                      

                    
                }
                }
                  
                  
                
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    break;
                }
            catch(IOException ex)
            {
            System.out.println(ex);
            }
            }
        }
    
    }
           
        public class Command
        {
        String command;
        String[] arguments;
        int count;
        boolean delete=false;

        public Command(String command) {
            this.command = command;
            count=0;
        }
        
        }
        
        
        
}
