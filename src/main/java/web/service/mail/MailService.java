package web.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class MailService {

    private final JavaMailSender mailSender;


    // 인증코드 이메일 발송
    public void sendPass(String toEmail,String tempPw){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("saykorean1324@naver.com", "saykorean");
            helper.setTo(toEmail);
            helper.setSubject("[sayKorean] 임시 비밀번호 안내");
            helper.setText(
                        "<h1>임시 비밀번호가 발급되었습니다.</h1><br>"+
                                "<div>임시 비밀번호 : <span style='color:#2d6cdf; font-size:2em; font-weight:bold;'>" + tempPw + "</span></div><br>"+
                            "<div>로그인 후 즉시 비밀번호를 변경해주세요.</div>"+
                            "<img src='cid:adminImage' style='width:300px; height:300px;' />" ,

                    true
            );
            // 이미지 첨부
            FileSystemResource res = new FileSystemResource(new File("src/main/saykorean/public/img/adminPage.png"));
            helper.addInline("adminImage", res);

            mailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("메일 전송 실패: "+e.getMessage());
        }
    }
}
