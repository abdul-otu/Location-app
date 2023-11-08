package com.example.myapplication;

public class LocationItem {
    private int id;
    private String address;
    private double latitude;
    private double longitude;

    // Constructor for the LocationItem class
    public LocationItem(int id, String address, double latitude, double longitude) {
        this.id = id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter method for retrieving the ID of the location
    public int getId() {
        return id;
    }

    // Getter method for retrieving the address of the location
    public String getAddress() {
        return address;
    }

    // Getter method for retrieving the latitude of the location
    public double getLatitude() {
        return latitude;
    }

    // Setter method for updating the address of the location
    public void setAddress(String address) {
        this.address = address;
    }

    // Getter method for retrieving the longitude of the location
    public double getLongitude() {
        return longitude;
    }

    // Custom method to format the location information as a string
    @Override
    public String toString() {
        return "ID: " + id + "\nAddress: " + address + "\nLatitude: " + latitude + "\nLongitude: " + longitude;
    }
}
