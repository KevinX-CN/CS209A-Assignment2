package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ChatRoom {

  protected UUID chatRoomId;
  protected String chatRoomType;
  protected String chatRoomName;
  protected List<String> userList = new ArrayList<>();
  private final List<TextMessage> messageList = new ArrayList<>();

  public ChatRoom() {

  }

  public void addMessage(TextMessage m) {
    messageList.add(m);
  }

  public UUID getChatRoomId() {
    return this.chatRoomId;
  }

  public String getChatRoomName() {
    return this.chatRoomName;
  }

  public List<TextMessage> getMessageList() {
    return this.messageList;
  }

  public List<String> getUserList() {
    return this.userList;
  }

}
