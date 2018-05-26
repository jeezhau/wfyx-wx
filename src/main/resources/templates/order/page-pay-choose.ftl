<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no">
	<title>摩放优选</title>
	<!-- Bootstrap -->
	<link href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
    <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <!--Vue -->
    <script src="https://cdn.jsdelivr.net/npm/vue"></script>
    <!-- -->
    <link href="/css/font-awesome.min.css" rel="stylesheet">
    <link href="/css/templatemo-style.css" rel="stylesheet">
    
    <script src="http://res.wx.qq.com/open/js/jweixin-1.2.0.js"></script>
    
    <link href="/css/weui.css" rel="stylesheet">
    
    <link href="/css/mfyx.css" rel="stylesheet">
    <script src="/script/common.js" type="text/javascript"></script>
</head>
<body class="light-gray-bg">
<#include "/common/tpl-msg-alert.ftl" encoding="utf8">
<#include "/common/tpl-loading-and-nomore-data.ftl" encoding="utf8">

 <#if (order.orderId)?? >
<div class="container " id="container" style="margin:0 0;padding:0;overflow:scroll">
 <!-- 收货人信息 -->
  <div class="row" style="margin:5px 1px ;padding:3px 0;background-color:white" >
    <div class="col-xs-12">
     <span>${(order.recvName)!''} , ${(order.recvPhone)!''}</span>
    </div>
    <div class="col-xs-12">
        <span>${order.recvProvince}</span> 
        <span>${order.recvCity}</span>
        <span>${order.recvArea}</span>
        <span>${order.recvAddr}</span>
     </div>
     <div class="col-xs-12">
       {{getDispatchMode(${order.dispatchMode})}}
     </div>
  </div>

  <!-- 商品信息 -->
  <div class="row" style="margin:5px 1px ;padding:3px 0;background-color:white" >
    <div class="col-xs-12" style="text-align:center;">${order.goodsName}</div>
    <div class="col-xs-12" style="text-align:center;">
      <a href="/goods/show/${(order.goodsId)?string('#')}">
       <img alt="" src="/image/file/show/${(order.mchtUId)?string('#')}/${(order.goodsMainImgPath)!''}" style="width:99%;height:150px;">
      </a>
    </div>
    <div class="col-xs-12" style="padding:0px 3px">
       <table class="table table-striped table-bordered table-condensed">
         <tr>
           <th width="30%" style="padding:2px 2px">规格名称</th>
           <th width="15%" style="padding:2px 2px">量值</th>
           <th width="15%" style="padding:2px 2px">售价(¥)</th>
           <th width="20%" style="padding:2px 2px">购买数量</th>
         </tr>
         <tr v-for="item,index in goodsSpecArr" >
           <td style="padding:2px 2px">
             <span style="width:100%" >{{item.name}}</span>
           </td>
           <td style="padding:2px 2px">
              <span style="width:100%" >{{item.val}} {{item.unit}}</span>
           </td>
           <td style="padding:2px 2px">
              <span style="width:100%" >{{item.price}}</span>
           </td> 	                         
           <td style="padding:2px 2px;text-align:center">
              <span style="width:100%" >{{item.buyNum}}</span>
           </td>
         </tr>
       </table>    
     </div>
  </div> 
  
  <!-- 官方信息 -->
  <div class="row" style="margin:5px 1px ;padding:3px 3px;" >
   <img alt="" src="/img/mfyx_logo.jpeg" width=30px height=30px style="border-radius:50%"><span style="padding:0 10px;color:red">摩放优选</span>
  </div>  
  
  <!-- 支付方式选择 -->
  <div class="row" style="">
    <div class="col-xs-12 " style="margin:1px 5px ;padding:10px 25px;background-color:white" @click="choosePay(2)">
     <img alt="" src="/icons/微信支付.png" width="20px" height=20px>
     <span>微信支付</span>
     <span v-if="param.payType == 2 " class="pull-right"><img src="/icons/选择.png" style="widht:20px;height:20px;"></span>
    </div>
    <div class="col-xs-12" style="margin:1px 5px;padding:10px 25px;background-color:white" @click="choosePay(1)">
     <img alt="" src="/icons/余额.png" width="20px" height=20px>
     <span>会员余额</span>
     <span v-if="param.payType == 1 " class="pull-right"><img src="/icons/选择.png" style="widht:20px;height:20px;"></span>
    </div>
    <div class="col-xs-12" style="margin:1px 5px;padding:10px 25px;">
      <p>注意：使用 [会员余额] 之外的第三方支付将收取下述交易额<span style="color:red"> 0.6%至0.9% </span>的手续费，该手续费付给第三方支付平台！下述实付金额不包含手续费，手续费将额外收取！</p>
    </div>
  </div>
  
  <!-- 支付 -->
