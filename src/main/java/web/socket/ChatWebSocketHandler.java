package web.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import web.model.dto.community.MessageDto;
import web.service.community.ChattingService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChattingService service;
    private final ObjectMapper om = new ObjectMapper();

    public ChatWebSocketHandler(ChattingService service) {
        this.service = service;
    }

    private final Map<Integer, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();

        Map<String, String> params = Arrays.stream(query.split("&"))
                .map(kv -> kv.split("="))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        int roomNo = Integer.parseInt(params.get("roomNo"));

        rooms.computeIfAbsent(roomNo, k -> new CopyOnWriteArrayList<>()).add(session);

        // HISTORY
        List<MessageDto> history = service.getMessages(roomNo);

        ObjectNode out = om.createObjectNode();
        out.put("type", "HISTORY");

        // ⭐ ArrayNode 만들어서 하나씩 add() (에러 없는 방식)
        var arr = out.putArray("messages");
        for (MessageDto m : history) {
            ObjectNode n = om.createObjectNode();
            n.put("messageNo", m.getMessageNo());
            n.put("sendNo", m.getSendNo());
            n.put("chatMessage", m.getChatMessage());
            n.put("chatTime", m.getChatTime());
            arr.add(n);
        }

        session.sendMessage(new TextMessage(out.toString()));

        System.out.println("🔵 WebSocket connected | HISTORY ok | roomNo=" + roomNo);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectNode node = (ObjectNode) om.readTree(message.getPayload());
        String type = node.has("type") ? node.get("type").asText() : "chat";

        if ("chat".equals(type)) handleChatMessage(node);
    }

    private void handleChatMessage(ObjectNode node) throws Exception {
        int roomNo = node.get("roomNo").asInt();
        int userNo = node.get("userNo").asInt();
        String msg = node.get("message").asText();

        MessageDto dto = new MessageDto();
        dto.setChatListNo(roomNo);
        dto.setSendNo(userNo);
        dto.setChatMessage(msg);

        service.saveMessage(dto);

        ObjectNode out = om.createObjectNode();
        out.put("type", "chat");
        out.put("roomNo", roomNo);
        out.put("sendNo", userNo);
        out.put("messageNo", dto.getMessageNo());
        out.put("message", msg);
        out.put("time", dto.getChatTime());

        broadcast(roomNo, new TextMessage(out.toString()));
    }

    private void broadcast(int roomNo, TextMessage msg) {
        List<WebSocketSession> list = rooms.get(roomNo);
        if (list == null) return;

        for (WebSocketSession s : list) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(msg);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        rooms.values().forEach(list -> list.remove(session));
    }
}
