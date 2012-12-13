/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexandr
 */
public class MessagePacket 
{
  byte[] Packet;
  int Type=0x09;
  int MessageID=0;
  
   public class Operation 
  {  
    public static final int SENDPRIVATMSG=0x80A1;
    public static final int SENDPRIVATMSGACK=0x00A2;
    public static final int SENDGROUPMSG=0x80B1;
    public static final int SENDGROUPMSGACK=0x00B2;

        }
 
        public MessagePacket(byte[] packet) 
    {
        this.Packet=packet;
    }
        
       public MessagePacket() 
    {
    }
       public byte[] GeneratePrivateMessage(int id,String myip, String destip, String msg)
       {
           byte[] message;
        try {
            message = msg.getBytes("UTF-16LE");
           // message= new byte[]{0x48,0,0x49,0};
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
           Packet= new byte[19+message.length];
           Packet[0]=(byte)Type;
           Packet[1]=0x00;
           Packet[2]=(byte)0xA1;
           int payloadsize=message.length+12;
           Packet[3]=(byte)((payloadsize>>8)&0xff);
           Packet[4]=(byte)(payloadsize&0xff);
           Packet[5]=(byte)((id>>24)&0xff);
           Packet[6]=(byte)((id>>16)&0xff);
           Packet[7]=(byte)((id>>8)&0xff);
           Packet[8]=(byte)((id)&0xff);
           Packet[9]=(byte)(Integer.parseInt(destip.split("\\.")[0]));
           Packet[10]=(byte)(Integer.parseInt(destip.split("\\.")[1]));
           Packet[11]=(byte)(Integer.parseInt(destip.split("\\.")[2]));
           Packet[12]=(byte)(Integer.parseInt(destip.split("\\.")[3]));
           Packet[13]=(byte)(Integer.parseInt(myip.split("\\.")[0]));
           Packet[14]=(byte)(Integer.parseInt(myip.split("\\.")[1]));
           Packet[15]=(byte)(Integer.parseInt(myip.split("\\.")[2]));
           Packet[16]=(byte)(Integer.parseInt(myip.split("\\.")[3]));
           
           for(int i=0;i<message.length;i++)
           {
               Packet[17+i]=message[i];
           }
           Packet[17+message.length]=GetChecksum(Packet);
           Packet[17+message.length+1]=0x03;


           
           
           return Packet;
       }
       
              public byte[] GenerateGroupMessage(int id,String myip, int destid, String msg)
       {
           byte[] message;
        try {
            message = msg.getBytes("UTF-16LE");
           // message= new byte[]{0x48,0,0x49,0};
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
           Packet= new byte[19+message.length];
           Packet[0]=(byte)Type;
           Packet[1]=0x00;
           Packet[2]=(byte)0xB1;
           int payloadsize=message.length+12;
           Packet[3]=(byte)((payloadsize>>8)&0xff);
           Packet[4]=(byte)(payloadsize&0xff);
           Packet[5]=(byte)((id>>24)&0xff);
           Packet[6]=(byte)((id>>16)&0xff);
           Packet[7]=(byte)((id>>8)&0xff);
           Packet[8]=(byte)((id)&0xff);
           Packet[9]=(byte)((destid>>24)&0xFF);
           Packet[10]=(byte)((destid>>16)&0xFF);
           Packet[11]=(byte)((destid>>8)&0xFF);
           Packet[12]=(byte)(destid&0xFF);
           Packet[13]=(byte)(Integer.parseInt(myip.split("\\.")[0]));
           Packet[14]=(byte)(Integer.parseInt(myip.split("\\.")[1]));
           Packet[15]=(byte)(Integer.parseInt(myip.split("\\.")[2]));
           Packet[16]=(byte)(Integer.parseInt(myip.split("\\.")[3]));
           
           for(int i=0;i<message.length;i++)
           {
               Packet[17+i]=message[i];
           }
           Packet[17+message.length]=GetChecksum(Packet);
           Packet[17+message.length+1]=0x03;


           
           
           return Packet;
       }
       
       
        
        
                public  boolean ChecksumOk()
    {
      
        try
        {
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

                                 int PayloadSize=(int)((int)((packet[3]<<8)&0xFF00)|packet[4]&0xFF);   
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
      
    public boolean  IsMessagePacket()
    {
        return(Type==Packet[0]);
    }
             public int  GetOperation()        
      {
          return (((Packet[1]<<8)&0xff00)|(int)(Packet[2])&0xff);
      }
             
public String GetMessage()
        {
           int payloadsize=(((Packet[3]<<8)&0xff00)|(int)(Packet[4])&0xff);
           byte[] text = new byte[payloadsize-12];
           for(int i=0;i<text.length;i++)
           {
               text[i]=Packet[17+i];
           }
           String s;
        try {
             s=new String(text, "UTF-16LE");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
           return s;
        }
       public int GetDestinationID()
        {
             
        return ((Packet[10]<<16)&0xFF0000|(Packet[11]<<8)&0xFF00|(Packet[12])&0xFF);
        }
       
       public int GetSourceID()
       {
       return ((Packet[14]<<16)&0xFF0000|(Packet[15]<<8)&0xFF00|(Packet[16])&0xFF);
       }
              public int GetRequestID()
       {
       return (int)((Packet[5]<<24)&0xFF000000|(Packet[6]<<16)&0xFF0000|(Packet[7]<<8)&0xFF00|(Packet[8])&0xFF);
       }
            
             public int GetPrivatResult()
       {
       return (Packet[17]);
       }
                          public int GetGroupResult()
       {
       return (Packet[13]);
       }
             
}
