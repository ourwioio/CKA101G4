package com.notification.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberRepository;
import com.webond.notification.model.NotificationVO;
import com.webond.notification.repository.NotificationRepository;

/**
 * 與單元測試 (Spring21) 的根本差別:
 *   - 單元測試: 不啟動 Spring，用 @Mock 模擬依賴
 *   - 整合測試: 啟動「真的」Spring 容器，@Autowired 注入「真的」Bean，連「真的」資料庫
 *
 * 三個關鍵 Annotation:
 *   @SpringJUnitConfig(AppConfig.class)
 *       = @ExtendWith(SpringExtension.class) + @ContextConfiguration(classes=AppConfig.class)
 *       啟動 Spring 測試引擎，載入 AppConfig 組態
 *   @Transactional (放在測試類別上)
 *       每個測試方法跑完「自動 rollback」，寫入的資料不會留在資料庫
 *
 * ★ 注意 : 測試裡的 @Transactional 行為與 Service 層「相反」——
 *   Service 的 @Transactional 預設成功就 commit；
 *   測試的 @Transactional 預設「一律 rollback」，避免測試資料污染資料庫。
 */
@SpringBootTest
@Transactional
class NotificationServiceIntegrationTest {

	// 這些是 Spring 容器創建的「真實」Bean，不是 Mock
	@Autowired
	private NotificationRepository notificationRepository;

	@Test
	@DisplayName("查詢所有通知")
	void findAll_shouldReturnRealData() {

	    List<NotificationVO> notificationList = notificationRepository.findAll();

	    assertNotNull(notificationList);

	    System.out.println("查到 " + notificationList.size() + " 筆通知");
	    
	}
	
}
