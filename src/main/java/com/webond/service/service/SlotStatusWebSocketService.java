package com.webond.service.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.webond.service.dto.SlotStatusMessage;

@Service
public class SlotStatusWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public SlotStatusWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 推送某個服務時段的最新狀態
    public void publishSlotStatus(Integer serviceId,
                                  Integer serviceSlotId,
                                  Byte slotStatus) {

        SlotStatusMessage message = new SlotStatusMessage(
                serviceId,
                serviceSlotId,
                slotStatus,
                getStatusText(slotStatus)
        );

        messagingTemplate.convertAndSend(
                "/topic/services/" + serviceId + "/slots",
                message
        );
    }

    private String getStatusText(Byte slotStatus) {

        if (slotStatus == null) {
            return "未知狀態";
        }

        if (slotStatus == 0) {
            return "可預約";
        }

        if (slotStatus == 1) {
            return "暫時鎖定";
        }

        if (slotStatus == 2) {
            return "已預約";
        }

        if (slotStatus == 3) {
            return "已封存";
        }

        return "未知狀態";
    }
}