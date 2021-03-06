/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import com.enc.Aes128;
import com.google.gson.Gson;
import com.rtp.RtpMediaSession;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import org.apache.log4j.Logger;



public class ClientToServer 
{
         Socket socket;
         BufferedWriter writer;
         BufferedReader reader;
         static Logger logger = Logger.getLogger(ClientToServer.class);
         long id=0;
         Gateway gateway;
         boolean IsConnected=false;
         boolean BusyStatus = false;
         
         public String serverIP="";
         public int serverPort=0;
         static int j[] ;
        
         
         SocketReader socketReader;
         Thread socketReaderThread;
         
         
    public ClientToServer(Gateway gateway)
    {
            this.gateway=gateway;
            socketReader= new SocketReader();
    }
         
         
    
    public void Connect()
    {

              try
            {
            this.serverIP=Settings.getInstance().serverIP;
            this.serverPort= Settings.getInstance().serverPort;    
                
            SocketAddress adress= new InetSocketAddress(InetAddress.getByName(serverIP),serverPort);
            //socket = new Socket(InetAddress.getByName(serverIP),serverPort);
            socket = new Socket();
            socket.connect(adress,2000);
            reader = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(
            socket.getOutputStream()));
            socket.setSoTimeout(2000);
            socketReader= new SocketReader();
            socketReaderThread= new Thread(socketReader);
            socketReaderThread.start();
            IsConnected=true;
            logger.info("Сокет открыт");
            }
            catch(SocketTimeoutException e)
            {
            
            }
            catch(Exception ex)
               {
                  logger.error(ex.getMessage());
                   return;
               }

              
       //   new Thread(new SocketReader()).start();   
        //  IsConnected=true;
      
    }
    
        public void Close()
    {
            if(IsConnected)
            {
                IsConnected=false;
            try {
                logger.warn("Сокет закрыт.");
                socketReaderThread.interrupt();
                socket.close();
                writer.close();
                reader.close();
            } catch (IOException ex) {
                logger.error(ex);
            }
            catch(Exception x)
            {
            }
            
            }
           
    }
        
            
         public void RadioIsUnreachable()
         {
             
         }
        
                 public void SendGpsToServer(int fromid, double lat,double lon,double speed, int dir,long time,int errorType) 
    {
            
             if(!IsConnected)return;
        try {
            logger.info("Send GPS position to Server");
            id++;
            Command command= new Command();
            command.command="GpsPosition";
            command.id=id;
        //    command.arguments= new String[]{String.valueOf(fromid),String.valueOf(lat),String.valueOf(lon),String.valueOf(speed),String.valueOf(dir),String.valueOf(time)};
            
            command.arguments.put("sourceid", String.valueOf(fromid));
            command.arguments.put("latitude", String.valueOf(lat));
            command.arguments.put("longitude", String.valueOf(lon));
            command.arguments.put("speed", String.valueOf(speed));
            command.arguments.put("direction", String.valueOf(dir));
            command.arguments.put("utc", String.valueOf(time));
            command.arguments.put("warning", String.valueOf(errorType));

            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }
           
                 
                 
        
                   public void SendMobileRadioLiveStateToServer(int radioid,int state)   //0 - dead, 1- live
    {
                      
             if(!IsConnected)return;
        try {
            id++;
            Command command= new Command();
            command.command="MobileRadioLiveState";
            command.id=id;
           // command.arguments= new String[]{String.valueOf(radioid),String.valueOf(state)};
            
            command.arguments.put("sourceid", String.valueOf(radioid));
            command.arguments.put("state", String.valueOf(state));
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
            WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }
        
           
       public void SendAnserMobileRadio(int radioid,int state)   //0 - dead, 1- live
    {
                      
             if(!IsConnected)return;
        try {
            id++;
            Command command= new Command();
            command.command="MobileRadioLiveState";
            command.id=id;
           // command.arguments= new String[]{String.valueOf(radioid),String.valueOf(state)};
            
            command.arguments.put("sourceid", String.valueOf(radioid));
            command.arguments.put("state", String.valueOf(state));
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
            WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }           
                   
                   
                   
                   
         public void SendMessageToServer(int fromid, int toid,int type,String text) //1- групповое сообщение 0- индивидуальное
    {
             if(!IsConnected)return;
        try {
            id++;
            Command command= new Command();
            command.command="IncomingMessage";
            command.id=id;
           // command.arguments= new String[]{String.valueOf(fromid),String.valueOf(toid),String.valueOf(type),text};
            
            command.arguments.put("sourceid", String.valueOf(fromid));
            command.arguments.put("destinationid", String.valueOf(toid));
            command.arguments.put("type", String.valueOf(type));
            command.arguments.put("text", text);
            
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }
    
   
         public void SendCallToServer(final int fromid,final int toid,final int type, final String gatewayIP, final int direction)   //0 - private 1- group 2-all
    {

             logger.warn("send call to server");
             if(!IsConnected)return;
             try {
            id++;
            Command command= new Command();
            command.command="IncomingCall";
            command.id=id;
     //       command.arguments= new String[]{String.valueOf(fromid),String.valueOf(toid),String.valueOf(type),gatewayIP};
            command.arguments.put("sourceid", String.valueOf(fromid));
            command.arguments.put("destinationid", String.valueOf(toid));
            command.arguments.put("type", String.valueOf(type));
            command.arguments.put("radiogatewayip", gatewayIP);
            command.arguments.put("direction", String.valueOf(direction));
            
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
            WriteToSocket(s);
            } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
//        	        }
//	    }).start();  
        
    }
         
      public void SendMobileRadioStateToServer(int radioid,int state,String radioip)   //0 - offline 1- online
    {
          // if(!IsConnected)return;
        try {
            id++;
            Command command= new Command();
            command.command="MobileRadioState";
            command.id=id;
        //    command.arguments= new String[]{String.valueOf(radioid),String.valueOf(state),radioip};
            
            command.arguments.put("sourceid", String.valueOf(radioid));
            command.arguments.put("state", String.valueOf(state));
            command.arguments.put("radiogatewayip", radioip);
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
            
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }
                  
                 
                  
                  
        public void SendRadioGatewayStateToServer(String radioip, int state)   //0 - offline 1- online
    {
                      if(!IsConnected)return;
        try {
            id++;
            Command command= new Command();
            command.command="RadioGatewayState";
            command.id=id;
   //         command.arguments= new String[]{String.valueOf(state),radioip};
            
            command.arguments.put("state", String.valueOf(state));
            command.arguments.put("radiogatewayip", radioip);
            
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } 
        
        catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }
//                public void SendOnlineRadioGatewayToServer()   
//    {
//                
//    }
        
         
              public void SendStopCallToServer(final int fromid,final int toid,final int type, final String gatewayIP)
    {
//                    new Thread(new Runnable() {
//	        @Override
//	        public void run() {
        if(!IsConnected)return;
        
        try {
            id++;
            Command command= new Command();
            command.command="StopIncomingCall";
            command.id=id;
        //    command.arguments= new String[]{String.valueOf(fromid),String.valueOf(toid),String.valueOf(type),gatewayIP};
            
            command.arguments.put("sourceid", String.valueOf(fromid));
            command.arguments.put("destinationid", String.valueOf(toid));
            command.arguments.put("type", String.valueOf(type));
            command.arguments.put("radiogatewayip", gatewayIP);
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
        
//           	        }
//	    }).start();  
        
    }
         
             
              public void SendLockToServer(int answer,String fromip,int toid,int type, String gatewayIP)
              {
                  
                          if(!IsConnected)return;
                try {
            id++;
            Command command= new Command();
            command.command="LockPtt";
            command.id=id;
        //    command.arguments= new String[]{String.valueOf(answer),fromip,String.valueOf(toid),String.valueOf(type),gatewayIP};
            command.arguments.put("state", String.valueOf(answer));
            command.arguments.put("sourceip", fromip);
            command.arguments.put("destinationid", String.valueOf(toid));
            command.arguments.put("type", String.valueOf(type));
            command.arguments.put("radiogatewayip", gatewayIP);
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
              }              
              
              
              
              
              public void SendUnlockToServer(String fromip, String gatewayIP)
              {
               
                 if(!IsConnected)return;
                try {
            id++;
            Command command= new Command();
            command.command="UlockPtt";
            command.id=id;
        //    command.arguments= new String[]{String.valueOf(answer),fromip,String.valueOf(toid),String.valueOf(type),gatewayIP};
 
          
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        } 
                  
                  
                  
              }
              
              
              public void SendFreeState(String gatewayIP)
              {
                 
                   if(!IsConnected)return;
                  try 
                  {
                     id++;
                     Command command= new Command();
                     command.command="GatewayIsFree";
                     
                     command.id=id;
                    //    command.arguments= new String[]{String.valueOf(answer),fromip,String.valueOf(toid),String.valueOf(type),gatewayIP};
                    
                     command.arguments.put("radiogatewayip", gatewayIP);
            
                    Gson gson= new Gson();
                    String s= gson.toJson(command);
                    WriteToSocket(s);
                 } 
                 catch (Exception ex) 
                 {
                    logger.error(ex.getMessage(),ex.fillInStackTrace());
                    IsConnected=false;
                 }
              }
              
              public void SendCallEndToServer(String gatewayIP)
              {
                  
                   if(!IsConnected)return;
                  try 
                  {
                     id++;
                     Command command= new Command();
                     command.command="CallIsEnd";
                     command.id=id;
                    //
                     command.arguments.put("radiogatewayip", gatewayIP);
            
                    Gson gson= new Gson();
                    String s= gson.toJson(command);
                    WriteToSocket(s);
                 } 
                 catch (Exception ex) 
                 {
                    logger.error(ex.getMessage(),ex.fillInStackTrace());
                    IsConnected=false;
                 }
              }
              
              
              
              public void SendSuppressToServer(int answer,String fromip,int toid,int type, String gatewayIP)  // max added
              {
                  if(!IsConnected)return;
                  try 
                  {
                     id++;
                     Command command= new Command();
                     command.command="SuppressionIncomingCall";
                     command.id=id;
                    //    command.arguments= new String[]{String.valueOf(answer),fromip,String.valueOf(toid),String.valueOf(type),gatewayIP};
                     command.arguments.put("state", String.valueOf(answer));
                     command.arguments.put("sourceip", fromip);
                     command.arguments.put("destinationid", String.valueOf(toid));
                     command.arguments.put("type", String.valueOf(type));
                     command.arguments.put("radiogatewayip", gatewayIP);
                     command.arguments.put("pcgatewayip", type);
            
                    Gson gson= new Gson();
                    String s= gson.toJson(command);
                    WriteToSocket(s);
                 } 
                 catch (Exception ex) 
                 {
                    logger.error(ex.getMessage(),ex.fillInStackTrace());
                    IsConnected=false;
                 }
              }      

              
              
             public void SendSuppressDelirivedToServer(int answer,String fromip,int toid,int type, String gatewayIP)  // max added
              {
                  if(!IsConnected)return;
                  try 
                  {
                     id++;
                     Command command= new Command();
                     command.command="SuppressionIsDelirived";
                     command.id=id;
                    //    command.arguments= new String[]{String.valueOf(answer),fromip,String.valueOf(toid),String.valueOf(type),gatewayIP};
                     command.arguments.put("state", String.valueOf(answer));
                     command.arguments.put("sourceip", fromip);
                     command.arguments.put("destinationid", String.valueOf(toid));
                     command.arguments.put("type", String.valueOf(type));
                     command.arguments.put("radiogatewayip", gatewayIP);
            
                    Gson gson= new Gson();
                    String s= gson.toJson(command);
                    WriteToSocket(s);
                 } 
                 catch (Exception ex) 
                 {
                    logger.error(ex.getMessage(),ex.fillInStackTrace());
                    IsConnected=false;
                 }
              }
            
             
              
            public void SendIsBusyToServer(int answer,String fromip,int toid,int type, String gatewayIP)
    {   
        if(!IsConnected)return;
        try {
            id++;
            Command command= new Command();
            command.command="IsBusy";
            command.id=id;
        //    command.arguments= new String[]{String.valueOf(answer),fromip,String.valueOf(toid),String.valueOf(type),gatewayIP};
            command.arguments.put("state", String.valueOf(answer));
            command.arguments.put("sourceip", fromip);
            command.arguments.put("destinationid", String.valueOf(toid));
            command.arguments.put("type", String.valueOf(type));
            command.arguments.put("radiogatewayip", gatewayIP);
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }
           public void SendMessageIsDeliveredToServer(int timeId,String dispIP,int state)
    {
        if(!IsConnected)return;
        if(timeId==0xFFFF)
        {
            return;
        }
        try {
            id++;
            Command command= new Command();
            command.command="MessageIsDelivered";
            command.id=id;
      //      command.arguments= new String[]{String.valueOf(timeId),dispIP};
            
            command.arguments.put("id", String.valueOf(timeId));
            command.arguments.put("operatorip", dispIP);
            command.arguments.put("state", String.valueOf(state));

            
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }
         
           
    public void SendMakeKillToServer(int radioid,String dispIP,int state)  // max added
    {
           try
           {
            Command command= new Command();
            command.command="MakeKillIsDelivered";
            command.id=id;
      //      command.arguments= new String[]{String.valueOf(timeId),dispIP};
            
            command.arguments.put("id", String.valueOf(radioid));
            command.arguments.put("operatorip", dispIP);
            command.arguments.put("state", String.valueOf(state));    
            Gson gson= new Gson();
            String s= gson.toJson(command);
            WriteToSocket(s);
           }
           catch(Exception ex) 
           {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;             
           }
    }
    
   
    
    public void SendMakeRemoteMonitorToServer(int radioid,String dispIP,int state)
    {
        try{
            Command command= new Command();
            command.command="MakeRemoteIsDelivered";
            command.id=id;     
            command.arguments.put("id", String.valueOf(radioid));
            command.arguments.put("operatorip", dispIP);
            logger.warn("disp ip "+dispIP);
            command.arguments.put("state", String.valueOf(state)); 
            Gson gson= new Gson();
            String s= gson.toJson(command);
            WriteToSocket(s);
           }
           catch(Exception ex) 
           {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;             
           }
    }
    
    public void SendMakeLiveToServer(int radioid,String dispIP,int state)
    {
           try
           {
            Command command= new Command();
            command.command="MakeLiveIsDelivered";
            command.id=id;
            command.arguments.put("id", String.valueOf(radioid));
            command.arguments.put("operatorip", dispIP);
            command.arguments.put("state", String.valueOf(state)); 
            Gson gson= new Gson();
            String s= gson.toJson(command);
            WriteToSocket(s);
           }
           catch(Exception ex) 
           {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;             
           }
    }
    
              
    public void SendMyTypeToServer(int type) //указываем серверу что это соединение от шлюза 0x00-администратор 0x01 - шлюз 0x02 - диспетчер
         {
             if(!IsConnected)return;
        try {
            id++;
            Command command= new Command();
            command.command="MyType";
            command.id=id;
           // command.arguments= new String[]{"1"};
            command.arguments.put("type", "1");
            
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } 
          catch (Exception ex) 
        {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
        }
         
    public void GetRadioGatewaysFromServer()
    {
        if(!IsConnected)return;
        try {
            id++;
            Command command= new Command();
            command.command="GetRadioGateways";
            command.id=id;
        //    command.arguments= new String[]{String.valueOf(toid),String.valueOf(type)};
            Gson gson= new Gson();
            String s= gson.toJson(command);
WriteToSocket(s);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            IsConnected=false;
        }
           
    }
                  
                  
          
    public void OutgoingCallReply(Command comand, int port)
     {
                        if(!IsConnected)return;
            try 
        {
            Gson gson= new Gson();
            Command replycommand= new Command();
            replycommand.arguments=comand.arguments;
            replycommand.command="OutgoingCallReply";       
            replycommand.arguments.put("port", String.valueOf(port));            
            replycommand.id=id++;
            String s=gson.toJson(replycommand);
            WriteToSocket(s);
        } catch (Exception ex) 
        {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
        }               
     }
         
         
    synchronized public void WriteToSocket(String s)
    {
        if(IsConnected)
        try {
           
            s=Aes128.getInstance().encrypt(s);
           
            writer.write(s);
            writer.newLine();
            writer.flush();
            } 
        catch (Exception ex) {
            logger.error(ex.getMessage(),ex.fillInStackTrace());
            logger.warn("Закрытие сокета из-за ошибки записи"); 
            Close();

        }
    
    }
      
      
    public class SocketReader implements Runnable
    {
        
        class CommandProcessor implements Runnable
        {
          Command command;

         
          
            public CommandProcessor(Command command) {
                this.command = command;
            }

            @Override
            public void run() 
            {   
            
                
                try
                {
                    
                       
                    
                     if(command.command.equals("ConnectionLimit"))  
               {
                       gateway.serverHasLimit=true;
                       logger.warn("Превышен лимит подключений шлюзов к серверу");   
                       new WarningWindow(null, "Превышен лимит подключений шлюзов к серверу");
               }
                    
                    
                if(command.command.equals("MobileRadioMonitor"))
               { 
                 logger.warn("get radio mon");
                   int id=Integer.parseInt((String)command.arguments.get("mobileid")); 
                   RadioStation station = gateway.GetRadiostatinByID(id);
                   String dispip = (String)command.arguments.get("sourceip");
          
                   if(station!=null){gateway.rccService.MakeRemoteMonitorToRadio(station.PcRadioIPAdress, id, dispip);}
                   else 
                   {
                      
                       gateway.client.SendMakeRemoteMonitorToServer(id, dispip, 0);
                   }
               }
               
                if(command.command.equals("SetGpsState"))
               { 
                   
//                               new Thread(new Runnable() {
//	        @Override
//	        public void run() {
                   ///
                   
                   
                   int state =Integer.parseInt((String)command.arguments.get("state")); 
                   int id=Integer.parseInt((String)command.arguments.get("mobileid")); 
              //     if(state==0)gateway.locationService.StopReport(id);
                   if(state==1)
                   {
                       gateway.locationService.StartReport(id);
                       // gateway.locationService.ImmadiateRequest(id);
                   }
               ////    
//             }
//	    }).start();  
                   
               }
               
                 if(command.command.equals("CheckOnlineRadio"))
               {   
                   
                  
                   int id =Integer.parseInt((String)command.arguments.get("destinationid"));   
                   gateway.rrsService.CheckOnlineRadio(id);
                           
               }
                
                
                   if(command.command.equals("SetMobileRadioLiveState"))
               {   
                   
//                   String radioip=command.arguments[2];   
//                   int state =Integer.parseInt(command.arguments[1]);
//                   int id=Integer.parseInt(command.arguments[0]);
                   
                   int id =Integer.parseInt((String)command.arguments.get("mobileid"));   
                   int state =Integer.parseInt((String)command.arguments.get("state"));  
                   String pcip=(String)command.arguments.get("pcgatewayip");       
                   String radioip=(String)command.arguments.get("radiogatewayip"); 
                   String dispip = (String)command.arguments.get("sourceip");                      
                   
                 
                   
                   
                   if(state==0)
                   gateway.rccService.MakeKillToRadio(radioip, id, dispip);
                   if(state==1)
                   gateway.rccService.MakeLiveRadio(radioip, id, dispip);
                   if(state==2) // отложенная блокировка
                   gateway.rccService.MakeDeferredKillRadio(radioip,id);
                   if(state==3) // отложенная разблокировка
                   gateway.rccService.MakeStopDeferredKillRadio(radioip,id);
                           
               }

                 if(command.command.equals("OutgoingMessage"))
               {      
                   //int type=Integer.parseInt(command.arguments[3]);
                   //String dispatcherIP=command.arguments[0];
                   //int id=Integer.parseInt(command.arguments[7]);
                   
                   
                         String pcip=(String)command.arguments.get("pcgatewayip");       
                         String radioip=(String)command.arguments.get("radiogatewayip"); 
                         //int from=(Integer)(command.arguments.get("operatorid"));
                         int to=Integer.parseInt((String)(command.arguments.get("destinationid")));
                         int type=Integer.parseInt((String)(command.arguments.get("type")));
                         int id=Integer.parseInt((String)(command.arguments.get("textid")));
                         String text=(String)(command.arguments.get("text"));
                         String dispatcherIP=(String)(command.arguments.get("sourceip"));
                   
                   if(type==0) //индивидуальное сообщение
                   gateway.messageService.SendPrivatMessageToRadio(to,text,dispatcherIP,id);
                   if(type==1) //групповое сообщение 
                   gateway.messageService.SendGroupMessageToRadio(to,radioip,text,dispatcherIP,id);
               }
                
                 
                 
              
                 
                 
                 
                  if(command.command.equals("OutgoingCall"))
               {      
               
                  String fromip = (String)command.arguments.get("sourceip"); 
                  int fromid = Integer.parseInt((String)command.arguments.get("operatorid"));
                  int to=Integer.parseInt((String)command.arguments.get("destinationid"));  
                  int type=Integer.parseInt((String)command.arguments.get("type"));           
                  String pcip=(String)command.arguments.get("pcgatewayip");       
                  String radioip=(String)command.arguments.get("radiogatewayip"); 
                  
                  
                   int port=GetFreeUDPPort(10000, 20000);
                   OutgoingCallReply(command,port);

                           
                   RadioStationPC stationPC= gateway.GetRadiostatinPCByIP(String.valueOf(radioip));
                   
                   
                   if(stationPC.rtpMediaSession.IsActive) 
                   {
                    if(stationPC.rtpMediaSession.direction==0) stationPC.rtpMediaSession.StopSession();
                        if(stationPC.rtpMediaSession.direction==1)  
                     {
                            if(stationPC.rtpMediaSession.operatorid==fromid)stationPC.rtpMediaSession.StopSession();
                            else {
                      
                                SendIsBusyToServer(1, fromip, to, type, radioip);
                                return;}
                     }
                     
                   }

                   stationPC.rtpMediaSession.StartSession(port, fromid);         
                   
                   if(gateway.rccService.currentStatus == gateway.rccService.workStatus.INCOMING_CALL) // Если шлюз в состоянии приема
                    {                                                                                  // отправляем диспетчеру сообщение о подавлении входящего
                        gateway.client.SendSuppressToServer(1, fromip, to, type, radioip);
                    }
                   
                  
                  if(!gateway.rccService.MakeCallToRadio(fromip,radioip, to, type)) // если не удалось сгенерить вызов - шлем серверу сообщение от занятости
                      gateway.client.SendIsBusyToServer(1, fromip, to, type, radioip); // если 
                 
                  /*
                  else if(gateway.rccService.currentStatus == gateway.rccService.workStatus.INCOMING_CALL) // но если все-таки вернул тру
                    {                                                                                  
                      //  gateway.client.SendSuppressDelirivedToServer(1, fromip, to, type, radioip);        // и подавил прием, то шлем сигнал о завершении вызова
                    }     
                   */
                 
               }   
                   if( command.command.equals("StopOutgoingCall"))
               {  
                    
                   
                    String pcip=(String)command.arguments.get("pcgatewayip");       
                    String radioip=(String)command.arguments.get("radiogatewayip"); 
                    gateway.GetRadiostatinPCByIP(String.valueOf(radioip)).rtpMediaSession.StopSession();
                    gateway.rccService.MakeReleasePTT(radioip);
                    
                  
               }
                  
                   if(command.command.equals("IncomingCallReply"))
               {     
                //gateway.rccService.MakeCallToRadio(Integer.parseInt(comand.arguments[0]),Integer.parseInt(comand.arguments[1]));
                   int port= Integer.parseInt((String)command.arguments.get("port"));
                   String radioip=(String)command.arguments.get("radiogatewayip");
                    if(IsConnected)gateway.GetRadiostatinPCByIP(String.valueOf(radioip)).rtpMediaSession.StartSession(port-1, serverIP, port);
        
               }
                  if(command.command.equals("GetRadioGatewaysReply"))
               {
                   
                //gateway.radioStationsPC.clear();
                List<RadioStationPC> newradioStations= new ArrayList<RadioStationPC>();
                List<String> radiogateways=(ArrayList)command.arguments.get("radiogatewaylist");
                
                for(int i=0;i<radiogateways.size();i++)
                {
                newradioStations.add(new RadioStationPC(radiogateways.get(i),gateway));
                  for(int j=0;j<gateway.radioStationsPC.size();j++)
                {
                    if(newradioStations.get(i).IPAdress.equals(gateway.radioStationsPC.get(j).IPAdress))
                    {
                    newradioStations.get(i).selected_mic=gateway.radioStationsPC.get(j).selected_mic;
                    newradioStations.get(i).selected_speak=gateway.radioStationsPC.get(j).selected_speak;
                    }
                }
                
                }
                for(int i=0;i<gateway.radioStationsPC.size();i++)
                {
                    try
                    {
                gateway.radioStationsPC.get(i).status_thread.interrupt();
                gateway.radioStationsPC.get(i).rtpMediaSession.StopSession();
                    }
                    catch(Exception ex){};
                }
                
                gateway.radioStationsPC.clear();
                gateway.radioStationsPC=newradioStations;
                
                   gateway.RefreshGatewayPanels();
               }
                }
                catch(Exception ex)
                {
                logger.error(ex.getMessage(),ex.fillInStackTrace());
                }
                
               // logger.info("Command "+command.command.toString()+ " thread end");
            }
          
          
        }
        
        public SocketReader() 
        {
        }

        @Override
        public void run() 
        {
            while(!Thread.interrupted())
            { 
            try {
               
               String s= ReadData();
              
               if(s==null)break;
               Gson gson= new Gson();
               Command command = gson.fromJson(s, Command.class);
               
               new Thread(new CommandProcessor(command)).start();
              
               
                    

            } 
            catch(NullPointerException e)
            {
            logger.error(e.getMessage(),e.fillInStackTrace()); 
          //  break;
            }
            catch (Exception ex) 
            {
             logger.error(ex.getMessage(),ex.fillInStackTrace()); 
            // break;
            }


        }
            Close();
            logger.warn("socket read therad is closed");
            
        }
        
                public String ReadData() 
        {
            String s="";
            int null_count=0;
            while(true)
            {
                try
                {
                s= reader.readLine();
                if(s==null)
                {
//                    null_count++;
//                    if(null_count<3)s="";
//                    
//                    if(socket.isInputShutdown())
//                    {
//                    s=null;
                    break; 
//                    }
                    
                }
                 if(s!=null )s=Aes128.getInstance().decrypt(s);
                }
                catch(SocketTimeoutException x)
                {
                }
                catch(SocketException e)
                {
                if(!socket.isClosed())logger.error(e.getMessage(),e.fillInStackTrace());
                s=null;
                break;  
                }
                catch(IOException ex)
                {
                logger.error(ex);
                s=null;
                break;               
                }

            if( !"".equals(s))break;
            }
            return s;
        }
        

    }
    public class Command
        {
        public HashMap arguments = new HashMap();        
        long id;
        String command;
        //boolean isreply;
        //String[] arguments;
        }
           
    public int GetFreeUDPPort(int min, int max)
    {   
        Random random = new Random();
        int res; 
                while(true)
                {
        res= random.nextInt(max);
        res =   res < min ? res + min : res;
        try {
            DatagramSocket socket= new DatagramSocket(res);
            if(socket.isBound())
            {
                socket.disconnect();
                socket.close();
                
                break;
            }
        } catch (SocketException ex) {
            
        }
                }
        
        return res;
    
    }
    
    
}
