
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

;

public class TwitterSearchActivity extends Activity {


    private ListView lista;
    private EditText texto;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        lista = (ListView) findViewById(R.id.lista);
        texto = (EditText) findViewById(R.id.texto);

    }

    public void buscar(View v) {

        String filtro = texto.getText().toString();
        if (TextUtils.isEmpty(filtro)) {
            Toast.makeText(this, "Digite algo para buscar", Toast.LENGTH_SHORT).show();
        } else {
            new RedditTask().execute(filtro);
        }

    }

    private class RedditTask extends AsyncTask<String, Void, String[]> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(TwitterSearchActivity.this);
            dialog.setMessage("aguarde...");
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            try {
                String filtro = Uri.encode(params[0]);

                if (TextUtils.isEmpty(filtro)) {
                    return null;
                }

                // Url api do reddit (não precisa de autenticação)
                String url = "https://www.reddit.com/search.json?q=" + filtro;

                Log.d("RedditURL",url);

                // fazendo a requisição
                String conteudo = HttpRequest.get(url)
                        .userAgent("android:com.example.myapllication:v1.0")
                        .body();

                JSONObject jsonObject = new JSONObject(conteudo);

                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray resultados = data.getJSONArray("children");

                String[] post = new String[resultados.length()];

                for (int i = 0; i < resultados.length(); i++) {
                    JSONObject child = resultados.getJSONObject(i);
                    JSONObject postData = child.getJSONObject("data");

                    String titulo = postData.getString("title");
                    String subreddit = postData.getString("subreddit");
                    String autor = postData.getString("author");

                    post[i] = "r/" + subreddit + " - " + autor + ": " + titulo;
                }
                return post;
            } catch (Exception e) {
                Log.e(getPackageName(), e.getMessage(), e);
                throw new RuntimeException(e);
            }


        }

            @Override
             protected void onPostExecute(String[] result){

                    if ( result != null){
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<String>(getBaseContext(),
                                android.R.layout.simple_list_item_1,result);

                        lista.setAdapter(adapter);

                    }
                    dialog.dismiss();
            }
    }
}


