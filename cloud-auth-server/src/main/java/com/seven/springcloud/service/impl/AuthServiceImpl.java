package com.seven.springcloud.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seven.springcloud.dao.AuthDao;
import com.seven.springcloud.service.AuthService;
import com.seven.springcloud.entities.UserAccount;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl extends ServiceImpl<AuthDao, UserAccount> implements AuthService {
    @Override
    public boolean sendMessage(String phoneNum, Map<String, Object> code) {

        if(StringUtils.isEmpty(phoneNum)) {
            return false;
        }

        /**创建阿里云连接
         * @Param regionld 默认为default
         * @Param accessKeyId 你的accessKey id
         * @Param secret 你的accessKey秘钥
         */
        DefaultProfile profile = DefaultProfile.getProfile("default",
                "Q2AtKVxX1N3tOh3AWHHzXyx", "ZgmmX3vSlMF9GnxliXZrLxoD7053Hx");
        IAcsClient client = new DefaultAcsClient(profile);

        //创建阿里云请求
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        request.putQueryParameter("PhoneNumbers", phoneNum);           //发送的手机号
        request.putQueryParameter("SignName", "阿里云短信测试");    //申请阿里云签名名称
        request.putQueryParameter("TemplateCode", "SMS_154950909"); //短信模板
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(code));

        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return response.getHttpResponse().isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
