package web.model.dto.point;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointRecordDto {

    private int recordNo;      // PK: 기록 번호 (있으면)
    private int userNo;        // FK: 사용자 번호
    private int pointNo;       // FK: 포인트 정책 번호
    private int updatePoint;   // 실제 적립/차감된 포인트 값
    private String createdAt;  // 문자열로 받거나 LocalDateTime 등 (선택)
}
