package id.go.jatengprov.bpsdmd.simapan;

import static id.go.jatengprov.bpsdmd.simapan.tools.Constant.SP_EMAIL;
import static id.go.jatengprov.bpsdmd.simapan.tools.Constant.SP_FOTO;
import static id.go.jatengprov.bpsdmd.simapan.tools.Constant.SP_NAMA;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import id.go.jatengprov.bpsdmd.simapan.tools.Constant;
import id.go.jatengprov.bpsdmd.simapan.tools.Network;

public class LoginActivity extends AppCompatActivity {
    private boolean sudahLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // cek shared preferences
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constant.MY_PREFS, MODE_PRIVATE);
        sudahLogin = sharedPreferences.getBoolean(Constant.SP_LOGIN, false);
        String spNama = sharedPreferences.getString(SP_NAMA, "");
        String spEmail = sharedPreferences.getString(SP_EMAIL, "");
        String spFoto = sharedPreferences.getString(SP_FOTO, "");

        Log.d("CEK_SP_LOGIN", spNama + ", " + spEmail + ", " + spFoto);

        // jika sudah login (sudah tersimpan data di shared preferences) alihkan ke halaman utama
        if(sudahLogin){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            // menitipkan data ke dalam intent
            intent.putExtra(SP_NAMA, spNama);
            intent.putExtra(SP_EMAIL, spEmail);
            intent.putExtra(SP_FOTO, spFoto);
            // biar tidak bisa di-back (kembali ke halaman login) setelah login
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        // binding
        EditText etEmail = findViewById(R.id.editTextEmail);
        EditText etPassword = findViewById(R.id.editTextPassword);
        Button btnLogin = findViewById(R.id.buttonLogin);
        TextView linkRegister = findViewById(R.id.textRegister);

        // button listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ambil value edit text email dan password
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                // show in log
                Log.d("CEK LOGIN", "Email: "+email);
                Log.d("CEK LOGIN", "Password: "+password);

                // tampilkan notif toast jika email atau password kosong
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Email dan password harus diisi!",
                            Toast.LENGTH_SHORT).show();
                }
                // menuju ke activity main (home)
                else{
                    // memanggil asynTask
                    new SendLoginData().execute(email, password);
                }

            }
        });

        // link register listener
        linkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // menuju ke activity register
                Intent intent = new Intent(
                        getApplicationContext(),
                        RegisterActivity.class);
                startActivity(intent);

            }
        });
    }

    // async task
    private class SendLoginData extends AsyncTask<String, Void, String>{
        private ProgressDialog progressDialog;

        // show loading animation
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Mohon ditunggu...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String strEmail = strings[0];
            String strPassword = strings[1];

            // cek ke API
            String response = Network.getJSON(
                    "http://192.3.168.178/flutter/login.php?" +
                            "email=" + strEmail +
                            "&password=" + strPassword
            );
            Log.d("CEK_API_LOGIN", "response: "
                    + response);

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            // cek indikator
            if(progressDialog.isShowing()) progressDialog.dismiss();
            // consume json
            String status = "";
            String message = "";
            String nama = "";
            String email = "";
            String foto = "";

            // tampilkan message login ke toast
            try {
                JSONObject object = new JSONObject(s);
                status = object.getString("status");
                message = object.getString("message");

                JSONObject objData = object.getJSONObject("data");
                nama = objData.getString("nama");
                email = objData.getString("email");
                foto = objData.getString("foto");

                Log.d("CEK_API_LOGIN", "status: " + status);
                Log.d("CEK_API_LOGIN", "message: " + message);
                Log.d("CEK_API_LOGIN", "nama: " + nama);
                Log.d("CEK_API_LOGIN", "email: " + email);
                Log.d("CEK_API_LOGIN", "foto: " + foto);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT).show();

            // ketika status=success redirect ke activity home
            if(status.equals("success")){
                // Jika belum tersimpan data login simpan ke shared preferences
                if(!sudahLogin){
                    SharedPreferences.Editor editor =
                            getSharedPreferences(Constant.MY_PREFS, MODE_PRIVATE).edit();
                    editor.putBoolean(Constant.SP_LOGIN, true).apply();
                    editor.putString(SP_NAMA, nama).apply();
                    editor.putString(SP_EMAIL, email).apply();
                    editor.putString(SP_FOTO, foto).apply();
                }
                // ke main activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                // menitipkan data ke dalam intent
                intent.putExtra(SP_NAMA, nama);
                intent.putExtra(SP_EMAIL, email);
                intent.putExtra(SP_FOTO, foto);
                startActivity(intent);
            }

        }
    }
}