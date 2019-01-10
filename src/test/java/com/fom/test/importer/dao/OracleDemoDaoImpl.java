package com.fom.test.importer.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.fom.test.importer.DemoBean;

public class OracleDemoDaoImpl extends SqlSessionDaoSupport implements DemoDao {

	@Override
	public List<Map<String,String>> selectDemo() {
		return getSqlSession().selectList("oracle.demo.selectDemo");
	}

	@Override
	public int inserDemo(DemoBean bean) {
		 return getSqlSession().insert("oracle.demo.insertDemo", bean);
	}

	@Override
	public int batchInsertDemo(List<DemoBean> list) {
		 return getSqlSession().insert("oracle.demo.batchInsertDemo", list);
	}

}