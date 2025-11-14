package web.model.mapper;

import org.apache.ibatis.annotations.*;
import web.model.dto.common.RankingDto;

import java.util.List;
import java.util.Map;

@Mapper
public interface RankingMapper {

    // [RK-01]	랭킹 생성	createRank()
    // 랭킹 테이블 레코드를 추가한다.
    // 매개변수 RankingDto
    // 반환 int (PK)
    // * 사용자가 시험을 본 후 로직을 받아 처리한다.
    // * 추가 : 제미나이 정확도 채점 로직 API 활용하여 isCorrect 측정
    // 유진님이 함 패스 ㅅㄱ

    // [RK-02]	랭킹 삭제 (사용자 탈퇴 시)	deleteRankByUser()
    // 랭킹 테이블 레코드를 삭제한다.
    // 매개변수 int
    // 반환 int
    // * 사용자가 탈퇴했을 경우에 사용하는 로직
    // 사용자 탈퇴해도 update로 데이터를 보관하는데, 랭킹은 삭제가 됨. 차라리 유저스테이트를 확인해서, 상태가 -1이면 데이터를 보내지 않는 것으로 하기
    @Delete("delete from ranking where userNo = #{userNo}")
    int deleteRankByUser(int userNo);

    // [RK-03] 랭킹 분야별조회 	getRank()
    // 랭킹 테이블 레코드를 조회한다.
    // 사용자닉네임(userNo FK)과 시험명(examNo FK), 시험문항명(examNo FK)도 함께 조회.
    // 랭킹 로직

    // 1) 정답왕 : 정답률이 높은 순
    // (isCorrect의 인트값 합산이 가장 높은 사람)
    @Select("select " +
            " u.nickName , " +
            " u.userNo, " +
            " sum(case when r.isCorrect = 1 then 1 else 0 end) as score, " +
            " count(*) as total, " +
            " round(sum(case when r.isCorrect = 1 then 1 else 0 end) * 100.0 / count(*), 2) as accuracy " +
            " from ranking r " +
            " join users u on r.userNo = u.userNo " +
            " group by u.userNo , u.nickName " +
            " having count(*) >= 3 " + // 최소 3문제 이상 푼 사람만
            " order by accuracy desc , score desc " + // accuracy: 맞힌 문제 비율(%)
            " limit 10")
    List<Map<String , Object>> getAccuracyRank();

    // 2) 도전왕 : 가장 많이 문제를 푼 순서
    // (isCorrect의 레코드 합산이 가장 높은 사람)
    @Select("select " +
            " u.nickName ," +
            " u.userNo , " +
            " count(*) as total , " +
            " sum(case when r.isCorrect = 1 then 1 else 0 end) as score " +
            " from ranking r " +
            " join users u on r.userNo = u.userNo " +
            " group by u.userNo , u.nickName " +
            " order by total desc , score desc " + // total: 총 푼 문제 수 , score: 맞힌 문제 수
            " limit 10")
    List<Map<String , Object>> getChallengeRank();

    // 3) 끈기왕 : 같은 문제에 여러번 도전한 순
    // (testRound의 평균값이 가장 높은 사람)
    // 매개변수 int
    // 반환 List<RankingDto>
    @Select("select " +
            " u.nickName , " +
            " u.userNo , " +
            " avg(r.testRound) as avgRound , " +    // avgRound: 문제당 평균 시도 횟수
            " count(distinct r.testItemNo) as uniqueItems , " + // uniqueItems: 중복 제외한 문제 개수
            " count(*) as totalAttempts " + // totalAttempts: 전체 시도 수
            " from ranking r " +
            " join users u on r.userNo = u.userNo " +
            " group by u.userNo , u.nickName " +
            " having count(*) >= 3 " +  // 최소 3문제 이상 푼 사람만
            " order by avgRound desc " +
            " limit 10")
    List<Map<String, Object>> getPersistenceRank();

    // ============================================
    // 신규 쿼리 (게임왕, 출석왕, 포인트왕)
    // ============================================

    // 4) 게임왕 : 게임 최고 점수 순
    @Select("select " +
            " u.nickName , " +
            " u.userNo , " +
            " max(g.gameScore) as maxScore , " +
            " count(*) as totalGames " +
            " sum(case when g.gameResult >= 1 then 1 else 0 end) as successCount " +
            " from gameLog g " +
            " join users u on g.userNo = u.userNo " +
            " where u.userState = 1 " +
            " group by u.userNo , u.nickName " +
            " order by maxScore desc , successCount desc " +
            " limit 10")
    List<Map<String, Object>> getGameRank();

