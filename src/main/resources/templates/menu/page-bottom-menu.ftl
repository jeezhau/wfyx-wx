 <!-- 商城管理底部主菜单-->
  <div class="row" style="min-height:60px"></div> 
  <div class="weui-tabbar" style="position:fixed;left:0px;bottom:0px;height:60px">
    	<a href="/shop/index" class="weui-tabbar__item <#if (sys_func!'shop')=='shop'>weui-bar__item_on </#if>" >
	    <span style="display: inline-block;position: relative;">
	        <img src="/icons/首页.png" alt="" class="weui-tabbar__icon">
	    </span>
	    <p class="weui-tabbar__label">商城首页</p>
	</a>
<!--    	<a href="/dayfresh/index/today" class="weui-tabbar__item <#if (sys_func!'shop')=='dayfresh'>weui-bar__item_on </#if>" >
      <span style="display: inline-block;position: relative;">
        <img src="/img/mfyx_logo.jpeg" alt="" class="weui-tabbar__icon">
      </span>
      <p class="weui-tabbar__label">每日鲜推</p>
    </a>	 -->
	<a href="/nearby/index" class="weui-tabbar__item <#if (sys_func!'shop')=='nearby'>weui-bar__item_on </#if>" >
	    <span style="display: inline-block;position: relative;">
	        <img src="/icons/附近商家.png" alt="" class="weui-tabbar__icon">
	    </span>
	    <p class="weui-tabbar__label">优选同城</p>
	</a>
 	<a href="/srvc/about" class="weui-tabbar__item <#if (sys_func!'shop')=='srvc'>weui-bar__item_on </#if>" >
	    <span style="display: inline-block;position: relative;">
	        <img src="/icons/服务.png" alt="" class="weui-tabbar__icon">
	    </span>
	    <p class="weui-tabbar__label">服务中心</p>
	</a>
	<a href="/user/index/basic" class="weui-tabbar__item <#if (sys_func!'shop')=='user'>weui-bar__item_on </#if>" >
	    <img src="/icons/个人信息.png" alt="" class="weui-tabbar__icon">
	    <p class="weui-tabbar__label">个人中心</p>
	</a>
  </div>