package web.model.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import web.model.dto.point.PointRecordDto;


@Mapper
public interface PointMapper {

    @Insert("""
        INSERT INTO pointLog (pointNo, userNo)
        VALUES (#{pointNo}, #{userNo})
        """)
    int insertPointRecord(PointRecordDto dto);

}
