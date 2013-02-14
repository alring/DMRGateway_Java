/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author Alexandr
 */
public class LocationPacket 
{
     byte Type=0x08; 
    
     byte[] Packet;
     
     
     
      public class Operation 
        { 
    public static final int SLIS_REQUEST=0xA001;  //(Standard Location Immediate Service)
    public static final int SLIS_ANSWER=0xA002;
    
    public static final int ELRS_REPORTSTOPREQ=0xB001;   //(Emergency Location Reporting Service)
    public static final int ELRS_REPORTSTOPANSWER=0xB002;
    public static final int ELRS_REPORT=0xB003;
    
    public static final int TLRS_REPORTREQ=0xC001;       //(Triggered Location Reporting Service)
    public static final int TLRS_REPORTANSWER=0xC002;
    public static final int TLRS_REPORT=0xC003;
    public static final int TLRS_REPORTSTOPREQ=0xC004;
    public static final int TLRS_REPORTSTOPANSWER=0xC005;

       }
     
      public class Result
        {  
    public static final int SUCCES=0x0000;
    public static final int FAILURE=0x0001;
     public static final int NOVALIDGPS=0x0006;

        }
    
      public LocationPacket(byte[] packet)
    {
       this.Packet=packet;
    }
      
      public LocationPacket()
    {
    }
    
      public  boolean ChecksumOk()
    {
       
        try
        {
                                 //int PayloadSize=(int)((int)(Packet[3]<<8)|Packet[4]);   
                                 int PayloadSize=(int)((int)((Packet[3]<<8) &0xFF00)|Packet[4]&0xFF); 
                                 byte Checksum = GetChecksum(Packet);
                                 if(Packet[5+PayloadSize]==Checksum)return true;
                                 else return false;
        }
        catch(Exception ex)
        {
        return false;
        }
                                 
    
    }
      
      public  byte GetChecksum(byte[] packet) 
    {
                                  int PayloadSize=(int)((int)((packet[3]<<8) &0xFF00)|packet[4]&0xFF); 
                                 //int PayloadSize=(int)((int)(packet[3]<<8)|packet[4]);   
                                 byte Checksum = (byte)(packet[1]+packet[2]);
                                 Checksum+=packet[3]+packet[4];
                                 int i=0;
                                 for(i=0;i<PayloadSize;i++)
                                 {
                                   Checksum+=packet[5+i];
                                     
                                 }
                                 Checksum= (byte)(~(Checksum));
                                 Checksum+=0x33;
        
    return (byte)Checksum;
    }
            
       public  boolean IsLocationPacket()
    {
        return(Type==Packet[0]);
        
    }
         
       public int  GetOperation()        
      {
          return (((Packet[1]<<8)&0xFF00)|(Packet[2]&0xFF));
      }
        
       public byte[] GenerateImmadiateRequest(int id,String ip)
      {
       byte[] pack= new byte[15];
       pack[0]=Type;
       pack[1]=(byte)0xA0;
       pack[2]=0x01;
       pack[3]=0x00;
       pack[4]=0x8;  //
       pack[5]=(byte)((id>>24)&0xFF);
       pack[6]=(byte)((id>>16)&0xFF);
       pack[7]=(byte)((id>>8)&0xFF);
       pack[8]=(byte)((id>>0)&0xFF);
       pack[9]=(byte)(Integer.parseInt(ip.split("\\.")[0]));
       pack[10]=(byte)(Integer.parseInt(ip.split("\\.")[1]));
       pack[11]=(byte)(Integer.parseInt(ip.split("\\.")[2]));
       pack[12]=(byte)(Integer.parseInt(ip.split("\\.")[3]));
       
       pack[13]=GetChecksum(pack);
       pack[14]=0x03;
       
       return pack;
       
      }
        
