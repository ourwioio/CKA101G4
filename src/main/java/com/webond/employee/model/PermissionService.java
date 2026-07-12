package com.webond.employee.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.employee.repository.EmpPermRepository;
import com.webond.employee.repository.PermissionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PermissionService {
	
	@Autowired
	PermissionRepository permRepo;
	
	@Autowired
	EmpPermRepository empPermRepo;
	
	
	public void addPermission(PermissionVO permVO) {
		permRepo.save(permVO);
	}
	public void updatePermission(PermissionVO permVO) {
		permRepo.save(permVO);
	}
	public void deletePermission(Integer permId) {
		if(permRepo.existsById(permId))
			permRepo.deleteById(permId);
	}
	public PermissionVO getOnePerm(Integer permId) {
		Optional<PermissionVO> optional = permRepo.findById(permId);
		return optional.orElse(null);
	}
	public List<PermissionVO> getAll(){
		return permRepo.findAll();
	}
	

// === 員工權限首頁(分頁邏輯) ===//
		public Page<PermissionVO> getAllByPage(int pageNo){
			int pageSize = 3;
			
			Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("permId").ascending());
			
			return permRepo.findAll(pageable);
		}
	
// === 送出修改 === //
		@Transactional
		public void updatePerm(PermissionVO permVO) {
			
			PermissionVO oldPerm = permRepo.findById(permVO.getPermId())
					.orElseThrow(() -> new RuntimeException("找不到該權限資料"));
			
			oldPerm.setPermName(permVO.getPermName());
			oldPerm.setPermDsc(permVO.getPermDsc());
			oldPerm.setPermUpdatedAt(new Timestamp(System.currentTimeMillis()));
			
			permRepo.save(oldPerm);
		}
		
// === 刪除 === //
		@Transactional
		public void deletePerm(Integer permId) {
			PermissionVO currentPerm = permRepo.findById(permId)
					.orElseThrow(()-> new EntityNotFoundException("找不到此員工權限，ID : " + permId));

			empPermRepo.deleteByPermId(currentPerm.getPermId());
			empPermRepo.flush();	
			
		    if (currentPerm.getEmpPermVO() != null) {
		        currentPerm.getEmpPermVO().clear();
		    }


			permRepo.delete(currentPerm);
		}

}
