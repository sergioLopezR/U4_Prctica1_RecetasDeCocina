package mx.edu.ittepic.u4_prctica1_recetasdecocina;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText identificacion, nombre, ingredientes, preparacion, observaciones;
    Button guardar, consultar, eliminar, modificar;
    BaseDatos base;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        identificacion = findViewById(R.id.identificacion);
        nombre = findViewById(R.id.nombre);
        ingredientes = findViewById(R.id.ingredientes);
        preparacion = findViewById(R.id.preparacion);
        observaciones = findViewById(R.id.observaciones);

        guardar = findViewById(R.id.guardar);
        consultar = findViewById(R.id.consultar);
        eliminar = findViewById(R.id.eliminar);
        modificar = findViewById(R.id.modificar);

        //Asignarle memoria (new) y configuracion
        base = new BaseDatos(this, "receta", null, 1);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (identificacion.length() == 0 || nombre.length()==0 || ingredientes.length() == 0 || preparacion.length() == 0 || observaciones.length() == 0){
                    Toast.makeText(MainActivity.this, "Favor de llenar todos los campos", Toast.LENGTH_LONG).show();
                }else{
                    guardarReceta();
                }
            }
        });

        consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirID(1);//Contendra el AlertDialog
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirID(2);
            }
        });

        modificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modificar.getText().toString().startsWith("CONFIRMAR ACTUALIZACION")){
                    invocarConfirmacionActualizacion();
                }else{
                    pedirID(3);
                }
            }
        });
    }

    private void guardarReceta() {
        try{
            SQLiteDatabase tabla = base.getWritableDatabase();

            String SQL = "INSERT INTO RECETA VALUES(%1, '%2', '%3', '%4', '%5')";
            SQL = SQL.replace("%1", identificacion.getText().toString());
            SQL = SQL.replace("%2", nombre.getText().toString());
            SQL = SQL.replace("%3", ingredientes.getText().toString());
            SQL = SQL.replace("%4", preparacion.getText().toString());
            SQL = SQL.replace("%5", observaciones.getText().toString());

            tabla.execSQL(SQL);

            Toast.makeText(this, "Se guardo exitosamente la receta con el id: "+identificacion.getText().toString(), Toast.LENGTH_LONG).show();

            identificacion.setText("");
            nombre.setText("");
            ingredientes.setText("");
            preparacion.setText("");
            observaciones.setText("");

            tabla.close();

        }catch (SQLiteException e){
            Toast.makeText(this, "ERROR: No se pudo guardar la receta", Toast.LENGTH_LONG).show();
        }
    }

    private void pedirID(final int origen) {

        final EditText pidoID = new EditText(this);
        pidoID.setInputType(InputType.TYPE_CLASS_NUMBER);
        pidoID.setHint("VALOR ENTERO MAYOR DE 0");

        String mensaje = "";
        String mensajeBoton = "";

        if (origen==1){
            mensaje = "ESCRIBA EL ID A BUSCAR";
            mensajeBoton = "BUSCAR";
        }
        if (origen==2){
            mensaje = "ESCRIBA EL ID QUE SE DESEA ELIMINAR";
            mensajeBoton = "ELIMINAR";
        }
        if (origen==3){
            mensaje = "ESCRIBA EL ID A MODIFICAR";
            mensajeBoton = "MODIFICAR";
        }

        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("ATENCION").setMessage(mensaje).setView(pidoID).setPositiveButton(mensajeBoton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (pidoID.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "DEBES ESCRIBIR VALOR", Toast.LENGTH_LONG).show();
                    return;
                }
                buscarDato(pidoID.getText().toString(), origen);
                dialog.dismiss();
            }
        }).setNegativeButton("CANCELAR", null).show();

    }

    private void buscarDato(String idABuscar, int origen){
        try{
            SQLiteDatabase tabla = base.getReadableDatabase();

            String SQL = "SELECT * FROM RECETA WHERE ID="+idABuscar;

            Cursor resultado = tabla.rawQuery(SQL, null);

            if (resultado.moveToFirst()){
                //Si hay resultado
                if (origen==2){
                    //Esto significa que se consulto para borrar
                    String datos = idABuscar+"&"+resultado.getString(1)+"&"+resultado.getString(2)+"&"+resultado.getString(3)+"&"+resultado.getString(4);
                    invocarConfirmacionEliminacion(datos);
                    return;
                }

                identificacion.setText(resultado.getString(0));
                nombre.setText(resultado.getString(1));
                ingredientes.setText(resultado.getString(2));
                preparacion.setText(resultado.getString(3));
                observaciones.setText(resultado.getString(4));

                if (origen==3){
                    //MODIFICAR
                    guardar.setEnabled(false);
                    consultar.setEnabled(false);
                    eliminar.setEnabled(false);
                    modificar.setText("CONFIRMAR ACTUALIZACION");
                    identificacion.setEnabled(false);
                }
            }else{
                //No hay!
                Toast.makeText(this, "ERROR: No se pudo buscar la receta", Toast.LENGTH_LONG).show();
            }
            tabla.close();

        }catch (SQLiteException e){
            Toast.makeText(this, "ERROR: NO SE ENCONTRO RESULTADO", Toast.LENGTH_LONG).show();
        }

    }

    private void invocarConfirmacionEliminacion(String datos) {

        String cadenaDatos[] = datos.split("&");
        final String id = cadenaDatos[0];
        String nombre = cadenaDatos[1];
        String ingredientes = cadenaDatos[2];
        String preparacion = cadenaDatos[3];
        String observaciones = cadenaDatos[4];

        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("ATENCION").setMessage("Deseas eliminar \nUsuario: "+id+"\nNombre: "+nombre+" \nIngredientes: "+ingredientes+" \nPreparacion: "+preparacion+" \nObservaciones: "+observaciones+"?").setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eliminarDato(id);
                dialog.dismiss();
            }
        }).setNegativeButton("NO", null).show();
    }

    private void eliminarDato(String id) {
        try{
            SQLiteDatabase tabla = base.getWritableDatabase();

            String SQL = "DELETE FROM RECETA WHERE ID="+id;

            tabla.execSQL(SQL);

            identificacion.setText("");
            nombre.setText("");
            ingredientes.setText("");
            preparacion.setText("");
            observaciones.setText("");

            Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_LONG).show();
            tabla.close();

        }catch (SQLiteException e){
            Toast.makeText(this, "ERROR: NO SE PUDO ELIMINAR", Toast.LENGTH_LONG).show();
        }
    }

    private void invocarConfirmacionActualizacion() {
        AlertDialog.Builder confir = new AlertDialog.Builder(this);

        confir.setTitle("IMPORTANTE").setMessage("¿Estas seguro que deseas aplicar los cambios?").setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aplicarActualizacion();
                dialog.dismiss();
            }
        }).setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                habilitarBotonesYLimpiarCampos();
                dialog.cancel();
            }
        }).show();
    }

    private void aplicarActualizacion(){
        try{
            SQLiteDatabase tabla = base.getWritableDatabase();
            String SQL = "UPDATE RECETA SET NOMBRE='"+nombre.getText().toString()+"', INGREDIENTES='"+ingredientes.getText().toString()+"', PREPARACION='"
                    +preparacion.getText().toString()+"', OBSERVACIONES='"+observaciones.getText().toString()+"' WHERE ID=" +identificacion.getText().toString();
            tabla.execSQL(SQL);
            tabla.close();
            Toast.makeText(this, "Se actualizaron correctamente los datos", Toast.LENGTH_LONG).show();
        }catch (SQLiteException e){
            Toast.makeText(this, "ERROR: NO SE PUDO ACTUALIZAR", Toast.LENGTH_LONG).show();
        }

        habilitarBotonesYLimpiarCampos();
    }

    private void habilitarBotonesYLimpiarCampos(){
        identificacion.setText("");
        nombre.setText("");
        ingredientes.setText("");
        preparacion.setText("");
        observaciones.setText("");
        guardar.setEnabled(true);
        consultar.setEnabled(true);
        eliminar.setEnabled(true);
        modificar.setText("ACTUALIZAR");
        identificacion.setEnabled(true);
    }

}
