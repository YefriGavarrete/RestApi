package com.example.restapi;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.restapi.config.Personas;
import com.example.restapi.config.RestApiMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityGet extends AppCompatActivity {

    private static final String TAG = "ActivityGet";
    private ListView listViewPersonas;
    private Button btnGetPersonas, btnRefresh;
    private ProgressBar progressBar;
    private PersonasAdapter adapter;
    private List<Personas> personasList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get);

        listViewPersonas = findViewById(R.id.listview_personas);
        btnGetPersonas = findViewById(R.id.btngetPersonas);
        btnRefresh = findViewById(R.id.btnVolver);
        progressBar = findViewById(R.id.progBar);
        personasList = new ArrayList<>();

        adapter = new PersonasAdapter(this, personasList);
        listViewPersonas.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this);
        setupClickListeners();
        cargarPersonas();
    }

    private void setupClickListeners() {
        btnGetPersonas.setOnClickListener(v -> cargarPersonas());
        btnRefresh.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityGet.this, ActivityCreate.class);
            startActivity(intent);
        });
        listViewPersonas.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Personas persona = personasList.get(position);
                Toast.makeText(ActivityGet.this, "Clic en: " + persona.getNombres(), Toast.LENGTH_SHORT).show();
                abrirActivityUpdate(persona);
            }
        });
    }

    private void cargarPersonas() {
        MostrarBarra(true);
        btnGetPersonas.setEnabled(false);

        // AGREGAR LOG PARA DEBUG
        Log.d(TAG, "=== INICIANDO CARGA DE PERSONAS ===");
        Log.d(TAG, "URL: " + RestApiMethods.EndpointGetPersons);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                RestApiMethods.EndpointGetPersons,
                null,
                response -> {
                    Log.d(TAG, "=== RESPUESTA RECIBIDA ===");
                    Log.d(TAG, "JSON completo: " + response.toString());
                    Log.d(TAG, "Número de elementos: " + response.length());

                    List<Personas> listaDesdeAPI = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject personaJson = response.getJSONObject(i);
                            Log.d(TAG, "Procesando persona " + i + ": " + personaJson.toString());

                            Personas persona = new Personas();

                            // Procesar ID de forma MÁS segura
                            try {
                                if (personaJson.has("id")) {
                                    Object idObj = personaJson.get("id");
                                    if (idObj instanceof Integer) {
                                        persona.setId(String.valueOf(idObj));
                                    } else if (idObj instanceof String) {
                                        persona.setId((String) idObj);
                                    } else {
                                        persona.setId("");
                                    }
                                } else {
                                    persona.setId("");
                                }
                            } catch (Exception idError) {
                                Log.e(TAG, "Error procesando ID", idError);
                                persona.setId("");
                            }

                            persona.setNombres(personaJson.optString("nombres", ""));
                            persona.setApellidos(personaJson.optString("apellidos", ""));
                            persona.setDireccion(personaJson.optString("direccion", ""));
                            persona.setTelefono(personaJson.optString("telefono", ""));
                            persona.setFechanac(personaJson.optString("fechanac", ""));
                            persona.setFoto(personaJson.optString("foto", ""));

                            Log.d(TAG, "Persona creada: " + persona.toString());
                            listaDesdeAPI.add(persona);
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando persona en posición " + i, e);
                            e.printStackTrace();
                        }
                    }

                    personasList.clear();
                    personasList.addAll(listaDesdeAPI);
                    adapter.notifyDataSetChanged();

                    MostrarBarra(false);
                    btnGetPersonas.setEnabled(true);

                    Log.d(TAG, "=== CARGA COMPLETADA ===");
                    Log.d(TAG, "Total personas cargadas: " + personasList.size());
                },
                error -> {
                    Log.e(TAG, "=== ERROR EN PETICIÓN ===");
                    Log.e(TAG, "Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Código HTTP: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Respuesta: " + new String(error.networkResponse.data));
                    }

                    MostrarBarra(false);
                    btnGetPersonas.setEnabled(true);
                    Toast.makeText(ActivityGet.this, "Error cargando personas", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    private void abrirActivityUpdate(Personas persona) {
        Intent intent = new Intent(this, ActivityUpdate.class);
        // ENVIAR COMO STRING
        intent.putExtra("id", persona.getId());
        intent.putExtra("nombres", persona.getNombres());
        intent.putExtra("apellidos", persona.getApellidos());
        intent.putExtra("direccion", persona.getDireccion());
        intent.putExtra("telefono", persona.getTelefono());
        intent.putExtra("fechanac", persona.getFechanac());
        intent.putExtra("foto", persona.getFoto());
        Log.d(TAG, "Abriendo ActivityUpdate para persona ID: " + persona.getId());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Log.d(TAG, "Regresando de ActivityUpdate, recargando lista...");
            cargarPersonas();
        }
    }

    private void MostrarBarra(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        listViewPersonas.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}