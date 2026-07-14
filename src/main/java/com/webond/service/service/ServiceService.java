package com.webond.service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.model.ServiceTypeVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.repository.ServiceOrderRepository;
import com.webond.service.repository.ServiceRepository;
import com.webond.service.repository.ServiceSlotRepository;
import com.webond.service.repository.ServiceTypeRepository;

@Service
@Transactional
public class ServiceService {

    // =====================
    // 服務狀態
    // =====================

    private static final byte STATUS_INACTIVE = 0; // 下架
    private static final byte STATUS_ACTIVE = 1;   // 上架
    private static final byte STATUS_ARCHIVED = 2; // 已刪除／封存
    private static final byte STATUS_DISABLED = 3; // 平台停用

    private final ServiceRepository serviceRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceSlotRepository serviceSlotRepository;

    public ServiceService(
            ServiceRepository serviceRepository,
            ServiceTypeRepository serviceTypeRepository,
            ServiceOrderRepository serviceOrderRepository,
            ServiceSlotRepository serviceSlotRepository) {

        this.serviceRepository = serviceRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceSlotRepository = serviceSlotRepository;
    }

    // =====================
    // 前台公開查詢
    // =====================

    @Transactional(readOnly = true)
    public List<ServiceVO> getActiveServices() {
        return serviceRepository.findActiveServices();
    }

    @Transactional(readOnly = true)
    public List<ServiceVO> getActiveServicesByServiceTypeId(
            Integer serviceTypeId) {

        return serviceRepository
                .findActiveServicesByServiceTypeId(serviceTypeId);
    }

    @Transactional(readOnly = true)
    public ServiceVO getActiveServiceById(Integer serviceId) {
        return serviceRepository.findActiveServiceById(serviceId);
    }

    @Transactional(readOnly = true)
    public List<ServiceVO> searchActiveServices(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return serviceRepository.findActiveServices();
        }

