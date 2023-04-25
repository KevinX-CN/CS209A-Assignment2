package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.ChatRoomGroup;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  private static final int ServerPort = 1777;
  protected static ChatRoomGroup Chat = new ChatRoomGroup();

  public static void main(String[] args) throws IOException {
    System.out.println("Server Start");
    UserS.Load();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        super.run();
        try {
          UserS.Save();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        ConnectionS.SendToAll("%Close%\n");
        System.out.println("Server End");
      }
    });
    ServerSocket Server = new ServerSocket(ServerPort);
    while (true) {
      Socket S = Server.accept();
      BufferedWriter Send = new BufferedWriter(new OutputStreamWriter(S.getOutputStream()));
      BufferedReader Receive = new BufferedReader(new InputStreamReader(S.getInputStream()));
      String UN = Receive.readLine();
      String PWD = Receive.readLine();
      if(UserS.HaveUser(UN))
      {
        if(!UserS.Identify(UN,PWD))
        {
          Send.write("Fail\n");
          Send.flush();
          continue;
        }
      }
      else if(!UserS.AddUser(UN, PWD)) {
        Send.write("Fail\n");
        Send.flush();
        continue;
      }
      Send.write("Success\n");
      Send.flush();
      ConnectionS SCT = new ConnectionS(S, UN);
      SCT.start();
      Send.flush();
    }

  }
}