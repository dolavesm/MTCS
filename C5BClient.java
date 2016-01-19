import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

class C5BClient extends Frame implements Runnable {
  
  static String name;
  static String serverMsg;
  Socket heartbeatSocket;
  Socket clientSocket;
  ServerSocket serverSocket;
  BufferedWriter bw;
  static BufferedReader inFromUser;
  BufferedWriter outToServer;
  BufferedReader inFromServer;
  BufferedWriter outToClient;
  BufferedReader inFromClient;
  ExecutorService executor;
  JFrame frame;
  JFrame game;
  JLabel label;
  JLabel group;
  static ArrayList<String> clients;
  
  public C5BClient(){
    inFromUser = new BufferedReader( new InputStreamReader(System.in));
    clients = new ArrayList<String>();
    executor = Executors.newFixedThreadPool(60);
    label = new JLabel("What is your name?");
    group = new JLabel();
    try{
      heartbeatSocket = new Socket("localhost", 6789);
      outToServer = new BufferedWriter(new OutputStreamWriter(heartbeatSocket.getOutputStream()));
      inFromServer = new BufferedReader(new InputStreamReader(heartbeatSocket.getInputStream()));
      serverSocket = new ServerSocket(0);
    }catch(Exception e){
      System.out.println("Build failed");
    }
  }
  
  public C5BClient(Socket client, JLabel label, JLabel group){
    this.label = label;
    this.group = group;
    this.clientSocket = client;
    try{
      this.outToClient = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
    }catch(Exception e){
      System.out.println("Server Socket Failed!");
    }
  }
  
  public void sendHeartbeat(){
    this.executor.execute(new Runnable() {
      public void run(){
        try{
          while(true){
            outToServer.write("heartbeat " + name + '\n');
            outToServer.flush();
      
      outToServer.write("getlist" + '\n');
      outToServer.flush();
      serverMsg = inFromServer.readLine();
      int size = Integer.parseInt(serverMsg.split(":")[0]);
      ArrayList<String> freshclients = new ArrayList<String>();
      for(int i = 0;i<size;i++){
        freshclients.add(serverMsg.split(":")[i+1]);
      }
      clients = freshclients;
      
      String group_names = "";
      for(int i =0; i <clients.size();i++){
        //System.out.println(clients.get(i).split(",")[0]);
        group_names += "<html>" +  clients.get(i).split(",")[0] + "<br><html>";
      }
      group.setText(group_names);
      
            try {
              Thread.sleep(1000);
            } catch (Exception e) {
              System.out.println("sleep failed");
            }
          }
        }catch(Exception e){
          System.out.println("heartbeat send failed");
        }
      }
    });
  }
  
  /*
   public void register(String regname){
   try{
   while(true){
   outToServer.write("register " + regname +":"+ serverSocket.getLocalPort() + '\n');
   outToServer.flush();
   serverMsg = inFromServer.readLine();
   if(serverMsg.equals("Failed")){
   System.out.println("Name taken. Choose another name");
   regname = inFromUser.readLine();
   }else{
   //System.out.println("Successfully Registered "+" : "+Integer.toString(serverSocket.getLocalPort()));
   break; 
   }
   }
   }catch(Exception e){
   System.out.println("Buffered reader/writer failed"); 
   }
   }
   */
  
  void sendToAll(String msg){
    try{
      for(int i =0; i<clients.size();i++){
        if(name.equals(clients.get(i).split(",")[0])){
          continue;
        }
        InetAddress address = InetAddress.getByName(clients.get(i).split(",")[1]);
        int port = Integer.parseInt(clients.get(i).split(",")[2]);
        Socket clientsock = new Socket(address, port);
        BufferedWriter writeToClient = new BufferedWriter(new OutputStreamWriter(clientsock.getOutputStream()));
        writeToClient.write(name + ": "+msg+"\n");
        writeToClient.flush();
        clientsock.close();
      }
    }catch(Exception e){
      System.out.println("failed sending to all clients");
    }
  }
  
