package id.go.jatengprov.bpsdmd.simapan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import id.go.jatengprov.bpsdmd.simapan.tools.Network;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //enable back navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // binding
        EditText etNama = findViewById(R.id.editTextRegNama);
        EditText etEmail = findViewById(R.id.editTextRegEmail);
        EditText etPassword = findViewById(R.id.editTextRegPassword);
        EditText etPassword2 = findViewById(R.id.editTextRegPassword2);
        Button btnRegister = findViewById(R.id.buttonRegister);

        // listener saat tombol REGISTER diklik
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ambil value
                String nama = etNama.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String password2 = etPassword2.getText().toString();

                // show in log
                Log.d("CEK REG", "Nama: "+nama);
                Log.d("CEK REG", "Email: "+email);
                Log.d("CEK REG", "Password: "+password);
                Log.d("CEK REG", "Konfirmasi Password: "+password2);

                // tampilkan notif toast jika ada data yang kosong
                if(nama.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Semua data harus diisi!",
                            Toast.LENGTH_SHORT).show();
                }
                // jika sudah diisi semua
                // password yang diketik 2 (dua) kali harus sama
                else if(!password.equals(password2)){
                    Toast.makeText(getApplicationContext(),
                            "Password yang diketikkan 2 (kali) harus sama!",
                            Toast.LENGTH_SHORT).show();
                }
                // jika sudah beres
                else{
                    // kirim ke server via API
                    new SendRegisterData().execute(nama, email, password);
                }
            }
        });
    }

    // enable back

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // async task
    private class SendRegisterData extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog;

        // show loading animation
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMessage("Mohon ditunggu...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String strNama = strings[0];
            String strEmail = strings[1];
            String strPassword = strings[2];

            // simpan ke API
            String response = Network.sendRegisterData(
                    "http://192.3.168.178/flutter/insert_user.php?",
                    strNama,
                    strEmail,
                    strPassword);
            Log.d("CEK_API_REGISTER", "response: "
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

            // tampilkan message login ke toast
            try {
                JSONObject object = new JSONObject(s);
                status = object.getString("status");
                message = object.getString("message");

                Log.d("CEK_API_LOGIN", "status: " + status);
                Log.d("CEK_API_LOGIN", "message: " + message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT).show();

            // ketika status=success redirect ke activity home
            if(status.equals("success")){
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }

        }
    }
}