package com.webond.venue.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webond.venue.model.VenueSlotVO;

import jakarta.persistence.LockModeType;

public interface VenueSlotRepository extends JpaRepository<VenueSlotVO, Integer>{

	// 關鍵：用悲觀鎖鎖定這一天的 Row，避免同毫秒的其他人進來改字串
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VenueSlotVO v WHERE v.venueSlotId = :id")
    Optional<VenueSlotVO> findByIdForUpdate(@Param("id") Integer id);
    
	@Query("SELECT MAX(s.slotDate) FROM VenueSlotVO s WHERE s.venueVO.venueId = :venueId")
	LocalDate findMaxSlotDateByVenueId(@Param("venueId") Integer venueId);
}
