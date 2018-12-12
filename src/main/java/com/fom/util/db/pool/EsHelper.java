package com.fom.util.db.pool;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.VersionConflictEngineException;

import com.fom.util.IoUtils;
import com.fom.util.exception.WarnException;
import com.fom.util.log.LoggerFactory;

/**
 * 
 * @author X4584
 * @date 2018年8月10日
 */
public class EsHelper {

	protected static final Logger LOG = LoggerFactory.getLogger("es");

	protected EsHelper(){

	}

	protected final boolean _isIndexExist(String poolName, String index) throws Exception {
		PoolEs pool = getPool(poolName);
		try{
			TransportClient client = pool.acquire().v;
			IndicesExistsResponse response = 
					client.admin().indices().exists(new IndicesExistsRequest().indices(new String[] { index })).actionGet();
			return response.isExists();
		}finally{
			pool.release();
		}
	}

	protected final void _createIndex(String poolName, String index) throws Exception{
		PoolEs pool = getPool(poolName);
		try{
			TransportClient client = pool.acquire().v;
			client.admin().indices().prepareCreate(index).execute().actionGet();
		}finally{
			pool.release();
		}
	}

	protected final void _delIndex(String poolName, String index) throws Exception{
		PoolEs pool = getPool(poolName);
		try{
			TransportClient client = pool.acquire().v;
			client.admin().indices().prepareDelete(index).execute().actionGet();
		}finally{
			pool.release();
		}
	}

	protected final void _mappingIndex(String poolName, String index, String type, File jsonFile) throws Exception {
		PoolEs pool = getPool(poolName);
		XContentParser parser = null;
		try{
			TransportClient client = pool.acquire().v;
			parser = XContentFactory.xContent(XContentType.JSON).createParser(new FileInputStream(jsonFile));
			XContentBuilder builder = XContentFactory.jsonBuilder().copyCurrentStructure(parser);
			PutMappingRequest mappingRequest = Requests.putMappingRequest(index).type(type).source(builder);
			client.admin().indices().putMapping(mappingRequest).actionGet();
		}finally{
			IoUtils.close(parser);
			pool.release();
		}
	}

	protected final List<Map<String,Object>> _multiGet(String poolName, String index, String type, Set<String> keySet) throws Exception {
		PoolEs pool = getPool(poolName);
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		try{
			TransportClient client = pool.acquire().v;
			MultiGetRequestBuilder muiltRequest = client.prepareMultiGet();
			for(String key : keySet){
				muiltRequest.add(index, type, key);
			}
			MultiGetResponse multiResps = muiltRequest.get();
			for(MultiGetItemResponse item : multiResps){
				GetResponse resp = item.getResponse();
				if(resp == null || !resp.isExists()){
					continue;
				}
				list.add(resp.getSourceAsMap());
			}
			return list;
		}finally{
			pool.release();
		}
	}

	protected final void _blukDelete(String poolName, String index, String type, Set<String> keySet) throws Exception {
		PoolEs pool = getPool(poolName);
		try{
			TransportClient client = pool.acquire().v;
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			for(String key : keySet){
				DeleteRequestBuilder delRequest = client.prepareDelete(index, type, key);
				bulkRequest.add(delRequest);
			}
			BulkResponse bulkResp = bulkRequest.execute().actionGet();
			if(bulkResp.hasFailures()){
				LOG.warn("删除失败数据：" + bulkResp.buildFailureMessage());
			}
		}finally{
			pool.release();
		}
	}

	protected Set<BulkItemResponse> _bulkUpdate(String poolName, String index, String type, Map<String,Map<String,Object>> data) throws Exception {
		Set<BulkItemResponse> conflictSet = new HashSet<>();
		PoolEs pool = getPool(poolName);
		try{
			TransportClient client = pool.acquire().v;
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			for(Entry<String, Map<String, Object>> entry : data.entrySet()){
				UpdateRequestBuilder updateRequest = client.prepareUpdate(index, type, entry.getKey()).setDoc(entry.getValue());
				//清除 client.prepareDelete(config.getEsIndex(),config.getEsType(), entry.getKey())
				bulkRequest.add(updateRequest);
			}

			//VersionConflictEngineException可以处理掉
			BulkResponse bulkResp = bulkRequest.execute().actionGet();
			if(bulkResp.hasFailures()){
				BulkItemResponse[] items = bulkResp.getItems();
				for(BulkItemResponse item : items){
					if(item.getFailure().getCause() instanceof VersionConflictEngineException){
						conflictSet.add(item);
					}else{
						LOG.warn("更新失败数据：" + item.getFailureMessage());
					}
				}
			}
		}finally{
			pool.release();
		}
		if(!conflictSet.isEmpty()){
			LOG.warn("[VersionConflictEngineException]返回更新失败数据");
		}
		return conflictSet;
	}

	protected final void _bulkInsert(String poolName, String index, String type, Map<String,Map<String,Object>> data) throws Exception {
		PoolEs pool = getPool(poolName);
		try{
			TransportClient client = pool.acquire().v;
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			for(Entry<String, Map<String, Object>> entry : data.entrySet()){
				IndexRequestBuilder indexRequest = client.prepareIndex(index, type,entry.getKey()).setSource(entry.getValue());
				bulkRequest.add(indexRequest);
			}

			BulkResponse bulkResp = bulkRequest.execute().actionGet();
			if(bulkResp.hasFailures()){
				LOG.warn("新增失败数据：" + bulkResp.buildFailureMessage());
			}
		}finally{
			pool.release();
		}
	} 

	private PoolEs getPool(String poolName) throws WarnException{
		PoolEs pool = (PoolEs)PoolManager.getPool(poolName);
		if(pool == null){
			throw new WarnException(poolName + "连接池不存在"); 
		}
		return pool;
	}
}