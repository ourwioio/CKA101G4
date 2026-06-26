package com.webond.member.model;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "member_report") //指定對應到資料庫的表名
public class MemberReportVO implements Serializable {

    @Id // 3. 宣告它是【主鍵 (Primary Key)】
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. 設定主鍵自動遞增 (Auto Increment)
    @Column(name = "report_id") // 5. 指定資料庫對應的欄位名稱
    private Integer reportId;
    
    
    @ManyToOne(fetch = FetchType.LAZY) // 1. 宣告多對一關係
    @JoinColumn(name = "REPORTER_ID", referencedColumnName = "MEMBER_ID", nullable = false) 
    // 2. name 寫你檢舉表的外來鍵欄位名，referencedColumnName 寫對方會員表的主鍵欄位名
    // 3. nullable = false 完美保留，代表這筆檢舉案一定要有一個合法的會員發起
    private MemberVO reporterId; // 🌟 4. 重點！型態直接換成對方的實體類物件 MemberVO！
    
    @Column(name = "reported_id", nullable = false)
    private Integer reportedId;
    
    @Column(name = "employee_id") // 沒寫 nullable 預設就是可為 null
    private Integer employeeId;
    
    @Column(name = "report_category")
    private Integer reportCategory;
    
    @Column(name = "report_content", columnDefinition = "TEXT") // TEXT 適合放詳細長描述
    private String reportContent;
    
    @Column(name = "evidence_path")
    private String evidencePath;
    
    @Column(name = "report_status")
    private Integer reportStatus;
    
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
    
    @Column(name = "created_at", insertable = false, updatable = false) 
    // insertable/updatable = false 可以交由資料庫在建立時自動生成預設時間（如 CURRENT_TIMESTAMP）
    private Timestamp createdAt;
    
    @Column(name = "processed_at")
    private Timestamp processedAt;
    
    @Column(name = "violation_points")
    private Integer violationPoints;

    // ==========================================
    // 🌟 下方的 建構子 與 Getter / Setter 完全保留即可！
    // ==========================================

    public MemberReportVO() {
        super();
    }

    public MemberReportVO(Integer reportId, MemberVO reporterId, Integer reportedId, Integer employeeId,
            Integer reportCategory, String reportContent, String evidencePath, Integer reportStatus,
            String adminNote, Timestamp createdAt, Timestamp processedAt, Integer violationPoints) {
        super();
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.reportedId = reportedId;
        this.employeeId = employeeId;
        this.reportCategory = reportCategory;
        this.reportContent = reportContent;
        this.evidencePath = evidencePath;
        this.reportStatus = reportStatus;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.violationPoints = violationPoints;
    }

    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public MemberVO getReporter() {return reporterId;}
    public void setReporter(MemberVO reporterId) {this.reporterId = reporterId;}
    
    public Integer getReportedId() { return reportedId; }
    public void setReportedId(Integer reportedId) { this.reportedId = reportedId; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public Integer getReportCategory() { return reportCategory; }
    public void setReportCategory(Integer reportCategory) { this.reportCategory = reportCategory; }

    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }

    public String getEvidencePath() { return evidencePath; }
    public void setEvidencePath(String evidencePath) { this.evidencePath = evidencePath; }

    public Integer getReportStatus() { return reportStatus; }
    public void setReportStatus(Integer reportStatus) { this.reportStatus = reportStatus; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getProcessedAt() { return processedAt; }
    public void setProcessedAt(Timestamp processedAt) { this.processedAt = processedAt; }

    public Integer getViolationPoints() { return violationPoints; }
    public void setViolationPoints(Integer violationPoints) { this.violationPoints = violationPoints; }
}

