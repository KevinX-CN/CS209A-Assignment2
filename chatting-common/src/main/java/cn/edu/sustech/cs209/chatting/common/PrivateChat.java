package cn.edu.sustech.cs209.chatting.common;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PrivateChat extends ChatRoom {

  public PrivateChat(UUID CID, List<String> UL) {
    super.chatRoomType = "Private";
    super.chatRoomId = CID;
    super.userList.addAll(UL);
    Collections.sort(this.userList);
    super.chatRoomName = super.userList.get(0) + "," + super.userList.get(1);
  }
}