package com.zhangkm.demo.mybatis.frame;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface BaseMapper<T> extends Mapper<T>, MySqlMapper<T> {
    //TODO
    //FIXME 特别注意，该接口不能被扫描到，否则会出错。 
	//FIXME 不能被 @MapperScan(basePackages = "com.zhangkm.zoo.dao.mysql") 扫描到。
	//FIXME 即不能放入xxx.xxx.dao.mysql包中。
	
}
