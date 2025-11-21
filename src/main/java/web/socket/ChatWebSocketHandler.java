package web.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import web.model.dto.community.MessageDto;
import web.model.mapper.ChattingMapper;
import web.service.community.ChattingService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChattingService service;
    private final ChattingMapper chattingMapper; // 🔥 추가
    private final ObjectMapper om = new ObjectMapper();

    // 방 번호별 세션 목록
    private final Map<Integer, List<WebSocketSession>> rooms =
            new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery(); // roomNo=3&userNo=1

        if (query == null || !query.contains("roomNo") || !query.contains("userNo")) {
            System.out.println("❌ WebSocket 연결 실패: query null 또는 파라미터 없음");
            return;
        }

        Map<String, String> params = Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        int roomNo = Integer.parseInt(params.get("roomNo"));
        int userNo = Integer.parseInt(params.get("userNo"));

        session.getAttributes().put("roomNo", roomNo);
        session.getAttributes().put("userNo", userNo);

        rooms.computeIfAbsent(roomNo, k -> new CopyOnWriteArrayList<>()).add(session);

        System.out.println("🔗 WebSocket 연결됨 (room " + roomNo + ", user " + userNo + ")");

        List<MessageDto> history = service.getMessages(roomNo);

        ObjectNode historyPayload = om.createObjectNode();
        historyPayload.put("type", "HISTORY");
        historyPayload.put("roomNo", roomNo);
        historyPayload.set("messages", om.valueToTree(history));

        session.sendMessage(new TextMessage(historyPayload.toString()));

        System.out.println("📨 기존 메시지 " + history.size() + "개 전송 완료");
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        Integer roomNo = (Integer) session.getAttributes().get("roomNo");
        Integer userNo = (Integer) session.getAttributes().get("userNo");

        var root = om.readTree(message.getPayload());

        String msg = null;

        if (root.hasNonNull("message")) {
            msg = root.get("message").asText();
        } else if (root.hasNonNull("content")) {
            msg = root.get("content").asText();
        }

        if (msg == null || msg.isBlank()) return;

        // 🔥 roomNo로 chatListTitle → u1, u2 추출
        String title = chattingMapper.getChatListTitle(roomNo);
        if (title == null) {
            System.out.println("❌ chatListTitle 조회 실패! roomNo=" + roomNo);
            return;
        }

        String[] parts = title.split("_");
        int u1 = Integer.parseInt(parts[0]);
        int u2 = Integer.parseInt(parts[1]);

        // 현재 유저와 비교 → 상대 유저 찾기
        int otherUser = (u1 == userNo) ? u2 : u1;

        // 🔥 chatListNo 정확히 조회 (roomNo가 아님!)
        Integer chatListNo = chattingMapper.getChatListNoByUsers(u1, u2);

        if (chatListNo == null) {
            System.out.println("❌ chatListNo 조회 실패! user=" + u1 + ", " + u2);
            return;
        }

        // 메시지 DTO 저장
        MessageDto dto = new MessageDto();
        dto.setChatListNo(chatListNo);
        dto.setSendNo(userNo);
        dto.setChatMessage(msg);
        dto.setChatTime(LocalDateTime.now().toString());

        service.saveMessage(dto);
        service.updateChatListLastMessage(chatListNo, msg);

        System.out.println("💾 저장됨 → chatListNo=" + chatListNo + ", msg=" + msg);

        ObjectNode out = om.createObjectNode();
        out.put("type", "CHAT");
        out.put("messageNo", dto.getMessageNo());
        out.put("sendNo", userNo);
        out.put("message", msg);
        out.put("time", dto.getChatTime());

        TextMessage sendMsg = new TextMessage(out.toString());

        var sessions = rooms.get(roomNo);
        if (sessions == null) return;

        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) ws.sendMessage(sendMsg);
        }
    }
}
