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
  private final BufferedReader receive;
  private final BufferedWriter send;
  private final User U;
  private List<String> userList = new ArrayList<>();
  private List<UUID> chatList = new ArrayList<>();
  private Map<String, UUID> privateChatMap = new HashMap<>();
  private Map<UUID, List<String>> groupChatUserMap = new HashMap<>();
  private Map<UUID, String> chatNameMap = new HashMap<>();
  private Map<String, UUID> rechatNameMap = new HashMap<>();
  private List<String> receiveList = new ArrayList<>();
  private int NowreceiveList = 0;
  private MessageListener ML;
  private UUID NowChat;
  private List<Message> NowMessageList = new ArrayList<>();
  private String TempString;
  private List<String> TempListString;

  public void ChangeChat(String ChatName) throws IOException {
    UUID CID = rechatNameMap.get(ChatName);
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

  ConnectionC(String UN, String pwd) throws Throwable {
    this.S = new Socket(ServerHost, ServerPort);
    this.receive = new BufferedReader(new InputStreamReader(this.S.getInputStream()));
    this.send = new BufferedWriter(new OutputStreamWriter(this.S.getOutputStream()));
    this.U = new User(UN, pwd);
  }

  public void Close() throws Throwable {
    this.send.write("Close\n");
    this.send.flush();
    this.send.close();
    this.receive.close();
    this.S.close();
  }

  public boolean Connect() throws Throwable {
    this.send.write(this.U.GetUserName() + "\n");
    this.send.write(this.U.GetUserPassWord() + "\n");
    this.send.flush();
    String Reply = this.receive.readLine();
    if (Reply.equals("Fail")) {
      this.Close();
      return false;
    }
    String ULS = this.receive.readLine();
    List<String> UL = Convert.stringToList(ULS);
    for (String i : UL) {
      if (!i.equals(this.U.GetUserName())) {
        this.userList.add(i);
      }
    }
    Controller.NowController.ChangeOnlineUserList(this.userList);
    ML = new MessageListener(this.receive, this);
    ML.start();
    return true;
  }


  //send Text Message
  public void STM(String T) throws IOException {
    Message<String> TM = new Message<>(U.GetUserName(), NowChat, T);
    this.send.write("#STM#\n");
    this.send.write(TM.ToJson().toJSONString() + "\n");
    this.send.flush();
  }


  //send Doc Message
  public void SDM(DocFile DF) throws IOException {
    DF.SetMessage(U.GetUserName(), NowChat);
    this.send.write("#SDM#\n");
    this.send.write(DF.ToJson().toJSONString() + "\n");
    this.send.flush();
    System.out.println("SDM:" + DF.ToJson().toJSONString());
  }

  public void ReloadMessageList() {
    List<String> ML = new ArrayList<>();
    if (groupChatUserMap.containsKey(NowChat)) {
      ML.add("CurrentUser:" + groupChatUserMap.get(NowChat).toString());
    }
    for (Message i : NowMessageList) {
      ML.add(i.ToString());
    }
    Controller.NowController.ChangeMessageList(ML);
  }

  public void ReloadchatList() {
    //Collections.sort(chatList, (o1, o2) -> o2.getSeq() - o1.getSeq());
    List<String> ChatNameList = new ArrayList<>();
    for (UUID i : chatList) {
      ChatNameList.add(chatNameMap.get(i));
    }
    Controller.NowController.ChangeChatList(ChatNameList);
  }

  //Create Private Chat Room
  public void CPCR(String UN2) throws IOException {
    if (privateChatMap.containsKey(UN2)) {
      ChangeChat(privateChatMap.get(UN2));
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
    UL.add(this.U.GetUserName());
    TempListString = UL;
  }

  //Get Chat Room Message
  private void GCRM(UUID CID) throws IOException {
    this.send.write("#GCRM#\n");
    this.send.write(CID + "\n");
    this.send.flush();
  }

  public List<String> GetuserList() {
    return this.userList;
  }

  public void CloseClient() throws Throwable {
    ML.interrupt();
    this.send.write("#Close#\n");
    this.send.flush();
    Close();
  }

  class MessageListener extends Thread {

    private BufferedReader Messagereceive;
    private ConnectionC OriginalConnection;

    public MessageListener(BufferedReader R, ConnectionC OC) throws IOException {
      this.Messagereceive = new BufferedReader(new InputStreamReader(S.getInputStream()));
      this.OriginalConnection = OC;
    }

    public void run() {
      String CommandLine;
      while (true) {
        try {
          CommandLine = Messagereceive.readLine();
          String DataLine1, DataLine2, DataLine3;
          UUID CID;
          switch (CommandLine) {
            case "%Quit%":
              Messagereceive.close();
              System.exit(0);
            case "%Close%":
              JOptionPane.showMessageDialog(null, "Server Closed!", "Server Closed!",
                  JOptionPane.INFORMATION_MESSAGE);
              Close();
              break;
            case "%AU%":
              DataLine1 = Messagereceive.readLine();
              if (DataLine1.equals(U.GetUserName())) {
                continue;
              }
              userList.add(DataLine1);
              Controller.NowController.ChangeOnlineUserList(userList);
              break;
            case "%RU%":
              DataLine1 = Messagereceive.readLine();
              userList.remove(DataLine1);
              if (privateChatMap.containsKey(DataLine1)) {
                CID = privateChatMap.get(DataLine1);
                privateChatMap.remove(DataLine1);
                chatList.remove(CID);
                rechatNameMap.remove(DataLine1);
                chatNameMap.remove(CID);
                if (NowChat == CID) {
                  NowChat = null;
                  NowMessageList = new ArrayList<>();
                }
              }
              Controller.NowController.ChangeOnlineUserList(userList);
              ReloadchatList();
              ReloadMessageList();
              break;
            case "%RDM%":
              DataLine1 = Messagereceive.readLine();
              DataLine2 = Messagereceive.readLine();
              System.out.println("RDM:" + DataLine2);
              if (UUID.fromString(DataLine1) == NowChat) {
                DocFile DF = new DocFile(JSONObject.parseObject(DataLine2));
                DF.WriteFile("data/client/" + U.GetUserName() + "/" + DF.GetFileName());
              }
              break;
            case "%ATM%":
              DataLine1 = Messagereceive.readLine();
              Message<String> M = new Message<>(JSONObject.parseObject(DataLine1));
              if (NowChat != null && NowChat.equals(M.GetChatRoomId())) {
                NowMessageList.add(M);
                ReloadMessageList();
              } else if (chatList.contains(M.GetChatRoomId())) {
                JOptionPane.showMessageDialog(null, M.ToString(), "Message-" + U.GetUserName(),
                    JOptionPane.INFORMATION_MESSAGE);
              }
              break;
            case "%APC%":
              DataLine1 = Messagereceive.readLine();
              DataLine2 = Messagereceive.readLine();
              DataLine3 = Messagereceive.readLine();
              CID = UUID.fromString(DataLine2);
              privateChatMap.put(DataLine1, CID);
              if (DataLine3.split(",")[1].equals(U.GetUserName())) {
                chatNameMap.put(CID, DataLine3.split(",")[0]);
                rechatNameMap.put(DataLine3.split(",")[0], CID);
              } else {
                chatNameMap.put(CID, DataLine3.split(",")[1]);
                rechatNameMap.put(DataLine3.split(",")[1], CID);
              }
              chatList.add(UUID.fromString(DataLine2));
              ReloadchatList();
              break;
            case "%AGC%":
              DataLine1 = Messagereceive.readLine();
              DataLine2 = Messagereceive.readLine();
              CID = UUID.fromString(DataLine1);
              chatNameMap.put(CID, DataLine2);
              rechatNameMap.put(DataLine2, CID);
              chatList.add(CID);
              NowChat = CID;
              ChangeChat(CID);
              ReloadchatList();
              break;
            case "%R-CPCR%":
              DataLine1 = Messagereceive.readLine();
              CID = UUID.fromString(DataLine1);
              privateChatMap.put(TempString, CID);
              chatNameMap.put(CID, TempString);
              rechatNameMap.put(TempString, CID);
              chatList.add(CID);
              NowChat = CID;
              ChangeChat(CID);
              ReloadchatList();
              break;
            case "%R-GCRM%":
              List<Message> LM = new ArrayList<>();
              DataLine1 = Messagereceive.readLine();
              for (int i = 0; i < Integer.parseInt(DataLine1); i++) {
                DataLine2 = Messagereceive.readLine();
                LM.add(new Message(JSONObject.parseObject(DataLine2)));
              }
              NowMessageList = LM;
              ReloadMessageList();
              break;
            case "%R-CGCR%":
              DataLine1 = Messagereceive.readLine();
              System.out.println(U.GetUserName() + ":%R-CGCR%\n" + DataLine1);
              DataLine2 = Messagereceive.readLine();
              CID = UUID.fromString(DataLine1);
              groupChatUserMap.put(CID, TempListString);
              chatNameMap.put(CID, DataLine2);
              rechatNameMap.put(DataLine2, CID);
              chatList.add(CID);
              NowChat = CID;
              System.out.println("CHANGE");
              System.out.println(chatList);
              ChangeChat(CID);
              ReloadchatList();
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
