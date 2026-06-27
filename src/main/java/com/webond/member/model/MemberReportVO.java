package com.webond.member.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

import com.webond.employee.model.EmployeeVO;

import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity 
@Table(name = "member_report") // 指定對應到資料庫的表名
public class MemberReportVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 設定主鍵自動遞增
    @Column(name = "report_id") 
    private Integer reportId;
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "REPORTER_ID", referencedColumnName = "MEMBER_ID", nullable = false)
    private MemberVO reporter; // 關聯的是整個物件，去除 Id
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "REPORTED_ID", referencedColumnName = "MEMBER_ID", nullable = false) 
    private MemberVO reported; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID") 
    private EmployeeVO employee; 
    
    @Column(name = "report_category")
    private Integer reportCategory;
    
    @Column(name = "report_content", columnDefinition = "TEXT") 
    private String reportContent;
    
  
    @Lob 
    @Column(name = "evidence", columnDefinition = "LONGBLOB") 
    private byte[] evidence; 
    
    @Column(name = "report_status")
    private Integer reportStatus;
    
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
    
    @Column(name = "created_at", insertable = false, updatable = false) 
    private Timestamp createdAt;
    
    @Column(name = "processed_at")
    private Timestamp processedAt;
    
    @Column(name = "violation_points", nullable = true)
    private Integer violationPoints;

    // ==========================================
    // 建構子 (Constructor)
    // ==========================================

    public MemberReportVO() {
        super();
    }

    public MemberReportVO(Integer reportId, MemberVO reporter, MemberVO reported, EmployeeVO employee,
            Integer reportCategory, String reportContent, byte[] evidence, Integer reportStatus,
            String adminNote, Timestamp createdAt, Timestamp processedAt, Integer violationPoints) {
        super();
        this.reportId = reportId;
        this.reporter = reporter;
        this.reported = reported;
        this.employee = employee;
        this.reportCategory = reportCategory;
        this.reportContent = reportContent;
        this.evidence = evidence; 
        this.reportStatus = reportStatus;
        this.adminNote = adminNote;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.violationPoints = violationPoints;
    }

    // ==========================================
    // 標準 Getter / Setter (名稱與屬性嚴格一致)
    // ==========================================

    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public MemberVO getReporter() { return reporter; }
    public void setReporter(MemberVO reporter) { this.reporter = reporter; }
    
    public MemberVO getReported() { return reported; }
    public void setReported(MemberVO reported) { this.reported = reported; }

    public EmployeeVO getEmployee() { return employee; }
    public void setEmployee(EmployeeVO employee) { this.employee = employee; }

    public Integer getReportCategory() { return reportCategory; }
    public void setReportCategory(Integer reportCategory) { this.reportCategory = reportCategory; }

    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }

    public byte[] getEvidence() { return evidence; }
    public void setEvidence(byte[] evidence) { this.evidence = evidence; }

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