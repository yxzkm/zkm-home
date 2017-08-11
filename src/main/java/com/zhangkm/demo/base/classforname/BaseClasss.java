package com.zhangkm.demo.base.classforname;

abstract class BaseClasss {

    public String name = "";

    abstract protected String init();

    abstract protected String distroy();

    public void start(){
        init();
    }
}