    // 5) 출석왕 : 출석 횟수가 가장 많은 순
    @Select("select " +
            " u.nickName , " +
            " u.userNo , " +
            " count(*) as attendCount , " +
            " max(a.attenDate) as lastAttendDate " +
            " from attendance a " +
            " join users u on a.userNo = u.userNo " +
            " where u.userState = 1 " +
            " group by u.userNo , u.nickName " +
            " order by totalPoint desc , pointLogCount desc " +
            " limit 10")
    List<Map<String, Object>> getAttendanceRank();

    // 6) 포인트왕 : 포인트 총합이 높은 순
    @Select("select " +
            " u.nickName , " +
            " u.userNo , " +
            " sum(pp.updatePoint) as totalPoint , " +
            " count(*) as pointLogCount " +
            " from pointLog pl " +
            " join pointPolicy pp on pl.pointNo = pp.pointNo " +
            " join users u on pl.userNo = u.userNo " +
            " where u.userState = 1 " +
            " group by u.userNo , u.nickName " +
            " order by totalPoint desc , pointLogCount desc " +
            " limit 10")
    List<Map<String , Object>> getPointRank();

    // ============================================
    // 내 순위 조회 쿼리
    // ============================================

    //  ROW_NUMBER(): 각 행에 고유한 순차 번호를 부여합니다.
    //  OVER (...): ROW_NUMBER() 함수가 적용될 범위를 지정합니다.

    // 정답왕 - 내 순위
    @Select("select " +
            " ranked.* " +
            " from (" +
            "   select " +
            "     u.nickName ," +
            "     u.userNo , " +
            "     sum(case when r.isCorrect = 1 then 1 else 0 end) as score, " +
            "     count(*) as total, " +
            "     round(sum(case when r.isCorrect = 1 then 1 else 0 end) * 100.0 / count(*), 2) as accuracy, " +
            "     row_number() over (order by round(sum(case when r.isCorrect = 1 then 1 else 0 end) * 100.0 / count(*), 2) desc, sum(case when r.isCorrect = 1 then 1 else 0 end) desc) as ranking " +
            "   from ranking r " +
            "   join users u on r.userNo = u.userNo " +
            "   where u.userState = 1 " +
            "   group by u.userNo , u.nickName " +
            "   having count(*) >= 3 " +
            " ) ranked " +
            " where ranked.userNo = #{userNo}")
    Map<String , Object> getMyAccuracyRank(@Param("userNo") int userNo);

    // 도전왕 - 내 순위
    @Select("select " +
            " ranked.* " +
            " from (" +
            "  select " +
            "    u.nickName , " +
            "    u.userNo , " +
            "    count(*) as total , " +
            "    sum(case when r.isCorrect = 1 then 1 else 0 end) as score, " +
            "    row_number() over (order by count(*) desc, sum(case when r.isCorrect = 1 then 1 else 0 end) desc) as ranking " +
            "  from ranking r " +
            "  join users u on r.userNo = u.userNo " +
            "  where u.userState = 1 " +
            "  group by u.userNo , u.nickName " +
            " ) ranked " +
            " where ranked.userNo = #{userNo}")
    Map<String, Object> getMyChallengeRank(@Param("userNo") int userNo);

    // 끈기왕 - 내 순위
    @Select("select " +
            " ranked.* " +
            " from (" +
            "  select " +
            "    u.nickName , " +
            "    u.userNo , " +
            "    avg(r.testRound) as avgRound , " +
            "    count(distinct r.testItemNo) as uniqueItems , " +
            "    count(*) as totalAttempts, " +
            "    row_number() over (order by avg(r.testRound) desc) as ranking " +
            "  from ranking r " +
            "  join users u on r.userNo = u.userNo " +
            "  where u.userState = 1 " +
            "  group by u.userNo , u.nickName " +
            "  having count(*) >= 3 " +
            " ) ranked " +
            " where ranked.userNo = #{userNo}")
    Map<String, Object> getMyPersistenceRank(@Param("userNo") int userNo);

