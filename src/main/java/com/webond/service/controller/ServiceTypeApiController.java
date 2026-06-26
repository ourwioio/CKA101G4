package com.webond.service.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webond.service.model.ServiceTypeService;
import com.webond.service.model.ServiceTypeVO;

@RestController
@RequestMapping("/api/service-types")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceTypeApiController {

    private final ServiceTypeService serviceTypeSvc;

    public ServiceTypeApiController(ServiceTypeService serviceTypeSvc) {
        this.serviceTypeSvc = serviceTypeSvc;
    }

    // =========================
    // 查全部服務類型
    // GET /api/service-types
    // =========================
    @GetMapping
    public ResponseEntity<List<ServiceTypeDTO>> getAll() {
        List<ServiceTypeDTO> list = serviceTypeSvc.getAll()
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(list);
    }

    // =========================
    // 查單一服務類型
    // GET /api/service-types/{serviceTypeId}
    // =========================
    @GetMapping("/{serviceTypeId}")
    public ResponseEntity<?> getOne(@PathVariable Integer serviceTypeId) {
        ServiceTypeVO serviceTypeVO = serviceTypeSvc.findByPK(serviceTypeId);

        if (serviceTypeVO == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "查無此服務類型"));
        }

        return ResponseEntity.ok(toDTO(serviceTypeVO));
    }

    // =========================
    // 新增服務類型
    // POST /api/service-types
    // =========================
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody ServiceTypeRequest request) {
        Map<String, String> errorMsgs = validateRequest(request);

        if (!errorMsgs.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMsgs);
        }

        ServiceTypeVO serviceTypeVO = serviceTypeSvc.add(
                request.getTypeName().trim(),
                request.getDescription().trim(),
                request.getTypeMode(),
                request.getImgURL().trim()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(serviceTypeVO));
    }

    // =========================
    // 修改服務類型
    // PUT /api/service-types/{serviceTypeId}
    // =========================
    @PutMapping("/{serviceTypeId}")
    public ResponseEntity<?> update(
            @PathVariable Integer serviceTypeId,
            @RequestBody ServiceTypeRequest request) {

        ServiceTypeVO oldVO = serviceTypeSvc.findByPK(serviceTypeId);

        if (oldVO == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "查無此服務類型"));
        }

        Map<String, String> errorMsgs = validateRequest(request);

        if (!errorMsgs.isEmpty()) {
            return ResponseEntity.badRequest().body(errorMsgs);
        }

        ServiceTypeVO serviceTypeVO = serviceTypeSvc.update(
                serviceTypeId,
                request.getTypeName().trim(),
                request.getDescription().trim(),
                request.getTypeMode(),
                request.getImgURL().trim()
        );

        return ResponseEntity.ok(toDTO(serviceTypeVO));
    }

    // =========================
    // 刪除服務類型
    // DELETE /api/service-types/{serviceTypeId}
    // =========================
    @DeleteMapping("/{serviceTypeId}")
    public ResponseEntity<?> delete(@PathVariable Integer serviceTypeId) {
        ServiceTypeVO oldVO = serviceTypeSvc.findByPK(serviceTypeId);

        if (oldVO == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "查無此服務類型"));
        }

        serviceTypeSvc.delete(serviceTypeId);

        return ResponseEntity.ok(Map.of("message", "刪除成功"));
    }

    // =========================
    // VO 轉 DTO
    // 給 Vue / Postman 用乾淨資料
    // =========================
    private ServiceTypeDTO toDTO(ServiceTypeVO vo) {
        ServiceTypeDTO dto = new ServiceTypeDTO();

        dto.setServiceTypeId(vo.getSvcTypeID());
        dto.setTypeName(vo.getTypeName());
        dto.setDescription(vo.getDescrip());
        dto.setTypeMode(vo.getTypeMode());
        dto.setTypeModeText(convertTypeModeText(vo.getTypeMode()));
        dto.setImgURL(vo.getImgURL());

        return dto;
    }

    private String convertTypeModeText(Integer typeMode) {
        if (typeMode == null) {
            return "未知";
        }

        if (typeMode == 0) {
            return "動態";
        }

        if (typeMode == 1) {
            return "靜態";
        }

        return "未知";
    }

    // =========================
    // 驗證 RequestBody
    // =========================
    private Map<String, String> validateRequest(ServiceTypeRequest request) {
        Map<String, String> errorMsgs = new LinkedHashMap<>();

        if (request == null) {
            errorMsgs.put("request", "請求內容不可為空");
            return errorMsgs;
        }

        String typeName = request.getTypeName();
        String description = request.getDescription();
        Integer typeMode = request.getTypeMode();
        String imgURL = request.getImgURL();

        if (typeName == null || typeName.trim().isEmpty()) {
            errorMsgs.put("typeName", "服務類型名稱請勿空白");
        } else if (typeName.trim().length() > 50) {
            errorMsgs.put("typeName", "服務類型名稱不可超過 50 字");
        }

        if (description == null || description.trim().isEmpty()) {
            errorMsgs.put("description", "服務類型描述請勿空白");
        } else if (description.trim().length() > 255) {
            errorMsgs.put("description", "服務類型描述不可超過 255 字");
        }

        if (typeMode == null) {
            errorMsgs.put("typeMode", "請選擇服務類型狀態");
        } else if (typeMode != 0 && typeMode != 1) {
            errorMsgs.put("typeMode", "類型狀態只能是 0 或 1");
        }

        if (imgURL == null || imgURL.trim().isEmpty()) {
            errorMsgs.put("imgURL", "圖片路徑請勿空白");
        } else if (imgURL.trim().length() > 255) {
            errorMsgs.put("imgURL", "圖片路徑不可超過 255 字");
        } else if (!imgURL.trim().matches("^/images/service/.+\\.(jpg|jpeg|png|gif|webp)$")) {
            errorMsgs.put("imgURL", "圖片路徑格式錯誤，例如：/images/service/walk.jpg");
        }

        return errorMsgs;
    }

    // =========================
    // Request DTO
    // 前端送進來的 JSON
    // =========================
    public static class ServiceTypeRequest {
        private String typeName;
        private String description;
        private Integer typeMode;
        private String imgURL;

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getTypeMode() {
            return typeMode;
        }

        public void setTypeMode(Integer typeMode) {
            this.typeMode = typeMode;
        }

        public String getImgURL() {
            return imgURL;
        }

        public void setImgURL(String imgURL) {
            this.imgURL = imgURL;
        }
    }

    // =========================
    // Response DTO
    // 回傳給 Vue / Postman 的資料
    // =========================
    public static class ServiceTypeDTO {
        private Integer serviceTypeId;
        private String typeName;
        private String description;
        private Integer typeMode;
        private String typeModeText;
        private String imgURL;

        public Integer getServiceTypeId() {
            return serviceTypeId;
        }

        public void setServiceTypeId(Integer serviceTypeId) {
            this.serviceTypeId = serviceTypeId;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getTypeMode() {
            return typeMode;
        }

        public void setTypeMode(Integer typeMode) {
            this.typeMode = typeMode;
        }

        public String getTypeModeText() {
            return typeModeText;
        }

        public void setTypeModeText(String typeModeText) {
            this.typeModeText = typeModeText;
        }

        public String getImgURL() {
            return imgURL;
        }

        public void setImgURL(String imgURL) {
            this.imgURL = imgURL;
        }
    }
}