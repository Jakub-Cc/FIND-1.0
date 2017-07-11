package com.example.jc.find_10;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**Klasa zarzadazajaca plikami
 */
public class File_Operator
{
    /**Sciezka folderu w ktorym zapisywane sa pliki aplikacji*/
    String path="/JC.FIND";

    /**
     * Metoda zapisujaca w danym pliku wskazny lancuch znakow. Jezeli plik już istnieje zostaje on nadpisany.
     * @param filename Nazwa pliku do ktorego lancuch ma byc zapisany
     * @param wynik Zapisywany lancuch znakow
     * @return
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean write_to_file(String filename, String wynik)
    {
        if (!Is_External_Write())
            return false;

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + path);
        dir.mkdirs();
        File file = new File(dir, filename);
        try
        {
            FileOutputStream fos=new FileOutputStream(file);
            fos.write(wynik.getBytes());

            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Metoda dopisujaca do pliku wskazny lancuch znakow.
     * @param filename Nazwa pliku do którego lancuch ma byc dopisany.
     * @param wynik Dopisywany łańcuch
     * @return
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean add_to_file(String filename, String wynik)
    {
        if (!Is_External_Write())
            return false;

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + path);
        dir.mkdirs();
        File file = new File(dir, filename);

        try
        {
            FileOutputStream fos=new FileOutputStream(file,true);
            fos.write(wynik.getBytes());
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Metoda sprawdzajaca uprawnienia do odczytu pamięci zewnętrznej
     * @return Możliwość odczytu pamięci zewnętrznej
     */
    public boolean Is_External_Read()
    {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    /**
     * Metoda sprawdzajaca uprawnienia do zapisu w pamięci zewnętrznej
     * @return Możliwość zapisu w pamięci zewnętrznej
     */
    public boolean Is_External_Write()
    {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            return true;
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState()))
        {
            return false;
        }
        else
        {
            return false;
        }
    }

    /**
     * Metoda konwerujaca strumien danych na lancuch znakow
     * @param is Strumien danych
     * @return Zwracany lancuch znakow
     * @throws Exception
     */
    public String convertStreamToString(InputStream is) throws Exception
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Metoda odczytujaca zawartosc pliku
     * @param filename Odczytywany plik
     * @return lancuch znakow zawarty w pliku
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public String Read (String filename)
    {
        if (!Is_External_Read())
        {
            return "";
        }
        else
        {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + path);
            dir.mkdirs();
            File file = new File(dir, filename);
            String result="";
            try
            {
                FileInputStream fin = new FileInputStream(file);
                result = convertStreamToString(fin);
                fin.close();
            }
            catch(Exception e)
            {

                e.printStackTrace();
            }
            return result;
        }
    }
}