  /*public void chat(){
    this.executor.execute(new Runnable() {
      public void run(){
        try{
          while(true){
            String msg = inFromUser.readLine();
            outToServer.write("getlist" + '\n');
            outToServer.flush();
            serverMsg = inFromServer.readLine();
            int size = Integer.parseInt(serverMsg.split(":")[0]);
            ArrayList<String> freshclients = new ArrayList<String>();
            for(int i = 0;i<size;i++){
              freshclients.add(serverMsg.split(":")[i+1]);
            }
            clients = freshclients;
            System.out.println(msg);
            if(msg.equals("!help")){
                System.out.println("helpsdfsdafsdf");
            }else if(msg.equals("!group")){
              for(int i =0; i<clients.size();i++){
                System.out.println("clients: "+clients.get(i).split(",")[0]);
              }
            }else{
              sendToAll(msg);
            }
          }
        }catch(Exception e){
          System.out.println("listener failed");
        }
      }
    });
  }*/
  
  public void run(){
    try{
      BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
      String read;
      while(((read = inFromClient.readLine()) != null) && !(read.equals(""))){
        //label = new JLabel("hello");
        //label.setText("read");
        
        String chat = label.getText();
        String con = "<html>"+read + "<br>" + chat + "<html>";
        //int count = (con.length() - con.replace("<br>", "").length()) / 4;
        int index = con.indexOf("<br>");
        int nl = 0;
        int prev = 0;
        while (index >= 0 && nl < 28) {
          //System.out.println(index);
          prev = index;
          index = con.indexOf("<br>", index + 1);
          nl++;
        }
        if(nl == 27){
          con = con.substring(0, prev);
        }
        //System.out.println("con: "+Integer.toString(count) + "\n"+ con);
        label.setText(con);
        //System.out.println(read);
      }
    }catch(Exception e){
      System.out.println("Connection Lost!");
      e.printStackTrace();
    }
  }
  
  
  public void listenClients(){
    this.executor.execute(new Runnable() {
      public void run(){
        try{
          while(true){
            Socket cs2 = serverSocket.accept();
            C5BClient cb2 = new C5BClient(cs2, label, group);
            executor.execute(cb2);  
          }
        }catch(Exception e){
          System.out.println("listener failed");
        }
      }
    });
  }
  
  public void sendMsg(String msg){
    try{      
      outToServer.write("getlist" + '\n');
      outToServer.flush();
      serverMsg = inFromServer.readLine();
      int size = Integer.parseInt(serverMsg.split(":")[0]);
      ArrayList<String> freshclients = new ArrayList<String>();
      for(int i = 0;i<size;i++){
        freshclients.add(serverMsg.split(":")[i+1]);
      }
      clients = freshclients;
      if(msg.equals("group")){
        for(int i =0; i<clients.size();i++){
          System.out.println("clients: "+clients.get(i).split(",")[0]);
        }
      }else{
        sendToAll(msg);
      }
    }catch(Exception e){
      System.out.println("listener failed");
    }
  }
  
