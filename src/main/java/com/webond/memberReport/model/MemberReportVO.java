package com.webond.memberReport.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "MEMBER_REPORT")
public class MemberReportVO implements java.io.Serializable  {

 	@Id
    @Column(name = "REPORT_ID")
    private Integer reportId;

}
