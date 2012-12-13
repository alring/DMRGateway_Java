/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

/**
 *
 * @author Alexandr
 */
public class HRNPPacket 
{
 
              public byte[] GenerateConnectPacket()
       {
 
           byte[] Packet= new byte[12];
           Packet[0]=0x7e;
           Packet[1]=0x00;   //version
           Packet[2]=(byte)0x00; //block
           Packet[3]=(byte)0xFE; //connect 
           Packet[4]=0x20;   //src id
           Packet[5]=(byte)0x10; //dest id (10-master)
           Packet[6]=0x00; //PN
           Packet[7]=0x00; //PN
           Packet[8]=0x00;   //length
           Packet[9]=0x0C;   //length
           Packet[10]=0x00;   
           Packet[11]=0x00;   
           int checksum=GetChecksum(Packet,10);
           Packet[10]=(byte)((checksum>>8)&0xFF);
           Packet[11]=(byte)(checksum&0xFF);
           

           return Packet;
       }
              
              public byte[] GenerateACK()
       {
 
           byte[] Packet= new byte[12];
           Packet[0]=0x7e;
           Packet[1]=0x00;   //version
           Packet[2]=(byte)0x00; //block
           Packet[3]=(byte)0x10; //ack 
           Packet[4]=0x20;   //src id
           Packet[5]=(byte)0x10; //dest id (10-master)
           Packet[6]=0x00; //PN
           Packet[7]=0x00; //PN
           Packet[8]=0x00;   //length
           Packet[9]=0x0C;   //length
           Packet[10]=0x00;   
           Packet[11]=0x00;   
           int checksum=GetChecksum(Packet,10);
           Packet[10]=(byte)((checksum>>8)&0xFF);
           Packet[11]=(byte)(checksum&0xFF);
           

           return Packet;
       }
              
          public byte[] GenerateDataPacket(byte[] rcc_pack)
       {
           byte[] Packet= new byte[12+rcc_pack.length];
           Packet[0]=0x7e;
           Packet[1]=0x00;   //version
           Packet[2]=(byte)0x00; //block
           Packet[3]=(byte)0x00; //type data
           Packet[4]=0x20;   //src id
           Packet[5]=(byte)0x10; //dest id (10-master)
           Packet[6]=0x00; //PN
           Packet[7]=0x00; //PN
           Packet[8]=0x00;   //length
           Packet[9]=(byte)(0x0C+rcc_pack.length);   //length  
           Packet[10]=0;
           Packet[11]=0;
           for(int i=0;i<rcc_pack.length;i++)
           {
           Packet[12+i]=rcc_pack[i];
           }
           int checksum=GetChecksum(Packet,Packet.length);
           Packet[10]=(byte)((checksum>>8)&0xFF);
           Packet[11]=(byte)(checksum&0xFF);

           return Packet;
       }
    
    
          public  int GetChecksum(byte[] packet,int len) 
    {
          int Checksum = 0;
          int i=0;
          while (len>1)
          {
                if(i!=10)
                {
                  int add=((packet[i]<<8)|((int)(packet[i+1]))&0xFF)&0xFFFF;
                Checksum+=add;
                if((Checksum>>16)!=0)
                    Checksum=(Checksum&0xFFFF)+(Checksum>>16);
                }
                i+=2;
                len-=2;
          }
          if(len>0)Checksum+=(((int)packet[i])&0xFF)<<8;
          
          while((Checksum>>16)>0) Checksum=(Checksum&0xFFFF)+(Checksum>>16);

        
    return (~Checksum)&0xFFFF;
    }
}
