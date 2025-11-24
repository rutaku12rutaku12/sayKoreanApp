package web.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.point.PointRecordDto;
import web.model.mapper.PointMapper;
import web.repository.ThemeMapper;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final ThemeMapper themeMapper;
    private final PointMapper pointMapper;



    @Transactional
    public void buyTheme(int userNo, int themeId) {

        // 1. 이미 가진 테마인지 체크
        int count = themeMapper.countUserTheme(userNo, themeId);
        if (count > 0) {
            throw new IllegalStateException("이미 구매한 테마입니다.");
        }

        // 2. 포인트 차감 (예: pointNo = 6 이 '테마 구매' 정책이라고 가정)
        PointRecordDto record = new PointRecordDto();
        record.setUserNo(userNo);
        record.setPointNo(6);   // 테마 구매용 pointNo
        pointMapper.insertPointRecord(record);

        // 3. user_theme에 INSERT
        themeMapper.insertUserTheme(userNo, themeId);
    }
}