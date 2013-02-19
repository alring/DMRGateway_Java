/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;


/**
 *
 * @author Alexandr
 */
   
public class RRSService 
{
    int Port;
    String ipAdd;
    DatagramSocket socket;
    DatagramPacket receivePacket;
    DatagramPacket sendPacket;
    DatagramPacket requestPacket;
    DataReceiver dataReceiver;
    OnlineChecker online_checker;
    byte[] receiveData = new byte[64];
    Gateway gateway;
    static Logger logger = Logger.getLogger(RRSService.class);
    RRSPacket packet;         
       
    public RRSService(int Port,Gateway gateway) 
    {
        this.Port = Port; 
        packet = new RRSPacket();
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        this.gateway=gateway;
        try {
            socket = new DatagramSocket(Port);
           } catch (SocketException ex) 
           {
            logger.warn(ex);
          }
    } 
    
    protected void finalize()
    {
        System.out.println("Вход в деструктор");
    }
    
    
    public void Start()
    { 
            dataReceiver= new DataReceiver();
            dataReceiver.start();
            online_checker = new OnlineChecker();
            online_checker.start();
            System.out.println("Вход в конструктор...");
    }
    
        public void Stop()
    {
        dataReceiver.interrupt();
    
    }
    
        
   public void CheckOnlineRadio(int ID) throws UnknownHostException, IOException
   {
      RRSPacket packetik = new RRSPacket(); 
      logger.warn("Ручной запрос состояния радиостанции, ID: " +ID);
      byte[] ipAddr = new byte[4];
      int DestIp;
      String addr;
      for(int j = 0; j < gateway.radioStationsPC.size();j++)
      {
          //addr = gateway.radioStationsPC.get(j).RealIPAdress;
          DestIp = (0x00FFFFFF & ID)|(((gateway.radioStationsPC.get(j).Subnet)<<24) & 0xFF000000);
          ipAddr[0] = (byte)(DestIp); 
          ipAddr[1] = (byte)(DestIp>>8); 
          ipAddr[2] = (byte)(DestIp>>16); 
          ipAddr[3] = (byte)(DestIp>>24); 
          addr = Integer.toString(DestIp);
           byte[] requestPack = packetik.GenerateOnlineRequest(ipAddr);
           DatagramPacket sendPack = new DatagramPacket(requestPack,requestPack.length,InetAddress.getByName(addr),Port);
           socket.send(sendPack);
          
      }
              
              

   }
        
        
    public class OnlineChecker extends Thread  // Поток, опрашивающий зависшые станции
    {
        
        
        synchronized public void writeToSocket(DatagramPacket packet) throws IOException  // синхрозапись в сокет
        {
           socket.send(packet);
        }
        
        public OnlineChecker()  
        {
            super("OnlineCheckThr");
        }
        
        @Override
        public void run()
        {      
           
           while(!Thread.interrupted())
           {
                
              try 
              {
           
                 
   
                       
                    
                  for(int i = 0; i < gateway.radioStations.size(); i++)  // Перебираем станции которые нужно рефрешить
                  {
                     
                       if ( (gateway.radioStations.get(i).IsOnline == true) && (gateway.radioStations.get(i).needRefresh)) // и направляем запрос о наличии их в сети
                       {
                          logger.warn("Запрос состояния радиостанции, ID: "  + gateway.radioStations.get(i).ID );
                          if (gateway.radioStations.get(i).timeToLive == 0)  // Если время ожидания истекло
                          {
                              gateway.radioStations.get(i).needRefresh = false; // перестаем сканировать эфир
                          }
                          sendPacket = gateway.radioStations.get(i).getRequestPacket();      
                          if(sendPacket == null) logger.warn("Send packet is null");
            
                          writeToSocket(sendPacket);    
                          Thread.sleep(10000);
                         
                       }
                  }
                    
                 Thread.sleep(10000);   
                }
                catch (SocketException ex) 
                {
                System.out.println(ex);
                }
                catch (IOException ex) 
                {
                System.out.println(ex);
                }
               catch (InterruptedException ex) 
               {
               System.out.println(ex);
               }
             
              }
            
           }
        
         }  
        
    public class DataReceiver extends Thread   // Поток, в котором приходят сообщения от РС
    {    
        public DataReceiver()
        {
            
        }
       
