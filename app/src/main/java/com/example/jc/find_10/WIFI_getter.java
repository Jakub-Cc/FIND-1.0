package com.example.jc.find_10;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

/**
 * Klasa pomocnicza do skanowania sieci WIFI
 */
public class WIFI_getter
{
    /** Menedzer wifi*/
    WifiManager mainWifi;

    /** Kontekst dzialania klasy*/
    Context context;

    /**
     * Inicjalicuje wszystkie zmienne
     * @param context kontekst klasy
     */
    public WIFI_getter(Context context)
    {
        mainWifi = (WifiManager) context.getSystemService( Context.WIFI_SERVICE );
        this.context=context;
    }


    /**
     * Funkcja do opuzniani pracy zadania
     * @param time_ms czas czekania
     */
    public void wait_helper(int time_ms)
    {
        try
        {
            synchronized(this)
            {
                wait(time_ms);
            }
        }
        catch(InterruptedException ex)
        {
            Log.e("wait_helper",ex.toString());
        }
    }

    //Zwraca numer mac telefonu

    /**
     * Zwraca nummer mac telefonu
     * @return numer mac
     */
    public String get_mac()
    {
        return mainWifi.getConnectionInfo().getMacAddress();
    }


    /**
     * Funkcja do przepakowania pomiarow do tekstu do wyslania
     * @param location lokacja telefony
     * @param date data pomiaru przekonwertowana na string
     * @param arrayList lista pomiarow
     * @return zformatowany tekst zawierajacy wszyskie pomiary
     */
    public String packager( Location location,String date,  ArrayList<String> arrayList)
    {
        String result="";
        if (location!=null)
        {
            for (int i = 0; i < arrayList.size(); i++)
            {
                result+= arrayList.get(i)+";" + date +";"+ location.getLatitude()+" "+location.getLongitude()+";;";
            }
            return result;
        }
        /*
        //tylko do debugowania gdy loc==null, potem return "";
        else
        {
            for (int i = 0; i < arrayList.size(); i++)
            {
                result+= arrayList.get(i) +";"+ date +";loc null"+";;";
            }
            return result;
        }
        */
        return "";
    }

    /**
     * Sluzy do utworzenia sformatowanego tekstu do wyswietlania pomiarow w glowym oknie
     * @param arrayList lista pomiarow
     * @return zformatowny tekst z pomiarami
     */
    public String from_array_to_formatted_string(ArrayList<String> arrayList)
    {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<arrayList.size();i++)
        {
            sb.append(i+1).append(". ").append(arrayList.get(i)).append(" ; ");
            sb.append("\n");
        }
        return "Wifi (SSID, BSSID, LEVEL) \n" + sb.toString();
    }


    /**
     * Tworzy liste pomiarow z sieciami wifi , zwierajace numery SSID, BSSID, sile sygnalu
     * Samodzielnie wlacza modul wifi
     * @return pomiary
     */
    public ArrayList<String> wifi()
    {
        List <ScanResult> wifiList;

        boolean helper=false;
        if (mainWifi.getWifiState()==0 || mainWifi.getWifiState()==1)
        {
            helper=true;
            mainWifi.setWifiEnabled(true);
        }
        while (mainWifi.getWifiState()!=3)
        {
            wait_helper(200);
        }
        if (helper)
        {
            wait_helper(1000);
        }
        ArrayList<String> wifi=new ArrayList<>();
        wifiList = mainWifi.getScanResults();
        String help;
        for(int i = 0; i < wifiList.size(); i++)
        {
            help=(wifiList.get(i)).SSID+";"+(wifiList.get(i)).BSSID+";"+
                    (wifiList.get(i)).level;
            wifi.add(help);
        }
        return wifi;
    }
}
