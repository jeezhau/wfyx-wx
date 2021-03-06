<!DOCTYPE html>
<html lang="zh-CN">
<head>
   <#include "/head/page-common-head.ftl" encoding="utf8">
</head>
<body class="light-gray-bg">
<#include "/common/tpl-msg-alert.ftl" encoding="utf8">
<#include "/common/tpl-loading-and-nomore-data.ftl" encoding="utf8">
<#include "/user/tpl-ajax-login-modal.ftl" encoding="utf8">

<#if ((userBasic.status)!'') == "1">
<div class="container" id="container" style="padding:0px 0px;oveflow:scroll">
  <div class="col-xs-12" style="margin-top:5px;background-color:white;">
     <p style="color:red">&nbsp;&nbsp;&nbsp;&nbsp;邮箱请确保是您在使用，因如果您忘记密码时将可使用该邮箱进行找回，同时该邮箱将用于接收账单等信息！！！</p>
	 <#if (userBasic.email)?? && (userBasic.email)?length gt 3>
	  <div class="row" style="margin:3px 0">
	    <label class="col-xs-4 control-label" style="padding-right:0">原邮箱<span style="color:red">*</span></label>
	    <div class="col-xs-8" style="padding-left:0">
	      <input type="email" class="form-control" v-model="param.oldEmail" disabled >
	    </div>
	  </div>	
	  <div class="row" style="margin:3px 0">
	    <label class="col-xs-4 control-label" style="padding-right:0">原邮箱验证码<span style="color:red">*</span></label>
	    <div class="col-xs-8" style="padding-left:0">
		    <div class="col-xs-7" style="padding:0">
		      <input class="form-control" v-model="param.oldVeriCode" pattern="\d{6}"  maxLength=6 required placeholder="原邮箱验证码">
		    </div>
		    <div class="col-xs-2">
		      <button v-if="time.oldTime==120 || time.oldTime <= 0 " type="button" class="btn btn-primary" @click="getVeriCode('old')">获取验证码</button>
		      <button v-if="time.oldTime<120 && time.oldTime > 0 " type="button" class="btn btn-default" > {{time.oldTime}} s </button>
		    </div>
	    </div>	    
	  </div> 
	 </#if>	
	 <div class="row" style="margin:3px 0">
	    <label class="col-xs-4 control-label" style="padding-right:0">新邮箱<span style="color:red">*</span></label>
	    <div class="col-xs-8" style="padding-left:0">
	      <input type="email" class="form-control" v-model="param.newEmail" maxLength=100 required placeholder="新邮箱">
	    </div>
	  </div>	
	  <div class="row" style="margin:3px 0">
	    <label class="col-xs-4 control-label" style="padding-right:0">新邮箱验证码<span style="color:red">*</span></label>
	    <div class="col-xs-8" style="padding-left:0">
		    <div class="col-xs-7" style="padding:0">
		      <input class="form-control" v-model="param.newVeriCode" pattern="\d{6}"  maxLength=6 required placeholder="新邮箱验证码">
		    </div>
		    <div class="col-xs-2">
		      <button v-if="time.newTime==120 || time.newTime <= 0 " type="button" class="btn btn-primary" @click="getVeriCode('new')">获取验证码</button>
		      <button v-if="time.newTime<120 && time.newTime > 0 " type="button" class="btn btn-default" > {{time.newTime}} s </button>
		    </div>
	    </div>	    
	  </div>
	  <div class="form-group">
         <div style="text-align:center">
           <button type="button" class="btn btn-info"  style="margin:20px" @click="submit">&nbsp;&nbsp;提 交&nbsp;&nbsp;</button>
         </div>
      </div>	
  </div>
</div>
<script type="text/javascript">
  var containerVue = new Vue({
	  el:'#container',
	  data:{
		  param:{
			  oldEmail:'${(userBasic.email)!''}',
			  oldVeriCode:'',
			  newEmail:'',
			  newVeriCode:''
		  },
  		  time:{
  			oldTimer: null,
  			oldTime: 120,
  			newTimer: null,
  			newTime: 120
  		  }
	  },
	  methods:{
		  submit: function(){
			  $("#dealingData").show();
			  var pattern1 = /^[A-Za-z0-9_\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
			  var pattern2 = /^\d{6}$/;
			  if(!pattern1.exec(this.param.newEmail)){
				  alertMsg('错误提示','新邮箱格式不正确！')
				  return;
			  }
			  if(!pattern2.exec(this.param.newVeriCode)){
				  alertMsg('错误提示','新邮箱验证码格式不正确！')
				  return;
			  }
			  <#if (userBasic.email)?? && (userBasic.email)?length gt 3>
			  if(!pattern2.exec(this.param.oldVeriCode)){
				  alertMsg('错误提示','原邮箱验证码格式不正确！')
				  return;
			  }
			  </#if>
			  $.ajax({
					url: '/user/setting/updemail',
					method:'post',
					data: this.param,
					success: function(jsonRet,status,xhr){
						$("#dealingData").hide();
						if(jsonRet && jsonRet.errmsg){
							if(jsonRet.errcode !== 0){
								if(jsonRet.errcode === -100000){
									$('#ajaxLoginModal').modal('show');
								}else{
									alertMsg('错误提示',jsonRet.errmsg);
								}
							}else{
								window.location.href = "/user/setting";
							}
						}else{
							alertMsg('错误提示','绑定邮箱失败！')
						}
					},
					failure:function(){
						$("#dealingData").hide();
					},
					dataType: 'json'
				});
		  },
		  getVeriCode: function(flag){
			  var email = "";
			  if(flag ==='new'){
				  email = this.param.newEmail; 
				 this.time.newTime = 120;
			  }else{
				  email = this.param.oldEmail; 
				  this.time.oldTime = 120; 
			  }
			  var pattern = /^[A-Za-z0-9_\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
			  if(!pattern.exec(email)){
				  alertMsg('错误提示','邮箱格式不正确！')
				  return;
			  }
			  veriCodeTime(flag);
			  $.ajax({
					url: '/user/vericode/email/apply',
					method:'post',
					data: {'email':email},
					success: function(jsonRet,status,xhr){
						if(jsonRet && jsonRet.errmsg){
							if(jsonRet.errcode !== 0){
								alertMsg('错误提示',jsonRet.errmsg)
							}
						}else{
							alertMsg('错误提示','获取验证码失败！')
						}
					},
					dataType: 'json'
				});
		  }
	  }
  });
  function veriCodeTime(flag){
	if(flag == 'new'){
		  if(containerVue.time.newTime == 0 && containerVue.time.newTimer){
			  clearTimeout(containerVue.time.newTimer);
		  }else{
			  containerVue.time.newTimer = setTimeout("veriCodeTime('new')",1000);
		  }
		  containerVue.time.newTime = containerVue.time.newTime - 1;
	}else{
		if(containerVue.time.oldTime == 0 && containerVue.time.oldTimer){
			  clearTimeout(containerVue.time.oldTimer);
		  }else{
			  containerVue.time.oldTimer = setTimeout("veriCodeTime('old')",1000);
		  }
		  containerVue.time.oldTime = containerVue.time.oldTime - 1;
	}
  }
  </script>
</#if>  

<#if errmsg??>
 <#include "/error/tpl-error-msg-modal.ftl" encoding="utf8">
</#if>

<footer>
  <#include "/menu/page-bottom-menu.ftl" encoding="utf8"> 
</footer>

</body>
</html>
