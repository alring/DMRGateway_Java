/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import sun.awt.windows.ThemeReader;

/**
 *
 * @author Alexandr
 */
public class RCCService 
{
    int Port;
    DatagramSocket socket;
    DatagramPacket receivePacket;
    DataReceiver dataReceiver;
    long startTime = 0;
    byte[] receiveData = new byte[64];
    byte[] sendData = new byte[1024];
    Gateway gateway;
    List<RadioGatewayState> gateway_state;
    static Logger logger = Logger.getLogger(RCCService.class);
    boolean needReleasePTT=false;
    boolean isGatewayBusy = false;   // переменная, для определения занятости шлюза
    boolean needAddRadioGatewayToList = false;
    int waitTime = 20;
    public WorkStatus workStatus = new WorkStatus();
    public int currentStatus = workStatus.INCOMING_CALL;
    
    
    public class WorkStatus
    {

       public static final int INCOMING_CALL = 0;
       public static final int HANG_TIME = 1;
       public static final int OUTGOING_CALL = 2;
       public static final int IDLE_STATE = 3;
 
    }

 
     
    public RCCService(int Port, Gateway gateway)  // КОНСТРУКТОР
        {
             gateway_state = new ArrayList<RadioGatewayState>();
             gateway_state.add(new RadioGatewayState()); 
             this.gateway=gateway;
             this.Port = Port; 
             receivePacket = new DatagramPacket(receiveData, receiveData.length);
        }
        
        
    public void Start()                          // СОЗДАНИЕ ПРИЕМНИКА ДАННЫХ ОТ РС
        {
            try 
            {
            socket = new DatagramSocket(Port);
            dataReceiver= new DataReceiver();
            dataReceiver.start();
             } catch (SocketException ex) 
             {
            System.out.println(ex);
             }
        
        }
       
    
    
   /*--------------------------------------------------------------------------*/  
    
    synchronized public void setWaitTime(int time)
    {
       if(time!=0) waitTime = time;
    }
    
   /*--------------------------------------------------------------------------*/   

