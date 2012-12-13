/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;


/**
 *
 * @author Alexandr
 */
public class StationPanel  extends JPanel
{
    JPanel ToolBar;
    CollapseablePanel CollapsPanel;
    boolean collapsed=false;
    JButton openButton;
    JLabel Ip;
    JLabel status;
    JLabel status_ico;
    JLabel state;
    JLabel state_txt;
    RadioStationPC radioStationPC;

    public StationPanel(RadioStationPC radioStationPC) 
    {
        this.radioStationPC=radioStationPC;
        ToolBar= new JPanel();
        ToolBar.setBackground(Color.WHITE);
        ToolBar.setLayout(new BoxLayout(ToolBar, BoxLayout.X_AXIS));
        ToolBar.setMaximumSize(new Dimension(10000, 30));
        CollapsPanel = new CollapseablePanel(radioStationPC);
        openButton=new JButton("-");
        openButton.setAlignmentX(Component.LEFT_ALIGNMENT);
       // openButton.setAlignmentY(Component.TOP_ALIGNMENT);
        openButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if(collapsed)
                {collapsed=false;
                CollapsPanel.setVisible(true);
                openButton.setText("-");
                }
                else 
                {
                collapsed=true;
                CollapsPanel.setVisible(false);
                openButton.setText("+");
                }
            }
        });
         ToolBar.add(openButton);
         ToolBar.add(Box.createRigidArea(new Dimension(10, 10)));
         ImageIcon img=new ImageIcon(getClass().getResource("/ico/radio.png"));
         Ip= new JLabel(radioStationPC.GetIP());
         Ip.setFont(new Font("Arial", Font.BOLD, 12));
         Ip.setIcon(img);
         Ip.setAlignmentX(Component.LEFT_ALIGNMENT);
         ImageIcon img2=new ImageIcon(getClass().getResource("/ico/red.png"));
         status_ico= new JLabel(img2);
         status_ico.setAlignmentX(Component.LEFT_ALIGNMENT);
        // ip.setAlignmentY(Component.CENTER_ALIGNMENT);
         ToolBar.add(Ip);
         
        
        

        ToolBar.add(Box.createRigidArea(new Dimension(10, 10)));
        
        ToolBar.add(new JSeparator(SwingConstants.VERTICAL));
     
        
        status= new JLabel("статус подключения:");
        status.setFont(new Font("Arial", Font.BOLD, 12));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);

        
       // status.setAlignmentY(Component.CENTER_ALIGNMENT); 
        ToolBar.add(status);
        ToolBar.add(Box.createRigidArea(new Dimension(10, 10)));
        ToolBar.add(status_ico);
        
        //
        ToolBar.add(Box.createRigidArea(new Dimension(10, 10)));
        
        
        ToolBar.add(new JSeparator(SwingConstants.VERTICAL));
        
        state= new JLabel("состояние: ");
        state.setFont(new Font("Arial", Font.BOLD, 12));
        state.setAlignmentX(Component.LEFT_ALIGNMENT);
        ToolBar.add(state);
        
        state_txt= new JLabel("?");
        state_txt.setForeground(Color.BLACK);
        state_txt.setFont(new Font("Arial", Font.BOLD, 12));
        ToolBar.add(state_txt);
        
        ToolBar.add(Box.createRigidArea(new Dimension(10, 10)));
        ToolBar.add(new JSeparator(SwingConstants.VERTICAL));
        
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         ToolBar.add(Box.createHorizontalGlue());
         CollapsPanel.add(Box.createHorizontalGlue());
         add(ToolBar);
         add(CollapsPanel);
         setAlignmentY(Component.LEFT_ALIGNMENT);
         setAlignmentY(Component.TOP_ALIGNMENT);
         
         CollapsPanel.jList1.clearSelection();
         CollapsPanel.jList2.clearSelection();
         CollapsPanel.jList1.removeAll();
         CollapsPanel.jList2.removeAll();
         
         CollapsPanel.jList1.setListData(radioStationPC.GetSoundmanager().GetMicrophoneList());
         CollapsPanel.jList2.setListData(radioStationPC.GetSoundmanager().GetSreakerList());
         CollapsPanel.jList1.setSelectedIndex(0);
         CollapsPanel.jList2.setSelectedIndex(0);
         CollapsPanel.jList1.setBackground(Color.WHITE);
         CollapsPanel.jList2.setBackground(Color.WHITE);
    }
    
      synchronized  public void SetState(String s)
    {
            state_txt.setText(s);
            if(s.equals("?"))state_txt.setForeground(Color.GREEN);
            else state_txt.setForeground(Color.GREEN);
    }
    
   synchronized public void Refresh()
    {
       
    if(radioStationPC.IsOnline())
    {
         ImageIcon img=new ImageIcon(getClass().getResource("/ico/green.png"));
         status_ico.setIcon(img);
         status_ico.repaint();
         CollapsPanel.jTextField1.setText(String.valueOf(radioStationPC.GetId()));
         CollapsPanel.jTextField2.setText(radioStationPC.GetRealIP());
         
         
    }
    else
    {
    
         ImageIcon img=new ImageIcon(getClass().getResource("/ico/red.png"));
         status_ico.setIcon(img);
         status_ico.repaint();
    }
    CollapsPanel.jList1.removeAll();
    CollapsPanel.jList2.removeAll();
         CollapsPanel.jList1.setListData(radioStationPC.GetSoundmanager().GetMicrophoneList());
         CollapsPanel.jList2.setListData(radioStationPC.GetSoundmanager().GetSreakerList());
         
         if(CollapsPanel.jList1.getLastVisibleIndex()>=radioStationPC.selected_mic)
         CollapsPanel.jList1.setSelectedIndex(radioStationPC.selected_mic);
         else
         {
         radioStationPC.selected_mic=0;
         CollapsPanel.jList1.setSelectedIndex(0);
         }
        if(CollapsPanel.jList2.getLastVisibleIndex()>=radioStationPC.selected_speak)
         CollapsPanel.jList2.setSelectedIndex(radioStationPC.selected_speak);
        else
        {
             radioStationPC.selected_speak=0;
             CollapsPanel.jList2.setSelectedIndex(0);
        }
         
    }
    
    
    
    
    
}
