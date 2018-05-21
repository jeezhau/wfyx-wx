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
    <script src="/script/common.js"></script>
</head>
<body class="light-gray-bg">

<#include "/common/tpl-loading-and-nomore-data.ftl" encoding="utf8"> 
<#include "/common/tpl-msg-alert.ftl" encoding="utf8">
<div class="container" id="container" style="padding:0;oveflow:scroll">
   <div class="row">
     <h3 class="row" style="margin:5px 0;text-align:center" >商品评价({{appr.apprCnt}})</h3>
   </div>
   <div class="row" style="margin:1px 0px;">
     <div class="row" v-for="order in appr.apprList" style="margin:1px 0;background-color:white;">
       <div class="row" style="margin:1px 2px;padding:3px 10px">
         <span class="pull-left"><img alt="头像" :src="order.headimgurl" width="20px" height="20px" style="border-radius:50%">{{order.nickname}}</span>
         <span class="pull-right">{{order.appraiseTime}}</span>
       </div>
       <#include "/order/tpl-order-buy-content-4vue.ftl" encoding="utf8">
       <div class="row" style="margin:1px 0">
         <div class="col-xs-12" v-for="sub in order.appraiseInfo">
         {{sub.time}} &nbsp;&nbsp;&nbsp;&nbsp;{{sub.content}}
         </div>
       </div>
     </div>
   </div>
  
</div><!-- end of container -->
<script type="text/javascript">
var containerVue = new Vue({
	el:'#container',
	data:{
		appr:{
			apprList:[],
			apprCnt:0,
		},
		param:{
			objNm: '${objNm}',
			objId: '${objId?string("#")}',
			begin:0,
			pageSize:100,
		}
	},
	methods:{
		getAllAppr: function(){
			 containerVue.appr.apprCnt = 0;
			 containerVue.appr.apprList = [];
			 var url = '';
			 if(this.param.objNm == 'goods'){
				 url = '/appraise/getall/goods/' + this.param.objId;
			 }else if(this.param.objNm == 'partner'){
				 url = '/appraise/getall/partner/' + this.param.objId;
			 }else{
				 return;
			 }
			 $.ajax({
					url: url,
					method:'post',
					data: this.param,
					success: function(jsonRet,status,xhr){
						if(jsonRet && jsonRet.errcode == 0){//
							for(var i=0;i<jsonRet.datas.length;i++){
								var appr = jsonRet.datas[i];
								if(appr.appraiseInfo){//有评价内容
									appr.appraiseInfo = JSON.parse(appr.appraiseInfo);
								}else{
									appr.appraiseInfo = {'time':appr.appraiseTime,'content':"卖家太懒，啥也没留下！！！"}
								}
								appr.goodsSpec = JSON.parse(appr.goodsSpec);
								appr.headimgurl = startWith(appr.headimgurl,'http')?appr.headimgurl:('/user/headimg/show/'+appr.userId)
								containerVue.appr.apprList.push(appr);
							}
							containerVue.param.begin = jsonRet.pageCond.begin;
							containerVue.appr.apprCnt = jsonRet.pageCond.count;
						}
					},
					dataType: 'json'
				});			 
		 }
	}
});
containerVue.getAllAppr();
</script>

<footer>
  <#include "/menu/page-bottom-menu.ftl" encoding="utf8"> 
</footer>

</body>
</html>