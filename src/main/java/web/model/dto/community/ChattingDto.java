package web.model.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class ChattingDto {
    private int chatListNo;
    private String chatListTitle;
    private int chatListState;
    private int userNo;
}
