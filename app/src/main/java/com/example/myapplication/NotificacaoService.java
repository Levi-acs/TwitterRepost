package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;


import androidx.core.app.NotificationCompat;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class NotificacaoService  extends Service {

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        ScheduledThreadPoolExecutor poll =
                new ScheduledThreadPoolExecutor(1);
        long delayInicial = 0;
        long periodo = 30;
        TimeUnit unit = TimeUnit.MINUTES;
        poll.scheduleAtFixedRate(new NotificacaoTask(), delayInicial, periodo, unit);

        return START_STICKY;
    }

    private class NotificacaoTask implements Runnable{
        private String baseUrl = "https://www.reddit.com/search.json";
        private String refreshURL = "?q=";
        private String lastPostId = "";

        @Override
        public void run(){
            if(!estaConectado()){
                return;
            }
            try {
                String url = baseUrl + refreshURL;

                String conteudo = HttpRequest
                        .get(url)
                        .userAgent("android:com.example.myapplication:v1.0")
                        .body();

                JSONObject json = new JSONObject(conteudo);
                JSONObject data = json.getJSONObject("data");
                JSONArray children = data.getJSONArray("children");

                for(int i = 0; i< children.length(); i++){
                    JSONObject child = children.getJSONObject(i);
                    JSONObject postData = child.getJSONObject("data");



                    String postId = postData.getString("id");
                    String titulo = postData.getString("title");
                    String subreddit = postData.getString("subreddit");
                    String autor = postData.getString("author");


                    if(postId.equals(lastPostId)){
                        continue;
                    }

                    lastPostId = postId;


                    criarNotificacao(
                            "r/" + subreddit + " - " + autor,
                            titulo,
                            i
                    );
                }

            }catch (Exception e){
                Log.e(getPackageName(),e.getMessage(),e);
            }
        }

    }

    private boolean estaConectado(){
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE
                );

        NetworkInfo info = manager.getActiveNetworkInfo();

        return info.isConnected();
    }

    private void criarNotificacao(String usuario,String texto, int id){
        int icone = R.drawable.ic_launcher_background;

        String aviso = getString(R.string.aviso);
        long data = System.currentTimeMillis();
        String titulo = usuario + " " + getString(R.string.titulo);


        Context context = getApplicationContext();
        Intent intent = new Intent(context, TweetActivity.class);
        intent.putExtra(TweetActivity.USUARIO,usuario.toString());
        intent.putExtra(TweetActivity.TEXTO,texto.toString());

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context,id,intent,Intent.FLAG_ACTIVITY_NEW_TASK);


        NotificationCompat.Builder notification	=
                new NotificationCompat.Builder(context,"canal_reddit")
                        .setSmallIcon(icone)
                        .setTicker(aviso);

        // flags
        notification.setAutoCancel(true);
        notification.setDefaults(Notification.DEFAULT_VIBRATE);
        notification.setDefaults(Notification.DEFAULT_LIGHTS);
        notification.setDefaults(Notification.DEFAULT_SOUND);


        notification.setContentTitle(titulo);
        notification.setContentText(texto);
        notification.setContentIntent(pendingIntent);


        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.notify(id,notification.build());

    }


}
