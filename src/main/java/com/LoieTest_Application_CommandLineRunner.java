package com;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.webond.member.model.MemberReportVO;
import com.webond.member.repository.MemberReportRepository;

import jakarta.transaction.Transactional;

	@SpringBootApplication
	public class LoieTest_Application_CommandLineRunner implements CommandLineRunner {

	    @Autowired
	    private MemberReportRepository reportRepository; // 注入你剛寫好的檢舉 Repository

	    public static void main(String[] args) {
	        SpringApplication.run(LoieTest_Application_CommandLineRunner.class, args);
	    }

	    @Override
	    @Transactional
	    public void run(String... args) throws Exception {
	        System.out.println("==================================================");
	        System.out.println("   開始執行 [會員檢舉 MEMBER_REPORT] 測試連線");
	        System.out.println("==================================================");

	        try {
	            // 測試讀取
	            List<MemberReportVO> allReports = reportRepository.findAll();
	            System.out.println("🎉 連線成功！目前資料庫中的檢舉總筆數為: " + allReports.size());
	        } catch (Exception e) {
	            System.err.println("❌ 連線或欄位對應失敗，錯誤訊息如下：");
	            e.printStackTrace();
	        }

	        System.out.println("==================================================");
	    }
	}