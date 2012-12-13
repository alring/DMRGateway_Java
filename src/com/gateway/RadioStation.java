/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.util.Calendar;

/**
 *
 * @author Alexandr
 */
public class RadioStation 
{
    int ID;
    String Name;
    String  IPAdress;
    String  PcRadioIPAdress;
    int Subnet;
    boolean IsOnline= false;
    public long registerTime;
    boolean gpsIsOn=false;

    public RadioStation(String IPAdress) 
    {
        this.IPAdress = IPAdress.replace("/", "");
        this.ID=(Integer.parseInt(IPAdress.split("\\.")[1])<<16)| (Integer.parseInt(IPAdress.split("\\.")[2])<<8)|(Integer.parseInt(IPAdress.split("\\.")[3]));
        Subnet=Integer.parseInt(IPAdress.split("\\.")[0].replace("/", ""));
        IsOnline=true;
        registerTime=Calendar.getInstance().getTimeInMillis();
    }
    
    
}
