package com.comze_instancelabs.signscan;

public class Address {

	private String street;
	private String number;
	private int x;
	private int z;
	
	public Address(String s, String nu, int x, int z){
		this.street = s;
		this.number = nu;
		this.x = x;
		this.z = z;
	}
	
	public String getStreet(){
		return this.street;
	}
	
	public String getNumber(){
		return this.number;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getZ(){
		return this.z;
	}
}
