package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class message extends AppCompatActivity {
    List arr;
    String temp_hl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Intent intent = getIntent();
        String id= intent.getStringExtra("id");
        String temp = "[]";
        arr = JSON.parseArray(temp);
        try {
            init_message(id);
        } catch (Exception e) {
            Toast.makeText(this,"加载失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
    public void init_message(String id) throws Exception {
        String text;
        ListView messages = findViewById(R.id.messages);
        //创建一个线程池
        ExecutorService pool = Executors.newFixedThreadPool(2);
        //创建一个有返回值的任务
        Callable c1 = new LoginCallable_5(id,0,"");
        //执行任务并获取Future对象
        Future f1 = pool.submit(c1);
        //从Future对象上获取任务的返回值，并输出到控制台
        text = f1.get().toString().replace("null","\"nope\"");
        //关闭线程池
        pool.shutdown();
        Map maps = (Map) JSON.parse(text);
        if (maps.get("code").toString().equals("200")){
            List Hot = JSON.parseArray(maps.get("hotComments").toString());
            for (int i = 0; i < Hot.size(); i++ ) {
                Map maps_temp = (Map)JSON.parse(Hot.get(i).toString());
                Map user_temp = (Map)JSON.parse(maps_temp.get("user").toString());
                temp_hl = "<font color='#FFFFFF'>" + maps_temp.get("content").toString() +  "</font>\n" + "<font color='#999999'>" + user_temp.get("nickname").toString() + "</font>";
                arr.add(temp_hl);
            }
            ArrayAdapter adapter = new ArrayAdapter(message.this, R.layout.items_messages, arr){
                public Object getItem(int position)
                {
                    return Html.fromHtml(arr.get(position).toString());
                }
            };
            messages.setAdapter(adapter);
        }
        else{
            Toast.makeText(this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url_str
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url_str, String param) {
        String result = "";
        try {
            //创建一个URL实例
            URL url = new URL(url_str + "?" + param);
            try {
                //通过URL的openStrean方法获取URL对象所表示的自愿字节输入流
                InputStream is = url.openStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");

                //为字符输入流添加缓冲
                BufferedReader br = new BufferedReader(isr);
                String data = br.readLine();//读取数据

                while (data != null) {//循环读取数据
                    result += data;
                    data = br.readLine();
                }

                br.close();
                isr.close();
                is.close();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "{\"msg\":\"获取失败，请检查网络\",\"code\":502,\"message\":\"播放失败，请检查网络\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"msg\":\"获取失败，请检查网络\",\"code\":502,\"message\":\"播放失败，请检查网络\"}";
        }
    }
    static class LoginCallable_5 implements Callable {
        String sts;
        String time;
        int type;
        LoginCallable_5(String st,int typ,String tim) {
            call();
            sts = st;
            type = typ;
            time = tim;
        }

        @Override
        public Object call() {
            String jg;
            if (type == 0){
                jg = sendGet("https://musicapi.leanapp.cn/comment/music","id=" + sts);
            }
            else{
                jg = sendGet("https://musicapi.leanapp.cn/comment/music","id=" + sts + "offset" + time);
            }
            return jg;
        }
    }

}