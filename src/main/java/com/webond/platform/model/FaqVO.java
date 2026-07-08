package com.webond.platform.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "FAQ")
public class FaqVO implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id //@Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增 (AUTO_INCREMENT)
    @Column(name = "FAQ_ID", updatable = false)
    private Integer faqId;

    @Column(name = "FAQ_TYPE")
    @NotNull(message = "請選擇FAQ類型")
    private Byte faqType;

    @Column(name = "QUESTION")
    @NotEmpty(message = "問題：請勿空白")
    @Size(max = 500, message = "問題：長度不能超過 {max} 個字元")
    private String question;

    @Column(name = "ANSWER")
    @Size(max = 500, message = "答案：長度不能超過 {max} 個字元")
    private String answer;

    @Column(name = "STATUS")
    private Byte status = (byte) 0; // 預設草稿

    @Column(name = "EMPLOYEE_ID")
    @NotNull(message = "請選擇負責員工")
    private Integer employeeId;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public FaqVO() {
        super();
    }

    public Integer getFaqId() {
        return faqId;
    }

    public void setFaqId(Integer faqId) {
        this.faqId = faqId;
    }

    public Byte getFaqType() {
        return faqType;
    }

    public void setFaqType(Byte faqType) {
        this.faqType = faqType;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}