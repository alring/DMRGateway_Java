/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

/**
 *
 * @author Alexandr
 */
public class RRSPacket 
{
    public static int packetID = 0;
    byte Type=0x11; 
    //операции
            public class Operation 
        { 
    public static final int REGWITHPASSWORD=0x00;
    public static final int REGWITHOUTPASSWORD=0x03;
    public static final int DEREGISTRATION=0x01;
    public static final int ONLINECHECK=0x02;
    public static final int REGACK=0x80;
    public static final int ONLINEACK=0x82;
       }
    //ответы
    
    
    byte[] Packet;
    
    public  boolean ChecksumOk()
    {
        
        try
        {
                                int PayloadSize=(int)((int)((Packet[3]<<8) &0xFF00)|Packet[4]&0xFF); 
                                // int PayloadSize=(int)((int)(Packet[3]<<8)|Packet[4]);   
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
    
        public byte ChecksumACK(byte[] packet)
        {
           /*Corrected by Max*/
          byte Checksum = 0x00;
          
         
          
          
          for(int i = 1; i < 14; i++)
          {  
              
              Checksum += packet[i]; 
          }
          
          Checksum = (byte)(~Checksum);
          Checksum +=0x33;
         
    return (byte)Checksum; 
        }
      
        public  boolean IsRRSPacket()
    {
        return(Type==Packet[0]);
        
    }
        
      public int  GetOperation()        
      {
          return (byte)Packet[2];
      }
      
      public byte[] GenerateRegACK()
      {
       byte[] ack= new byte[16];
       ack[0]=Type;
       ack[1]=0x00;          //opcode
       ack[2]=(byte)0x80;    // 
       ack[3]=0x00;          // of bytes
       ack[4]=0x09;          // 
       ack[5]=GetStationIP()[0]; // payload
       ack[6]=GetStationIP()[1]; //
       ack[7]=GetStationIP()[2]; //
       ack[8]=GetStationIP()[3]; //
       ack[9]=0x00;     //ok     //
       ack[10]=0x00;             //
       ack[11]=0x00;             //
       ack[12]=0x00;             //
       ack[13]=(byte)0xC8;    //4 минуты
       ack[14]= ChecksumACK(ack);    //chk sum
       ack[15]=0x03; 
       return ack;
      }
      
        public int  GetStationID()        
      {
          return (((Packet[6]<<16)&0xFF0000)|((Packet[7]<<8)&0xFF00)|((Packet[8])&0xFF));
      }
        
          public byte[]  GetStationIP()        
      {
          byte[] ip= new byte[4];
          ip[0]= Packet[5];
          ip[1]= Packet[6];
          ip[2]= Packet[7];
          ip[3]= Packet[8];
               
          return ip;
      }
        
    public byte[] GenerateOnlineRequest(byte[] ipAddr)
    {
        byte[] request = new byte[11];
       
        request[0] = 0x11;  // RSS protocall
        request[1] = 0x00;  // opcode is "online status check"
        request[2] = 0x02;  
        request[3] = 0x00;  //of bytes
        request[4] = 0x04;  
        request[5] = ipAddr[3];  // Destination ip      
        request[6] = ipAddr[2];
        request[7] = ipAddr[1];
        request[8] = ipAddr[0];
        request[9] = GetChecksum(request); // check sum        
        request[10] = 0x03;  // messg end                          
                
        return request;
    }
        
    
    public RRSPacket(byte[] packet)
    {
       this.Packet=packet;
    }
    
    
}