        synchronized public void writeToSocket(DatagramPacket packet) throws IOException
        {
           socket.send(packet);
        }
        
        
    @Override
        public void run() 
        {
         
           while(!Thread.interrupted())
            {  
               
                try {
                socket.receive(receivePacket);        
                packet = new RRSPacket(receivePacket.getData());
                
              
                if(packet.ChecksumOk())
                { 
                  
                if(packet.IsRRSPacket())
                {
                  
                    // Если пришел запрос на регистрацию
                    if(packet.GetOperation()==RRSPacket.Operation.REGWITHOUTPASSWORD)
                    {
                       logger.warn("Получено сообщение подтверждение онлайн режима, ID: " + packet.GetStationID());
                       Thread.sleep(500);
                       
                       byte[] answer =packet.GenerateRegACK();         
                       
                       DatagramPacket sendPacket= new DatagramPacket(answer, answer.length,receivePacket.getSocketAddress());
                       writeToSocket(sendPacket);
                       RadioStation radio = new RadioStation(receivePacket.getAddress().toString()); 
                       String fromip=radio.IPAdress;
                       int subnet=Integer.parseInt(fromip.split("\\.")[0]);
                       RadioStationPC radioPC= gateway.GetRadiostatinPCBySubnet(subnet);
                       if(radioPC==null) continue;
                       radio.PcRadioIPAdress=radioPC.IPAdress;
                       radio.setArrayIP(packet.GetStationIP()); // пихаем в массив IP станции, которая прислала запрос на регистрацию
                       boolean add= true;
                       
                       
                       
                       
                       for(int i=0;i<gateway.radioStations.size();i++) // костыль, исп
                       {
                       if(gateway.radioStations.get(i).ID==radio.ID)      // ищем есть ли уже
                             {
                                 add = false;                           // нашли - не добавляем
                             }                      
                       }
                       
                       
                       if(add)
                       {             
                           gateway.radioStations.add(radio); 
                           gateway.client.SendMobileRadioStateToServer(radio.ID,1,radioPC.IPAdress); logger.info("Регистрация обьекта ID="+radio.ID);
                       }
                       else
                       {
                           gateway.client.SendMobileRadioStateToServer(radio.ID,1,radioPC.IPAdress);
                       if(!gateway.GetRadiostatinByID(radio.ID).IsOnline) 
                       {
                           logger.info("Регистрация обьекта ID="+radio.ID); 

                       }
                       gateway.GetRadiostatinByID(radio.ID).IsOnline=true;
                       }
                       
                       
                       
                       
                       for(int i=0;i<gateway.radioStations.size();i++)  // инициализируем TTL и формируем запрос для этой станции
                       {
                       if(gateway.radioStations.get(i).ID==radio.ID)
                             {
                                 add=false;
                                 byte[] request = packet.GenerateOnlineRequest(gateway.radioStations.get(i).getArrayIP()); /*max added*/
                                 DatagramPacket reqPack= new DatagramPacket(request, request.length,receivePacket.getSocketAddress()); /*max added*/
                                 gateway.radioStations.get(i).setRequestPacket(reqPack);
                                 gateway.radioStations.get(i).registerTime=Calendar.getInstance().getTimeInMillis(); 
                                 gateway.radioStations.get(i).timeToLive = 7; 
                                 gateway.radioStations.get(i).timeToLineBeforeOffline = 3;
                                 break;   
                             }
                       }
                       
                      // Thread.sleep(500);
                       
                       
                       
                     //  gateway.rccService.MakeLiveRadio("192.168.1.115", "192.168.10.60", 104);
               
                       
                    }
                    
                    
                   //******************************************************************************* 
                   // Если пришел запрос на снятие с регистрации
                    
                    
                      if(packet.GetOperation()==RRSPacket.Operation.DEREGISTRATION)
                    {
                        logger.warn("Получено сообщение о переходе в оффлайн режим, ID: " + packet.GetStationID());
                       
                        int id=packet.GetStationID();
                        RadioStation radio= gateway.GetRadiostatinByID(packet.GetStationID());
                        if(radio==null) continue;
                        String fromip=radio.IPAdress;
                       int subnet=Integer.parseInt(fromip.split("\\.")[0]);
                       RadioStationPC radioPC= gateway.GetRadiostatinPCBySubnet(subnet);
                       if(radioPC==null) continue;
                       
                        if(radio!=null)
                        {
                       gateway.GetRadiostatinByID(packet.GetStationID()).IsOnline=false;
                       logger.info("Отключение объекта ID="+radio.ID);
                        }
                       gateway.client.SendMobileRadioStateToServer(packet.GetStationID(),0,radioPC.IPAdress);
                    }
                                              
                    
                   //******************************************************************************* 
                   // Если пришел ответ на онлайн запрос
                     
                       if(packet.GetOperation()==(byte)RRSPacket.Operation.ONLINEACK)
                      {
                          logger.warn("Пришел ответ на запрос");
                          for(int i = 0; i < gateway.radioStations.size(); i++) // среди всех станции ищем ту,
                          {                                                     // от которой только что приняли пакет
                               gateway.radioStations.get(i).needRefresh = false;    
                               gateway.radioStations.get(i).timeToLineBeforeOffline = 3; // (((( Не хардкодь!
                               gateway.radioStations.get(i).timeToLive = 7;
                              if (gateway.radioStations.get(i).ID == packet.GetStationID())
                              {
                                 gateway.radioStations.get(i).registerTime = Calendar.getInstance().getTimeInMillis(); 
                              }
                          }
                          
                          
                          
                      }
                    
                       
                   //******************************************************************************* 
                   

                  
                }
                
               
                 
                
                }

                  
                    Thread.sleep(1000);
                } catch (InterruptedException ex) 
                {
                    break;
                }
            catch(IOException ex)
            {
            //System.out.println(ex);
            }
               catch(NullPointerException ex)
            {
              //System.out.println(ex);  
            }
            }
        }
    
    }
    
    
    
}
