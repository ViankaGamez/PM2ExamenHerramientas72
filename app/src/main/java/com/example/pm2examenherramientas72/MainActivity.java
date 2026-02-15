package com.example.pm2examenherramientas72;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pm2examenherramientas72.Configuraciones.SQLiteConexion;
import com.example.pm2examenherramientas72.Configuraciones.Transacciones;

public class MainActivity extends AppCompatActivity {

    EditText nombre, apellido, edad, correo;
    ImageView foto;
    Button btnAgregar;
    Button btnFoto;

    static final int REQUEST_CAMARA_PERMISO = 230;
    static final int REQUEST_IMAGE_CAPTURE = 191;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        nombre = (EditText) findViewById(R.id.nombre);
        apellido = (EditText) findViewById(R.id.apellido);
        edad = (EditText) findViewById(R.id.edad);
        correo = (EditText) findViewById(R.id.correo);
        foto = (ImageView) findViewById(R.id.foto);
        btnAgregar = (Button) findViewById(R.id.boton);
        btnFoto = (Button) findViewById(R.id.botonFoto);

        /*
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Hola", Toast.LENGTH_LONG).show();
                //AddPersona();
            }
        });

         */

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TomarFoto();
            }
        });

    }

    private void TomarFoto() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA},
                    REQUEST_CAMARA_PERMISO);
        } else{
            AbrirCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

        if(requestCode == REQUEST_CAMARA_PERMISO){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                AbrirCamara();
            } else{
                Toast.makeText(getApplicationContext(), "Permiso Denegado ", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void AbrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager()) != null ){
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap img = (Bitmap) extras.get("data");
            foto.setImageBitmap(img);
        }
    }

    /*
    private void AddPersona() {
        try {
            SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.dbname,null, Transacciones.dbversion);
            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put(Transacciones.h_nombre, nombre.getText().toString());
            valores.put(Transacciones.h_descripcion, descripcion.getText().toString());
            valores.put(Transacciones.h_especificaciones, especificaciones.getText().toString());
            valores.put(Transacciones.h_foto_uri, "");
            valores.put(Transacciones.h_estado, "DISPONIBLE");


            Long resultado = db.insert(Transacciones.tbPersonas, Transacciones.id, valores);
            Toast.makeText(getApplicationContext(), "Registro Ingresado " + resultado.toString(), Toast.LENGTH_LONG).show();

            db.close();

            LimpiarPantalla();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Ha ocurrido un error " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
     */

    private void LimpiarPantalla(){
        nombre.setText("");
        apellido.setText("");
        edad.setText("");
        correo.setText("");
        nombre.requestFocus();
    }
}