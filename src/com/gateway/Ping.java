/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import javax.swing.event.EventListenerList;
import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
/**
 * @author Michael Netter
 */
 
public class Ping {
  public static int ARPTIMEOUT = 4000;
  public static int ICMPTIMEOUT = 4000;  
 
  public static void main(String[] argv){
    System.out.println(new Ping().ping(argv[0]));
  }
 
  /**
   * Send an ICMP packet (type 8) (ping) to the provided address
   * @param dst_ip Destination IP / hostname
   * @return true/false
   */
  public boolean ping(String dst_ip){
    return new PacketSender().ping(dst_ip);
  }
 
  /**
   * Send an ICMP packet (type 8) (ping) to the provided address
   * @param dst_ip Destination IP / hostname
   * @param timeout Timeout for receiving a response in milliseconds
   * @return
   */
  public boolean ping(String dst_ip, int timeout){
    ARPTIMEOUT = timeout;
    ICMPTIMEOUT = timeout;
    return new PacketSender().ping(dst_ip);
  }
 
  /**
   * Class for assembling and sending ARP and ICMP packets
   */
  private class PacketSender implements IARPPacketListener, IICMPPacketListener{
    private boolean arpReplyArrived = false;
    private boolean icmpReplyArrived = false;
    private InetAddress src_ip = null;
    private ARPPacket arpReplyPacket = null;
    private String dest_ip = null;
 
 
    public boolean ping(String dest_ip){
      this.dest_ip = dest_ip;
      NetworkInterface device = determineDevice();
      src_ip = determineSRCIP(device);
      ARPPacket arpPacket = assembleARPPacket(dest_ip,src_ip,device);
 
      JpcapCaptor captor = openDevice(device);
      JpcapSender sender = getSender(captor);
 
      // create packet receiver and register observer
      PacketReceiver receiver = new PacketReceiver(captor);
      receiver.addARPListener(this);
      receiver.addICMPListener(this);
      Thread packetReceiverThread = new Thread(receiver);
      packetReceiverThread.start();
      sender.sendPacket(arpPacket);
 
      // wait some milliseconds for a arp response
      try {
        Thread.sleep(ARPTIMEOUT);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if(arpReplyArrived && arpReplyPacket != null){
        // MAC detection successful continue with ICMP Packet construction
        ICMPPacket icmpPacket = assembleICMPPacket(device, arpReplyPacket.sender_hardaddr);
 
        // send ping
        sender.sendPacket(icmpPacket);
 
        // wait some milliseconds for a icmp response
        try {
          Thread.sleep(ICMPTIMEOUT);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
 
        packetReceiverThread.stop();  
        return (icmpReplyArrived ? true : false);
 
      }
      else{
        packetReceiverThread.stop();
        return false;
      }
    }
 
    /**
     * Function required by the  IARPPacketListener interface
     */
    public void arpPacketArrived(ARPPacketArrivedEvent event){
      ARPPacket arp = event.getARPPacket();
 
      if(Arrays.equals(arp.target_protoaddr,src_ip.getAddress()) && arp.operation == ARPPacket.ARP_REPLY){
        arpReplyPacket = arp;
        arpReplyArrived = true;
      }      
    }
 
    /**
     * Function required by the  IICMPPacketListener interface
     */
    public void icmpPacketArrived(ICMPPacketArrivedEvent event){      
      ICMPPacket icmpPacket = event.getICMPPacket();
      InetAddress dst_ip =null;
      try {
        dst_ip = InetAddress.getByName(dest_ip);
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
      if(icmpPacket.src_ip.equals(dst_ip) && icmpPacket.type == ICMPPacket.ICMP_ECHOREPLY){
        icmpReplyArrived = true;
      }
    }
 
    /**
     * Construct a valid PING packet (ICMP type 8)
     * @param device
     * @param dst_mac
     * @return
     */
    public ICMPPacket assembleICMPPacket(NetworkInterface device, byte[] dst_mac){
      ICMPPacket p=new ICMPPacket();
      p.type=ICMPPacket.ICMP_ECHO;
      p.seq=1000;
      p.id=999;
      p.orig_timestamp=123;
      p.trans_timestamp=456;
      p.recv_timestamp=789;
      try {
        p.setIPv4Parameter(0,false,false,false,0,false,false,false,0,1010101,100,IPPacket.IPPROTO_ICMP,
            src_ip,InetAddress.getByName(dest_ip));
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
      p.data="data".getBytes();
      EthernetPacket ether=new EthernetPacket();
      ether.frametype=EthernetPacket.ETHERTYPE_IP;
      ether.src_mac=device.mac_address;      
      ether.dst_mac=dst_mac;
      p.datalink=ether;      
      return p;
    }
 
    /**
     * Select the probable correct interface
     * @return NetworkInterface
     */
    public NetworkInterface determineDevice(){
      NetworkInterface dev = null;
      for(NetworkInterface device : JpcapCaptor.getDeviceList()){      
        for(NetworkInterfaceAddress address : device.addresses){
          if(address.address instanceof Inet4Address && !address.address.toString().equalsIgnoreCase("/0.0.0.0") ){
            dev = device;    
            break;
          }
        }        
      }
      return dev;
    }
 
    /**
     * get src_ip
     * @param device
     * @return
     */
    public InetAddress determineSRCIP(NetworkInterface device){
      // determine src_ip
      InetAddress srcip=null;
      for(NetworkInterfaceAddress addr:device.addresses){
        if(addr.address instanceof Inet4Address){
          srcip=addr.address;
          break;
        }
      }
      return srcip;
    }
 
    /**
     * Construct a valid ARP Request
     * @param dst_ip - destination ip
     * @param device - NetworkInterface
     * @return ARPPacket
     */
    public ARPPacket assembleARPPacket(String dst_ip, InetAddress src_ip, NetworkInterface device ){
 
      byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
      ARPPacket arp=new ARPPacket();
      arp.hardtype=ARPPacket.HARDTYPE_ETHER;
      arp.prototype=ARPPacket.PROTOTYPE_IP;
      arp.operation=ARPPacket.ARP_REQUEST;
      arp.hlen=6;
      arp.plen=4;
      arp.sender_hardaddr=device.mac_address;
      arp.sender_protoaddr=src_ip.getAddress();
      arp.target_hardaddr=broadcast;
      try {
        arp.target_protoaddr=Inet4Address.getByName(dst_ip).getAddress();
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }    
      EthernetPacket ether=new EthernetPacket();
      ether.frametype=EthernetPacket.ETHERTYPE_ARP;
      ether.src_mac=device.mac_address;
      ether.dst_mac=broadcast;
      arp.datalink=ether;    
      return arp;
    }
 
    /**
     * Open JpcapCaptor
     * @param device
     * @return JpcapCaptor
     */
    public JpcapCaptor openDevice(NetworkInterface device){
      JpcapCaptor captor = null;
      try {
        captor = JpcapCaptor.openDevice(device,2000,false,3000);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return captor;
    }
 
    /**
     * Get JpcapSender
     * @param captor
     * @return JpcapSender
     */
    public JpcapSender getSender(JpcapCaptor captor){
      return captor.getJpcapSenderInstance();
    }
  }
 
 
  /**
   * Packet receiver implemented as a seperate thread
   * Register observers to get notified of received packets
   */
  private class PacketReceiver implements Runnable{
    private JpcapCaptor captor;
    private EventListenerList listeners = new EventListenerList();  
 
    public PacketReceiver(JpcapCaptor captor){
      this.captor = captor;
    }  
    public void addARPListener( IARPPacketListener listener ) {
      listeners.add( IARPPacketListener.class, listener );
    }
 
    public void removeARPListener( IARPPacketListener listener ){
      listeners.remove( IARPPacketListener.class, listener );
    }
    protected synchronized void notifyARPListeners( ARPPacketArrivedEvent event )  {
      for ( IARPPacketListener l : listeners.getListeners( IARPPacketListener.class )){
        l.arpPacketArrived(event);
      }
    } 
 
    public void addICMPListener( IICMPPacketListener listener ) {
      listeners.add( IICMPPacketListener.class, listener );
    }
 
    public void removeICMPListener( IICMPPacketListener listener ){
      listeners.remove( IICMPPacketListener.class, listener );
    }
    protected synchronized void notifyICMPListeners( ICMPPacketArrivedEvent event )  {
      for ( IICMPPacketListener l : listeners.getListeners( IICMPPacketListener.class )){
        l.icmpPacketArrived(event);
      }
    } 
 
    public void run(){
      while(true){
        Packet packet = captor.getPacket();
        if(packet instanceof ARPPacket){
          ARPPacket arpPacket = (ARPPacket)packet;
          notifyARPListeners(new ARPPacketArrivedEvent(this,arpPacket));
        }
        if(packet instanceof ICMPPacket){
          ICMPPacket icmpPacket = (ICMPPacket)packet;
          notifyICMPListeners(new ICMPPacketArrivedEvent(this,icmpPacket));
        }
      }
    }
  }
 
  private interface IARPPacketListener extends EventListener{
    void arpPacketArrived(ARPPacketArrivedEvent event);
  }
 
  private interface IICMPPacketListener extends EventListener{
    void icmpPacketArrived(ICMPPacketArrivedEvent event);
  }
 
  private class ARPPacketArrivedEvent extends EventObject{
    private ARPPacket arpPacket;
    public ARPPacketArrivedEvent(Object source, ARPPacket arpPacket){
      super(source);
      this.arpPacket = arpPacket;
    }
    public ARPPacket getARPPacket(){
      return arpPacket;
    }
  }
 
  private class ICMPPacketArrivedEvent extends EventObject{
    private ICMPPacket icmpPacket;
    public ICMPPacketArrivedEvent(Object source, ICMPPacket icmpPacket){
      super(source);
      this.icmpPacket = icmpPacket;
    }
    public ICMPPacket getICMPPacket(){
      return icmpPacket;
    }
  }
}