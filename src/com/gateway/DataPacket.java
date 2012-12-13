/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Alexandr
 */
public class DataPacket 
{
    byte Type=0x13; 
     byte[] Packet;
     
                 public class Operation 
        { 
    public static final int DTS_REQUEST=0x01;
    public static final int DTS_ANSWER=0x11;
    public static final int DATA_FLAG_TRANSMIT=0x02;
    public static final int DATA_FLAG_ANSWER=0x12;
       }
     
    
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
      
            public int  GetOperation()        
      {
          return Packet[2];
      }
    
        public  boolean IsDataPacket()
    {
        return(Type==Packet[0]);
        
    }
        
          public DataPacket(byte[] packet)
    {
       this.Packet=packet;
    }
        public DataPacket()
    {
        
    }
                 public byte[] GenerateFragTransmit(String myip, String destip,byte[] msg, int block_num)
       {
           int length=msg.length-448*block_num;
           if(length<=448)
           Packet= new byte[17+length];
           else
           Packet= new byte[17+448];    
           Packet[0]=(byte)Type;
           Packet[1]=(byte)0xA0;
           Packet[2]=(byte)0x02;
           int payloadsize=10;
           
           if(length<=448)
           payloadsize+=length;
           else
           payloadsize+=448;
 
           Packet[3]=(byte)((payloadsize>>8)&0xff);
           Packet[4]=(byte)(payloadsize&0xff);
           Packet[5]=(byte)(Integer.parseInt(destip.split("\\.")[0]));
           Packet[6]=(byte)(Integer.parseInt(destip.split("\\.")[1]));
           Packet[7]=(byte)(Integer.parseInt(destip.split("\\.")[2]));
           Packet[8]=(byte)(Integer.parseInt(destip.split("\\.")[3]));
           Packet[9]=(byte)(Integer.parseInt(myip.split("\\.")[0]));
           Packet[10]=(byte)(Integer.parseInt(myip.split("\\.")[1]));
           Packet[11]=(byte)(Integer.parseInt(myip.split("\\.")[2]));
           Packet[12]=(byte)(Integer.parseInt(myip.split("\\.")[3]));

           Packet[13]=(byte)((block_num>>8)&0xff);
           Packet[14]=(byte)(block_num&0xff);
           
           int j=0;
           int i=0;
           List<Byte> current_block= new ArrayList<Byte>();
           
           for(i=block_num*448, j=0;j<448;i++,j++)
           {
               if(j==(length))break;
               current_block.add(msg[i]);
               
           }
           
           for(i=0;i<current_block.size();i++)
           {
           Packet[15+i]=current_block.get(i).byteValue();
           }
           Packet[15+i]=GetChecksum(Packet);
           Packet[16+i]=0x03;
       
           return Packet;
       }  
                 
