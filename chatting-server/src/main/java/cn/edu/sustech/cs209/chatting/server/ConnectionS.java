package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Convert;
import cn.edu.sustech.cs209.chatting.common.FileMessage;
import cn.edu.sustech.cs209.chatting.common.TextMessage;
import cn.edu.sustech.cs209.chatting.common.User;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionS extends Thread {

  private static Map<String, ConnectionS> ConnectionSMap = new HashMap<>();
  private final Socket Connection;
  private final BufferedReader Receive;
  private final BufferedWriter Send;
  private final String UserName;

  ConnectionS(Socket S, String UN) throws IOException {
    this.Connection = S;
    this.Receive = new BufferedReader(new InputStreamReader(this.Connection.getInputStream()));
    this.Send = new BufferedWriter(new OutputStreamWriter(this.Connection.getOutputStream()));
    this.UserName = UN;
    ConnectionSMap.put(UN, this);
    this.Send.write(UserS.GetAllUserName() + "\n");
    AU(this.UserName);
  }

  //Send To All User
  public static void SendToAll(String S) {
    ConnectionSMap.values().stream().forEach(connection -> {
      BufferedWriter BW = connection.Send;
      try {
        BW.write(S);
        BW.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  //AddUser
  public static void AU(String UN) {
    String S = "";
    S = S + "%AU%\n";
    S = S + UN + "\n";
    SendToAll(S);
  }

  //RemoveUser
  public static void RU(String UN) {
    String S = "";
    S = S + "%RU%\n";
    S = S + UN + "\n";
    SendToAll(S);
  }

  //AddTextMessage
  public static void ATM(TextMessage M) {
    String S = "";
    S = S + "%ATM%\n";
    S = S + M.toJson().toJSONString() + "\n";
    SendToAll(S);
  }

  @Override
  public void run() {
    String DataLine = "";
    while (true) {
      try {
        DataLine = Receive.readLine();
        UUID CID;
        String UN2;
        ConnectionS U2 = null;
        switch (DataLine) {
          case "#Close#":
            this.Send.flush();
            ConnectionSMap.remove(this.UserName);
            UserS.RemoveUser(this.UserName);
            RU(this.UserName);
            this.Send.close();
            this.Receive.close();
            this.Connection.close();
            this.Send.write("%Quit%\n");
            this.interrupt();
            break;
          //Send Text Message
          case "#STM#":
            System.out.println("#STM#");
            DataLine = Receive.readLine();
            System.out.println(DataLine);
            JSONObject J = JSONObject.parseObject(DataLine);
            TextMessage TM = new TextMessage(J);
            Server.Chat.AddMessage(TM);
            ATM(TM);
            break;
          //Create Private Chat Room
          case "#CPCR#":
            DataLine = Receive.readLine();
            UN2 = DataLine;
            CID = Server.Chat.AddPrivateChat(this.UserName, UN2);
            Send.write("%R-CPCR%\n");
            Send.write(CID + "\n");
            Send.flush();
            U2 = ConnectionSMap.get(UN2);
            U2.Send.write("%APC%\n");
            U2.Send.write(this.UserName + "\n");
            U2.Send.write(CID + "\n");
            U2.Send.write(Server.Chat.GetChatRoom(CID).getChatRoomName() + "\n");
            U2.Send.flush();
            break;
          //Create Group Chat Room
          case "#CGCR#":
            DataLine = Receive.readLine();
            List<String> UL = Convert.stringToList(DataLine);
            UL.add(this.UserName);
            System.out.println("UL:" + UL);
            CID = Server.Chat.AddGroupChat(UL);
            Send.write("%R-CGCR%\n");
            Send.write(CID + "\n");
            Send.write(Server.Chat.GetChatRoom(CID).getChatRoomName() + "\n");
            Send.flush();
            System.out.println("Send Fnishied");
            UL.remove(this.UserName);
            for (String i : UL) {
              U2 = ConnectionSMap.get(i);
              U2.Send.write("%AGC%\n");
              U2.Send.write(CID + "\n");
              U2.Send.write(Server.Chat.GetChatRoom(CID).getChatRoomName() + "\n");
              U2.Send.flush();
            }
            break;
          //Join Group Chat Room
          case "#JGCR#":
            DataLine = Receive.readLine();
            CID = UUID.fromString(DataLine);
            Server.Chat.JoinGroupChat(CID, this.UserName);
            break;
          //Quit Group Chat Room
          case "#QGCR#":
            DataLine = Receive.readLine();
            CID = UUID.fromString(DataLine);
            Server.Chat.QuitGroupChat(CID, this.UserName);
            break;
          case "#GCRN#":
            DataLine = Receive.readLine();
            CID = UUID.fromString(DataLine);
            String GN = Server.Chat.GetChatRoom(CID).getChatRoomName();
            Send.write(CID + "\n");
            Send.flush();
            break;
          case "#GCRM#":
            DataLine = Receive.readLine();
            CID = UUID.fromString(DataLine);
            List<TextMessage> LM = Server.Chat.GetChatRoom(CID).getMessageList();
            Send.write("%R-GCRM%\n");
            Send.write(LM.size() + "\n");
            for (TextMessage i : LM) {
              Send.write(i.toJson().toJSONString() + "\n");
            }
            Send.flush();
            break;
          case "#SDM#":
            DataLine = Receive.readLine();
            System.out.println("SDM:" + DataLine);
            FileMessage DF = new FileMessage(JSONObject.parseObject(DataLine));
            CID = DF.getChatRoomId();
            List<String> UN = Server.Chat.GetChatRoom(CID).getUserList();
            UN.remove(this.UserName);
            for (String i : UN) {
              U2 = ConnectionSMap.get(i);
              U2.Send.write("%RDM%\n");
              U2.Send.write(CID + "\n");
              U2.Send.write(DataLine + "\n");
              U2.Send.flush();
            }
            break;
        }
      } catch (Exception e) {
        interrupt();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    int ServerPort = 1777;
    ServerSocket Server = new ServerSocket(ServerPort);
    Socket S = Server.accept();
    BufferedWriter Send = new BufferedWriter(new OutputStreamWriter(S.getOutputStream()));
    BufferedReader Receive = new BufferedReader(new InputStreamReader(S.getInputStream()));
    String UN = Receive.readLine();
    if (!UserS.AddUser(UN, "")) {
      Send.write("Fail\n");
      Send.flush();
    }
    Send.write("Success\n");
    Send.flush();
    ConnectionS SCT = new ConnectionS(S, UN);
    SCT.run();
  }
}
