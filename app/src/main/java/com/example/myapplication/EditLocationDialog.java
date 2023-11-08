package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

public class EditLocationDialog extends DialogFragment {
    private LocationItem location;
    private EditText editTextNewAddress;

    // Constructor that receives a LocationItem object to edit
    public EditLocationDialog(LocationItem location) {
        this.location = location;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_location_dialog, null);
        editTextNewAddress = view.findViewById(R.id.editTextNewAddress);

        // Set the current address of the location in the EditText
        editTextNewAddress.setText(location.getAddress());

        builder.setView(view)
                .setTitle("Edit Location Address")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newAddress = editTextNewAddress.getText().toString();
                        if (!newAddress.isEmpty()) {
                            // Update the address of the location and refresh the locations in the MainActivity
                            location.setAddress(newAddress);
                            updateLocationInDatabase(location);
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.refreshLocations();
                        } else {
                            // Show a toast message if the new address is empty
                            Toast.makeText(getActivity(), "Address cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle cancel button click
                    }
                });

        return builder.create();
    }

    // Update the location's address in the database
    private void updateLocationInDatabase(LocationItem location) {
        LocationDatabaseHelper databaseHelper = new LocationDatabaseHelper(getContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocationDatabaseHelper.COLUMN_ADDRESS, location.getAddress());
        String whereClause = LocationDatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(location.getId()) };
        int updatedRows = db.update(LocationDatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);

        // Handle the update result if needed
        if (updatedRows > 0) {
            // Successfully updated
        } else {
            // Update failed
        }

        db.close();
    }
}
