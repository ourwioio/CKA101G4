package com.webond.activity.model;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActRptRepository;
import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;

@Service
public class ActRptService {
	
	@Autowired
    private ActRptRepository actRptRepo;
	
	@Autowired
	private MemberService memSvc;
	

    @Transactional
    public ActRptVO createReport(ActRptVO actRptVO) {

    	actRptVO.setActRptTime(new Timestamp(System.currentTimeMillis()));
        
        actRptVO.setActRptStatus(0); 
        
        return actRptRepo.save(actRptVO);
    }
     
    
//===============    後台審核
    public Page<ActRptVO> getRptsByStatusWithPage(Integer status, int page, int size){
    	Pageable pageable = PageRequest.of(page, size, Sort.by("actRptTime"));
    	return actRptRepo.findByActRptStatus(status, pageable);
    }
    
    public ActRptVO getRetById(Integer id) {
    	return actRptRepo.findById(id).orElse(null);
    }
    
    // 更新審核結果
    @Transactional
    public void reviewRpt(ActRptVO reviewData) {
    	ActRptVO existing = actRptRepo.findById(reviewData.getActRptId()).orElse(null);
    	if(existing != null) {
    		existing.setEmpId(reviewData.getEmpId());
    		existing.setActRptStatus(reviewData.getActRptStatus()); 
            existing.setPenaltyType(reviewData.getPenaltyType());
            existing.setPenaltyValue(reviewData.getPenaltyValue()); 
            existing.setRemark(reviewData.getRemark()); 
            existing.setUpdated(new Timestamp(System.currentTimeMillis())); 
            
            if (Integer.valueOf(1).equals(reviewData.getActRptStatus())) {
                
            	MemberVO memVO = memSvc.getOneMember(existing.getActId().getMemberId());
                
                if (memVO != null) {
                    int currentPoints = memVO.getReportPoints() != null ? memVO.getReportPoints() : 0;
                    int penalty = reviewData.getPenaltyValue() != null ? reviewData.getPenaltyValue() : 0;
                    
                    memVO.setReportPoints(currentPoints + penalty);
                    
                    memSvc.updateMember(memVO); 
                }
            }
            
            actRptRepo.save(existing);
    	}
    }
    
    public ActRptVO getOneActRpt(Integer actRptId) {
    	Optional<ActRptVO> vo = actRptRepo.findById(actRptId);
    	return vo.orElse(null);
    }
    
    public Page<ActRptVO> getAllRpts(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("actRptId").ascending());
        return actRptRepo.findAll(pageable); 
    }
    
    
    
    
    
// === 前台申訴 === //
    
    public void submitAppeal(Integer actRptId, String content, byte[] img) {
    	
    	ActRptVO actRpt = actRptRepo.findById(actRptId)
    			.orElseThrow(() -> new IllegalArgumentException("找不到此檢舉案"));
    	
    	actRpt.setAppealContent(content);
        actRpt.setAppealImg(img);
        actRpt.setAppealTime(new Timestamp(System.currentTimeMillis()));
        actRpt.setUpdated(new Timestamp(System.currentTimeMillis()));
        
        actRpt.setActRptStatus(3); 

        actRptRepo.save(actRpt);
    }
    	
    	
    
    

}
