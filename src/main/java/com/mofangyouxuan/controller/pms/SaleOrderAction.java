package com.mofangyouxuan.controller.pms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.dto.Aftersale;
import com.mofangyouxuan.dto.Appraise;
import com.mofangyouxuan.dto.Order;
import com.mofangyouxuan.dto.PartnerBasic;
import com.mofangyouxuan.dto.PartnerStaff;
import com.mofangyouxuan.dto.PayFlow;
import com.mofangyouxuan.dto.VipBasic;
import com.mofangyouxuan.service.AftersaleService;
import com.mofangyouxuan.service.AppraiseService;
import com.mofangyouxuan.service.OrderService;
import com.mofangyouxuan.utils.PageCond;

/**
 * 
 * 合作伙伴销售订单管理
 * 1、包含三部分：订单查询、备货与发货、评价用户
 * @author jeekhan
 *
 */
@Controller
@RequestMapping("/psaleorder")
@SessionAttributes({"sys_func","partnerUserTP","partnerPasswd","partnerStaff","partnerBindVip","myPartner"})
public class SaleOrderAction {
	
	private String[] statusArr = new String[]{"all","4pay","4delivery","4sign","4appraise","4refund","4exchange"};
	
	@Value("${sys.local-server-url}")
	private String localServerName;
	
	
	/**
	 * 获取商家的销售订单显示界面
	 * @param status 订单状态:all(全部)、forPay（待付款）、forDlivery（待发货）、
	 * 				forTake（待收货）、forAppraise（待评价）、forRefund（待退款）
	 * @return
	 */
	@RequestMapping("/show/{status}")
	public String showAll(@PathVariable("status")String status,ModelMap map) {
		PartnerBasic myPartner = (PartnerBasic) map.get("myPartner");
		if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus()))) {
			map.put("errmsg", "您还未开通合作伙伴或状态限制！");
			return "redirect:/partner/manage" ;
		}
		
		boolean flag = false;
		for(String s:statusArr) {
			if(s.equals(status)) {
				flag = true; break;
			}
		}
		if(!flag) {
			status = "all";
		}
		map.put("status", status);
		map.put("sys_func", "partner-saleorder");
		return "porder/page-porder-show";
	}
	
	
	/**
	 * 获取指定状态的所有订单
	 * @param status
	 * @param map
	 * @return {errcode:0,errmsg:"ok",pageCond:{},datas:[{}...]} 
	 */
	@RequestMapping("/getall")
	@ResponseBody
	public String getAll(@RequestParam(required=true)String status,PageCond pageCond,
			ModelMap map) {
		JSONObject jsonRet = new JSONObject();
		PartnerBasic myPartner = (PartnerBasic) map.get("myPartner");
		if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
			jsonRet.put("errcode", ErrCodes.PARTNER_NO_EXISTS);
			jsonRet.put("errmsg", "您还未开通合作伙伴功能！");
			return jsonRet.toJSONString();
		}
		try {
			String statCode = "";
			boolean flag = false;
			for(String stat:statusArr) {
				if(stat.equals(status)) {
					flag = true;
					if("all".equals(stat)) {
						statCode = null;
					}else if("4pay".equals(stat)) {
						statCode = "10,12";
					}else if("4delivery".equals(stat)) {
						statCode = "20,21";
					}else if("4sign".equals(stat)) {
						statCode = "30";
					}else if("4appraise".equals(stat)) {
						statCode = "41,56";
					}else if("4refund".equals(stat)) { //可申请退款与已经进行中
						statCode = "21,22,30,31,40,41,50,51,52,53,54,55,56,60,61,62,63,64,65";
					}else if("4exchange".equals(stat)) { //可申请退货与已经进行中
						statCode = "40,41,50,51,52,53,54,55,56";
					}
					break;
				}
			}
			if(!flag) {
				statCode = null;
			}
			JSONObject params = new JSONObject();
			
			params.put("partnerId", myPartner.getPartnerId());
			params.put("status", statCode);
			params.put("incart", 0);
			JSONObject sortParams = new JSONObject();
			sortParams.put("createTime", "1#1");
			JSONObject showGroups = new JSONObject();
			//needReceiver,needLogistics,needGoodsAndUser
			showGroups.put("needReceiver", false);
			showGroups.put("needLogistics", false);
			showGroups.put("needGoodsAndUser", true);
			jsonRet = OrderService.searchOrders(showGroups.toJSONString(),params.toJSONString(), sortParams.toString(), JSONObject.toJSONString(pageCond));
			if(jsonRet == null || !jsonRet.containsKey("errcode")) {
				jsonRet = new JSONObject();
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "获取订单信息失败！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
		
	}
		
	/**
	 * 备货与取消备货
	 * @param orderId
	 * @return
	 */
	@RequestMapping("/ready/{orderId}")
	@ResponseBody
	public String readyOrUnOrder(@PathVariable(value="orderId")String orderId,ModelMap map) {
		JSONObject jsonRet = new JSONObject();
		try {
			PartnerBasic myPartner = (PartnerBasic) map.get("myPartner");
			String partnerUserTP = (String) map.get("partnerUserTP");
			//String partnerPasswd = (String) map.get("partnerPasswd");
			VipBasic vip = (VipBasic) map.get("partnerBindVip");
			PartnerStaff staff = (PartnerStaff) map.get("partnerStaff");
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus()))) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您还未开通合作伙伴或状态限制！");
				return jsonRet.toJSONString();
			}
			//Integer updateOpr = null;
			if("bindVip".equals(partnerUserTP)) {
				if(vip == null || !"1".equals(vip.getStatus())) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "系统获取您的会员信息失败！");
					return jsonRet.toJSONString();
				}
				//updateOpr = vip.getVipId();
			}else {
				if(staff == null || staff.getPartnerId() == null) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "系统获取您的员工信息失败！");
					return jsonRet.toJSONString();
				}
				//updateOpr = staff.getUserId();
			}
			jsonRet = OrderService.getOrder( orderId);
			if(jsonRet == null || !jsonRet.containsKey("errcode")) {
				jsonRet = new JSONObject();
				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
				jsonRet.put("errmsg", "获取订单信息失败！");
			}
			Order order = JSONObject.toJavaObject(jsonRet.getJSONObject("order"), Order.class);
			if(!"20".equals(order.getStatus()) && !"21".equals(order.getStatus())) {
				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
				jsonRet.put("errmsg", "该订单当前不可进行备货管理！");
				return jsonRet.toJSONString();
			}
			jsonRet = OrderService.readyGoods(order, myPartner);
			if(jsonRet == null || !jsonRet.containsKey("errcode")) {
				jsonRet.put("errcode",ErrCodes.COMMON_EXCEPTION);
				jsonRet.put("errmsg", "出现系统错误！");
				return jsonRet.toString();
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 获取订单发货页面
	 * @param orderId
	 * @param map
	 * @return
	 */
	@RequestMapping("/delivery/begin/{orderId}")
	public String beginDelivery(@PathVariable("orderId")String orderId,ModelMap map) {
		Order order = null;
		try {
			PartnerBasic myPartner = (PartnerBasic) map.get("myPartner");
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
				map.put("errmsg", "您还未开通合作伙伴功能或状态限制！");
				return "porder/page-porder-delivery";
			}
			JSONObject jsonRet = OrderService.getOrder(orderId);
			
			if(jsonRet != null && jsonRet.containsKey("order")) {
				order = JSONObject.toJavaObject(jsonRet.getJSONObject("order"),Order.class);
				if(!myPartner.getPartnerId().equals(order.getPartnerId())) {
					map.put("errmsg", "该订单不是您的销售订单！");
				}else {
					if(!"20".equals(order.getStatus()) && !"21".equals(order.getStatus())) {
						map.put("errmsg", "您当前不可对该订单进行发货管理！");
					}else {
						map.put("order", order);
					}
				}
			}else {
				map.put("errmsg", "获取订单信息失败！");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			map.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return "porder/page-porder-delivery";
	}
	
	/**
	 * 立即提交发货信息
	 * 
	 * @param orderId
	 * @param logisticsComp	配送方名称
	 * @param logisticsNo	物流单号(快递单号)
	 * @param map
	 * @return
	 */
	@RequestMapping("/delivery/submit/{orderId}")
	@ResponseBody
	public String submitDelivery(@PathVariable(value="orderId")String orderId,
			@RequestParam(value="logisticsComp",required=true)String logisticsComp,
			@RequestParam(value="logisticsNo",required=true)String logisticsNo,
			ModelMap map) {
		JSONObject jsonRet = new JSONObject();
		try {
			PartnerBasic myPartner = (PartnerBasic) map.get("myPartner");
			String partnerUserTP = (String) map.get("partnerUserTP");
			String partnerPasswd = (String) map.get("partnerPasswd");
			VipBasic vip = (VipBasic) map.get("partnerBindVip");
			PartnerStaff staff = (PartnerStaff) map.get("partnerStaff");
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus()))) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您还未开通合作伙伴或状态限制！");
				return jsonRet.toJSONString();
			}
			Integer updateOpr = null;
			if("bindVip".equals(partnerUserTP)) {
				if(vip == null || !"1".equals(vip.getStatus())) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "系统获取您的会员信息失败！");
					return jsonRet.toJSONString();
				}
				updateOpr = vip.getVipId();
			}else {
				if(staff == null || staff.getPartnerId() == null) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "系统获取您的员工信息失败！");
					return jsonRet.toJSONString();
				}
				updateOpr = staff.getUserId();
			}
