package com.example.myapplication;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationsActivity extends AppCompatActivity {

    private LocationDatabaseHelper databaseHelper;
    private EditText editTextLongitude;
    private EditText editTextLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        databaseHelper = new LocationDatabaseHelper(this);
        editTextLongitude = findViewById(R.id.editTextLongitude);
        editTextLatitude = findViewById(R.id.editTextLatitude);

        // Retrieve latitude and longitude values from the intent
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);

        // Set the retrieved values in EditText fields
        editTextLatitude.setText(String.valueOf(latitude));
        editTextLongitude.setText(String.valueOf(longitude));
    }

    public void addLocation(View view) {
        // Retrieve latitude and longitude values from EditText fields
        String longitudeText = editTextLongitude.getText().toString();
        String latitudeText = editTextLatitude.getText().toString();

        if (!longitudeText.isEmpty() && !latitudeText.isEmpty()) {
            double longitude = Double.parseDouble(longitudeText);
            double latitude = Double.parseDouble(latitudeText);

            // Get the complete address based on latitude and longitude
            String address = getCompleteAddress(latitude, longitude);

            if (!address.isEmpty()) {
                // Insert the location into the database
                insertLocation(address, latitude, longitude);
                editTextLongitude.setText("");
                editTextLatitude.setText("");
            }
        }
    }

    private void insertLocation(String address, double latitude, double longitude) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String insertQuery = "INSERT INTO " + LocationDatabaseHelper.TABLE_NAME + " (" +
                LocationDatabaseHelper.COLUMN_ADDRESS + ", " +
                LocationDatabaseHelper.COLUMN_LATITUDE + ", " +
                LocationDatabaseHelper.COLUMN_LONGITUDE + ") VALUES (?, ?, ?)";
        db.execSQL(insertQuery, new String[]{address, String.valueOf(latitude), String.valueOf(longitude)});
        db.close();
    }

    private String getCompleteAddress(double latitude, double longitude) {
        String address = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strToReturn = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strToReturn.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address = strToReturn.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    public void goBackToMainActivity(View view) {
        // Return to the main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void deleteLocation(View view) {
        // Retrieve latitude and longitude values from the intent
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);

        // Log the deletion information
        Log.d("DeleteLocation", "Latitude: " + latitude + ", Longitude: " + longitude);

        // Delete the location from the database and finish the activity
        deleteLocationFromDatabase(latitude, longitude);
        finish();
    }

    private void deleteLocationFromDatabase(double latitude, double longitude) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String selection = LocationDatabaseHelper.COLUMN_LATITUDE + " = ? AND " +
                LocationDatabaseHelper.COLUMN_LONGITUDE + " = ?";
        String[] selectionArgs = {String.valueOf(latitude), String.valueOf(longitude)};

        // Delete the location based on the latitude and longitude
        db.delete(LocationDatabaseHelper.TABLE_NAME, selection, selectionArgs);
        db.close();
    }
}
