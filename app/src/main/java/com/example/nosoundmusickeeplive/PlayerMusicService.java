package com.example.nosoundmusickeeplive;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.nosoundmusickeeplive.util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PlayerMusicService extends Service {
    private final static String TAG = PlayerMusicService.class.getSimpleName();
    private MediaPlayer mMediaPlayer;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("==================================onCreate,启动服务");
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.no_kill);
        mMediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPlayMusic();
            }
        }).start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

//                threadPool.execute(new Runnable() {
//                    @Override
//                    public void run() {
                //写入文件
                writeFile(PlayerMusicService.this);
//                    }
//                });

                //核心：每隔10min触发亮屏
//                lightScreen();

                handler.postDelayed(this, 30 * 1000L);
            }
        }, 30 * 1000L);


        return START_STICKY;
    }

    private void startPlayMusic() {
        if (mMediaPlayer == null) {
            System.out.println("=====================启动后台播放音乐");
            mMediaPlayer = MediaPlayer.create(MyApplication.getContext(), R.raw.no_kill);
            mMediaPlayer.start();
        } else {
            mMediaPlayer.start();
        }
    }

    private void stopPlayMusic() {
        if (mMediaPlayer != null) {
            System.out.println("=======================关闭后台播放音乐");
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusic();
        System.out.println("==================================onCreate,停止服务");
        // 重启自己
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(MyApplication.getContext(), PlayerMusicService.class));
        } else {
            startService(new Intent(MyApplication.getContext(), PlayerMusicService.class));
        }
    }

    ///////////////////////
    @SuppressLint("WakelockTimeout")
    private void lightScreen() {
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        if (minute % 10 == 0) {
            System.out.println("每隔多长时间点亮屏幕minute==================" + minute);

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()) {
                @SuppressLint("InvalidWakeLockTag")
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.PARTIAL_WAKE_LOCK, "bright");
                wl.acquire();
                wl.release();
            }
        }
    }

    private void writeFile(Context context) {
        try {
            String path = Environment.getExternalStoragePublicDirectory("") + "/print1music.txt";
            System.out.println("==============================" + path);
            File file = new File(path);
            if (!file.exists() && file.createNewFile()) {
                Toast.makeText(context, "创建文件成功", Toast.LENGTH_LONG).show();
                System.out.println("=======================Create file successed");
            } else {
//                Toast.makeText(context, "创建文件失败", Toast.LENGTH_LONG).show();
            }
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
            //获取当前时间
            Date date = new Date(System.currentTimeMillis());
            String time = simpleDateFormat.format(date);
            FileUtil.method2(path, time + ",sendNum:" + sendNum + ",sum:" + sum);
            FileUtil.method2(path, "\r\n");
            Toast.makeText(context, "写入文件成功", Toast.LENGTH_SHORT).show();

//            wakeAndNotify(context);
//            this.wakeScreen(context);
//            this.sendNotification(context);
        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(context, "报错了", Toast.LENGTH_SHORT).show();
        }
    }

    private static long sum = 1;
    private static int sendNum = 1;

    private void wakeAndNotify(Context context) {
        long fa = factorial(sendNum);
        if (sum == fa) {
            this.wakeScreen(context);
            this.sendNotification2(context);
            sendNum++;
        }
        sum++;
    }

    /**
     * 计算阶乘数，即n! = n * (n-1) * ... * 2 * 1
     */
    private static long factorial(int n) {
        long sum = 0;
        while (n > 0) {
            sum = sum + n--;
        }
        return sum;
    }

    private void wakeScreen(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
//            String msg = intent.getStringExtra("msg");
//            textview.setText("又收到消息:" + msg);
            //点亮屏幕
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire();
            wl.release();
        }

    }

    private void sendNotification2(Context context) {
        String id = "my_channel_01";
        String name = "我是渠道名字";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
//            Toast.makeText(this, mChannel.toString(), Toast.LENGTH_SHORT).show();
//            Log.i(TAG, mChannel.toString());
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(context)
                    .setChannelId(id)
                    .setContentTitle("我是标题")
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentText("我是内容" + sendNum + "," + sum)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setOngoing(true)
                    .setDefaults(Notification.DEFAULT_SOUND).build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle("我是标题")
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentText("我是内容" + sendNum + "," + sum)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setOngoing(true)
                    .setDefaults(Notification.DEFAULT_SOUND);
            //无效
            notification = notificationBuilder.build();
        }
        notificationManager.notify(111123, notification);

    }

    private void sendNotification(Context context) {
        /**
         *  创建通知栏管理工具
         */

        NotificationManager notificationManager = (NotificationManager) context.getSystemService
                (NOTIFICATION_SERVICE);

        /**
         *  实例化通知栏构造器
         */

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        /**
         *  设置Builder
         */
        //设置标题
        mBuilder.setContentTitle("我是标题")
                //设置内容
                .setContentText("我是内容" + sendNum + "," + sum)
                //设置大图标
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher_round)
                //设置通知时间
                .setWhen(System.currentTimeMillis())
                //首次进入时显示效果
                .setTicker("我是测试内容")
                //设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
                .setDefaults(Notification.DEFAULT_SOUND);
        //发送通知请求
        notificationManager.notify(10, mBuilder.build());
    }

}

//Value will be forced up to 60000 as of Android 5.1; don't rely on this to be exact less... (Ctrl+F1)
//Inspection info:Frequent alarms are bad for battery life. As of API 22,
//the AlarmManager will override near-future and high-frequency alarm requests,
//delaying the alarm at least 5 seconds into the future and ensuring that the repeat interval is at least 60 seconds.
//If you really need to do work sooner than 5 seconds, post a delayed message or runnable to a Handler.
//从Android 5.1开始，其值将被强制达到60000；不要依赖于此来减少…（CTRL+F1）
//检查信息：频繁报警对电池寿命不利。
//至于API 22，AlarmManager将覆盖近期和高频报警请求，将警报延迟至少5秒，并确保重复间隔至少为60秒。
//如果您确实需要在5秒内完成工作，请将延迟的message或runnable发布给handler。