package com;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.webond.member.model.MemberVO;
import com.webond.venue.model.VenueTypeVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueImagesRepository;
import com.webond.venue.repository.VenueOrderRepository;
import com.webond.venue.repository.VenueRepository;
import com.webond.venue.repository.VenueSlotRepository;
import com.webond.venue.repository.VenueTypeRepository;
import com.webond.venue.service.VenueService;
import com.webond.venue.service.VenueTypeService;

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
	private VenueTypeService venueTypeService;

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

		// ==========================================
		// 🎯 測試：直接呼叫 VenueTypeService 查詢場地
		// ==========================================
//		System.out.println("\n====== 🎯 開始測試：使用 VenueTypeService 查詢 ======");
//
//		Integer targetTypeId = 1; // 假設要查場地類型編號 1 底下的場地
//
//		try {
//			// 直接呼叫你在 Service 寫好的方法
//			Set<VenueVO> venues = venueTypeService.getVenueByVenueType(targetTypeId);
//
//			if (venues != null && !venues.isEmpty()) {
//				System.out.println("👉 透過 Service 查到該類型底下有 " + venues.size() + " 間場地：");
//				for (VenueVO v : venues) {
//					System.out.println("   - 場地 ID: " + v.getVenueId() + " | 名稱: " + v.getVenueName());
//				}
//			} else {
//				System.out.println("👉 該類型底下目前沒有綁定任何場地，或找不到該場地類型。");
//			}
//		} catch (Exception e) {
//			System.err.println("❌ 測試發生錯誤，請檢查錯誤訊息：");
//			e.printStackTrace();
//		}
//
//		System.out.println("====== 🎯 測試結束 ======\n");
		
//		try {
//	        System.out.println("====== 1. 開始準備場地主表資料 ======");
//	        VenueVO venueVO = new VenueVO();
//	        venueVO.setVenueName("大安活動中心 - 聯動新增測試");
//	        // ... 其他場地必填欄位塞一塞 ...
//
//	        // 🎯 綁定你說的「已經存在的會員 8 號」
//	        MemberVO memberVO = new MemberVO();
//	        memberVO.setMemberId(8); 
//	        venueVO.setMember(memberVO); 
//	        
//	        VenueTypeVO venueTypeVO = new VenueTypeVO();
//	        venueTypeVO.setVenueTypeId(1);
//	        venueVO.setVenueTypeVO(venueTypeVO);
//
//
//	        System.out.println("====== 2. 準備測試用的假照片資料 ======");
//	        List<byte[]> imageList = new java.util.ArrayList<>();
//	        
//	        // 模擬兩張假圖片的 byte array
//	        imageList.add(new byte[]{1, 2, 3, 4, 5});
//	        imageList.add(new byte[]{6, 7, 8, 9, 10});
//
//
//	        System.out.println("====== 3. 開始執行 Service 聯動新增 ======");
//	        // 呼叫你的 service 執行儲存
//	        vs.addVenueWithImages(venueVO, imageList);
//	        
//	        System.out.println("✨ 聯動新增測試成功！請去資料庫看 VENUE 表與 VENUE_IMAGES 表是否有同步多出資料。");
//
//	    } catch (Exception e) {
//	        System.out.println("❌ 測試失敗，請檢查下方錯誤訊息：");
//	        e.printStackTrace();
//	    }

	}

}
