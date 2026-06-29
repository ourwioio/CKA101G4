package com.webond.venue.model;

import java.util.Arrays;

import com.webond.venue.model.VenueVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "VENUE_IMAGES")
public class VenueImagesVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "VENUE_IMAGES_ID", updatable = false)
	private Integer imagesId;

	@ManyToOne
	@JoinColumn(name = "VENUE_ID", referencedColumnName = "VENUE_ID")
	private VenueVO venueVO;

	@Column(name = "VENUE_IMAGES")
	private byte[] images;
	
	@Column(name = "VENUE_COVER")
	private Byte cover;  // 0 = 一般照片, 1 = 封面

	public VenueImagesVO() {
		super();
	}

	public VenueImagesVO(Integer imagesId, VenueVO venueVO, byte[] images, Byte cover) {
		super();
		this.imagesId = imagesId;
		this.venueVO = venueVO;
		this.images = images;
		this.cover = cover;
	}

	public Integer getImagesId() {
		return imagesId;
	}

	public void setImagesId(Integer imagesId) {
		this.imagesId = imagesId;
	}

	public VenueVO getVenueVO() {
		return venueVO;
	}

	public void setVenueVO(VenueVO venueVO) {
		this.venueVO = venueVO;
	}

	public byte[] getImages() {
		return images;
	}

	public void setImages(byte[] images) {
		this.images = images;
	}

	public Byte getCover() {
		return cover;
	}

	public void setCover(Byte cover) {
		this.cover = cover;
	}


}
