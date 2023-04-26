package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Convert;
import cn.edu.sustech.cs209.chatting.common.FileMessage;
import cn.edu.sustech.cs209.chatting.common.TextMessage;
import cn.edu.sustech.cs209.chatting.common.User;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.application.Platform;
import javax.swing.JOptionPane;

public class ConnectionC {

  private static final String ServerHost = "localhost";
  private static final int ServerPort = 1777;
  private final Socket S;
  private final BufferedReader Receive;
  private final BufferedWriter Send;
  protected final User U;
  private List<String> UserList = new ArrayList<>();
  private List<UUID> ChatList = new ArrayList<>();
  private Map<String, UUID> PrivateChatMap = new HashMap<>();
  private Map<UUID, List<String>> GroupChatUserMap = new HashMap<>();
  private Map<UUID, String> ChatNameMap = new HashMap<>();
  private Map<String, UUID> ReChatNameMap = new HashMap<>();
  private Map<UUID, Long> ChatLastTime = new HashMap<>();
  private MessageListener ML;
  UUID NowChat = UUID.randomUUID();
  private List<TextMessage> NowMessageList = new ArrayList<>();
  private String TempString;
  private List<String> TempListString;

  String formatMap(Map<String, UUID> map) {
    return Joiner.on(",").withKeyValueSeparator("=").join(map);
  }

  Map<String, UUID> parseMap(String formattedMap) {
    Map<String, String> sMap = Splitter.on(",").withKeyValueSeparator("=").split(formattedMap);
    Map<String, UUID> uMap = new HashMap<>();
    for (String i : sMap.keySet()) {
      uMap.put(i, UUID.fromString(sMap.get(i)));
    }
    return uMap;
  }

  Map<UUID, String> parseMap2(String formattedMap) {
    Map<String, String> sMap = Splitter.on(",").withKeyValueSeparator("=").split(formattedMap);
    Map<UUID, String> uMap = new HashMap<>();
    for (String i : sMap.keySet()) {
      uMap.put(UUID.fromString(i), sMap.get(i));
    }
    return uMap;
  }

  /*public void Save() throws IOException {
    File f = new File("data/client/" + U.getUserName() + "/History.data");
    System.out.println(f.getAbsolutePath());
    if (!f.exists()) {
      f.createNewFile();
    }
    BufferedWriter w = new BufferedWriter(new FileWriter(f));
    w.write(ChatList + "\n");
    w.write(formatMap(PrivateChatMap) + "\n");
    w.write(formatMap(ReChatNameMap) + "\n");
    w.flush();
  }

  public void Load() throws IOException {
    File f = new File("data/client/" + U.getUserName() + "/History.data");
    BufferedReader r = new BufferedReader(new FileReader(f));
    this.ChatList = new ArrayList<>();
    List<String> sList = Convert.stringToList(r.readLine());
    for (String i : sList) {
      this.ChatList.add(UUID.fromString(i));
    }
    this.PrivateChatMap = parseMap(r.readLine());
    this.ReChatNameMap = parseMap(r.readLine());
    this.ChatNameMap = new HashMap<>();
    for (String i : this.ReChatNameMap.keySet()) {
      this.ChatNameMap.put(ReChatNameMap.get(i), i);
    }
  }*/

