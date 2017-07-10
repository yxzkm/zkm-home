package com.zhangkm.demo.mybatis.dao.mysql;

import java.util.List;
import java.util.Map;

import com.zhangkm.demo.mybatis.frame.BaseMapper;
import com.zhangkm.demo.mybatis.model.Country;

public interface CountryMapper extends BaseMapper<Country> {
	public List<Map<String,Object>> zkmCountryList();

}