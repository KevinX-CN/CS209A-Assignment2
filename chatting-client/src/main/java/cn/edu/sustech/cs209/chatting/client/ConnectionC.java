package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Convert;
import cn.edu.sustech.cs209.chatting.common.FileMessage;
import cn.edu.sustech.cs209.chatting.common.TextMessage;
import cn.edu.sustech.cs209.chatting.common.User;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.application.Platform;
import javax.swing.JOptionPane;

public class ConnectionC {

  private static final String ServerHost = "localhost";
  private static final int ServerPort = 1777;
  private final Socket s;
  private final BufferedReader receive;
  private final BufferedWriter send;
  protected final User U;
  private final List<String> UserList = new ArrayList<>();
  private final List<UUID> ChatList = new ArrayList<>();
  private final Map<String, UUID> PrivateChatMap = new HashMap<>();
  private final Map<UUID, List<String>> GroupChatUserMap = new HashMap<>();
  private final Map<UUID, String> ChatNameMap = new HashMap<>();
  private final Map<String, UUID> ReChatNameMap = new HashMap<>();
  private final Map<UUID, Long> ChatLastTime = new HashMap<>();
  private MessageListener ml;
  UUID NowChat = UUID.randomUUID();
  private List<TextMessage> NowMessageList = new ArrayList<>();
  private String TempString;
  private List<String> TempListString;


  public void ChangeChat(String ChatName) throws IOException {
    UUID cid = ReChatNameMap.get(ChatName);
    if (NowChat == cid) {
      return;
    }
    NowMessageList = new ArrayList<>();
    NowChat = cid;
    GCRM(cid);
    Platform.runLater(this::ReloadMessageList);
  }

  public void ChangeChat(UUID cid) throws IOException {
    NowMessageList = new ArrayList<>();
    NowChat = cid;
    GCRM(cid);
    Platform.runLater(this::ReloadMessageList);
  }

  ConnectionC(String un, String pwd) throws Throwable {
    this.U = new User(un, pwd);
    this.s = new Socket(ServerHost, ServerPort);
    this.receive = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
    this.send = new BufferedWriter(new OutputStreamWriter(this.s.getOutputStream()));

    File folder = new File("data/client/" + un + "/");
    folder.mkdirs();
  }

  public void Close() throws Throwable {
    this.send.write("Close\n");
    this.send.flush();
    this.send.close();
    this.receive.close();
    this.s.close();
  }

  public boolean Connect() throws Throwable {
    this.send.write(this.U.getUserName() + "\n");
    this.send.write(this.U.getPassWord() + "\n");
    this.send.flush();
    String Reply = this.receive.readLine();
    if (Reply.equals("Fail")) {
      this.Close();
      return false;
    }
    String uls = this.receive.readLine();
    List<String> ul = Convert.stringToList(uls);
    for (String i : ul) {
      if (!i.equals(this.U.getUserName())) {
        this.UserList.add(i);
      }
    }
    try {
      Controller.nowController.changeOnlineUserList(this.UserList);
    } catch (Exception ignored) {

    }
    ml = new MessageListener();
    ml.start();
    return true;
  }


  //Send Text Message
  public void stm(String T) throws IOException {
    TextMessage tm = new TextMessage(U.getUserName(), NowChat, T);
    this.send.write("#STM#\n");
    this.send.write(tm.toJson().toJSONString() + "\n");
    this.send.flush();
    ChatLastTime.put(NowChat, System.currentTimeMillis());
  }


  //Send Doc Message
  public void sdm(FileMessage df) throws IOException {
    this.send.write("#SDM#\n");
    this.send.write(df.ToJson().toJSONString() + "\n");
    this.send.flush();
    System.out.println("SDM:" + df.ToJson().toJSONString());
  }

  public void ReloadMessageList() {
    ReloadChatList();
    List<String> ml = new ArrayList<>();
    if (GroupChatUserMap.containsKey(NowChat)) {
      ml.add("CurrentUser:" + GroupChatUserMap.get(NowChat).toString());
    }
    for (TextMessage i : NowMessageList) {
      ml.add(i.toString());
    }
    try {
      Controller.nowController.ChangeMessageList(ml);
    } catch (Exception ignored) {

    }
  }

