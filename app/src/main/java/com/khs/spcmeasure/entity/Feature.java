package com.khs.spcmeasure.entity;

public class Feature {

    private static final String TAG = "Feature";

	// private member variables
	private Long id;		// Object to allow for null
	private long prodId;
	private long featId;
	private String name;
	private boolean active;
	private long limitRev;
    private Double cp;
    private Double cpk;

	// empty constructor
	public Feature() {}

	// constructor
	public Feature(Long id, long prodId, long featId, String name, boolean active, long limitRev, Double cp, Double cpk) {
		super();
		this.id = id;
		this.prodId = prodId;
		this.featId = featId;
		this.name = name;
		this.active = active;
		this.limitRev = limitRev;
        this.cp = cp;
        this.cpk = cpk;
	}
	
	public Feature(long prodId, long featId, String name, boolean active, long limitRev, Double cp, Double cpk) {
		this(null, prodId, featId, name, active, limitRev, cp, cpk);
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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getLimitRev() {
		return this.limitRev;
	}

	public void setLimitRev(long limitRev) {
		this.limitRev = limitRev;
	}

    public Double getCp() {
        return cp;
    }

    public void setCp(Double cp) {
        this.cp = cp;
    }

    public Double getCpk() {
        return cpk;
    }

    public void setCpk(Double cpk) {
        this.cpk = cpk;
    }
}
