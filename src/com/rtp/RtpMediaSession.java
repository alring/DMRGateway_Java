/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtp;


import com.gateway.RadioStationPC;
import com.sound.SoundInOut;
import com.sound.SoundManager;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexandr
 */



public class RtpMediaSession implements RtpListener
{

    	/** The RTP manager for this application. */
    	protected RtpManager rtpManager;
        
        
       public RadioStationPC radioStationPC;
        
        protected SoundInOut soundInOut;
        public int direction=-1;
        public int operatorid=-1;
        public boolean IsActive=false;
        
        static Logger logger = Logger.getLogger(RtpMediaSession.class);
        int SN=1;
        long lastSentTime = System.nanoTime();
        int SSRC= (int)(Math.random()*999999);
        
        public long sessionId;
        
        boolean soundEnable= false;

	/** A single RTP session. */
	protected RtpSession rtpSession;

        public RtpMediaSession(RadioStationPC radioStationPC)
        {
            this.radioStationPC=radioStationPC;
        try {
            rtpManager =  new RtpManager(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception ex) {
            logger.error(ex);
        }
        //soundManager= mediaManager.callListener.sipMeetingManager.soundmanager;
        soundInOut= new SoundInOut(this);
        }
        
//        public int GetID()
//        {
//        return ID;
//        }

        
        
       synchronized public void StartSession(int myRtpRecvPort,String remoteIpAddress,int remoteRtpRecvPort)
        {
            try
            {
      //  logger.info("Active="+IsActive) ;       
        if(IsActive)
            this.StopSession();
         rtpSession = rtpManager.createRtpSession(myRtpRecvPort,
	remoteIpAddress, remoteRtpRecvPort);
        lastSentTime = System.nanoTime();
	rtpSession.addRtpListener(this);
	rtpSession.receiveRTPPackets();

    //    soundInOut.closeLines();
        soundInOut.OpenMicLine();
        direction=0;
        sessionId=Calendar.getInstance().getTimeInMillis(); 
        IsActive=true;
        logger.info("Входящая сессия ID="+sessionId+" порт:"+remoteRtpRecvPort);
            }
            catch(Exception e)
            {
            logger.error(e);
            }
      //  logger.info("Active="+IsActive) ;  
        }
        
           synchronized     public void StartSession(int myRtpRecvPort,int operatorid)
        {
            if(IsActive)this.StopSession();
            soundInOut.OpenSpeakLine();
            try
            {
        rtpSession = rtpManager.createRtpSession(myRtpRecvPort);
	rtpSession.addRtpListener(this);
	rtpSession.receiveRTPPackets();
        sessionId=Calendar.getInstance().getTimeInMillis();
        direction=1;
        this.operatorid=operatorid;
        logger.info("Исходящая сессия ID="+sessionId+" порт:"+myRtpRecvPort);
        IsActive=true;
            }
            catch(Exception e)
            {
            logger.error(e);
            }
        
        }
        
        
       synchronized  public void StopSession()
         {
             if(IsActive)
             {
            // logger.info("Stop session"); 
             soundInOut.closeLines();
             if(rtpSession!=null)
             {
             rtpSession.stopRtpPacketReceiver();   
             rtpSession.shutDown();
             rtpSession=null;
             }
             IsActive=false;
             direction=-1;
             operatorid=-1;
             }
             
         }
            synchronized public void Send(byte[] packet)
         {
             if(rtpSession==null)return;
             // Set up a test RTP packet
            RtpPacket rtpPacket = new RtpPacket();
            rtpPacket.setV(2);
            rtpPacket.setP(0);
            rtpPacket.setX(0);
            rtpPacket.setCC(0);
            rtpPacket.setM(0);
            rtpPacket.setPT(0);
            rtpPacket.setTS((int)((System.nanoTime()-lastSentTime)/1000));
            rtpPacket.setSSRC(SSRC);
            rtpPacket.setSN(SN);
            rtpPacket.setPayload(packet, packet.length);
        try {
            rtpSession.sendRtpPacket(rtpPacket);
            SN++;
            if(SN>65535)SN=SN%65535;
        } catch (RtpException ex) {
            logger.error(ex);
        } catch (UnknownHostException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
             
         }
        

    @Override
    public void handleRtpPacketEvent(RtpPacketEvent rtpEvent) {
        	// Print the remote IP address and port
	//	RtpSession rtpSession_ = (RtpSession) rtpEvent.getSource();

		RtpPacket rtpPacket = rtpEvent.getRtpPacket();

		//textArea.append("Received RTP packet #" + rtpPacket.getSN() + "\n");
		//textArea.setCaretPosition(textArea.getDocument().getLength());

//		logger.debug("---------------\n[TestApplication] RTP Data:");
//		logger.debug("[TestApplication] Received V: " + rtpPacket.getV());
//		logger.debug("[TestApplication] Received P: " + rtpPacket.getP());
//		logger.debug("[TestApplication] Received X: " + rtpPacket.getX());
//		logger.debug("[TestApplication] Received CC: " + rtpPacket.getCC());
//		logger.debug("[TestApplication] Received M: " + rtpPacket.getM());
//		logger.debug("[TestApplication] Received PT: " + rtpPacket.getPT());
//		logger.debug("[TestApplication] Received SN: " + rtpPacket.getSN());
//		logger.debug("[TestApplication] Received TS: " + rtpPacket.getTS());
//		logger.debug("[TestApplication] Received SSRC: " + rtpPacket.getSSRC());
//		logger.debug("[TestApplication] Received Payload size: " + rtpPacket.getPayloadLength());
                
             //play sound   
            soundInOut.PlayPacket(rtpPacket);
           

    }

    @Override
    public void handleRtpStatusEvent(RtpStatusEvent rtpEvent) {
        		if (rtpEvent.getStatus() == RtpStatus.RECEIVER_STOPPED) {

			logger.debug("RECEIVED RTP STATUS EVENT: RECEIVER_STOPPED");

		} else if (rtpEvent.getStatus() == RtpStatus.RECEIVER_STARTED) {

			logger.debug("RECEIVED RTP STATUS EVENT: RECEIVER_STARTED");

		}
    }

    @Override
    public void handleRtpTimeoutEvent(RtpTimeoutEvent rtpEvent) {
      //  logger.error(rtpEvent);
    }

    @Override
    public void handleRtpErrorEvent(RtpErrorEvent rtpEvent) {
        logger.error(rtpEvent);
    }
    
}
