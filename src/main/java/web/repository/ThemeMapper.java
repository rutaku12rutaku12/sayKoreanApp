package web.repository;

import org.apache.ibatis.annotations.*;
import web.model.dto.store.ThemeDto;

import java.util.List;

@Mapper
public interface ThemeMapper {

    // 1. 전체 테마 목록
    @Select("""
        SELECT 
            theme_id   AS themeId,
            theme_name AS themeName,
            price
        FROM theme
        """)
    List<ThemeDto> findAll();


    // 2. 유저가 가진 테마 목록
    @Select("""
        SELECT 
            t.theme_id   AS themeId,
            t.theme_name AS themeName,
            t.price
        FROM user_theme ut
        JOIN theme t ON ut.theme_id = t.theme_id
        WHERE ut.user_no = #{userNo}
        """)
    List<ThemeDto> findByUserNo(int userNo);


    // 3. 유저가 이 테마를 이미 가지고 있는지 체크
    @Select("""
        SELECT COUNT(*)
        FROM user_theme
        WHERE user_no = #{userNo}
          AND theme_id = #{themeId}
        """)
    int countUserTheme(
            @Param("userNo") int userNo,
            @Param("themeId") int themeId
    );


    // 4. 테마 구매(소유권 추가)
    @Insert("""
        INSERT INTO user_theme(user_no, theme_id, owned_at)
        VALUES(#{userNo}, #{themeId}, NOW())
        """)
    int insertUserTheme(
            @Param("userNo") int userNo,
            @Param("themeId") int themeId
    );
}