    // 게임왕 - 내 순위
    @Select("select " +
            " ranked.*" +
            " from ( " +
            "  select " +
            "     u.nickName , " +
            "     u.userNo , " +
            "     max(g.gameScore) as maxScore , " +
            "     count(*) as totalGames , " +
            "     sum(case when g.gameResult >= 1 then 1 else 0 end) as successCount, " +
            "     row_number() over (order by max(g.gameScore) desc, sum(case when g.gameResult >= 1 then 1 else 0 end) desc) as ranking " +
            "  from gamelog g " +
            "  join users u on g.userNo = u.userNo " +
            "  where u.userState = 1 " +
            "  group by u.userNo , u.nickName " +
            " ) ranked " +
            " where ranked.userNo = #{userNo}")
    Map<String , Object> getMyGameRank(@Param("userNo") int userNo);

    // 출석왕 - 내 순위
    @Select("select " +
            " ranked.* " +
            " from ( " +
            "  select " +
            "    u.nickName , " +
            "    u.userNo , " +
            "    count(*) as attendCount , " +
            "    max(a.attenDate) as lastAttendDate , " +
            "    row_number() over (order by count(*) desc , max(a.attenDate) desc) as ranking " +
            "  from attendance a " +
            "  join users u on a.userNo = u.userNo " +
            "  where u.userState = 1 " +
            "  group by u.userNo , u.nickName " +
            " ) ranked " +
            " where ranked.userNo = #{userNo}")
    Map<String , Object> getMyAttendanceRank(@Param("userNo") int userNo);

    // 포인트왕 - 내 순위
    @Select("select " +
            " ranked.* " +
            " from ( " +
            "  select " +
            "     u.nickName , " +
            "     u.userNo , " +
            "     sum(pp.updatePoint) as totalPoint , " +
            "     count(*) as pointLogCount , " +
            "     row_number() over (order by sum(pp.updatePoint) desc, count(*) desc) as ranking " +
            "  from pointLog pl " +
            "  join pointPolicy pp on pl.pointNo = pp.pointNo " +
            "  join users u on pl.userNo = u.userNo " +
            "  where u.userState = 1 " +
            "  group by u.userNo , u.nickName " +
            " ) ranked " +
            " where ranked.userNo = #{userNo}")
    Map<String , Object> getMyPointRank(@Param("userNo") int userNo);


    // [RK-04]	랭킹 검색조회	searchRank() (안할거)
    // 랭킹 테이블 레코드를 검색조회한다.
    // 사용자닉네임(userNo FK)과 시험명(examNo FK), 시험문항명(examNo FK)도 함께 조회.
    // 서브쿼리 활용
    // 매개변수 int
    // 반환 RankingDto

    // 1. 닉네임 사용자 검색

    // 2. 한국어

    // [*] 기존 메소드(userNo, testItemNo 만 정의됨)

    // 1) 사용자(userNo 조인)
    @Select("select " +
            " r.rankNo , " +
            " r.testRound , " +
            " r.userAnswer , " +
            " r.isCorrect , " +
            " r.resultDate , " +
            " r.testItemNo , " +
            " r.userNo , " +
            " u.nickName , " +
            " ti.question , " +
            " t.testTitle " +
            " from ranking r " +
            " join users u on r.userNo = u.userNo " +
            " join testItem ti on r.testItemNo = ti.testItemNo " +
            " join test t on ti.testNo = t.testNo " +
            " where r.userNo = #{userNo} " +
            " order by r.resultDate desc")
    List<RankingDto> searchRankByUser(int userNo);

    // 2) 시험문항별(testItemNo 조인)
    @Select("select " +
            " r.rankNo , " +
            " r.testRound , " +
            " r.userAnswer , " +
            " r.isCorrect , " +
            " r.resultDate , " +
            " r.testItemNo , " +
            " r.userNo , " +
            " u.nickName , " +
            " ti.question " +
            " from ranking r " +
            " join users u on r.userNo = u.userNo " +
            " join testItem ti on r.testItemNo = ti.testItemNo " +
            " where r.testItemNo = #{testItemNo} " +
            " order by r.isCorrect desc , r.resultDate asc")
    List<RankingDto> searchRankByTestItem(int testItemNo);

    // 3) 사용자, 시험문항 복합 검색
    @Select("select " +
            " r.rankNo ," +
            " r.testRound ," +
            " r.userAnswer ," +
            " r.isCorrect , " +
            " r.resultDate , " +
            " r.testItemNo , " +
            " r.userNo " +
            " from ranking r " +
            " where r.userNo = #{userNo} and r.testItemNo = #{testItemNo} " +
            " order by r.resultDate desc")
    List<RankingDto> searchRankByUserAndItem(@Param("userNo") int userNo, @Param("testItemNo") int testItemNo);

}
