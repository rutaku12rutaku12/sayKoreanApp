package web.model.dto.test;

///  시험 모드 정의하는 enum
public enum TestMode {
    REGULAR("정기시험" , "그림/음성/주관식 각 1문제씩 (난수화)"),
    DAILY("일일시험" , "매일 다른 문제 3개 (난수화)"),
    INFINITE("무한모드" , "배운 내용 중 틀릴 때까지"),
    HARD("하드모드" , "배우지 않은 내용 포함 틀릴 때까지");

    private final String displayName;
    private final String description;

    TestMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
