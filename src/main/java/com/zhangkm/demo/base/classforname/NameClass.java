package com.zhangkm.demo.base.classforname;

public class NameClass {
	public NameClass(ParameterClass pc, String s){
		System.out.println("NameClass: new instance: public NameClass(ParameterClass pc, String s)");
		System.out.println("NameClass: String s: " + s);
	}
	public NameClass(){
		System.out.println("NameClass: new instance: public NameClass()");
	}
	
	public ReturnClass doSomething(){
		System.out.println("NameClass: doSomething()");
		return new ReturnClass();
	}
}
