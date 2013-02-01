/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
                
              try {
                 Thread.sleep(1000); 
                 
                for(int i = 0; i < gateway.radioStations.size();i++)
                 {
                 
                      /*                
                    if (gateway.radioStations.get(i).needRefresh == true) // и направляем запрос о наличии их в сети
                    {
                          logger.warn("Запрос состояния радиостанции, ID: "  + gateway.radioStations.get(i).ID );
                          byte[] request = packet.GenerateOnlineRequest(gateway.radioStations.get(i).getArrayIP()); // отправляем radio online status check request

                          requestPacket = new DatagramPacket(request, request.length,receivePacket.getSocketAddress());
                               
                          writeToSocket(requestPacket);    
                     } 
                        */  

                          
                  }
                       
                    
                  for(int i = 0; i < gateway.radioStations.size(); i++)  // Перебираем станции которые нужно рефрешить
                  {
                     
                       if ( (gateway.radioStations.get(i).IsOnline == true) && (gateway.radioStations.get(i).needRefresh)) // и направляем запрос о наличии их в сети
                       {
                          logger.warn("Запрос состояния радиостанции, ID: "  + gateway.radioStations.get(i).ID );
                          if (gateway.radioStations.get(i).timeToLive == 0)  // Если время ожидания истекло
                          {
                              gateway.radioStations.get(i).needRefresh = false; // перестаем сканировать эфир
                          }
                          byte[] request = packet.GenerateRegACK(); // отправляем radio online status check request
                          sendPacket = gateway.radioStations.get(i).getRequestPacket();      
                          writeToSocket(sendPacket);    
                          Thread.sleep(300000);
                       }
                  }
                    
                    
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
                       RadioStation radio= new RadioStation(receivePacket.getAddress().toString()); 
                       String fromip=radio.IPAdress;
                       int subnet=Integer.parseInt(fromip.split("\\.")[0]);
                       RadioStationPC radioPC= gateway.GetRadiostatinPCBySubnet(subnet);
                       if(radioPC==null) continue;
                       radio.PcRadioIPAdress=radioPC.IPAdress;
                       radio.setArrayIP(packet.GetStationIP()); // пихаем в массив IP станции, которая прислала запрос на регистрацию
                       boolean add= true;
                       for(int i=0;i<gateway.radioStations.size();i++)
                       {
                       if(gateway.radioStations.get(i).ID==radio.ID)
                             {
                                 add=false;
                                 
                                 byte[] request = packet.GenerateOnlineRequest(gateway.radioStations.get(i).getArrayIP()); /*max added*/
                                 DatagramPacket reqPack= new DatagramPacket(request, request.length,receivePacket.getSocketAddress()); /*max added*/
                                 gateway.radioStations.get(i).setRequestPacket(reqPack);
                                 gateway.radioStations.get(i).registerTime=Calendar.getInstance().getTimeInMillis(); 
                                 gateway.radioStations.get(i).timeToLive = 10; 
                                 gateway.radioStations.get(i).timeToLineBeforeOffline = 3;
                                 break;   
                             }
                       }
                       
                      // Thread.sleep(500);
                       if(add){
                           gateway.radioStations.add(radio); gateway.client.SendMobileRadioStateToServer(radio.ID,1,radioPC.IPAdress); logger.info("Регистрация обьекта ID="+radio.ID);
                          // gateway.rccService.MakeRadioCheckToRadio(radioPC.IPAdress, radio.ID);
                       }
                       else
                      // if(!gateway.GetRadiostatinByID(radio.ID).IsOnline)
                       {gateway.client.SendMobileRadioStateToServer(radio.ID,1,radioPC.IPAdress);
                       if(!gateway.GetRadiostatinByID(radio.ID).IsOnline) 
                       {
                           logger.info("Регистрация обьекта ID="+radio.ID); 
                         //  gateway.rccService.MakeRadioCheckToRadio(radioPC.IPAdress, radio.ID);
                       }
                       gateway.GetRadiostatinByID(radio.ID).IsOnline=true;}
                       
                       
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
                               gateway.radioStations.get(i).timeToLineBeforeOffline = 10; // (((( Не хардкодь!
                               gateway.radioStations.get(i).timeToLive = 3;
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
