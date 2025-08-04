package com.example.restapi;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.app.DatePickerDialog;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.restapi.config.Personas;
import com.example.restapi.config.RestApiMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ActivityUpdate extends AppCompatActivity {
    static final int REQUEST_IMAGE = 101;
    static final int ACCESS_CAMERA = 201;
    ImageView imageView;
    Button btnfoto, btnActualizar;
    String currentPhotoPath;
    EditText nombres, apellidos, fechanac, telefono, foto, direccion;
    private RequestQueue requestQueue;
    int personaId = -1;
    private String originalFoto = "";
    boolean fotoChanged = false;

    Calendar calendario = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);
        imageView = findViewById(R.id.imageView);
        btnfoto = findViewById(R.id.btntakefoto);
        btnActualizar = findViewById(R.id.btnactualizar);
        nombres = findViewById(R.id.nombres);
        apellidos = findViewById(R.id.apellidos);
        direccion = findViewById(R.id.direccion);
        fechanac = findViewById(R.id.fecha);
        telefono = findViewById(R.id.telefono);
        requestQueue = Volley.newRequestQueue(this);
        fechanac.setOnClickListener(view -> {
            int año = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ActivityUpdate.this,
                    (view1, year, month, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        fechanac.setText(fechaSeleccionada);
                    },
                    año, mes, dia
            );
            datePickerDialog.show();
        });


        btnfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fotoChanged = true;
                PermisosCamara();
            }
        });
        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarCampos()) {
                    actualizarPersona();
                }
            }
        });

        cargarInformacion();
    }
    public void cargarInformacion(){
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("id")) {

            String personaIdString = intent.getStringExtra("id");
            try {
                if (personaIdString != null && !personaIdString.isEmpty()) {
                    personaId = Integer.parseInt(personaIdString);
                } else {
                    personaId = -1;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing ID: " + personaIdString, e);
                personaId = -1;
            }

            if (personaId <= 0) {
                Toast.makeText(this, "Error: ID de persona no válido", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            nombres.setText(intent.getStringExtra("nombres"));
            apellidos.setText(intent.getStringExtra("apellidos"));
            direccion.setText(intent.getStringExtra("direccion"));
            telefono.setText(intent.getStringExtra("telefono"));
            fechanac.setText(intent.getStringExtra("fechanac"));
            originalFoto = intent.getStringExtra("foto");
            if (originalFoto == null) originalFoto = "";
            loadImageFromBase64(originalFoto);
            fotoChanged = false;

            Log.d(TAG, "Datos cargados para persona ID: " + personaId);
        } else {
            Toast.makeText(this, "Error: No se recibieron datos de la persona", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    private void loadImageFromBase64(String base64String) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedBitmap != null) {
                    imageView.setImageBitmap(decodedBitmap);
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_camera);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error decodificando imagen", e);
                imageView.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private boolean validarCampos() {
        if (personaId == -1) {
            Toast.makeText(this, "Error: ID de persona no válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (nombres.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el nombre", Toast.LENGTH_SHORT).show();
            nombres.requestFocus();
            return false;
        }
        if (apellidos.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el apellido", Toast.LENGTH_SHORT).show();
            apellidos.requestFocus();
            return false;
        }

        if (direccion.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa la dirección", Toast.LENGTH_SHORT).show();
            direccion.requestFocus();
            return false;
        }
        if (telefono.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el teléfono", Toast.LENGTH_SHORT).show();
            telefono.requestFocus();
            return false;
        }
        return true;
    }
    private void actualizarPersona() {
        Log.d(TAG, "Iniciando actualización para ID: " + personaId);
        btnActualizar.setEnabled(false);
        btnActualizar.setText("Actualizando...");
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", personaId);
            jsonObject.put("nombres", nombres.getText().toString().trim());
            jsonObject.put("apellidos", apellidos.getText().toString().trim());
            jsonObject.put("direccion", direccion.getText().toString().trim());
            jsonObject.put("telefono", telefono.getText().toString().trim());
            jsonObject.put("fechanac", fechanac.getText().toString().trim());
            String fotoToSend;
            if (fotoChanged && currentPhotoPath != null) {
                fotoToSend = ConvertImageBase64(currentPhotoPath);
                Log.d(TAG, "Usando nueva imagen");
            } else {
                fotoToSend = originalFoto;
                Log.d(TAG, "Manteniendo imagen original");
            }
            jsonObject.put("foto", fotoToSend);
            Log.d(TAG, "Enviando datos a: " + RestApiMethods.EndpointUpdatePerson);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    RestApiMethods.EndpointUpdatePerson,
                    jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Respuesta exitosa: " + response.toString());
                            handleUpdateSuccess(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error en actualización", error);
                            handleUpdateError(error);
                        }
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                    30000,
                    0,
                    com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creando JSON", e);
            Toast.makeText(this, "Error preparando datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            ActualizarButton();
        }
    }
    private void handleUpdateSuccess(JSONObject response) {
        try {
            String mensaje = "Persona actualizada correctamente";
            if (response.has("message")) {
                mensaje = response.getString("message");
            }

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            Limpiar();
            setResult(RESULT_OK);
            finish();
        } catch (JSONException e) {
            Log.e(TAG, "Error procesando respuesta", e);
            Toast.makeText(this, "Actualizado correctamente", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        }
    }
    private void handleUpdateError(VolleyError error) {
        String mensaje = "Error actualizando persona";
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            String responseBody = new String(error.networkResponse.data);
            Log.e(TAG, "Error HTTP " + statusCode + ": " + responseBody);
            mensaje = "Error HTTP " + statusCode;
        } else if (error.getMessage() != null) {
            mensaje = error.getMessage();
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

        ActualizarButton();
    }
    private void Limpiar() {
        nombres.setText("");
        apellidos.setText("");
        direccion.setText("");
        telefono.setText("");
        fechanac.setText("");
        imageView.setImageResource(android.R.drawable.ic_menu_camera);
        currentPhotoPath = null;
        originalFoto = "";
        fotoChanged = false;
    }
    private void ActualizarButton() {
        btnActualizar.setEnabled(true);
    }
    private String ConvertImageBase64(String path) {
        try {
            if (path == null || path.isEmpty()) {
                return "";
            }
            File imageFile = new File(path);
            if (!imageFile.exists()) {
                Log.e(TAG, "Archivo de imagen no existe: " + path);
                return "";
            }

            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap == null) {
                Log.e(TAG, "No se pudo decodificar la imagen: " + path);
                return "";
            }
            bitmap = resizeBitmap(bitmap, 800, 600);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo imagen a Base64", e);
            return "";
        }
    }
    private Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);
        if (scale >= 1.0f) {
            return originalBitmap;
        }
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }
    private void PermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, ACCESS_CAMERA);
        } else {
            abrirCamara();
        }
    }
    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creando archivo de imagen", ex);
                Toast.makeText(this, "Error creando archivo de imagen", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.restapi.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            if (currentPhotoPath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    fotoChanged = true;
                    Log.d(TAG, "Imagen capturada y mostrada");
                } else {
                    Toast.makeText(this, "Error cargando la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}



