package cn.hellohao.controller;

import cn.hellohao.auth.token.JWTUtil;
import cn.hellohao.pojo.AppClient;
import cn.hellohao.pojo.Msg;
import cn.hellohao.pojo.UploadConfig;
import cn.hellohao.pojo.User;
import cn.hellohao.service.AppClientService;
import cn.hellohao.service.ImgService;
import cn.hellohao.service.UploadConfigService;
import cn.hellohao.service.impl.UserServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Hellohao
 * @version 1.0
 * @date 2022-05-16 32:45
 */
@RestController
@RequestMapping("/client")
public class ClientAppController {

    @Autowired
    private ImgService imgService;
    @Autowired
    private AppClientService appClientService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UploadConfigService uploadConfigService;

    @PostMapping("/loginByToken")
    @ResponseBody
    public Msg loginByToken(@RequestParam(value = "data", defaultValue = "") String data) {
        Msg msg = new Msg();
        JSONObject jsonObj = JSONObject.parseObject(data);
        JSONObject jsonObject = new JSONObject();
        try{
            User newUser = new User();
            String userToken = jsonObj.getString("userToken");
            newUser.setToken(userToken);
            User userData = userService.loginByToken(userToken);
            if(userData.getIsok()<1){
                SecurityUtils.getSubject().logout();
                msg.setInfo("账号暂时无法使用，请咨询管理员");
                msg.setCode("110403");
                return msg;
            }
            String jwtToken = JWTUtil.createToken(userData);
            UploadConfig uploadConfig = uploadConfigService.getUpdateConfig();
            jsonObject.put("username", userData.getUsername());
            jsonObject.put("jwttoken", jwtToken);
            jsonObject.put("suffix",uploadConfig.getSuffix().split(","));
            jsonObject.put("myImgTotal", imgService.countimg(userData.getId()));
            jsonObject.put("filesize",Integer.valueOf(uploadConfig.getFilesizeuser())/1024);
            jsonObject.put("imgcount",uploadConfig.getImgcountuser());
            jsonObject.put("uploadSwitch",uploadConfig.getUserclose());
            long memory = Long.valueOf(userData.getMemory());
            Long usermemory = imgService.getusermemory(userData.getId())==null?0L:imgService.getusermemory(userData.getId());
            if(memory==0){
                jsonObject.put("myMemory","无容量");
            }else{
                Double aDouble = Double.valueOf(String.format("%.2f", (((double)usermemory/(double)memory)*100)));
                if(aDouble>=999){
                    jsonObject.put("myMemory",999);
                }else{
                    jsonObject.put("myMemory",aDouble);
                }
            }
            msg.setData(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            msg.setCode("110500");
            msg.setInfo("未获取到用户，请先在设置中添加Token");
        }
        return msg;
    }

    @PostMapping("/getVersion")
    @ResponseBody
    public Msg getVersion() {
        Msg msg = new Msg();
        try {
            AppClient app = appClientService.getAppClientData("app");
            if(app.getAppupdate().equals("on")){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("winpackurl",app.getWinpackurl());
                jsonObject.put("macpackurl",app.getMacpackurl());
                msg.setData(jsonObject);
            }else{
                msg.setCode("000");
            }
        }catch (Exception e){
            msg.setCode("000");
        }
        return msg;
    }



}