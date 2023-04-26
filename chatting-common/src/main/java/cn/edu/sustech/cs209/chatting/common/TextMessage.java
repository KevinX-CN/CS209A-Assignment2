package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSONObject;
import java.util.UUID;

public class TextMessage {

  private final Long timestamp;
  private final String senderName;
  private final UUID chatRoomId;
  private final String message;

  public TextMessage(String un, UUID cid, String m) {
    this.timestamp = System.currentTimeMillis();
    this.senderName = un;
    this.chatRoomId = cid;
    this.message = m;
  }

  public TextMessage(JSONObject J) {
    this.timestamp = J.getLong("timestamp");
    this.senderName = J.getString("senderName");
    this.chatRoomId = UUID.fromString(J.getString("chatRoomId"));
    this.message = J.getString("message");
  }

  public JSONObject toJson() {
    JSONObject messageJson = new JSONObject();
    messageJson.put("timestamp", this.timestamp);
    messageJson.put("senderName", this.senderName);
    messageJson.put("chatRoomId", this.chatRoomId);
    messageJson.put("message", this.message);
    return messageJson;
  }

  public Long getTimestamp() {
    return this.timestamp;
  }

  public UUID getChatRoomId() {
    return this.chatRoomId;
  }

  public String toString() {
    return this.senderName + ":\n" + this.message;
  }
}