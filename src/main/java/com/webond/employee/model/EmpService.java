package com.webond.employee.model;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.webond.employee.dto.EmpPasswordDTO;
import com.webond.employee.model.EmpPermVO;
import com.webond.employee.model.EmployeeVO;
import com.webond.employee.model.PermissionVO;
import com.webond.employee.repository.EmpPermRepository;
import com.webond.employee.repository.EmployeeRepository;
import com.webond.member.model.MemberVO;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EmpService {
	
	@Autowired
	private EmployeeRepository repository;

	@Autowired
	private EmpPermRepository empPermRepo;
	
	private final PasswordEncoder passwordEncoder;
	public EmpService(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	
	
	public void addEmployee(EmployeeVO empVO) {
		repository.save(empVO);
	}
	public void updateEmployee(EmployeeVO empVO) {
		repository.save(empVO);
	}
	public void deleteEmployee(Integer employeeId) {
		if(repository.existsById(employeeId))
			repository.deleteById(employeeId);
	}
	public EmployeeVO getOneEmp(Integer employeeId) {
		Optional<EmployeeVO> optional = repository.findById(employeeId);
		return optional.orElse(null);
	}
	public List<EmployeeVO> getAll(){
		return repository.findAll();
	}
	
	
	
	//  查全部(分頁)
	public Page<EmployeeVO> getAllByPage(int pageNo){
		int pageSize = 5;
		
		// Sort.by("empId") 代表預設用員工編號排序，.ascending() 是正向排序。
		Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("employeeId").ascending());
		
		// 直接將 pageable 傳給 repository.findAll()，它就會精準查出該頁的 3 筆資料並回傳。
		return repository.findAll(pageable);
		
		
	}
	

//=== 為了新增寫的 ===	//
	@Transactional
	public void saveEmp(EmployeeVO empVO, List<Integer> permIds, MultipartFile empImg) throws IOException{
		
		// 圖片處理
		if(empImg != null && !empImg.isEmpty()) {
			byte[] imgBytes = empImg.getBytes();
			empVO.setEmpImg(imgBytes);
		}
		
		// 密碼加密
		 String hashedPassword = passwordEncoder.encode(empVO.getEmpPassword());
	     empVO.setEmpPassword(hashedPassword);
	     
	     
	     // 新增預設狀態
	     empVO.setEmpStatus(0);
	     
	     
	     // 先儲存員工基本資料 (流水號)
	     EmployeeVO savedEmp = repository.save(empVO);
	     
	     
	     
	     // 拿出員工，綁上權限存入empPerm
	     if(permIds != null && !permIds.isEmpty()) {  	 	
	    	 for(Integer permId :permIds) {
	    		 EmpPermVO empPermVO = new EmpPermVO();  	
	    		 empPermVO.setEmps(savedEmp);  			 	
	    		 
	    		 
	    		 PermissionVO permVO = new PermissionVO();
	    		 permVO.setPermId(permId);			 		
	    		 empPermVO.setPerms(permVO);  
	    		 
	    		 empPermVO.setAssignedAt(new Timestamp(System.currentTimeMillis()));
	    		 
	    		 empPermRepo.save(empPermVO); 
	    		 
	    	 }
	     }
	     
	}
	
//=== 新增用到的帳號唯一性 ===// 
    public boolean isEmpAccountExists(String empAccount) {
        return repository.existsByEmpAccount(empAccount);
    }
	
	
//=== 第一次登入修改密碼 === //
    
    //檢查舊密碼是否正確
    public boolean checkOldPassword(String empAccount, EmpPasswordDTO empPasswordDTO) {
    	
    	Optional<EmployeeVO> empOptional = repository.findByEmpAccount(empAccount);
    	EmployeeVO emp = empOptional.orElseThrow(()-> new RuntimeException("找不到該使用者"));
    	
    	return passwordEncoder.matches(empPasswordDTO.getCurrentPassword(), emp.getEmpPassword());
    }
    
    @Transactional
    public void updatePassword(String empAccount, EmpPasswordDTO empPasswordDTO) {
    	Optional<EmployeeVO> empOptional = repository.findByEmpAccount(empAccount);
    	EmployeeVO emp = empOptional.orElseThrow(()-> new RuntimeException("找不到該使用者"));
    	
    	emp.setEmpPassword(passwordEncoder.encode(empPasswordDTO.getNewPassword()));
    	emp.setEmpStatus(1);
    	repository.save(emp);
    }
    
    
//=== 修改時去資料庫拿圖片 ===//    
    public byte[] getEmpImage(Integer employeeId) throws IOException{
    	EmployeeVO emp = repository.findById(employeeId).orElse(null);
    	
    	if(emp !=null && emp.getEmpImg() != null && emp.getEmpImg().length > 0) {
    		return emp.getEmpImg();
    	}
    	
    	return null;
    }
    
// === 處理圖片改過的狀態 ===//
    public EmployeeVO processEmpImg(
    		EmployeeVO formEmp, 
    		MultipartFile upImg,
    		byte[] sessionImg ) throws IOException{
    	
    	
    	if(formEmp.getEmployeeId() == null) {
    		return formEmp;
    	}
    	
    	EmployeeVO oldEmp = repository.findById(formEmp.getEmployeeId()).orElse(null);
    	
    	if(oldEmp != null) {
    		if(upImg != null && !upImg.isEmpty()) {
    			formEmp.setEmpImg(upImg.getBytes());
    		}else {
    			
                if (sessionImg != null && sessionImg.length > 0) {
                    formEmp.setEmpImg(sessionImg);
                } else {
                    formEmp.setEmpImg(oldEmp.getEmpImg());
                }
    		}
    	}
    	return formEmp;
    }
    
    
// === 為了(送出)修改寫的 === //
    @Transactional
	public void updateEmp(
			EmployeeVO formEmp, 
			List<Integer> permIds, 
			MultipartFile empImg ) throws IOException{
		
    	
    	EmployeeVO currentEmp = repository.findById(formEmp.getEmployeeId())
    			.orElseThrow(() -> new EntityNotFoundException("找不到此員工，ID : " + formEmp.getEmployeeId()));
    	
    	
		// 圖片處理
		if(empImg != null && !empImg.isEmpty()) {
			byte[] imgBytes = empImg.getBytes();
			currentEmp.setEmpImg(imgBytes);
			formEmp.setEmpImg(imgBytes);
		} else {
	        formEmp.setEmpImg(currentEmp.getEmpImg());
	    }
	     
	    currentEmp.setEmpName(formEmp.getEmpName());
	    currentEmp.setEmpStatus(formEmp.getEmpStatus());
	    currentEmp.setRoleTitle(formEmp.getRoleTitle());
	    
	    currentEmp.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
	     
	     EmployeeVO savedEmp = repository.save(currentEmp);
	     
	     empPermRepo.deleteByEmployeeId(savedEmp.getEmployeeId());
	     
	     // 拿出員工，綁上權限存入empPerm
	     if(permIds != null && !permIds.isEmpty()) {  	 	
	    	 for(Integer permId :permIds) {
	    		 EmpPermVO empPermVO = new EmpPermVO();  	
	    		 empPermVO.setEmps(savedEmp);  			 	
	    		 
	    		 
	    		 PermissionVO permVO = new PermissionVO();	
	    		 permVO.setPermId(permId);			 		
	    		 empPermVO.setPerms(permVO);    
	    		 
	    		 empPermVO.setAssignedAt(new Timestamp(System.currentTimeMillis()));
	    		 
	    		 empPermRepo.save(empPermVO); 
	    		 
	    	 }
	     }
	     
	}
    
    
// === 為了刪除寫的 === //
    @Transactional
    public void deleteEmp(Integer employeeId) {
    	EmployeeVO currentEmp = getOneEmp(employeeId);
    	if(currentEmp == null) {
    		throw new EntityNotFoundException("找不到此員工，ID : " + employeeId);
    	}
    	
    	empPermRepo.deleteByEmployeeId(currentEmp.getEmployeeId());
    
    	repository.delete(currentEmp);
    }
    
    
    public EmployeeVO findByEmpAccount(String empAccount) {
		if (empAccount == null || empAccount.trim().isEmpty()) {
			return null;
		}
		Optional<EmployeeVO> optional = repository.findByEmpAccount(empAccount.trim());
		return optional.orElse(null);
    }
    
    
	
}
