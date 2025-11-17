package web.model.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class ChatRoomDto {
    private int roomNo;
    private String chatListTitle;
    private int friendNo;
    private String friendName;
    private String lastMessage;
    private String lastTime;
}
