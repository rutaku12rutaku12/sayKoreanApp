package web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import web.model.entity.game.GameEntity;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Integer> {

    // [AGL-04]	게임 종류 추가(관리자단)	createGame()	게임 테이블을 추가한다.

    // [AGL-05]	게임 전체조회(관리자단)	getGame()	게임 테이블을 전체조회한다.

    // [AGL-06]	게임 상세조회(관리자단)	getDetailGame()	게임 테이블을 상세조회한다.

    // [AGL-07]	게임 삭제(관리자단)	deleteGame()	게임 테이블을 삭제한다.

}
