package com.example.jc.find_10;

import android.content.Context;
import android.location.Location;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Klasa pomocnicza do skanowania sieci gsm
 */
public class GSM_getter
{
    /** Menadzer polaczen */
    TelephonyManager telManager;

    /** Kontekst dzialania klasy */
    Context context;

    /**
     * Inicjalizuje wszystkie zmienne
     * @param context kontekst dzialania klasy
     */
    public GSM_getter(Context context)
    {
        this.context=context;
        this.telManager=((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
    }

    /**
     * Funkcja do przepakowania pomiarow do tekstu do wyslania
     * @param location lokacja telefony
     * @param date data pomiaru przekonwertowana na string
     * @param arrayList lista pomiarow
     * @return zformatowany tekst zawierajacy wszyskie pomiary
     */
    public String packager(Location location, String date, ArrayList<String> arrayList)
    {
        String result="";
        if (location!=null)
        {
            for (int i = 0; i < arrayList.size(); i++)
            {
               result+= arrayList.get(i) + ";"+date+";"+ location.getLatitude()+" "+location.getLongitude()+";;";
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
        return "GSM (Lac, Cid, Psc, NetTyp, RSSI dBm \n" + sb.toString();
    }

    /**
     * Zwraca nazwe dostawcy sieci
     */
    public String get_network_provider()
    {
        return telManager.getNetworkOperatorName();
    }

    /**
     * Tworzy liste pomiarow z masztami gsm, zwierajace numery Lac, Cid, dostawce sieci oraz sile sygnalu
     * w zaleznosci od typu sieci nie sa dostpne numery Lac oraz cid
     * @return pomiary
     */
    public ArrayList<String> gsm_maszty()
    {
        List<NeighboringCellInfo> NeighboringList = telManager.getNeighboringCellInfo();
        ArrayList<String> gsm_maszty=new ArrayList<>();
        String help;
        String dBm;
        int rssi;
        for(int i=0; i < NeighboringList.size(); i++)
        {
            rssi=NeighboringList.get(i).getRssi();
            if(rssi != NeighboringCellInfo.UNKNOWN_RSSI && NeighboringList.get(i).getLac()!=-1 && NeighboringList.get(i).getLac()!=0
                    && NeighboringList.get(i).getCid()!=-1 && NeighboringList.get(i).getCid()!=65535)
            {
                dBm = String.valueOf(-113 + 2 * rssi);
                help = NeighboringList.get(i).getLac() +";"+ NeighboringList.get(i).getCid() +";"+ get_network_provider() +";"+ dBm;
                gsm_maszty.add(help);
            }
        }
        return gsm_maszty;
    }

    /**
     * Tworzy liste pomiarow z sygnalami gsm, zwierajace typ sieci, dostawce sieci oraz sile sygnalu
     * @return pomiary
     */
    public static ArrayList<String> gsm(GSM_getter gsm_getter)
    {
        List<NeighboringCellInfo> NeighboringList = gsm_getter.telManager.getNeighboringCellInfo();
        ArrayList<String> gsm=new ArrayList<>();

        for(int i=0; i < NeighboringList.size(); i++)
        {
            String help;
            String dBm;
            int rssi = NeighboringList.get(i).getRssi();
            if(rssi != NeighboringCellInfo.UNKNOWN_RSSI /*&&( NeighboringList.get(i).getLac()==-1 || NeighboringList.get(i).getLac()==0
                    || NeighboringList.get(i).getCid()==-1 || NeighboringList.get(i).getCid()==65535 )*/ )
            {
                dBm = String.valueOf(-113 + 2 * rssi);
                help = gsm_getter.get_network_provider()+";"+NeighboringList.get(i).getNetworkType()+ ";" + dBm;
                gsm.add(help);
            }
        }
        return gsm;
    }
}
