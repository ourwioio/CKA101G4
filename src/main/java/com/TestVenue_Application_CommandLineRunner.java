package com;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.repository.VenueImagesRepository;
import com.webond.venue.repository.VenueOrderRepository;
import com.webond.venue.repository.VenueRepository;
import com.webond.venue.repository.VenueSlotRepository;
import com.webond.venue.repository.VenueTypeRepository;
import com.webond.venue.service.VenueService;

import jakarta.transaction.Transactional;

@SpringBootApplication // 注意: 當使用 maven install 匯出 jar 檔時，整個系統只能保留一個 @SpringBootApplication 的設定
public class TestVenue_Application_CommandLineRunner implements CommandLineRunner {

	@Autowired
	VenueRepository repository;

	@Autowired
	VenueImagesRepository repository2;

	@Autowired
	VenueTypeRepository repository3;

	@Autowired
	VenueSlotRepository repository4;

	@Autowired
	VenueOrderRepository repository5;
	
//	@Autowired
//	MemberRepository repository6;

	@Autowired
	private VenueService vs;

	@Autowired
	private SessionFactory sessionFactory;

	public static void main(String[] args) {
		SpringApplication.run(TestVenue_Application_CommandLineRunner.class);
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {

		// 新增
//		VenueVO venueVO = new VenueVO();
//		VenueImagesVO imageVO = new VenueImagesVO();
//		byte[] imageBytes = Files.readAllBytes(Paths.get("src/main/resources/static/DB_photos1/7002.png"));
//		venueVO.setVenueId(2012);
//		venueVO.setMemberId(8);
//		venueVO.setVenueTypeId(1);
//		venueVO.setVenueName("測試場地");
//		
//		imageVO.setImages(imageBytes);
//		imageVO.setVenueVO(venueVO);
//		
//		Set<VenueImagesVO> imgSet = new HashSet<>();
//		imgSet.add(imageVO);
//		venueVO.setViVO(imgSet);
//		
//		repository.save(venueVO);

		// 刪除
//		repository.deleteById(2001);

		// 查全部
//		List<VenueVO> list = repository.findAll();
//		for (VenueVO vVO : list) {
//			System.out.print(vVO.getVenueId() + ",");
//			System.out.print(vVO.getMember().getMemberId() + ",");
//			System.out.print(vVO.getVenueTypeVO().getTypeName() + ",");
//			System.out.print(vVO.getVenueName() + ",");
//			System.out.print(vVO.getAddress() + ",");
//			System.out.print(vVO.getCapacity() + ",");
//			System.out.print(vVO.getHourlyRate() + ",");
//			System.out.print(vVO.getVenueStatus() + ",");
//			System.out.print(vVO.getCreatedAt() + ",");
//			System.out.print(vVO.getOpenDays() + ",");
//			System.out.print(vVO.getAvailableHours() + ",");
//			System.out.print(vVO.getRatingStars() + ",");
//			System.out.print(vVO.getRatingcount() + ",");
//			System.out.println();
//		}

		// 場地照片查全部
//		List<VenueImagesVO> list2 = repository2.findAll();
//		for (VenueImagesVO viVO : list2) {
//			System.out.print(viVO.getVenueVO().getVenueId() + ",");
//			System.out.print(viVO.getImagesId() + ",");
//			System.out.print(viVO.getImages() + ",");
//			System.out.println();
//
//		}

		// 場地類型 查全部
//		List<VenueTypeVO> list3 = repository3.findAll();
//		for (VenueTypeVO vtVO : list3) {
//			System.out.print(vtVO.getVenueTypeId());
//			System.out.print(vtVO.getTypeName());
//			System.out.print(vtVO.getTypeDesc());
//			System.out.print(vtVO.getTypeMode());
//			System.out.println();
//		}

		// 場地預約時段 查全部
//		List<VenueSlotVO> list4 = repository4.findAll();
//		for (VenueSlotVO vsVO : list4) {
//			System.out.print(vsVO.getVenueSlotId() + ",");
//			System.out.print(vsVO.getVenueVO().getVenueId() + ",");
//			System.out.print(vsVO.getSlotDate() + ",");
//			System.out.print(vsVO.getSlotStatus() + ",");
//			System.out.println();
//		}

		// 場地訂單 查全部
//		List<VenueOrderVO> list5 = repository5.findAll();
//		for (VenueOrderVO voVO : list5) {
//			System.out.print(voVO.getVenueOrderId() + ",");
//			System.out.print(voVO.getMember().getMemberId() + ",");
//			// 改用三元運算子：如果員工不是 null 就印出 ID，否則印出 "無負責員工"
//			System.out.print((voVO.getEmpVO() != null ? voVO.getEmpVO().getEmpId() : "無負責員工") + ",");
//			System.out.print(voVO.getVenueRating() + ",");
//			System.out.print(voVO.getVenueComment() + ",");
//			System.out.print(voVO.getPayoutAmount() + ",");
//			System.out.print(voVO.getRefundReason() + ",");
//			System.out.print(voVO.getRefundStatus() + ",");
//			System.out.print(voVO.getHandledAt() + ",");
//			System.out.print(voVO.getCreatedAt() + ",");
//			System.out.print(voVO.getStartAt() + ",");
//			System.out.print(voVO.getEndAt() + ",");
//			System.out.print(voVO.getTotalAmount() + ",");
//			System.out.print(voVO.getPaymentMethod() + ",");
//			System.out.println();
//		}

//		vs.deleteVenue(2009);
//		vs.getImagesByVenue(2001);

//		System.out.println("====== 1. 開始準備場地主表資料 ======");
//
//		// 🟢 建立場地基礎物件並塞入測試資料
//		VenueVO venueVO = new VenueVO();
//		venueVO.setMemberId(8); // 測試用的會員 ID
//		venueVO.setVenueName("大安活動中心 - 聯動新增測試");
//
//		// 🟢 處理外鍵 VENUE_TYPE_ID (場地類型)
//		// 🎯 注意：請先確認你資料庫 VENUE_TYPE 表裡面「有沒有 ID 為 1 的資料」，沒有的話請改成資料庫現有的 ID！
//		VenueTypeVO typeVO = new VenueTypeVO();
//		typeVO.setVenueTypeId(1);
//		venueVO.setVenueTypeVO(typeVO);
//
//		// 🟢 關鍵初始化：因為你的 VenueVO 類別裡沒有初始化 Set，不手動 new 會噴 NullPointerException
//		if (venueVO.getViVO() == null) {
//			venueVO.setViVO(new java.util.HashSet<>());
//		}
//
//		System.out.println("====== 2. 準備測試用的假照片資料 ======");
//
//		// 🟢 建立一個 List 容器來裝多張照片的 byte[]
//		java.util.List<byte[]> imageList = new java.util.ArrayList<>();
//
//		// 這裡用簡單的假 byte 陣列代替，避免因為檔案路徑不對而讓程式卡住
//		byte[] fakeImage1 = new byte[] { 0x01, 0x02, 0x03, 0x04 };
//		byte[] fakeImage2 = new byte[] { 0x05, 0x06, 0x07, 0x08 };
//		imageList.add(fakeImage1);
//		imageList.add(fakeImage2);
//
//		// 🟢 3. 呼叫 Service 執行聯動新增
//		try {
//			System.out.println("====== 3. 開始執行 Service 聯動新增 ======");
//
//			// 呼叫你在 Service 寫的方法
//			vs.addVenueWithImages(venueVO, imageList);
//
//			System.out.println("====== 4. 聯動新增測試成功！ ======");
//			System.out.println("資料庫自動生成的場地 ID 為: " + venueVO.getVenueId());
//
//		} catch (Exception e) {
//			System.err.println("❌ 測試失敗，請檢查下方錯誤訊息：");
//			e.printStackTrace();
//		}

//		Integer idToTest = 2001; // 🎯 確保這筆場地在你的資料庫真的存在
//
//		System.out.println("====== [EAGER 模式] 開始測試一條龍查詢 ======");
//
//		// 呼叫你原本最標準的 getOneVenue 即可
//		VenueVO venue = vs.getOneVenue(idToTest);
//
//		if (venue != null) {
//			System.out.println("【場地名稱】: " + venue.getVenueName());
//			System.out.println("----------------------------------------------");
//
//			// 🟢 1. 測試照片
//			System.out.println("📸 [照片] 數量: " + (venue.getVenueImages() != null ? venue.getVenueImages().size() : 0));
//			if (venue.getVenueImages() != null) {
//				for (VenueImagesVO img : venue.getVenueImages()) {
//					System.out.println("   -> 照片ID: " + img.getImagesId());
//				}
//			}
//
//			// 🟢 2. 測試時段
//			System.out.println("\n📅 [時段] 數量: " + (venue.getVenueSlots() != null ? venue.getVenueSlots().size() : 0));
//			if (venue.getVenueSlots() != null) {
//				for (VenueSlotVO slot : venue.getVenueSlots()) {
//					System.out.println("   -> 時段ID: " + slot.getVenueSlotId() + " | 日期: " + slot.getSlotDate());
//				}
//			}
//
//			// 🟢 3. 測試訂單 (加上防空 check，不怕 NULL 員工卡死)
//			System.out.println("\n💰 [訂單] 數量: " + (venue.getVenueOrders() != null ? venue.getVenueOrders().size() : 0));
//			if (venue.getVenueOrders() != null) {
//				for (VenueOrderVO order : venue.getVenueOrders()) {
//					String empIdStr = (order.getEmpVO() != null) ? String.valueOf(order.getEmpVO().getEmpId())
//							: "無(NULL)";
//					System.out.println("   -> 訂單ID: " + order.getVenueOrderId() + " | 負責員工ID: " + empIdStr + " | 金額: "
//							+ order.getTotalAmount());
//				}
//			}
//
//			System.out.println("\n====== 測試結束：EAGER 聯動查詢成功！ ======");
//		} else {
//			System.err.println("❌ 找不到 ID 為 " + idToTest + " 的場地。");
//		}
		
//		 MemberVO member = repository6.findById(8).orElse(null);
//		    
//		    if (member != null) {
//		        System.out.println("會員ID: " + member.getMemberId());
//		        System.out.println("場地總數: " + member.getVenues().size());
//		        
//		        for (VenueVO venue : member.getVenues()) {
//		            System.out.println("場地ID: " + venue.getVenueId() 
//		                + " | 場地名稱: " + venue.getVenueName());
//		        }
//		    }
	    
//		    List<VenueVO> venueList = repository.findByMember_MemberId(8);
//		    System.out.println("會員8 新增的場地數量: " + venueList.size());
//
//		    for (VenueVO venue : venueList) {
//		        System.out.println("場地ID: " + venue.getVenueId() + " | 場地名稱: " + venue.getVenueName());
//		    }
 
	}

}