       public byte[] GenerateStartReport(int id,String ip,byte[] data)
      {
       byte[] pack= new byte[51];
       pack[0]=Type;
       pack[1]=(byte)0xC0;
       pack[2]=0x01;
       pack[3]=0x00;
       pack[4]=0x2C;  //44 байта
       pack[5]=(byte)((id>>24)&0xFF);
       pack[6]=(byte)((id>>16)&0xFF);
       pack[7]=(byte)((id>>8)&0xFF);
       pack[8]=(byte)((id>>0)&0xFF);
       pack[9]=(byte)(Integer.parseInt(ip.split("\\.")[0]));
       pack[10]=(byte)(Integer.parseInt(ip.split("\\.")[1]));
       pack[11]=(byte)(Integer.parseInt(ip.split("\\.")[2]));
       pack[12]=(byte)(Integer.parseInt(ip.split("\\.")[3]));

       
       pack[13]=0x32;
       pack[14]=0x30; 
       pack[15]=data[26]; 
       pack[16]=data[27]; 
       pack[17]=data[24]; 
       pack[18]=data[25]; 
       pack[19]=data[22];  
       pack[20]=data[23]; 
       pack[21]=data[16]; 
       pack[22]=data[17]; 
       int min=(data[18]-0x30)*10 +(data[19]-0x30);
       min++;
       pack[23]=(byte)((min/10)+0x30);
       pack[24]=(byte)((min%10)+0x30);
       pack[25]=0x31;
       pack[26]=0x30;

       
       //
       pack[27]=0x30; 
       pack[28]=0x30; 
       pack[29]=0x30; 
       pack[30]=0x30;
       pack[31]=0x30; 
       pack[32]=0x30;
       pack[33]=0x30; 
       pack[34]=0x30;
       pack[35]=0x30; 
       pack[36]=0x30;
       pack[37]=0x30; 
       pack[38]=0x30;
       pack[39]=0x30; 
       pack[40]=0x30;
       //
       pack[41]=0x30; 
       pack[42]=0x30;
       pack[43]=0x30; 
       pack[44]=0x30;
       pack[45]=0x30; 
       pack[46]=0x30;
       pack[47]=0x33; 
       pack[48]=0x30;  //30 сек
       
       
       pack[49]=GetChecksum(pack);
       pack[50]=0x03;
       

       return pack;
      }         
        
       public byte[] GenerateStartReport(int id,String ip)
      {
       byte[] pack= new byte[51];
       pack[0]=Type;
       pack[1]=(byte)0xC0;
       pack[2]=0x01;
       pack[3]=0x00;
       pack[4]=0x2C;  //44 байта
       pack[5]=(byte)((id>>24)&0xFF);
       pack[6]=(byte)((id>>16)&0xFF);
       pack[7]=(byte)((id>>8)&0xFF);
       pack[8]=(byte)((id>>0)&0xFF);
       pack[9]=(byte)(Integer.parseInt(ip.split("\\.")[0]));
       pack[10]=(byte)(Integer.parseInt(ip.split("\\.")[1]));
       pack[11]=(byte)(Integer.parseInt(ip.split("\\.")[2]));
       pack[12]=(byte)(Integer.parseInt(ip.split("\\.")[3]));
       
       pack[13]=0x30; // start time
       pack[14]=0x30; // start time
       pack[15]=0x30; // start time
       pack[16]=0x30; // start time
       pack[17]=0x30; // start time
       pack[18]=0x30; // start time
       pack[19]=0x30; // start time
       pack[20]=0x30; // start time
       pack[21]=0x30; // start time
       pack[22]=0x30; // start time
       pack[23]=0x30; // start time
       pack[24]=0x30; // start time
       pack[25]=0x30; // start time
       pack[26]=0x30; // start time
       

       //
       pack[27]=0x30; // stop time
       pack[28]=0x30; // stop time
       pack[29]=0x30; // stop time
       pack[30]=0x30;// stop time
       pack[31]=0x30; // stop time
       pack[32]=0x30;// stop time
       pack[33]=0x30; // stop time
       pack[34]=0x30;// stop time
       pack[35]=0x30; // stop time
       pack[36]=0x30;// stop time
       pack[37]=0x30; // stop time
       pack[38]=0x30;// stop time
       pack[39]=0x30; // stop time
       pack[40]=0x30;// stop time
       //
       pack[41]=0x30; // interval
       pack[42]=0x30;// interval
       pack[43]=0x30; // interval; 
       pack[44]=0x30;// interval
       pack[45]=0x30; // interval
       pack[46]=0x30;// interval
       pack[47]=0x30; // interval
       pack[48]=0x39;  //5 сек
       
       
       pack[49]=GetChecksum(pack);
       pack[50]=0x03;
       

       return pack;
      }
        
