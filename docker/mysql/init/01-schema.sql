-- =====================================================
-- WeBond 微伴｜完整整合版 SQL
-- =====================================================
SET SQL_SAFE_UPDATES = 0;

-- 指定匯入連線使用 UTF-8
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS webond_project
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE webond_project;

-- 再次確認目前資料庫連線編碼
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET collation_connection = 'utf8mb4_unicode_ci';

SET FOREIGN_KEY_CHECKS = 0;

-- 平台

DROP TABLE IF EXISTS FAQ;
DROP TABLE IF EXISTS PLATFORM_SPECIFICATION;
DROP TABLE IF EXISTS BULLETIN;

-- 通知 / 聊天
DROP TABLE IF EXISTS NOTIFICATION;
DROP TABLE IF EXISTS CHAT_MESSAGE;
DROP TABLE IF EXISTS CHAT_ROOM;
DROP TABLE IF EXISTS GROUP_CHAT_MESSAGE;

-- 場地
DROP TABLE IF EXISTS VENUE_REPORT;
DROP TABLE IF EXISTS VENUE_ORDER;
DROP TABLE IF EXISTS VENUE_IMAGES;
DROP TABLE IF EXISTS VENUE_REVIEW;
DROP TABLE IF EXISTS VENUE_SLOT;
DROP TABLE IF EXISTS VENUE;
DROP TABLE IF EXISTS VENUE_TYPE;

-- 服務
DROP TABLE IF EXISTS SERVICE_REPORT;
DROP TABLE IF EXISTS SERVICE_ORDER;
DROP TABLE IF EXISTS SERVICE_SLOT;
DROP TABLE IF EXISTS SERVICE;
DROP TABLE IF EXISTS SERVICE_TYPE;

-- 活動
DROP TABLE IF EXISTS ACTIVITY_REPORT;
DROP TABLE IF EXISTS ACT_ORDER;
DROP TABLE IF EXISTS ACTIVITY;
DROP TABLE IF EXISTS ACTIVITY_TYPE;

