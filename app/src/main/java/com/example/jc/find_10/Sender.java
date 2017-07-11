package com.example.jc.find_10;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Klasa odpowiedzialna za przesylanie danych na serwer
 * Do poprawy metoda POST, uzywa starych metod, problem wystepuje gdy siec blokuje wysylanie nie otrzymujemy wtedy inforamcji zwrotnej
 */
public class Sender
{
    /**Kontekst dzialania klasy */
    Context context;
    /**
     * Menager polaczen, sluzacy do sprawdzenia dostepnosci sieci
     */
    ConnectivityManager connectivityManager;

    /**
     * Inicjalizuje wszystkie zmienne
     * @param context kontekst klasy
     */
    public Sender(Context context)
    {
        this.context=context;
        this.connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Metoda wysylajaca plik JSON na wskazany adres URL
     * Wymaga poprawy aby rozwiazac problem blokowania wysylania przez siec
     * @param url adres Adres na ktory ma byc wyslany plik
     * @param jsonObject Przesylany plik JSON
     * @return Odpowiedź z serwera lub inforamcja o bledzie
     */
    public String POST(String url,JSONObject jsonObject)
    {
        InputStream inputStream = null;
        String result = "";
        try
        {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json;

            // 3. build jsonObject
            //JSONObject jsonObject = new JSONObject();
            //jsonObject.accumulate("name","Im a test");

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        }
        catch (Exception e)
        {
            Log.e("Sender", e.toString());
        }

        // 11. return result
        return result;
    }

    /** teskt pomocnyczy przechowujacy ostatnia odpowiedz serwera */
    String s="";

    /**
     * Funckja dzialajace w tle, majaca na celu wyslanei danych,
     * po wyslaniu jesli komunikat jest "Data sent successfully" kasuje dane
     */
    private class HttpAsyncTask extends AsyncTask<String, Void, String>
    {
        JSONObject jsonObject;
        HttpAsyncTask (JSONObject jsonObject)
        {
            this.jsonObject=jsonObject;
        }
        @Override
        protected String doInBackground(String... urls)
        {
            s=POST(urls[0],jsonObject);
            return s;
        }
        @Override
        protected void onPostExecute(String result)
        {
            Toast.makeText(context, "Data Sent!", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, s , Toast.LENGTH_LONG).show();
            if (s.equals("Data sent successfully"))
            {
                File_Operator file_operator = new File_Operator();
                file_operator.write_to_file("Wifi_resault.txt", "");
                file_operator.write_to_file("Gsm_resault.txt", "");
                file_operator.write_to_file("Mac_resault.txt", "");
                file_operator.write_to_file("Gsm_Maszty_resault.txt", "");
            }
        }
    }

    /**
     * Metoda konwerujaca strumien danych na lancuch znakow
     * @param inputStream Strumien danych
     * @return Zwracany lancuch znakow
     * @throws IOException
     */
    private static String convertInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    /**
     * Metoda przesylajaca wskazany plik JSON na adres http://156.17.134.130:6229
     * @param jsonObject Przesylany plik JSON
     */
    public void send(JSONObject jsonObject)
    {
        if (isConnected())
        {
            new HttpAsyncTask(jsonObject).execute("http://156.17.134.130:6229");
        }
        else
        {
            Toast.makeText( context, "Data not send!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Metoda sprawdzajaca dostep do sieci
     * Nie informuje jezeli zablokowane jest wysylanie
     * @return Dostep do sieci
     */
    public boolean isConnected()
    {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
