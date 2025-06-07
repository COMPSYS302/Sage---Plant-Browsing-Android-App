package com.example.sage.data;

import java.util.List;

public class Plant {

    // Fields
    private int plantid;
    private String name;
    private String category;
    private double price;
    private List<String> images;
    private String sunlight;
    private String season;
    private String water;
    private String description;
    private int views;

    // Required empty constructor for Firestore
    public Plant() {}

    // Getters

    public int getPlantid() {
        return plantid;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getImages() {
        return images;
    }

    public String getSunlight() {
        return sunlight;
    }

    public String getSeason() {
        return season;
    }

    public String getWater() {
        return water;
    }

    public String getDescription() {
        return description;
    }

    public int getViews() {
        return views;
    }

    // Setters
    public void setPlantid(int plantid) {
        this.plantid = plantid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void setSunlight(String sunlight) {
        this.sunlight = sunlight;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setWater(String water) {
        this.water = water;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setViews(int views) {
        this.views = views;
    }



}

