package web.model.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import web.model.dto.point.PointRecordDto;


@Mapper
public interface PointMapper {

    // 포인트 적립/차감 기록 저장
    @Insert("""
        INSERT INTO pointLog (pointNo, userNo)
        VALUES (#{pointNo}, #{userNo})
        """)
    int insertPointRecord(PointRecordDto dto);

    // 내 포인트 총합 조회
    @Select("""
        SELECT COALESCE(SUM(pointNo), 0)
        FROM pointLog
        WHERE userNo = #{userNo}
        """)
    int getTotalPoint(int userNo);

}