//			
//			jsonRet = OrderService.getOrder(true, null, null, null, true, orderId);
//			if(jsonRet == null || !jsonRet.containsKey("errcode")) {
//				jsonRet = new JSONObject();
//				jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
//				jsonRet.put("errmsg", "获取订单信息失败！");
//			}
//			Order order = JSONObject.toJavaObject(jsonRet.getJSONObject("order"), Order.class);
//			if(!"20".equals(order.getStatus()) && !"21".equals(order.getStatus())) {
//				jsonRet.put("errcode", ErrCodes.ORDER_PARAM_ERROR);
//				jsonRet.put("errmsg", "该订单当前不可进行发货管理！");
//				return jsonRet.toJSONString();
//			}
			jsonRet = OrderService.deliveryGoods(myPartner, orderId, logisticsComp, logisticsNo,updateOpr,partnerPasswd);
			if(jsonRet == null || !jsonRet.containsKey("errcode")) {
				jsonRet.put("errcode",ErrCodes.COMMON_EXCEPTION);
				jsonRet.put("errmsg", "出现系统错误！");
				return jsonRet.toString();
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode", ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return jsonRet.toString();
	}
	
	/**
	 * 卖家获取订单评价页面
	 * @param orderId
	 * @param map
	 * @return
	 */
	@RequestMapping("/appraise/begin/{orderId}")
	public String beginAppr4User(@PathVariable("orderId")String orderId,ModelMap map) {
		Order order = null;
		try {
			PartnerBasic myPartner = (PartnerBasic) map.get("myPartner");
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
				map.put("errmsg", "您还未开通合作伙伴功能！");
				return "porder/page-porder-appraise";
			}
			
			JSONObject jsonRet = OrderService.getOrder(orderId);
			JSONObject jsonRetAppr = AppraiseService.getAppraise(orderId, "2");
			if(jsonRet != null && jsonRet.containsKey("order")) {
				order = JSONObject.toJavaObject(jsonRet.getJSONObject("order"),Order.class);
				if(!myPartner.getPartnerId().equals(order.getPartnerId())) {
					map.put("errmsg", "该订单不是您的销售订单！");
				}else {
					if(!"41".equals(order.getStatus()) && !"57".equals(order.getStatus())) {
						map.put("errmsg", "您当前不可对订单进行评价！");
					}else {
						if(jsonRetAppr != null && jsonRetAppr.containsKey("appraise")) {
							Appraise appraise = JSONObject.toJavaObject(jsonRetAppr.getJSONObject("appraise"), Appraise.class);
							map.put("appraise", appraise);
						}
						map.put("order", order);
					}
				}
			}else {
				map.put("errmsg", "获取订单信息失败！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			map.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return "porder/page-porder-appraise";
	}
	
	/**
	 * 卖家提交订单评价
	 * @param orderId
	 * @param map
	 * @return
	 */
	@RequestMapping("/appraise/submit/{orderId}")
	@ResponseBody
	public String submitAppr4User(@PathVariable("orderId")String orderId,
			Integer score, String content,ModelMap map) {
		JSONObject jsonRet = new JSONObject();
		try {
			PartnerBasic myPartner = (PartnerBasic) map.get("myPartner");
			String partnerUserTP = (String) map.get("partnerUserTP");
			String partnerPasswd = (String) map.get("partnerPasswd");
			VipBasic vip = (VipBasic) map.get("partnerBindVip");
			PartnerStaff staff = (PartnerStaff) map.get("partnerStaff");
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus()))) {
				jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
				jsonRet.put("errmsg", "您还未开通合作伙伴或状态限制！");
				return jsonRet.toJSONString();
			}
			Integer updateOpr = null;
			if("bindVip".equals(partnerUserTP)) {
				if(vip == null || !"1".equals(vip.getStatus())) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "系统获取您的会员信息失败！");
					return jsonRet.toJSONString();
				}
				updateOpr = vip.getVipId();
			}else {
				if(staff == null || staff.getPartnerId() == null) {
					jsonRet.put("errcode", ErrCodes.COMMON_PRIVILEGE_ERROR);
					jsonRet.put("errmsg", "系统获取您的员工信息失败！");
					return jsonRet.toJSONString();
				}
				updateOpr = staff.getUserId();
			}
			
