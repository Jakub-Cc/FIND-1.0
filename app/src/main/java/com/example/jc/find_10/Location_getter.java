package com.example.jc.find_10;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Klasa pomocnicza do pobierania aktualnej lokalizacji
 * Brak optymalizacji pod wzgledem poboru pradu
 */
public class Location_getter
{
    /** Kontekst dzialania klasy */
    Context context;
    /** Menedzer lokalizacji */
    LocationManager locationManager;
    /** Nazwa obecnego dostarczyciela lokalizacji  */
    String locationProvider;
    /** Ostatnio znan lokalizacja*/
    Location lastKnownLocation;
    /**Sluchacz na zmiany znanej lokalizacji */
    LocationListener locationListener;

    /**
     * Inicjalizuje wszystkie zmienne oraz zluchacze
     * @param context kontekst klasy
     */
    public Location_getter(Context context)
    {
        this.context=context;
        this.locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener()
        {
            public void onLocationChanged(Location location)
            {
                //Toast.makeText(context, "Location changed ", Toast.LENGTH_SHORT).show();
            }
            public void onStatusChanged(String s,int i, Bundle b)
            {
                //Toast.makeText(context, "status changed ", Toast.LENGTH_LONG).show();
            }
            public void onProviderEnabled(String s)
            {
                //Toast.makeText(context, "provide enabled ", Toast.LENGTH_LONG).show();
            }
            public void onProviderDisabled(String s)
            {
                //Toast.makeText(context, "provider disabled ", Toast.LENGTH_LONG).show();
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    /**
     * Zwraca ostatnio znana lokalizacje
     * @return ostatnio znana lokalizacja
     */
    Location get_Last_Known_Location()
    {
        locationProvider = LocationManager.PASSIVE_PROVIDER;
        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation==null)
        {
            //Toast.makeText(context, "pasiv  null", Toast.LENGTH_LONG).show();
        }
        else
        {
            //Toast.makeText(context, lastKnownLocation.toString(), Toast.LENGTH_LONG).show();
            return lastKnownLocation;
        }
        locationProvider = LocationManager.NETWORK_PROVIDER;
        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation==null)
        {
           // Toast.makeText(context, "net null", Toast.LENGTH_LONG).show();
        }
        else
        {
            //Toast.makeText(context, lastKnownLocation.toString(), Toast.LENGTH_LONG).show();
            return lastKnownLocation;
        }
        locationProvider = LocationManager.GPS_PROVIDER;
        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation==null)
        {
           // Toast.makeText(context, "gps null", Toast.LENGTH_LONG).show();
        }
        else
        {
            //Toast.makeText(context, lastKnownLocation.toString(), Toast.LENGTH_LONG).show();
            return lastKnownLocation;
        }
        return null;
    }
}
