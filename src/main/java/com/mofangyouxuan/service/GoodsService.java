package com.mofangyouxuan.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.dto.Category;
import com.mofangyouxuan.dto.Goods;
import com.mofangyouxuan.wx.utils.HttpUtils;
import com.mofangyouxuan.wx.utils.ObjectToMap;

/**
 * 商品管理服务
 * @author jeekhan
 *
 */
@Component
public class GoodsService {
	private static String mfyxServerUrl;
	private static String goodsAddUrl;
	private static String goodsUpdateUrl;
	private static String goodsGetUrl;
	private static String goodsGetallUrl;
	private static String goodsChangeStatusUrl;
	private static String goodsChangeStockUrl;
	private static String goodsCategoryUrl;
	
	@Value("${mfyx.mfyx-server-url}")
	public void setMfyxServerUrl(String mfyxServerUrl) {
		GoodsService.mfyxServerUrl = mfyxServerUrl;
	}
	
	@Value("${mfyx.goods-add-url}")
	public void setGoodsAddUrl(String goodsAddUrl) {
		GoodsService.goodsAddUrl = goodsAddUrl;
	}
	@Value("${mfyx.goods-update-url}")
	public void setGoodsUpdateUrl(String goodsUpdateUrl) {
		GoodsService.goodsUpdateUrl = goodsUpdateUrl;
	}
	@Value("${mfyx.goods-get-url}")
	public void setGoodsGetRrl(String url) {
		GoodsService.goodsGetUrl = url;
	}
	@Value("${mfyx.goods-getall-url}")
	public void setGoodsGetallRrl(String url) {
		GoodsService.goodsGetallUrl = url;
	}
	@Value("${mfyx.goods-change-status-url}")
	public void setGoodsChangeStatusUrl(String goodsChangeStatusUrl) {
		GoodsService.goodsChangeStatusUrl = goodsChangeStatusUrl;
	}
	@Value("${mfyx.goods-change-stock-url}")
	public void setGoodsChangeStockUrl(String goodsChangeStockUrl) {
		GoodsService.goodsChangeStockUrl = goodsChangeStockUrl;
	}
	@Value("${mfyx.goods-category-url}")
	public void setGoodsCategoryUrl(String goodsCategoryUrl) {
		GoodsService.goodsCategoryUrl = goodsCategoryUrl;
	}
	
	/**
	 * 
	 * @param goodsId
	 * @return {"errcode":-1,"errmsg":"错误信息"} 或 {商品所有字段}
	 */
	public static Goods getGoods(Long goodsId) {
		String url = mfyxServerUrl + goodsGetUrl + goodsId;
		String strRet = HttpUtils.doGet(url);
		Goods goods = null;
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			if(jsonRet.containsKey("goodsId")) {//成功
				goods = JSONObject.parseObject(strRet, Goods.class);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return goods;
	}
	
	/**
	 * 
	 * @param jsonSearchParams
	 * @param jsonSortParams
	 * @param jsonPageCond
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	public static JSONObject searchGoods(String jsonSearchParams,String jsonSortParams,String jsonPageCond) {
		String url = mfyxServerUrl + goodsGetallUrl;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("jsonSearchParams", jsonSearchParams);
		params.put("jsonSortParams", jsonSortParams);
		params.put("jsonPageCond", jsonPageCond);
		String strRet = HttpUtils.doPost(url, params);
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param goodsIds
	 * @param partnerId
	 * @param newStatus
	 * @return {errcode:0,errmsg:"ok"}
	 */
	public static JSONObject changeStatus(String goodsIds,Integer partnerId,String newStatus) {
		String url = mfyxServerUrl + goodsChangeStatusUrl;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("goodsIds", goodsIds);
		params.put("partnerId", partnerId);
		params.put("newStatus", newStatus);
		String strRet = HttpUtils.doPost(url, params);
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param partnerId
	 * @param goodsId
	 * @param newCnt
	 * @return {errcode:0,errmsg:"ok"}
	 */
	public static JSONObject changeStock(Integer partnerId,Long goodsId,Integer newCnt) {
		String url = mfyxServerUrl + goodsChangeStockUrl;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("goodsId", goodsId);
		params.put("partnerId", partnerId);
		params.put("newCnt", newCnt);
		String strRet = HttpUtils.doPost(url, params);
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param goods
	 * @return {errcode:0,errmsg:'ok',goodsId:111}
	 */
	public static JSONObject addGoods(Goods goods) {
		String url = mfyxServerUrl + goodsAddUrl;
		Map<String, Object> params = new HashMap<String,Object>();
		String[] excludeFields = {"updateTime","reviewLog","reviewOpr","reviewTime","reviewResult"};
		params = ObjectToMap.object2Map(goods,excludeFields,false);
		String strRet = HttpUtils.doPost(url, params);
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param goods
	 * @return {errcode:0,errmsg:'ok',goodsId:111}
	 */
	public static JSONObject updateGoods(Goods goods) {
		String url = mfyxServerUrl + goodsUpdateUrl;
		Map<String, Object> params = new HashMap<String,Object>();
		String[] excludeFields = {"updateTime","reviewLog","reviewOpr","reviewTime","reviewResult"};
		params = ObjectToMap.object2Map(goods,excludeFields,false);
		String strRet = HttpUtils.doPost(url, params);
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			return jsonRet;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 
	 * @return 
	 */
	public static List<Category> getCategories(){
		String url = mfyxServerUrl + goodsCategoryUrl;
		String strRet = HttpUtils.doGet(url);
		try {
			JSONObject jsonRet = JSONObject.parseObject(strRet);
			if(jsonRet.containsKey("errcode") && jsonRet.getIntValue("errcode") ==0) {
				return JSONArray.parseArray(jsonRet.getJSONArray("categories").toJSONString(), Category.class);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}