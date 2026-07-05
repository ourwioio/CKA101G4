package com.webond.venue.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class VenueOrderDTO {

    @NotNull(message = "請選擇場地")
    private Integer venueId;

    @NotNull(message = "請選擇日期")
    private Integer venueSlotId;

    @NotNull(message = "請選擇日期")
    private LocalDate bookDate;

    @NotNull(message = "請選擇開始時間")
    @Min(value = 0, message = "開始時間不正確")
    @Max(value = 23, message = "開始時間不正確")
    private Integer startHour;

    @NotNull(message = "請選擇結束時間")
    @Min(value = 1, message = "結束時間不正確")
    @Max(value = 24, message = "結束時間不正確")
    private Integer endHour;

    @NotNull(message = "請選擇付款方式")
    private Byte paymentMethod;

    public VenueOrderDTO() {
    }

    public Integer getVenueId() {
        return venueId;
    }

    public void setVenueId(Integer venueId) {
        this.venueId = venueId;
    }

    public Integer getVenueSlotId() {
        return venueSlotId;
    }

    public void setVenueSlotId(Integer venueSlotId) {
        this.venueSlotId = venueSlotId;
    }

    public LocalDate getBookDate() {
        return bookDate;
    }

    public void setBookDate(LocalDate bookDate) {
        this.bookDate = bookDate;
    }

    public Integer getStartHour() {
        return startHour;
    }

    public void setStartHour(Integer startHour) {
        this.startHour = startHour;
    }

    public Integer getEndHour() {
        return endHour;
    }

    public void setEndHour(Integer endHour) {
        this.endHour = endHour;
    }

    public Byte getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Byte paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}