  synchronized public void EnableRXTX(String ip)  // РАЗРЕШАЕМ ПРИИЕМ И ПЕРЕДАЧУ
        {         
            
            RadioStationPC statioPC= gateway.GetRadiostatinPCByRealIP(ip);
            if(statioPC!=null) 
            for(int i=0;i<2;i++)                           
             {
                RCCPacket packet = new RCCPacket();
                byte[] pack= packet.GenerateConnect();
                try 
                {
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

  /*--------------------------------------------------------------------------*/

     public void MakeRemoteMonitorToRadio(String gatewayIP,int toid, String dispip)    //прослушка
         {
                 
               RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(gatewayIP);
               logger.warn("monitor");
               //for(int i=0;i<1;i++)                           
             //  {
    
                  RCCPacket packet = new RCCPacket();
                  byte[] pack =packet.GenerateRemoteMonitor(toid);
                  try
                  {
                      DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
                      socket.send(sendPacket);
                      Thread.sleep(17000);
                  }
                  catch(Exception ex)
                  {
                       logger.error(ex);
                  }  
               if(statioPC.status.remotemonitorOK){statioPC.status.remotemonitorOK=false;return;} 
             //}     
               logger.warn("send no monitor");
             gateway.client.SendMakeRemoteMonitorToServer(toid, dispip, 0);
         }   
  
   /*--------------------------------------------------------------------------*/

   public void MakeKillToRadio(String gatewayIP,int toid, String dispip)    // Убийство
        {
            
              RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(gatewayIP);
             // for(int i=0;i<3;i++)                           
              // {
    
                  RCCPacket packet = new RCCPacket();
                  byte[] pack =packet.GenerateKill(toid);
                  try
                  {
                      DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
                      socket.send(sendPacket);
                      Thread.sleep(17000);
                  }
                  catch(Exception ex)
                  {
                    logger.error(ex);
                  }
                 if(statioPC.status.killreplyOK){statioPC.status.killreplyOK=false;logger.warn("выход по получению ответа");return;} 
           //   }      
              logger.warn("dispip " + dispip);
              gateway.client.SendMakeKillToServer(toid, dispip, 0);       
        }  
  
   /*--------------------------------------------------------------------------*/ 
  
   
   public void MakeStopDeferredKillRadio(String gatewayIP,int toid)
   {
        RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(gatewayIP);
        statioPC.status.stopDeferredKill = true;
   }
   
   
    public void MakeStartDefferedKillRadio(String gatewayIP, int toid)
    {
        
        DeferredBlock radioKiller = new DeferredBlock(gatewayIP,toid);
        radioKiller.Start();
    }
   
   
     public void MakeDeferredKillRadio(String gatewayIP,int toid)   // отложенное убийство
     {
           logger.warn("Начата процедура отложенного убийства, ID " + toid);
           RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(gatewayIP);
           RadioStation radio = gateway.GetRadiostatinByID(toid);
            radio.stopDeferredKill = false;
              while((radio.stopDeferredKill==false))                        
               {    
                  RCCPacket packet = new RCCPacket();
                  byte[] pack =packet.GenerateKill(toid);
                  try
                  {
                      
                      DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
                      if(radio.stopDeferredKill == true) break;
                      socket.send(sendPacket);
                      Thread.sleep(150000);
                  }
                  catch(Exception ex)
                  {
                    logger.error(ex);
                  }
              }
           logger.warn("Закончена процедура отложенного убийства, ID " + toid);
     }
     
     
     
     public void MakeLiveRadio(String gatewayIP,int toid, String dispip)  // Оживление
        {
            
           RadioStationPC statioPC= gateway.GetRadiostatinPCByIP(gatewayIP);
           statioPC.status.stopDeferredKill = true;
             //for(int i=0;i<3;i++)                           
            // {  
               RCCPacket packet = new RCCPacket();
               byte[] pack =packet.GenerateLive(toid);
               try
               {
                DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
                socket.send(sendPacket);
                Thread.sleep(17000);
               }
               catch(Exception ex)
               {
                logger.error(ex);
               }
               if(statioPC.status.livereplyOK){statioPC.status.livereplyOK=false;return;} 
             //}    
            gateway.client.SendMakeLiveToServer(toid, dispip, 0); 
        }  
   
   /*--------------------------------------------------------------------------*/ 
  
   public boolean MakeCallToRadio(String fromip,String gatewayIP,int toid,int type)    // ЗАПРОС НА СОЗДАНИЕ ИСХОДЯЩЕГО ВЫЗОВА
        {     
             isGatewayBusy = true; // признак занятости шлюза
             RadioStationPC radioPC = gateway.GetRadiostatinPCByIP(gatewayIP);   // берем значение из листа 
             radioPC.status.txModeOnly = true; 
             if (radioPC.IsBusy) return false;                                   // если занят, то возвращаем фолс вызывающему методу
             
             radioPC.status.generationCallACK = false;
             radioPC.status.pttPressACK = false;
             radioPC.status.pttReleaseACK = false;
             
             
             try
             { 
                  RCCPacket packet = new RCCPacket();
                  byte[] pack = packet.GenerateMakeCall(toid,type);
                  DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(gatewayIP).RealIPAdress),Port);
                  radioPC.status.currentButtonOperation = radioPC.TypeOperation.PRESS_PTT; // Указываем, что было произведено НАЖАТИЕ кнопки
                  socket.send(sendPacket);        // отправили пакет
               
                  for(int j = 0; j < waitTime; j++) // проверяем 400 мс получение ответа в потоке DataReciever
                  {
                      if(radioPC.status.generationCallACK == true) 
                      {
                          setWaitTime(40);
                          if(gateway.rccService.currentStatus == gateway.rccService.workStatus.INCOMING_CALL) 
                            {                                                                                  
                              gateway.client.SendSuppressDelirivedToServer(type, fromip, toid, type, gatewayIP);
                            }     
                          return true;
                      }
                      Thread.sleep(10);
                  }               
             }
             catch(Exception ex)
             { 
                 
             } 
            logger.warn("call method returned false");
            if(gateway.rccService.currentStatus == gateway.rccService.workStatus.INCOMING_CALL) 
               {                                                                                  
                   gateway.client.SendSuppressDelirivedToServer(type, fromip, toid, type, gatewayIP);
               }   
            return false; // если РС не ответила, то отсылаем на сервер признак занятости шлюза
         }

    /*--------------------------------------------------------------------------*/ 
   
    public void MakeReleasePTT(String radioip)// запуск в другом потоке
    {
         RadioStationPC radioPC = gateway.GetRadiostatinPCByIP(radioip); 
         radioPC.status.currentButtonOperation = radioPC.TypeOperation.RELEASE_PTT; 
         new MakeRelease(radioip);
         return;
         
    }
  
    
    public void _MakePressPTT(String radioip)               // запуск в другом потоке
    {
        new MakePressPTT(radioip);
    }

/*****************************************************************************
 *****************************************************************************
 *****************************************************************************
 ****************************************************************************/
   
    public class CheckRadioStatus implements Runnable
    {
        Thread thr;
        String ipAddr;
        public CheckRadioStatus(String newIP)
        {
                   ipAddr = newIP;
                   thr = new Thread(thr);
                   thr.start();
        }
        
        
        
        @Override
        public void run()
        {
            RadioStationPC radioPC = gateway.GetRadiostatinPCByIP(ipAddr); 
            RCCPacket packet = new RCCPacket();
            while(!Thread.interrupted())
            {
                 byte[] pack = packet.GenerateReleasePTT();
                 try
                 {
                      DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(ipAddr).RealIPAdress),Port);
                      Thread.sleep(50);
                      socket.send(sendPacket);
                 }
                 catch(Exception ex)
                 {
                    logger.error(ex);
                 }
           }
        }   
    }
        
    
    
    
   
    public class DataReceiver extends Thread
    {

       
     @Override
     public void run() 
     {
       
       while(!Thread.interrupted())
        {   
          
          receivePacket = new DatagramPacket(new byte[128], 128);
          try 
          {
           socket.receive(receivePacket);
          } 
          catch (IOException ex) 
          {
           java.util.logging.Logger.getLogger(RCCService.class.getName()).log(Level.SEVERE, null, ex);
          }
             
             RCCPacket packet = new RCCPacket(receivePacket.getData());
             if(packet.ChecksumOk())
             {
                    
                if(packet.GetHRNPOperation()!=RCCPacket.Operation.DATA_ACK && packet.GetHRNPOperation()!=RCCPacket.Operation.CONNECTION_REJECT && packet.GetHRNPOperation()!=RCCPacket.Operation.MASTER_BROADCAST)
                {
                        
                     if(packet.GetHRNPOperation()==RCCPacket.Operation.MASTER_BROADCAST)
                     {
                       int i=0;
                     }

                      byte[] ack= packet.GenerateHRNPACK();
                      try 
                      {
                         DatagramPacket sendPacket_= new DatagramPacket(ack, ack.length,receivePacket.getAddress(),Port);
                         socket.send(sendPacket_);
                      }
                      catch(Exception ex)
                      {
                         logger.error(ex);
                      }

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
                        logger.warn("makeremotemonitor reply");
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
                          RadioStation radio = gateway.GetRadiostatinByID(id);
                          radio.stopDeferredKill = true;
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
                       
                        statioPC.status.generationCallACK = true;
                        statioPC.status.txModeOnly = true;
                        new MakePressPTT(receivePacket.getAddress().toString().replace("/", ""));
                       
                        }
                        
                        if(packet.GetMakeCallReply()==RCCPacket.CallReplyStatus.FAILURE)
                        {
                            logger.warn("Generation call not replyed");
                        }

                   }
                
                     
                   if (statioPC.status.txModeOnly == false)   
                   {
                    if( packet.GetOperation()==RCCPacket.Operation.RECEIVE_STATUS)
                    {
                       int type=packet.GetCallType();
                       int TargetID=packet.GetCallTarget();
                       int SenderID=packet.GetCallSender();    
                       RadioStationPC radioStationPC = gateway.GetRadiostatinPCByRealIP(receivePacket.getAddress().toString().replace("/", ""));
                       if(radioStationPC!=null)
                       { 
                        if(packet.GetCallStatus()==RCCPacket.CallStatus.START_CALL)       
                        {
                            setWaitTime(500);
                            gateway.client.SendCallToServer(SenderID, TargetID, type, radioStationPC.IPAdress,0);
                            radioStationPC.stationPanel.SetState("Входяший вызов");   
                            currentStatus = workStatus.INCOMING_CALL;
                        }
                        
                        if(packet.GetCallStatus()==RCCPacket.CallStatus.END_CALL)
                        {
                            type=1;         //звонок полностью завершен
                            logger.warn("end recv call");
                            gateway.client.SendStopCallToServer(SenderID, TargetID, type, radioStationPC.IPAdress);  //type указываеьт на тип завершения звонка
                            radioStationPC.stationPanel.SetState("Свободен");
                             statioPC.status.pttPressACK = false;
                            gateway.client.SendFreeState(radioStationPC.IPAdress);
                            currentStatus = workStatus.IDLE_STATE;
                            
                        }
                             if(packet.GetCallStatus()==RCCPacket.CallStatus.HANGTIME)
                        {
                            radioStationPC.rtpMediaSession.StopSession();
                            type=0;         //звонок в состоянии ожидания
                           
                            statioPC.status.txModeOnly = false; // на всякий случай
                            radioStationPC.stationPanel.SetState("Входяший вызов (ожидание ответа)");
                            gateway.client.SendStopCallToServer(SenderID, TargetID, type, radioStationPC.IPAdress); //type указываеьт на тип завершения звонка
                            currentStatus = workStatus.IDLE_STATE;
                        }
                       }
                     }
                    }

                    
                     if(packet.GetOperation()==RCCPacket.Operation.TRANSMIT_STATUS)
                     {
  
                        RadioStationPC radioStationPC = gateway.GetRadiostatinPCByRealIP(receivePacket.getAddress().toString().replace("/", ""));
                        if(radioStationPC!=null)
                        {
                          if((packet.GetCallStatus()==RCCPacket.CallStatus.START_CALL)  )
                          {
                              logger.warn("start call, calltypeextptt: " + packet.GetCallTypeExtPTT() + " calltypetarget " + packet.GetCallTarget());
                               currentStatus = workStatus.OUTGOING_CALL;
                              radioStationPC.stationPanel.SetState("Исходящий вызов");
                              if(!statioPC.status.pttPressACK)  //если вызов с тангеты
                              {
                                int target=packet.GetCallTargetExtPTT();  
                                int type=packet.GetCallTypeExtPTT();
                                if(type==1)// óдостовермяемся
                                {
                                gateway.client.SendCallToServer(radioStationPC.ID, target, type, radioStationPC.IPAdress,2);
                                }
                              }
                              /*
                              int type = packet.GetCallTypeExtPTT();
                              if(type==1)  //если вызов с тангеты
                              {
                                int target=packet.GetCallTargetExtPTT();
                                logger.warn(type);
                                gateway.client.SendCallToServer(radioStationPC.ID, target, type, radioStationPC.IPAdress,2);
                              }*/
                          }
                        
                         
                          
                          
                        if( (packet.GetCallStatus()==RCCPacket.CallStatus.END_CALL)  )
                        {     
                            
                            radioStationPC.rtpMediaSession.StopSession();
                            radioStationPC.stationPanel.SetState("Свободен");
                 
                            if(!statioPC.status.pttReleaseACK)statioPC.status.pttReleaseACK=true;
                            statioPC.status.txModeOnly = false;
                            currentStatus = workStatus.IDLE_STATE;
                            gateway.client.SendFreeState(radioStationPC.IPAdress);
                        }
                        
                        if(packet.GetCallStatus()==RCCPacket.CallStatus.HANGTIME)
                        {
                          
                            radioStationPC.stationPanel.SetState("Исходящий вызов (ожидание ответа)");
                            statioPC.status.txModeOnly = false;
                            if(!statioPC.status.pttReleaseACK)  //если вызов с тангеты
                            {  
                             int target=packet.GetCallTargetExtPTT();  
                             int type=packet.GetCallTypeExtPTT();
                             gateway.client.SendStopCallToServer(radioStationPC.ID, target, type, radioStationPC.IPAdress);
                            }
                            else
                            {
                             //isGatewayBusy = false;    
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
                        RadioStationPC radioStationPC = gateway.GetRadiostatinPCByRealIP(receivePacket.getAddress().toString().replace("/", ""));
                        int reply=packet.GetMakeCallReply();
                        int key =packet.GetKey();
                        int oper = packet.GetKeyOper();
                        if(key==RCCPacket.Key.PTT)
                         {
                             if(oper==RCCPacket.KeyOper.PRESS)
                             {
                                      if(reply==RCCPacket.CallReplyStatus.SUCCES)
                                      {
                                        statioPC.status.pttPressACK = true;
                                        
                                      }
                            
                                      if(reply==RCCPacket.CallReplyStatus.FAILURE)
                                      {
                                        statioPC.status.pttPressACK = false;
                                        logger.warn("External 'PTT Press' command isn't delirived");
                                      }
                             }
                                
                            
                             if(oper==RCCPacket.KeyOper.RELEASE)
                             {    
                                      if(reply==RCCPacket.CallReplyStatus.SUCCES)
                                      {
                                       statioPC.status.pttReleaseACK = true;
                                       statioPC.status.txModeOnly = false;   logger.warn("Входящие вызовы разрешены");// Устанавливаем разрешение на обработку входящих вызово 
                                       gateway.client.SendCallEndToServer(radioStationPC.IPAdress);
                                        logger.warn("PTT отпущена!");
                                      }
                            
                                      if(reply==RCCPacket.CallReplyStatus.FAILURE)
                                      {
                                        statioPC.status.txModeOnly = true;   logger.warn("Входящие вызовы разрешены");// Устанавливаем разрешение на обработку входящих вызово 
                                        statioPC.status.pttReleaseACK = false;
                                        logger.warn("External 'PTT Release' command isn't delirived");
                                      }                           
                             }  
                          }
                      }
                     }
             }              
            }         
        }
     }
    
    
/*****************************************************************************/
/*****************************************************************************/    
/*****************************************************************************/
    
    
  public class MakePressPTT implements Runnable
  {
     String adr;
     Thread makePttThread;
     
     public MakePressPTT(String adr) 
     {
            this.adr = adr;
            makePttThread = new Thread(this,"MakePress");
            makePttThread.start();
     }

     @Override
     public void run()
     {
            
           RadioStationPC statioPC= gateway.GetRadiostatinPCByRealIP(adr);
           RCCPacket packet = new RCCPacket();
           byte[] pack =packet.GeneratePressPTT();
           try
           {
             DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(adr),Port);
             logger.warn("Отправка пакета нажатия PTT");
             socket.send(sendPacket);
             Thread.sleep(100);
           }
           catch(Exception ex)
           {
             logger.error(ex);
           }
      } 

   }   
