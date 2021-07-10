package cn.wearbbs.music.ui;

import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCAppExtendObject;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.MVApi;
import cn.wearbbs.music.api.MusicApi;
import cn.wearbbs.music.detail.Data;
import cn.wearbbs.music.util.DownloadUtil;
import cn.wearbbs.music.util.UserInfoUtil;

public class ConsoleActivity extends SlideBackActivity {
    String id;
    String name;
    String artists;
    String song;
    String url;
    int type;
    String coverurl;
    String comment;
    boolean is_like = false;
    String mvid;
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        Intent intent = getIntent();
        type = intent.getIntExtra("type",Data.fmMode);
        if(getIntent().getBooleanExtra("repeatOne",false)){
            ImageView iv_repeat = findViewById(R.id.iv_repeat);
            iv_repeat.setImageResource(R.drawable.icon_repeat_one);
        }
        if(type== Data.defaultMode || type==Data.fmMode){
            id = intent.getStringExtra("id");
            name = intent.getStringExtra("name");
            artists = intent.getStringExtra("artists");
            song = intent.getStringExtra("song");
            url = intent.getStringExtra("url");
            mvid = intent.getStringExtra("mvid");
            coverurl = intent.getStringExtra("coverUrl");
            comment = intent.getStringExtra("comment");
            LinearLayout ll_second = findViewById(R.id.ll_second);
            ll_second.setVisibility(View.VISIBLE);
            LinearLayout l3 = findViewById(R.id.ll_third);
            l3.setVisibility(View.VISIBLE);
            if("".equals(mvid)){
                ll_second.setVisibility(View.GONE);
                l3.setVisibility(View.GONE);
            }
            if(comment!=null){
                if("true".equals(comment)){
                    ll_second.setVisibility(View.VISIBLE);
                    l3.setVisibility(View.VISIBLE);
                }
            }
        }
        if(type==Data.localMode){
            LinearLayout l2 = findViewById(R.id.ll_second);
            l2.setVisibility(View.GONE);
            LinearLayout l3 = findViewById(R.id.ll_third);
            l3.setVisibility(View.GONE);
        }
        Thread thread = new Thread(()->{
            try {
                cookie = UserInfoUtil.getUserInfo(this,"cookie");
                File user = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
                BufferedReader in = new BufferedReader(new FileReader(user));
                String temp = in.readLine();
                Map user_id_temp = (Map)JSON.parse(temp);
                String user_id = user_id_temp.get("userId").toString();
                Map maps = new MusicApi().likeList(user_id,cookie);
                List list = JSON.parseArray(maps.get("ids").toString());
                for(int i = 0;i<list.size();i+=1){
                    if(list.get(i).toString().equals(id)){
                        is_like = true;
                    }
                }
                ImageView like_view = findViewById(R.id.iv_like);
                if(is_like){
                    like_view.setImageResource(R.drawable.ic_baseline_favorite_24);
                }
                else{
                    like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
    public void repeat(View view){
        Intent intent = new Intent();
        ImageView iv_repeat = findViewById(R.id.iv_repeat);
        if(getIntent().getBooleanExtra("repeatOne",false)){
            // 处于单曲循环模式
            intent.putExtra("repeatOne",false);
            iv_repeat.setImageResource(R.drawable.icon_repeat);
        }
        else{
            // 处于顺序播放模式
            intent.putExtra("repeatOne",true);
            iv_repeat.setImageResource(R.drawable.icon_repeat_one);
        }
        setResult(RESULT_OK,intent);
    }
    public void voice_up(View view){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(value == max){
            Toast.makeText(this,"媒体音量已到最高",Toast.LENGTH_SHORT).show();
        }
        else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,value + 1,0); //音乐音量
            Toast.makeText(this,Integer.toString(value + 1),Toast.LENGTH_SHORT).show();
        }
    }
    public void voice_down(View view){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(value == 0){
            Toast.makeText(this,"媒体音量已到最低",Toast.LENGTH_SHORT).show();
        }
        else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,value - 1,0); //音乐音量
            Toast.makeText(this,Integer.toString(value - 1),Toast.LENGTH_SHORT).show();
        }
    }
    public void message(View view){
        Intent intent = new Intent(ConsoleActivity.this, CommentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("id",id);
        startActivity(intent);
    }
    Bitmap bitmap;
    public void share(View view) throws InterruptedException {
        if ("XTC".equals(Build.MANUFACTURER)) {
            //第一步：创建XTCAppExtendObject对象
            XTCAppExtendObject xtcAppExtendObject = new XTCAppExtendObject();
            //设置点击分享的内容启动的页面
            xtcAppExtendObject.setStartActivity(MainActivity.class.getName());
            //设置分享的扩展信息，点击分享的内容会将该扩展信息带入跳转的页面
            xtcAppExtendObject.setExtInfo(id);
            //第二步: 音乐封面转BitMap
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        Bitmap myBitmap = Glide.with(ConsoleActivity.this)
                                .asBitmap()
                                .load(coverurl)
                                .submit(512, 512).get();
                        bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };
            t.start();
            t.join();
            //第三步：创建XTCShareMessage对象，并将shareObject属性设置为xtcTextObject对象
            XTCShareMessage xtcShareMessage = new XTCShareMessage();
            xtcShareMessage.setShareObject(xtcAppExtendObject);
            //设置图片
            xtcShareMessage.setThumbImage(bitmap);
            //设置文本
            xtcShareMessage.setDescription(name + "\n" + artists);
            //第四步：创建SendMessageToXTC.Request对象，并设置message属性为xtcShareMessage
            SendMessageToXTC.Request request = new SendMessageToXTC.Request();
            request.setMessage(xtcShareMessage);
            //第五步：创建ShareMessageManagr对象，调用sendRequestToXTC方法，传入SendMessageToXTC.Request对象和AppKey
            ShareMessageManager SMM = new ShareMessageManager(this);
            SMM.sendRequestToXTC(request, "");
        }
        else{
            Intent intent = new Intent(ConsoleActivity.this, QRCodeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type",Data.defaultMode);
            intent.putExtra("id",id);
            startActivity(intent);
        }
    }
    public void playList(View view){
        Intent intent = new Intent(ConsoleActivity.this, PlayListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("mvids",getIntent().getStringExtra("mvids"));
        intent.putExtra("list", getIntent().getStringExtra("list"));
        intent.putExtra("type",type);
        intent.putExtra("musicIndex",getIntent().getStringExtra("musicIndex"));
        startActivity(intent);
    }
    public void mv(View view){
        if(mvid!=null) {
            if("0".equals(mvid)){
                Toast.makeText(ConsoleActivity.this, "该视频没有对应MV", Toast.LENGTH_SHORT).show();
            }
            else{
                Map maps = null;
                try {
                    maps = (Map) new MVApi().getMVUrl(mvid,UserInfoUtil.getUserInfo(this,"cookie"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Map data = (Map) JSON.parse(maps.get("data").toString());
                try
                {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer", "cn.luern0313.wristvideoplayer.ui.PlayerActivity"));
                    intent.putExtra("mode", 1);
                    intent.putExtra("url", data.get("url").toString());
                    intent.putExtra("url_backup", data.get("url").toString());
                    intent.putExtra("title", name);
                    intent.putExtra("identity_name", getString(R.string.appName));
                    startActivityForResult(intent, 0);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    try
                    {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer_free", "cn.luern0313.wristvideoplayer_free.ui.PlayerActivity"));
                        intent.putExtra("mode", 1);
                        intent.putExtra("url", data.get("url").toString());
                        intent.putExtra("url_backup", data.get("url").toString());
                        intent.putExtra("title", name);
                        intent.putExtra("identity_name", getString(R.string.appName));
                        startActivityForResult(intent, 0);
                    }
                    catch(Exception ee)
                    {
                        Toast.makeText(ConsoleActivity.this, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG).show();
                        ee.printStackTrace();
                    }
                }
            }
        }
    }
    public void download(View view) throws Exception {
        if(!"1".equals(type)){
            File temp = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/" + song + ".mp3");
            if(!temp.exists()){
                if (url.contains("http://")){
                    url = url.replace("http://","https://");
                }
                new DownloadUtil().download(url,"/Android/data/cn.wearbbs.music/download/music/",song + ".mp3", (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE));
                String text;
                Map lrc_map;
                Map maps = new MusicApi().getMusicLrc(cookie,id);
                if("200".equals(maps.get("code").toString())) {
                    if(maps.get("lrc") != null){
                        lrc_map = (Map) JSON.parse(maps.get("lrc").toString());
                        File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/lrc");
                        dir.mkdirs();
                        File tl = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/lrc/" + song + ".lrc");
                        tl.createNewFile();
                        FileOutputStream outputStream;
                        outputStream = new FileOutputStream(tl);
                        outputStream.write(lrc_map.get("lyric").toString().getBytes());
                        outputStream.close();
                    }else{
                        File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/lrc");
                        dir.mkdirs();
                        File tl = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/lrc/" + song + ".lrc");
                        tl.createNewFile();
                        FileOutputStream outputStream;
                        outputStream = new FileOutputStream(tl);
                        outputStream.write("[00:00.00]无歌词".getBytes());
                        outputStream.close();
                    }
                }
                else{
                    Toast.makeText(ConsoleActivity.this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
                }
                new DownloadUtil().download(coverurl,"/Android/data/cn.wearbbs.music/download/cover/",song + ".jpg", (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE));
                Toast.makeText(this,"已加入下载列表",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"已下载",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this,"已下载",Toast.LENGTH_SHORT).show();
        }
    }
    public void like(View view) throws Exception {
        ImageView like_view = findViewById(R.id.iv_like);
        if(!is_like){
            Map maps = new MusicApi().likeMusic(id,cookie);
            if("200".equals(maps.get("code").toString())){
                like_view.setImageResource(R.drawable.ic_baseline_favorite_24);
                Toast.makeText(this,"收藏成功！",Toast.LENGTH_SHORT).show();
                is_like = true;
            }
            else{
                Toast.makeText(this,"收藏失败！",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Map maps = new MusicApi().dislikeMusic(id,cookie);
            if("200".equals(maps.get("code").toString())){
                like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                Toast.makeText(this,"取消收藏成功！",Toast.LENGTH_SHORT).show();
                is_like = false;
            }
            else{
                Toast.makeText(this,"取消收藏失败！",Toast.LENGTH_SHORT).show();
            }
        }
    }
}

