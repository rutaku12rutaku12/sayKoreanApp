package web.util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 비밀키 알고리즘 : HS256알고리즘 , HS512알고리즘
    // private String secretKey = "인코딩된 HS512 비트 키";
    // (1) 개발자 임의로 지정한 키 : private String secreteKey = "2C68318E352971113645CBC72861E1EC23F48D5BAA5F9B405FED9DDDCA893EB4";
    // (2) 라이브러리 이용한 임의(랜덤) 키 :
        // import java.security.Key;
        // Keys.secretKeyFor( SignatureAlgorithm.알고리즘명 );
    private Key secretKey = Keys.secretKeyFor( SignatureAlgorithm.HS256 );

    // [1] JWT 토큰 발급, 사용자의 이메일을 받아서 토큰 만들기
    public String createToken( String email , int userNo){
        // return Jwts.builder()
        String token = Jwts.builder() // * 해당 반환된 토큰을 변수에 저장
                .setSubject( email ) // 토큰에 넣을 내용물 , 로그인 성공한 회원의 이메일을 넣는다.
                .claim("userNo", userNo)
                .setIssuedAt( new Date() ) // 토큰이 발급된 날짜 , new Date() : 자바에서 제공하는 현재날짜 클래스
                // 토큰 만료시간 , 밀리초(1000/1) , new Date( System.currentTimeMillis() ) : 현재시간의 밀리초
                // new Date( System.currentTimeMillis() + ( 1000 * 초 * 분 * 시 ) )
                .setExpiration( new Date( System.currentTimeMillis() + ( 1000 * 60 * 60 * 24 ) ) ) // 1일의 토큰 유지기간
                .signWith( secretKey ) // 지정한 비밀키로 암호화 한다.
                .compact(); // 위 정보로 JWT 토큰 생성하고 반환한다.

        System.out.println("토큰 생성 완료 : email : "+ email + ", userNo : "+ userNo);
        return token;
    } // f end

    // [2] JWT 토큰 검증 및 이메일 추출
    public String validateToken( String token ){
        try{
            Claims claims = Jwts.parser() // 1. parser() : JWT토큰 검증하기 위한함수
                    .setSigningKey( secretKey ) // 2. .setSigningKey( 비밀키 ) : 검증에 필요한 비밀키 지정.
                    .build() // 3. 검증을 실행할 객체 생성,
                    .parseClaimsJws( token ) // 4. 검증에 사용할 토큰 지정
                    .getBody(); // 5. 검증된 (claims) 객체 생성후 반환
            // claims 안에는 다양한 토큰 정보 들어있다.
            System.out.println("토큰 검증 성공 - email: " +claims.getSubject() ); // 토큰에 저장된 (로그인된)회원이메일
            return claims.getSubject(); // 이메일 반환

        }catch ( ExpiredJwtException e){
            System.out.println(" >> JWT 토큰 기한 만료 : "+ e.getMessage());
        } catch (Exception e){
            System.out.println(" >> JWT 토큰 검증 실패 : "+ e.getMessage());
        }
        return null; // 유효하지 않은 토큰 또는 오류 발생시 null 반환
    } // f end

    // [3] 토큰에서 userNo 추출
    public Integer getUserNo(String token) {
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Integer userNo = claims.get("userNo", Integer.class);
            System.out.println("토큰에서 userNo 추출: "+ userNo);
            return userNo;

        }catch ( ExpiredJwtException e){
            System.out.println(" >> JWT 토큰 기한 만료 : "+ e.getMessage());
        } catch (Exception e){
            System.out.println(" >> JWT 토큰 검증 실패 : "+ e.getMessage());
        }
        return null; // 유효하지 않은 토큰 또는 오류 발생시 null 반환
    } // f end

    // [4] 토큰 유효성 검사 (boolean 반환)
    public boolean isTokenValid(String token){
        try{
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e){
            System.out.println(" >> JWT 토큰 기한 만료");
            return false;
        }catch (Exception e){
            System.out.println(" >> JWT 토큰 유효성 검증 실패");
            return false;
        }
    }
    // [5] 토큰에서 이메일 추출
    public String getEmail(String token){
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            System.out.println(" >> 토큰에서 이메일 추출 실패: "+ e.getMessage());
            return null;
        }
    }

} // class end
