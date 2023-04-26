package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Convert;
import cn.edu.sustech.cs209.chatting.common.FileMessage;
import cn.edu.sustech.cs209.chatting.common.TextMessage;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionS extends Thread {

  private static final Map<String, ConnectionS> ConnectionSMap = new HashMap<>();
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
    ConnectionSMap.values().forEach(connection -> {
      BufferedWriter bw = connection.Send;
      try {
        bw.write(S);
        bw.flush();
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
    String DataLine;
    while (true) {
      try {
        DataLine = Receive.readLine();
        UUID cid;
        String un2;
        ConnectionS U2;
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
            TextMessage tm = new TextMessage(J);
            Server.Chat.addMessage(tm);
            ATM(tm);
            break;
          //Create Private Chat Room
          case "#CPCR#":
            DataLine = Receive.readLine();
            un2 = DataLine;
            cid = Server.Chat.addPrivateChat(this.UserName, un2);
            Send.write("%R-CPCR%\n");
            Send.write(cid + "\n");
            Send.flush();
            U2 = ConnectionSMap.get(un2);
            U2.Send.write("%APC%\n");
            U2.Send.write(this.UserName + "\n");
            U2.Send.write(cid + "\n");
            U2.Send.write(Server.Chat.getChatRoom(cid).getChatRoomName() + "\n");
            U2.Send.flush();
            break;
          //Create Group Chat Room
          case "#CGCR#":
            DataLine = Receive.readLine();
            List<String> ul = Convert.stringToList(DataLine);
            ul.add(this.UserName);
            System.out.println("UL:" + ul);
            cid = Server.Chat.addGroupChat(ul);
            Send.write("%R-CGCR%\n");
            Send.write(cid + "\n");
            Send.write(Server.Chat.getChatRoom(cid).getChatRoomName() + "\n");
            Send.flush();
            System.out.println("Send Fnishied");
            ul.remove(this.UserName);
            for (String i : ul) {
              U2 = ConnectionSMap.get(i);
              U2.Send.write("%AGC%\n");
              U2.Send.write(cid + "\n");
              U2.Send.write(Server.Chat.getChatRoom(cid).getChatRoomName() + "\n");
              U2.Send.flush();
            }
            break;
          //Join Group Chat Room
          case "#JGCR#":
            DataLine = Receive.readLine();
            cid = UUID.fromString(DataLine);
            Server.Chat.joinGroupChat(cid, this.UserName);
            break;
          //Quit Group Chat Room
          case "#QGCR#":
            DataLine = Receive.readLine();
            cid = UUID.fromString(DataLine);
            Server.Chat.quitGroupChat(cid, this.UserName);
            break;
          case "#GCRN#":
            DataLine = Receive.readLine();
            cid = UUID.fromString(DataLine);
            Send.write(cid + "\n");
            Send.flush();
            break;
          case "#GCRM#":
            DataLine = Receive.readLine();
            cid = UUID.fromString(DataLine);
            List<TextMessage> lm = Server.Chat.getChatRoom(cid).getMessageList();
            Send.write("%R-GCRM%\n");
            Send.write(lm.size() + "\n");
            for (TextMessage i : lm) {
              Send.write(i.toJson().toJSONString() + "\n");
            }
            Send.flush();
            break;
          case "#SDM#":
            DataLine = Receive.readLine();
            System.out.println("SDM:" + DataLine);
            FileMessage df = new FileMessage(JSONObject.parseObject(DataLine));
            cid = df.getChatRoomId();
            List<String> un = Server.Chat.getChatRoom(cid).getUserList();
            un.remove(this.UserName);
            for (String i : un) {
              U2 = ConnectionSMap.get(i);
              U2.Send.write("%RDM%\n");
              U2.Send.write(cid + "\n");
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
}
