package com.webond.activity.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.repository.MemberRepository;
import com.webond.member.service.NotificationService;

@Service
public class ActivityNotificationService {

	private static final byte ACTIVITY_NOTIFICATION_TYPE = 0;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private MemberRepository memberRepository;

	public void notifyHostNewOrder(ActivityVO activityVO, ActivityOrderVO orderVO) {
		if (activityVO == null || orderVO == null) {
			return;
		}

		addMemberNotification(activityVO.getMemberId(), "\u6d3b\u52d5\u6536\u5230\u65b0\u7684\u5831\u540d\u7533\u8acb",
				"\u4f60\u7684\u6d3b\u52d5\u300c" + activityVO.getActivityTitle()
						+ "\u300d\u6536\u5230\u6703\u54e1\u0020" + orderVO.getBuyerMemberId()
						+ "\u0020\u7684\u5831\u540d\u7533\u8acb\uff0c\u8acb\u5230\u6d3b\u52d5\u540d\u55ae\u5be9\u6838\u3002");
	}

	public void notifyBuyerApproved(ActivityVO activityVO, ActivityOrderVO orderVO) {
		if (activityVO == null || orderVO == null) {
			return;
		}

		addMemberNotification(orderVO.getBuyerMemberId(), "\u6d3b\u52d5\u5831\u540d\u5df2\u901a\u904e",
				"\u4f60\u7533\u8acb\u7684\u6d3b\u52d5\u300c" + activityVO.getActivityTitle()
						+ "\u300d\u5df2\u901a\u904e\u5be9\u6838\uff0c\u8acb\u5230\u6211\u7684\u8a02\u55ae\u9078\u64c7\u4ed8\u6b3e\u3002");
	}

	public void notifyBuyerRejected(ActivityVO activityVO, ActivityOrderVO orderVO) {
		if (activityVO == null || orderVO == null) {
			return;
		}

		addMemberNotification(orderVO.getBuyerMemberId(), "\u6d3b\u52d5\u5831\u540d\u672a\u901a\u904e",
				"\u4f60\u7533\u8acb\u7684\u6d3b\u52d5\u300c" + activityVO.getActivityTitle()
						+ "\u300d\u672a\u901a\u904e\u4e3b\u8fa6\u5be9\u6838\u3002");
	}

	public void notifyHostPaymentCompleted(ActivityVO activityVO, ActivityOrderVO orderVO) {
		if (activityVO == null || orderVO == null) {
			return;
		}

		addMemberNotification(activityVO.getMemberId(), "\u6d3b\u52d5\u8a02\u55ae\u5df2\u4ed8\u6b3e",
				"\u6703\u54e1\u0020" + orderVO.getBuyerMemberId() + "\u0020\u5df2\u5b8c\u6210\u6d3b\u52d5\u300c"
						+ activityVO.getActivityTitle() + "\u300d\u4ed8\u6b3e\u3002");
	}

	public void notifyHostOrderCancelled(ActivityVO activityVO, ActivityOrderVO orderVO, Byte oldOrderStatus) {
		if (activityVO == null || orderVO == null || oldOrderStatus == null) {
			return;
		}

		if (oldOrderStatus == 0) {
			addMemberNotification(activityVO.getMemberId(), "\u6d3b\u52d5\u8a02\u55ae\u53d6\u6d88\u4e26\u5f85\u9000\u6b3e",
					"\u6703\u54e1\u0020" + orderVO.getBuyerMemberId()
							+ "\u0020\u5df2\u53d6\u6d88\u6d3b\u52d5\u300c" + activityVO.getActivityTitle()
							+ "\u300d\uff0c\u6b64\u8a02\u55ae\u5df2\u4ed8\u6b3e\u4e26\u9032\u5165\u5f85\u9000\u6b3e\u3002");
			return;
		}

		addMemberNotification(activityVO.getMemberId(), "\u6d3b\u52d5\u5831\u540d\u5df2\u53d6\u6d88",
				"\u6703\u54e1\u0020" + orderVO.getBuyerMemberId() + "\u0020\u5df2\u53d6\u6d88\u6d3b\u52d5\u300c"
						+ activityVO.getActivityTitle() + "\u300d\u5831\u540d\u3002");
	}

	public void notifyBuyerRefundDone(ActivityVO activityVO, ActivityOrderVO orderVO) {
		if (activityVO == null || orderVO == null) {
			return;
		}

		addMemberNotification(orderVO.getBuyerMemberId(), "\u6d3b\u52d5\u9000\u6b3e\u5df2\u5b8c\u6210",
				"\u6d3b\u52d5\u300c" + activityVO.getActivityTitle()
						+ "\u300d\u9000\u6b3e\u5df2\u5b8c\u6210\uff0c\u8acb\u78ba\u8a8d\u6b3e\u9805\u3002");
	}

	public void notifyHostPayoutDone(ActivityVO activityVO, ActivityOrderVO orderVO) {
		if (activityVO == null || orderVO == null) {
			return;
		}

		addMemberNotification(activityVO.getMemberId(), "\u6d3b\u52d5\u64a5\u6b3e\u5df2\u5b8c\u6210",
				"\u6d3b\u52d5\u300c" + activityVO.getActivityTitle()
						+ "\u300d\u64a5\u6b3e\u5df2\u5b8c\u6210\u3002");
	}

	private void addMemberNotification(Integer memberId, String title, String content) {
		if (memberId == null || !memberRepository.existsById(memberId)) {
			return;
		}

		MemberVO memberVO = memberRepository.getReferenceById(memberId);
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setMember(memberVO);
		notificationVO.setTitle(title);
		notificationVO.setContent(content);
		notificationVO.setNotificationType(ACTIVITY_NOTIFICATION_TYPE);
		notificationService.addNotification(notificationVO);
	}
}