       public byte[] GenerateStopReport(int id,String ip)
      {
       byte[] pack= new byte[15];
       pack[0]=Type;
       pack[1]=(byte)0xC0;
       pack[2]=0x04;
       pack[3]=0x00;
       pack[4]=0x08;  //8 байта
       pack[5]=(byte)((id>>24)&0xFF);
       pack[6]=(byte)((id>>16)&0xFF);
       pack[7]=(byte)((id>>8)&0xFF);
       pack[8]=(byte)((id>>0)&0xFF);
       pack[9]=(byte)(Integer.parseInt(ip.split("\\.")[0]));
       pack[10]=(byte)(Integer.parseInt(ip.split("\\.")[1]));
       pack[11]=(byte)(Integer.parseInt(ip.split("\\.")[2]));
       pack[12]=(byte)(Integer.parseInt(ip.split("\\.")[3]));
       
       pack[13]=GetChecksum(pack);
       pack[14]=0x03;
       

       
       return pack;
      } 
               
       public int GetResult()
      {
          return (((Packet[13]<<8)&0xFF00)|(Packet[14]&0xFF));
      }   
           public String GetRadioIPInAnswer()
      {
        return String.valueOf((Packet[9])&0xFF)+"."+String.valueOf((Packet[10])&0xFF)+"."+String.valueOf((Packet[11])&0xFF)+"."+String.valueOf((Packet[12])&0xFF);  
      }
           
          public int GetRadioID()
      {
        return (((Packet[10])<<16&0xFF0000)|((Packet[11])<<8&0xFF00)|((Packet[12])&0xFF));  
      }
                      
             public int GetRadioID_err()
      {
        return (((Packet[6])<<16&0xFF0000)|((Packet[7])<<8&0xFF00)|((Packet[8])&0xFF));  
      }
               
          public String GetRadioIPInReport()
      {
        return String.valueOf((Packet[5])&0xFF)+"."+String.valueOf((Packet[6])&0xFF)+"."+String.valueOf((Packet[7])&0xFF)+"."+String.valueOf((Packet[8])&0xFF);  
      }
          
       public int GetRadioState()
      {
        return (byte)(Packet[9]&0xFF); 
      }
       
