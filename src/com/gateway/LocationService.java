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
import org.apache.log4j.Logger;

/**
 *
 * @author Alexandr
 */
public class LocationService 
{
    int Port;
    DatagramSocket socket;
    DatagramPacket receivePacket;
    byte[] receiveData = new byte[512];
    byte[] sendData = new byte[1024];
    RadioStationPC stationPC;
    Gateway gateway;
    static Logger logger = Logger.getLogger(LocationService.class);
    DataReceiver dataReceiver;
    
    int requestID=0;
    //
    boolean startreportreplyOK=false;
    boolean stopreportreplyOK=false;

    
    public LocationService(int Port, Gateway gateway) {
        this.Port = Port;
        this.gateway = gateway;
        
       
        receivePacket = new DatagramPacket(receiveData, receiveData.length);

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
            
          public void Stop()
    {
        dataReceiver.interrupt();
    
    }
          
          
                    public void ImmadiateRequest(int radioid)  
        {
           LocationPacket packet = new LocationPacket();
           RadioStation station= gateway.GetRadiostatinByID(radioid);
           if(station==null)return;
           
           byte[] pack = packet.GenerateImmadiateRequest(++requestID,station.IPAdress);
           try
           {
           //DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCBySubnet(station.Subnet).RealIPAdress),Port);
               DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(station.IPAdress),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
//             try 
//            {
//           Thread.sleep(1000);
//             } 
//             catch (InterruptedException ex) 
//             {
//                logger.error(ex);
//             }
        }
          
               public void StartReport(int radioid,byte[] data)  
        {
            startreportreplyOK = false;
              for(int i=0;i<2;i++)                           
             {   
           LocationPacket packet = new LocationPacket();
           RadioStation station= gateway.GetRadiostatinByID(radioid);
           if(station==null)return;
           
           byte[] pack = packet.GenerateStartReport(++requestID,station.IPAdress,data);
           try
           {
           //DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCBySubnet(station.Subnet).RealIPAdress),Port);
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(station.IPAdress),Port); 
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
             try 
            {
            Thread.sleep(1000);
             } 
             catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(startreportreplyOK){startreportreplyOK=false;return;} 
             }     
            
        }          
                    
          public void StartReport(int radioid)  
        {
            
              for(int i=0;i<1;i++)                           
             {   
                 logger.warn("Req pack");
           LocationPacket packet = new LocationPacket();
           RadioStation station= gateway.GetRadiostatinByID(radioid);
           if(station==null)return;
           byte[] pack = packet.GenerateStartReport(++requestID,station.IPAdress);
           try
           {
           //DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCBySubnet(station.Subnet).RealIPAdress),Port);
           DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(station.IPAdress),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
             try 
             {
              Thread.sleep(1500);
             } 
             catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(startreportreplyOK){startreportreplyOK=false;return;} 
             }     
            
        }
        
          public void StopReport(int radioid)  
        {
            
               for(int i=0;i<2;i++)                           
             {   
           LocationPacket packet = new LocationPacket();
           RadioStation station= gateway.GetRadiostatinByID(radioid);
           if(station==null)return;
           
           byte[] pack = packet.GenerateStopReport(++requestID,station.IPAdress);
           try
           {
        
           //DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(gateway.GetRadiostatinPCBySubnet(station.Subnet).RealIPAdress),Port);
               DatagramPacket sendPacket= new DatagramPacket(pack, pack.length,InetAddress.getByName(station.IPAdress),Port);
           socket.send(sendPacket);
           }
           catch(Exception ex)
           {
            logger.error(ex);
           }
             try 
            {
            Thread.sleep(1500);
             } 
             catch (InterruptedException ex) 
             {
                logger.error(ex);
             }
            if(stopreportreplyOK){stopreportreplyOK=false;return;} 
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
                LocationPacket packet = new LocationPacket(receivePacket.getData());
                if(packet.ChecksumOk())
                {
                if(packet.IsLocationPacket())
                {
                    logger.warn("GET LOCATION PACK");
                        if(packet.GetOperation()==LocationPacket.Operation.SLIS_ANSWER)
                    {
                          logger.warn("SLIS ANSWER");
                        boolean state = packet.GetGpsStateImmadiate();
                        int id = packet.GetRadioID();
                        
                        if(state && !gateway.GetRadiostatinByID(id).gpsIsOn)
                        {
                        StartReport(id,packet.Packet);
                        }
                    }
                    
                    
                    if(packet.GetOperation()==LocationPacket.Operation.TLRS_REPORTANSWER)
                    {
                        if(packet.GetResult()==LocationPacket.Result.SUCCES || packet.GetResult()==LocationPacket.Result.NOVALIDGPS)
                        {
                         logger.warn("get TLRS report answer packet");
                         startreportreplyOK=true;
                         int id =packet.GetRadioID();
                         gateway.GetRadiostatinByID(id).gpsIsOn=true;
                        }
                    }
                     if(packet.GetOperation()==LocationPacket.Operation.TLRS_REPORTSTOPANSWER)
                    {
                        logger.warn("get TLRS report stop answer packet");
                         if(packet.GetResult()==LocationPacket.Result.SUCCES)
                        {
                        stopreportreplyOK=true;
                        }
      
                    }
                     if(packet.GetOperation()==LocationPacket.Operation.TLRS_REPORT)
                    {
                          logger.warn("get TLRS repor");
                           gateway.client.SendGpsToServer(packet.GetRadioID(), packet.GetLatitude(), packet.GetLongitude(), packet.GetSpeed(), packet.GetDirection(), packet.GetTime(),0);
                    }
                     
                     if(packet.GetOperation()==LocationPacket.Operation.ELRS_REPORT)
                    {
                        int type=packet.GetErrType();
                        logger.warn("get ELRS report");
                        gateway.client.SendGpsToServer(packet.GetRadioID_err(), packet.GetLatitude_err(), packet.GetLongitude_err(), packet.GetSpeed_err(), packet.GetDirection_err(), packet.GetTime_err(),type);
                        
                    }
                       if(packet.GetOperation()==LocationPacket.Operation.ELRS_REPORTSTOPREQ)
                    {
                        logger.warn("get TLRS report stop req packet");
                        int i=0;
                    }
                         if(packet.GetOperation()==LocationPacket.Operation.ELRS_REPORTSTOPANSWER)
                    {
                        logger.warn("get TLRS report stop answer");
                        int i=0;
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
               catch(NullPointerException ex)
            {
              System.out.println(ex);  
            }
            }
        }
    
    }
                    
                    
}
