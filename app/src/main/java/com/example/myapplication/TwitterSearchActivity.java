package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class TwitterSearchActivity extends Activity {


    private ListView lista;
    private EditText texto;
    private String accessToken;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        lista = (ListView) findViewById(R.id.lista);
        texto = (EditText) findViewById(R.id.texto);

        new AutenticacaoTask().execute();
    }

    public void buscar(View v) {

        String filtro = texto.getText().toString();
        if(accessToken == null){
            Toast.makeText(this, "token não disponivel", Toast.LENGTH_SHORT).show();
        }else{
            new TwitterTask().execute(filtro);
        }

    }

    private class AutenticacaoTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            try {
                Map<String, String> data =
                        new HashMap<String, String>();
                data.put("grand_type", "client_credentials");
                String Json = com.github.kevinsawicki.http.HttpRequest
                        .post("https://api.twitter.com/oauth2/token")
                        .authorization("Basic" + gerarChave())
                        .form(data)
                        .body();

                JSONObject token = new JSONObject(Json);
                accessToken = token.getString("acess_token");

            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }


    private String gerarChave() throws UnsupportedEncodingException {
        String key = "0co7hEzqSq7XwOauXMCsHGBge";
        String secret = "eOaDvf2WALYoaZ898VSCCf6VYOlcH2eNEKf6YB6hxWa0b6tqGb";
        String token = key + ":" + secret;
        String base64 = android.util.Base64.encodeToString(token.getBytes(), android.util.Base64.NO_WRAP);

        return base64;
    }

    private class TwitterTask extends AsyncTask<String, Void, String[]> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(TwitterSearchActivity.this);
            dialog.setMessage("aguarde");
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            try {
                String filtro = params[0];

                if (TextUtils.isEmpty(filtro)) {
                    return null;
                }

                String urlTwitter =
                        "https://api.twitter.com/1.1/search/tweets.json?q=";

                String url = Uri.parse(urlTwitter + filtro).toString();

                String conteudo = HttpRequest.get(url)
                        .authorization("bearer" + accessToken)
                        .body();

                JSONObject jsonObject = new JSONObject(conteudo);

                JSONArray resultados = jsonObject.getJSONArray("statuses");

                String[] tweets = new String[resultados.length()];

                for (int i = 0; i < resultados.length(); i++) {
                    JSONObject tweet = resultados.getJSONObject(i);
                    String texto = tweet.getString("text");
                    String usuario = tweet.getJSONObject("user")
                            .getString("screen_name");

                    tweets[i] = usuario + "-" + texto;
                }
                return tweets;
            } catch (Exception e) {
                Log.e(getPackageName(), e.getMessage(), e);
                throw new RuntimeException(e);
            }

        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(getBaseContext(),
                                android.R.layout.simple_list_item_1, result);
                lista.setAdapter(adapter);
            }
            dialog.dismiss();
        }
    }


}
