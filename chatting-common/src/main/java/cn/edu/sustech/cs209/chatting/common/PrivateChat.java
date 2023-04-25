package cn.edu.sustech.cs209.chatting.common;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PrivateChat extends ChatRoom {

  public PrivateChat(UUID CID,List<String> UL) {
    this.RoomType="Private";
    this.ChatRoomId = CID;
    this.UserList.addAll(UL);
    Collections.sort(this.UserList);
    this.ChatRoomName = this.UserList.get(0)+","+this.UserList.get(1);
  }
}