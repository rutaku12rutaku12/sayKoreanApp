package web.model.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportMessageDto {

    private int reportNo;
    private int messageNo;
    private int reporterNo;
    private int reportedNo;
    private String reportReason;
    private int reportStatus;
    private String reportTime;
    private String snapshotMessage;
}
