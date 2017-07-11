package com.example.jc.find_10;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;



/**
 * Glowna klasa, laczaca wszystkie metody oraz klasy pomocnicze, nadzoruje okresowe wykonywanie wszystkich operacji, inicjalizuje widoki
 */
public class MainActivity extends AppCompatActivity
{
    /** Sluzy do pobierania danych z  wifi */
    private WIFI_getter wifi_getter;
    /** Sluzy do pobierania lokacji */
    private Location_getter location_getter;
    /** Sluzy do pobierania danych z sieci gsm */
    private GSM_getter gsm_getter;
    /** Sluzy do pobierania adresow mac, obecnie nie uzywany  */
    private MAC_getter mac_getter;
    /** Sluzy do wysylania danych na serwer*/
    private Sender sender;
    /** Sluzy do przechowywania spakowanych danych do wyslania */
    private JSONObject jsonObject;
    /** Czas pomiedzy kolejnymi skanowaniami, ustawiany w ustawieniach  */
    int Refresh_Interval;

    /** Czas pomiedzy kolejnym wyslaniem danych do serwera, ustawiany w ustawieniach  */
    int Send_Interval;

    /** Zmienna sluzaca do okresowych zadan */
    private Handler mHandler;

    /** Zmienna do pobierania ustawien z pamieci wewnetrznej telefonu   */
    SharedPreferences preferences;

    /** Zmienna pomocnicza do pracy na plikach w pamieci wewnetrznej  */
    File_Operator file_operator;

    /**
     * Metoda wywolywana przy wlaczeniu aplikacji, dynamicznie ustawia wielkosc pol tekstowych, inicjalizuje klasy pomocnicze,
     * przygotowuje puste paczki do wysylania danych, ustawia handler dla okresowych zadan oraz pobiera ustawienia aplikacji z pamieci wewnetrznej
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Dynamiczne ustawianie wielkosci pol tekstowycj
        Point po=new Point();
        getWindowManager().getDefaultDisplay().getSize(po);
        int width=po.x;
        int height=po.y;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height/4);
        params.setMargins(0, 0, 0, 10);
        findViewById(R.id.scrollView).setLayoutParams(params);
        findViewById(R.id.scrollView2).setLayoutParams(params);
        findViewById(R.id.scrollView3).setLayoutParams(params);

        //wszystie klasy pomocnicze do uzyskiwania danych
        location_getter = new Location_getter( getApplicationContext());
        wifi_getter=new WIFI_getter(getApplicationContext() );
        gsm_getter= new GSM_getter(getApplicationContext() );
        mac_getter=new MAC_getter(getApplicationContext() );

        sender=new Sender(getApplicationContext() );
        file_operator = new File_Operator();

        //przygotowania paczki do wyslania
        jsonObject = new JSONObject();
        try
        {
            jsonObject.put("WIFI", "");
            jsonObject.put("MAC", "");
            jsonObject.put("GSM", "");
            jsonObject.put("GSM_MASZTY", "");
            jsonObject.put("Sender_Mac",wifi_getter.get_mac());
        }
        catch(Exception e)
        {
            Log.e("OnCreate-JSON-Put", e.toString());
        }

        //Handler do okresowych zadan,
        mHandler = new Handler();

        //pobieranie ustawien z pamieci/drugiego okna
        preferences = getSharedPreferences("JC.FIND", Activity.MODE_PRIVATE);
    }

    /** Przy wylaczniu aplikacji z glownego okna, zaleznosci od ustawien wylacza prace w tle  */
    @Override
    protected void onPause()
    {
        super.onPause();
        if ( !preferences.getBoolean("Praca_w_tle",false) )
        {
            stopRepeatingTask();
        }
    }

