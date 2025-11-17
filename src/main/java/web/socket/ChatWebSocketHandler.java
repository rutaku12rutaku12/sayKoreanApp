package web.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
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
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var query = session.getUri().getQuery(); // roomNo=3&userNo=1
        var params = Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        int roomNo = Integer.parseInt(params.get("roomNo"));
        int userNo = Integer.parseInt(params.get("userNo"));

        session.getAttributes().put("roomNo", roomNo);
        session.getAttributes().put("userNo", userNo);

        rooms.computeIfAbsent(roomNo, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        int roomNo = (int) session.getAttributes().get("roomNo");
        int userNo = (int) session.getAttributes().get("userNo");

        String msg = om.readTree(message.getPayload()).get("content").asText();

        service.saveMessage(roomNo, userNo, msg);

        ObjectNode out = om.createObjectNode();
        out.put("sendNo", userNo);
        out.put("message", msg);
        out.put("time", LocalDateTime.now().toString());

        TextMessage sendMsg = new TextMessage(out.toString());

        for (WebSocketSession ws : rooms.get(roomNo)) {
            if (ws.isOpen()) ws.sendMessage(sendMsg);
        }
    }
}
