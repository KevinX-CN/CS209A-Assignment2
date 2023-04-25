package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSONObject;
import java.util.UUID;

public class Message<T> {

  private final Long Timestamp;
  private final String SenderName;
  private final UUID ChatRoomId;
  private final T Message;

  public Message(String UN, UUID CID, T M) {
    this.Timestamp = System.currentTimeMillis();
    this.SenderName = UN;
    this.ChatRoomId = CID;
    this.Message = M;
  }

  public Message(JSONObject J) {
    this.Timestamp = J.getLong("Timestamp");
    this.SenderName = J.getString("SenderName");
    this.ChatRoomId = UUID.fromString(J.getString("ChatRoomId"));
    this.Message = (T) J.getString("chatting/common/Message");
  }

  public JSONObject ToJson() {
    JSONObject MessageJSON = new JSONObject();
    MessageJSON.put("Timestamp", this.Timestamp);
    MessageJSON.put("SenderName", this.SenderName);
    MessageJSON.put("ChatRoomId", this.ChatRoomId);
    MessageJSON.put("chatting/common/Message", this.Message);
    return MessageJSON;
  }

  public Long GetTimestamp() {
    return this.Timestamp;
  }

  public UUID GetChatRoomId() {
    return this.ChatRoomId;
  }

  public String ToString() {
    return this.SenderName+":\n"+this.Message;
  }
}