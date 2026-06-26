package com.webond.service.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webond.service.model.ServiceSlotService;
import com.webond.service.model.ServiceSlotVO;

@RestController
@RequestMapping("/api/service-slots")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceSlotApiController {

    private final ServiceSlotService serviceSlotSvc;

    public ServiceSlotApiController(ServiceSlotService serviceSlotSvc) {
        this.serviceSlotSvc = serviceSlotSvc;
    }

    // =========================
    // 查全部服務時段
    // GET /api/service-slots
    // =========================
    @GetMapping
    public ResponseEntity<List<ServiceSlotDTO>> getAll() {
        List<ServiceSlotDTO> list = serviceSlotSvc.getAll()
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(list);
    }

    // =========================
    // 查單一服務時段
    // GET /api/service-slots/{serviceSlotId}
    // =========================
    @GetMapping("/{serviceSlotId}")
    public ResponseEntity<?> getOne(@PathVariable Integer serviceSlotId) {
        ServiceSlotVO serviceSlotVO = serviceSlotSvc.getOneServiceSlot(serviceSlotId);

        if (serviceSlotVO == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "查無此服務時段"));
        }

        return ResponseEntity.ok(toDTO(serviceSlotVO));
    }

    // =========================
    // 依服務編號查該服務底下所有時段
    // GET /api/service-slots/service/{serviceId}
    // =========================
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ServiceSlotDTO>> getByServiceId(@PathVariable Integer serviceId) {
        List<ServiceSlotDTO> list = serviceSlotSvc.getByServiceId(serviceId)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(list);
    }

    // =========================
    // 依服務編號查可預約時段
    // GET /api/service-slots/service/{serviceId}/available
    // slotStatus = 0 代表可預約
    // =========================
    @GetMapping("/service/{serviceId}/available")
    public ResponseEntity<List<ServiceSlotDTO>> getAvailableByServiceId(@PathVariable Integer serviceId) {
        List<ServiceSlotDTO> list = serviceSlotSvc.getByServiceId(serviceId)
                .stream()
                .filter(slot -> slot.getSlotStatus() != null && slot.getSlotStatus() == 0)
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(list);
    }

    // =========================
    // 新增服務時段
    // POST /api/service-slots
    // =========================
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody ServiceSlotRequest request) {
        Map<String, String> errorMsgs = validateRequest(request);

        if (!errorMsgs.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMsgs);
        }

        ServiceSlotVO serviceSlotVO = serviceSlotSvc.add(
                request.getServiceId(),
                parseDateTime(request.getStartTime()),
                parseDateTime(request.getEndTime()),
                request.getSlotStatus(),
                parseDateTimeAllowNull(request.getLockExpiresAt())
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(serviceSlotVO));
    }

    // =========================
    // 修改服務時段
    // PUT /api/service-slots/{serviceSlotId}
    // =========================
    @PutMapping("/{serviceSlotId}")
    public ResponseEntity<?> update(
            @PathVariable Integer serviceSlotId,
            @RequestBody ServiceSlotRequest request) {

        Map<String, String> errorMsgs = validateRequest(request);

        if (!errorMsgs.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMsgs);
        }

        ServiceSlotVO oldVO = serviceSlotSvc.getOneServiceSlot(serviceSlotId);

        if (oldVO == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "查無此服務時段"));
        }

        ServiceSlotVO serviceSlotVO = serviceSlotSvc.update(
                serviceSlotId,
                request.getServiceId(),
                parseDateTime(request.getStartTime()),
                parseDateTime(request.getEndTime()),
                request.getSlotStatus(),
                parseDateTimeAllowNull(request.getLockExpiresAt())
        );

        return ResponseEntity.ok(toDTO(serviceSlotVO));
    }

    // =========================
    // 刪除服務時段
    // DELETE /api/service-slots/{serviceSlotId}
    // =========================
    @DeleteMapping("/{serviceSlotId}")
    public ResponseEntity<?> delete(@PathVariable Integer serviceSlotId) {
        ServiceSlotVO oldVO = serviceSlotSvc.getOneServiceSlot(serviceSlotId);

        if (oldVO == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "查無此服務時段"));
        }

        serviceSlotSvc.delete(serviceSlotId);

        return ResponseEntity.ok(Map.of("message", "刪除成功"));
    }

    // =========================
    // VO 轉 DTO
    // 重點：不要把 Hibernate 關聯物件直接丟給前端
    // =========================
    private ServiceSlotDTO toDTO(ServiceSlotVO vo) {
        ServiceSlotDTO dto = new ServiceSlotDTO();

        dto.setServiceSlotId(vo.getServiceSlotId());
        dto.setServiceId(vo.getServiceId());
        dto.setStartTime(formatDateTime(vo.getStartTime()));
        dto.setEndTime(formatDateTime(vo.getEndTime()));
        dto.setSlotStatus(vo.getSlotStatus());
        dto.setSlotStatusText(convertSlotStatusText(vo.getSlotStatus()));
        dto.setLockExpiresAt(formatDateTime(vo.getLockExpiresAt()));

        return dto;
    }

    private String convertSlotStatusText(Byte slotStatus) {
        if (slotStatus == null) {
            return "未知";
        }

        return switch (slotStatus) {
            case 0 -> "可預約";
            case 1 -> "暫時鎖定";
            case 2 -> "已預約";
            default -> "未知";
        };
    }

    // =========================
    // 驗證 RequestBody
    // =========================
    private Map<String, String> validateRequest(ServiceSlotRequest request) {
        Map<String, String> errorMsgs = new LinkedHashMap<>();

        if (request == null) {
            errorMsgs.put("request", "請求內容不可為空");
            return errorMsgs;
        }

        if (request.getServiceId() == null) {
            errorMsgs.put("serviceId", "服務編號請勿空白");
        }

        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            errorMsgs.put("startTime", "開始時間請勿空白");
        }

        if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
            errorMsgs.put("endTime", "結束時間請勿空白");
        }

        if (request.getSlotStatus() == null) {
            errorMsgs.put("slotStatus", "時段狀態請勿空白");
        } else if (request.getSlotStatus() < 0 || request.getSlotStatus() > 2) {
            errorMsgs.put("slotStatus", "時段狀態只能是 0、1、2");
        }

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        try {
            if (request.getStartTime() != null && !request.getStartTime().trim().isEmpty()) {
                startTime = parseDateTime(request.getStartTime());
            }
        } catch (Exception e) {
            errorMsgs.put("startTime", "開始時間格式錯誤");
        }

        try {
            if (request.getEndTime() != null && !request.getEndTime().trim().isEmpty()) {
                endTime = parseDateTime(request.getEndTime());
            }
        } catch (Exception e) {
            errorMsgs.put("endTime", "結束時間格式錯誤");
        }

        try {
            if (request.getLockExpiresAt() != null && !request.getLockExpiresAt().trim().isEmpty()) {
                parseDateTime(request.getLockExpiresAt());
            }
        } catch (Exception e) {
            errorMsgs.put("lockExpiresAt", "鎖定到期時間格式錯誤");
        }

        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            errorMsgs.put("endTime", "結束時間必須晚於開始時間");
        }

        return errorMsgs;
    }

    // =========================
    // 字串轉 LocalDateTime
    // 支援：
    // 2026-06-20T14:00
    // 2026-06-20T14:00:00
    // 2026-06-20 14:00:00
    // =========================
    private LocalDateTime parseDateTime(String value) {
        String text = value.trim();

        if (text.length() == 16 && text.contains("T")) {
            return LocalDateTime.parse(
                    text,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            );
        }

        if (text.length() == 19 && text.contains("T")) {
            return LocalDateTime.parse(
                    text,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            );
        }

        return LocalDateTime.parse(
                text,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    private LocalDateTime parseDateTimeAllowNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return parseDateTime(value);
    }

    // =========================
    // LocalDateTime 轉字串
    // 這樣前端 Vue 比較好接
    // =========================
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    // =========================
    // Request DTO
    // 前端送進來的 JSON 用這個接
    // =========================
    public static class ServiceSlotRequest {
        private Integer serviceId;
        private String startTime;
        private String endTime;
        private Byte slotStatus;
        private String lockExpiresAt;

        public Integer getServiceId() {
            return serviceId;
        }

        public void setServiceId(Integer serviceId) {
            this.serviceId = serviceId;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public Byte getSlotStatus() {
            return slotStatus;
        }

        public void setSlotStatus(Byte slotStatus) {
            this.slotStatus = slotStatus;
        }

        public String getLockExpiresAt() {
            return lockExpiresAt;
        }

        public void setLockExpiresAt(String lockExpiresAt) {
            this.lockExpiresAt = lockExpiresAt;
        }
    }

    // =========================
    // Response DTO
    // 回傳給 Vue / Postman 的乾淨資料
    // 不包含 Hibernate 關聯物件，避免 no session
    // =========================
    public static class ServiceSlotDTO {
        private Integer serviceSlotId;
        private Integer serviceId;
        private String startTime;
        private String endTime;
        private Byte slotStatus;
        private String slotStatusText;
        private String lockExpiresAt;

        public Integer getServiceSlotId() {
            return serviceSlotId;
        }

        public void setServiceSlotId(Integer serviceSlotId) {
            this.serviceSlotId = serviceSlotId;
        }

        public Integer getServiceId() {
            return serviceId;
        }

        public void setServiceId(Integer serviceId) {
            this.serviceId = serviceId;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public Byte getSlotStatus() {
            return slotStatus;
        }

        public void setSlotStatus(Byte slotStatus) {
            this.slotStatus = slotStatus;
        }

        public String getSlotStatusText() {
            return slotStatusText;
        }

        public void setSlotStatusText(String slotStatusText) {
            this.slotStatusText = slotStatusText;
        }

        public String getLockExpiresAt() {
            return lockExpiresAt;
        }

        public void setLockExpiresAt(String lockExpiresAt) {
            this.lockExpiresAt = lockExpiresAt;
        }
    }
}