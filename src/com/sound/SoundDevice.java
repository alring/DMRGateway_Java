/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sound;

/**
 *
 * @author Alexandr
 */
public class SoundDevice
{
    public int Type;    //0-mic 1- speaker
    public String ip;
    public String Name;

    public SoundDevice(String ip, String Name,int Type ) 
    {
        this.Type = Type;
        this.ip = ip;
        this.Name = Name;
    }
    
    
    
    
}
