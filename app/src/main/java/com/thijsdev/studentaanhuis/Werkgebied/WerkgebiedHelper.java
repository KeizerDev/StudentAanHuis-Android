package com.thijsdev.studentaanhuis.Werkgebied;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.thijsdev.studentaanhuis.Callback;
import com.thijsdev.studentaanhuis.Database.DatabaseHandler;
import com.thijsdev.studentaanhuis.Database.Werkgebied;
import com.thijsdev.studentaanhuis.GeoLocationHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WerkgebiedHelper {
    final GeoLocationHelper locHelper = new GeoLocationHelper();
    final WerkgebiedHTTPHandler werkgebiedHTTPHandler = new WerkgebiedHTTPHandler();

    private Context context;

    //Temp variables
    Elements werkgebieden = null;

    public WerkgebiedHelper(Context _context) {
        context = _context;
    }

    /**
     * Function to read the SAH 3.0 website for wekgebieden.
     * Places Elements into "werkgebieden" variable.
     * finished callback returns Elements werkgebieden.
     *
     * @throws RuntimeException
     * @param finished
     * @param failure
     */
    public void readWerkgebieden(final Callback finished, final Callback failure) {
        //Check variables
        if(context == null)
            throw new RuntimeException("Context is null. Call constructor first.");

        werkgebiedHTTPHandler.getWerkGebieden(context, new Callback() {
            @Override
            public void onTaskCompleted(Object... results) {
                Document doc = Jsoup.parse((String) results[0]);
                werkgebieden = doc.select("tr:has(td)");

                finished.onTaskCompleted(werkgebieden);
            }
        }, failure);
    }

    /**
     * Function to count the ammount of werkgebieden on SAH 3.0.
     *
     * @throws RuntimeException
     * @return
     */
    public int countWerkgebiedAantal() {
        if(werkgebieden == null)
            throw new RuntimeException("werkgebieden is null. Call readWerkgebieden() first.");
        else
            return werkgebieden.size();
    }

    /**
     * Function to process the werkgebieden from SAH 3.0.
     * itemFinished returns: bool isUpdate, Werkgebied werkgebied.
     * finished return nothing.
     *
     * @throws RuntimeException
     * @param itemFinished
     * @param finished
     */
    public void processWerkgebieden(Callback itemFinished, Callback finished) {
        //Check variables
        if(context == null)
            throw new RuntimeException("Context is null. Call constructor first.");
        if(werkgebieden == null)
            throw new RuntimeException("werkgebieden is null. Call readWerkgebieden() first.");

        final DatabaseHandler db = new DatabaseHandler(context);

        for (Element tr : werkgebieden) {
            Elements tds = tr.select("td");
            int id = Integer.parseInt(tds.get(0).child(0).attr("work_area_id"));

            Werkgebied werkgebiedDB = db.getWerkgebied(id);
            boolean isUpdate = werkgebiedDB != null;
            Werkgebied werkgebied;

            if (!isUpdate)
                werkgebied = new Werkgebied();
            else
                werkgebied = werkgebiedDB;

            int actief = 0;
            Location adres = locHelper.getLocationFromAddress(context, tds.get(2).text() + ", The Netherlands");

            if(tds.get(0).child(0).attr("checked").equals("checked"))
                actief = 1;

            //Only set the ID if it's not an update
            if(!isUpdate)
                werkgebied.setId(id);

            werkgebied.setActief(actief);
            werkgebied.setNaam(tds.get(1).text());
            werkgebied.setAdres(tds.get(2).text());
            werkgebied.setStraal(tds.get(3).text());
            if(adres != null) {
                werkgebied.setLat(adres.getLatitude());
                werkgebied.setLng(adres.getLongitude());
            }else{
                werkgebied.setLat(0.0);
                werkgebied.setLng(0.0);
            }

            if(!isUpdate)
                db.addWerkgebied(werkgebied);
            else
                db.updateWerkgebied(werkgebied);

            itemFinished.onTaskCompleted(isUpdate, werkgebied);
        }

        finished.onTaskCompleted();
    }

    //DEPRICATED
    public void updateWerkgebieden(final Activity activity, final Callback callback) {
        /*
        final DatabaseHandler db = new DatabaseHandler(activity);


                        final Werkgebied werkgebied = new Werkgebied();
                        int actief = 0;
                        Location adres = locHelper.getLocationFromAddress(activity, tds.get(2).text() + ", The Netherlands");

                        if(tds.get(0).child(0).attr("checked").equals("checked"))
                            actief = 1;

                        werkgebied.setId(id);
                        werkgebied.setActief(actief);
                        werkgebied.setNaam(tds.get(1).text());
                        werkgebied.setAdres(tds.get(2).text());
                        werkgebied.setStraal(tds.get(3).text());
                        if(adres != null) {
                            werkgebied.setLat(adres.getLatitude());
                            werkgebied.setLng(adres.getLongitude());
                        }else{
                            werkgebied.setLat(0.0);
                            werkgebied.setLng(0.0);
                        }

                        db.addWerkgebied(werkgebied);

                        Log.v("SAH", "Werkgebied Added");
                    }
                }

                callback.onTaskCompleted((Object[])null);
            }
        }, new Callback());
        */
    }

    public CharSequence[] getWerkgebiedenArray(Context context) {
        final DatabaseHandler db = new DatabaseHandler(context);

        ArrayList<String> werkgebieden = new ArrayList<String>();
        for(Werkgebied werkgebied : db.getActiveWerkgebieden()) {
            werkgebieden.add(werkgebied.getNaam());
        }

        return werkgebieden.toArray(new CharSequence[db.getActiveWerkgebieden().size()]);
    }

    public CharSequence[] getWerkgebiedenIDArray(Context context) {
        final DatabaseHandler db = new DatabaseHandler(context);

        ArrayList<String> werkgebieden = new ArrayList<String>();
        for(Werkgebied werkgebied : db.getActiveWerkgebieden()) {
            werkgebieden.add(Integer.toString(werkgebied.getId()));
        }

        return werkgebieden.toArray(new CharSequence[db.getActiveWerkgebieden().size()]);
    }

    public List<Werkgebied> getActiveWerkgebieden(Context context) {
        final DatabaseHandler db = new DatabaseHandler(context);

        return db.getActiveWerkgebieden();
    }

    public Location getFirstWerkgebiedLocation(Context context) {
        final DatabaseHandler db = new DatabaseHandler(context);
        List<Werkgebied> werkgebieden = db.getActiveWerkgebieden();
        if(werkgebieden.size() == 0)
            return null;

        return db.getActiveWerkgebieden().get(0).getLocation();
    }

    public Location getLocationFromWerkgebied(Context context, String WerkgebiedId) {
        final DatabaseHandler db = new DatabaseHandler(context);
        Werkgebied werkgebied = db.getWerkgebied(Integer.parseInt(WerkgebiedId));
        if(werkgebied == null)
            return null;

        return db.getActiveWerkgebieden().get(0).getLocation();
    }

    public void forceUpdateWerkgebieden(final Activity activity, Callback callback) {
        final DatabaseHandler db = new DatabaseHandler(activity);
        db.deleteAllWerkgebieden();
        updateWerkgebieden(activity, callback);
    }
}
