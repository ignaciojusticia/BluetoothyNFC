package com.ignaciojusticia.bluetoothynfc.bluetoothynfc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Ignacio Justicia Ramos
 */

public class MainActivity extends AppCompatActivity {
    private EditText nombre, direccion;
    private String name, macAddress;


    private NfcAdapter mNfcAdapter;    // Adaptador NFC
    private PendingIntent mNfcPendingIntent;
    boolean mWriteMode = false;  // Modo de escritura (inicialmente a false)


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.acerca_de:

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Botón de escribir etiqueta
        ((ImageButton) findViewById(R.id.boton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombre = (EditText) findViewById(R.id.nombre);
                direccion = (EditText) findViewById(R.id.direccion);
                name = nombre.getText().toString();
                macAddress = direccion.getText().toString();

                mNfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
                mNfcPendingIntent = PendingIntent.getActivity(MainActivity.this, 0,
                        new Intent(MainActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

                enableTagWriteMode();
                new AlertDialog.Builder(MainActivity.this).setTitle("Acerque la etiqueta NFC a su dispositivo")
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                disableTagWriteMode();
                            }
                        }).create().show();
            }
        });



        // Botón de ayuda
        ImageButton img = (ImageButton) findViewById(R.id.ayuda);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                DialogoAlerta dialogo = new DialogoAlerta();
                dialogo.show(fragmentManager, "tagAlerta");
            }
        });
    }


    /**
     *  Diálogo que se muestra al pulsar el botón de ayuda.
     *  Se encarga de explicar las instrucciones básicas de funcionamiento de de la aplicación.
     */
    public static class DialogoAlerta extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(
                    "1.- Escriba el nombre y la dirección bluetooth del dispositivo al que desea conectarse.\n2.- Pulse el botón <<Escribir etiqueta>>.\n3.- Acerque una etiqueta NFC.\n4.- Compruebe que la escritura ha sido correcta.")
                    .setTitle("Información").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            return builder.create();
        }
    }


    /**
     *  Habilita el modo de escritura en el Tag NFC
     */
    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }


    /**
     *  Deshabilita el modo de escritura en el Tag NFC
     */
    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }

    // ****************************************************************************************
    // ****************              Intent detector del TAG                 ******************
    // ****************************************************************************************

    /**
     *  Intent detector del Tag NFC encargado de preparar el mensaje NDEFMESSAGE
     * @param intent es el evento que recibirá cuando un Tag NFC se aproxime al smartphone
     */
    @Override
    protected void onNewIntent(Intent intent){
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);  // Etiqueta NFC detectada

            String[] macAddressParts = macAddress.split(":");      // Vector de String que contiene la dirección Bluetooth del dispositivo
            byte[] di = convertToBytes(macAddressParts);           // se convierte a un vector de bytes para poder tratar con ella

            String[] nameParts2 = name.split("");                  // Separación de cada uno de los caracteres del Nombre
            String[] nameParts = new String[nameParts2.length-1];
            for(int i=0; i<nameParts.length; i++){                 // Se elimina el primer espacio en blanco
                nameParts[i]=nameParts2[i+1];
            }
            byte[] no = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                no = name.getBytes(StandardCharsets.UTF_8);
            }

            int tam = nameParts.length;           // tamaño del nombre de dispositivo Bluetooth

            byte[] mimeData = new byte[] { 0x21, 0x00 };           // cabecera opcional OOB
            byte[] m = new byte[tam + 20];                         // creo el vector

            di = flip(di);  // CODIFICACIÓN LITTLE ENDIAN para el campo dirección BT
            no = flip(no);  // CODIFICACIÓN LITTLE ENDIAN para el campo nombre de BT

            System.arraycopy(mimeData, 0, m, 0, 2);                // copio la cabecera opcional
            System.arraycopy(di, 0, m, 2, di.length - 1);            // copio la direccion

            String tamanio = Integer.toHexString(tam);             // Guardo el tamaño del nombre de dispositivo
                                                                   // en un String


            byte[] aux1 = new byte[] {Byte.valueOf(tamanio),0x09};   // EIR Data Length y EIR Data Type
                                                                        // 0x09 indica Complete Local Name
            System.arraycopy(aux1, 0, m, 1+di.length, 2);          // copio EIR Data Type y EIR Data Length
            System.arraycopy(no, 0, m, 1+di.length+2, no.length);

            byte[] aux2 = new byte[]{
                    0x04, // EIR Data Length: 4 Bytes
                    0x0D, // EIR Data Type: Class of Device
                    0x08, // Minor-Device Class: Set-Top-Box
                    0x04, // Major-Device Class: Audio/Video
                    0x24, // Service Class: Audio
                    0x05, // EIR Data Length
                    0x03, // EIR Data Type: 16-bit Service Class UUID list (complete)
                    0x1E, // HFP-HF
                    0x11,
                    0x0B, // A2DP-SNK
                    0x11};

            System.arraycopy(aux2, 0, m, 1+di.length+no.length+2, 11); // Copio el resto

            NdefMessage message = new NdefMessage(NdefRecord.createMime("application/vnd.bluetooth.ep.oob", m));  // Formato del mensaje NDEFMESSAGE

            if (writeTag(message, detectedTag)) {
                Toast.makeText(this, "Etiqueta escrita correctamente", Toast.LENGTH_LONG).show();    // Mensaje de éxito en la escritura
            }
        }
    }

    /**
     *
     *  Método encargado de la escritura en la etiqueta o tag NFC.
     *
     * @param message es el mensaje que se desea escribir, con formato NDEFMESSAGE
     * @param tag objeto de tipo Tag que representa la etiqueta a escribir
     * @return
     */
    private boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    // Contemplo que hay etiquetas preparadas para que no se puedan sobreescribir
                    Toast.makeText(getApplicationContext(), "Error: etiqueta no escribible", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(getApplicationContext(), "Error: etiqueta demasiado pequeña", Toast.LENGTH_SHORT).show();
                    return false;
                }
                ndef.writeNdefMessage(message);  // escribimos el mensaje en la etiqueta
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Este método se encarga de invertir un vector de bytes
     *
     * @param a es el vector de tipo byte[] que recibe como argumento
     * @return
     */
    private byte[] flip(byte[] a) {
        for (int i = 0; i < a.length / 2; i++) {
            byte temp = a[i];
            a[i] = a[a.length - i - 1];
            a[a.length - i - 1] = temp;
        }
        return a;
    }

    /**
     *  Convierte un array de Strings a otro de bytes
     *
     * @param strings recibe como argumento un vector de String
     * @return devuelve un vector de bytes
     */
    private static byte[] convertToBytes(String[] strings) {
        byte[] data = new byte[strings.length];
        String str;
        for (int i = 0; i < strings.length; i++) {
            str = strings[i];
            data[i] = hexStringToByte(str);
        }
        return data;
    }

    /**
     * Este método convierte un String que contiene un dato en hexadecimal a byte
     *
     * @param data es el String que recibe como argumento
     * @return devuelve un dato de tipo byte
     */
    private static byte hexStringToByte(String data) {

        return (byte) ((Character.digit(data.charAt(0), 16) << 4) + Character.digit(data.charAt(1), 16));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public String toString() {
        return super.toString();
    }
}
