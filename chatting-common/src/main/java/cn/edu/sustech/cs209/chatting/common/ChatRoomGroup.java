package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatRoomGroup {

  private final Map<UUID, ChatRoom> chatRoomMap = new HashMap<>();
  private final Map<String, UUID> privateChatMap = new HashMap<>();

  private UUID generateCid() {
    UUID cid = UUID.randomUUID();
    while (chatRoomMap.containsKey(cid)) {
      cid = UUID.randomUUID();
    }
    return cid;
  }

  public UUID addPrivateChat(String un1, String un2) {
    List<String> ul = new ArrayList<>();
    ul.add(un1);
    ul.add(un2);
    ChatRoom p = new PrivateChat(generateCid(), ul);
    if (privateChatMap.containsKey(p.getChatRoomName())) {
      return null;
    }
    chatRoomMap.put(p.getChatRoomId(), p);
    privateChatMap.put(p.getChatRoomName(), p.getChatRoomId());
    return p.getChatRoomId();
  }

  public UUID addGroupChat(List<String> ul) {
    ChatRoom g = new GroupChat(generateCid(), ul);
    chatRoomMap.put(g.getChatRoomId(), g);
    return g.getChatRoomId();
  }

  public void joinGroupChat(UUID cid, String un) {
    GroupChat targetChatRoom = (GroupChat) chatRoomMap.get(cid);
    targetChatRoom.AddUser(un);
  }

  public void quitGroupChat(UUID cid, String un) {
    GroupChat targetChatRoom = (GroupChat) chatRoomMap.get(cid);
    targetChatRoom.RemoveUser(un);
  }

  public void addMessage(TextMessage m) {
    ChatRoom targetChatRoom = chatRoomMap.get(m.getChatRoomId());
    targetChatRoom.addMessage(m);
  }


  public ChatRoom getChatRoom(UUID cid) {
    return this.chatRoomMap.get(cid);
  }

}