           public byte[] GenerateFragAnswer(String destip,String sourceip,int num_block, int result )
       {
           Packet= new byte[18];
           Packet[0]=(byte)Type;
           Packet[1]=(byte)0xA0;
           Packet[2]=(byte)0x12;
           int payloadsize=11;
           Packet[3]=(byte)((payloadsize>>8)&0xff);
           Packet[4]=(byte)(payloadsize&0xff);
           Packet[5]=(byte)(Integer.parseInt(destip.split("\\.")[0]));
           Packet[6]=(byte)(Integer.parseInt(destip.split("\\.")[1]));
           Packet[7]=(byte)(Integer.parseInt(destip.split("\\.")[2]));
           Packet[8]=(byte)(Integer.parseInt(destip.split("\\.")[3]));
           Packet[9]=(byte)(Integer.parseInt(sourceip.split("\\.")[0]));
           Packet[10]=(byte)(Integer.parseInt(sourceip.split("\\.")[1]));
           Packet[11]=(byte)(Integer.parseInt(sourceip.split("\\.")[2]));
           Packet[12]=(byte)(Integer.parseInt(sourceip.split("\\.")[3]));
           Packet[13]=(byte)((num_block>>8)&0xff);
           Packet[14]=(byte)(num_block&0xff);
           Packet[15]=(byte)result;

           Packet[16]=GetChecksum(Packet);
           Packet[17]=0x03;
       
           return Packet;
       }  
                 
        
                  public byte[] GenerateDTSAnswer(String destip,String sourceip )
       {
           Packet= new byte[16];
           Packet[0]=(byte)Type;
           Packet[1]=(byte)0xA0;
           Packet[2]=(byte)0x11;
           int payloadsize=9;
           Packet[3]=(byte)((payloadsize>>8)&0xff);
           Packet[4]=(byte)(payloadsize&0xff);
           Packet[5]=(byte)(Integer.parseInt(destip.split("\\.")[0]));
           Packet[6]=(byte)(Integer.parseInt(destip.split("\\.")[1]));
           Packet[7]=(byte)(Integer.parseInt(destip.split("\\.")[2]));
           Packet[8]=(byte)(Integer.parseInt(destip.split("\\.")[3]));
           Packet[9]=(byte)(Integer.parseInt(sourceip.split("\\.")[0]));
           Packet[10]=(byte)(Integer.parseInt(sourceip.split("\\.")[1]));
           Packet[11]=(byte)(Integer.parseInt(sourceip.split("\\.")[2]));
           Packet[12]=(byte)(Integer.parseInt(sourceip.split("\\.")[3]));

           Packet[13]=(byte)(0x00);


           Packet[14]=GetChecksum(Packet);
           Packet[15]=0x03;
           
           return Packet;
       } 
                 
                 
         public byte[] GenerateDTSRequest(String myip, String destip,byte[] msg)
       {
           String filename_= String.valueOf(Calendar.getInstance().getTimeInMillis())+".data";
      
           byte[] filename;
           byte[] message =msg;
        try {
            filename = filename_.getBytes("UTF-8");
      //      message = msg.getBytes("UTF-8");
           // message= new byte[]{0x48,0,0x49,0};
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
        
           Packet= new byte[17+filename.length];
           Packet[0]=(byte)Type;
           Packet[1]=(byte)0xA0;
           Packet[2]=(byte)0x01;
           int payloadsize=10+filename.length;
           Packet[3]=(byte)((payloadsize>>8)&0xff);
           Packet[4]=(byte)(payloadsize&0xff);
           Packet[5]=(byte)(Integer.parseInt(destip.split("\\.")[0]));
           Packet[6]=(byte)(Integer.parseInt(destip.split("\\.")[1]));
           Packet[7]=(byte)(Integer.parseInt(destip.split("\\.")[2]));
           Packet[8]=(byte)(Integer.parseInt(destip.split("\\.")[3]));
           Packet[9]=(byte)(Integer.parseInt(myip.split("\\.")[0]));
           Packet[10]=(byte)(Integer.parseInt(myip.split("\\.")[1]));
           Packet[11]=(byte)(Integer.parseInt(myip.split("\\.")[2]));
           Packet[12]=(byte)(Integer.parseInt(myip.split("\\.")[3]));

           int numofframes=message.length/448;
           if((message.length%448)!=0)numofframes++;
           Packet[13]=(byte)((numofframes>>8)*0xFF);
           Packet[14]=(byte)((numofframes)*0xFF);
           
           
           
           for(int i=0;i<filename.length;i++)
           {
               Packet[15+i]=filename[i];
           }
           Packet[15+filename.length]=GetChecksum(Packet);
           Packet[15+filename.length+1]=0x03;


           
           
           return Packet;
       }
         
         
        public String GetDestitation()
        {
            
           return String.valueOf((Packet[5])&0xFF)+"."+String.valueOf((Packet[6])&0xFF)+"."+String.valueOf((Packet[7])&0xFF)+"."+String.valueOf((Packet[8])&0xFF);  
        }
        
       public String GetSource()
       {
         return String.valueOf((Packet[9])&0xFF)+"."+String.valueOf((Packet[10])&0xFF)+"."+String.valueOf((Packet[11])&0xFF)+"."+String.valueOf((Packet[12])&0xFF);  
       }
               
       public int GetResult()
       {
           return ((Packet[13])&0xFF);
       }
              public int GetBlockNum()
       {
           return (((Packet[13]<<8)&0xFF00) | ((Packet[14]<<8)&0xFF));
       }
         public byte[] GetData()
       {
           int dataSize=(int)((int)((Packet[3]<<8) &0xFF00)|Packet[4]&0xFF) -10; 
           byte[] data= new byte[dataSize];
           for(int i=0;i<dataSize;i++)
           {
               data[i]=(Packet[15+i]);
           }
           
           return data;
       }    
    
}
