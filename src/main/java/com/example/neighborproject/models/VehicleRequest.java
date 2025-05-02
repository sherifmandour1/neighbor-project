package com.example.neighborproject.models;

public class VehicleRequest {
    private int length;
    private int quantity;


    public VehicleRequest() {}

    public VehicleRequest(int length, int quantity) {
        this.length = length;
        this.quantity = quantity;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "VehicleRequest{length=" + length + ", quantity=" + quantity + "}";
    }
}
