package web.model.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class FriendsDto {
    private int frenNo;
    private int frenStatus;
    private String frenUpdate;
    private int offer;
    private int receiver;
}
