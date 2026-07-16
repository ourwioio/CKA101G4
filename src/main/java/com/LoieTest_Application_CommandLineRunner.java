package com;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberReportVO;
import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberReportRepository;
import java.util.Optional;
import jakarta.transaction.Transactional;

//@SpringBootApplication
public class LoieTest_Application_CommandLineRunner implements CommandLineRunner {

    @Autowired
    private MemberReportRepository reportRepository;
    
 // 🟢 確保有加上這一行，我們才能去 EMPLOYEE 表撈員工
    @Autowired
    private com.webond.employee.repository.EmployeeRepository employeeRepository;

    public static void main(String[] args) {
        SpringApplication.run(LoieTest_Application_CommandLineRunner.class, args);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
//   System.out.println("\n========== 🚀 [會員檢舉 MemberReportVO] 全功能密集測試開始 ==========");

//        // ====================================================================
//        // 🔍 【測試 1】查全部測試（驗證基礎連線與欄位對齊）
//        // ====================================================================
//        System.out.println("\n👉 [STEP 1] 正在測試 findAll()...");
//        List<MemberReportVO> listBefore = reportRepository.findAll();
//        System.out.println("🟢 目前資料庫已有筆數: " + listBefore.size());
//
//
//        // ====================================================================
//        // 💾 【測試 2】聯動新增測試（驗證 byte[] 圖片與外鍵關聯）
//        // ====================================================================
//        System.out.println("\n👉 [STEP 2] 開始測試新增一筆「待處理」的檢舉案...");
//        
//        MemberReportVO report = new MemberReportVO();
//        
//        // 🎯 關鍵：模擬關聯對象 (請確保你的 MEMBER 表裡真的有 ID 1 和 2 的會員)
//        MemberVO reporter = new MemberVO();
//        reporter.setMemberId(1); // 檢舉人 ID
//        report.setReporter(reporter);
//        
//        MemberVO reported = new MemberVO();
//        reported.setMemberId(2); // 被檢舉人 ID
//        report.setReported(reported);
//        
//        // 填寫基礎欄位
//        report.setReportCategory(1); // 假設 1 代表「言語辱罵」
//        report.setReportContent("這個人在社群推文下方使用不雅字眼人身攻擊，有截圖為證。");
//        report.setReportStatus(0);    // 0 代表「待處理」
//        //report.setCreatedAt(new Timestamp(System.currentTimeMillis())); // 填入當前時間
//        
//        // 測試 LONGBLOB 欄位：利用假 byte 陣列模擬圖片檔案
//        byte[] fakeScreenshot = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 }; 
//        report.setEvidence(fakeScreenshot);
//        
//        // 剛送出檢舉時，審核員工、違規分數、處理時間、備註皆為空 (NULL)
//        report.setEmployee(null);
//        report.setViolationPoints(null);
//        report.setAdminNote(null);
//        report.setProcessedAt(null);
//
//        Integer generatedId = null;
//        try {
//            MemberReportVO savedReport = reportRepository.save(report);
//            generatedId = savedReport.getReportId();
//            System.out.println("🟢 新增成功！自動生成的檢舉案 ID (PK) 為: " + generatedId);
//        } catch (Exception e) {
//            System.err.println("❌ 新增失敗！請檢查是否因為資料庫沒有 MEMBER ID 1 或 2 導致外鍵失敗。");
//            e.printStackTrace();
//            return; // 新增失敗就中斷後續測試
//        }
//
//
//     // ====================================================================
//        // 🔄 【測試 3】更新測試（模擬後台員工真正輸入 1003 進行審核）
//        // ====================================================================
//        System.out.println("\n👉 [STEP 3] 開始測試更新功能（人工模擬後台輸入員工 1003 審核）...");
//        
//        try {
//            // 撈出剛剛新增的那筆檢舉案
//            MemberReportVO reportToUpdate = reportRepository.findById(generatedId).orElse(null);
//            
//            if (reportToUpdate != null) {
//                System.out.println("📝 成功取出檢舉案 #" + generatedId + "，開始注入審核結果...");
//                
//                // 🟢 模擬前端/後台傳進來的員工資料：我們直接 new 一個外殼並塞入 1003
//                EmployeeVO admin = new EmployeeVO();
//                admin.setEmployeeId(1003); 
//                reportToUpdate.setEmployee(admin); // 直接塞入！(搭配 CascadeType.MERGE 資料庫會自動對接 1003)
//                
//                // 修改狀態欄位與處理細節
//                reportToUpdate.setReportStatus(1);    // 1 代表「審核通過，已處罰」
//                reportToUpdate.setViolationPoints(3); // 記違規 3 點
//                reportToUpdate.setAdminNote("經後台查證屬實，已對該被檢舉會員執行記點處分。");
//                reportToUpdate.setProcessedAt(new Timestamp(System.currentTimeMillis())); // 處置時間
//                
//                // 執行更新
//                reportRepository.save(reportToUpdate);
//                System.out.println("🟢 審核更新成功！");
//            }
//        } catch (Exception e) {
//            System.err.println("❌ 更新失敗！發生了預期之外的錯誤。");
//            e.printStackTrace();
//            return;
//        }
//
//        // ====================================================================
//        // 📝 【測試 4】再次查詢並列印（確認所有欄位寫入與讀出皆正常）
//        // ====================================================================
//        System.out.println("\n👉 [STEP 4] 再次從資料庫完整取出，驗證最終資料...");
//        
//        try {
//            MemberReportVO finalCheck = reportRepository.findById(generatedId).orElse(null);
//            if (finalCheck != null) {
//                System.out.println("--------------------------------------------------");
//                System.out.println("【檢舉案細節最終確認】");
//                System.out.println("案件主鍵 ID: " + finalCheck.getReportId());
//                System.out.println("檢舉人會員 ID: " + finalCheck.getReporter().getMemberId());
//                System.out.println("被檢舉會員 ID: " + finalCheck.getReported().getMemberId());
//                System.out.println("檢舉分類代碼: " + finalCheck.getReportCategory());
//                System.out.println("檢舉詳細內容: " + finalCheck.getReportContent());
//                System.out.println("照片證據大小: " + (finalCheck.getEvidence() != null ? finalCheck.getEvidence().length + " bytes" : "無照片"));
//                System.out.println("案件處理狀態: " + finalCheck.getReportStatus());
//                System.out.println("檢舉建立時間: " + finalCheck.getCreatedAt());
//                System.out.println("----------------------------------------------");
//                
//                // 🟢 這裡換回最安全的安全版列印，絕對不會引發 NullPointerException
//                System.out.println("負責審核員工 ID: " + (finalCheck.getEmployee() != null ? finalCheck.getEmployee().getEmployeeId() : "暫無審核員工"));
//                
//                System.out.println("判定違規點數: " + finalCheck.getViolationPoints());
//                System.out.println("管理員審核備註: " + finalCheck.getAdminNote());
//                System.out.println("案件結案時間: " + finalCheck.getProcessedAt());
//                System.out.println("--------------------------------------------------");
//                System.out.println("🟢 恭喜！所有欄位型態、時間戳記、圖片欄位、雙向/單向外鍵對接全部 100% 正常！");
//            }
//        } catch (Exception e) {
//            System.err.println("❌ 讀取最終資料失敗，可能某些欄位對應型態不匹配。");
//            e.printStackTrace();
//        }
//
//       
//        System.out.println("\n========== 🏁 [會員檢舉 MemberReportVO] 全功能密集測試結束 ==========");
    }
}