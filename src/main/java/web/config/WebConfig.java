package web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // application.properties에서 파일 업로드 경로 주입
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 경로 정규화 (역슬래시 → 슬래시)
        String normalizedPath = uploadPath.replace("\\" , "/");

        // ✅ 끝에 슬래시가 없으면 추가
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        // ✅ file:/// 프리픽스 추가 (Windows/Linux 모두 호환)
        // 'file:///' 프리픽스는 Windows 절대 경로를 위해 슬래시 3개 사용
        String resourceLocation = "file:///" + normalizedPath;

        // ✅ ResourceHandler 등록 (URL 경로가 '/upload/**' 패턴으로 시작하는 모든 요청에 대해)
        // 실제 파일 시스템의 'uploadPath' 경로에서 파일을 찾아 제공함
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(resourceLocation);

        // ✅ 디렉토리 존재 확인 및 생성
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                log.info("📁 업로드 디렉토리 생성됨: {}", uploadPath);
            } else {
                log.warn("⚠️ 업로드 디렉토리 생성 실패: {}", uploadPath);
            }
        }

        // ✅ 디버깅 로그 (개발 단계)
        log.info("=== WebConfig 설정 완료 ===");
        log.info("📂 Upload Path: {}", uploadPath);
        log.info("🔗 Resource Location: {}", resourceLocation);
        log.info("✅ URL Pattern: /upload/**");
        log.info("📝 예시: http://localhost:8080/upload/image/nov_25/1_img.jpg");

        // ✅ 디렉토리 읽기/쓰기 권한 확인
        if (uploadDir.exists()) {
            log.info("📊 디렉토리 상태:");
            log.info("   - 읽기 권한: {}", uploadDir.canRead());
            log.info("   - 쓰기 권한: {}", uploadDir.canWrite());
            log.info("   - 실행 권한: {}", uploadDir.canExecute());
        }
    }
}
