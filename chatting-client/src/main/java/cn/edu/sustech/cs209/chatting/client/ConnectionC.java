package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Convert;
import cn.edu.sustech.cs209.chatting.common.DocFile;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.JOptionPane;

public class ConnectionC {

  private static final String ServerHost = "localhost";
  private static final int ServerPort = 1777;
  private final Socket S;
  private final BufferedReader Receive;
  private final BufferedWriter Send;
  private final User U;
  private List<String> UserList = new ArrayList<>();
  private List<UUID> ChatList = new ArrayList<>();
  private Map<String, UUID> PrivateChatMap = new HashMap<>();
  private Map<UUID, List<String>> GroupChatUserMap = new HashMap<>();
  private Map<UUID, String> ChatNameMap = new HashMap<>();
  private Map<String, UUID> ReChatNameMap = new HashMap<>();
  private List<String> ReceiveList = new ArrayList<>();
  private int NowReceiveList = 0;
  private MessageListener ML;
  private UUID NowChat;
  private List<Message> NowMessageList = new ArrayList<>();
  private String TempString;
  private List<String> TempListString;

  public void ChangeChat(String ChatName) throws IOException {
    UUID CID = ReChatNameMap.get(ChatName);
    if (NowChat == CID) {
      return;
    }
    NowMessageList = new ArrayList<>();
    NowChat = CID;
    GCRM(CID);
    ReloadMessageList();
  }

  public void ChangeChat(UUID CID) throws IOException {
    NowMessageList = new ArrayList<>();
    NowChat = CID;
    GCRM(CID);
    ReloadMessageList();
  }

  ConnectionC(String UN, String PWD) throws Throwable {
    this.S = new Socket(ServerHost, ServerPort);
    this.Receive = new BufferedReader(new InputStreamReader(this.S.getInputStream()));
    this.Send = new BufferedWriter(new OutputStreamWriter(this.S.getOutputStream()));
    this.U = new User(UN, PWD);
  }

  public void Close() throws Throwable {
    this.Send.write("Close\n");
    this.Send.flush();
    this.Send.close();
    this.Receive.close();
    this.S.close();
  }

  public boolean Connect() throws Throwable {
    this.Send.write(this.U.GetUserName() + "\n");
    this.Send.write(this.U.GetUserPassWord() + "\n");
    this.Send.flush();
    String Reply = this.Receive.readLine();
    if (Reply.equals("Fail")) {
      this.Close();
      return false;
    }
    String ULS = this.Receive.readLine();
    List<String> UL = Convert.StringToList(ULS);
    for (String i : UL) {
      if (!i.equals(this.U.GetUserName())) {
        this.UserList.add(i);
      }
    }
    Controller.NowController.ChangeOnlineUserList(this.UserList);
    ML = new MessageListener(this.Receive, this);
    ML.start();
    return true;
  }


  //Send Text Message
  public void STM(String T) throws IOException {
    Message<String> TM = new Message<>(U.GetUserName(), NowChat, T);
    this.Send.write("#STM#\n");
    this.Send.write(TM.ToJson().toJSONString() + "\n");
    this.Send.flush();
  }


  //Send Doc Message
  public void SDM(DocFile DF) throws IOException {
    DF.SetMessage(U.GetUserName(), NowChat);
    this.Send.write("#SDM#\n");
    this.Send.write(DF.ToJson().toJSONString() + "\n");
    this.Send.flush();
    System.out.println("SDM:" + DF.ToJson().toJSONString());
  }

  public void ReloadMessageList() {
    List<String> ML = new ArrayList<>();
    if (GroupChatUserMap.containsKey(NowChat)) {
      ML.add("CurrentUser:" + GroupChatUserMap.get(NowChat).toString());
    }
    for (Message i : NowMessageList) {
      ML.add(i.ToString());
    }
    Controller.NowController.ChangeMessageList(ML);
  }

