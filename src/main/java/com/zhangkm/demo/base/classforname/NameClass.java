package com.zhangkm.demo.base.classforname;

public class NameClass extends BaseClasss{
	public NameClass(ParameterClass pc, String s){
		System.out.println("NameClass: new instance: public NameClass(ParameterClass pc, String s)");
		System.out.println("NameClass: String s: " + s);
	}
	public NameClass(){
		System.out.println("NameClass: new instance: public NameClass()");
	}
	
	@Override
    public String init(){
        System.out.println("NameClass: public String init()");
        return null;
    }

    @Override
    public String distroy(){
        System.out.println("NameClass: public String distroy()");
        return null;
    }

	public ReturnClass doSomething(){
		System.out.println("NameClass: doSomething()");
		return new ReturnClass();
	}
}