-- 權限 / 會員 / 員工
DROP TABLE IF EXISTS EMPLOYEE_PERMISSION;
DROP TABLE IF EXISTS PERMISSION;
DROP TABLE IF EXISTS MEMBER_REPORT;
DROP TABLE IF EXISTS MEMBER;
DROP TABLE IF EXISTS EMPLOYEE;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1. 員工 EMPLOYEE：整合 ZH.txt 與場地 table 需要的員工
-- =====================================================
CREATE TABLE EMPLOYEE
( EMPLOYEE_ID       INT  NOT NULL AUTO_INCREMENT COMMENT '員工編號',
  EMP_ACCOUNT       VARCHAR(50)  NOT NULL  UNIQUE COMMENT '登入帳號',
  PASSWORD_HASH     VARCHAR(255) NOT NULL COMMENT '密碼雜湊',
  EMP_NAME          VARCHAR(50)  NOT NULL COMMENT '員工姓名',
  ROLE_TITLE        TINYINT  NOT NULL COMMENT '職稱:0 系統管理員;1營運總監;2客服專員;3場地審核專員;4財務專員;5行銷專員',
  EMP_STATUS        TINYINT  NOT NULL COMMENT '帳號狀態：0未驗證、1正常、2註銷、3停權',
  CREATED_AT        DATETIME COMMENT '建立時間',
  UPDATED_AT        DATETIME COMMENT '更新時間',	
  LAST_LOGIN_AT     DATETIME COMMENT '最後登入時間',
  EMP_IMG           LONGBLOB COMMENT '員工頭貼',
  CONSTRAINT EMP_ID_PK PRIMARY KEY (EMPLOYEE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='員工';

-- 原本 ZH.txt 的員工資料，保留原始帳號、姓名、職稱與時間
INSERT INTO EMPLOYEE
(EMPLOYEE_ID, EMP_ACCOUNT, PASSWORD_HASH, EMP_NAME, ROLE_TITLE, EMP_STATUS, CREATED_AT, LAST_LOGIN_AT)
VALUES
(1, 'king111', 'king111', 'KING', '0', 1, '2025-01-01 00:00:00', '2026-05-22 00:00:00'),
(2, 'activity_emp2', 'activity_emp2', '活動員工2', '1', 1, '2025-05-01 00:00:00', '2026-05-22 00:00:00'),
(3, 'activity_emp3', 'activity_emp3', '活動員工3', '1', 1, '2025-05-01 00:00:00', '2026-05-22 00:00:00'),
(1001, 'emp123', 'emp123', '林', '1', 1, '2025-04-01 00:00:00', '2026-05-21 00:00:00'),
(1002, 'emp456', 'emp456', '吳', '2', 1, '2025-05-10 00:00:00', '2026-05-20 00:00:00'),
(1003, 'emp789', 'emp789', '郭', '3', 1, '2025-03-21 00:00:00', '2026-05-19 00:00:00'),
(1004, 'emp124', 'emp123', '王', '4', 1, '2025-05-17 00:00:00', '2026-05-21 00:00:00'),
(1005, 'emp125', 'emp125', '曾', '5', 1, '2025-04-27 00:00:00', '2026-05-18 00:00:00'),
-- 原本場地 table 使用 7001~7003，保留讓場地假資料不用大改
(7001, 'venue_emp1', 'venue_emp1', '場地員工1', '1', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00'),
(7002, 'venue_emp2', 'venue_emp2', '場地員工2', '1', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00'),
(7003, 'venue_emp3', 'venue_emp3', '場地員工3', '1', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00'),
(7004, 'employee1@webond.com', '$2a$10$DLeRy4Q0ohqaarO535ieue2H4SHmrr/2EHCbvFPT86er88kN2eH6m', '有所有權限者', '0', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00'),
(7005, 'employee2@webond.com', '$2a$10$DLeRy4Q0ohqaarO535ieue2H4SHmrr/2EHCbvFPT86er88kN2eH6m', '除了員工管理和交易', '1', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00'),
(7006, 'employee3@webond.com', '$2a$10$DLeRy4Q0ohqaarO535ieue2H4SHmrr/2EHCbvFPT86er88kN2eH6m', '處理會員跟檢舉', '2', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00'),
(7007, 'employee4@webond.com', '$2a$10$DLeRy4Q0ohqaarO535ieue2H4SHmrr/2EHCbvFPT86er88kN2eH6m', '處理場地審核', '3', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00'),
(7008, 'employee5@webond.com', '$2a$10$DLeRy4Q0ohqaarO535ieue2H4SHmrr/2EHCbvFPT86er88kN2eH6m', '處理交易', '4', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00'),
(7009, 'employee6@webond.com', '$2a$10$DLeRy4Q0ohqaarO535ieue2H4SHmrr/2EHCbvFPT86er88kN2eH6m', '處理行銷', '5', 1, '2026-05-01 00:00:00', '2026-05-25 00:00:00');


-- =====================================================
-- 2. 會員 MEMBER：沿用原本 5 筆，再新增到 10 筆
-- =====================================================
CREATE TABLE MEMBER (
    MEMBER_ID INT AUTO_INCREMENT COMMENT '會員編號',
    PASSWORD_HASH VARCHAR(255) NOT NULL COMMENT '密碼雜湊',
    NICKNAME VARCHAR(50) NOT NULL COMMENT '會員綽號', 
    MEMBER_PIC  LONGBLOB NULL COMMENT '大頭貼路徑', 
    GENDER TINYINT NOT NULL COMMENT '性別 (0：男, 1：女, 2：其他)',
    PHONE VARCHAR(20) COMMENT '手機號碼',
    EMAIL VARCHAR(100) NOT NULL UNIQUE COMMENT '電子郵件',
    MEMBER_INTRO VARCHAR(200) COMMENT '自我介紹',
    ACCOUNT_STATUS TINYINT NOT NULL DEFAULT 0 COMMENT '帳號狀態 (0：未驗證, 1：正常, 2：註銷, 3：停權, 4：限制活動權限)',
    CREATED_AT DATE NOT NULL COMMENT '建立時間',
    
    SERVICE_RATE_SUM DECIMAL(10,2) NOT NULL DEFAULT 0.0 COMMENT '參加服務被評價總分',
    SERVICE_RATE_COUNT INT NOT NULL DEFAULT 0 COMMENT '參加服務被評價次數',
    SERVICER_RATE_SUM DECIMAL(10,2) NOT NULL DEFAULT 0.0 COMMENT '提供服務被評價總分',
    SERVICER_RATE_COUNT INT NOT NULL DEFAULT 0 COMMENT '提供服務被評價次數',
    ACT_RATE_SUM DECIMAL(10,2) NOT NULL DEFAULT 0.0 COMMENT '參加活動被評價總分',
    ACT_RATE_COUNT INT NOT NULL DEFAULT 0 COMMENT '參加活動被評價次數',
    HOLDACT_RATE_SUM DECIMAL(10,2) NOT NULL DEFAULT 0.0 COMMENT '舉辦活動被評價總分',
    HOLDACT_RATE_COUNT INT NOT NULL DEFAULT 0 COMMENT '舉辦活動被評價次數',
    
    REPORT_POINTS INT NOT NULL DEFAULT 0 COMMENT '檢舉累計點數 (滿5點停權)', 
    
    KYC_ID INT UNIQUE COMMENT '審核編號',
    REAL_NAME VARCHAR(50) NOT NULL COMMENT '真實姓名',
    EMPLOYEE_ID INT NULL COMMENT '審核員工編號(FK)',
    ID_IMAGE  MEDIUMBLOB COMMENT '證件圖片路徑',
    FACE_IMAGE  MEDIUMBLOB COMMENT '臉部照片路徑',
    ID_NUMBER VARCHAR(20) NOT NULL COMMENT '身分證字號',
    KYC_STATUS TINYINT NOT NULL DEFAULT 0 COMMENT '審核狀態 (0：審核中, 1：審核通過, 2：審核失敗)',
	SUBMITTED_AT DATETIME NULL COMMENT '送審時間',
	REVIEWED_AT DATETIME NULL COMMENT '審核時間',
    
    BANK_CODE VARCHAR(10) COMMENT '銀行代碼',
    BANK_ACCOUNT VARCHAR(30) COMMENT '銀行帳號',
    
    PRIMARY KEY (MEMBER_ID),
    CONSTRAINT FK_MEMBER_EMPLOYEE FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員總表';

-- 原本「會員與會員檢舉.txt」的 5 筆會員，內容盡量原封不動
INSERT INTO MEMBER (
    MEMBER_ID, PASSWORD_HASH, NICKNAME, MEMBER_PIC, GENDER, PHONE, 
    EMAIL, MEMBER_INTRO, ACCOUNT_STATUS,CREATED_AT, 
    SERVICE_RATE_SUM, SERVICE_RATE_COUNT, SERVICER_RATE_SUM, SERVICER_RATE_COUNT,
    ACT_RATE_SUM, ACT_RATE_COUNT, HOLDACT_RATE_SUM, HOLDACT_RATE_COUNT,
    REPORT_POINTS, KYC_ID, REAL_NAME, EMPLOYEE_ID, 
    ID_IMAGE, FACE_IMAGE, ID_NUMBER, KYC_STATUS, SUBMITTED_AT, REVIEWED_AT, BANK_CODE, BANK_ACCOUNT
) VALUES 
(1, '$2a$10$hash001', '小咪', NULL, 1, '0912345678', 'mimi@email.com', '喜歡羽球與慢跑', 1,  CURDATE(), 60, 12, 44, 8, 60, 15, 25, 5, 0, 1001, '林小咪', 1003, NULL, NULL, 'F223456789', 1, '2026-04-15', '2026-04-17', '007', '123456789012'),
(2, '$2a$10$hash002', '小汪', NULL, 0, '0922333444', 'wang@email.com', '熱愛揪團打球', 1,  CURDATE(), 100, 20, 42, 10, 48, 18, 4.4, 6, 0, 1002, '王大汪', 1003,NULL, NULL, 'A123456789', 1, '2026-04-15', '2026-04-18', '822', '987654321098'),
(3, '$2a$10$hash003', '小鹿', NULL, 2, '0955666777', 'bambi@email.com', '喜歡參加各種活動', 3,  CURDATE(), 25, 5, 7, 2, 28, 4, 2.7, 1, 7, 1003, '張小鹿', 1003, NULL, NULL, 'E199999999', 1, '2026-04-15', '2026-04-18', '013', '555666777888'),
(4, '$2a$10$hash004', '歐巴', NULL, 0, '0933111222', 'oppa@email.com', '提供專業陪打服務', 1,  CURDATE(), 80, 50, 80, 120, 120, 80, 277, 60, 0, 1004, '崔歐巴', 1003, NULL, NULL, 'A112233445', 1, '2025-06-10', '2025-06-12', '012', '999888777666'),
(5, '$2a$10$hash005', '達人阿奇', NULL, 0, '0955999888', 'achie@email.com', '揪團狂熱份子', 1,  CURDATE() , 400, 80, 50, 30, 4.8, 100, 120, 25, 0, 1005, '洪阿奇', 1003, NULL, NULL, 'H123456789', 1, '2025-11-15', '2025-11-18', '812', '111222333444'),
(6, '$2a$10$hash006', 'Tom', NULL, 0, '0911222333', 'tom@gmail.com', '專業羽球教練', 1,  CURDATE(), 400, 10, 33, 8, 37, 5, 0, 0, 0, 1006, 'Tom Chen', 1003,NULL, NULL, 'T123456789', 1, '2026-04-20', '2026-04-21', '007', '123456789012'),
(7, '$2a$10$hash007', 'Mary', NULL, 1, '0922333444', 'mary@gmail.com', '喜歡揪團打球', 1,  CURDATE() , 35, 7, 25, 5, 38, 9, 0, 0, 0, 1007, 'Mary Lin', 1003, NULL, NULL, 'M223456789', 1, '2026-04-20', '2026-04-21', '822', '123456789012'),
(8, '$2a$10$hash008', 'John', NULL, 0, '0933444555', 'john@gmail.com', '場地管理員', 1,  CURDATE(), 43, 15, 27, 14, 47, 10, 0, 0, 0, 1008, 'John Wang', 1003, NULL, NULL, 'J123456789', 1, '2026-04-20', '2026-04-21', '700', '123456789012'),
(9, '$2a$10$hash009', 'Lisa', NULL, 1, '0944555666', 'lisa@gmail.com', '熱愛運動與旅行', 0,  CURDATE(), 13, 4, 23, 6, 9, 3, 6, 5, 0, 1009, 'Lisa Huang', 1003, NULL, NULL, 'L123456789', 0, '2026-04-20', NULL, '012', '123456789012'),
(10, '$2a$10$hash010', 'Kevin', NULL, 0, '0955666777', 'kevin@gmail.com', '兼職教練與活動主辦', 1,  CURDATE(), 11, 6, 9, 4, 13, 5, 7, 2, 1, 1010, 'Kevin Lee', 1003, NULL, NULL, 'F123456789', 2, '2026-04-20', '2026-04-22', '808', '123456789012'),
(11,'$2a$10$SY.goAPRT.qSAKN.OHtaA.3WxOHFxFPNzYf.G2C5rEclV.t.Ov2we' , 'tomcat01', NULL, 0, '0912000001', 'tomcat01@email.com', '我是 tomcat01，帳號功能正常', 1, CURDATE(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 1011, '湯姆一號', 1003, NULL, NULL, 'B123456789', 1, NOW(), NOW(), '822', '123456789012'),
(12, '$2a$10$SY.goAPRT.qSAKN.OHtaA.3WxOHFxFPNzYf.G2C5rEclV.t.Ov2we', 'tomcat02', NULL, 1, '0912000002', 'tomcat02@email.com', '我是 tomcat02，剛註冊待審核', 0, CURDATE(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 1012, '湯姆二號', NULL, NULL, NULL, 'C220011223', 0, NOW(), NULL, '013', '008123456789'),
(13, '$2a$10$SY.goAPRT.qSAKN.OHtaA.3WxOHFxFPNzYf.G2C5rEclV.t.Ov2we', 'tomcat03', NULL, 2, '0912000003', 'tomcat03@email.com', '我是 tomcat03，實名審核失敗', 2, CURDATE(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 1013, '湯姆三號', 1003, NULL, NULL, 'D188776655', 2, NOW(), NOW(), '700', '00012345678912'),
(14, '$2a$10$SY.goAPRT.qSAKN.OHtaA.3WxOHFxFPNzYf.G2C5rEclV.t.Ov2we', 'tomcat04', NULL, 0, '0912000004', 'tomcat04@email.com', '我是 tomcat04，被檢舉 4 點高風險', 1, CURDATE(), 0, 0, 0, 0, 0, 0, 0, 0, 4, 1014, '湯姆四號', 1003, NULL, NULL, 'G199887766', 1, NOW(), NOW(), '812', '987654321012'),
(15, '$2a$10$SY.goAPRT.qSAKN.OHtaA.3WxOHFxFPNzYf.G2C5rEclV.t.Ov2we', 'tomcat05', NULL, 0, '0912000005', 'tomcat05@email.com', '我是 tomcat05，已遭停權', 3, CURDATE(), 0, 0, 0, 0, 0, 0, 0, 0, 5, 1015, '湯姆五號', 1003, NULL, NULL, 'I112233445', 1, NOW(), NOW(), '004', '050012345678');




-- =====================================================
-- 3. 權限 PERMISSION / EMPLOYEE_PERMISSION：沿用 ZH.txt
-- =====================================================
CREATE TABLE PERMISSION
( PERMISSION_ID     INT  NOT NULL AUTO_INCREMENT COMMENT '權限編號',
  PERMISSION_NAME   VARCHAR(50) COMMENT '權限名稱',
  PERM_DESCRIPTION   VARCHAR(255) COMMENT '權限描述',
  PERM_UPDATE_AT     DATETIME COMMENT '權限更新時間',
  CONSTRAINT PERMISSION_ID_PK PRIMARY KEY (PERMISSION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='權限';

INSERT INTO PERMISSION (PERMISSION_ID, PERMISSION_NAME, PERM_DESCRIPTION) VALUES
(1, '員工管理', '負責對 "員工資料" 及 "權限" 進行新增、修改、刪除，以及員工帳號密碼管理。'),
(2, '服務管理', '負責會員之間的檢舉審核與管理，爭議訂單的檢舉審核與管理，惡意評價檢舉的審核與管理。'),
(3, '活動管理', '會員對揪團活動的檢舉審核與管理，爭議訂單的檢舉審核與管理，惡意評價檢舉的審核與管理。'),
(4, '場地管理', '爭議訂單的檢舉審核與管理，場地訂單，惡意評價檢舉的審核與管理。'),
(5, '會員管理', '負責會員的KYC審核，會員狀態的管理，會員的停權與復權管理。'),
(6, '場地審核管理', '負責場地審核的管理。'),
(7, '訂單交易管理', '負責付款狀態管理，撥款狀態管理，對交易紀錄的訂單及收入總額進行管理，包括服務訂單、揪團活動訂單及場地訂單。'),
(8, '平台管理', '平台內容管理，包括消息的發布、公告的管理及FAQ的管理。');

CREATE TABLE EMPLOYEE_PERMISSION
( EMP_PERM_ID       INT  NOT NULL AUTO_INCREMENT COMMENT '員工權限編號',
  EMPLOYEE_ID       INT  NOT NULL COMMENT '員工編號',
  PERMISSION_ID     INT NOT NULL COMMENT '權限編號',
  ASSIGNED_AT       DATETIME COMMENT '授權時間',
  CONSTRAINT PERMISSION_ID_PK PRIMARY KEY (EMP_PERM_ID),
  CONSTRAINT EMPLOYEE_PERMISSION_EMPLOYEE_ID_FK FOREIGN KEY(EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID),
  CONSTRAINT EMPLOYEE_PERMISSION_PERMISSION_ID_FK FOREIGN KEY(PERMISSION_ID) REFERENCES PERMISSION(PERMISSION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='員工權限';

INSERT INTO EMPLOYEE_PERMISSION (EMPLOYEE_ID, PERMISSION_ID, ASSIGNED_AT)VALUES
(1, 1, '2025-01-02 00:00:00'),
(1001, 2, '2025-05-22 00:00:00'),
(1002, 3, '2025-05-22 00:00:00'),
(1002, 4, '2025-05-22 00:00:00'),
(1003, 5, '2025-05-22 00:00:00'),
(1004, 6, '2025-05-22 00:00:00'),
(1005, 4, '2025-05-22 00:00:00'),
(7004, 1, '2025-05-22 00:00:00'),
(7004, 2, '2025-05-22 00:00:00'),
(7004, 3, '2025-05-22 00:00:00'),
(7004, 4, '2025-05-22 00:00:00'),
(7004, 5, '2025-05-22 00:00:00'),
(7004, 6, '2025-05-22 00:00:00'),
(7004, 7, '2025-05-22 00:00:00'),
(7004, 8, '2025-05-22 00:00:00'),
(7005, 2, '2025-05-22 00:00:00'),
(7005, 3, '2025-05-22 00:00:00'),
(7005, 4, '2025-05-22 00:00:00'),
(7005, 5, '2025-05-22 00:00:00'),
(7005, 6, '2025-05-22 00:00:00'),
(7005, 8, '2025-05-22 00:00:00'),
(7006, 2, '2025-05-22 00:00:00'),
(7006, 3, '2025-05-22 00:00:00'),
(7006, 4, '2025-05-22 00:00:00'),
(7006, 5, '2025-05-22 00:00:00'),
(7007, 6, '2025-05-22 00:00:00'),
(7008, 7, '2025-05-22 00:00:00'),
(7009, 8, '2025-05-22 00:00:00');



-- =====================================================
-- 4. 會員檢舉 MEMBER_REPORT：沿用原本 4 筆，再補 1 筆支援通知 FK
-- =====================================================
CREATE TABLE MEMBER_REPORT (
    REPORT_ID INT AUTO_INCREMENT COMMENT '檢舉案編號',
    REPORTER_ID INT NOT NULL COMMENT '檢舉者編號(FK)',
    REPORTED_ID INT NOT NULL COMMENT '被檢舉者編號(FK)',
    EMPLOYEE_ID INT  COMMENT '審核員工編號(FK)',
    REPORT_CATEGORY TINYINT NOT NULL COMMENT '檢舉類別 (0：騷擾, 1：內容不實, 2：私下交易, 3：其他)',
    REPORT_CONTENT VARCHAR(500) NULL COMMENT '檢舉詳細描述',
    EVIDENCE MEDIUMBLOB  NULL COMMENT '證據截圖',
    REPORT_STATUS TINYINT NOT NULL DEFAULT 0 COMMENT '處理狀態 (0：待處理, 1：處理中, 2：成立, 3：不成立)',
    ADMIN_NOTE VARCHAR(255) NULL COMMENT '管理員處理備註',
    VIOLATION_POINTS INT DEFAULT 0 COMMENT '此案判定違規點數',     CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '檢舉送出時間',
    PROCESSED_AT DATETIME NULL COMMENT '審核完成時間',
    
    PRIMARY KEY (REPORT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員檢舉表';


INSERT INTO MEMBER_REPORT (
    REPORT_ID, REPORTER_ID, REPORTED_ID, EMPLOYEE_ID, REPORT_CATEGORY, REPORT_CONTENT, REPORT_STATUS, ADMIN_NOTE, VIOLATION_POINTS, CREATED_AT, PROCESSED_AT
) VALUES 
-- 案例一：私訊性暗示（成立，記 2 點）→ 類別改為 1 惡意騷擾
(
    1, 1, 3, 1003, 1, 
    '該會員在私訊中頻繁傳送帶有強烈性暗示的文字騷擾，並企圖索取私密照，造成極大不舒服。', 
    2, '經查雙方私訊對話，性騷擾與性暗示言論屬實，予以記違規點數 2 點。', 
    2, '2026-05-15 10:00:00', '2026-05-16 11:00:00'
),
-- 案例二：傳送色情圖片（成立，記 2 點）→ 類別改為 1 惡意騷擾
(
    2, 1, 3, 1003, 1, 
    '傳屌照給其他會員，並企圖約炮。', 
    2, '查閱聊天室日誌，確實傳送色情圖片給會員，記 2 點。', 
    2, '2026-05-15 16:00:00', '2026-05-15 18:00:00'
),
-- 案例三：聊天室惡意辱罵（成立，記 3 點 ➔ 累計滿 7 點觸發停權）→ 類別 0 不當言論，不變
(
    3, 4, 3, 1003, 0, 
    '在揪團的多人臨時聊天室中，因為意見不合突然瘋狂用髒話惡意辱罵團員，人身攻擊言詞極為粗暴。', 
    2, '查閱多人聊天室日誌，惡意辱罵與粗暴言詞屬實。因屬嚴重情節且為累犯，記 3 點並執行停權。', 
    3, '2026-05-19 16:00:00', '2026-05-19 18:00:00'
),
-- 案例四：內容誇大之報復性投訴（不成立，記 0 點）→ 類別改為 3 其他原因
(
    4, 3, 2, 1003, 3, 
    '惡意報復性投訴，覺得小汪在聊天室分享的揪團景點介紹有誇大不實、涉嫌欺騙。', 
    3, '經查投訴內容僅為使用者個人主觀感受不同，不構成違規，裁定不成立。', 
    0, '2026-05-20 12:00:00', '2026-05-20 15:00:00'
),
-- 案例五：涉嫌詐騙行為（待處理/未審核，空值設定範例）→ 類別 2 詐騙行為，不變
(
    5, 2, 4, NULL, 2,                     
    '該會員在私訊中提供不明外部投資連結，並一直鼓吹我加入高投報率的虛擬貨幣群組，懷疑是詐騙集團。', 
    0, NULL, 0, '2026-05-21 09:30:00', NULL
);

-- =====================================================
-- 5. 通知 NOTIFICATION：沿用「會員+服務檢舉+通知.txt」原本 5 筆
-- =====================================================
CREATE TABLE NOTIFICATION(
    NOTIFICATION_ID INT PRIMARY KEY AUTO_INCREMENT COMMENT  '通知編號',
    MEMBER_ID INT NOT NULL COMMENT '接收會員編號',
    TITLE VARCHAR(100) NOT NULL COMMENT '通知標題',
    CONTENT VARCHAR(500) NOT NULL COMMENT '通知內容',
    NOTIFICATION_TYPE TINYINT NOT NULL COMMENT '通知類型：0訂單、1評價、2系統、3聊天室',
    IS_READ TINYINT DEFAULT 0 NOT NULL COMMENT '是否已讀：0未讀、1已讀',
    REPORT_ID INT COMMENT '會員檢舉案編號',
    EMPLOYEE_ID INT COMMENT '員工編號',
    CREATED_AT DATETIME COMMENT '建立時間',
    CONSTRAINT FK_NOTIFICATION_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER(MEMBER_ID),
    CONSTRAINT FK_NOTIFICATION_REPORT FOREIGN KEY (REPORT_ID) REFERENCES MEMBER_REPORT(REPORT_ID),
    CONSTRAINT FK_NOTIFICATION_EMPLOYEE FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知';

INSERT INTO NOTIFICATION
(NOTIFICATION_ID, MEMBER_ID, TITLE, CONTENT, NOTIFICATION_TYPE, IS_READ, REPORT_ID, EMPLOYEE_ID, CREATED_AT)
VALUES
(1, 3, '檢舉成立通知', '您的服務因違反平台規範已下架', 2, 1, 1, 1003, NOW()),
(2, 1, '系統通知', '歡迎加入本平台', 2, 0, NULL, 1002, NOW()),
(3, 3, '訂單通知', '您有新的場地預約訂單', 0, 0, NULL, 1001, NOW()),
(4, 4, '聊天室通知', '您收到新的聊天訊息', 3, 1, NULL, 1001, NOW()),
(5, 5, '評價通知', '有會員給您新的評價', 1, 1, NULL, 1001, NOW());
-- =====================================================

-- =====================================================
-- 6. 一對一服務模組
-- 版本：8 種服務類型、10 筆親民價格服務假資料
-- 圖片：SERVICE_ID 1～10 對應 1.png～10.png
-- 注意：SERVICE_REPORT 欄位與約束維持原本設計，不做修改

-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS SERVICE_REPORT;
DROP TABLE IF EXISTS SERVICE_ORDER;
DROP TABLE IF EXISTS SERVICE_SLOT;
DROP TABLE IF EXISTS SERVICE;
DROP TABLE IF EXISTS SERVICE_TYPE;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- SERVICE_TYPE
-- =====================================================

CREATE TABLE SERVICE_TYPE (

    SERVICE_TYPE_ID INT NOT NULL AUTO_INCREMENT COMMENT '服務類型編號',

    TYPE_NAME VARCHAR(50) NOT NULL COMMENT '類型名稱',

    DESCRIPTION VARCHAR(255) COMMENT '類型描述',

    TYPE_MODE TINYINT NOT NULL COMMENT '0：動態 1：靜態',

    DEFAULT_IMAGE_URL VARCHAR(255) COMMENT '服務類型預設圖片',

    PRIMARY KEY (SERVICE_TYPE_ID),

    CONSTRAINT CK_SERVICE_TYPE_MODE
        CHECK (TYPE_MODE IN (0,1))

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='服務類型';


-- =====================================================
-- SERVICE
-- =====================================================

CREATE TABLE SERVICE (

    SERVICE_ID INT NOT NULL AUTO_INCREMENT COMMENT '服務編號',

    SERVICE_TYPE_ID INT NOT NULL COMMENT '服務類型',

    MEMBER_ID INT NOT NULL COMMENT '服務提供者',

    SERVICE_NAME VARCHAR(100) NOT NULL COMMENT '服務名稱',

    DESCRIPTION VARCHAR(500) COMMENT '服務描述',

    HOURLY_RATE INT NOT NULL COMMENT '每小時價格',

    STATUS TINYINT NOT NULL DEFAULT 1
        COMMENT '0下架 1上架 2封存 3平台停用',

    CREATED_AT DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        COMMENT '建立時間',

    SERVICE_IMAGE LONGBLOB COMMENT '服務圖片',

    SERVICE_IMAGE_TYPE VARCHAR(100)
        COMMENT '圖片格式',

    SERVICE_CITY VARCHAR(20) NOT NULL
        COMMENT '服務縣市',

    SERVICE_DISTRICT VARCHAR(20) NOT NULL
        COMMENT '服務行政區',

    SERVICE_LOCATION VARCHAR(255) NOT NULL
        COMMENT '服務地點說明',

    PRIMARY KEY (SERVICE_ID),

    CONSTRAINT CK_SERVICE_RATE
        CHECK (HOURLY_RATE >= 100),

    CONSTRAINT CK_SERVICE_STATUS
        CHECK (STATUS IN (0,1,2,3)),

    CONSTRAINT FK_SERVICE_TYPE
        FOREIGN KEY (SERVICE_TYPE_ID)
        REFERENCES SERVICE_TYPE (SERVICE_TYPE_ID),

    CONSTRAINT FK_SERVICE_MEMBER
        FOREIGN KEY (MEMBER_ID)
        REFERENCES MEMBER (MEMBER_ID)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='服務';


-- =====================================================
-- SERVICE_SLOT
-- =====================================================

CREATE TABLE SERVICE_SLOT (

    SERVICE_SLOT_ID INT NOT NULL AUTO_INCREMENT COMMENT '服務時段編號',

    SERVICE_ID INT NOT NULL COMMENT '服務編號',

    START_TIME DATETIME NOT NULL COMMENT '開始時間',

    END_TIME DATETIME NOT NULL COMMENT '結束時間',

    SLOT_STATUS TINYINT NOT NULL DEFAULT 0
        COMMENT '0可預約 1暫時鎖定 2已預約 3已封存',

    LOCK_EXPIRES_AT DATETIME
        COMMENT '鎖定到期時間',

    PRIMARY KEY (SERVICE_SLOT_ID),

    CONSTRAINT CK_SLOT_STATUS
        CHECK (SLOT_STATUS IN (0,1,2,3)),

    CONSTRAINT FK_SLOT_SERVICE
        FOREIGN KEY (SERVICE_ID)
        REFERENCES SERVICE (SERVICE_ID)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='服務時段';


-- =====================================================
-- SERVICE_ORDER
-- =====================================================

CREATE TABLE SERVICE_ORDER (

    SERVICE_ORDER_ID INT NOT NULL AUTO_INCREMENT
        COMMENT '服務訂單編號',

    SERVICE_SLOT_ID INT NOT NULL,

    SERVICE_ID INT NOT NULL,

    BUYER_MEMBER_ID INT NOT NULL,

    EMPLOYEE_ID INT,

    ORDER_HOURLY_RATE INT NOT NULL,

    TOTAL_AMOUNT INT NOT NULL,

    ORDER_STATUS TINYINT NOT NULL DEFAULT 0
        COMMENT '0待賣家確認 1待買家付款 2已成立 3已完成(待撥款) 4已取消',

    BUYER_REQUEST_NOTE VARCHAR(500),

    SELLER_REQUIREMENT_NOTE VARCHAR(500),

    SERVICE_PAYMENT_METHOD TINYINT
        COMMENT '0信用卡 1ATM',

    CREATED_AT DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    SERVICE_COMPLETED_AT DATETIME,

    BUYER_RATE_SELLER TINYINT,

    BUYER_REVIEW_COMMENT VARCHAR(500),

    BUYER_REVIEWED_AT DATETIME,

    SELLER_RATE_BUYER TINYINT,

    SELLER_REVIEW_COMMENT VARCHAR(500),

    SELLER_REVIEWED_AT DATETIME,

    PAYOUT_STATUS TINYINT NOT NULL DEFAULT 0
        COMMENT '0未撥款 1已撥款',

    REFUND_STATUS TINYINT NOT NULL DEFAULT 0
        COMMENT '0無退款 1待退款 2已退款',

    HANDLED_AT DATETIME,

    CANCELLED_BY_ROLE TINYINT
        COMMENT '0買方取消 1賣方取消 2後台取消 3系統逾時取消',

    CANCEL_REASON VARCHAR(500),

    CANCELLED_AT DATETIME,

    REFUND_AMOUNT INT,

    SELLER_CONFIRM_EXPIRES_AT DATETIME,

    PAYMENT_EXPIRES_AT DATETIME,

    SELLER_MEMBER_ID INT,

    SERVICE_NAME_SNAPSHOT VARCHAR(100) NOT NULL,

    SERVICE_TYPE_NAME_SNAPSHOT VARCHAR(500) NOT NULL,

    SERVICE_DESCRIPTION_SNAPSHOT VARCHAR(500) NULL,

    SLOT_START_TIME_SNAPSHOT DATETIME,

    SLOT_END_TIME_SNAPSHOT DATETIME,

    SERVICE_CITY_SNAPSHOT VARCHAR(20) NOT NULL,

    SERVICE_DISTRICT_SNAPSHOT VARCHAR(20) NOT NULL,

    SERVICE_LOCATION_SNAPSHOT VARCHAR(255) NOT NULL,

    PRIMARY KEY (SERVICE_ORDER_ID),

    CONSTRAINT CK_ORDER_STATUS
        CHECK (ORDER_STATUS IN (0,1,2,3,4)),

    CONSTRAINT CK_PAYMENT_METHOD
        CHECK (SERVICE_PAYMENT_METHOD IN (0,1)),

    CONSTRAINT CK_PAYOUT_STATUS
        CHECK (PAYOUT_STATUS IN (0,1)),

    CONSTRAINT CK_REFUND_STATUS
        CHECK (REFUND_STATUS IN (0,1,2)),

    CONSTRAINT CK_CANCEL_ROLE
        CHECK (
            CANCELLED_BY_ROLE IS NULL
            OR CANCELLED_BY_ROLE IN (0,1,2,3)
        ),

    CONSTRAINT FK_ORDER_SLOT
        FOREIGN KEY (SERVICE_SLOT_ID)
        REFERENCES SERVICE_SLOT (SERVICE_SLOT_ID),

    CONSTRAINT FK_ORDER_SERVICE
        FOREIGN KEY (SERVICE_ID)
        REFERENCES SERVICE (SERVICE_ID),

    CONSTRAINT FK_SERVICE_ORDER_BUYER
        FOREIGN KEY (BUYER_MEMBER_ID)
        REFERENCES MEMBER (MEMBER_ID),

    CONSTRAINT FK_SERVICE_ORDER_EMPLOYEE
        FOREIGN KEY (EMPLOYEE_ID)
        REFERENCES EMPLOYEE (EMPLOYEE_ID)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='服務訂單';


-- =====================================================
-- SERVICE_REPORT
-- 檢舉欄位與約束完全保留原本版本
-- =====================================================

CREATE TABLE SERVICE_REPORT (

    SERVICE_REPORT_ID INT NOT NULL AUTO_INCREMENT
        COMMENT '服務檢舉編號',

    SERVICE_ID INT NOT NULL
        COMMENT '服務編號',

    REPORTER_MEMBER_ID INT NOT NULL
        COMMENT '提出檢舉會員',

    SERVICE_REPORT_COM VARCHAR(500)
        COMMENT '檢舉內容',

    SERVICE_REPORT_TIME DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        COMMENT '檢舉時間',

    EMPLOYEE_ID INT
        COMMENT '處理員工',

    SERVICE_REPORT_HANDLE_TIME DATETIME
        COMMENT '處理時間',

    SERVICE_REPORT_STATUS TINYINT NOT NULL DEFAULT 0
        COMMENT '0:未審核 1:已審核通過 2:已審核未通過',

    PRIMARY KEY (SERVICE_REPORT_ID),

    CONSTRAINT CK_SERVICE_REPORT_STATUS
        CHECK (SERVICE_REPORT_STATUS IN (0,1,2)),

    CONSTRAINT FK_SERVICE_REPORT_ORDER
        FOREIGN KEY (SERVICE_ID)
        REFERENCES SERVICE (SERVICE_ID),

    CONSTRAINT FK_REPORT_MEMBER
        FOREIGN KEY (REPORTER_MEMBER_ID)
        REFERENCES MEMBER (MEMBER_ID),

    CONSTRAINT FK_SERVICE_REPORT_EMPLOYEE
        FOREIGN KEY (EMPLOYEE_ID)
        REFERENCES EMPLOYEE (EMPLOYEE_ID)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='服務檢舉';

-- =====================================================
-- SERVICE_TYPE 假資料
-- 固定 SERVICE_TYPE_ID 1～8
-- =====================================================

INSERT INTO SERVICE_TYPE
(
    SERVICE_TYPE_ID,
    TYPE_NAME,
    DESCRIPTION,
    TYPE_MODE,
    DEFAULT_IMAGE_URL
)
VALUES
(1, '公園散步', '公園步道散步、放鬆與日常陪伴', 0, NULL),
(2, '市集逛街', '戶外市集、文創攤位與輕鬆逛街', 0, NULL),
(3, '夜市美食', '夜市散步、品嘗小吃與美食同行', 0, NULL),
(4, '野餐休閒', '公園野餐、草地休息與戶外放鬆', 0, NULL),
(5, '戶外球類', '網球、飛盤等戶外休閒球類活動', 0, NULL),
(6, '慢跑單車', '河濱慢跑、單車與輕量戶外運動', 0, NULL),
(7, '登山健行', '新手友善步道、郊山與自然健行', 0, NULL),
(8, '河濱聊天', '河岸散步、看風景與輕鬆聊天', 0, NULL);


-- =====================================================
-- SERVICE 假資料
-- SERVICE_ID 1～10 對應圖片檔名 1.png～10.png
-- MEMBER_ID 使用現有會員 1～10
-- =====================================================

INSERT INTO SERVICE
(
    SERVICE_ID,
    SERVICE_TYPE_ID,
    MEMBER_ID,
    SERVICE_NAME,
    DESCRIPTION,
    HOURLY_RATE,
    STATUS,
    CREATED_AT,
    SERVICE_CITY,
    SERVICE_DISTRICT,
    SERVICE_LOCATION
)
VALUES

-- 圖片：1.png
(1, 8, 1, '河濱散步聊天',
 '沿著河濱步道輕鬆散步、看夕陽與聊生活，不趕行程，也不需要任何專業能力。',
 150, 1, DATE_SUB(NOW(), INTERVAL 10 DAY),
 '台北市', '中山區', '大佳河濱公園入口集合'),

-- 圖片：2.png
(2, 1, 2, '公園散步陪伴',
 '在綠意公園中慢慢散步，可以聊天、放空或坐在長椅休息，行程依當天狀況調整。',
 120, 1, DATE_SUB(NOW(), INTERVAL 9 DAY),
 '桃園市', '桃園區', '虎頭山公園遊客中心附近'),

-- 圖片：3.png
(3, 2, 3, '一起逛戶外市集',
 '一起逛戶外市集、看看文創小物與生活雜貨，沒有購物壓力，單純享受逛街氣氛。',
 150, 1, DATE_SUB(NOW(), INTERVAL 8 DAY),
 '台中市', '西區', '草悟廣場戶外市集入口'),

-- 圖片：4.png
(4, 3, 4, '夜市美食同行',
 '一起逛夜市、挑選想吃的小吃並分享感想，餐點費用各自負擔。',
 150, 1, DATE_SUB(NOW(), INTERVAL 7 DAY),
 '台北市', '松山區', '饒河街觀光夜市入口'),

-- 圖片：5.png
(5, 4, 5, '公園野餐陪伴',
 '在公園草地簡單野餐、聊天與曬太陽，可自備食物，也可以只帶飲料輕鬆坐坐。',
 180, 1, DATE_SUB(NOW(), INTERVAL 6 DAY),
 '新北市', '板橋區', '板橋435藝文特區大草坪'),

-- 圖片：6.png
(6, 5, 6, '戶外網球休閒陪打',
 '以輕鬆對打和接球為主，不是專業教學，適合想找球伴活動一下的會員。',
 250, 1, DATE_SUB(NOW(), INTERVAL 5 DAY),
 '桃園市', '桃園區', '桃園市立網球場戶外球場'),

-- 圖片：7.png
(7, 6, 7, '河濱慢跑同行',
 '依彼此體力安排輕鬆慢跑或跑走交替，不追求配速，也不是正式訓練課程。',
 150, 1, DATE_SUB(NOW(), INTERVAL 4 DAY),
 '新北市', '三重區', '幸福水漾公園河濱步道'),

-- 圖片：8.png
(8, 5, 8, '公園飛盤陪玩',
 '在公園草地簡單丟接飛盤，不需要經驗，適合想在戶外活動又不想太累的會員。',
 120, 1, DATE_SUB(NOW(), INTERVAL 3 DAY),
 '台北市', '大安區', '大安森林公園中央草地'),

-- 圖片：9.png
(9, 6, 9, '河濱單車同行',
 '沿河濱自行車道輕鬆騎乘，可依體力調整距離與休息次數，不競速。',
 180, 1, DATE_SUB(NOW(), INTERVAL 2 DAY),
 '新北市', '八里區', '八里左岸自行車租借站'),

-- 圖片：10.png
(10, 7, 10, '郊山健行陪伴',
 '選擇大眾化的新手步道一起健行，途中可隨時休息，重點是欣賞風景與安全同行。',
 250, 1, DATE_SUB(NOW(), INTERVAL 1 DAY),
 '台北市', '信義區', '象山步道靈雲宮入口');


-- =====================================================
-- SERVICE_SLOT 假資料
-- 固定 SERVICE_SLOT_ID 1～20
-- 每個服務各有兩筆時段
-- =====================================================

INSERT INTO SERVICE_SLOT
(
    SERVICE_SLOT_ID,
    SERVICE_ID,
    START_TIME,
    END_TIME,
    SLOT_STATUS,
    LOCK_EXPIRES_AT
)
VALUES

-- 第一輪：部分時段搭配訂單狀態
(1,  1, DATE_SUB(NOW(), INTERVAL 5 DAY),
         DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 1 HOUR),
         2, NULL),

(2,  2, DATE_SUB(NOW(), INTERVAL 4 DAY),
         DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 2 HOUR),
         2, NULL),

(3,  3, DATE_ADD(NOW(), INTERVAL 5 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 2 HOUR),
         0, NULL),

(4,  4, DATE_ADD(NOW(), INTERVAL 6 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 6 DAY), INTERVAL 2 HOUR),
         1, DATE_ADD(NOW(), INTERVAL 1 DAY)),

(5,  5, DATE_ADD(NOW(), INTERVAL 7 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 7 DAY), INTERVAL 2 HOUR),
         2, NULL),

(6,  6, DATE_ADD(NOW(), INTERVAL 8 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 DAY), INTERVAL 2 HOUR),
         0, NULL),

(7,  7, DATE_ADD(NOW(), INTERVAL 2 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 1 HOUR),
         0, NULL),

(8,  8, DATE_ADD(NOW(), INTERVAL 10 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 10 DAY), INTERVAL 1 HOUR),
         0, NULL),

(9,  9, DATE_SUB(NOW(), INTERVAL 3 DAY),
         DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 2 HOUR),
         2, NULL),

(10, 10, DATE_SUB(NOW(), INTERVAL 2 DAY),
          DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 3 HOUR),
          2, NULL),

-- 第二輪：全部可預約
(11, 1, DATE_ADD(NOW(), INTERVAL 6 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 6 DAY), INTERVAL 1 HOUR),
         0, NULL),

(12, 2, DATE_ADD(NOW(), INTERVAL 7 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 7 DAY), INTERVAL 1 HOUR),
         0, NULL),

(13, 3, DATE_ADD(NOW(), INTERVAL 8 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 DAY), INTERVAL 2 HOUR),
         0, NULL),

(14, 4, DATE_ADD(NOW(), INTERVAL 9 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 9 DAY), INTERVAL 2 HOUR),
         0, NULL),

(15, 5, DATE_ADD(NOW(), INTERVAL 10 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 10 DAY), INTERVAL 2 HOUR),
         0, NULL),

(16, 6, DATE_ADD(NOW(), INTERVAL 11 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 11 DAY), INTERVAL 2 HOUR),
         0, NULL),

(17, 7, DATE_ADD(NOW(), INTERVAL 12 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 12 DAY), INTERVAL 1 HOUR),
         0, NULL),

(18, 8, DATE_ADD(NOW(), INTERVAL 13 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 13 DAY), INTERVAL 1 HOUR),
         0, NULL),

(19, 9, DATE_ADD(NOW(), INTERVAL 13 DAY),
         DATE_ADD(DATE_ADD(NOW(), INTERVAL 13 DAY), INTERVAL 2 HOUR),
         0, NULL),

(20, 10, DATE_ADD(NOW(), INTERVAL 14 DAY),
          DATE_ADD(DATE_ADD(NOW(), INTERVAL 14 DAY), INTERVAL 3 HOUR),
          0, NULL);


-- =====================================================
-- SERVICE_ORDER 假資料
-- 買家與賣家不會是同一位會員
-- 員工使用目前已存在的 1001～1005
-- =====================================================

INSERT INTO SERVICE_ORDER
(
    SERVICE_ORDER_ID,
    SERVICE_SLOT_ID,
    SERVICE_ID,
    BUYER_MEMBER_ID,
    EMPLOYEE_ID,
    ORDER_HOURLY_RATE,
    TOTAL_AMOUNT,
    ORDER_STATUS,
    BUYER_REQUEST_NOTE,
    SELLER_REQUIREMENT_NOTE,
    SERVICE_PAYMENT_METHOD,
    CREATED_AT,
    SERVICE_COMPLETED_AT,
    PAYOUT_STATUS,
    REFUND_STATUS,
    HANDLED_AT,
    CANCELLED_BY_ROLE,
    CANCEL_REASON,
    CANCELLED_AT,
    REFUND_AMOUNT,
    SELLER_CONFIRM_EXPIRES_AT,
    PAYMENT_EXPIRES_AT,
    SELLER_MEMBER_ID,
    SERVICE_NAME_SNAPSHOT,
    SERVICE_TYPE_NAME_SNAPSHOT,
    SERVICE_DESCRIPTION_SNAPSHOT,
    SLOT_START_TIME_SNAPSHOT,
    SLOT_END_TIME_SNAPSHOT,
    SERVICE_CITY_SNAPSHOT,
    SERVICE_DISTRICT_SNAPSHOT,
    SERVICE_LOCATION_SNAPSHOT
)
VALUES

-- 1：已完成、未撥款
(1, 1, 1, 6, NULL,
 150, 150, 3,
 '想沿著河邊走走並聊聊最近的生活。',
 '穿舒適鞋子即可，飲料各自準備。',
 0,
 DATE_SUB(NOW(), INTERVAL 6 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 1 HOUR),
 0, 0, NULL,
 NULL, NULL, NULL, 0,
 NULL, NULL,
 1,
 '河濱散步聊天',
 '河濱聊天',
 '沿著河濱步道輕鬆散步、看夕陽與聊生活，不趕行程，也不需要任何專業能力。',
 DATE_SUB(NOW(), INTERVAL 5 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 1 HOUR),
 '台北市', '中山區', '大佳河濱公園入口集合'),

-- 2：已完成、已撥款
(2, 2, 2, 7, 1001,
 120, 240, 3,
 '希望慢慢走，不需要趕行程。',
 '請穿適合散步的鞋子。',
 1,
 DATE_SUB(NOW(), INTERVAL 5 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 2 HOUR),
 1, 0, NOW(),
 NULL, NULL, NULL, 0,
 NULL, NULL,
 2,
 '公園散步陪伴',
 '公園散步',
 '在綠意公園中慢慢散步，可以聊天、放空或坐在長椅休息，行程依當天狀況調整。',
 DATE_SUB(NOW(), INTERVAL 4 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 2 HOUR),
 '桃園市', '桃園區', '虎頭山公園遊客中心附近'),

-- 3：待賣家確認
(3, 3, 3, 8, NULL,
 150, 300, 0,
 '想找人一起看看市集攤位。',
 NULL,
 NULL,
 NOW(),
 NULL,
 0, 0, NULL,
 NULL, NULL, NULL, 0,
 DATE_ADD(NOW(), INTERVAL 1 DAY),
 NULL,
 3,
 '一起逛戶外市集',
 '市集逛街',
 '一起逛戶外市集、看看文創小物與生活雜貨，沒有購物壓力，單純享受逛街氣氛。',
 DATE_ADD(NOW(), INTERVAL 5 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 2 HOUR),
 '台中市', '西區', '草悟廣場戶外市集入口'),

-- 4：待買家付款
(4, 4, 4, 9, NULL,
 150, 300, 1,
 '想一起吃幾樣夜市小吃。',
 '餐點費用各自負擔。',
 NULL,
 NOW(),
 NULL,
 0, 0, NULL,
 NULL, NULL, NULL, 0,
 NULL,
 DATE_ADD(NOW(), INTERVAL 1 DAY),
 4,
 '夜市美食同行',
 '夜市美食',
 '一起逛夜市、挑選想吃的小吃並分享感想，餐點費用各自負擔。',
 DATE_ADD(NOW(), INTERVAL 6 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 6 DAY), INTERVAL 2 HOUR),
 '台北市', '松山區', '饒河街觀光夜市入口'),

-- 5：已成立、尚未完成
(5, 5, 5, 10, NULL,
 180, 360, 2,
 '希望在草地坐坐、聊天和吃點東西。',
 '可以自備簡單食物與野餐墊。',
 0,
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 NULL,
 0, 0, NULL,
 NULL, NULL, NULL, 0,
 NULL, NULL,
 5,
 '公園野餐陪伴',
 '野餐休閒',
 '在公園草地簡單野餐、聊天與曬太陽，可自備食物，也可以只帶飲料輕鬆坐坐。',
 DATE_ADD(NOW(), INTERVAL 7 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 7 DAY), INTERVAL 2 HOUR),
 '新北市', '板橋區', '板橋435藝文特區大草坪'),

-- 6：已取消、待退款
(6, 6, 6, 1, NULL,
 250, 500, 4,
 '只想休閒對打，不需要正式教學。',
 '請自行攜帶球拍與飲用水。',
 1,
 DATE_SUB(NOW(), INTERVAL 2 DAY),
 NULL,
 0, 1, NULL,
 1,
 '賣家臨時無法前往球場。',
 NOW(),
 500,
 NULL, NULL,
 6,
 '戶外網球休閒陪打',
 '戶外球類',
 '以輕鬆對打和接球為主，不是專業教學，適合想找球伴活動一下的會員。',
 DATE_ADD(NOW(), INTERVAL 8 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 DAY), INTERVAL 2 HOUR),
 '桃園市', '桃園區', '桃園市立網球場戶外球場'),

-- 7：已取消、不需退款
(7, 7, 7, 2, NULL,
 150, 150, 4,
 '希望用跑走交替的方式完成。',
 '請依自己的體力準備水和毛巾。',
 0,
 DATE_SUB(NOW(), INTERVAL 2 DAY),
 NULL,
 0, 0, NULL,
 0,
 '買家於服務開始前三天內取消。',
 NOW(),
 0,
 NULL, NULL,
 7,
 '河濱慢跑同行',
 '慢跑單車',
 '依彼此體力安排輕鬆慢跑或跑走交替，不追求配速，也不是正式訓練課程。',
 DATE_ADD(NOW(), INTERVAL 2 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 1 HOUR),
 '新北市', '三重區', '幸福水漾公園河濱步道'),

-- 8：已取消、已退款
(8, 8, 8, 3, 1002,
 120, 120, 4,
 '第一次玩飛盤，希望簡單丟接即可。',
 '請穿方便活動的服裝。',
 1,
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 NULL,
 0, 2, NOW(),
 2,
 '因場地臨時封閉，由平台協助取消。',
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 120,
 NULL, NULL,
 8,
 '公園飛盤陪玩',
 '戶外球類',
 '在公園草地簡單丟接飛盤，不需要經驗，適合想在戶外活動又不想太累的會員。',
 DATE_ADD(NOW(), INTERVAL 10 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 10 DAY), INTERVAL 1 HOUR),
 '台北市', '大安區', '大安森林公園中央草地'),

-- 9：已完成、未撥款
(9, 9, 9, 4, NULL,
 180, 360, 3,
 '希望以輕鬆速度騎河濱路線。',
 '請自備安全帽，單車可現場租借。',
 0,
 DATE_SUB(NOW(), INTERVAL 4 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 2 HOUR),
 0, 0, NULL,
 NULL, NULL, NULL, 0,
 NULL, NULL,
 9,
 '河濱單車同行',
 '慢跑單車',
 '沿河濱自行車道輕鬆騎乘，可依體力調整距離與休息次數，不競速。',
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 2 HOUR),
 '新北市', '八里區', '八里左岸自行車租借站'),

-- 10：已完成、已撥款
(10, 10, 10, 5, 1003,
 250, 750, 3,
 '希望走新手可以完成的大眾步道。',
 '請準備飲水、防曬與防滑鞋。',
 0,
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 3 HOUR),
 1, 0, NOW(),
 NULL, NULL, NULL, 0,
 NULL, NULL,
 10,
 '郊山健行陪伴',
 '登山健行',
 '選擇大眾化的新手步道一起健行，途中可隨時休息，重點是欣賞風景與安全同行。',
 DATE_SUB(NOW(), INTERVAL 2 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 3 HOUR),
 '台北市', '信義區', '象山步道靈雲宮入口');


-- =====================================================
-- 已完成訂單評價假資料
-- =====================================================

UPDATE SERVICE_ORDER
SET
    BUYER_RATE_SELLER = 5,
    BUYER_REVIEW_COMMENT = '行程很輕鬆，途中也會配合休息。',
    BUYER_REVIEWED_AT = NOW(),
    SELLER_RATE_BUYER = 5,
    SELLER_REVIEW_COMMENT = '準時且好相處，整體活動很順利。',
    SELLER_REVIEWED_AT = NOW()
WHERE SERVICE_ORDER_ID = 9;


-- =====================================================
-- SERVICE_REPORT 假資料
-- SERVICE_REPORT 欄位與約束未修改
-- =====================================================

INSERT INTO SERVICE_REPORT
(
    SERVICE_ID,
    REPORTER_MEMBER_ID,
    SERVICE_REPORT_COM,
    SERVICE_REPORT_TIME,
    EMPLOYEE_ID,
    SERVICE_REPORT_HANDLE_TIME,
    SERVICE_REPORT_STATUS
)
VALUES
(1, 6, '服務內容與頁面描述不完全相符。', NOW(), NULL, NULL, 0),

(4, 7, '夜市集合時間與實際約定有落差，平台已完成處理。',
 NOW(), 1004, NOW(), 1),

(8, 9, '查證後未發現違規內容，檢舉不成立。',
 NOW(), 1005, NOW(), 2);


-- =====================================================





-- =====================================================
-- 7. 活動模組：建表用「活動資料庫.txt」，假資料用「活動資料庫假資料.txt」
-- =====================================================
CREATE TABLE ACTIVITY_TYPE (
    ACTIVITY_TYPE_ID INT NOT NULL AUTO_INCREMENT COMMENT '活動類型編號',
    TYPE_NAME VARCHAR(50) NOT NULL COMMENT '類型名稱',
    TYPE_MODE TINYINT NOT NULL COMMENT '類型狀態 (0：動態, 1：靜態)',
    PRIMARY KEY (ACTIVITY_TYPE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活動類型';

CREATE TABLE ACTIVITY (
    ACTIVITY_ID INT NOT NULL AUTO_INCREMENT COMMENT '活動編號',
    ACTIVITY_TYPE_ID INT NOT NULL COMMENT '活動類型編號',
    MEMBER_ID INT NOT NULL COMMENT '活動發起會員編號',
    ACTIVITY_TITLE VARCHAR(100) NOT NULL COMMENT '活動標題',
    ACTIVITY_DESCRIPTION VARCHAR(500) COMMENT '活動描述',
    ACTIVITY_IMAGE LONGBLOB COMMENT '活動圖片',
    ACTIVITY_IMAGE_TYPE VARCHAR(20) COMMENT '活動圖片格式',
    ACTIVITY_PRICE INT DEFAULT 0 COMMENT '活動價格',
    MIN_PARTICIPANTS INT DEFAULT 1 COMMENT '最低人數',
    MAX_PARTICIPANTS INT COMMENT '最高人數',
    ATTENDEES_COUNT INT DEFAULT 0 COMMENT '已參加人數',
    REGISTRATION_STARTTIME DATETIME COMMENT '報名開始日期',
    REGISTRATION_DEADLINE DATETIME COMMENT '報名截止時間',
    ACTIVITY_STATUS TINYINT DEFAULT 0 COMMENT '活動狀態 (0：正常舉行, 1：延期, 2：取消)',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    START_TIME DATETIME NOT NULL COMMENT '開始時間',
    END_TIME DATETIME NOT NULL COMMENT '結束時間',
    PRIMARY KEY (ACTIVITY_ID),
    CONSTRAINT FK_ACTIVITY_TYPE FOREIGN KEY (ACTIVITY_TYPE_ID) REFERENCES ACTIVITY_TYPE(ACTIVITY_TYPE_ID),
    CONSTRAINT FK_ACTIVITY_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER(MEMBER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='揪團活動';


CREATE TABLE ACT_ORDER (
    ACTIVITY_ORDER_ID INT NOT NULL AUTO_INCREMENT COMMENT '活動訂單編號',
    ACTIVITY_ID INT NOT NULL COMMENT '活動編號',
    BUYER_MEMBER_ID INT COMMENT '買方會員編號',
    EMPLOYEE_ID INT COMMENT '員工編號',
   ORDER_STATUS TINYINT DEFAULT 0 COMMENT '訂單狀態 (0：已付款, 1：已取消,  2：待審核, 3：待付款, 4：已完成)',
    BOOKING_COUNT INT DEFAULT 1 COMMENT '報名人數',
    ACTIVITY_PRICE INT COMMENT '活動價格',
    TOTAL_AMOUNT INT COMMENT '訂單總金額',
    ORDER_NOTE VARCHAR(500) COMMENT '訂單備註',
    ACTIVITY_PAYMENT_METHOD TINYINT COMMENT '付款方式 (0：信用卡, 1：ATM轉帳)',
    PAID_AT DATETIME COMMENT '付款時間',
    APPROVED_AT DATETIME COMMENT '主辦審核通過時間',
    ACTIVITY_COMPLETED_AT DATETIME COMMENT '完成時間',
    BUYER_RATE_SELLER TINYINT COMMENT '買方給賣方評分',
    BUYER_REVIEW_COMMENT VARCHAR(500) COMMENT '買方評價內容',
    BUYER_REVIEWED_AT DATETIME COMMENT '買方評價時間',
    SELLER_RATE_BUYER TINYINT COMMENT '賣方給買方評分',
    SELLER_REVIEW_COMMENT VARCHAR(500) COMMENT '賣方評價內容',
    SELLER_REVIEWED_AT DATETIME COMMENT '賣方評價時間',
    PAYOUT_AMOUNT BOOLEAN DEFAULT FALSE COMMENT '撥款狀態 (0：待撥款, 1：已撥款)',
    REFUND_REASON VARCHAR(255) COMMENT '退款原因',
    REFUND_STATUS TINYINT COMMENT '退款狀態 (0：審核中, 1：成立, 2：不成立)',

    PRIMARY KEY (ACTIVITY_ORDER_ID),
    CONSTRAINT FK_ORDER_ACTIVITY FOREIGN KEY (ACTIVITY_ID) REFERENCES ACTIVITY(ACTIVITY_ID),
    CONSTRAINT FK_ORDER_BUYER FOREIGN KEY (BUYER_MEMBER_ID) REFERENCES MEMBER(MEMBER_ID),
    CONSTRAINT FK_ORDER_EMPLOYEE FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活動訂單';

CREATE TABLE ACTIVITY_REPORT (
    ACTIVITY_REPORT_ID INT NOT NULL AUTO_INCREMENT COMMENT '活動檢舉編號',
    ACTIVITY_ID INT NOT NULL COMMENT '活動編號',
    REPORTER_ID INT NOT NULL COMMENT '檢舉者',
    EMPLOYEE_ID INT COMMENT '處理員工編號',
    ACTIVITY_REPORT_TIME DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '檢舉時間',
    REPORT_TYPE TINYINT COMMENT '檢舉分類 (0 詐騙/虛假活動；1色情/不當派對；2暴力/危險活動；3違法行為Ex.聚賭、毒品；4其他)',
    UPDATED DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '狀態更新時間',
    ACTIVITY_REPORT_COM VARCHAR(500) NOT NULL COMMENT '檢舉內容',
    ACTIVITY_REPORT_IMAGE LONGBLOB  COMMENT '證據圖片',
    APPEAL_CONTENT VARCHAR(500) COMMENT '申訴內容',
    APPEAL_IMAGE LONGBLOB  COMMENT '申訴圖片',
    APPEAL_TIME DATETIME COMMENT '提出申訴時間',
    ACTIVITY_REPORT_STATUS TINYINT DEFAULT 0 COMMENT '審核狀態 (0待處理；1檢舉成立/已處罰；2檢舉駁回；3申訴中/調查中；4申訴成功/已撤銷；5申訴駁回/維持原判)',
    PENALTY_TYPE TINYINT DEFAULT 0 COMMENT '懲處類型 (0無/駁回；1記點；)',
    PENALTY_VALUE INT COMMENT '懲處數值',
    REMARK VARCHAR(500) COMMENT '審核備註',
    PRIMARY KEY (ACTIVITY_REPORT_ID),
    CONSTRAINT FK_REPORT_ACTIVITY FOREIGN KEY (ACTIVITY_ID) REFERENCES ACTIVITY(ACTIVITY_ID),
    CONSTRAINT FK_ACT_REPORT_MEMBER FOREIGN KEY (REPORTER_ID) REFERENCES MEMBER(MEMBER_ID),
    CONSTRAINT FK_REPORT_EMPLOYEE FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活動檢舉';



CREATE TABLE GROUP_CHAT_MESSAGE (
    GROUP_CHAT_MESSAGE_ID INT NOT NULL AUTO_INCREMENT COMMENT '群聊訊息編號',
    ACTIVITY_ID INT NOT NULL COMMENT '活動編號',
    SENDER_MEMBER_ID INT NOT NULL COMMENT '發送者會員編號',
    CONTENT VARCHAR(500) NOT NULL COMMENT '訊息內容',
    SENT_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '發送時間',
    PRIMARY KEY (GROUP_CHAT_MESSAGE_ID),
    CONSTRAINT FK_GROUP_CHAT_ACTIVITY FOREIGN KEY (ACTIVITY_ID) REFERENCES ACTIVITY(ACTIVITY_ID),
    CONSTRAINT FK_GROUP_CHAT_SENDER FOREIGN KEY (SENDER_MEMBER_ID) REFERENCES MEMBER(MEMBER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊訊息';


INSERT INTO ACTIVITY_TYPE (ACTIVITY_TYPE_ID, TYPE_NAME, TYPE_MODE) VALUES
(1, '戶外路跑', 0),
(2, '桌遊益智', 1),
(3, '攀岩體驗', 0),
(4, '戶外踏青', 0),
(5, '探店美食', 1),
(6, '看電影/追劇', 1),
(7, '登山露營', 0),
(8, '健身重訓', 1),
(9, '水上活動', 0),
(10, '藝文展覽', 1),
(11, '遊戲電競', 1),
(12, '其他', 1);


INSERT INTO ACTIVITY (
    ACTIVITY_ID, ACTIVITY_TYPE_ID, MEMBER_ID, ACTIVITY_TITLE, ACTIVITY_DESCRIPTION,
    ACTIVITY_IMAGE, ACTIVITY_IMAGE_TYPE,
    ACTIVITY_PRICE, MIN_PARTICIPANTS, MAX_PARTICIPANTS, ATTENDEES_COUNT,
    REGISTRATION_STARTTIME, REGISTRATION_DEADLINE, ACTIVITY_STATUS, START_TIME, END_TIME
) VALUES
(1, 1, 1, '週末大佳河濱公園放風跑', '輕鬆跑 5K，適合初學者，請自行準備飲用水。', NULL, NULL, 0, 3, 10, 3, '2026-07-15 08:00:00', '2026-07-24 18:00:00', 0, '2026-07-26 07:30:00', '2026-07-26 10:00:00'),
(2, 2, 2, '阿瓦隆狼人殺新手團', '新手友善，現場會進行規則教學，預計進行兩至三局。', NULL, NULL, 150, 5, 10, 5, '2026-07-15 12:00:00', '2026-07-29 22:00:00', 0, '2026-08-01 18:00:00', '2026-08-01 21:30:00'),
(3, 3, 4, '內湖室內攀岩抱石體驗', '由現場教練進行基礎教學，費用包含入場與裝備租借。', NULL, NULL, 500, 2, 6, 2, '2026-07-16 09:00:00', '2026-07-30 23:59:59', 0, '2026-08-02 14:00:00', '2026-08-02 17:00:00'),
(4, 2, 5, '派對狼人殺！12人標準局', '採十二人標準配置，建議具備基本狼人殺遊戲經驗。', NULL, NULL, 250, 9, 12, 9, '2026-07-17 10:00:00', '2026-08-05 23:59:59', 0, '2026-08-08 19:00:00', '2026-08-08 22:30:00'),
(5, 12, 2, '信義商圈週年慶血拚特攻隊', '一起逛街並討論優惠組合，消費金額由參加者自行負擔。', NULL, NULL, 0, 2, 4, 2, '2026-07-18 00:00:00', '2026-08-06 23:59:59', 0, '2026-08-09 13:00:00', '2026-08-09 18:00:00'),
(6, 12, 1, '林口三井 Outlet 週末尋寶去', '預計共乘前往，車資平均分攤，回程可一起用餐。', NULL, NULL, 0, 2, 5, 3, '2026-07-20 00:00:00', '2026-08-12 23:59:59', 0, '2026-08-15 10:30:00', '2026-08-15 20:00:00'),
(7, 4, 6, '陽明山擎天崗大草原野餐', '請自行準備餐點與野餐墊，可攜帶寵物但需遵守園區規定。', NULL, NULL, 0, 4, 10, 4, '2026-07-21 00:00:00', '2026-08-13 23:59:59', 0, '2026-08-16 11:00:00', '2026-08-16 16:00:00'),
(8, 4, 2, '象山觀景台夜景輕鬆健行', '因天候因素延期，集合地點為捷運象山站。', NULL, NULL, 0, 2, 8, 2, '2026-07-22 00:00:00', '2026-08-14 23:59:59', 1, '2026-08-22 19:00:00', '2026-08-22 21:30:00'),
(9, 5, 1, '人氣拉麵店排隊探店', '預計提早排隊，餐費由參加者自行支付。', NULL, NULL, 300, 2, 4, 4, '2026-07-23 00:00:00', '2026-08-19 23:59:59', 0, '2026-08-22 11:00:00', '2026-08-22 14:00:00'),
(10, 5, 7, '東區特色咖啡廳甜點下午茶', '適合喜歡甜點與拍照的朋友，餐點費用各自支付。', NULL, NULL, 400, 2, 4, 2, '2026-07-24 00:00:00', '2026-08-20 23:59:59', 0, '2026-08-23 14:00:00', '2026-08-23 17:00:00'),
(11, 6, 2, '熱門動作電影 IMAX 揪團', '活動費用包含電影票，看完電影後可自由參加心得交流。', NULL, NULL, 350, 4, 6, 4, '2026-07-25 00:00:00', '2026-08-24 23:59:59', 0, '2026-08-28 19:30:00', '2026-08-28 23:00:00'),
(12, 6, 1, '深夜恐怖片試膽大會', '因主辦人臨時無法出席，本次活動取消。', NULL, NULL, 280, 2, 4, 0, '2026-07-26 00:00:00', '2026-08-25 23:59:59', 2, '2026-08-29 23:00:00', '2026-08-30 02:00:00'),
(13, 7, 2, '宜蘭山區週末豪華露營', '費用包含營地、住宿設備及早晚餐，交通方式另行討論。', NULL, NULL, 1800, 4, 8, 4, '2026-07-27 00:00:00', '2026-08-26 23:59:59', 0, '2026-09-05 14:00:00', '2026-09-06 12:00:00'),
(14, 8, 10, '重訓新手村：一起練深蹲與硬舉', '以互相協助與動作交流為主，不涉及私人教練課程推銷。', NULL, NULL, 0, 2, 3, 2, '2026-07-28 00:00:00', '2026-09-02 23:59:59', 0, '2026-09-05 18:00:00', '2026-09-05 20:00:00'),
(15, 9, 1, '東北角 SUP 日出體驗團', '由專業教練帶領，費用包含 SUP 板、救生衣及基礎教學。', NULL, NULL, 1200, 4, 10, 4, '2026-07-29 00:00:00', '2026-09-03 23:59:59', 0, '2026-09-06 05:00:00', '2026-09-06 09:00:00'),
(16, 10, 2, '奇美博物館特展參觀行', '於高鐵台南站集合，活動費用包含博物館特展門票。', NULL, NULL, 450, 4, 8, 4, '2026-08-01 00:00:00', '2026-09-09 23:59:59', 0, '2026-09-12 13:00:00', '2026-09-12 18:00:00'),
(17, 11, 1, 'Switch 派對遊戲馬拉松', '預計遊玩瑪利歐派對與任天堂明星大亂鬥，現場提供簡單飲料。', NULL, NULL, 100, 4, 8, 4, '2026-08-02 00:00:00', '2026-09-10 23:59:59', 0, '2026-09-13 18:00:00', '2026-09-13 22:00:00'),
(18, 12, 4, '流浪動物之家週末一日志工', '協助環境清潔、物資整理及照顧動物，請穿著方便活動的服裝。', NULL, NULL, 0, 5, 15, 5, '2026-08-03 00:00:00', '2026-09-16 23:59:59', 0, '2026-09-20 09:00:00', '2026-09-20 17:00:00');

INSERT INTO ACT_ORDER (
    ACTIVITY_ORDER_ID, ACTIVITY_ID, BUYER_MEMBER_ID, EMPLOYEE_ID,
    ORDER_STATUS, BOOKING_COUNT, ACTIVITY_PRICE, TOTAL_AMOUNT,
    ORDER_NOTE, ACTIVITY_PAYMENT_METHOD, PAID_AT, APPROVED_AT
) VALUES
(1, 1, 4, NULL, 0, 2, 0, 0, '與朋友一起參加', 0, '2026-07-16 10:20:00', '2026-07-16 11:00:00'),
(2, 1, 5, NULL, 0, 1, 0, 0, '第一次參加路跑活動', 1, '2026-07-18 14:35:00', '2026-07-18 15:00:00'),
(3, 1, 7, NULL, 3, 1, 0, 0, '尚未完成付款', 0, NULL, '2026-07-20 09:30:00'),
(4, 2, 1, NULL, 0, 2, 150, 300, '兩位新手，希望安排教學', 0, '2026-07-17 12:30:00', '2026-07-17 13:00:00'),
(5, 2, 4, NULL, 0, 2, 150, 300, '有玩過幾次狼人殺', 1, '2026-07-19 18:10:00', '2026-07-19 19:00:00'),
(6, 2, 6, NULL, 0, 1, 150, 150, NULL, 0, '2026-07-22 20:05:00', '2026-07-22 20:30:00'),
(7, 3, 1, NULL, 0, 1, 500, 500, '沒有攀岩經驗', 0, '2026-07-18 09:20:00', '2026-07-18 10:00:00'),
(8, 3, 2, NULL, 0, 1, 500, 500, '需要租借全套裝備', 1, '2026-07-20 15:40:00', '2026-07-20 16:00:00'),
(9, 3, 8, NULL, 1, 1, 500, 500, '臨時有事取消報名', 0, NULL, NULL),
(10, 4, 1, NULL, 0, 3, 250, 750, '三人一起報名', 0, '2026-07-20 11:10:00', '2026-07-20 12:00:00'),
(11, 4, 2, NULL, 0, 2, 250, 500, '兩位玩家都有經驗', 1, '2026-07-22 17:20:00', '2026-07-22 18:00:00'),
(12, 4, 4, NULL, 0, 2, 250, 500, NULL, 0, '2026-07-25 13:30:00', '2026-07-25 14:00:00'),
(13, 4, 6, NULL, 0, 2, 250, 500, '希望不要安排高難度角色', 1, '2026-07-28 19:45:00', '2026-07-28 20:00:00'),
(14, 5, 1, NULL, 0, 1, 0, 0, '想一起湊滿額贈', 0, '2026-07-21 10:00:00', '2026-07-21 10:30:00'),
(15, 5, 4, NULL, 0, 1, 0, 0, NULL, 1, '2026-07-24 15:20:00', '2026-07-24 16:00:00'),
(16, 6, 2, NULL, 0, 2, 0, 0, '兩人同行，可以一起分攤車資', 0, '2026-07-23 09:40:00', '2026-07-23 10:00:00'),
(17, 6, 5, NULL, 0, 1, 0, 0, '可以協助開車', 1, '2026-07-27 18:30:00', '2026-07-27 19:00:00'),
(18, 6, 7, NULL, 2, 1, 0, 0, '等待主辦人審核', NULL, NULL, NULL),
(19, 7, 1, NULL, 0, 2, 0, 0, '會攜帶一隻小型犬', 0, '2026-07-24 12:10:00', '2026-07-24 13:00:00'),
(20, 7, 2, NULL, 0, 1, 0, 0, '會準備水果與飲料', 1, '2026-07-26 16:20:00', '2026-07-26 17:00:00'),
(21, 7, 4, NULL, 0, 1, 0, 0, NULL, 0, '2026-07-29 10:15:00', '2026-07-29 11:00:00'),
(22, 8, 1, NULL, 0, 2, 0, 0, '已知道活動延期', 0, '2026-07-25 19:20:00', '2026-07-25 20:00:00'),
(23, 8, 5, NULL, 1, 1, 0, 0, '延期後時間無法配合', NULL, NULL, NULL),
(24, 9, 2, NULL, 0, 2, 300, 600, '兩人同行，可以接受排隊', 0, '2026-07-27 12:30:00', '2026-07-27 13:00:00'),
(25, 9, 4, NULL, 0, 2, 300, 600, '希望可以坐同一桌', 1, '2026-07-30 20:20:00', '2026-07-30 21:00:00'),
(26, 10, 1, NULL, 0, 1, 400, 400, '想拍甜點照片', 0, '2026-07-28 14:10:00', '2026-07-28 15:00:00'),
(27, 10, 2, NULL, 0, 1, 400, 400, NULL, 1, '2026-08-01 17:30:00', '2026-08-01 18:00:00'),
(28, 11, 1, NULL, 0, 2, 350, 700, '希望安排中間附近的位置', 0, '2026-07-30 11:00:00', '2026-07-30 12:00:00'),
(29, 11, 4, NULL, 0, 2, 350, 700, NULL, 1, '2026-08-03 19:20:00', '2026-08-03 20:00:00'),
(30, 11, 6, NULL, 3, 1, 350, 350, '已通過審核，等待付款', NULL, NULL, '2026-08-05 13:00:00'),
(31, 12, 2, NULL, 1, 2, 280, 560, '活動取消，訂單同步取消', 0, NULL, NULL),
(32, 12, 4, NULL, 1, 1, 280, 280, '主辦人取消活動', 1, NULL, NULL),
(33, 13, 1, NULL, 0, 2, 1800, 3600, '兩人同行，希望安排同一帳篷', 0, '2026-08-01 10:20:00', '2026-08-01 11:00:00'),
(34, 13, 4, NULL, 0, 2, 1800, 3600, '飲食沒有特殊需求', 1, '2026-08-05 18:40:00', '2026-08-05 19:00:00'),
(35, 13, 7, NULL, 2, 1, 1800, 1800, '等待主辦人確認名額', NULL, NULL, NULL),
(36, 14, 1, NULL, 0, 1, 0, 0, '想學習深蹲基本動作', 0, '2026-08-03 09:15:00', '2026-08-03 10:00:00'),
(37, 14, 2, NULL, 0, 1, 0, 0, '沒有硬舉經驗', 1, '2026-08-07 18:30:00', '2026-08-07 19:00:00'),
(38, 15, 2, NULL, 0, 2, 1200, 2400, '兩人都不會游泳，請準備救生衣', 0, '2026-08-05 12:20:00', '2026-08-05 13:00:00'),
(39, 15, 4, NULL, 0, 2, 1200, 2400, '有一次 SUP 經驗', 1, '2026-08-10 16:40:00', '2026-08-10 17:00:00'),
(40, 15, 8, NULL, 3, 1, 1200, 1200, '等待完成付款', NULL, NULL, '2026-08-13 12:00:00'),
(41, 16, 1, NULL, 0, 2, 450, 900, '從台北搭高鐵前往', 0, '2026-08-08 11:30:00', '2026-08-08 12:00:00'),
(42, 16, 4, NULL, 0, 2, 450, 900, '兩人同行', 1, '2026-08-14 19:20:00', '2026-08-14 20:00:00'),
(43, 17, 2, NULL, 0, 2, 100, 200, '兩人都會玩大亂鬥', 0, '2026-08-10 14:10:00', '2026-08-10 15:00:00'),
(44, 17, 4, NULL, 0, 1, 100, 100, '可以攜帶自己的控制器', 1, '2026-08-15 17:20:00', '2026-08-15 18:00:00'),
(45, 17, 5, NULL, 0, 1, 100, 100, NULL, 0, '2026-08-20 20:30:00', '2026-08-20 21:00:00'),
(46, 18, 1, NULL, 0, 2, 0, 0, '兩人都有照顧狗狗的經驗', 0, '2026-08-12 10:20:00', '2026-08-12 11:00:00'),
(47, 18, 2, NULL, 0, 2, 0, 0, '可以協助搬運物資', 1, '2026-08-18 15:40:00', '2026-08-18 16:00:00'),
(48, 18, 5, NULL, 0, 1, 0, 0, '不怕大型犬', 0, '2026-08-25 18:30:00', '2026-08-25 19:00:00'),
(49, 18, 6, NULL, 2, 1, 0, 0, '等待主辦人審核', NULL, NULL, NULL);

/* 依有效訂單重新同步活動報名人數：
   0 = 已付款、4 = 已完成，才計入 ATTENDEES_COUNT */
UPDATE ACTIVITY a
LEFT JOIN (
    SELECT ACTIVITY_ID, SUM(BOOKING_COUNT) AS TOTAL_ATTENDEES
    FROM ACT_ORDER
    WHERE ORDER_STATUS IN (0, 4)
    GROUP BY ACTIVITY_ID
) o ON a.ACTIVITY_ID = o.ACTIVITY_ID
SET a.ATTENDEES_COUNT = COALESCE(o.TOTAL_ATTENDEES, 0);


INSERT INTO ACTIVITY_REPORT (ACTIVITY_REPORT_ID, ACTIVITY_ID, REPORTER_ID, EMPLOYEE_ID, ACTIVITY_REPORT_COM, ACTIVITY_REPORT_STATUS) VALUES 
(1, 1,11, 1, '現場推銷保險', 0),
(2, 2,14, 2, '內文疑似詐騙', 1),
(3, 3,15, 3, '主辦人無故延期且不回應', 2),
(201, 4,12, 1002, '色情內容', 2),
(202, 5,13, 1002, '暴力內容', 2),
(203, 6,11, 1005, '就是想檢舉', 1),
(204, 7,13, 1005, '不符合活動內容', 0),
(205, 8,14, 1002, '不喜歡', 0);


INSERT INTO GROUP_CHAT_MESSAGE (GROUP_CHAT_MESSAGE_ID, ACTIVITY_ID , SENDER_MEMBER_ID, CONTENT) VALUES 
(1, 1, 1, '記得帶水和毛巾喔！'),
(2, 1, 2, '收到，大佳河濱公園集合嗎？'),
(3, 2, 2, '桌遊團再徵 2 人滿團！');

-- =====================================================
-- 8. 場地模組：沿用新版「場地txt.txt」
-- 注意：原本 MEMBER_ID = 1001，整合後改成 MEMBER_ID = 8（John，場地主管理員）
-- 注意：新版會用 WITH RECURSIVE 依 CURDATE() 自動產生未來 16 天的 VENUE_SLOT
-- =====================================================
CREATE TABLE VENUE_TYPE (
    VENUE_TYPE_ID INT AUTO_INCREMENT NOT NULL,
    TYPE_NAME     VARCHAR(50),
    TYPE_DESC     VARCHAR(255),
    TYPE_MODE     TINYINT,
    CONSTRAINT VENUE_TYPE_ID_PK PRIMARY KEY (VENUE_TYPE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='場地類型';

INSERT INTO VENUE_TYPE VALUES
(1, '運動', '', 0),
(2, '桌遊', '', 1),
(3, '活動', '', 0),
(4, '會議', '', 0),
(5, '其他', '', 0);

CREATE TABLE VENUE (
    VENUE_ID                INT AUTO_INCREMENT NOT NULL,
    MEMBER_ID               INT NOT NULL,
    VENUE_TYPE_ID           INT NOT NULL,
    VENUE_NAME              VARCHAR(100),
    ADDRESS                 VARCHAR(255),
    CAPACITY                INT,
    HOURLY_RATE             INT,
    VENUE_STATUS            TINYINT DEFAULT 0,
    CREATED_AT              DATETIME DEFAULT CURRENT_TIMESTAMP,
    DEFAULT_OPEN_DAYS       VARCHAR(7),
    DEFAULT_AVAILABLE_HOURS VARCHAR(24),
    TOTAL_RATING_STARS      INT DEFAULT 0,
    RATING_COUNT            INT DEFAULT 0,
    VENUE_DESCRIPTION       VARCHAR(255),
    CONSTRAINT VENUE_ID_PK PRIMARY KEY (VENUE_ID),
    CONSTRAINT MEMBER_ID_VENUE_FK FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER (MEMBER_ID),
    CONSTRAINT VENUE_ID_TYPE_VENUE_FK FOREIGN KEY (VENUE_TYPE_ID) REFERENCES VENUE_TYPE (VENUE_TYPE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 2001 COMMENT='場地資源';

INSERT INTO VENUE (VENUE_ID, MEMBER_ID, VENUE_TYPE_ID, VENUE_NAME, ADDRESS, CAPACITY, HOURLY_RATE, VENUE_STATUS, DEFAULT_OPEN_DAYS, DEFAULT_AVAILABLE_HOURS, VENUE_DESCRIPTION, TOTAL_RATING_STARS, RATING_COUNT) VALUES
(2001, 8, 1, '羽球場', '臺中市北區北屯路99號', 10, 500, 1, '1111111', '222222220000000000002222', '超讚的場地', 5, 1),
(2002, 8, 2, '桌遊店', '臺北市中山區123號', 10, 1000, 2, '1111111', '222222220000000000002222', '超讚的場地', 0, 0),
(2003, 8, 4, '會議室', '桃園市中壢區123號', 30, 1500, 1, '0111001', '222222220000000000002222', '超讚的場地', 5, 1),
(2004, 11, 3, '大場地', '臺中市北區北屯路99號', 100, 5000, 1, '1111111', '222222220000000000002222', '超讚的場地', 0, 0),
(2005, 12, 1, '桌球場', '臺中市北區北屯路99號', 15, 500, 1, '1111111', '222222220000000000002222', '超讚的場地', 0, 0),
(2006, 13, 3, '活動場地', '臺中市北區北屯路99號', 50, 500, 1, '1111111', '222222220000000000002222', '超讚的場地', 0, 0),
(2007, 14, 1, '桌遊場地', '臺中市北區北屯路99號', 20, 500, 1, '1111111', '222222220000000000002222', '超讚的場地', 0, 0);



CREATE TABLE VENUE_SLOT (
    VENUE_SLOT_ID INT AUTO_INCREMENT NOT NULL,
    VENUE_ID      INT NOT NULL,
    SLOT_DATE     DATE,
    SLOT_STATUS   VARCHAR(24) DEFAULT '000000000000000000000000',
    CONSTRAINT VENUE_SLOT_ID_PK PRIMARY KEY (VENUE_SLOT_ID),
    CONSTRAINT VENUE_ID_VENUE_SLOT_FK FOREIGN KEY (VENUE_ID) REFERENCES VENUE(VENUE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 3001 COMMENT='場地時段預約';

CREATE TABLE VENUE_REVIEW (
    VENUE_REVIEW_ID INT AUTO_INCREMENT NOT NULL,
    VENUE_ID        INT NOT NULL,
    EMPLOYEE_ID     INT,
    REVIEW_STATUS   TINYINT NOT NULL DEFAULT 0,
    REVIEW_NOTE     VARCHAR(255),
    REVIEWED_AT     DATETIME,
    CONSTRAINT VENUE_REVIEW_ID_PK PRIMARY KEY (VENUE_REVIEW_ID),
    CONSTRAINT VENUE_ID_VENUE_REVIEW_FK FOREIGN KEY (VENUE_ID) REFERENCES VENUE(VENUE_ID),
    CONSTRAINT EMPLOYEE_ID_VENUE_REVIEW_FK FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE (EMPLOYEE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 4001 COMMENT='場地審核';

INSERT INTO VENUE_REVIEW (VENUE_ID, EMPLOYEE_ID, REVIEW_STATUS, REVIEW_NOTE, REVIEWED_AT) VALUES
(2001, 7001, 1, '審核通過', '2026-05-23 22:50:27'),
(2002, 7001, 2, '地址不完整', '2026-05-23 22:53:27'),
(2003, 7001, 0, NULL, NULL);

CREATE TABLE VENUE_IMAGES(
    VENUE_IMAGES_ID INT AUTO_INCREMENT NOT NULL,
    VENUE_ID        INT NOT NULL,
    VENUE_IMAGES    LONGBLOB,
    VENUE_COVER     TINYINT,
    CONSTRAINT VENUE_IMAGES_ID_PK PRIMARY KEY (VENUE_IMAGES_ID),
    CONSTRAINT VENUE_ID_VENUE_IMAGES_FK FOREIGN KEY (VENUE_ID) REFERENCES VENUE(VENUE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 5001 COMMENT='場地圖片';

CREATE TABLE VENUE_ORDER(
    VENUE_ORDER_ID       INT AUTO_INCREMENT NOT NULL,
    VENUE_ID             INT NOT NULL,
    MEMBER_ID            INT NOT NULL,
    EMPLOYEE_ID          INT,
    VENUE_RATING         INT,
    VENUE_COMMENT        VARCHAR(255),
    PAYOUT_AMOUNT        TINYINT DEFAULT 0,
    REFUND_REASON        VARCHAR(255),
    REFUND_STATUS        TINYINT DEFAULT 2,
    HANDLED_AT           DATETIME,
    CREATED_AT           DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    BOOKING_DATE		 DATE,
    START_AT             TIME,
    END_AT               TIME,
    TOTAL_AMOUNT         INT,
    VENUE_PAYMENT_METHOD TINYINT,
    ORDER_STATUS         TINYINT DEFAULT 0,
    VENUE_SLOT_ID        INT,
    CONSTRAINT VENUE_ORDER_ID_PK PRIMARY KEY (VENUE_ORDER_ID),
    CONSTRAINT VENUE_ID_VENUE_ORDER_FK FOREIGN KEY (VENUE_ID) REFERENCES VENUE(VENUE_ID),
    CONSTRAINT MEMBER_ID_VENUE_ORDER_FK FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER (MEMBER_ID),
    CONSTRAINT EMPLOYEE_ID_VENUE_ORDER_FK FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE (EMPLOYEE_ID),
    CONSTRAINT VENUE_SLOT_ID_VENUE_ORDER_FK FOREIGN KEY (VENUE_SLOT_ID) REFERENCES VENUE_SLOT (VENUE_SLOT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 6001 COMMENT='場地訂單';

INSERT INTO VENUE_ORDER (VENUE_ID, MEMBER_ID, VENUE_RATING, VENUE_COMMENT, START_AT, END_AT, TOTAL_AMOUNT, VENUE_PAYMENT_METHOD, BOOKING_DATE,ORDER_STATUS) VALUES
(2001, 8, 5, '乾淨', '13:00:00', '15:00:00', 1000, 0, '2026-07-12', 3);
INSERT INTO VENUE_ORDER (VENUE_ID, MEMBER_ID, EMPLOYEE_ID, REFUND_REASON, REFUND_STATUS, START_AT, END_AT, TOTAL_AMOUNT, VENUE_PAYMENT_METHOD, BOOKING_DATE,ORDER_STATUS) VALUES
(2002, 8, 7001, '找到更好的場地', 0, '15:00:00', '17:00:00', 1000, 1, '2026-07-10', 4);
INSERT INTO VENUE_ORDER (VENUE_ID, MEMBER_ID, EMPLOYEE_ID, VENUE_RATING, VENUE_COMMENT, START_AT, END_AT, TOTAL_AMOUNT, VENUE_PAYMENT_METHOD, BOOKING_DATE,ORDER_STATUS) VALUES
(2003, 8, 7001, 5, '乾淨', '15:00:00', '17:00:00', 1000, 1, '2026-07-10', 3);


CREATE TABLE VENUE_REPORT (
    VENUE_REPORT_ID  INT          NOT NULL AUTO_INCREMENT COMMENT '場地檢舉編號',
    VENUE_ORDER_ID   INT          NOT NULL                COMMENT '場地訂單編號',
    SER_REPORT_COM   VARCHAR(500)                         COMMENT '檢舉內容',
    SER_REPORT_TIME  DATETIME     NOT NULL                COMMENT '檢舉時間',
    REPORT_STATUS    TINYINT      NOT NULL DEFAULT 0      COMMENT '0審核中 1審核通過 2審核未通過',
    EMPLOYEE_ID      INT                                  COMMENT '處理員工編號',
    HANDLED_AT       DATETIME                             COMMENT '處理完成時間',
    PRIMARY KEY (VENUE_REPORT_ID),
    CONSTRAINT FK_VENUE_REPORT_ORDER
        FOREIGN KEY (VENUE_ORDER_ID) REFERENCES VENUE_ORDER (VENUE_ORDER_ID),
    CONSTRAINT FK_VENUE_REPORT_EMPLOYEE
        FOREIGN KEY (EMPLOYEE_ID)    REFERENCES EMPLOYEE (EMPLOYEE_ID)
) AUTO_INCREMENT = 8001;

INSERT INTO VENUE_REPORT (VENUE_ORDER_ID, SER_REPORT_COM, SER_REPORT_TIME, REPORT_STATUS)
VALUES
(6001, '場地實際狀況與網站照片嚴重不符，冷氣完全不能運作。', '2026-07-01 14:30:00', 0),
(6002, '現場環境髒亂，垃圾未清理，地板有明顯污漬。',       '2026-07-03 09:15:00', 0),
(6003, '投影設備損壞無法使用，影響活動進行。',             '2026-07-05 20:05:00', 0);


-- 排程每天新增一筆資料
-- 注意：整合版先保留為註解，不會自動執行；需要啟用 MySQL Event Scheduler 時再打開。
-- DROP EVENT IF EXISTS auto_generate_venue_slots;
-- CREATE EVENT auto_generate_venue_slots
-- ON SCHEDULE EVERY 1 DAY
-- STARTS '2026-05-25 00:00:00'        -- 每天晚上 0 點執行
-- ON COMPLETION PRESERVE
-- DO
-- INSERT INTO VENUE_SLOT (VENUE_ID, SLOT_DATE, SLOT_STATUS)
-- WITH RECURSIVE dates (v_date) AS
-- (
--    SELECT CURDATE()
--    UNION ALL
--    SELECT v_date + INTERVAL 1 DAY
--    FROM dates
--    WHERE v_date + INTERVAL 1 DAY <= ADDDATE(CURDATE(), INTERVAL 14 DAY)
-- )
-- SELECT b.VENUE_ID,
--        d.v_date,
--        IF(SUBSTR(b.DEFAULT_OPEN_DAYS, WEEKDAY(d.v_date)+1, 1) = '1',
--           b.DEFAULT_AVAILABLE_HOURS,
--           REPEAT('2', 24)) AS SLOT_STATUS
-- FROM dates d
-- CROSS JOIN VENUE b
-- LEFT JOIN VENUE_SLOT r
--   ON d.v_date = r.SLOT_DATE
--  AND b.VENUE_ID = r.VENUE_ID
-- WHERE r.SLOT_DATE IS NULL;

-- 手動新增預約時段資料
-- 這段會依照目前日期 CURDATE()，為每個場地建立今天到 15 天後的時段資料。
INSERT INTO VENUE_SLOT (VENUE_ID, SLOT_DATE, SLOT_STATUS)
WITH RECURSIVE dates (v_date) AS
(
   SELECT CURDATE()
   UNION ALL
   SELECT v_date + INTERVAL 1 DAY
   FROM dates
   WHERE v_date + INTERVAL 1 DAY <= ADDDATE(CURDATE(), INTERVAL 15 DAY)
)
SELECT b.VENUE_ID,
       d.v_date,
       IF(SUBSTR(b.DEFAULT_OPEN_DAYS, WEEKDAY(d.v_date)+1, 1) = '1',
          b.DEFAULT_AVAILABLE_HOURS,
          REPEAT('2', 24)) AS SLOT_STATUS
FROM dates d
CROSS JOIN VENUE b
LEFT JOIN VENUE_SLOT r
  ON d.v_date = r.SLOT_DATE
 AND b.VENUE_ID = r.VENUE_ID
WHERE r.SLOT_DATE IS NULL;


-- 預約2026-05-26 13~15時段
-- UPDATE VENUE_SLOT
-- SET SLOT_STATUS = CONCAT(SUBSTR(SLOT_STATUS, 1, 13), '11', SUBSTR(SLOT_STATUS, 16))
-- WHERE VENUE_ID = 2001
--   AND SLOT_DATE = '2026-05-26';

-- -- 預約2026-05-26 15~17時段
-- UPDATE VENUE_SLOT
-- SET SLOT_STATUS = CONCAT(SUBSTR(SLOT_STATUS, 1, 15), '11', SUBSTR(SLOT_STATUS, 18))
-- WHERE VENUE_ID = 2002
--   AND SLOT_DATE = '2026-05-26';

-- =====================================================
-- 9. 一對一聊天室：沿用 ZH.txt
-- =====================================================
CREATE TABLE CHAT_ROOM
( CHAT_ROOM_ID INT NOT NULL,
  MEMBER1_ID   INT NOT NULL,
  MEMBER2_ID   INT NOT NULL,
  CREATED_AT   DATETIME,
  CONSTRAINT CHATROOM_ID_PK PRIMARY KEY (CHAT_ROOM_ID),
  CONSTRAINT CHATROOM_MEM1ID_FK FOREIGN KEY(MEMBER1_ID) REFERENCES MEMBER(MEMBER_ID),
  CONSTRAINT CHATROOM_MEM2ID_FK FOREIGN KEY(MEMBER2_ID) REFERENCES MEMBER(MEMBER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室';

INSERT INTO CHAT_ROOM VALUES
(100, 1, 2, '2026-05-20'),
(101, 2, 3, '2026-05-21'),
(102, 3, 4, '2026-04-22'),
(103, 1, 4, '2026-03-23'),
(104, 2, 5, '2026-04-25');

CREATE TABLE CHAT_MESSAGE
( MESSAGE_ID       INT AUTO_INCREMENT NOT NULL,
  RECEIVER_MEMBER_ID     INT NOT NULL,
  SENDER_MEMBER_ID INT NOT NULL,
  CONTENT          VARCHAR(255),
  SENT_AT          DATETIME,
  IS_READ          TINYINT,
  CONSTRAINT CHATMES_ID_PK PRIMARY KEY (MESSAGE_ID),
  CONSTRAINT CHATMES_RECEIVER_MEMBER_ID_FK FOREIGN KEY(RECEIVER_MEMBER_ID) REFERENCES MEMBER(MEMBER_ID),
  CONSTRAINT CHATMES_SENDER_MEMBER_ID_FK FOREIGN KEY(SENDER_MEMBER_ID) REFERENCES MEMBER(MEMBER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天訊息';

INSERT INTO CHAT_MESSAGE VALUES
(221, 14, 11, 'ABC', '2026-05-20 10:30:15', 1),
(222, 11, 14, 'CDE', '2026-05-20 10:35:40', 0),
(223, 11, 15, 'FFFFFF', '2026-04-20 20:01:10', 1),
(224, 15, 11, 'CCCCCCCC', '2026-04-25 07:55:11', 0),
(225, 11, 15, 'RRRRR', '2026-04-26 13:02:36', 1);


-- =====================================================
-- 10. 平台表格：沿用「平台表格.txt」結構
-- ===================================================== 		

-- =============================================
-- 公告 (BULLETIN) 表格
-- =============================================

CREATE TABLE BULLETIN (
    BULLETIN_ID  INT           NOT NULL AUTO_INCREMENT COMMENT '公告編號',
    TITLE        VARCHAR(50)   NOT NULL               COMMENT '公告標題',
    CONTENT      VARCHAR(500)                         COMMENT '公告內容',
    STATUS       TINYINT       NOT NULL DEFAULT 0     COMMENT '公告狀態：0=草稿, 1=已發布',
    PUBLISH_DATE DATE                                 COMMENT '發布日期（第一次發布時寫入，之後不再更動）',
    EMPLOYEE_ID  INT                                  COMMENT '員工編號 (FK)',
    CREATED_AT   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    UPDATED_AT   DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間（系統自動填入）',
    TAGS         VARCHAR(50)                         COMMENT '標籤，用於搜尋與分類，可選填',

    PRIMARY KEY (BULLETIN_ID),
    CONSTRAINT FK_BULLETIN_EMPLOYEE
        FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID)
) COMMENT = '公告'; 	

INSERT INTO BULLETIN (TITLE, CONTENT, STATUS, PUBLISH_DATE, EMPLOYEE_ID, TAGS) VALUES
('系統維護公告', '本平台將於2024年3月1日凌晨2時至4時進行系統維護，期間服務將暫停使用，造成不便敬請見諒。', 1, '2024-03-01', 1, '系統維護,停機公告'),
('春節假期公告', '本平台春節假期為2024年2月8日至2月14日，假期間客服回覆將延遲，請會員耐心等候。', 1, '2024-02-01', 2, '春節,假期公告'),
('新功能上線預告', '本平台即將推出揪團活動新功能，預計於2024年4月上線，敬請期待。', 0, NULL, 3, '新功能,揪團');

-- =============================================
-- 平台規範 (PLATFORM_SPECIFICATION) 表格
-- =============================================

CREATE TABLE PLATFORM_SPECIFICATION (
    SPEC_ID     INT          NOT NULL AUTO_INCREMENT COMMENT '規範編號',
    SPEC_TYPE   TINYINT      NOT NULL               COMMENT '規範類型：0=帳號與會員規範, 1=付款與退款規範, 2=服務規範, 3=揪團活動規範, 4=場地規範, 5=檢舉與爭議處理規範, 6=安全與隱私規範, 7=其他',
    TITLE       VARCHAR(50)  NOT NULL               COMMENT '規範標題',
    DESCRIPTION VARCHAR(500)                        COMMENT '規範內容',
    STATUS      TINYINT      NOT NULL DEFAULT 0     COMMENT '規範狀態：0=草稿, 1=已發布',
    EMPLOYEE_ID INT                                 COMMENT '員工編號 (FK)',
    CREATED_AT  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    UPDATED_AT  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    PRIMARY KEY (SPEC_ID),
    CONSTRAINT FK_SPEC_EMPLOYEE
        FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID)
) COMMENT = '平台規範';

INSERT INTO PLATFORM_SPECIFICATION (SPEC_TYPE, TITLE, DESCRIPTION, STATUS, EMPLOYEE_ID) VALUES
(0, '會員註冊規範', '會員註冊時須填寫真實姓名與有效電子郵件，每人限註冊一組帳號，禁止使用他人資料申請帳號，違者將停權處理。', 1, 1),
(6, '個人資料保護規範', '本平台依據個人資料保護法蒐集、處理及利用會員個人資料，所有資料僅供平台內部使用，未經會員同意不得提供予第三方。會員可隨時申請查閱、更正或刪除個人資料。', 1, 2),
(7, '智慧財產權規範', '本平台所有內容，包含文字、圖片、設計及程式碼，均受著作權法保護，未經本平台書面授權，禁止任何形式之複製、轉載或商業使用，違者將依法追究相關責任。', 1, 2);

-- =============================================
-- FAQ 表格
-- =============================================

CREATE TABLE FAQ (
    FAQ_ID      INT          NOT NULL AUTO_INCREMENT COMMENT 'FAQ編號，唯一識別FAQ',
    FAQ_TYPE    TINYINT      NOT NULL               COMMENT 'FAQ類型：0=帳號問題, 1=訂單問題, 2=服務問題, 3=揪團活動問題, 4=場地租借問題, 5=檢舉與爭議, 6=系統操作問題, 7=其他',
    QUESTION    VARCHAR(500) NOT NULL               COMMENT '問題，FAQ問題內容',
    ANSWER      VARCHAR(500)                        COMMENT 'FAQ的詳細回答',
    STATUS      TINYINT      NOT NULL DEFAULT 0     COMMENT 'FAQ狀態：0=草稿, 1=已發布',
    EMPLOYEE_ID INT                                 COMMENT '員工編號 (FK)',
    CREATED_AT  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間（系統自動填入）',
    UPDATED_AT  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間（系統自動填入）',

    PRIMARY KEY (FAQ_ID),
    CONSTRAINT FK_FAQ_EMPLOYEE
        FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID)
) COMMENT = 'FAQ';

INSERT INTO FAQ (FAQ_TYPE, QUESTION, ANSWER, STATUS, EMPLOYEE_ID) VALUES
(0, '忘記密碼該如何處理？', '請至登入頁面點選「忘記密碼」，輸入註冊時使用的電子郵件，系統將寄送重設密碼連結至您的信箱，連結有效期限為30分鐘。', 1, 1),
(6, '如何修改個人資料？', '登入帳號後至右上角「個人設定」頁面，即可修改姓名、聯絡電話、電子郵件等基本資料，修改完成後請點選「儲存」按鈕，系統將即時更新您的資料。', 1, 1),
(7, '本平台的服務適用地區為何？', '本平台目前服務範圍僅限台灣地區，包含台灣本島及離島。海外地區目前暫不提供服務，未來若有擴展計畫將於公告頁面另行通知。', 0, 2);


















