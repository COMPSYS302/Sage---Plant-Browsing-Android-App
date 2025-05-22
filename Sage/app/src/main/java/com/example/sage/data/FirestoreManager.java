package com.example.sage.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private static final String COLLECTION_NAME = "plants";
    private static FirebaseFirestore db = null;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }
    public void uploadPlant(Plant plant) {
        db.collection(COLLECTION_NAME)
                .document(String.valueOf(plant.getPlantid()))  // 用 plantId 作为文档 ID
                .set(plant)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Plant " + plant.getPlantid() + " uploaded successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to upload plant " + plant.getPlantid(), e);
                    }
                });
    }
    public void retrievePlantById(int plantId, OnSuccessListener<Plant> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(String.valueOf(plantId))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Plant plant = documentSnapshot.toObject(Plant.class);
                        onSuccess.onSuccess(plant);
                    } else {
                        onFailure.onFailure(new Exception("Plant with ID " + plantId + " not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }
    public static void retrieveAllPlants(OnSuccessListener<List<Plant>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Plant> plantList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Plant plant = document.toObject(Plant.class);
                        if (plant != null) {
                            plantList.add(plant);
                        }
                    }
                    onSuccess.onSuccess(plantList);
                })
                .addOnFailureListener(onFailure);
    }

}

