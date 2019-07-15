package com.uway.domcv.component;

import org.springframework.stereotype.Component;

//给容器中加入我们自己定义的ErrorAttributes
@Component
public class MyErrorAttributes {

    //返回值的map就是页面和json能获取的所有字段



   /* public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Map<String, Object> map = super.getErrorAttributes(request, includeStackTrace);
        map.put("company","atguigu");

        //我们的异常处理器携带的数据
        //Map<String,Object> ext = (Map<String, Object>) requestAttributes.getAttribute("ext", 0);
        //map.put("ext",ext);
        return map;
    }*/
}
