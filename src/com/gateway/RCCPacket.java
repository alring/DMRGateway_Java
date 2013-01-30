/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexandr
 */
public class RCCPacket 
{
        byte Type=0x02; 
    //операции
        public class Operation 
        {  
    public static final int CALLTARGET=0x0841;
    public static final int CALLREPLY=0x8841;
    public static final int KEY_OPER_REPLY=0x8041;
    public static final int RADIOMESSAGEQUERYREQUEST=0x0201;
    public static final int RADIOMESSAGEQUERYREPLY=0x8201;
    public static final int REMOVERADIOREQUEST=0x0842;
    public static final int REMOVERADIOREPLY=0x8842;
    public static final int KILLREPLY=0x8849;
    public static final int LIVEREPLY=0x884A;
    
   // public static final int CALLALERTREQUEST=0x0835;
    //public static final int LIVEREPLY=0x884A;
    //hrnp
    public static final int DATA=0x0000;
    public static final int DATA_ACK=0x0010;
    public static final int CONNECTION_ACCEPT=0x00FD;
    public static final int CONNECTION_CLOSE=0x00FB;
    public static final int CONNECTION_CLOSE_ACK=0x00FA;
    public static final int CONNECTION_REJECT=0x00FC;
    public static final int MASTER_BROADCAST=0x00FF;
    public static final int CN_REQUEST=0x00FE;
    
    
    
    
    
    public static final int RECEIVE_STATUS=0xB844;
    public static final int TRANSMIT_STATUS=0xB843;
    public static final int BROADCAST_STATUS_CONF_REPLY=0x80C9;
    
    //
    public static final int PRIVAT_CALL=0x0000;
    public static final int GROUP_CALL=0x0001;
    public static final int ALL_CALL=0x0002;
    
    public static final int STATUS_CHECK_REPLY=0x80ED;
    
    public static final int REMOTE_MONITOR_REPLY=0x8834;
    
    public static final int CALL_ALERT_REQUEST_ACK=0x8835;
    public static final int CALL_ALERT_REQUEST=0x0835;
    
    public static final int RADIO_CHECK_REQUEST_ACK=0x8835;
    public static final int RADIO_CHECK_REQUEST=0x0833;
    
        }
      public class CallStatus
        {  
    public static final int START_CALL=0x0001;
    public static final int END_CALL=0x0003;
    public static final int HANGTIME=0x0002;
        }
            public class CallReplyStatus
        {  
    public static final int SUCCES=0x0000;
    public static final int FAILURE=0x0001;

        }
         public class Key
        {  
    public static final int PTT=0x001E;
   
        }

         public class KeyOper
         {
             public static final int PRESS = 0x0001;
             public static final int RELEASE = 0x0000;
         }
    
    byte[] Packet;
    HRNPPacket hrnpPacket= new HRNPPacket();

    public RCCPacket(byte[] packet) 
            
    {
       List<Byte>array= new ArrayList<Byte>();        
        for(int i=0;i<packet.length;i++)
        {
        array.add(packet[i]);
        }

        //int index=array.lastIndexOf(new Byte((byte)0x03));
        int index=((int)(packet[9]))&0xFF;
        
        if(index>=12)array=array.subList(0,index);
        
        if(index==-1 || index<12)
        {
        if(packet.length>=12)
        {
            Packet= new byte[12];
         for(int i=0;i<12;i++)
        {
        this.Packet[i]=packet[i];
        }
        }
        else this.Packet=packet;
            
        return;
        }
                
        this.Packet= new byte[array.size()];
        for(int i=0;i<array.size();i++)
        {
        this.Packet[i]=(byte)array.get(i);
        }
        
 
    }
        public RCCPacket() 
    {
    }
    
        
        public  boolean ChecksumOk()
    {
         int hrnp_sum = hrnpPacket.GetChecksum(Packet,Packet.length);
         boolean hrnp_sum_ok= false;
         boolean rcc_sum_ok= false;
         try
         {
             int sum=(Packet[10]<<8|((int)(Packet[11])&0xFF))&0xFFFF;
         if(sum==hrnp_sum)hrnp_sum_ok=true;
         }
          catch(Exception ex)
        {
        
        }
        
        if(Packet.length>12)
         {
        try
        {
                                 int PayloadSize=(int)(Packet[15]);   
                                 byte Checksum = GetChecksum(Packet,12);
                                 if(Packet[17+PayloadSize]==Checksum)rcc_sum_ok= true;
        }
        catch(Exception ex)
        {
        rcc_sum_ok= false;
        }
        
        return hrnp_sum_ok&rcc_sum_ok;
      }
        else
        {
        return hrnp_sum_ok;
        }
                                 
    
    }
    
