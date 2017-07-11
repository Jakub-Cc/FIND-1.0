package com.example.jc.find_10;

import android.content.Context;
import android.location.Location;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Klasa pomocnicza do skanowania adresow mac telefonow z okolicy
 */
public class MAC_getter
{
    /** Kontekst dzialania klasy */
    Context context;

    /**
     * Inicjalizuje wszystkie zmienne
     * @param context kontekst dzialania klasy
     */
    public MAC_getter(Context context)
    {
        this.context=context;
    }

    /**
     * Funkcja do przepakowania pomiarow do tekstu do wyslania
     * @param location lokacja telefony
     * @param date data pomiaru przekonwertowana na string
     * @param arrayList lista pomiarow
     * @return zformatowany tekst zawierajacy wszyskie pomiary
     */
    public String packager (Location location,String date, ArrayList<String> arrayList)
    {
        String result="";
        if (location!=null)
        {
            for (int i = 0; i < arrayList.size(); i++)
            {
                result+= arrayList.get(i) +";"+ date+";"+ location.getLatitude()+" "+location.getLongitude()+";;";
            }
        }
        return result;
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
            sb.append(i+1).append(". ").append(arrayList.get(i));
            sb.append("\n");
        }
        return "mac (NetworkInterface's) \n" + sb.toString();
    }

    /**
     * Tworzy liste pomiarow z adresami mac, nie dziala poprawnie, zwiazku z problemami technicznymi
     * @return pomiary
     */
    public ArrayList<String> mac()
    {
        try
        {
            java.util.List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            ArrayList<String> p2pMac=new ArrayList<>();
            for (int i=0; i<interfaces.size();i++)
            {
                String mac="";
                byte [] byt=null;
                if (interfaces.get(i).getHardwareAddress()!=null)
                {
                    byt=interfaces.get(i).getHardwareAddress();
                }
                if (byt!=null)
                {
                    for (byte aByt : byt)
                    {
                        mac += String.format("%02X:", aByt);
                    }
                    mac=mac.substring(0,mac.length()-1);
                }
                else
                {
                    mac = "null";
                }
                p2pMac.add(interfaces.get(i).toString()+ " "+mac);
            }
            return p2pMac;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
