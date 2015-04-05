package com.khs.spcmeasure.entity;

public class SimpleCode {

    private static final String TAG = "SimpleCode";

    // type constants
    public static final String TYPE_ACTION_CAUSE = "actionCause";

    // internal codes constants
    public static final String INTERNAL_CODE_NO_CAUSE = "0";

	// private member variables
	private long id;
	private String type;
    private String code;
    private String description;
    private String intCode;
	private boolean active;

	// empty constructor
	public SimpleCode() {}

	// constructor
	public SimpleCode(long id, String type, String code, String description, String intCode, boolean active) {
		super();
		this.id = id;
		this.type = type;
        this.code = code;
        this.description = description;
        this.intCode = intCode;
		this.active = active;
	}

	// getter & setter methods
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIntCode() {
        return intCode;
    }

    public void setIntCode(String intCode) {
        this.intCode = intCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
	public String toString() {
		return this.type + ":" + this.code;
	}
}
