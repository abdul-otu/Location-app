package com.example.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private LocationDatabaseHelper databaseHelper;
    private EditText editTextSearch;
    private ListView listViewResults;
    private ArrayList<LocationItem> allLocations = new ArrayList<>();
    private ArrayAdapter<String> locationsAdapter;
    private ArrayList<String> filteredLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database helper and populate the location data
        databaseHelper = new LocationDatabaseHelper(this);
        fillDatabaseWithLocations();

        // Initialize UI elements
        editTextSearch = findViewById(R.id.editTextSearch);
        listViewResults = findViewById(R.id.listViewResults);
        locationsAdapter = new ArrayAdapter<>(this, R.layout.list_item_location, R.id.textLocation, filteredLocations);
        listViewResults.setAdapter(locationsAdapter);

        // Load locations from the database and populate the initial search results
        loadLocationsFromDatabase();

        // Set up a text change listener for the search input
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchLocations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set up item click listener for editing location addresses
        listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editLocationAddress(position);
            }
        });

        // Initialize the adapter with location data
        locationsAdapter.addAll(getLocationStrings(allLocations));
    }

    public void onDeleteButtonClick(View view) {
        int position = listViewResults.getPositionForView(view);
        if (position != ListView.INVALID_POSITION) {
            // Delete a location when the delete button is clicked
            LocationItem location = allLocations.get(position);
            removeFromDatabase(location);
            allLocations.remove(location);
            filteredLocations.clear();
            filteredLocations.addAll(getLocationStrings(allLocations));
            locationsAdapter.notifyDataSetChanged();
        }
    }

    public void editLocationAddress(final int position) {
        // Show a dialog for editing a location's address
        LocationItem location = allLocations.get(position);
        EditLocationDialog editDialog = new EditLocationDialog(location);
        editDialog.show(getSupportFragmentManager(), "EditLocationDialog");
    }

    public void refreshLocations() {
        // Refresh and update the list of locations
        loadLocationsFromDatabase();
        filteredLocations.clear();
        filteredLocations.addAll(getLocationStrings(allLocations));
        locationsAdapter.notifyDataSetChanged();
    }

    private void removeFromDatabase(LocationItem location) {
        // Remove a location from the database
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String whereClause = LocationDatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(location.getId()) };
        db.delete(LocationDatabaseHelper.TABLE_NAME, whereClause, whereArgs);
        db.close();
    }

    private void fillDatabaseWithLocations() {
        // Fill the database with initial locations from a file
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + LocationDatabaseHelper.TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        cursor.close();
        if (rowCount == 0) {
            try {
                InputStream inputStream = getAssets().open("locations.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                double latitude = 0.0;
                double longitude = 0.0;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        latitude = Double.parseDouble(parts[0]);
                        longitude = Double.parseDouble(parts[1]);
                        String address = getCompleteAddress(latitude, longitude);
                        insertLocation(db, address, latitude, longitude);
                    }
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        db.close();
    }

    private void loadLocationsFromDatabase() {
        // Load locations from the database into the 'allLocations' list
        allLocations.clear();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] projection = {
                LocationDatabaseHelper.COLUMN_ID,
                LocationDatabaseHelper.COLUMN_ADDRESS,
                LocationDatabaseHelper.COLUMN_LATITUDE,
                LocationDatabaseHelper.COLUMN_LONGITUDE
        };
        Cursor cursor = db.query(
                LocationDatabaseHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(LocationDatabaseHelper.COLUMN_ID));
            String address = cursor.getString(cursor.getColumnIndex(LocationDatabaseHelper.COLUMN_ADDRESS));
            double latitude = cursor.getDouble(cursor.getColumnIndex(LocationDatabaseHelper.COLUMN_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndex(LocationDatabaseHelper.COLUMN_LONGITUDE));
            allLocations.add(new LocationItem(id, address, latitude, longitude));
        }
        cursor.close();
        db.close();
    }

    private String getCompleteAddress(double latitude, double longitude) {
        // Get the complete address for a given latitude and longitude
        String address = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strToReturn = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strToReturn.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address = strToReturn.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }

    private void insertLocation(SQLiteDatabase db, String address, double latitude, double longitude) {
        // Insert a new location into the database
        String insertQuery = "INSERT INTO " + LocationDatabaseHelper.TABLE_NAME + " (" +
                LocationDatabaseHelper.COLUMN_ADDRESS + ", " +
                LocationDatabaseHelper.COLUMN_LATITUDE + ", " +
                LocationDatabaseHelper.COLUMN_LONGITUDE + ") VALUES (?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(insertQuery);
        statement.bindString(1, address);
        statement.bindDouble(2, latitude);
        statement.bindDouble(3, longitude);
        statement.execute();
    }

    private void searchLocations(String query) {
        // Filter and update the displayed location list based on the search query
        filteredLocations.clear();
        for (LocationItem location : allLocations) {
            if (location.getAddress().toLowerCase().contains(query.toLowerCase())) {
                filteredLocations.add(location.getAddress() + "\nLatitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude());
            }
        }
        locationsAdapter.notifyDataSetChanged();
    }

    private List<String> getLocationStrings(List<LocationItem> locations) {
        // Create a list of strings from the LocationItem objects for display
        List<String> locationStrings = new ArrayList<>();
        for (LocationItem location : locations) {
            locationStrings.add(location.getAddress() + "\nLatitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude());
        }
        return locationStrings;
    }

    public void showAddLocationDialog(View view) {
        // Show the "Add Location" dialog
        AddLocationDialogFragment dialog = new AddLocationDialogFragment(databaseHelper);
        dialog.show(getSupportFragmentManager(), "AddLocationDialog");
    }

    public void addLocation(double latitude, double longitude) {
        if (isValidLatitude(latitude) && isValidLongitude(longitude)) {
            // Add a new location to the database and refresh the list
            String address = getCompleteAddress(latitude, longitude);
            insertLocationToDatabase(address, latitude, longitude);
            loadLocationsFromDatabase();
            filteredLocations.clear();
            filteredLocations.addAll(getLocationStrings(allLocations));
            locationsAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidLatitude(double latitude) {
        // Check if the latitude value is within the valid range
        return latitude >= -90.0 && latitude <= 90.0;
    }

    private boolean isValidLongitude(double longitude) {
        // Check if the longitude value is within the valid range
        return longitude >= -180.0 && longitude <= 180.0;
    }

    public LocationDatabaseHelper getLocationDatabaseHelper() {
        return databaseHelper;
    }

    private void insertLocationToDatabase(String address, double latitude, double longitude) {
        // Insert a new location into the database
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String insertQuery = "INSERT INTO " + LocationDatabaseHelper.TABLE_NAME + " (" +
                LocationDatabaseHelper.COLUMN_ADDRESS + ", " +
                LocationDatabaseHelper.COLUMN_LATITUDE + ", " +
                LocationDatabaseHelper.COLUMN_LONGITUDE + ") VALUES (?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(insertQuery);
        statement.bindString(1, address);
        statement.bindDouble(2, latitude);
        statement.bindDouble(3, longitude);
        statement.execute();
        db.close();
    }

    public void onEditButtonClick(View view) {
        int position = listViewResults.getPositionForView(view);
        if (position != ListView.INVALID_POSITION) {
            // Handle editing a location when the edit button is clicked
            editLocationAddress(position);
        }
    }
}
