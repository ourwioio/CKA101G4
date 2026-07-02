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

	// 服務狀態
	private static final byte STATUS_INACTIVE = 0; // 下架
	private static final byte STATUS_ACTIVE = 1; // 上架
	private static final byte STATUS_ARCHIVED = 2; // 已刪除 / 封存
	private static final byte STATUS_DISABLED = 3; // 平台停用

	private final ServiceRepository serviceRepository;
	private final ServiceTypeRepository serviceTypeRepository;
	private final ServiceOrderRepository serviceOrderRepository;
	private final ServiceSlotRepository serviceSlotRepository;

	public ServiceService(ServiceRepository serviceRepository, ServiceTypeRepository serviceTypeRepository,
			ServiceOrderRepository serviceOrderRepository, ServiceSlotRepository serviceSlotRepository) {
		this.serviceRepository = serviceRepository;
		this.serviceTypeRepository = serviceTypeRepository;
		this.serviceOrderRepository = serviceOrderRepository;
		this.serviceSlotRepository = serviceSlotRepository;
	}

	// =====================
	// 前台公開
	// =====================

	@Transactional(readOnly = true)
	public List<ServiceVO> getActiveServices() {
		return serviceRepository.findActiveServices();
	}

	@Transactional(readOnly = true)
	public List<ServiceVO> getActiveServicesByServiceTypeId(Integer serviceTypeId) {
		return serviceRepository.findActiveServicesByServiceTypeId(serviceTypeId);
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
	// 會員中心
	// =====================

	// 會員新增服務
	public ServiceVO addBySeller(Integer loginMemberId, Integer serviceTypeId, String serviceName, String description,
			Integer hourlyRate) {

		validateMemberServiceRequest(serviceTypeId, serviceName, description, hourlyRate);

		ServiceVO serviceVO = new ServiceVO();

		ServiceTypeVO serviceType = serviceTypeRepository.getReferenceById(serviceTypeId);
		serviceVO.setServiceType(serviceType);

		serviceVO.setMemberId(loginMemberId);
		serviceVO.setServiceName(serviceName.trim());
		serviceVO.setDescription(description.trim());
		serviceVO.setHourlyRate(hourlyRate);

		// 會員新增後預設上架
		serviceVO.setStatus(STATUS_ACTIVE);

		return serviceRepository.save(serviceVO);
	}

	// 會員修改服務前，查自己的服務給表單回填
	@Transactional(readOnly = true)
	public ServiceVO getOwnServiceForEdit(Integer serviceId, Integer loginMemberId) {

		ServiceVO serviceVO = getOwnServiceOrThrow(serviceId, loginMemberId);

		if (Byte.valueOf(STATUS_ARCHIVED).equals(serviceVO.getStatus())) {
			throw new IllegalArgumentException("已封存的服務不能修改");
		}

		if (Byte.valueOf(STATUS_DISABLED).equals(serviceVO.getStatus())) {
			throw new IllegalArgumentException("平台停用的服務不能修改");
		}

		return serviceVO;
	}

	// 會員修改自己的服務
	public void updateBySeller(Integer serviceId, Integer loginMemberId, Integer serviceTypeId, String serviceName,
			String description, Integer hourlyRate) {

		validateMemberServiceRequest(serviceTypeId, serviceName, description, hourlyRate);

		ServiceVO serviceVO = getOwnServiceOrThrow(serviceId, loginMemberId);

		if (Byte.valueOf(STATUS_ARCHIVED).equals(serviceVO.getStatus())) {
			throw new IllegalArgumentException("已封存的服務不能修改");
		}

		if (Byte.valueOf(STATUS_DISABLED).equals(serviceVO.getStatus())) {
			throw new IllegalArgumentException("平台停用的服務不能修改");
		}

		ServiceTypeVO serviceType = serviceTypeRepository.getReferenceById(serviceTypeId);
		serviceVO.setServiceType(serviceType);

		serviceVO.setServiceName(serviceName.trim());
		serviceVO.setDescription(description.trim());
		serviceVO.setHourlyRate(hourlyRate);

		serviceRepository.save(serviceVO);
	}

	@Transactional(readOnly = true)
	public List<ServiceVO> getManageableServicesByMemberId(Integer memberId) {
		return serviceRepository.findManageableServicesByMemberId(memberId);
	}

	// 下架自己的服務
	// 只有 status = 1 可以下架
	public void deactivateBySeller(Integer serviceId, Integer loginMemberId) {

		ServiceVO serviceVO = getOwnServiceOrThrow(serviceId, loginMemberId);

		if (!Byte.valueOf(STATUS_ACTIVE).equals(serviceVO.getStatus())) {
			throw new IllegalArgumentException("只有上架中的服務可以下架");
		}

		serviceVO.setStatus(STATUS_INACTIVE);
		serviceRepository.save(serviceVO);
	}

	// 重新上架自己的服務
	// 只有 status = 0 可以重新上架
	public void activateBySeller(Integer serviceId, Integer loginMemberId) {

		ServiceVO serviceVO = getOwnServiceOrThrow(serviceId, loginMemberId);

		if (!Byte.valueOf(STATUS_INACTIVE).equals(serviceVO.getStatus())) {
			throw new IllegalArgumentException("只有已下架的服務可以重新上架");
		}

		serviceVO.setStatus(STATUS_ACTIVE);
		serviceRepository.save(serviceVO);
	}

	// 刪除自己的服務
	// 沒訂單：真刪除
	// 有訂單：封存 status = 2
	public void deleteBySeller(Integer serviceId, Integer loginMemberId) {

		ServiceVO serviceVO = getOwnServiceOrThrow(serviceId, loginMemberId);

		if (Byte.valueOf(STATUS_ARCHIVED).equals(serviceVO.getStatus())) {
			throw new IllegalArgumentException("此服務已封存，不能重複刪除");
		}

		if (Byte.valueOf(STATUS_DISABLED).equals(serviceVO.getStatus())) {
			throw new IllegalArgumentException("平台停用的服務不能自行刪除");
		}

		boolean hasAnyOrder = serviceOrderRepository.existsByServiceId(serviceId);

		if (hasAnyOrder) {
			serviceVO.setStatus(STATUS_ARCHIVED);
			serviceRepository.save(serviceVO);
			return;
		}

		// 沒訂單才真刪除
		// 先刪時段，再刪服務，避免外鍵擋住
		serviceSlotRepository.deleteByServiceId(serviceId);
		serviceRepository.delete(serviceVO);
	}

	// =====================
	// 後台之後再整理
	// =====================

	@Transactional(readOnly = true)
	public ServiceVO getOneService(Integer serviceId) {
		return serviceRepository.findOneWithServiceType(serviceId);
	}

	@Transactional(readOnly = true)
	public List<ServiceVO> getAll() {
		return serviceRepository.findAllWithServiceType();
	}

	@Transactional(readOnly = true)
	public List<ServiceVO> getServicesByServiceTypeId(Integer serviceTypeId) {
		return serviceRepository.findByServiceTypeId(serviceTypeId);
	}

	// =====================
	// 共用
	// =====================

	private ServiceVO getOwnServiceOrThrow(Integer serviceId, Integer loginMemberId) {

		if (loginMemberId == null) {
			throw new IllegalArgumentException("請先登入");
		}

		return serviceRepository.findByServiceIdAndMemberId(serviceId, loginMemberId)
				.orElseThrow(() -> new IllegalArgumentException("查無此服務，或你沒有權限操作"));
	}

	private void validateMemberServiceRequest(Integer serviceTypeId, String serviceName, String description,
			Integer hourlyRate) {

		if (serviceTypeId == null || serviceTypeId <= 0) {
			throw new IllegalArgumentException("請選擇服務類型");
		}

		if (serviceName == null || serviceName.trim().isEmpty()) {
			throw new IllegalArgumentException("服務名稱請勿空白");
		}

		if (serviceName.trim().length() > 100) {
			throw new IllegalArgumentException("服務名稱不可超過 100 個字");
		}

		if (description == null || description.trim().isEmpty()) {
			throw new IllegalArgumentException("服務描述請勿空白");
		}

		if (description.trim().length() > 500) {
			throw new IllegalArgumentException("服務描述不可超過 500 個字");
		}

		if (hourlyRate == null || hourlyRate < 100) {
			throw new IllegalArgumentException("每小時費率不可低於 100");
		}
	}
}