package com.webond.service.dto;

public class SlotStatusMessage {

    private Integer serviceId;
    private Integer serviceSlotId;
    private Byte slotStatus;
    private String statusText;

    public SlotStatusMessage() {
    }

    public SlotStatusMessage(Integer serviceId,
                             Integer serviceSlotId,
                             Byte slotStatus,
                             String statusText) {
        this.serviceId = serviceId;
        this.serviceSlotId = serviceSlotId;
        this.slotStatus = slotStatus;
        this.statusText = statusText;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getServiceSlotId() {
        return serviceSlotId;
    }

    public void setServiceSlotId(Integer serviceSlotId) {
        this.serviceSlotId = serviceSlotId;
    }

    public Byte getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(Byte slotStatus) {
        this.slotStatus = slotStatus;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}