  public void ReloadChatList() {
    //Collections.sort(ChatList, (o1, o2) -> o2.getSeq() - o1.getSeq());
    List<String> ChatNameList = new ArrayList<>();
    for (UUID i : ChatList) {
      ChatNameList.add(ChatNameMap.get(i));
    }
    Controller.NowController.ChangeChatList(ChatNameList);
  }

  //Create Private Chat Room
  public void CPCR(String UN2) throws IOException {
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
    UL.add(this.U.GetUserName());
    TempListString = UL;
  }

  //Get Chat Room Message
  private void GCRM(UUID CID) throws IOException {
    this.Send.write("#GCRM#\n");
    this.Send.write(CID + "\n");
    this.Send.flush();
  }

  public List<String> GetUserList() {
    return this.UserList;
  }

  public void CloseClient() throws Throwable {
    ML.interrupt();
    this.Send.write("#Close#\n");
    this.Send.flush();
    Close();
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
              if (DataLine1.equals(U.GetUserName())) {
                continue;
              }
              UserList.add(DataLine1);
              Controller.NowController.ChangeOnlineUserList(UserList);
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
              Controller.NowController.ChangeOnlineUserList(UserList);
              ReloadChatList();
              ReloadMessageList();
              break;
            case "%RDM%":
              DataLine1 = MessageReceive.readLine();
              DataLine2 = MessageReceive.readLine();
              System.out.println("RDM:" + DataLine2);
              if (UUID.fromString(DataLine1) == NowChat) {
                DocFile DF = new DocFile(JSONObject.parseObject(DataLine2));
                DF.WriteFile("data/client/" + U.GetUserName() + "/" + DF.GetFileName());
              }
              break;
            case "%ATM%":
              DataLine1 = MessageReceive.readLine();
              Message<String> M = new Message<>(JSONObject.parseObject(DataLine1));
              if (NowChat != null && NowChat.equals(M.GetChatRoomId())) {
                NowMessageList.add(M);
                ReloadMessageList();
              } else if (ChatList.contains(M.GetChatRoomId())) {
                JOptionPane.showMessageDialog(null, M.ToString(), "Message-" + U.GetUserName(),
                  JOptionPane.INFORMATION_MESSAGE);
              }
              break;
            case "%APC%":
              DataLine1 = MessageReceive.readLine();
              DataLine2 = MessageReceive.readLine();
              DataLine3 = MessageReceive.readLine();
              CID = UUID.fromString(DataLine2);
              PrivateChatMap.put(DataLine1, CID);
              if (DataLine3.split(",")[1].equals(U.GetUserName())) {
                ChatNameMap.put(CID, DataLine3.split(",")[0]);
                ReChatNameMap.put(DataLine3.split(",")[0], CID);
              } else {
                ChatNameMap.put(CID, DataLine3.split(",")[1]);
                ReChatNameMap.put(DataLine3.split(",")[1], CID);
              }
              ChatList.add(UUID.fromString(DataLine2));
              ReloadChatList();
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
              ReloadChatList();
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
              ReloadChatList();
              break;
            case "%R-GCRM%":
              List<Message> LM = new ArrayList<>();
              DataLine1 = MessageReceive.readLine();
              for (int i = 0; i < Integer.parseInt(DataLine1); i++) {
                DataLine2 = MessageReceive.readLine();
                LM.add(new Message(JSONObject.parseObject(DataLine2)));
              }
              NowMessageList = LM;
              ReloadMessageList();
              break;
            case "%R-CGCR%":
              DataLine1 = MessageReceive.readLine();
              System.out.println(U.GetUserName() + ":%R-CGCR%\n" + DataLine1);
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
              ReloadChatList();
              break;
          }
        } catch (Exception e) {
          e.printStackTrace();
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static void main(String[] args) throws Throwable {
    ConnectionC A = new ConnectionC("A", "");
    ConnectionC B = new ConnectionC("B", "");
    ConnectionC C = new ConnectionC("C", "");
    A.Connect();
    B.Connect();
    C.Connect();
    A.CPCR("B");
    A.CPCR("C");
  }
}
