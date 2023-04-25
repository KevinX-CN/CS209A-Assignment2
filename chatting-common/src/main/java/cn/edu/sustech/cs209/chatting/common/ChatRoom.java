package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatRoom {

  protected UUID ChatRoomId;
  protected String ChatRoomName;
  protected List<String> UserList = new ArrayList<>();
  protected List<Message> MessageList = new ArrayList<>();
  protected Long LastReplyTimestamp = 0L;
  protected String RoomType;

  public ChatRoom() {

  }

  public ChatRoom(JSONObject J) {
    this.ChatRoomId = UUID.fromString(J.getString("ChatRoomId"));
    this.ChatRoomName = J.getString("ChatRoomName");
    this.RoomType = J.getString("RoomType");
    this.LastReplyTimestamp = J.getLong("LastReplyTimestamp");
    this.UserList = Convert.StringToList(J.getString("UserList"));
    List<String> MSList = Convert.StringToList(J.getString("MessageList"));
    if (MSList.isEmpty()|| MSList.get(0).equals("")) {
      return;
    }
    for (String i : MSList) {
      System.out.println(i);
      MessageList.add(new Message(JSON.parseObject(i)));
    }
  }

  public void AddMessage(Message M) {
    MessageList.add(M);
    LastReplyTimestamp = M.GetTimestamp();
  }

  public UUID GetChatRoomId() {
    return this.ChatRoomId;
  }

  public String GetChatRoomName() {
    return this.ChatRoomName;
  }

  public String GetRoomType() {
    return this.RoomType;
  }

  public List<Message> GetMessageList() {
    return this.MessageList;
  }

  public List<String> GetUserList() {
    return this.UserList;
  }

  public JSONObject ToJSON() {
    JSONObject ChatRoonJSON = new JSONObject();
    ChatRoonJSON.put("ChatRoomId", this.ChatRoomId);
    ChatRoonJSON.put("ChatRoomName", this.ChatRoomName);
    ChatRoonJSON.put("RoomType", this.RoomType);
    ChatRoonJSON.put("LastReplyTimestamp", this.LastReplyTimestamp);
    ChatRoonJSON.put("UserList", this.UserList);
    List<String> ML=new ArrayList<>();
    for(int )
    {
      ChatRoonJSON.put("MessageList", this.MessageList);
    }
    return ChatRoonJSON;
  }

  public static void main(String[] args) {
    List<String> UL = new ArrayList<>();
    UL.add("A");
    UL.add("B");
    PrivateChat PC = new PrivateChat(UUID.randomUUID(), UL);
    System.out.println(PC.ToJSON());
    Message<String> MS1=new Message("A",UUID.randomUUID(),"HAHA");
    Message<String> MS2=new Message("A",UUID.randomUUID(),"WAWA");
    UL.add("C");
    GroupChat GC = new GroupChat(UUID.randomUUID(), UL);
    GC.AddMessage(MS1);
    GC.AddMessage(MS2);
    System.out.println(GC.ToJSON());
    ChatRoom CR = new ChatRoom(GC.ToJSON());
  }
}
