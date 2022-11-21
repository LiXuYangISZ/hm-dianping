package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SendSmsUtil;
import com.hmdp.utils.SystemConstants;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 3.如果符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到session
        session.setAttribute("code"+phone,code);
        // 5.发送验证码
        String[] phoneNumber = new String[1];
        String[] templateParam = new String[2];
        phoneNumber[0] = phone;
        templateParam[0] = code;
        templateParam[1] = "5";
        SendSmsUtil.sendSms(phoneNumber,templateParam);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.验证手机号是否正确
        String code = loginForm.getCode();
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误！");
        }
        // 2.查询手机号对应的验证码是否一致（存在）
        String cacheCode = (String) session.getAttribute("code" + loginForm.getPhone());
        if(cacheCode != null && !cacheCode.equals(code)){
            // 3.不一致，报错
            return Result.fail("验证码错误！");
        }

        // 4.一致，根据手机号查询对应的用户
        User user = this.query().eq("phone", phone).one();
        // 5.判断用户是否存在
        if(user == null){
            // 6.不存在，则创建新用户，并保存到数据库
            user  = createUserWithPhone(phone);
        }
        // 7.存在，则保存用户到session
        session.setAttribute("user",user);
        //注意，这里是否需要返回登录成功的凭证信息呢？
        //不需要，因为登录或注册后会在session中存放user,一个session对应一个sessionID，sessionID会被自动放到Cookie
        //下次请求时，Cookie会带着SessionID找到对应的session
        return Result.ok();
    }

    /**
     * 创建新用户
     * @param phone
     * @return
     */
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        this.save(user);
        return user;
    }
}