/*****************************************************************************/
  
  public class MakeRelease implements Runnable
  {
      String ipAddr;
      Thread makeReleaseThread;
      RadioStationPC radioPC;        
      public MakeRelease(String addr)
      {
          ipAddr = addr;
          makeReleaseThread = new Thread(this,"MakeRelease");
          makeReleaseThread.start();
      }
      
      @Override
      public void run()
      {
          RadioStationPC radioPC = gateway.GetRadiostatinPCByIP(ipAddr); 
          int counter = 30;
          boolean flag = false;
          for(int i = 0;i<100;i++)
          {
              if(radioPC.status.pttPressACK==true)break;
              logger.warn("PTT не нажата");
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(RCCService.class.getName()).log(Level.SEVERE, null, ex);
                }
          }
          
          RCCPacket packet = new RCCPacket();
          byte[] pack = packet.GenerateReleasePTT();
          try
          {
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCByIP(ipAddr).RealIPAdress),Port);
           logger.warn("Отправка пакета отпускания PTT");
           Thread.sleep(50);
           socket.send(sendPacket);
          }
          catch(Exception ex)
          {
            logger.error(ex);
          }
      }   
  }   
  
  public class DeferredBlock implements Runnable
  {
      Thread thr;
      Gateway gateway;
      String gatewayIP = "";
      public int toid;

      
      public DeferredBlock(String newgatewayIP,int newtoid)
      {
          gatewayIP = newgatewayIP;
          toid = newtoid;
          thr = new Thread(this,"DeferrBlock");
       
      }
      
      public void Start()
      {
             thr.start();
      }
      
      public void run()
      {
         MakeDeferredKillRadio(gatewayIP,toid);
      }
      
      
  }
  
  
}
      
    
    
    