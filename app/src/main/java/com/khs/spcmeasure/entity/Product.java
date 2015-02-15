package com.khs.spcmeasure.entity;

public class Product {
	// private member variables
	private long id;
	private String name;
	private boolean active;
	private String customer;
	private String program;

	// empty constructor
	public Product() {}

	// constructor
	public Product(long id, String name, boolean active, String customer, String program) {
		super();
		this.id = id;
		this.name = name;
		this.active = active;
		this.customer = customer;
		this.program = program;
	}
	
	public Product(long id, String name, boolean active) {
		super();
		this.id = id;
		this.name = name;
		this.active = active;
	}

	// getter & setter methods
	
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getCustomer() {
		return this.customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getProgram() {
		return this.program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	@Override
	public String toString() {
		// TODO return "Product [name=" + name + "]";
		return this.name;
	}
}