      public int GetKeyOper()
      {
           return ((Packet[25]));
      }
        
        
        
        
      public  byte GetChecksum(byte[] packet,int d) 
    {
                                
                                 int PayloadSize=(int)(packet[d+3]&0xFF);   
                                 byte Checksum = (byte)(packet[1+d]+packet[2+d]);
                                 Checksum+=packet[3+d]+packet[4+d];
                                 int i=0;
                                 for(i=0;i<PayloadSize;i++)
                                 {
                                   Checksum+=packet[5+i+d];
                                     
                                 }
                                 Checksum= (byte)(~(Checksum));
                                 Checksum+=0x33;
        
    return (byte)Checksum;
    }
      
      boolean IsRCCPacket()
      {
          if(Packet.length>12)
           return(Type==Packet[12]);
          else return false;
      }
      
      boolean IsHRNP()
      {
      return(Packet.length==12);
      
      }
      
      
         public int  GetOperation()        
      {
          return ((Packet[14]<<8)|((int)(Packet[13])&0xFF))&0xFFFF;
      }
         
          public int  GetHRNPOperation()        
      {
          return ((int)(Packet[3]))&0xFF;
      }
          
              public byte[] GenerateRemoteMonitor(int toid)
       {
           Packet= new byte[11];
           Packet[0]=(byte)Type;
           //почемуто в пакете байты должны быть на оборот
           Packet[1]=0x34;
           Packet[2]=(byte)0x08;
           Packet[3]=0x04;
           Packet[4]=0x00;
           Packet[5]=(byte)((toid)&0xff);
           Packet[6]=(byte)((toid>>8)&0xff);
           Packet[7]=(byte)((toid>>16)&0xff);
           Packet[8]=(byte)(byte)((toid>>24)&0xff);
           Packet[9]=GetChecksum(Packet,0);
           Packet[10]=(byte)(0x03);
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
       }
              
                            public byte[] GenerateRadioCheck(int toid)
       {
           Packet= new byte[11];
           Packet[0]=(byte)Type;
           //почемуто в пакете байты должны быть на оборот
           Packet[1]=0x33;
           Packet[2]=(byte)0x08;
           Packet[3]=0x04;
           Packet[4]=0x00;
           Packet[5]=(byte)((toid)&0xff);
           Packet[6]=(byte)((toid>>8)&0xff);
           Packet[7]=(byte)((toid>>16)&0xff);
           Packet[8]=(byte)(byte)((toid>>24)&0xff);
           Packet[9]=GetChecksum(Packet,0);
           Packet[10]=(byte)(0x03);
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
       }
          
          
                    public byte[] GenerateFunctionStatusCheck(int target)
       {
           Packet= new byte[8];
           Packet[0]=(byte)Type;
           //почемуто в пакете байты должны быть на оборот
           Packet[1]=(byte)0xED;
           Packet[2]=(byte)0x00;
           Packet[3]=0x01;
           Packet[4]=0x00;
           Packet[5]=(byte)((target)&0xff);
           Packet[6]=GetChecksum(Packet,0);
           Packet[7]=(byte)(0x03);
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
       }
          
          public byte[] GenerateKill(int toid)
       {
           Packet= new byte[11];
           Packet[0]=(byte)Type;
           //почемуто в пакете байты должны быть на оборот
           Packet[1]=0x49;
           Packet[2]=(byte)0x08;
           Packet[3]=0x04;
           Packet[4]=0x00;
           Packet[5]=(byte)((toid)&0xff);
           Packet[6]=(byte)((toid>>8)&0xff);
           Packet[7]=(byte)((toid>>16)&0xff);
           Packet[8]=(byte)(byte)((toid>>24)&0xff);
           Packet[9]=GetChecksum(Packet,0);
           Packet[10]=(byte)(0x03);
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
           
       }
          
