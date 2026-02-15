package com.example.pm2examenherramientas72;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.pm2examenherramientas72.Configuraciones.SQLiteConexion;
import com.example.pm2examenherramientas72.Configuraciones.Transacciones;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class HerramientaFormActivity extends AppCompatActivity {

    // Controles UI
    private EditText edtNombre, edtDescripcion, edtEspecificaciones;
    private ImageView imgFoto;
    private Button btnFoto, btnCamara, btnGuardar;

    // Guardaremos el URI de la imagen seleccionada (como String en SQLite)
    private String fotoUriString = null;

    // URI temporal para la foto tomada con cámara
    private Uri fotoUriTemporal = null;

    private static final int REQUEST_CAMARA_PERMISO = 230;

    // Launcher: abrir galería y obtener el URI
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Guardamos el URI como texto para persistirlo en SQLite
                    fotoUriString = uri.toString();
                    // Mostramos la imagen seleccionada en el ImageView
                    imgFoto.setImageURI(uri);
                }
            });

    // Launcher: tomar foto con cámara y guardarla en un URI (FileProvider)
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && fotoUriTemporal != null) {
                    // Guardamos el URI como texto para persistirlo en SQLite
                    fotoUriString = fotoUriTemporal.toString();
                    // Mostramos la imagen tomada
                    imgFoto.setImageURI(fotoUriTemporal);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_herramienta_form);

        // Referencias a los controles
        edtNombre = findViewById(R.id.edtNombre);
        edtDescripcion = findViewById(R.id.edtDescripcion);
        edtEspecificaciones = findViewById(R.id.edtEspecificaciones);
        imgFoto = findViewById(R.id.imgFoto);

        btnFoto = findViewById(R.id.btnFoto);
        btnCamara = findViewById(R.id.btnCamara);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Botón: elegir foto desde galería
        btnFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Botón: tomar foto con cámara
        btnCamara.setOnClickListener(v -> {
            // Pedimos permiso si hace falta
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMARA_PERMISO);
            } else {
                tomarFotoConCamara();
            }
        });

        // Botón: guardar herramienta en SQLite
        btnGuardar.setOnClickListener(v -> guardarHerramienta());

        Button btnVerLista = findViewById(R.id.btnVerLista);
        btnVerLista.setOnClickListener(v -> {
            startActivity(new Intent(this, HerramientaListActivity.class));
        });

    }

    private void guardarHerramienta() {
        // 1) Tomar textos y limpiar espacios
        String nombre = edtNombre.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();
        String especificaciones = edtEspecificaciones.getText().toString().trim();

        // 2) Validaciones obligatorias
        if (nombre.isEmpty() || descripcion.isEmpty() || especificaciones.isEmpty()) {
            mostrarAlerta("Faltan datos",
                    "Nombre, descripción y especificaciones son obligatorios.");
            return;
        }

        // 3) Validación de nombre: letras, números y espacios (mínimo 3)
        Pattern patronNombre = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 ]{3,}$");
        if (!patronNombre.matcher(nombre).matches()) {
            mostrarAlerta("Nombre inválido",
                    "El nombre debe tener mínimo 3 caracteres y solo letras, números y espacios.");
            return;
        }

        // 4) Insertar en SQLite (tabla Herramientas)
        SQLiteConexion conexion = new SQLiteConexion(
                this,
                Transacciones.dbname,
                null,
                Transacciones.dbversion
        );

        SQLiteDatabase db = conexion.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(Transacciones.h_nombre, nombre);
        valores.put(Transacciones.h_descripcion, descripcion);
        valores.put(Transacciones.h_especificaciones, especificaciones);

        // Guardar foto_uri (si existe)
        if (fotoUriString != null) {
            valores.put(Transacciones.h_foto_uri, fotoUriString);
        } else {
            valores.putNull(Transacciones.h_foto_uri);
        }

        // Estado inicial: DISPONIBLE
        valores.put(Transacciones.h_estado, "DISPONIBLE");

        long resultado = db.insert(Transacciones.tbHerramientas, null, valores);
        db.close();

        if (resultado > 0) {
            mostrarAlerta("Éxito", "Herramienta guardada con ID: " + resultado);
            limpiarPantalla();
        } else {
            mostrarAlerta("Error", "No se pudo guardar la herramienta.");
        }
    }

    private void tomarFotoConCamara() {
        try {
            // Crear archivo temporal donde se guardará la foto
            File fotoFile = crearArchivoImagen();

            // Obtener URI seguro usando FileProvider
            fotoUriTemporal = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    fotoFile
            );

            // Lanzar cámara para guardar la foto en ese URI
            takePictureLauncher.launch(fotoUriTemporal);

        } catch (IOException e) {
            mostrarAlerta("Error cámara", "No se pudo crear el archivo de imagen: " + e.getMessage());
        }
    }

    private File crearArchivoImagen() throws IOException {
        // Nombre único basado en fecha/hora
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        // Directorio interno de imágenes de la app (no requiere permisos de almacenamiento)
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMARA_PERMISO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFotoConCamara();
            } else {
                mostrarAlerta("Permiso denegado", "No se puede usar la cámara sin permiso.");
            }
        }
    }

    private void limpiarPantalla() {
        edtNombre.setText("");
        edtDescripcion.setText("");
        edtEspecificaciones.setText("");
        fotoUriString = null;
        fotoUriTemporal = null;
        imgFoto.setImageResource(android.R.drawable.ic_menu_camera);
        edtNombre.requestFocus();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
    }
}

