package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddLocationDialogFragment extends AppCompatDialogFragment {
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private LocationDatabaseHelper databaseHelper;

    // Constructor that receives a reference to the database helper
    public AddLocationDialogFragment(LocationDatabaseHelper dbHelper) {
        this.databaseHelper = dbHelper;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflating the custom layout for the dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_location, null);

        editTextLatitude = view.findViewById(R.id.editTextLatitude);
        editTextLongitude = view.findViewById(R.id.editTextLongitude);

        builder.setView(view)
                .setTitle("Add Location")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String latitudeStr = editTextLatitude.getText().toString();
                        String longitudeStr = editTextLongitude.getText().toString();
                        if (!latitudeStr.isEmpty() && !longitudeStr.isEmpty()) {
                            // Parse latitude and longitude from input
                            double latitude = Double.parseDouble(latitudeStr);
                            double longitude = Double.parseDouble(longitudeStr);

                            // Get a reference to the MainActivity and call the addLocation method
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.addLocation(latitude, longitude);

                            // Close the dialog
                            dismiss();
                        } else {
                            // Show a toast message if latitude or longitude is missing
                            Toast.makeText(getActivity(), "Please enter latitude and longitude.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return builder.create();
    }
}