  public void ChangeChat(String ChatName) throws IOException {
    UUID CID = ReChatNameMap.get(ChatName);
    if (NowChat == CID) {
      return;
    }
    NowMessageList = new ArrayList<>();
    NowChat = CID;
    GCRM(CID);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        ReloadMessageList();
      }
    });
  }

  public void ChangeChat(UUID CID) throws IOException {
    NowMessageList = new ArrayList<>();
    NowChat = CID;
    GCRM(CID);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        ReloadMessageList();
      }
    });
  }

  ConnectionC(String UN, String PWD) throws Throwable {
    this.U = new User(UN, PWD);
    //File f = new File("data/client/" + UN + "/History.data");
    //if (f.exists()) {
      //Load();
    //}
    this.S = new Socket(ServerHost, ServerPort);
    this.Receive = new BufferedReader(new InputStreamReader(this.S.getInputStream()));
    this.Send = new BufferedWriter(new OutputStreamWriter(this.S.getOutputStream()));

    File folder = new File("data/client/" + UN + "/");
    folder.mkdirs();
  }

  public void Close() throws Throwable {
    this.Send.write("Close\n");
    this.Send.flush();
    this.Send.close();
    this.Receive.close();
    this.S.close();
  }

  public boolean Connect() throws Throwable {
    this.Send.write(this.U.getUserName() + "\n");
    this.Send.write(this.U.getPassWord() + "\n");
    this.Send.flush();
    String Reply = this.Receive.readLine();
    if (Reply.equals("Fail")) {
      this.Close();
      return false;
    }
    String ULS = this.Receive.readLine();
    List<String> UL = Convert.stringToList(ULS);
    for (String i : UL) {
      if (!i.equals(this.U.getUserName())) {
        this.UserList.add(i);
      }
    }
    try {
      Controller.NowController.ChangeOnlineUserList(this.UserList);
    } catch (Exception e) {

    }
    ML = new MessageListener(this.Receive, this);
    ML.start();
    return true;
  }


  //Send Text Message
  public void STM(String T) throws IOException {
    TextMessage TM = new TextMessage(U.getUserName(), NowChat, T);
    this.Send.write("#STM#\n");
    this.Send.write(TM.toJson().toJSONString() + "\n");
    this.Send.flush();
    ChatLastTime.put(NowChat, System.currentTimeMillis());
  }


  //Send Doc Message
  public void SDM(FileMessage DF) throws IOException {
    this.Send.write("#SDM#\n");
    this.Send.write(DF.ToJson().toJSONString() + "\n");
    this.Send.flush();
    System.out.println("SDM:" + DF.ToJson().toJSONString());
  }

  public void ReloadMessageList() {
    ReloadChatList();
    List<String> ML = new ArrayList<>();
    if (GroupChatUserMap.containsKey(NowChat)) {
      ML.add("CurrentUser:" + GroupChatUserMap.get(NowChat).toString());
    }
    for (TextMessage i : NowMessageList) {
      ML.add(i.toString());
    }
    try {
      Controller.NowController.ChangeMessageList(ML);
    } catch (Exception e) {

    }
  }

  public void ReloadChatList() {
    Collections.sort(this.ChatList,
      (o1, o2) -> (int) (ChatLastTime.get(o2) - ChatLastTime.get(o1)));
    System.out.println(ChatLastTime.toString());
    List<String> ChatNameList = new ArrayList<>();
    for (UUID i : ChatList) {
      ChatNameList.add(ChatNameMap.get(i));
    }
    try {
      Controller.NowController.ChangeChatList(ChatNameList);
    } catch (Exception e) {

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
    this.Send.write("#CPCR#\n");
    this.Send.write(UN2 + "\n");
    this.Send.flush();
  }

  //Create Group Chat Room
  public void CGCR(List<String> UL) throws IOException {
    this.Send.write("#CGCR#\n");
    this.Send.write(UL + "\n");
    this.Send.flush();
    UL.add(this.U.getUserName());
    TempListString = UL;
  }

  //Get Chat Room Message
  private void GCRM(UUID CID) throws IOException {
    this.Send.write("#GCRM#\n");
    this.Send.write(CID + "\n");
    this.Send.flush();
  }

  public void writeFile(FileMessage DF) throws IOException {
    DF.writeFile("data/client/" + U.getUserName() + "/" + DF.getFileName());
  }

  public List<String> GetUserList() {
    return this.UserList;
  }

  public void CloseClient() throws Throwable {
    ML.interrupt();
    this.Send.write("#Close#\n");
    this.Send.flush();
    Close();
    Thread.sleep(5000);
  }

  class MessageListener extends Thread {

    private BufferedReader MessageReceive;
    private ConnectionC OriginalConnection;

    public MessageListener(BufferedReader R, ConnectionC OC) throws IOException {
      this.MessageReceive = new BufferedReader(new InputStreamReader(S.getInputStream()));
      this.OriginalConnection = OC;
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
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  Controller.NowController.ChangeOnlineUserList(UserList);
                }
              });
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
              Platform.runLater(new Runnable() {
                @Override
                public void run() {

                  Controller.NowController.ChangeOnlineUserList(UserList);
                  ReloadChatList();
                  ReloadMessageList();
                }
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
                Platform.runLater(new Runnable() {
                  @Override
                  public void run() {
                    ReloadMessageList();
                  }
                });
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
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  ReloadChatList();
                }
              });
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
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  ReloadChatList();
                }
              });
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
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  ReloadChatList();
                }
              });
              break;
            case "%R-GCRM%":
              List<TextMessage> LM = new ArrayList<>();
              DataLine1 = MessageReceive.readLine();
              for (int i = 0; i < Integer.parseInt(DataLine1); i++) {
                DataLine2 = MessageReceive.readLine();
                LM.add(new TextMessage(JSONObject.parseObject(DataLine2)));
              }
              NowMessageList = LM;
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  ReloadMessageList();
                }
              });
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
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  ReloadChatList();
                }
              });
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
