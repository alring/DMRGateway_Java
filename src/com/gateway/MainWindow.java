/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gateway;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import org.apache.log4j.Logger;

import org.jdesktop.swingx.JXTaskPaneContainer;

public class MainWindow  extends JFrame {
    static Logger logger = Logger.getLogger(MainWindow.class);
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
 //   private javax.swing.JToolBar TopToolBar;
    private javax.swing.JToolBar DownToolBar;
    private JLabel statusLabel;
    private JLabel statusLabelIco;
    
    public JPanel centerPanel;
    JScrollPane scroll;
    JXTaskPaneContainer tpc ;
    //
   Gateway gateway;
    
    //public MainWindow(Connection conn,int id) 
    public MainWindow() 
    {   
        try {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
        }
    }
} catch (Exception e) {
    logger.error(e);
}
        Image img=Toolkit.getDefaultToolkit().getImage(getClass().getResource("/ico/gateway.png"));
        this.setIconImage(img);
        this.setTitle("Шлюз DMR");


        //меню
        statusLabel= new JLabel("Подключение к серверу:  ");
        statusLabelIco= new JLabel(new ImageIcon(getClass().getResource("/ico/red.png")));
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2= new JMenuItem("Соединение");
        jMenuItem3= new JMenuItem("О программе");
        jMenu2 = new javax.swing.JMenu();
        jMenu1.setText("Файл");
        jMenu3= new JMenu("Помощь");
        jMenuItem1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                System.exit(0);
            }
            
        });  
        
        jMenuItem2.addMouseListener(new MouseAdapter() {


            @Override
            public void mouseReleased(MouseEvent e) {
                 new SettingsWindow(GetMainWindow());
            }
            
        
        
        
        });
                jMenuItem3.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                new AboutWindow();
            }
            
        });  
        
        jMenuItem1.setText("Выход");
        jMenu1.add(jMenuItem1);
        jMenuBar1.add(jMenu1);
        jMenu2.setText("Настройки");
        jMenu2.add(jMenuItem2);
        jMenuBar1.add(jMenu2);
        centerPanel= new JPanel();
        jMenuBar1.add(jMenu3);
        jMenu3.add(jMenuItem3);
        scroll= new JScrollPane(centerPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        setJMenuBar(jMenuBar1);

//        TopToolBar= new JToolBar();
//        TopToolBar.add(new JLabel("Tool bar for something"));
//        TopToolBar.setFloatable(false);
//        add(TopToolBar,BorderLayout.NORTH);
        
        DownToolBar= new JToolBar();
        DownToolBar.add(statusLabel);
        DownToolBar.add(statusLabelIco);
        DownToolBar.setFloatable(false);
        add(DownToolBar,BorderLayout.SOUTH);
        //
        add(scroll,BorderLayout.CENTER);

        
        ///////
         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));



setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setSize(1000, 700);
setVisible( true );
////////////////
   gateway= new Gateway(this);
   gateway.Start();
    }
    
    
    //обработка событий
        private void jMenu1MouseClicked(java.awt.event.MouseEvent evt) {

        System.exit(0);
    }
       public void SetOnline(boolean online)
       {
       if(online) statusLabelIco.setIcon(new ImageIcon(getClass().getResource("/ico/green.png")));
       else statusLabelIco.setIcon(new ImageIcon(getClass().getResource("/ico/red.png")));
       }
       
    public MainWindow GetMainWindow()
    {
        return this;
    }

    
}