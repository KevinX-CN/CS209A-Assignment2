package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatRoomGroup {

  private Map<UUID, ChatRoom> chatRoomMap = new HashMap<>();
  private Map<String, UUID> privateChatMap = new HashMap<>();

  private UUID generateCid() {
    UUID cid = UUID.randomUUID();
    while (chatRoomMap.containsKey(cid)) {
      cid = UUID.randomUUID();
    }
    return cid;
  }

  public UUID addPrivateChat(String un1, String un2) {
    List<String> UL = new ArrayList<>();
    UL.add(un1);
    UL.add(un2);
    ChatRoom P = new PrivateChat(generateCid(), UL);
    if (privateChatMap.containsKey(P.GetChatRoomName())) {
      return null;
    }
    chatRoomMap.put(P.GetChatRoomId(), P);
    privateChatMap.put(P.GetChatRoomName(), P.GetChatRoomId());
    return P.GetChatRoomId();
  }

  public UUID AddGroupChat(List<String> UL) {
    ChatRoom G = new GroupChat(generateCid(), UL);
    chatRoomMap.put(G.GetChatRoomId(), G);
    return G.GetChatRoomId();
  }

  public void JoinGroupChat(UUID CID, String UN) {
    GroupChat TargetChatRoom = (GroupChat) chatRoomMap.get(CID);
    TargetChatRoom.AddUser(UN);
  }

  public void QuitGroupChat(UUID CID, String UN) {
    GroupChat TargetChatRoom = (GroupChat) chatRoomMap.get(CID);
    TargetChatRoom.RemoveUser(UN);
  }

  public void AddMessage(Message M) {
    ChatRoom TargetChatRoom = chatRoomMap.get(M.GetChatRoomId());
    TargetChatRoom.AddMessage(M);
  }

  public ChatRoom GetChatRoom(UUID CID) {
    return this.chatRoomMap.get(CID);
  }

}