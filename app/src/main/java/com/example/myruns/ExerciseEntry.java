package com.example.myruns;

import java.util.Calendar;

public class ExerciseEntry {
    private Long id;
    private int mInputType = 1;  // Manual, GPS or automatic
    private String mActivityType = "Running";     // Running, cycling etc.
    private String mDateTime;    // When does this entry happen
    private int mDuration = 100;         // Exercise duration in seconds
    private float mDistance = 250;      // Distance traveled. Either in meters or feet.
    private float mAvgPace = 25;       // Average pace
    private float mAvgSpeed = 12;    // Average speed
    private int mCalorie = 10;        // Calories burnt
    private float mClimb = 314;         // Climb. Either in meters or feet.
    private int mHeartRate = 120;       // Heart rate
    private String mComment = "hello world";       // Comments
    private String units = "metric";
    private String gpsData = "";
    //private ArrayList<LatLng> mLocationList; // Location list
    public String getUnits() { return this.units; }

    public Long getId() {
        return id;
    }

    public int getInputType() {
        return mInputType;
    }

    public String getActivityType() {
        return mActivityType;
    }

    public String getDateTime() {
        return mDateTime;
    }

    public int getDuration() {
        return mDuration;
    }

    public float getDistance() {
        return mDistance;
    }

    public float getAvgPace() {
        return mAvgPace;
    }

    public float getAvgSpeed() {
        return mAvgSpeed;
    }

    public int getCalorie() {
        return mCalorie;
    }

    public float getClimb() {
        return mClimb;
    }

    public int getHeartRate() {
        return mHeartRate;
    }

    public String getComment() {
        return mComment;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setInputType(int mInputType) {
        this.mInputType = mInputType;
    }

    public void setActivityType(String mActivityType) {
        this.mActivityType = mActivityType;
    }

    public void setDateTime(String mDateTime) {
        this.mDateTime = mDateTime;
    }

    public void setDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public void setDistance(float mDistance) {
        this.mDistance = mDistance;
    }

    public void setAvgPace(float mAvgPace) {
        this.mAvgPace = mAvgPace;
    }

    public void setAvgSpeed(float mAvgSpeed) {
        this.mAvgSpeed = mAvgSpeed;
    }

    public void setCalorie(int mCalorie) {
        this.mCalorie = mCalorie;
    }

    public void setClimb(float mClimb) {
        this.mClimb = mClimb;
    }

    public void setHeartRate(int mHeartRate) {
        this.mHeartRate = mHeartRate;
    }

    public void setComment(String mComment) {
        this.mComment = mComment;
    }

    @Override
    public String toString() {
        return "id: " + String.valueOf(this.id) +
                "date: " + String.valueOf(this.getDateTime()) +
                "duration: " + String.valueOf(this.getDuration()) +
                "distance: " + String.valueOf(this.getDistance()) +
                "avg pace: " + String.valueOf(this.getAvgPace()) +
                "avg speed: " + String.valueOf(this.getAvgSpeed()) +
                "calorie: " + String.valueOf(this.getCalorie()) +
                "climb: " + String.valueOf(this.getClimb()) +
                "heart: " + String.valueOf(this.getHeartRate()) +
                "comment: " + this.getComment();
    }

    public String getGpsData() {
        return gpsData;
    }

    public void setGpsData(String gpsData) {
        this.gpsData = gpsData;
    }
}