//			jsonRet = OrderService.getOrder(true, true, true, true, true, orderId);
//			if(jsonRet == null || !jsonRet.containsKey("order")) {
//				jsonRet.put("errmsg", "系统中没有该订单信息！");
//				jsonRet.put("errcode", ErrCodes.ORDER_NO_EXISTS);
//				return jsonRet.toString();
//			}
//			
//			Order order = JSONObject.toJavaObject(jsonRet.getJSONObject("order"), Order.class);
//			if(!myPartner.getPartnerId().equals(order.getPartnerId())) {
//				jsonRet.put("errmsg", "您没有权限查询该订单信息！");
//				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
//				return jsonRet.toString();
//			}
//			if(!"30".equals(order.getStatus()) && !"31".equals(order.getStatus()) && 
//					!"40".equals(order.getStatus()) && !"41".equals(order.getStatus()) && 
//					!"54".equals(order.getStatus()) && !"55".equals(order.getStatus()) && !"56".equals(order.getStatus())) {
//				jsonRet.put("errmsg", "您当前不可对该订单进行评价！");
//				jsonRet.put("errcode", ErrCodes.ORDER_PRIVILEGE_ERROR);
//				return jsonRet.toString();
//			}
			//发送请求
			jsonRet = AppraiseService.appr2User(myPartner, orderId, score, content,updateOpr,partnerPasswd);
			if(jsonRet == null || !jsonRet.containsKey("errcode")) {
				jsonRet.put("errcode",ErrCodes.COMMON_EXCEPTION);
				jsonRet.put("errmsg", "出现系统错误！");
				return jsonRet.toString();
			}
		}catch(Exception e) {
			e.printStackTrace();
			jsonRet.put("errcode",ErrCodes.COMMON_EXCEPTION);
			jsonRet.put("errmsg", "出现异常，异常信息：" + e.getMessage());
			jsonRet.toString();
		}
		return jsonRet.toString();
	}
	
	/**
	 * 显示订单详情：仅买卖双方可看到
	 * @param orderId
	 * @param map
	 */
	@RequestMapping("/detail/{orderId}")
	public String showDetail(@PathVariable("orderId")String orderId,ModelMap map) {
		
		PartnerBasic myPartner= (PartnerBasic) map.get("myPartner");
		try {
			if(myPartner == null || myPartner.getPartnerId() == null) {
				map.put("errmsg", "您没有权限查询该订单信息！");
				return "";
			}
			JSONObject jsonRet = OrderService.getOrder(orderId);
			if(jsonRet == null || !jsonRet.containsKey("order")) {
				map.put("errmsg", "系统中没有该订单信息！");
				return "";
			}
			Order order = JSONObject.toJavaObject(jsonRet.getJSONObject("order"), Order.class);
			String orderBalStat = null;
			if(jsonRet.containsKey("orderBalStat")) {
				orderBalStat = jsonRet.getString("orderBalStat");
			}
			JSONObject jsonApprMcht = AppraiseService.getAppraise(orderId, "1");
			JSONObject jsonApprUser = AppraiseService.getAppraise(orderId, "2");
			JSONObject jsonaf = AftersaleService.getAftersale(orderId);
			if(myPartner.getPartnerId().equals(order.getPartnerId())) {
				jsonRet = OrderService.getPayFlow(orderId, order.getUserId(), null);
				if(jsonRet != null && jsonRet.containsKey("payFlow")) {
					PayFlow payFlow = JSONObject.toJavaObject(jsonRet.getJSONObject("payFlow"), PayFlow.class);
					map.put("payFlow", payFlow);
				}
				if(jsonApprMcht != null && jsonApprMcht.containsKey("appraise")) {
					Appraise apprMcht = JSONObject.toJavaObject(jsonApprMcht.getJSONObject("appraise"), Appraise.class);
					map.put("apprMcht", apprMcht);
				}
				if(jsonApprUser != null && jsonApprUser.containsKey("appraise")) {
					Appraise apprUser = JSONObject.toJavaObject(jsonApprUser.getJSONObject("appraise"), Appraise.class);
					map.put("apprUser", apprUser);
				}
				if(jsonaf != null && jsonaf.containsKey("aftersale")) {
					Aftersale aftersale = JSONObject.toJavaObject(jsonaf.getJSONObject("aftersale"), Aftersale.class);
					map.put("aftersale", aftersale);
				}
				map.put("orderBalStat", orderBalStat);
				map.put("order", order);
			}else {
				map.put("errmsg", "您没有权限查询该订单信息！");
			}			
		}catch(Exception e) {
			e.printStackTrace();
			map.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return "porder/page-porder-detail";
	}
	
	/**
	 * 显示物流详情：仅买卖双方可看到
	 * @param orderId
	 * @param map
	 */
	@RequestMapping("/logistics/{orderId}")
	public String showLogistics(@PathVariable("orderId")String orderId,ModelMap map) {
		PartnerBasic myPartner= (PartnerBasic) map.get("myPartner");
		try {
			if(myPartner == null || myPartner.getPartnerId() == null) {
				map.put("errmsg", "您没有权限查询该订单信息！");
				return "";
			}
			JSONObject jsonRet = OrderService.getLogistics(orderId);
			if(jsonRet == null || !jsonRet.containsKey("order")) {
				if(jsonRet.containsKey("errmsg")) {
					map.put("errmsg", jsonRet.getString("errmsg"));
				}else {
					map.put("errmsg", "系统中没有该订单信息！");
				}
				return "porder/page-porder-logistics";
			}
			Order order = JSONObject.toJavaObject(jsonRet.getJSONObject("order"), Order.class);
			if(myPartner.getPartnerId().equals(order.getPartnerId())) {
				map.put("order", order);
			}else {
				map.put("errmsg", "您没有权限查询该订单信息！");
			}			
		}catch(Exception e) {
			e.printStackTrace();
			map.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return "porder/page-porder-logistics";
	}
	
	/**
	 * 显示评价审核页面
	 * @param orderId
	 * @param map
	 */
	@RequestMapping("/appraise/review/{orderId}")
	public String showApprReview(@PathVariable("orderId")String orderId,ModelMap map) {
		try {
			PartnerBasic myPartner = (PartnerBasic) map.get("myPartner");
			if(myPartner == null || !("S".equals(myPartner.getStatus()) || "C".equals(myPartner.getStatus())) ){
				map.put("errmsg", "您还未开通合作伙伴功能！");
				return "porder/page-review-appraise";
			}
			JSONObject jsonOrder = OrderService.getOrder(orderId);
			JSONObject jsonRet = AppraiseService.getAppraise(orderId, "1");
			if(jsonRet != null && jsonRet.containsKey("appraise") && jsonOrder != null && jsonOrder.containsKey("order")) {
				Appraise appraise = JSONObject.toJavaObject(jsonRet.getJSONObject("appraise"), Appraise.class);
				if(null == appraise) {
					map.put("errmsg", "该订单评价当前不可进行审核！");
					return "porder/page-review-appraise";
				}else {
					Order order = JSONObject.toJavaObject(jsonOrder.getJSONObject("order"), Order.class);
					map.put("order", order);
					map.put("appraise", appraise);
				}
			}else {
				map.put("errmsg", "获取订单信息失败！");
			}
		}catch(Exception e) {
			e.printStackTrace();
			map.put("errmsg", "出现异常，异常信息：" + e.getMessage());
		}
		return "porder/page-review-appraise";
	}
	
}


