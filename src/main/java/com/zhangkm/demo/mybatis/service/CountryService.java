package com.zhangkm.demo.mybatis.service;

import com.github.pagehelper.PageHelper;
import com.zhangkm.demo.mybatis.dao.mysql.CountryMapper;
import com.zhangkm.demo.mybatis.model.Country;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(value="countryService")
public class CountryService {

    @Autowired
    private CountryMapper countryMapper;

    public List<Country> getAll(Country country) {
        if (country.getPage() != null && country.getRows() != null) {
            PageHelper.startPage(country.getPage(), country.getRows());
        }
        return countryMapper.selectAll();
    }

    public Country getById(Integer id) {
        return countryMapper.selectByPrimaryKey(id);
    }

    public void deleteById(Integer id) {
        countryMapper.deleteByPrimaryKey(id);
    }

    public void save(Country country) {
        if (country.getId() != null) {
            countryMapper.updateByPrimaryKey(country);
        } else {
            countryMapper.insert(country);
        }
    }
}
