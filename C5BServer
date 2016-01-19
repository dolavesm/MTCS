import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class C5BServer implements Runnable{
  
  private static ArrayList<String> clients;
  private static ArrayList<String> heartbeat;
  ExecutorService executor;
  ServerSocket serverSocket;
  Socket clientSocket;
  BufferedReader inFromClient;
  BufferedWriter  outToClient;
  
  public C5BServer(){
    executor = Executors.newFixedThreadPool(60);
    clients = new ArrayList<String>();
    heartbeat = new ArrayList<String>();
    try{
      serverSocket = new ServerSocket(6789);
    }catch(Exception e){
      System.out.println("Server Socket Failed!");
    }
  }
  
  public C5BServer(Socket client){
    this.clientSocket = client;
    try{
      this.outToClient = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
    }catch(Exception e){
      System.out.println("Server Socket Failed!");
    }
  }
  
  public boolean registerClient(String name){
    String namePort = name.split(":")[1];
    name = name.split(":")[0];
    for(int i = 0; i < clients.size(); i++){
      String temp = clients.get(i).split(",")[0];
      if(temp.equals(name)){
        try{
          outToClient.write("Failed\n");
          outToClient.flush();
          return false;
        }catch (IOException e) {
          System.out.println("error: sending failed");
        }
      }
    }
    String c = name+","+clientSocket.getInetAddress().toString().substring(1)+","+namePort;
    clients.add(c);
    Long time = System.currentTimeMillis();
    String h = name + "," + Long.toString(time);
    heartbeat.add(h);
    try{
      outToClient.write("Success\n");
      outToClient.flush();
      System.out.printf("Registered: %s\n",name);
    }catch (IOException e) {
      System.out.println("error: sending success");
    } 
    return true;
  }
  
  public void heartbeatChecker(){
    this.executor.execute(new Runnable() {
      public void run() {
        while(true){
          //System.out.println("Checking Heartbeat");
          for(int i = 0; i < heartbeat.size();i++){
            Long temp = Long.valueOf(heartbeat.get(i).split(",")[1]).longValue();
            Long time = System.currentTimeMillis();
            if((time - temp) > 3000){
              String remname = heartbeat.get(i).split(",")[0];
              clients.remove(i);
              heartbeat.remove(i);
              System.out.printf("removed %s from group\n",remname);
            }
          }
          try {
            Thread.sleep(1000);
          } catch (Exception e) {
            System.out.println("heartbeat sleep failed");
          }
        }
      }
    });
  }
  
  public void analyze(String msg){
    //System.out.println("msg: "+msg);
    String firstWord = msg.split("\\s+")[0];
    if(firstWord.equals("register")){
      String name = msg.split("\\s+")[1];
      registerClient(name);
    }
    else if(msg.equals("getlist")){
      try{
        String group = Integer.toString(clients.size());
        for(int i = 0;i<clients.size();i++){
          group += ":"+clients.get(i);
        }
        outToClient.write(group+"\n");
        outToClient.flush();
      }catch(Exception e){
        System.out.println("oos failed");
      }
    }
    else if(firstWord.equals("heartbeat")){
      String name = msg.split("\\s+")[1];
      for(int i = 0; i < heartbeat.size() ; i++){
        String temp = heartbeat.get(i).split(",")[0]; 
        if(name.equals(temp)){
          Long time = System.currentTimeMillis();
          String input = name + "," + Long.toString(time);
          heartbeat.set(i,input);
          //System.out.printf("updated %s to %s\n",name, time.toString());
          break;
        }
      }
    }
  }
  
  
  public void run(){
    try{
      BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      while(true){
        String read = inFromClient.readLine();
        analyze(read);
      }
    }catch(Exception e){
      System.out.println("Connection Lost!");
    }
  }
  
  public void startServer() {
    heartbeatChecker();
    while(true){
      try{
        System.out.println("Waiting for client..");
        Socket cs2 = this.serverSocket.accept();
        C5BServer cb2 = new C5BServer(cs2);
        executor.execute(cb2);
      }catch(Exception e) { 
        System.out.println("Whoops!"); 
      }
    }
  }
  
  
  
  public static void main(String[] args){
    try {
      C5BServer cb = new C5BServer();
      cb.startServer();
    }catch(Exception e) { 
      System.out.println("Whoops! It didn't work!"); 
    }
  }
}
