package web.service.community;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.community.MessageDto;
import web.model.dto.community.ReportMessageDto;
import web.model.mapper.ChattingMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ChattingService {

    private final ChattingMapper chattingMapper;

    /**
     * 친구 수락 시 1:1 채팅방 생성 또는 기존 방 번호 반환
     */
    public int ensureRoom(int u1, int u2) {
        // 방 없으면 생성
        if (chattingMapper.checkRoom(u1, u2) == 0) {
            chattingMapper.createRoom(u1, u2);
        }

        Integer roomNo = chattingMapper.getRoomNo(u1, u2);
        if (roomNo == null) {
            throw new IllegalStateException("채팅방 생성/조회 실패 (u1=" + u1 + ", u2=" + u2 + ")");
        }
        return roomNo;
    }

    /**
     * 채팅방 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(int roomNo) {
        return chattingMapper.selectMessages(roomNo);
    }

    /**
     * 메시지 저장
     */
    public void saveMessage(MessageDto dto) {
        int rows = chattingMapper.insertMessage(dto);
        if (rows != 1) {
            throw new IllegalStateException("메시지 저장 실패");
        }
    }

    /**
     * 내 채팅방 목록
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyRooms(int userNo) {
        return chattingMapper.getMyRooms(userNo);
    }

    /**
     * 친구 삭제(frenStatus = -1) 시 채팅방 삭제용
     */
    public void deleteRoomsByUsers(int u1, int u2) {
        chattingMapper.deleteRoomsByUsers(u1, u2);
    }

    /**
     * 메시지 신고
     * - messageNo로 원본 메시지 찾기
     * - sendNo를 신고당한 사람(reportedNo)으로 사용
     * - chatMessage를 snapshot으로 저장
     */
    public void reportMessage(int messageNo, int reporterNo, String reason) {
        // 1) 중복 신고 방지
        int count = chattingMapper.countReportByUser(messageNo, reporterNo);
        if (count > 0) {
            throw new IllegalStateException("이미 신고한 메시지입니다.");
        }

        // 2) 메시지 조회
        MessageDto msg = chattingMapper.findMessageByNo(messageNo);
        if (msg == null) {
            throw new IllegalArgumentException("해당 메시지를 찾을 수 없습니다. messageNo=" + messageNo);
        }

        int reportedNo = msg.getSendNo();  // 메시지 보낸 사람 = 신고 당한 사람

        // 3) DTO 만들어서 저장
        ReportMessageDto dto = new ReportMessageDto();
        dto.setMessageNo(messageNo);
        dto.setReporterNo(reporterNo);
        dto.setReportedNo(reportedNo);
        dto.setReportReason(reason);
        dto.setSnapshotMessage(msg.getChatMessage());

        chattingMapper.insertReportMessage(dto);
    }



}