  public void gameGUI(){
    
    game = new JFrame("C5B Game GUI");
    game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    game.setSize(1000,700);
    game.setLocationRelativeTo(null);
    game.setLayout(new BorderLayout());
    
    
    //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    //game.setLocation(dim.width/2-game.getSize().width/2, dim.height/2-game.getSize().height/2);
    Border boutline = BorderFactory.createLineBorder(Color.blue);
    Border goutline = BorderFactory.createLineBorder(Color.green);
    Border routline = BorderFactory.createLineBorder(Color.red); 
    
    JPanel chatpanel = new JPanel(new BorderLayout());
    chatpanel.setBorder(boutline);
    game.add(chatpanel, BorderLayout.EAST);
    
    JPanel textpanel = new JPanel(new BorderLayout());
    textpanel.setBorder(routline);
    textpanel.setPreferredSize(new Dimension(175,20));
    chatpanel.add(textpanel, BorderLayout.WEST);
    
    JPanel grouppanel = new JPanel(new BorderLayout());
    grouppanel.setBorder(goutline);
    grouppanel.setPreferredSize(new Dimension(100,20));
    chatpanel.add(grouppanel, BorderLayout.EAST);
    
    
    final JTextField chatbox = new JTextField();
    chatbox.setBorder(routline);
    textpanel.add(chatbox,BorderLayout.SOUTH);
    //chatbox.setPreferredSize(new Dimension(100,20));
    //chatbox.setColumns(15);
    textpanel.add(label,BorderLayout.CENTER);
    
    grouppanel.add(group);
    
    Action chataction = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        //System.out.println(chatbox.getText());
        String msg = chatbox.getText();
        if(!msg.equals("")){
          
          
          String chat = label.getText();
          String con = "<html>"+name+": "+msg + "<br>" + chat + "<html>";
          //int count = (con.length() - con.replace("<br>", "").length()) / 4;
          int index = con.indexOf("<br>");
          int nl = 0;
          int prev = 0;
          while (index >= 0 && nl < 28) {
            //System.out.println(index);
            prev = index;
            index = con.indexOf("<br>", index + 1);
            nl++;
          }
          if(nl == 27){
            con = con.substring(0, prev);
          }
          //System.out.println("con: "+Integer.toString(count) + "\n"+ con);
          label.setText(con);
          sendMsg(chatbox.getText());
          chatbox.setText("");
        }
      }
    };
    chatbox.addActionListener( chataction );
    
    //game.pack();
    game.setVisible(true);
  }
  
  
  public void startClient(){
    try{      
      sendHeartbeat();
      listenClients();
      //chat();
      gameGUI();
      //startGUI();
      //gameStart();
      
    }catch(Exception e){
      System.out.println("Connection to Server Failed!");
    }
  }  
  
  public void startGUI(){
    
    frame = new JFrame("C5B Start GUI");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
    
    
    
    JPanel pane = new JPanel();
    frame.add(pane, BorderLayout.NORTH);
    pane.add(label);
    
    JPanel noti = new JPanel();
    frame.add(noti, BorderLayout.SOUTH);
    
    final JLabel notilabel = new JLabel();
    //frame.add(label,BorderLayout.NORTH);
    noti.add(notilabel);
    
    final JTextField namebox;
    namebox = new JTextField("");
    pane.add(namebox);
    namebox.setColumns(25);
    
    
    /*
     * frame = new JFrame("C5B Start GUI");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new FlowLayout());
    
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
    
    
    
    JPanel pane = new JPanel();
    frame.add(pane);
    
    
    //frame.add(label,BorderLayout.NORTH);
    pane.add(label);
    
    final JTextField namebox;
    namebox = new JTextField("");
    pane.add(namebox);
    namebox.setColumns(25);
     * */
    
    Button enterBtn;
    enterBtn = new Button("Enter");
    pane.add(enterBtn, BorderLayout.SOUTH);
    enterBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        name = namebox.getText();
        if(!name.equals("")){
          register2(name);
        }
      }
    });
    
    Action action = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        name = namebox.getText();///put check for spaces in name
        if(!name.equals("")){
          register2(name);
        }
      }
    };
    namebox.addActionListener( action );
    
    
    frame.setSize(500,100);
    //frame.pack();
    frame.setVisible(true);
    //cb.register(namee);
  }
  
  public void register2(String regname){
    try{
      outToServer.write("register " + regname +":"+ serverSocket.getLocalPort() + '\n');
      outToServer.flush();
      serverMsg = inFromServer.readLine();
      if(serverMsg.equals("Failed")){
        System.out.println("Name taken. Choose another name");
        label.setText("Name taken");
      }else{
        System.out.println("Successfully Registered: "+name+":"+Integer.toString(serverSocket.getLocalPort()));
        //label.setText("<html>Welcome!<br>Type \"!help\" for more options!<html>");
        label.setText("");
        frame.dispose();
        startClient();
      }
    }catch(Exception e){
      System.out.println("Buffered reader/writer failed"); 
    }
  }
  
  public static void main(String argv[]) throws Exception  {
    C5BClient cb = new C5BClient();
    cb.startGUI();
    //cb.gameGUI();
    //System.out.println("What is your name?");
    //name = inFromUser.readLine();
    //cb.register(name);
    //cb.startClient();
  }
} 
