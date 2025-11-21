package web.model.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import web.model.dto.point.PointDto;
import web.model.dto.point.PointRecordDto;

@Mapper
public interface PointMapper {

    // 1. 포인트 적립/차감 기록 저장 (로그)
    @Insert("""
        INSERT INTO pointLog (pointNo, userNo)
        VALUES (#{pointNo}, #{userNo})
        """)
    int insertPointRecord(PointRecordDto dto);

    // 2. 포인트 정책 1건 조회 (PointDto 사용)
    @Select("""
        SELECT pointNo,
               pointName,
               updatePoint
        FROM pointPolicy
        WHERE pointNo = #{pointNo}
        """)
    PointDto findByPointNo(int pointNo);

    // 3. 사용자 총 포인트 (pointLog + pointPolicy 조인해서 updatePoint 합산)
    @Select("""
        SELECT COALESCE(SUM(p.updatePoint), 0)
        FROM pointLog pl
        JOIN pointPolicy p ON pl.pointNo = p.pointNo
        WHERE pl.userNo = #{userNo}
        """)
    int getTotalPoint(int userNo);
}
