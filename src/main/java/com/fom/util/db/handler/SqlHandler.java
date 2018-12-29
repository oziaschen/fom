package com.fom.util.db.handler;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author shanhm
 * @date 2018年12月23日
 *
 */
public interface SqlHandler {
	
	SqlHandler handler = new SqlHandlerImpl();
	
	/**
	 * 查询
	 * @param poolName 连接池名称
	 * @param sql sql语句，其中参数变量以#修饰开头和结尾 
	 * @param paramMap 对应sql语句中的参数变量的值
	 * @return 
	 * @throws Exception
	 */
	List<Map<String, Object>> queryForList(String poolName, String sql, Map<String, Object> paramMap) throws Exception;
	
	/**
	 * 新增/删除/修改
	 * @param poolName 连接池名称
	 * @param sql sql语句，其中参数变量以#修饰开头和结尾 
	 * @param paramMap 对应sql语句中的参数变量的值
	 * @return
	 * @throws Exception
	 */
	int execute(String poolName, String sql,Map<String, Object> paramMap) throws Exception;

	/**
	 * 批量 新增/删除/修改
	 * @param poolName poolName 连接池名称
	 * @param sql sql语句，其中参数变量以#修饰开头和结尾 
	 * @param paramMaps 对应sql语句中的参数变量的值
	 * @param isTransaction 是否事务执行
	 * @return
	 * @throws Exception
	 */
	int[] batchExecute(String poolName, String sql, List<Map<String, Object>> paramMaps) throws Exception;
	
	/**
	 * 开始事务
	 * @param poolName
	 * @throws Exception
	 */
	void startTransaction(String poolName) throws Exception;
	
	/**
	 * 结束事务
	 * @param poolName
	 * @throws Exception
	 */
	void endTransaction(String poolName) throws Exception;
}