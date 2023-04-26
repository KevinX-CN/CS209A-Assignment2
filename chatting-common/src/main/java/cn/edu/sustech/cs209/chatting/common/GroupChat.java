package cn.edu.sustech.cs209.chatting.common;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GroupChat extends ChatRoom {

  public GroupChat(UUID cid, List<String> ul) {
    super.chatRoomType = "Group";
    super.chatRoomId = cid;
    super.userList.addAll(ul);
    Rename();
  }

  public void Rename() {
    Collections.sort(super.userList);
    if (super.userList.size() == 1) {
      super.chatRoomName = super.userList.get(0) + "(1)";
    } else if (super.userList.size() == 2) {
      super.chatRoomName = this.userList.get(0) + "," + this.userList.get(1) + "(2)";
    } else if (this.userList.size() == 3) {
      super.chatRoomName =
        super.userList.get(0) + "," + this.userList.get(1) + "," + this.userList.get(2) + "(3)";
    } else {
      super.chatRoomName =
        this.userList.get(0) + "," + this.userList.get(1) + "," + this.userList.get(2) + "...("
          + this.userList.size() + ")";
    }
  }

  public void AddUser(String un) {
    super.userList.add(un);
    Rename();
  }

  public void RemoveUser(String un) {
    super.userList.remove(un);
    Rename();
  }
}