<footer >
  <div class="row" style="margin:50px 0"></div>
  <div class="weui-tabbar" style="position:fixed;left:0px;bottom:5px">
    	<span class="weui-tabbar__item " >
	    <span class="weui-tabbar__label" >实付(含运费) <span style="color:red;font-size:18px">¥ ${order.amount}</span></span>
	</span>   
     <a href="javascript:;" class="weui-tabbar__item " style='background-color:red;text-align:center;vertical-align:center;'>
	    <span class="weui-tabbar__label" style="font-size:20px;color:white" @click="prepay">立即支付</span>
     </a>     	
  </div>
</footer>
</div><!-- end of container -->

<script type="text/javascript">
var containerVue = new Vue({
	el:'#container',
	data:{
		goodsSpecArr:JSON.parse('${(order.goodsSpec)!"[]"}'),
		param:{
			orderId:'${(order.orderId)}',
			payType:0 //支付方式:1-会员余额,2-微信
		}
	},
	methods:{
		choosePay:function(tp){
			this.param.payType = tp;
		},
		prepay: function(){
			$("#dealingData").show();
			if(!this.param.payType){
				alertMsg('系统提示','请先选择支付方式！');
				return;
			}
			$.ajax({
				url: '/order/prepay/' + this.param.orderId + '/' + this.param.payType,
				method:'post',
				data: {},
				success: function(jsonRet,status,xhr){
					$("#dealingData").hide();
					if(jsonRet && jsonRet.errmsg){
						if(jsonRet.errcode === 0){//创建支付成功
							if(jsonRet.payType == '1'){ //使用余额支付
								 window.location.href = "/order/pay/use/bal/" + containerVue.param.orderId;
							}
							else if(jsonRet.payType == '2'){//微信支付
								if (jsonRet.outPayUrl){
									window.location.href = jsonRet.outPayUrl;
								<#if (wxPay!'')=='1'>
								}else if(jsonRet.prepay_id){
									WeixinJSBridge.invoke(
								       'getBrandWCPayRequest', {
								           "appId":jsonRet.appId,     //公众号名称，由商户传入     
								           "timeStamp":jsonRet.timeStamp,         //时间戳，自1970年以来的秒数     
								           "nonceStr":jsonRet.nonceStr, //随机串     
								           "package":"prepay_id=" + jsonRet.prepay_id,     
								           "signType":"MD5",         //微信签名方式：     
								           "paySign":jsonRet.paySign //微信签名
										},
										function(res){     
											if(res.err_msg == "get_brand_wcpay_request:ok" ) {
												 window.location.href = "/order/pay/finish/" + containerVue.param.orderId;
											}else{
												alertMsg('系统提示','使用微信支付出现失败！'+ res.err_msg);		        	   
											}
										}
									); 
								</#if>
								}else{
									alertMsg('错误提示','调用微信支付失败！');
								}
							}
						}else{//出现逻辑错误
							alertMsg('错误提示',jsonRet.errmsg);
						}
					}else{
						alertMsg('错误提示','系统数据访问失败！');
					}
				},
				failure:function(){
					$("#dealingData").hide();
				},
				dataType: 'json'
			});
		}
	}
});

</script>
</#if>

<#if errmsg??>
 <#include "/error/tpl-error-msg-modal.ftl" encoding="utf8">
</#if>


</body>
</html>
