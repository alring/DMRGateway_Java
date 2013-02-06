/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.util.Calendar;
import java.net.DatagramPacket;
/**
 *
 * @author Alexandr
 */
public class RadioStation 
{
    int ID;             //
    String Name;
    String  IPAdress;
    String  PcRadioIPAdress;
    byte[] arrayIP = new byte[4];
    int Subnet;
    public boolean needRefresh = false;
    public long timeToLive = 6;       // цикл опроса
    public long timeToLineBeforeOffline = 3; // цикло опроса до перевода в выключенное состояние
    boolean IsOnline= false;
    public long registerTime;
    boolean gpsIsOn=false;
    
    DatagramPacket requestPacket; 
            
    public DatagramPacket getRequestPacket()
    {
       return requestPacket;         
    }
    
    public void setRequestPacket(DatagramPacket newPacket)
    {
        requestPacket = newPacket;
    }
    
    public byte[] getArrayIP()
    {
        return arrayIP;
    }
    
    public void setArrayIP(byte[] newIP)
    {
        arrayIP[0] = newIP[3]; 
        arrayIP[1] = newIP[2]; 
        arrayIP[2] = newIP[1]; 
        arrayIP[3] = newIP[0]; 
    }
    
    public RadioStation(String IPAdress) 
    {
        this.IPAdress = IPAdress.replace("/", "");
        this.ID=(Integer.parseInt(IPAdress.split("\\.")[1])<<16)| (Integer.parseInt(IPAdress.split("\\.")[2])<<8)|(Integer.parseInt(IPAdress.split("\\.")[3]));
        Subnet=Integer.parseInt(IPAdress.split("\\.")[0].replace("/", ""));
        IsOnline=true;
        registerTime=Calendar.getInstance().getTimeInMillis();
    }
    
    
}
