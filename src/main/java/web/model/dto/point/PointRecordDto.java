package web.model.dto.point;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointRecordDto { // class start
    /*
     * 포인트 적립/사용 기록
     *
     * 스키마에 영문명이 두 번 pointNo 로 되어 있어서
     * 첫 번째 컬럼은 pointRecordNo (포인트기록번호) 로 가정했어!
     */

    private int pointLogNo;   // PK: 포인트기록번호
    private String updateDate; // 포인트적립일시
    private int pointNo;         // FK: 포인트번호
    private int userNo;          // FK: 사용자번호

} // class end