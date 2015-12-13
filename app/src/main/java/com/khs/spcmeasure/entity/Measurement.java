package com.khs.spcmeasure.entity;

import android.util.Log;

import java.util.Date;

public class Measurement {
	
	private static final String TAG = "Measurement";
	
	// private member variables
	private Long id;	// Object to allow for null
	private long pieceId;
	private long prodId;
	private long featId;	
	private Date collectDt;		// TODO is this required as piece has it? 
	private String operator;	// TODO is this required as piece has it?
	private double value;
    private double range;
    private Long cause;
	private long limitRev;
	private boolean inControl;
	private boolean inEngLim;
		
	// empty constructor
	public Measurement() {}

	// constructor
	public Measurement(Long id, long pieceId, long prodId, long featId, 
			Date collectDt, String operator, double value, double range, Long cause, long limitRev, boolean inControl, boolean inEngLim) {
		super();
		this.id = id;
		this.pieceId = pieceId;
		this.prodId = prodId;
		this.featId = featId;
		this.collectDt = collectDt;
		this.operator = operator;
		this.value = value;
        this.range = range;
        this.cause = cause;
		this.limitRev = limitRev;
		this.inControl = inControl;
		this.inEngLim = inEngLim;
	}

	public Measurement(long pieceId, long prodId, long featId, 
			Date collectDt, String operator, double value, double range, Long cause, long limitRev, boolean inControl, boolean inEngLim) {
		this(null, pieceId, prodId, featId, collectDt, operator, value, range, cause, limitRev, inControl, inEngLim);
	}

	// getter & setter methods
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getPieceId() {
		return pieceId;
	}

	public void setPieceId(long pieceId) {
		this.pieceId = pieceId;
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

	public Date getCollectDt() {
		return collectDt;
	}

	public void setCollectDt(Date collectDt) {
		this.collectDt = collectDt;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public Long getCause() {
        return cause;
    }

    public void setCause(Long cause) {
        this.cause = cause;
    }

    public long getLimitRev() {
		return limitRev;
	}

	public void setLimitRev(long limitRev) {
		this.limitRev = limitRev;
	}

	public boolean isInControl() {
		return inControl;
	}

	public void setInControl(boolean inControl) {
		Log.d(TAG, "setInControl = " + inControl);
		this.inControl = inControl;
	}

	public boolean isInEngLim() {
		return inEngLim;
	}

	public void setInEngLim(boolean inEngLim) {
		this.inEngLim = inEngLim;
	}
	
}
