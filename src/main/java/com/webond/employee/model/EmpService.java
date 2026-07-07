package com.webond.employee.model;

import java.io.IOException;
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
import com.webond.employee.repository.EmpPermRepository;
import com.webond.employee.repository.EmployeeRepository;

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

	
	
	public void addEmp(EmployeeVO empVO) {
		repository.save(empVO);
	}
	public void updateEmp(EmployeeVO empVO) {
		repository.save(empVO);
	}
	public void deleteEmp(Integer employeeId) {
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
		}else {
			empVO.setEmpImg(null);
		}
		
		// 密碼加密
		 String hashedPassword = passwordEncoder.encode(empVO.getEmpPassword());
	     empVO.setEmpPassword(hashedPassword);
	     
	     
	     // 新增預設狀態
	     empVO.setEmpStatus(0);
	     
	     
	     // 先儲存員工基本資料 (流水號)
	     EmployeeVO savedEmp = repository.save(empVO);
	     
	     
	     
	     // 拿出員工，綁上權限存入empPerm
	     if(permIds != null && !permIds.isEmpty()) {  	 	//拿到有勾的權限ID
	    	 for(Integer permId :permIds) {
	    		 EmpPermVO empPermVO = new EmpPermVO();  	//建立新的員工權限物件
	    		 empPermVO.setEmps(savedEmp);  			 	//把員工權限ID 綁定新增的資料
	    		 
	    		 // EX. 員工權限編號: 1 , 員工編號 7001
	    		 
	    		 PermissionVO permVO = new PermissionVO();	// 建立權限物件
	    		 permVO.setPermId(permId);			 		// 把有勾的權限ID資料放進物件
	    		 empPermVO.setPerms(permVO);    			// 把權限員工ID 綁定有勾的權限
	    		 // Ex. 員工權限編號: 1 , 員工編號 7001 , 權限編號 2
	    		 
	    		 empPermRepo.save(empPermVO);  //存入上面的資料
	    		 
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
    
    
    
    
    
    
	
}
