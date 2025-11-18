package web.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import web.model.dto.community.MessageDto;
import web.service.community.ChattingService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChattingService service;
    private final ObjectMapper om = new ObjectMapper();

    private final Map<Integer, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        String query = session.getUri().getQuery(); //roomNo=3&userNo=1

        // 웹에서 query가 null 로 들어올 수 있음 -> 방어코드 추가
        if(query == null || !query.contains("roomNo") || !query.contains("userNo")) {
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

        //히스토리 전송
        List<MessageDto> history = service.getMessages(roomNo);

        for (MessageDto m : history){
            ObjectNode out = om.createObjectNode();
            out.put("sendNo", m.getSendNo());
            out.put("message", m.getChatMessage());
            out.put("time", m.getChatTime());
            out.put("type", "history");//히스토리 타입 구분

            session.sendMessage(new TextMessage(out.toString()));
        }
        System.out.println("📨 기존 메시지 " + history.size() + "개 전송 완료");
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        int roomNo = (int) session.getAttributes().get("roomNo");
        int userNo = (int) session.getAttributes().get("userNo");

        String msg = om.readTree(message.getPayload()).get("content").asText();

        service.saveMessage(roomNo, userNo, msg);//DB 저장

        ObjectNode out = om.createObjectNode();
        out.put("sendNo", userNo);
        out.put("message", msg);
        out.put("time", LocalDateTime.now().toString());
        out.put("type", "message"); // 새 메시지 구분

        TextMessage sendMsg = new TextMessage(out.toString());

        for (WebSocketSession ws : rooms.get(roomNo)) {
            if (ws.isOpen()) ws.sendMessage(sendMsg);
        }
    }
}
