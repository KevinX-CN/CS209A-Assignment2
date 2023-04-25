package cn.edu.sustech.cs209.chatting.common;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GroupChat extends ChatRoom {

  public GroupChat(UUID CID, List<String> UL) {
    this.RoomType = "Group";
    this.ChatRoomId = CID;
    this.UserList.addAll(UL);
    Rename();
  }

  public void Rename() {
    Collections.sort(this.UserList);
    if (this.UserList.size() == 1) {
      this.ChatRoomName = this.UserList.get(0) + "(1)";
    } else if (this.UserList.size() == 2) {
      this.ChatRoomName = this.UserList.get(0) + "," + this.UserList.get(1) + "(2)";
    } else if (this.UserList.size() == 3) {
      this.ChatRoomName =
        this.UserList.get(0) + "," + this.UserList.get(1) + "," + this.UserList.get(2) + "(3)";
    } else {
      this.ChatRoomName =
        this.UserList.get(0) + "," + this.UserList.get(1) + "," + this.UserList.get(2) + "...("
          + this.UserList.size() + ")";
    }
  }

  public void AddUser(String UN) {
    UserList.add(UN);
    Rename();
  }

  public void RemoveUser(String UN) {
    UserList.remove(UN);
    Rename();
  }
}