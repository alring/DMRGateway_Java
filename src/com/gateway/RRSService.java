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
    DataReceiver dataReceiver;
    byte[] receiveData = new byte[64];
    Gateway gateway;
    static Logger logger = Logger.getLogger(RRSService.class);
             
       
    public RRSService(int Port,Gateway gateway) {
        this.Port = Port; 
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        this.gateway=gateway;
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
    
    public class DataReceiver extends Thread
    {
    //  byte[] receiveData = new byte[64];
       
        @Override
        public void run() {
           while(!Thread.interrupted())
            {   
                try {
                socket.receive(receivePacket);   
                RRSPacket packet = new RRSPacket(receivePacket.getData());
                if(packet.ChecksumOk())
                {
                if(packet.IsRRSPacket())
                {
                    if(packet.GetOperation()==RRSPacket.Operation.REGWITHOUTPASSWORD)
                    {
                       byte[] answer =packet.GenerateRegACK(); 
                       DatagramPacket sendPacket= new DatagramPacket(answer, answer.length,receivePacket.getSocketAddress());
                       socket.send(sendPacket);
                       RadioStation radio= new RadioStation(receivePacket.getAddress().toString()); 
                       String fromip=radio.IPAdress;
                       int subnet=Integer.parseInt(fromip.split("\\.")[0]);
                       RadioStationPC radioPC= gateway.GetRadiostatinPCBySubnet(subnet);
                       if(radioPC==null) continue;
                       radio.PcRadioIPAdress=radioPC.IPAdress;
                       boolean add= true;
                       for(int i=0;i<gateway.radioStations.size();i++)
                       {
                       if(gateway.radioStations.get(i).ID==radio.ID){add=false;gateway.radioStations.get(i).registerTime=Calendar.getInstance().getTimeInMillis(); break;}
                       }
                       Thread.sleep(500);
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
                      if(packet.GetOperation()==RRSPacket.Operation.DEREGISTRATION)
                    {
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
                    
                }
                }
                  
                    Thread.sleep(100);
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