                    public byte[] GenerateLive(int toid)
       {
           Packet= new byte[11];
           Packet[0]=(byte)Type;
           //почемуто в пакете байты должны быть на оборот
           Packet[1]=0x4A;
           Packet[2]=(byte)0x08;
           Packet[3]=0x04;
           Packet[4]=0x00;
           Packet[5]=(byte)((toid)&0xff);
           Packet[6]=(byte)((toid>>8)&0xff);
           Packet[7]=(byte)((toid>>16)&0xff);
           Packet[8]=(byte)(byte)((toid>>24)&0xff);
           Packet[9]=GetChecksum(Packet,0);
           Packet[10]=(byte)(0x03);
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
           
       }
         
         
          public byte[] GenerateMakeCall(int toid,int type)
       {
           if(type==0x02)toid=16777215;
           Packet= new byte[12];
           Packet[0]=(byte)Type;
           //почемуто в пакете байты должны быть на оборот
           Packet[1]=0x41;
           Packet[2]=(byte)0x08;
           Packet[3]=0x05;
           Packet[4]=0x00;
           Packet[5]=(byte)type;
           Packet[6]=(byte)((toid)&0xff);
           Packet[7]=(byte)((toid>>8)&0xff);
           Packet[8]=(byte)((toid>>16)&0xff);
           Packet[9]=(byte)(byte)((toid>>24)&0xff);
           Packet[10]=GetChecksum(Packet,0);
           Packet[11]=(byte)(0x03);

           
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
       }
                    public byte[] GeneratePressPTT()
       {
 
           Packet= new byte[9];
           Packet[0]=(byte)Type;
           //почемуто в пакете байты должны быть на оборот
           Packet[1]=0x41;
           Packet[2]=(byte)0x00;
           Packet[3]=0x02;
           Packet[4]=0x00;
           Packet[5]=0x1E;   //ptt
           Packet[6]=0x01;   //press
           Packet[7]=GetChecksum(Packet,0);
           Packet[8]=(byte)(0x03);

           
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
       }
              public byte[] GenerateReleasePTT()
       {
 
           Packet= new byte[9];
           Packet[0]=(byte)Type;
           //почемуто в пакете байты должны быть на оборот
           Packet[1]=0x41;
           Packet[2]=(byte)0x00;
           Packet[3]=0x02;
           Packet[4]=0x00;
           Packet[5]=0x1E;   //ptt
           Packet[6]=0x00;   //Release
           Packet[7]=GetChecksum(Packet,0);
           Packet[8]=(byte)(0x03);

           
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
       }
          
         public byte[]  GenerateEnableRXTX()
         {
           Packet= new byte[12];
           Packet[0]=(byte)Type;
           Packet[1]=(byte)0xC9;
           Packet[2]=(byte)0x10;
           Packet[3]=0x05;
           Packet[4]=0x00;
           Packet[5]=2;
           Packet[6]=0x00;
           Packet[7]=0x01;
           Packet[8]=0x01;
           Packet[9]=0x01;
           Packet[10]=0x4f;//GetChecksum(Packet);
           Packet[11]=(byte)(0x03);
           
           Packet=hrnpPacket.GenerateDataPacket(Packet);
           
           return Packet;
         
         }
         
               public byte[] GenerateHRNPACK()
      {
          return hrnpPacket.GenerateACK();
      }
          
         public byte[] GenerateConnect()
         {
         return hrnpPacket.GenerateConnectPacket();
         
         }
          
           public int GetDestinationID()
        {
            
        return (((Packet[6]<<24)&0xFF000000)|((Packet[7]<<16)&0xFF0000)|((Packet[8]<<8)&0xFF00)|((Packet[9])&0xFF));
        }
          public int GetKillRadioID()
        {
            
        return (((Packet[20]<<16)&0xFF0000)|((Packet[19]<<8)&0xFF00)|((int)(Packet[18])&0xFF))&0xFFFFFF;
        }
       
       public int GetCallType()
       {
       return ((Packet[19]));
       }
       
        public int GetCallStatus()
       {
       return ((Packet[17]));
       }
        
        public int GetCallTarget()
       {
       return (((Packet[23]<<16)&0xFF0000)|((Packet[22]<<8)&0xFF00)|((int)(Packet[21])&0xFF))&0xFFFFFF;
       }
         public int GetCallSender()
       {
       return (((Packet[27]<<16)&0xFF0000)|((Packet[26]<<8)&0xFF00)|((int)(Packet[25])&0xFF))&0xFFFFFF;
       }
                  public int GetCallTargetExtPTT()
       {
       return (((Packet[25]<<16)&0xFF0000)|((Packet[24]<<8)&0xFF00)|((int)(Packet[23])&0xFF))&0xFFFFFF;
       }
          public int GetCallTypeExtPTT()
       {
       return ((Packet[21]));
       }
         
         public int GetMakeCallReply()
       {
       return ((Packet[17]));
       }
          public int GetKey()
       {
       return ((Packet[21]));
       }

              public int GetCheckFunctionResult()
       {
       return ((Packet[17]));
       }
             public int GetCheckFunctionTarget()
       {
       return ((Packet[18]));
       }
            public int GetCheckFunctionStatus()
       {
        return ((Packet[19]));
       }
            
        public int GetRemoteMonitorResult()
       {
        return ((Packet[17]));
       }
                public int GetRemoteMonitorTargetID()
       {
        return (((Packet[21]<<16)&0xFF0000)|((Packet[20]<<8)&0xFF00)|((int)(Packet[19])&0xFF))&0xFFFFFF;
       }
}
