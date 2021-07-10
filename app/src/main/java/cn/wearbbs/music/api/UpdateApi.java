package cn.wearbbs.music.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import cn.wearbbs.music.util.NetWorkUtil;

public class UpdateApi {
    private String result;
    public JSONObject checkUpdate() throws InterruptedException {
        Thread tmp = new Thread(() -> result = NetWorkUtil.sendByGetUrl("https://wearbbs-wearmusic-1253496522.cos.ap-beijing.myqcloud.com/checkUpdate.json",""));
        tmp.start();
        tmp.join();
        return JSON.parseObject(result);
    }
}
