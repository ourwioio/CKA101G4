package com.webond.member.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.repository.MemberRepository;
import com.webond.venue.model.VenueOrderVO;

import jakarta.transaction.Transactional;

@Service
public class MemberService {
	
	@Autowired
	MemberRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private MemberDeactivationCoordinator memberDeactivationCoordinator; 
	
    
    public String getNickname(Integer memberId) {
        if (memberId == null) {
            return "未知會員";
        }
        String nickname = repository.findNicknameById(memberId);
        return (nickname != null) ? nickname : "未知會員";
    }
    
    

    //act controller private 
    private Map<Integer, String> buildRegistrationStatusMap(List<ActivityVO> activityList) {
        Map<Integer, String> statusMap = new HashMap<>();
        for (ActivityVO activityVO : activityList) {
            statusMap.put(activityVO.getActivityId(), getRegistrationStatusText(activityVO));
        }
        return statusMap;
    }

    private String getRegistrationStatusText(ActivityVO activityVO) {
        if (activityVO == null) {
            return "活動不存在";
        }

        LocalDateTime now = LocalDateTime.now();
        if (activityVO.getEndTime() != null && now.isAfter(activityVO.getEndTime())) {
            return "活動已結束";
        }

        if (activityVO.getRegistrationStartTime() != null && now.isBefore(activityVO.getRegistrationStartTime())) {
            return "尚未開放報名";
        }

        if (activityVO.getRegistrationDeadline() != null && now.isAfter(activityVO.getRegistrationDeadline())) {
            return "報名已截止";
        }

        if (activityVO.getRegistrationDeadline() != null
                && Duration.between(now, activityVO.getRegistrationDeadline()).toHours() <= 168) {
            return "即將截止";
        }

        return "報名中";
    }
    
    // for controller    
    public Map<Integer, String> getRegistrationStatusMap(List<ActivityVO> activityList) {
        return buildRegistrationStatusMap(activityList);
    }

    
    
	
	public void addMember(MemberVO memberVO) {
		repository.save(memberVO);
	}
	

	public void updateMember(MemberVO memberVO) {
		repository.save(memberVO);
	}
	
	public void deleteMember(Integer memberId) {
		if(repository.existsById(memberId)) { //先利用existsById判斷有沒有這項pk
			repository.deleteById(memberId);
		}
	}
	
	public MemberVO getOneMember(Integer memberId) {
		Optional<MemberVO> optional = repository.findById(memberId);
		return optional.orElse(null); //查詢值存在回傳值，否則回傳.orElse()內的值
	}
	
	public List<MemberVO> getALL(){
		return repository.findAll();
	}
	
	public Set<NotificationVO> getNotificationsByMember(Integer memberId){
		return getOneMember(memberId).getNotifications();
	}
	
	public Set<VenueOrderVO> getVenueOrderByMember(Integer memberId){
		return getOneMember(memberId).getVenueOrders();
	}
	
	public void updateMemberStatus(Integer memberId, byte accountStatus) {
	    MemberVO member = repository.findById(memberId).orElse(null);
	    if (member != null) {
	        member.setAccountStatus(accountStatus);
	        repository.save(member);
	    }
	}
	
	@Transactional
	public void changePassword(Integer memberId, String oldPassword, String newPassword) {

	    MemberVO member = repository.findById(memberId).orElse(null);
	    
	    if(member == null) {
	    	throw new IllegalArgumentException("查無此會員");
	    }
		
		if(!passwordEncoder.matches(oldPassword, member.getPasswordHash())) {
			throw new IllegalArgumentException("原密碼輸入錯誤");
		}
		
		if(passwordEncoder.matches(newPassword,  member.getPasswordHash())) {
			throw new IllegalArgumentException("新密碼不可與原密碼相同");
		}
		
		member.setPasswordHash(passwordEncoder.encode(newPassword));
		repository.save(member);
	}
	
	@Transactional
	public void updateAccountStatus(Integer memberId, Byte status) {
	    MemberVO member = repository.findById(memberId)
	            .orElseThrow(() -> new RuntimeException("查無此會員"));
	    member.setAccountStatus(status);
	    MemberVO updatedMember = repository.save(member);

	    // 會員自行註銷原本就會經過本方法；狀態儲存成功後再通知跨模組協調器。
	    // 協調器只接受狀態 2（註銷）或 3（停權），其他狀態不會執行任何連動。
	    memberDeactivationCoordinator.handleDisabledMember(updatedMember);
	}
	
	
	
	
}
