package web.model.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TtsRequestDto {
    private String text;           // TTS로 변환할 텍스트
    private String languageCode;   // Google TTS 언어 코드 (예: ko-KR, ja-JP, zh-CN, en-US, es-ES)
    private int examNo;            // 연결될 예문 번호
    private int lang;              // 언어 코드 (1=한국어, 2=일본어, 3=중국어, 4=영어, 5=스페인어)
}
