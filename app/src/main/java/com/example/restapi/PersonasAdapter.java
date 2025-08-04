package com.example.restapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.restapi.config.Personas;

import java.util.List;

public class PersonasAdapter extends BaseAdapter {
    private static final String TAG = "PersonasAdapter";
    private Context context;
    private List<Personas> personasList;
    private LayoutInflater inflater;

    public PersonasAdapter(Context context, List<Personas> personasList) {
        this.context = context;
        this.personasList = personasList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount(){
        int count = personasList != null ? personasList.size() : 0;
        Log.d(TAG, "getCount: " + count);
        return count;
    }

    @Override
    public Object getItem(int position) {
        return personasList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "=== getView para posición: " + position + " ===");

        ViewHolder holder;

        if (convertView == null) {
            Log.d(TAG, "Creando nueva vista");
            convertView = inflater.inflate(R.layout.item_persona, parent, false);
            holder = new ViewHolder();
            holder.imageViewFoto = convertView.findViewById(R.id.imageView);
            holder.txtNombres = convertView.findViewById(R.id.nombres);
            holder.txtApellidos = convertView.findViewById(R.id.apellidos);
            holder.txtTelefono = convertView.findViewById(R.id.telefono);
            convertView.setTag(holder);

            // Verificar que los elementos se encontraron
            Log.d(TAG, "imageView encontrado: " + (holder.imageViewFoto != null));
            Log.d(TAG, "nombres encontrado: " + (holder.txtNombres != null));
            Log.d(TAG, "apellidos encontrado: " + (holder.txtApellidos != null));
            Log.d(TAG, "telefono encontrado: " + (holder.txtTelefono != null));
        } else {
            Log.d(TAG, "Reutilizando vista existente");
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            Personas persona = personasList.get(position);
            Log.d(TAG, "Datos de persona: " + persona.toString());

            // Asignar textos con logs
            String nombres = persona.getNombres() != null ? persona.getNombres() : "Sin nombre";
            String apellidos = persona.getApellidos() != null ? persona.getApellidos() : "Sin apellido";
            String telefono = persona.getTelefono() != null ? persona.getTelefono() : "Sin teléfono";

            Log.d(TAG, "Asignando nombres: '" + nombres + "'");
            Log.d(TAG, "Asignando apellidos: '" + apellidos + "'");
            Log.d(TAG, "Asignando telefono: '" + telefono + "'");

            holder.txtNombres.setText(nombres);
            holder.txtApellidos.setText(apellidos);
            holder.txtTelefono.setText(telefono);

            // Hacer textos visibles por si acaso
            holder.txtNombres.setVisibility(View.VISIBLE);
            holder.txtApellidos.setVisibility(View.VISIBLE);
            holder.txtTelefono.setVisibility(View.VISIBLE);

            // Manejar imagen con más debug
            String fotoBase64 = persona.getFoto();
            Log.d(TAG, "Foto Base64 presente: " + (fotoBase64 != null && !fotoBase64.trim().isEmpty()));

            if (fotoBase64 != null && !fotoBase64.trim().isEmpty()) {
                Log.d(TAG, "Longitud Base64: " + fotoBase64.length());
                Log.d(TAG, "Primeros 50 caracteres: " + fotoBase64.substring(0, Math.min(50, fotoBase64.length())));

                try {
                    // Limpiar Base64 (remover saltos de línea y espacios)
                    String cleanBase64 = fotoBase64.replaceAll("\\s+", "");

                    byte[] decodedString = Base64.decode(cleanBase64, Base64.DEFAULT);
                    Log.d(TAG, "Bytes decodificados: " + decodedString.length);

                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (decodedBitmap != null) {
                        Log.d(TAG, "Imagen decodificada exitosamente: " + decodedBitmap.getWidth() + "x" + decodedBitmap.getHeight());
                        holder.imageViewFoto.setImageBitmap(decodedBitmap);
                    } else {
                        Log.w(TAG, "No se pudo crear bitmap de los bytes");
                        holder.imageViewFoto.setImageResource(android.R.drawable.ic_menu_camera);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error decodificando imagen", e);
                    holder.imageViewFoto.setImageResource(android.R.drawable.ic_menu_camera);
                }
            } else {
                Log.d(TAG, "No hay imagen, usando icono por defecto");
                holder.imageViewFoto.setImageResource(android.R.drawable.ic_menu_camera);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error general en getView", e);
            holder.txtNombres.setText("ERROR");
            holder.txtApellidos.setText("ERROR");
            holder.txtTelefono.setText("ERROR");
            holder.imageViewFoto.setImageResource(android.R.drawable.ic_menu_camera);
        }

        Log.d(TAG, "=== Fin getView para posición: " + position + " ===");
        return convertView;
    }

    static class ViewHolder {
        ImageView imageViewFoto;
        TextView txtNombres, txtApellidos, txtTelefono;
    }

    public void updateList(List<Personas> newList) {
        this.personasList = newList;
        notifyDataSetChanged();
    }
}