        return serviceRepository.searchActiveServices(keyword.trim());
    }

    // =====================
    // 會員新增服務
    // =====================

    public ServiceVO addBySeller(
            Integer loginMemberId,
            Integer serviceTypeId,
            String serviceName,
            String description,
            Integer hourlyRate,
            byte[] serviceImage,
            String serviceImageType,
            String serviceCity,
            String serviceDistrict,
            String serviceLocation) {

        if (loginMemberId == null) {
            throw new IllegalArgumentException("請先登入");
        }

        validateMemberServiceRequest(
                serviceTypeId,
                serviceName,
                description,
                hourlyRate,
                serviceCity,
                serviceDistrict,
                serviceLocation
        );

        if (!serviceTypeRepository.existsById(serviceTypeId)) {
            throw new IllegalArgumentException("查無此服務類型");
        }

        ServiceVO serviceVO = new ServiceVO();

        ServiceTypeVO serviceType =
                serviceTypeRepository.getReferenceById(serviceTypeId);

        serviceVO.setServiceType(serviceType);
        serviceVO.setMemberId(loginMemberId);

        serviceVO.setServiceName(serviceName.trim());
        serviceVO.setDescription(description.trim());
        serviceVO.setHourlyRate(hourlyRate);

        // 服務圖片
        serviceVO.setServiceImage(serviceImage);
        serviceVO.setServiceImageType(
                normalizeImageType(serviceImage, serviceImageType)
        );

        // 服務地區
        serviceVO.setServiceCity(serviceCity.trim());
        serviceVO.setServiceDistrict(serviceDistrict.trim());
        serviceVO.setServiceLocation(
                normalizeOptionalText(serviceLocation)
        );

        // 新增後預設上架
        serviceVO.setStatus(STATUS_ACTIVE);

        return serviceRepository.save(serviceVO);
    }

    /*
     * 沒有上傳圖片時也可以使用此方法。
     * 主要是避免舊 Controller 尚未傳入圖片參數時編譯失敗。
     */
    public ServiceVO addBySeller(
            Integer loginMemberId,
            Integer serviceTypeId,
            String serviceName,
            String description,
            Integer hourlyRate,
            String serviceCity,
            String serviceDistrict,
            String serviceLocation) {

        return addBySeller(
                loginMemberId,
                serviceTypeId,
                serviceName,
                description,
                hourlyRate,
                null,
                null,
                serviceCity,
                serviceDistrict,
                serviceLocation
        );
    }

    // =====================
    // 會員修改服務
    // =====================

    @Transactional(readOnly = true)
    public ServiceVO getOwnServiceForEdit(
            Integer serviceId,
            Integer loginMemberId) {

        ServiceVO serviceVO =
                getOwnServiceOrThrow(serviceId, loginMemberId);

        if (Byte.valueOf(STATUS_ARCHIVED)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "已封存的服務不能修改"
            );
        }

        if (Byte.valueOf(STATUS_DISABLED)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "平台停用的服務不能修改"
            );
        }

        return serviceVO;
    }

    /*
     * 修改服務基本資料與地區。
     * 此方法不修改圖片，原本圖片會保留。
     */
    public void updateBySeller(
            Integer serviceId,
            Integer loginMemberId,
            Integer serviceTypeId,
            String serviceName,
            String description,
            Integer hourlyRate,
            String serviceCity,
            String serviceDistrict,
            String serviceLocation) {

        validateMemberServiceRequest(
                serviceTypeId,
                serviceName,
                description,
                hourlyRate,
                serviceCity,
                serviceDistrict,
                serviceLocation
        );

        ServiceVO serviceVO =
                getOwnServiceOrThrow(serviceId, loginMemberId);

        validateServiceCanBeModified(serviceVO);

        if (!serviceTypeRepository.existsById(serviceTypeId)) {
            throw new IllegalArgumentException("查無此服務類型");
        }

        ServiceTypeVO serviceType =
                serviceTypeRepository.getReferenceById(serviceTypeId);

        serviceVO.setServiceType(serviceType);

        serviceVO.setServiceName(serviceName.trim());
        serviceVO.setDescription(description.trim());
        serviceVO.setHourlyRate(hourlyRate);

        serviceVO.setServiceCity(serviceCity.trim());
        serviceVO.setServiceDistrict(serviceDistrict.trim());
        serviceVO.setServiceLocation(
                normalizeOptionalText(serviceLocation)
        );

        serviceRepository.save(serviceVO);
    }

    /*
     * 修改服務基本資料、地區及圖片。
     *
     * replaceImage：
     * true  = 使用本次傳入的圖片取代舊圖片
     * false = 保留原本圖片
     */
    public void updateBySeller(
            Integer serviceId,
            Integer loginMemberId,
            Integer serviceTypeId,
            String serviceName,
            String description,
            Integer hourlyRate,
            byte[] serviceImage,
            String serviceImageType,
            boolean replaceImage,
            String serviceCity,
            String serviceDistrict,
            String serviceLocation) {

        validateMemberServiceRequest(
                serviceTypeId,
                serviceName,
                description,
                hourlyRate,
                serviceCity,
                serviceDistrict,
                serviceLocation
        );

        ServiceVO serviceVO =
                getOwnServiceOrThrow(serviceId, loginMemberId);

        validateServiceCanBeModified(serviceVO);

        if (!serviceTypeRepository.existsById(serviceTypeId)) {
            throw new IllegalArgumentException("查無此服務類型");
        }

        ServiceTypeVO serviceType =
                serviceTypeRepository.getReferenceById(serviceTypeId);

        serviceVO.setServiceType(serviceType);

        serviceVO.setServiceName(serviceName.trim());
        serviceVO.setDescription(description.trim());
        serviceVO.setHourlyRate(hourlyRate);

        serviceVO.setServiceCity(serviceCity.trim());
        serviceVO.setServiceDistrict(serviceDistrict.trim());
        serviceVO.setServiceLocation(
                normalizeOptionalText(serviceLocation)
        );

        if (replaceImage) {
            serviceVO.setServiceImage(serviceImage);
            serviceVO.setServiceImageType(
                    normalizeImageType(
                            serviceImage,
                            serviceImageType
                    )
            );
        }

        serviceRepository.save(serviceVO);
    }

    @Transactional(readOnly = true)
    public List<ServiceVO> getManageableServicesByMemberId(
            Integer memberId) {

        return serviceRepository
                .findManageableServicesByMemberId(memberId);
    }

    // =====================
    // 會員上下架服務
    // =====================

    public void deactivateBySeller(
            Integer serviceId,
            Integer loginMemberId) {

        ServiceVO serviceVO =
                getOwnServiceOrThrow(serviceId, loginMemberId);

        if (!Byte.valueOf(STATUS_ACTIVE)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "只有上架中的服務可以下架"
            );
        }

        serviceVO.setStatus(STATUS_INACTIVE);
        serviceRepository.save(serviceVO);
    }

    public void activateBySeller(
            Integer serviceId,
            Integer loginMemberId) {

        ServiceVO serviceVO =
                getOwnServiceOrThrow(serviceId, loginMemberId);

        if (!Byte.valueOf(STATUS_INACTIVE)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "只有已下架的服務可以重新上架"
            );
        }

        serviceVO.setStatus(STATUS_ACTIVE);
        serviceRepository.save(serviceVO);
    }

    // =====================
    // 會員刪除服務
    // =====================

    /*
     * 沒有訂單：
     * 刪除時段後，真正刪除服務。
     *
     * 已有訂單：
     * 為保留歷史資料，改成 status = 2 封存。
     */
    public void deleteBySeller(
            Integer serviceId,
            Integer loginMemberId) {

        ServiceVO serviceVO =
                getOwnServiceOrThrow(serviceId, loginMemberId);

        if (Byte.valueOf(STATUS_ARCHIVED)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "此服務已封存，不能重複刪除"
            );
        }

        if (Byte.valueOf(STATUS_DISABLED)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "平台停用的服務不能自行刪除"
            );
        }

        boolean hasAnyOrder =
                serviceOrderRepository.existsByServiceId(serviceId);

        if (hasAnyOrder) {
            serviceVO.setStatus(STATUS_ARCHIVED);
            serviceRepository.save(serviceVO);
            return;
        }

        /*
         * 沒有任何訂單時：
         * 先刪除此服務底下的時段，再刪除服務，
         * 避免 SERVICE_SLOT 外鍵擋住。
         */
        serviceSlotRepository.deleteByService_ServiceId(serviceId);
        serviceRepository.delete(serviceVO);
    }

    // =====================
    // 後台服務管理
    // =====================

    public void disableByAdmin(Integer serviceId) {

        ServiceVO serviceVO =
                serviceRepository.findById(serviceId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "查無此服務"
                                )
                        );

        if (Byte.valueOf(STATUS_ARCHIVED)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "已封存的服務不能停用"
            );
        }

        serviceVO.setStatus(STATUS_DISABLED);
        serviceRepository.save(serviceVO);
    }

    public void restoreByAdmin(Integer serviceId) {

        ServiceVO serviceVO =
                serviceRepository.findById(serviceId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "查無此服務"
                                )
                        );

        if (!Byte.valueOf(STATUS_DISABLED)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "只有平台停用的服務可以恢復"
            );
        }

        serviceVO.setStatus(STATUS_ACTIVE);
        serviceRepository.save(serviceVO);
    }

    @Transactional(readOnly = true)
    public ServiceVO getOneService(Integer serviceId) {
        return serviceRepository
                .findOneWithServiceType(serviceId);
    }

    @Transactional(readOnly = true)
    public List<ServiceVO> getAll() {
        return serviceRepository.findAllWithServiceType();
    }

    @Transactional(readOnly = true)
    public List<ServiceVO> getServicesByServiceTypeId(
            Integer serviceTypeId) {

        return serviceRepository
                .findByServiceTypeId(serviceTypeId);
    }

    // =====================
    // 共用方法
    // =====================

    private ServiceVO getOwnServiceOrThrow(
            Integer serviceId,
            Integer loginMemberId) {

        if (loginMemberId == null) {
            throw new IllegalArgumentException("請先登入");
        }

        if (serviceId == null || serviceId <= 0) {
            throw new IllegalArgumentException(
                    "服務編號不正確"
            );
        }

        return serviceRepository
                .findByServiceIdAndMemberId(
                        serviceId,
                        loginMemberId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "查無此服務，或你沒有權限操作"
                        )
                );
    }

    private void validateServiceCanBeModified(
            ServiceVO serviceVO) {

        if (Byte.valueOf(STATUS_ARCHIVED)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "已封存的服務不能修改"
            );
        }

        if (Byte.valueOf(STATUS_DISABLED)
                .equals(serviceVO.getStatus())) {

            throw new IllegalArgumentException(
                    "平台停用的服務不能修改"
            );
        }
    }

    private void validateMemberServiceRequest(
            Integer serviceTypeId,
            String serviceName,
            String description,
            Integer hourlyRate,
            String serviceCity,
            String serviceDistrict,
            String serviceLocation) {

        if (serviceTypeId == null || serviceTypeId <= 0) {
            throw new IllegalArgumentException(
                    "請選擇服務類型"
            );
        }

        if (serviceName == null ||
                serviceName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "服務名稱請勿空白"
            );
        }

        if (serviceName.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "服務名稱不可超過 100 個字"
            );
        }

        if (description == null ||
                description.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "服務描述請勿空白"
            );
        }

        if (description.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "服務描述不可超過 500 個字"
            );
        }

        if (hourlyRate == null || hourlyRate < 100) {
            throw new IllegalArgumentException(
                    "每小時費率不可低於 100"
            );
        }

        if (serviceCity == null ||
                serviceCity.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "請選擇服務縣市"
            );
        }

        if (serviceCity.trim().length() > 20) {
            throw new IllegalArgumentException(
                    "服務縣市不可超過 20 個字"
            );
        }

        if (serviceDistrict == null ||
                serviceDistrict.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "請選擇服務行政區"
            );
        }

        if (serviceDistrict.trim().length() > 20) {
            throw new IllegalArgumentException(
                    "服務行政區不可超過 20 個字"
            );
        }

        if (serviceLocation == null ||
                serviceLocation.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "請填寫服務地點說明"
            );
        }

        if (serviceLocation.trim().length() > 255) {
            throw new IllegalArgumentException(
                    "服務地點說明不可超過 255 個字"
            );
        }
    }

    private String normalizeOptionalText(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeImageType(
            byte[] serviceImage,
            String serviceImageType) {

        if (serviceImage == null || serviceImage.length == 0) {
            return null;
        }

        if (serviceImageType == null ||
                serviceImageType.trim().isEmpty()) {

            return "application/octet-stream";
        }

        return serviceImageType.trim();
    }
}