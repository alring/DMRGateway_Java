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
public class DTService 
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
    static Logger logger = Logger.getLogger(DTService.class);
    boolean dtsrequestOK=false;
    boolean fragOK=false;
    int current_block;
    List<Byte> inData=new ArrayList<Byte>();
    
    
        public DTService(int Port, Gateway gateway) 
    {
        this.Port = Port; 
       // this.stationPC=station;
        this.gateway= gateway;
        receivePacket= new DatagramPacket(receiveData, receiveData.length);
       
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
                
                public void SendData(String toip,String fromip, byte[] data)
                {
                for(int i=0;i<2;i++)    
                {
                try
           {
           RadioStationPC stationPC=gateway.GetRadiostatinPCByRealIP(fromip);
           if(stationPC==null)return;
           DataPacket packet= new DataPacket();
           byte[] pack =packet.GenerateDTSRequest(fromip, toip,data);

           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(toip),Port);
           socket.send(sendPacket);
           }
                catch(Exception e)
                {
                logger.error(e);
                }
                
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ex) 
            {
                logger.error(ex);
            }
                if(dtsrequestOK)break;
                
                }
                if(dtsrequestOK)
                {
                  int numofframes=data.length/448;
                 if((data.length%448)!=0)numofframes++;
                 
                for(int j=0;j<numofframes;j++)
                {
                      for(int i=0;i<2;i++)    
                { 
                        fragOK=false;  
                        current_block=j;
                        SendFrame(toip,fromip,data,j);
                        try {
                            Thread.sleep(500);
                        } 
                        catch (InterruptedException ex) {
                            logger.error(ex);
                        }
                        if(!fragOK)break;
                }
                  
                }
                 
                 
                }
                
          }
                
                 public void SendFrame(String toip,String fromip, byte[] data,int index)
           {
                try
           {
          // RadioStationPC stationPC=gateway.GetRadiostatinPCByRealIP(fromip);
          // if(stationPC==null)return;
           DataPacket packet= new DataPacket();
           byte[] pack =packet.GenerateFragTransmit(fromip, toip,data,index);

           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(toip),Port);
           socket.send(sendPacket);
           }
                catch(Exception e)
                {
                logger.error(e);
                }
         }
                 
                public void SendDTSAnswer(String fromip,String toip)
           {
                try
           {
           RadioStationPC stationPC=gateway.GetRadiostatinPCByRealIP(toip);
           if(stationPC==null)return;
           DataPacket packet= new DataPacket();
           byte[] pack =packet.GenerateDTSAnswer(fromip, toip);

           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(fromip),Port);
           socket.send(sendPacket);
           }
                catch(Exception e)
                {
                logger.error(e);
                }
         }
                 public void SendFragAnswer(String fromip,String toip,int block_num, int result)
           {
                try
           {
       ///    RadioStationPC stationPC=gateway.GetRadiostatinPCByRealIP(toip);
        //   if(stationPC==null)return;
           DataPacket packet= new DataPacket();
           byte[] pack =packet.GenerateFragAnswer(toip, fromip,block_num,result);

           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(fromip),Port);
           socket.send(sendPacket);
           }
                catch(Exception e)
                {
                logger.error(e);
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
                DataPacket packet = new DataPacket(receivePacket.getData());
                if(packet.ChecksumOk())
                {
                if(packet.IsDataPacket())
                {
      
                    if(packet.GetOperation()==DataPacket.Operation.DATA_FLAG_ANSWER)
                    {
                         
                       if(current_block== packet.GetBlockNum())fragOK=true;
                      
                    }
                      if(packet.GetOperation()==DataPacket.Operation.DATA_FLAG_TRANSMIT)
                    {
                        String destIP=packet.GetDestitation();
                        String sourceIP=packet.GetSource();
                        int block=packet.GetBlockNum();
                        SendFragAnswer(sourceIP, destIP,block,0);
                        byte[] data =packet.GetData();
                        String s= new String(data);
                        int i=0;
                      
                    }
                       if(packet.GetOperation()==DataPacket.Operation.DTS_ANSWER)
                    {
                        
                        String destIP=packet.GetDestitation();
                        String sourceIP=packet.GetSource();
                        int result=packet.GetResult();
                        if(result==0)dtsrequestOK=true;
                        
                    }
                       if(packet.GetOperation()==DataPacket.Operation.DTS_REQUEST)
                    {
                        String destIP=packet.GetDestitation();
                        String sourceIP=packet.GetSource();
                        SendDTSAnswer(sourceIP, destIP);
   
                      
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
