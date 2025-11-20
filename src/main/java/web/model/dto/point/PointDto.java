package web.model.dto.point;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointDto {
    /*
     * 포인트 정책 마스터
     *  - 예) 회원가입포인트 300, 출석포인트 10, 시험완수 100 ...
     */

    private int pointNo;      // PK: 포인트번호
    private String pointName; // 포인트명 (예: 회원가입포인트, 출석포인트 등)
    private int updatePoint;  // 지급포인트 (예: 300, 10, 50, 100, 5, -1000)
}
