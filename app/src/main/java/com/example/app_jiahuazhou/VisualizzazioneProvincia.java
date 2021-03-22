package com.example.app_jiahuazhou;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VisualizzazioneProvincia extends AppCompatActivity {

    String url;
    String codiceProvincia;

    TextView textView_nome;
    TextView textView_lastUpdate;
    TextView textView_popolazione;
    TextView textView_area;
    TextView textView_densita;


    String lastUpdate;
    String nome;
    int popolazione;
    int area;
    float densità;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizzazione_provincia);

        codiceProvincia = getIntent().getStringExtra("provincia");

        textView_nome = findViewById(R.id.textView_nome);
        textView_popolazione  = findViewById(R.id.textView_popolazione);
        textView_area = findViewById(R.id.textView_area);
        textView_densita = findViewById(R.id.textView_densita);
        textView_lastUpdate  = findViewById(R.id.textView_lastUpdateP);

        url = "https://api.covid19tracker.ca/provinces";

        try {
            loadData();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void loadData() throws MalformedURLException {
        VisualizzazioneProvincia.DownloadInternet downloadInternet = new VisualizzazioneProvincia.DownloadInternet();
        URL myUrl = new URL(url);
        downloadInternet.execute(myUrl);
    }

    private class DownloadInternet extends AsyncTask<URL, String, String> {

        final ProgressDialog dialog = new ProgressDialog(VisualizzazioneProvincia.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Loading data...");
            dialog.show();
        }

        @Override
        protected String doInBackground(URL... strings) {
            String json = "";

            try {
                HttpURLConnection conn = (HttpURLConnection) strings[0].openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    json += line;
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return json;
        }

        @Override
        protected void onPostExecute(String strings) {

            dialog.dismiss();
            try {
                parsingJson(strings);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            textView_lastUpdate.setText(lastUpdate);
            textView_nome.setText(nome);
            textView_area.setText("Area: "+Integer.toString(area));
            textView_densita.setText("Densità: "+Double.toString(densità));
            textView_popolazione.setText("Popolazione: "+Integer.toString(popolazione));
        }
    }

    public void parsingJson(String json) throws JSONException {
        JSONArray listaProvincieJson = new JSONArray(json);
        JSONObject provinciaAttuale = new JSONObject();
        for(int i=0; i<listaProvincieJson.length();i++)
        {
            JSONObject provincia = (JSONObject) listaProvincieJson.get(i);
            String code = provincia.getString("code");
            if(code.equals(codiceProvincia))
            {
                provinciaAttuale = provincia;
            }
        }
        String updated_at = provinciaAttuale.getString("updated_at");
        updated_at = updated_at.replace("T", " ").replace(".000000Z", "");//ci sono caratteri "inquinanti"
        lastUpdate = "Dati aggiornati:   "+updated_at;
        nome = provinciaAttuale.getString("name");
        popolazione = provinciaAttuale.getInt("population");
        area = provinciaAttuale.getInt("area");
        densità = popolazione/area;
    }
}