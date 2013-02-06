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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexandr
 */
public class MessageService 
{

    int Port;
    DatagramSocket socket;
    DatagramPacket receivePacket;
    DataReceiver dataReceiver;
    byte[] receiveData = new byte[512];
    byte[] sendData = new byte[1024];
    RadioStationPC stationPC;
    int msgID=0;
    Gateway gateway;
    static Logger logger = Logger.getLogger(MessageService.class);
    
    List<Message> messages;
    
    
    
    public class Message
    {
    int id;

    String dispatcherIP;

        public Message(int id,String dispatcherIP) {
            this.id = id;
            this.dispatcherIP = dispatcherIP;
        }
    
        
    }
    
    Message GetMessageByID(int id)
    {
        for(int i=0;i<messages.size();i++)
        {
        if(messages.get(i).id==id)return messages.get(i);
        }
        return null;
    }


    public MessageService(int Port, Gateway gateway) 
    {
        this.Port = Port; 
       // this.stationPC=station;
        this.gateway= gateway;
        receivePacket= new DatagramPacket(receiveData, receiveData.length);
        messages= new ArrayList<Message>();
    }
    
        public void Start()
    {
        try {
            socket = new DatagramSocket(Port);
            dataReceiver= new DataReceiver();
            dataReceiver.start();
            //socket.setSoTimeout(100);
        } catch (SocketException ex) {
            System.out.println(ex);
        }
    }
        
  //      public boolean SendMessageToRadio(int id, String msg)
         public void SendPrivatMessageToRadio(int toid, String msg,String dispIP,int msgId)
                
        {
          // if(stationPC.IsOnline())
         //  {
                          synchronized(this)
              {
                       try
           {
           RadioStation station =gateway.GetRadiostatinByID(toid);
           if(station==null){gateway.client.SendMessageIsDeliveredToServer(msgId, dispIP,0); return ;}
           String toip=station.IPAdress;
           RadioStationPC stationPC=gateway.GetRadiostatinPCBySubnet(station.Subnet);
           if(stationPC==null)return ;
           String fromip= stationPC.GetIP();     
           fromip= fromip.split("\\.")[0]+"."+fromip.split("\\.")[1]+"."+fromip.split("\\.")[2]+"."+String.valueOf(Integer.parseInt(fromip.split("\\.")[3])+1);    

           MessagePacket packet = new MessagePacket();
           byte[] pack =packet.GeneratePrivateMessage(msgId,fromip,toip ,msg);

           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(stationPC.RealIPAdress),Port);
           socket.send(sendPacket);
           Message m= new Message(msgId, dispIP);
           messages.add(m);
           int count=0;
           while(messages.contains(m))
                   {
            count++;
           if(count>20)
           {
               gateway.client.SendMessageIsDeliveredToServer(msgId, dispIP,0);
               break;
           }
           Thread.sleep(500);
                   }
           messages.remove(m);
           }
           catch(Exception ex)
           {
           logger.error(ex);
           }
                       
              }
           //}
         //  return false;
       }
        
          public void SendGroupMessageToRadio(int toid,String radioip ,String msg,String dispIP,int msgId)
          {
              synchronized(this)
              {
        try {
            //String toip=station.IPAdress;
            RadioStationPC stationPC=gateway.GetRadiostatinPCByIP(radioip);
            if(stationPC==null)return ;
           String fromip= stationPC.GetIP();     
           fromip= fromip.split("\\.")[0]+"."+fromip.split("\\.")[1]+"."+fromip.split("\\.")[2]+"."+String.valueOf(Integer.parseInt(fromip.split("\\.")[3])+1);        
                    
       
            MessagePacket packet = new MessagePacket();
            byte[] pack =packet.GenerateGroupMessage(msgId,fromip,toid ,msg);

            DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(stationPC.RealIPAdress),Port);
            socket.send(sendPacket);
             
            Message m= new Message(msgId, dispIP);
           messages.add(m);
           int count=0;
           while(messages.contains(m))
                   {
            count++;
           if(count>8)
           {
               gateway.client.SendMessageIsDeliveredToServer(msgId, dispIP,0);
               break;
           }
           Thread.sleep(500);
                   }
           messages.remove(m);
            
            
            //Thread.sleep(1000);
            }
            catch(Exception ex)
            {
            logger.error(ex);
            }

    
              }
          }
    
    
       public class DataReceiver extends Thread
    {
    //  byte[] receiveData = new byte[64];
       
        @Override
        public void run() {
           while(!Thread.interrupted())
            {   
                try {
                socket.receive(receivePacket);   
                MessagePacket packet = new MessagePacket(receivePacket.getData());
                if(packet.ChecksumOk())
                {
                if(packet.IsMessagePacket())
                {
      
                    if(packet.GetOperation()==MessagePacket.Operation.SENDGROUPMSGACK)
                    {
                       int result=packet.GetGroupResult();
                       if(result==0)
                       {
                       int messageID=packet.GetRequestID();
                       Message message=GetMessageByID(messageID);
                       if(message!=null)
                       {
                       gateway.client.SendMessageIsDeliveredToServer(message.id, message.dispatcherIP,1);
                       messages.remove(message);
                       }
                       }
                      
                    }
                      if(packet.GetOperation()==MessagePacket.Operation.SENDPRIVATMSGACK)
                    {
                       int result=packet.GetPrivatResult();
                       if(result==0)
                       {
                       int messageID=packet.GetRequestID();
                       Message message=GetMessageByID(messageID);
                       if(message!=null)
                       {
                       {
                       gateway.client.SendMessageIsDeliveredToServer(message.id, message.dispatcherIP,1);
                       messages.remove(message);
                       }
                       }
                       }  
                    }
                       if(packet.GetOperation()==MessagePacket.Operation.SENDPRIVATMSG)
                    {
                        String msg= packet.GetMessage();
                        int toid= packet.GetDestinationID();
                        int fromid= packet.GetSourceID();  
                        gateway.client.SendMessageToServer(fromid, toid,0, msg);
            
                    }
                      if(packet.GetOperation()==MessagePacket.Operation.SENDGROUPMSG)
                    {
                        String msg= packet.GetMessage();
                        int toid= packet.GetDestinationID();
                        int fromid= packet.GetSourceID();  
                        gateway.client.SendMessageToServer(fromid, toid,1, msg);

                    }
                      
                    
                }
                }
                  
                
                    Thread.sleep(100);
                } catch (InterruptedException ex) 
                {
                    break;
                }
            catch(IOException ex)
            {
            System.out.println(ex);
            }
            }
        }
    
    }
}