    /** Uruchomiana przy zniszczeniu aplikacji, zatrzymuje wszystkie okresowe zadania  */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopRepeatingTask();
    }

    /** Przy powrocie do aplikacji z innego watku, wlacza cykliczne zadania oraz wymusza wlaczanie funcki GPS  */
    @Override
    protected void onResume()
    {
        super.onResume();

        Refresh_Interval = preferences.getInt("Refresh_Interval", 30000) ;
        Send_Interval=preferences.getInt("Send_Interval",1800000);

        startRepeatingTask();

        //sprawdza czy jest lokalizacja jest wlaczona jesli nie to wysyla do opcji,
        //nie da sie sensownie wymusic wlaczanie modulu to najlepsze obejscie
        if (! ((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            buildAlertMessageNoGps();
        }
    }

    /** Funkcja pomocnicza do wywolania informacji oraz wlaczenia okna z ustawieniami GPS */
    private void buildAlertMessageNoGps()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Twój moduł GPS jest wyłaczony, czy chcesz go włączyć?")
                .setCancelable(false)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    /** Uruchomiana przy przycisnieciu przycisku ustawien  */
    public void uruchom_ustawienia(View view)
    {
        final Intent intencja = new Intent(this,Settings.class);
        startActivity(intencja);
    }

    /** Funckja do okresowego skanowania wszystkich danych, uruchomia sie sama z pewnym opuznieniem  */
    Runnable Cyclic_refresh = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                reload(); //this function can change value of mInterval.
            }
            catch (Exception e)
            {
              Log.e("Cyclic",e.toString());
            }
            finally
            {
                mHandler.postDelayed(Cyclic_refresh, Refresh_Interval);
            }
        }
    };

    /** Funckja do okresowego wysylania wszystkich danych, uruchomia sie sama z pewnym opuznieniem  */
    Runnable Cyclic_Sender = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                send();
            }
            catch (Exception e)
            {
                Log.e("Cyclic",e.toString());
            }
            finally
            {
                mHandler.postDelayed(Cyclic_Sender, Send_Interval);
            }
        }
    };

    /** Wlacznie okresowych funkcji */
    void startRepeatingTask()
    {
        Cyclic_refresh.run();
        Cyclic_Sender.run();
    }

    /** Wylaczanie okresowych funkcji */
    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(Cyclic_refresh);
        mHandler.removeCallbacks(Cyclic_Sender);
    }
    

    /** Przy przycisnieciu przycisku test, chwilowo brak uzycia  */
    public void test(View view)
    {
        /*
        location_getter.get_Last_Known_Location();
        ConnectivityManager check = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        String help="";
        NetworkInfo[] info = check.getAllNetworkInfo();
        int i;
        for (i=0;i<info.length;i++)
        {
            help+=i+" "+info[i].toString()+"\n";
        }
        ((TextView)findViewById(R.id.textView2)).setText(help);
        */
        //Toast.makeText(getBaseContext(), wifi_resault, Toast.LENGTH_LONG).show();
    }


    /**
     * Przy przycisnieciu przycisku reload
     */
    public void reload_button(View view)
    {
        reload();
    }


    /**
     * Przeladowuje wszystkie pola tekstowe oraz dodaje do pliku wyniki skanowania
     */
    public void reload()
    {
        ArrayList<String> wifi_array = wifi_getter.wifi();
        ArrayList<String> gsm_array = GSM_getter.gsm(gsm_getter);
        ArrayList<String> mac_array = mac_getter.mac();
        ArrayList<String> gsm_maszty_array = gsm_getter.gsm_maszty();

        //zmienna pomocnicza aby wyswietlic wyniki skanowania sieci
        ArrayList<String> gsm_all = new ArrayList<>(gsm_array );
        gsm_all.addAll(gsm_maszty_array);

        ((TextView) findViewById(R.id.textView)).setText(wifi_getter.from_array_to_formatted_string(wifi_array));
        ((TextView) findViewById(R.id.textView2)).setText(gsm_getter.from_array_to_formatted_string(gsm_all) );
        ((TextView) findViewById(R.id.textView3)).setText(mac_getter.from_array_to_formatted_string(mac_array));

        String data= android.text.format.DateFormat.format("yyyy-MM-dd kk:mm:ss", new java.util.Date()).toString();

        String wifi_resault=wifi_getter.packager(location_getter.get_Last_Known_Location(), data, wifi_array);
        String gsm_resault=gsm_getter.packager(location_getter.get_Last_Known_Location(), data, gsm_array);
        String gsm_maszty_resault=gsm_getter.packager(location_getter.get_Last_Known_Location(), data, gsm_maszty_array);
        String mac_resault=mac_getter.packager(location_getter.get_Last_Known_Location(), data, mac_array);

        file_operator.add_to_file("Wifi_resault.txt",wifi_resault);
        file_operator.add_to_file("Gsm_resault.txt",gsm_resault);
        file_operator.add_to_file("Gsm_Maszty_resault.txt",gsm_maszty_resault);
        file_operator.add_to_file("Mac_resault.txt",mac_resault);
    }

    /**
     * przy przycisnieciu send
     */
    public void send_button(View view)
    {
        send();

    }

    //

    /**
     * wysyla wszystkie zapisane dane z plikow, na serwer,
     * w przypadku otrzymania komunikatu "Data sent successfully"  kasuje dane z plikow
     * chilowo nie wysyla wynikow skanow mac
     */
    public void send()
    {
        String wifi_resault=file_operator.Read("Wifi_resault.txt");
        String gsm_resault=file_operator.Read("Gsm_resault.txt");
        String gsm_maszty_resault=file_operator.Read("Gsm_Maszty_resault.txt");
        //String mac_resault=file_operator.Read("Mac_resault.txt");
        try
        {
            jsonObject.put("WIFI", wifi_resault);
            //jsonObject.put("MAC", mac_resault);
            jsonObject.put("GSM", gsm_resault);
            jsonObject.put("GSM_MASZTY", gsm_maszty_resault);
            sender.send(jsonObject);
        }
        catch(Exception e)
        {
            Log.e("Sender", e.toString());
        }
    }

}
