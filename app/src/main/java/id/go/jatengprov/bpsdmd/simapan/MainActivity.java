package id.go.jatengprov.bpsdmd.simapan;

import static id.go.jatengprov.bpsdmd.simapan.tools.Constant.MY_PREFS;
import static id.go.jatengprov.bpsdmd.simapan.tools.Constant.SP_EMAIL;
import static id.go.jatengprov.bpsdmd.simapan.tools.Constant.SP_FOTO;
import static id.go.jatengprov.bpsdmd.simapan.tools.Constant.SP_LOGIN;
import static id.go.jatengprov.bpsdmd.simapan.tools.Constant.SP_NAMA;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import id.go.jatengprov.bpsdmd.simapan.tools.Network;

public class MainActivity extends AppCompatActivity {
    String savedNama = "";
    String savedEmail = "";
    String savedFoto = "";
    Bitmap bitmap;
    ImageView fotoProfile;
    private ArrayList<HashMap<String, String>> listTraining;
    private ListView listViewTraining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ambil data extras dari login activity
        Bundle extras =getIntent().getExtras();
        if(extras != null){
            savedNama = extras.getString(SP_NAMA);
            savedEmail = extras.getString(SP_EMAIL);
            savedFoto = extras.getString(SP_FOTO);
            Log.d("CEK_EXTRAS", "savedNama: " + savedNama);
            Log.d("CEK_EXTRAS", "savedEmail: " + savedEmail);
            Log.d("CEK_EXTRAS", "savedFoto: " + savedFoto);
        }

        // navigation drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // set color hamburger icon
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        // navigation view dan click listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.nav_home){
                    Toast.makeText(getApplicationContext(), "Menu Home dipilih", Toast.LENGTH_LONG).show();
                }
                else if(id == R.id.nav_logout){
                    // button logout listener
                    // logout: hapus semua shared preferences
                    SharedPreferences.Editor editor =
                            getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
                    editor.putBoolean(SP_LOGIN, false).apply();
                    editor.putString(SP_NAMA, "").apply();
                    editor.putString(SP_EMAIL, "").apply();
                    editor.putString(SP_FOTO, "").apply();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    // biar tidak bisa di-back setelah logout
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // binding header nav
        View header = navigationView.getHeaderView(0);
        TextView tvNama = header.findViewById(R.id.navName);
        TextView tvEmail = header.findViewById(R.id.navEmail);
        tvNama.setText(savedNama);
        tvEmail.setText(savedEmail);
        fotoProfile = header.findViewById(R.id.navProfileImage);
        new GetUserFoto().execute(savedFoto);

        listViewTraining = findViewById(R.id.listView);
        new GetTrainingList().execute();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    // asyncTask get foto
    private class GetUserFoto extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            String fotoURL = strings[0];
            try {
                URL url = new URL(fotoURL);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            fotoProfile.setImageBitmap(bitmap);
        }
    }

    // asyncTask get training
    private class GetTrainingList extends AsyncTask<String, Void, String>{
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Memuat data...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = Network.getJSON("http://192.3.168.178/flutter/training.php");
            listTraining = new ArrayList<>();
            try {
                JSONObject objTrainning = new JSONObject(response);
                JSONArray jsonArray = objTrainning.getJSONArray("data");
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject itemTraining = jsonArray.getJSONObject(i);
                    String namaTraining = itemTraining.getString("nama_training");
                    String hargaTraining = itemTraining.getString("harga");
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("nama_training", namaTraining);
                    hashMap.put("harga_training", hargaTraining);
                    listTraining.add(hashMap);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if(progressDialog.isShowing()) progressDialog.dismiss();
            // adapter
            SimpleAdapter adapter = new SimpleAdapter(
                    MainActivity.this,
                    listTraining,
                    R.layout.item_training,
                    new String[]{"nama_training", "harga_training"},
                    new int[]{R.id.trainingNama, R.id.trainingHarga});

            listViewTraining.setAdapter(adapter);
        }
    }
}