  public void ReloadChatList() {
    this.ChatList.sort((o1, o2) -> (int) (ChatLastTime.get(o2) - ChatLastTime.get(o1)));
    System.out.println(ChatLastTime);
    List<String> ChatNameList = new ArrayList<>();
    for (UUID i : ChatList) {
      ChatNameList.add(ChatNameMap.get(i));
    }
    try {
      Controller.nowController.ChangeChatList(ChatNameList);
    } catch (Exception ignored) {

    }
  }

  //Create Private Chat Room
  public void CPCR(String UN2) throws IOException {
    if (UN2 == null) {
      return;
    }
    if (PrivateChatMap.containsKey(UN2)) {
      ChangeChat(PrivateChatMap.get(UN2));
      return;
    }
    this.TempString = UN2;
    this.send.write("#CPCR#\n");
    this.send.write(UN2 + "\n");
    this.send.flush();
  }

  //Create Group Chat Room
  public void CGCR(List<String> UL) throws IOException {
    this.send.write("#CGCR#\n");
    this.send.write(UL + "\n");
    this.send.flush();
    UL.add(this.U.getUserName());
    TempListString = UL;
  }

  //Get Chat Room Message
  private void GCRM(UUID CID) throws IOException {
    this.send.write("#GCRM#\n");
    this.send.write(CID + "\n");
    this.send.flush();
  }

  public void writeFile(FileMessage DF) throws IOException {
    DF.writeFile("data/client/" + U.getUserName() + "/" + DF.getFileName());
  }

  public List<String> GetUserList() {
    return this.UserList;
  }

  public void CloseClient() throws Throwable {
    ml.interrupt();
    this.send.write("#Close#\n");
    this.send.flush();
    Close();
    Thread.sleep(5000);
  }

  class MessageListener extends Thread {

    private final BufferedReader MessageReceive;

