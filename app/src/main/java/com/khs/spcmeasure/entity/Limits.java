package com.khs.spcmeasure.entity;

import com.khs.spcmeasure.library.LimitType;

public class Limits {
	// private member variables
	private Long id;		// Object to allow for null
	private long prodId;
	private long featId;
	private long limitRev;
	private LimitType limitType;
	private double upper;
	private double lower;

	// empty constructor
	public Limits() {}

	// constructor
	public Limits(Long id, long prodId, long featId, long limitRev, LimitType limitType, double upper, double lower) {
		super();
		this.id = id;
		this.prodId = prodId;
		this.featId = featId;
		this.limitRev = limitRev;
		this.limitType = limitType;
		this.upper = upper;
		this.lower = lower;
	}
	
	public Limits(long prodId, long featId, long limitRev, LimitType limitType, double upper, double lower) {
		this(null, prodId, featId, limitRev, limitType, upper, lower);
	}
	
	// getter & setter methods
	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}	
	
	public long getProdId() {
		return prodId;
	}

	public void setProdId(long prodId) {
		this.prodId = prodId;
	}

	public long getFeatId() {
		return featId;
	}

	public void setFeatId(long featId) {
		this.featId = featId;
	}

	public long getLimitRev() {
		return limitRev;
	}

	public void setLimitRev(long limitRev) {
		this.limitRev = limitRev;
	}

	public LimitType getLimitType() {
		return this.limitType;
	}

	public void setLimitType(LimitType limitType) {
		this.limitType = limitType;
	}

	public double getUpper() {
		return this.upper;
	}

	public void setUpper(double upper) {
		this.upper = upper;
	}

	public double getLower() {
		return this.lower;
	}

	public void setLower(double lower) {
		this.lower = lower;
	}
}