                  public long GetTime_err()
      {
          try
          {
          int hour= Integer.parseInt(String.valueOf(Packet[11]-0x30)+ String.valueOf(Packet[12]-0x30));
          int min= Integer.parseInt(String.valueOf(Packet[13]-0x30)+ String.valueOf(Packet[14]-0x30));
          int sec= Integer.parseInt(String.valueOf(Packet[15]-0x30)+ String.valueOf(Packet[16]-0x30)); 
          
          int day=Integer.parseInt(String.valueOf(Packet[17]-0x30)+ String.valueOf(Packet[18]-0x30)); 
          int mouth=Integer.parseInt(String.valueOf(Packet[19]-0x30)+ String.valueOf(Packet[20]-0x30));     
          int year=Integer.parseInt("20"+String.valueOf(Packet[21]-0x30)+ String.valueOf(Packet[22]-0x30)); 
                  
                          
        TimeZone tz=TimeZone.getTimeZone("GMT"); 
        Calendar cal = new GregorianCalendar(tz);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, mouth);
        cal.set(Calendar.DAY_OF_MONTH, day);
        
        long ll=cal.getTimeInMillis()/1000;
        
        return cal.getTimeInMillis()/1000;
           }
          catch(Exception ex)
          {
          return 0;
          }
      }
       
           public long GetTime()
      {
          try
          {
          int hour= Integer.parseInt(String.valueOf(Packet[22]-0x30)+ String.valueOf(Packet[23]-0x30));
          int min= Integer.parseInt(String.valueOf(Packet[24]-0x30)+ String.valueOf(Packet[25]-0x30));
          int sec= Integer.parseInt(String.valueOf(Packet[26]-0x30)+ String.valueOf(Packet[27]-0x30)); 
          
          int day=Integer.parseInt(String.valueOf(Packet[28]-0x30)+ String.valueOf(Packet[29]-0x30)); 
          int mouth=Integer.parseInt(String.valueOf(Packet[30]-0x30)+ String.valueOf(Packet[31]-0x30));     
          int year=Integer.parseInt("20"+String.valueOf(Packet[32]-0x30)+ String.valueOf(Packet[33]-0x30)); 
                  
                          
        TimeZone tz=TimeZone.getTimeZone("GMT"); 
        Calendar cal = new GregorianCalendar(tz);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, mouth-1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        
        //long ll=cal.getTimeInMillis()/1000;
        
        return cal.getTimeInMillis()/1000;
           }
          catch(Exception ex)
          {
          return 0;
          }
      }
            public void GetData()
      {
        
      }
                         public double GetLatitude_err()
      {
          try
          {
          double lat= Integer.parseInt(String.valueOf(Packet[24]-0x30)+ String.valueOf(Packet[25]-0x30));
          String lat_min="";
          for(int i=0;i<7;i++)        
          {
           char c=(char)Packet[26+i]; 
           lat_min+=c;
          }
          double min=Double.parseDouble(lat_min);
          lat+=min/60;
          return lat;
          }
          catch(Exception e)
          {
          return 0;
          }
          
      }
            
             public double GetLatitude()
      {
          try
          {
          double lat= Integer.parseInt(String.valueOf(Packet[35]-0x30)+ String.valueOf(Packet[36]-0x30));
          String lat_min="";
          for(int i=0;i<7;i++)        
          {
           char c=(char)Packet[37+i]; 
           lat_min+=c;
          }
          double min=Double.parseDouble(lat_min);
          lat+=min/60;
          return lat;
          }
          catch(Exception e)
          {
          return 0;
          }
          
      }
                    public double GetLongitude_err()
      {
                    try
          {
          double lon= Integer.parseInt(String.valueOf(Packet[34]-0x30)+ String.valueOf(Packet[35]-0x30)+ String.valueOf(Packet[36]-0x30));
          String lon_min="";
          for(int i=0;i<7;i++)        
          {
           char c=(char)Packet[37+i]; 
           lon_min+=c;
          }
          double min=Double.parseDouble(lon_min);
          lon+=min/60;
         return lon;
          }
          catch(Exception ex)
           {  
             return 0;  
           }
      }   
             
             
          public double GetLongitude()
      {
                    try
          {
          double lon= Integer.parseInt(String.valueOf(Packet[45]-0x30)+ String.valueOf(Packet[46]-0x30)+ String.valueOf(Packet[47]-0x30));
          String lon_min="";
          for(int i=0;i<7;i++)        
          {
           char c=(char)Packet[48+i]; 
           lon_min+=c;
          }
          double min=Double.parseDouble(lon_min);
          lon+=min/60;
         return lon;
          }
          catch(Exception ex)
           {  
             return 0;  
           }
      }
          
                    public double GetSpeed_err()
      {
          try
          {
          String speed_s="";
          speed_s+=(char)(Packet[44]);
          speed_s+=(char)(Packet[45]);
          speed_s+=(char)(Packet[46]);
         double speed=Double.parseDouble(speed_s); 
         return speed;
          }
          catch(Exception ex)
          {
          return 0;
          }
      }  
          
          public double GetSpeed()
      {
          try
          {
          String speed_s="";
          speed_s+=(char)(Packet[55]);
          speed_s+=(char)(Packet[56]);
          speed_s+=(char)(Packet[57]);
         double speed=Double.parseDouble(speed_s); 
         return speed;
          }
          catch(Exception ex)
          {
          return 0;
          }
      }  
          
                    public int GetErrType()
      {
          return (byte)(Packet[9]&0xFF); 
      }
            
                  public int GetDirection_err()
      {
          try
          {
         String dir_s=""; 
         dir_s+=(char)(Packet[47]);
         dir_s+=(char)(Packet[48]);
         dir_s+=(char)(Packet[49]);
         int dir=Integer.parseInt(dir_s); 
         return dir;
          }
          catch(Exception ex)
          {
          return 0;
          }
      }  
                    
         public int GetDirection()
      {
          try
          {
         String dir_s=""; 
         dir_s+=(char)(Packet[58]);
         dir_s+=(char)(Packet[59]);
         dir_s+=(char)(Packet[60]);
         int dir=Integer.parseInt(dir_s); 
         return dir;
          }
          catch(Exception ex)
          {
          return 0;
          }
      }  
         
                  public boolean GetGpsStateImmadiate()
      {
          if(Packet[15]=='A')
              return true;
          if(Packet[15]=='V')
              return false;
          return false;
          
      }
        
    
}