    public MessageListener() throws IOException {
      this.MessageReceive = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    public void run() {
      String CommandLine;
      while (true) {
        try {
          CommandLine = MessageReceive.readLine();
          String DataLine1, DataLine2, DataLine3;
          UUID CID;
          switch (CommandLine) {
            case "%Quit%":
              MessageReceive.close();
              System.exit(0);
            case "%Close%":
              JOptionPane.showMessageDialog(null, "Server Closed!", "Server Closed!",
                  JOptionPane.INFORMATION_MESSAGE);
              Close();
              while (true) {
                continue;
              }
            case "%AU%":
              DataLine1 = MessageReceive.readLine();
              if (DataLine1.equals(U.getUserName())) {
                continue;
              }
              UserList.add(DataLine1);
              Platform.runLater(() -> Controller.nowController.changeOnlineUserList(UserList));
              break;
            case "%RU%":
              DataLine1 = MessageReceive.readLine();
              UserList.remove(DataLine1);
              if (PrivateChatMap.containsKey(DataLine1)) {
                CID = PrivateChatMap.get(DataLine1);
                PrivateChatMap.remove(DataLine1);
                ChatList.remove(CID);
                ReChatNameMap.remove(DataLine1);
                ChatNameMap.remove(CID);
                if (NowChat == CID) {
                  NowChat = null;
                  NowMessageList = new ArrayList<>();
                }
              }
              Platform.runLater(() -> {

                Controller.nowController.changeOnlineUserList(UserList);
                ReloadChatList();
                ReloadMessageList();
              });
              break;
            case "%RDM%":
              DataLine1 = MessageReceive.readLine();
              DataLine2 = MessageReceive.readLine();
              FileMessage DF = new FileMessage(JSONObject.parseObject(DataLine2));
              System.out.println("RDM:" + DF.ToJson().toJSONString());
              System.out.println(
                "File Receive in data/client/" + U.getUserName() + "/" + DF.getFileName());
              writeFile(DF);
              break;
            case "%ATM%":
              DataLine1 = MessageReceive.readLine();
              TextMessage M = new TextMessage(JSONObject.parseObject(DataLine1));
              if (NowChat != null && NowChat.equals(M.getChatRoomId())) {
                NowMessageList.add(M);
                ChatLastTime.put(M.getChatRoomId(), M.getTimestamp());
                Platform.runLater(ConnectionC.this::ReloadMessageList);
              } else if (ChatList.contains(M.getChatRoomId())) {
                JOptionPane.showMessageDialog(null, M.toString(), "Message-" + U.getUserName(),
                  JOptionPane.INFORMATION_MESSAGE);
                ChatLastTime.put(M.getChatRoomId(), M.getTimestamp());
              }
              break;
            case "%APC%":
              DataLine1 = MessageReceive.readLine();
              DataLine2 = MessageReceive.readLine();
              DataLine3 = MessageReceive.readLine();
              CID = UUID.fromString(DataLine2);
              PrivateChatMap.put(DataLine1, CID);
              if (DataLine3.split(",")[1].equals(U.getUserName())) {
                ChatNameMap.put(CID, DataLine3.split(",")[0]);
                ReChatNameMap.put(DataLine3.split(",")[0], CID);
              } else {
                ChatNameMap.put(CID, DataLine3.split(",")[1]);
                ReChatNameMap.put(DataLine3.split(",")[1], CID);
              }
              ChatList.add(UUID.fromString(DataLine2));
              ChatLastTime.put(CID, 0L);
              Platform.runLater(ConnectionC.this::ReloadChatList);
              break;
            case "%AGC%":
              DataLine1 = MessageReceive.readLine();
              DataLine2 = MessageReceive.readLine();
              CID = UUID.fromString(DataLine1);
              ChatNameMap.put(CID, DataLine2);
              ReChatNameMap.put(DataLine2, CID);
              ChatList.add(CID);
              NowChat = CID;
              ChangeChat(CID);
              Platform.runLater(ConnectionC.this::ReloadChatList);
              ChatLastTime.put(CID, 0L);
              break;
            case "%R-CPCR%":
              DataLine1 = MessageReceive.readLine();
              CID = UUID.fromString(DataLine1);
              PrivateChatMap.put(TempString, CID);
              ChatNameMap.put(CID, TempString);
              ReChatNameMap.put(TempString, CID);
              ChatList.add(CID);
              NowChat = CID;
              ChangeChat(CID);
              ChatLastTime.put(CID, System.currentTimeMillis());
              Platform.runLater(ConnectionC.this::ReloadChatList);
              break;
            case "%R-GCRM%":
              List<TextMessage> LM = new ArrayList<>();
              DataLine1 = MessageReceive.readLine();
              for (int i = 0; i < Integer.parseInt(DataLine1); i++) {
                DataLine2 = MessageReceive.readLine();
                LM.add(new TextMessage(JSONObject.parseObject(DataLine2)));
              }
              NowMessageList = LM;
              Platform.runLater(ConnectionC.this::ReloadMessageList);
              break;
            case "%R-CGCR%":
              DataLine1 = MessageReceive.readLine();
              System.out.println(U.getUserName() + ":%R-CGCR%\n" + DataLine1);
              DataLine2 = MessageReceive.readLine();
              CID = UUID.fromString(DataLine1);
              GroupChatUserMap.put(CID, TempListString);
              ChatNameMap.put(CID, DataLine2);
              ReChatNameMap.put(DataLine2, CID);
              ChatList.add(CID);
              NowChat = CID;
              System.out.println("CHANGE");
              System.out.println(ChatList);
              ChangeChat(CID);
              ChatLastTime.put(CID, System.currentTimeMillis());
              Platform.runLater(ConnectionC.this::ReloadChatList);
              break;
          }
        } catch (java.lang.IllegalStateException e) {
          System.out.println("E2");
        } catch (Throwable e) {
          System.out.println("E1");
        }
      }
    }
  }
}
