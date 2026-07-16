package com.webond.member.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.member.model.NotificationVO;
import com.webond.member.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository repository;

    public void addNotification(NotificationVO notificationVO) {

        notificationVO.setCreatedAt(LocalDate.now());
        notificationVO.setIsRead((byte) 0);

        if (notificationVO.getReport() != null) {
            notificationVO.setNotificationType((byte) 2);
        }

        repository.save(notificationVO);
    }

    public void updateNotification(NotificationVO notificationVO) {

        NotificationVO original = repository .findById(notificationVO.getNotificationId()).orElse(null);

        if (original != null) {
            notificationVO.setCreatedAt(original.getCreatedAt());
        }

        notificationVO.setIsRead((byte) 0);

        repository.save(notificationVO);
    }

    public NotificationVO getOneForUpdate(Integer notificationId) {

        return repository.findById(notificationId).orElse(null);
    }

    public void deleteNotification(Integer notificationId) {

        if (repository.existsById(notificationId)) {
            repository.deleteById(notificationId);
        }
    }

    public NotificationVO getOneNotification(Integer notificationId) {

        Optional<NotificationVO> optional =
                repository.findById(notificationId);

        NotificationVO notificationVO = optional.orElse(null);

        if (notificationVO != null) {

            notificationVO.setIsRead((byte) 1);

            repository.save(notificationVO);
        }

        return notificationVO;
    }

    public List<NotificationVO> getNotificationByMemberId(Integer memberId) {

        return repository
                .findByMember_MemberId(memberId);
    }

    public List<NotificationVO> getNotificationByEmployeeId(Integer employeeId) {

        return repository
                .findByEmployee_EmployeeId(employeeId);
    }

    public List<NotificationVO> getAll() {
        return repository.findAll();
    }

    public void markNotificationAsRead(Integer notificationId) {

        repository.markAsRead(notificationId);
    }

    public void markAllNotificationAsRead(Integer memberId) {

        repository.markAllAsRead(memberId);
    }

    public int countUnread(
            Integer memberId) {

        if (memberId == null) {
            return 0;
        }

        return repository
                .countByMember_MemberIdAndIsRead(
                    memberId,
                    0
                );
    }
    
    
    public String redirectUrl(NotificationVO n) {
    	Byte type = n.getNotificationType();
    	
    	if(type == null) {
    		return fallbackUrl(n.getNotificationId());
    	}
    	
    	return switch (type) {
    		case 0 -> resolveOrderRedirect(n.getTitle(), n.getNotificationId());
    		case 1 -> resolveReviewRedirect(n.getTitle(), n.getNotificationId());
    		case 2 -> fallbackUrl(n.getNotificationId());
    		case 3 -> fallbackUrl(n.getNotificationId());
    		default -> fallbackUrl(n.getNotificationId());
    	
    	};
    }
    
    public String resolveOrderRedirect(String title, Integer notificationId) {
    	if(title == null) {
    		return fallbackUrl(notificationId);
    	}
    	
    	if(title.contains("服務")) {
    		if(title.contains("新的預約申請") || title.contains("付款完成") || title.contains("買家取消")) {
        		return "/member/service-orders/seller";
    		}
    		else if(title.contains("通過") || title.contains("賣家取消") || title.contains("預約時段")
    				|| title.contains("退款完成") || title.contains("付款期限")) {
    			return "/member/service-orders/buyer";
    		}
    	}
    	
    	if(title.contains("活動")) {
    		if(title.contains("申請") || title.contains("付款") || title.contains("退款") 
    		   || title.contains("取消") || title.contains("撥款已完成")) {
        		return "/activity/front/myHostActivity";
    		}
    		else if(title.contains("通過") || title.contains("退款已完成")) {
    			return "/activity/front/myOrder";
    		}
    	}
    	
    	if(title.contains("場地")) {
    		if(title.contains("預約") || title.contains("取消")) {
        		return "/front/venueOrder/myVenuesReservations";
    		}
    		else if(title.contains("審核") || title.contains("遭檢舉")) {
    			return "/front/venue/myVenue";
    		}
    		else if(title.contains("撥款")) {
    			return "/front/venueOrder/myVenuesCompleted";
    		}
    		else if(title.contains("退款") || title.contains("檢舉受理") ) {
    			return "/front/venueOrder/myVenueOrder";
    		}
    	}
    	
        return fallbackUrl(notificationId); 	
    	
    }
    

    public String resolveReviewRedirect(String title, Integer notificationId) {
    	if(title == null) {
    		return fallbackUrl(notificationId);
    	}  
    	
    	if(title.contains("服務")) {
    		if(title.contains("新評價通知")) {
        		return "/front/memberPage/my";
    		}
    		else if(title.contains("服務評價提醒通知")) {
    			return "/member/services";    			
    		}
    	} 
    	
    	if(title.contains("活動")) {
    		if(title.contains("新評價通知")) {
        		return "/front/memberPage/my";
    		}
    		else if(title.contains("活動評價提醒通知")) {
    			return "/member/services";    			
    		}
    	}
    	
    	if(title.contains("場地")) {
    		if(title.contains("場地被評價通知")) {
        		return "/front/venueOrder/myVenuesCompleted";
    		}
    		else if(title.contains("場地評價提醒通知")) {
    			return "/front/venueOrder/myVenueOrder";    			
    		}
    	}  
    	

        return fallbackUrl(notificationId); 
    	
    }
    


    private String fallbackUrl(Integer notificationId) {
        return "/front/notification/getOneNotification?notificationId=" + notificationId;
    }
}