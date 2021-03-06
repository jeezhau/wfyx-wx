<!DOCTYPE html>
<html lang="zh-CN">
<head>
   <#include "/head/page-common-head.ftl" encoding="utf8">
</head>
<body class="light-gray-bg">
<#include "/common/tpl-msg-alert.ftl" encoding="utf8">
<#include "/common/tpl-loading-and-nomore-data.ftl" encoding="utf8">
<#include "/user/tpl-ajax-login-modal.ftl" encoding="utf8">

<#if ((vipBasic.status)!'') == '1'>
<div class="container" id="container" style="padding:0px 0px;oveflow:scroll">
  <div class="row" style="width:100%;margin:0px 0px 0px 0px;padding:5px 8px;background-color:white;" >
    <p style="color:red">&nbsp;&nbsp;&nbsp;&nbsp;在本系统中的会员将有一个会员管理密码，修改银行账户信息、余额支付、取消、退款、提现等都需要使用该密码，请谨慎设置和保管，不要向任何人透露，我们的工作人员绝不会以任何理由要求您提供您的密码！！！初始设置请使用忘记密码！</p>
    	<form class="form-horizontal" method ="post" autocomplete="on" enctype="multipart/form-data" role="form" >  
      <div class="form-group">
        <label class="col-xs-3 control-label">原密码<span style="color:red">*</span></label>
        <div class="col-xs-8">
          <input type="password" class="form-control" v-model="param.oldPwd" pattern="\w{6,20}" maxLength=20 required placeholder="请输入原密码（6-20个字符）">
        </div>
      </div>
      <div class="form-group">
        <label class="col-xs-3 control-label">新密码<span style="color:red">*</span></label>
        <div class="col-xs-8">
          <input type="password" class="form-control" v-model="param.newPwd" pattern="\w{6,20}" maxLength=20 required placeholder="请输入新密码（6-20个字符）">
        </div>
      </div>
      <div class="form-group">
        <label class="col-xs-3 control-label">确认密码<span style="color:red">*</span></label>
        <div class="col-xs-8">
          <input type="password" class="form-control" v-model="param.reNewPwd" pattern="\w{6,20}" maxLength=20 required placeholder="请输入确认密码（6-20个字符）">
        </div>
      </div>       
      <div class="form-group">
         <div style="text-align:center">
           <button type="button" class="btn btn-info"  style="margin:20px" @click="submit">&nbsp;&nbsp;提 交&nbsp;&nbsp;</button>
         </div>
      </div>
	</form>
	<a href="javascript:;" @click="resetpwd">忘记密码？</a>
  </div>
</div><!-- end of container -->
<script type="text/javascript">
var containerVue = new Vue({
	el:'#container',
	data:{
		param:{
			oldPwd:'',
			newPwd:'',
			reNewPwd:'',
		}
	},
	methods:{
		submit: function(){
			$("#dealingData").show();
			//密码强度正则，6-20位，包括至少1个大写字母，1个小写字母，1个数字
			var pPattern = /^.*(?=.{6,20})(?=.*\d)(?=.*[A-Z])(?=.*[a-z]).*$/;
			if(!pPattern.exec(this.param.newPwd)){
				alertMsg('错误提示','新密码格式不正确(6-20位，包括至少1个大写字母，1个小写字母，1个数字)！');
				return;
			}
			if(this.param.newPwd != this.param.reNewPwd){
				alertMsg('错误提示','新密码与确认密码格式不一致！');
				return;
			}
			if(!this.param.oldPwd){
				alertMsg('错误提示','原密码不可为空！');
				return;
			}
			$.ajax({
				url: '/vip/vipset/updpwd',
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
							window.location.href = "/vip/vipset";
						}
					}else{
						alertMsg('错误提示','设置会员密码失败！')
					}
				},
				failure:function(){
					$("#dealingData").hide();
				},
				dataType: 'json'
			});
		},
		resetpwd:function(){
			$('#resetPwdModal').modal('show');
			resetPwdVue.param.type = '';
		}
	}
});
</script>

<!-- 重置密码（Modal） -->
<div class="modal fade " id="resetPwdModal" tabindex="-1" role="dialog" aria-labelledby="resetPwdTitle" aria-hidden="false" data-backdrop="static" style="top:20%">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"  aria-hidden="true">× </button>
        <h4 class="modal-title" id="resetPwdTitle" style="color:red">重置密码</h4>
      </div>
      <div class="modal-body">
        <p>密码接收媒介为用户基本信息中绑定的手机或邮箱；提交之后，系统将随机生成一个密码并发送到您选择的已绑定媒介，在收到该密码之后请尽快完成对该密码的修改，以确保安全！特别提示：不要频繁使用该操作，频繁操作将可能导致重置密码不可用！</p>
        <div class="row" style="padding:0 18px 0 18px">
          <div class="form-group">
		    <label class="col-xs-4 control-label" style="padding:0 1px">密码接收媒介<span style="color:red">*</span></label>
			<div class="col-xs-4" style="padding:0 1px">
		      <label><input type="radio" value="1" v-model="param.type" style="display:inline-block;width:18px;height:18px" >手机</label>
		      <label><input type="radio" value="2" v-model="param.type" style="display:inline-block;width:18px;height:18px" >邮箱</label>
			</div>
		  </div> 
		</div>
      </div>
      <div class="modal-footer">
     	<div style="text-align:center">
           <button v-if="isSending == 0" type="button" class="btn btn-info"  @click="submit">&nbsp;&nbsp;提 交&nbsp;&nbsp;</button>
           <button v-if="isSending == 1" type="button" class="btn btn-info"  >发送中...</button>
         </div>
      </div>
  </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<script type="text/javascript">
 var resetPwdVue = new Vue({
	 el:'#resetPwdModal',
	 data:{
		 isSending:0,
		 param:{
			 type:'',
		 }
	 },
	 methods:{
		 submit: function(){
			 if(!this.param.type){
				alertMsg('错误提示','请选择密码接收媒介！');
				return;
			 }
			 this.isSending = 1;
			 $.ajax({
					url: '/vip/vipset/resetpwd',
					method:'post',
					data: this.param,
					success: function(jsonRet,status,xhr){
						resetPwdVue.isSending = 0;
						$('#resetPwdModal').modal('hide');
						if(jsonRet && jsonRet.errmsg){
							if(jsonRet.errcode !== 0){
								if(jsonRet.errcode === -100000){
									$('#ajaxLoginModal').modal('show');
								}else{
									alertMsg('错误提示',jsonRet.errmsg);
								}
							}else{
								var msg = '新的随机重置密码已经发送至您绑定的' + (resetPwdVue.param.type=='1'?'手机':'邮箱') +  '，请尽快完成对重置密码的修改！';
								alertMsg('系统提示',msg);
							}
						}else{
							alertMsg('错误提示','设置会员密码失败！')
						}
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

<footer>
  <#include "/menu/page-bottom-menu.ftl" encoding="utf8"> 
</footer>

</body>
</html>
