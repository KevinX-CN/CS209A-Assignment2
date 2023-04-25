package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatRoomGroup {

  private Map<UUID, ChatRoom> ChatRoomMap = new HashMap<>();
  private Map<String, UUID> PrivateChatMap = new HashMap<>();

  private UUID GenerateCID() {
    UUID CID = UUID.randomUUID();
    while (ChatRoomMap.containsKey(CID)) {
      CID = UUID.randomUUID();
    }
    return CID;
  }

  public UUID AddPrivateChat(String UN1, String UN2) {
    List<String> UL = new ArrayList<>();
    UL.add(UN1);
    UL.add(UN2);
    ChatRoom P = new PrivateChat(GenerateCID(), UL);
    if (PrivateChatMap.containsKey(P.GetChatRoomName())) {
      return null;
    }
    ChatRoomMap.put(P.GetChatRoomId(), P);
    PrivateChatMap.put(P.GetChatRoomName(), P.GetChatRoomId());
    return P.GetChatRoomId();
  }

  public UUID AddGroupChat(List<String> UL) {
    ChatRoom G = new GroupChat(GenerateCID(), UL);
    ChatRoomMap.put(G.GetChatRoomId(), G);
    return G.GetChatRoomId();
  }

  public void RemoveChatRoom(UUID CID) {
    if (ChatRoomMap.get(CID).GetRoomType() == "Private") {
      PrivateChatMap.remove(ChatRoomMap.get(CID).GetChatRoomName());
    }
    ChatRoomMap.remove(CID);
  }

  public void JoinGroupChat(UUID CID, String UN) {
    GroupChat TargetChatRoom = (GroupChat) ChatRoomMap.get(CID);
    TargetChatRoom.AddUser(UN);
  }

  public void QuitGroupChat(UUID CID, String UN) {
    GroupChat TargetChatRoom = (GroupChat) ChatRoomMap.get(CID);
    TargetChatRoom.RemoveUser(UN);
  }

  public void AddMessage(Message M) {
    ChatRoom TargetChatRoom = ChatRoomMap.get(M.GetChatRoomId());
    TargetChatRoom.AddMessage(M);
  }


  public ChatRoom GetChatRoom(UUID CID) {
    return this.ChatRoomMap.get(CID);
  }

  public void Save() {

  }

  public static void main(String[] args) {

  }
}