package com.example.app_jiahuazhou;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String url;
    ArrayAdapter adapter;
    ListView listView;
    ArrayList<Provincia> province;
    String lastUpdate;
    TextView textView_lastUpdate;
    TextView textView_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = "https://api.covid19tracker.ca/summary/split";
        lastUpdate = "";

        listView = findViewById(R.id.listView);
        textView_title = findViewById(R.id.textView_title);
        textView_lastUpdate = findViewById(R.id.textView_lastUpdate);
        province = new ArrayList<>();

        textView_title.setText("Provincie canadesi");

        try {
            loadData();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }




        //imposto ciò che deve fare il programma una volta che viene cliccato un elemento della listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent visualizzazioneProvincia = new Intent(MainActivity.this, VisualizzazioneProvincia.class);
                visualizzazioneProvincia.putExtra("provincia", province.get(position).getProvincia());
                startActivity(visualizzazioneProvincia);
            }
        });
    }

    public void loadData() throws MalformedURLException {
        DownloadInternet downloadInternet = new DownloadInternet();
        URL myUrl = new URL(url);
        downloadInternet.execute(myUrl);
    }

    //scarico i dati
    private class DownloadInternet extends AsyncTask<URL, String, String> {

        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
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


            adapter = new ArrayAdapter<Provincia>(getApplicationContext(), R.layout.row, R.id.provincia, province) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView provincia = view.findViewById(R.id.provincia);
                    provincia.setText(province.get(position).getProvincia());
                    return view;
                }
            };
            listView.setAdapter(adapter);
        }
    }

    //"carico l'arrayList province con i dati scaricati da internet
    public void parsingJson(String json) throws JSONException {
        JSONObject datiJson = new JSONObject(json);
        JSONArray provinceJson =  datiJson.getJSONArray("data");
        lastUpdate = datiJson.getString("last_updated");
        textView_lastUpdate.setText("Dati aggiornati:   "+lastUpdate);
        for(int i=0; i<provinceJson.length();i++)
        {
            JSONObject provinciaJson = provinceJson.getJSONObject(i);
            province.add(new Provincia(provinciaJson.getString("province")));
        }
    }
}