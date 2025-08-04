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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.restapi.config.Personas;
import com.example.restapi.config.RestApiMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityGet extends AppCompatActivity {

    private static final String TAG = "ActivityGet";
    private ListView listViewPersonas;
    private Button btnGetPersonas, btnRefresh, btnEliminar;
    private PersonasAdapter adapter;
    private List<Personas> personasList;
    private RequestQueue requestQueue;

    // Variables para manejo de selección
    private Personas personaSeleccionada = null;
    private int posicionSeleccionada = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get);

        listViewPersonas = findViewById(R.id.listview_personas);
        btnGetPersonas = findViewById(R.id.btngetPersonas);
        btnRefresh = findViewById(R.id.btnVolver);
        btnEliminar = findViewById(R.id.btnEliminar);
        personasList = new ArrayList<>();

        adapter = new PersonasAdapter(this, personasList);
        listViewPersonas.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this);
        listViewPersonas.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setupClickListeners();
        cargarPersonas();
        actualizarEstadoBtnEliminar();
    }
    private void setupClickListeners() {
        btnGetPersonas.setOnClickListener(v -> cargarPersonas());
        btnRefresh.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityGet.this, ActivityCreate.class);
            startActivity(intent);
        });
        btnEliminar.setOnClickListener(v -> {
            if (personaSeleccionada != null) {
                mostrarDialogoConfirmacionEliminacion();
            } else {
                Toast.makeText(this, "Selecciona una persona para eliminar", Toast.LENGTH_SHORT).show();
            }
        });
        listViewPersonas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private long ultimoClick = 0;
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // milliseconds
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long clickTime = System.currentTimeMillis();

                if (clickTime - ultimoClick < DOUBLE_CLICK_TIME_DELTA) {
                    Personas persona = personasList.get(position);
                    abrirActivityUpdate(persona);
                } else {
                    // Click simple - seleccionar
                    seleccionarPersona(position);
                }

                ultimoClick = clickTime;
            }
        });

        listViewPersonas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Personas persona = personasList.get(position);
                Toast.makeText(ActivityGet.this, "Abriendo para editar: " + persona.getNombres(), Toast.LENGTH_SHORT).show();
                abrirActivityUpdate(persona);
                return true;
            }
        });
    }

    private void seleccionarPersona(int position) {
        if (posicionSeleccionada != -1) {
            listViewPersonas.setItemChecked(posicionSeleccionada, false);
        }
        posicionSeleccionada = position;
        personaSeleccionada = personasList.get(position);
        listViewPersonas.setItemChecked(position, true);
        Toast.makeText(this, "Seleccionado: " + personaSeleccionada.getNombres() +
                " (Doble click para editar)", Toast.LENGTH_SHORT).show();

        actualizarEstadoBtnEliminar();
    }

    private void actualizarEstadoBtnEliminar() {
        if (btnEliminar != null) {
            btnEliminar.setEnabled(personaSeleccionada != null);
            btnEliminar.setText(personaSeleccionada != null ?
                    "Eliminar " + personaSeleccionada.getNombres() : "Eliminar");
        }
    }

    private void mostrarDialogoConfirmacionEliminacion() {
        if (personaSeleccionada == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Estás seguro de que deseas eliminar a " +
                personaSeleccionada.getNombres() + " " +
                personaSeleccionada.getApellidos() + "?");

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            EliminarUsuario();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }


    private void cargarPersonas() {
        btnGetPersonas.setEnabled(false);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                RestApiMethods.EndpointGetPersons,
                null,
                response -> {
                    List<Personas> listaDesdeAPI = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject personaJson = response.getJSONObject(i);
                            Log.d(TAG, "Procesando persona " + i + ": " + personaJson.toString());
                            Personas persona = new Personas();
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
                    personaSeleccionada = null;
                    posicionSeleccionada = -1;
                    actualizarEstadoBtnEliminar();
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
                    btnGetPersonas.setEnabled(true);
                    Toast.makeText(ActivityGet.this, "Error cargando personas", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    private void abrirActivityUpdate(Personas persona) {
        Intent intent = new Intent(this, ActivityUpdate.class);
        intent.putExtra("id", persona.getId());
        intent.putExtra("nombres", persona.getNombres());
        intent.putExtra("apellidos", persona.getApellidos());
        intent.putExtra("direccion", persona.getDireccion());
        intent.putExtra("telefono", persona.getTelefono());
        intent.putExtra("fechanac", persona.getFechanac());
        intent.putExtra("foto", persona.getFoto());
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
    public void EliminarUsuario() {
        if (personaSeleccionada == null || personaSeleccionada.getId() == null || personaSeleccionada.getId().isEmpty()) {
            Toast.makeText(this, "Selecciona una persona válida para eliminar.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No se puede eliminar: personaSeleccionada es nula o no tiene ID.");
            return;
        }
        Log.d(TAG, "eliminar usuario con ID: " + personaSeleccionada.getId());
        String deleteUrl = RestApiMethods.EndpointDeletePerson + "?id=" + personaSeleccionada.getId();
        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                deleteUrl,
                response -> {
                    Toast.makeText(this, "Usuario eliminado correctamente.", Toast.LENGTH_SHORT).show();
                    personasList.remove(posicionSeleccionada);
                    adapter.notifyDataSetChanged();
                    personaSeleccionada = null;
                    posicionSeleccionada = -1;
                    actualizarEstadoBtnEliminar();
                },
                error -> {
                    String errorMessage = "Error desconocido";
                    if (error.networkResponse != null) {
                        errorMessage = new String(error.networkResponse.data);
                        Log.e(TAG, "Error del servidor: Código HTTP " + error.networkResponse.statusCode + ", Respuesta: " + errorMessage);
                    } else {
                        Log.e(TAG, "Error de Volley: " + error.toString());
                    }
                    Toast.makeText(this, "Error eliminando usuario: " + errorMessage, Toast.LENGTH_LONG).show();
                }
        );
        requestQueue.add(request);
    }
}