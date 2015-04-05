package com.khs.spcmeasure.entity;

import java.util.Date;

import com.khs.spcmeasure.library.CollectStatus;

public class Piece {

    private static final String TAG = "Piece";

	// private member variables
	private Long id;		// Object to allow for null
	private long prodId;
	private Long sgId;
	private long pieceNum;
	private Date collectDt;
	private String operator;
	private String lot;
	private CollectStatus status;
	
	// empty constructor
	public Piece() {}

	// constructors
	public Piece(Long id, long prodId, Long sgId, long pieceNum, 
			Date collectDt, String operator, String lot, CollectStatus status) {
		super();
		this.id = id;
		this.prodId = prodId;
		this.sgId = sgId;
		this.pieceNum = pieceNum;		
		this.collectDt = collectDt;
		this.operator = operator;
		this.lot = lot;
		this.status = status;
	}
	
	public Piece(long prodId, Long sgId, long pieceNum, 
			Date collectDt, String operator, String lot, CollectStatus status) {
		this(null, prodId, sgId, pieceNum, collectDt, operator, lot, status);
	}	

	public Piece(long prodId, long pieceNum, 
			Date collectDt, String operator, String lot, CollectStatus status) {
		this(null, prodId, null, pieceNum, collectDt, operator, lot, status);
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

	public Long getSgId() {
		return sgId;
	}

	public void setSgId(Long sgId) {
		this.sgId = sgId;
	}

	public long getPieceNum() {
		return pieceNum;
	}

	public void setPieceNum(long pieceNum) {
		this.pieceNum = pieceNum;
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

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public CollectStatus getStatus() {
		return status;
	}

	public void setStatus(CollectStatus status) {
		this.status = status;
	}
	
}
