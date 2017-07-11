package com.example.jc.find_10;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

//Maly problem z wyswietlaniem wartosci spinerow, inne przerwy miedzy wartosciami,
// nie wiem skad to sie bierze

/**
 * Klasa odpowiedzialna za przechowywanie ustawien aplikacji
 */
public class Settings extends AppCompatActivity
{
    /** Tablica wyswietlanych w spinerze mozliwych do ustawienia odstepow w czasie skanowania*/
    String[] refresh={"10 sekund","15 sekund","20 sekund","25 sekund","30 sekund","45 sekund","1 minuta","1:30 minut  ","3 minuty","5 minut"};
    /** Tablica wyswietlanych w spinerze mozliwych do ustawienia odstepow w czasie wysylania*/
    String[] sender={"15 minut","30 minut","45 minut","1 godzina","1:30 godzin"};
    /** Tablca wartosci w milisekundach odpowiadajacych znajdujacym sie w tablic refresh lancuchom*/
    int [] refresh_value={10000,15000,20000,25000,30000,45000,60000,90000,180000,300000};
    /** Tablca wartosci w milisekundach odpowiadajacych znajdujacym sie w tablic sender lancuchom*/
    int [] sender_value={900000,1800000,2700000,3600000,5400000};

    Spinner spinner_refresh;
    Spinner spinner_sender;
    CheckBox checkBox;
    /** Klasa sluzaca do pobierania z pamieci wewnetrznej*/
    private SharedPreferences preferences;

    /**
     * Metoda wywolywana przy utworzeniu aktywnosci pobierajaca ustawienia z pamieci wewnetrznej, w przypadku
     * inicjaluzje widok ustwien
     * ich braku uzywa wartosci domyslnych
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ustawienia);

        preferences = getSharedPreferences("JC.FIND", Activity.MODE_PRIVATE);

        spinner_refresh =(Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, refresh);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_refresh.setAdapter(dataAdapter);
        spinner_refresh.setSelection(preferences.getInt("Refresh_Position", 0));

        spinner_sender =(Spinner)findViewById(R.id.spinner2);
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, sender);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_sender.setAdapter(dataAdapter2);
        spinner_sender.setSelection(preferences.getInt("Send_Position", 0));

        checkBox=(CheckBox) findViewById(R.id.checkBox);
        checkBox.setChecked(preferences.getBoolean("Praca_w_tle",false));
    }

    /**
     * Przy nacisnieciu powrotu
     */
    @Override
    public void onBackPressed()
    {
        finish();
    }

    /**
     * przy nacisnieciu przycisku powrotu
     */
    public void back(View view)
    {
        onBackPressed();
    }

    /**
     * Metoda zapisujaca wybrane ustawienia
     * Wywolywana przy nacisnieciu przycisku "Zapisz"
     */
    public void zapisz(View view)
    {
        int refresh_position=spinner_refresh.getSelectedItemPosition();
        int send_position=spinner_sender.getSelectedItemPosition();

        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt("Refresh_Position",refresh_position);
        preferencesEditor.putInt("Send_Position", send_position);
        preferencesEditor.putInt("Refresh_Interval",refresh_value[refresh_position]);
        preferencesEditor.putInt("Send_Interval", sender_value[send_position]);
        preferencesEditor.putBoolean("Praca_w_tle",checkBox.isChecked());
        preferencesEditor.apply();
        Toast.makeText(getBaseContext(), "Zapisano ustawienia", Toast.LENGTH_SHORT).show();
    }
}
