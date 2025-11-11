package web.model.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class MessageDto {
    private int messageNo;
    private String chatMessage;
    private String chatTime;
    private int chatListNo;
    private int sendNo;
}
