package web.service.admin;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import web.model.dto.study.ExamDto;
import web.model.dto.study.GenreDto;
import web.model.dto.study.StudyDto;
import web.model.mapper.admin.AdminStudyMapper;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExcelService {

    // [*] DI
    private final AdminStudyMapper adminStudyMapper;

    // [1] 모든 예문을 언어별 시트로 구성된 엑셀 파일로 생성
    public byte[] generateExamExcel() {
        log.info("예문 엑셀 생성 시작");

        try {
            // 1. 모든 데이터 조회
            List<GenreDto> genres = adminStudyMapper.getGenre();
            List<StudyDto> studies = adminStudyMapper.getStudy();
            List<ExamDto> exams = adminStudyMapper.getExam();

            // 2. 워크북 생성
            Workbook workbook = new XSSFWorkbook();

            // 3. 각 언어별 시트 생성
            createLanguageSheet(workbook, exams, studies, genres, "한국어-일본어", "Ko", "Jp");
            createLanguageSheet(workbook, exams, studies, genres, "한국어-중국어", "Ko", "Cn");
            createLanguageSheet(workbook, exams, studies, genres, "한국어-영어", "Ko", "En");
            createLanguageSheet(workbook, exams, studies, genres, "한국어-스페인어", "Ko", "Es");
            createLanguageSheet(workbook, exams, studies, genres, "발음기호", "Ko", "Roman");

            // 4. 바이트 배열로 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("예문 엑셀 생성 완료 - 총 {}개 예문" , exams.size());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("예문 엑셀 생성 실패: " + e.getMessage());
            return null;
        }

    }
    
    // [2] 언어별 시트 생성
    private void createLanguageSheet(Workbook workbook ,
                                     List<ExamDto> exams,
                                     List<StudyDto> studies,
                                     List<GenreDto> genres,
                                     String sheetName, String lang1, String lang2) {

        Sheet sheet = workbook.createSheet(sheetName);

        // 스타일 생성
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        // 헤더 행 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {"번호" , "장르" , "주제" , "한국어" , getLanguageName(lang2) , "수정한 예문"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);

        }

        // 데이터 행 생성
        int rowNum = 1;
        for (ExamDto exam : exams) {
            Row row = sheet.createRow(rowNum++);

            // 장르명 찾기
            StudyDto study = studies.stream()
                    .filter(s -> s.getStudyNo() == exam.getStudyNo())
                    .findFirst()
                    .orElse(null);

            String genreName = "";
            String themeKo = "";
            if (study != null) {
                themeKo = study.getThemeKo();
                GenreDto genre = genres.stream()
                        .filter(g -> g.getGenreNo() == study.getGenreNo())
                        .findFirst()
                        .orElse(null);
                if (genre != null) {
                    genreName = genre.getGenreName();
                }

            }

            // 셀 생성
            createCell(row, 0 , exam.getExamNo() , dataStyle);
            createCell(row, 1 , genreName , dataStyle);
            createCell(row, 2 , themeKo , dataStyle);
            createCell(row, 3 , exam.getExamKo() , dataStyle);
            createCell(row, 4, getExamValue(exam, lang2) , dataStyle); // 다국어 데이터셀
            createCell(row, 5, "수정 내용 없음 (※ 지우고 수정해주세요. )", dataStyle);
        }

        // 컬럼 너비 자동 조절
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            sheet.setColumnWidth(5, 60 * 256);
        }

    }
    
    // [3] 헤더 스타일
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 배경색
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 테두리
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 정렬
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 폰트
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        return style;
    }
    
    // [4] 데이터 스타일
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 테두리
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 정렬
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true); // 텍스트 줄바꿈

        return style;
    }
    
    // [5] 셀 생성 헬퍼
    private void createCell(Row row , int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);

        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }

        cell.setCellStyle(style);
    }

    // [6] 언어 코드에 따른 예문 값 반환
    private String getExamValue(ExamDto exam, String lang) {
        return switch (lang) {
            case "Ko" -> exam.getExamKo();
            case "Roman" -> exam.getExamRoman();
            case "Jp" -> exam.getExamJp();
            case "Cn" -> exam.getExamCn();
            case "En" -> exam.getExamEn();
            case "Es" -> exam.getExamEs();
            default -> "";
        };
    }


    // [7] 언어 코드 한글명으로 변환
    private String getLanguageName(String lang){
        return switch (lang) {
            case "Ko" -> "한국어";
            case "Roman" -> "발음기호";
            case "Jp" -> "일본어";
            case "Cn" -> "중국어";
            case "En" -> "영어";
            case "Es" -> "스페인어";
            default -> "";
        };
    }
    
}
