package com.example.sage.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void incrementPlantViews(int plantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("plants")
                .whereEqualTo("plantid", plantId)
                .limit(1) // in case there's more than one with the same plantid
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        doc.getReference().update("views", FieldValue.increment(1));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreManager", "Failed to increment views: ", e);
                });
    }


    public interface OnPlantDataLoadedListener {
        void onDataLoaded(List<Plant> plants);
    }

    public void getAllPlants(OnPlantDataLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("plants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Plant> plantList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Plant plant = document.toObject(Plant.class);
                        plantList.add(plant);
                    }
                    listener.onDataLoaded(plantList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching plants", e);
                    listener.onDataLoaded(null);
                });
    }
    public void createUserWithEmail(
            String email,
            String password,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();

                    // Create user data map
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", email);

                    // We explicitly store only the integer ID of each favorite plant
                    userData.put("favourites", new ArrayList<Integer>());

                    // Store user profile in Firestore under 'users' collection
                    db.collection("users")
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }
    public void addIdToFavourite(int plantId, String email, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String userDocId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("users")
                                .document(userDocId)
                                .update("favourites", FieldValue.arrayUnion(plantId))
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("User with email " + email + " not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }
    public void getFavouriteIdsByEmail(String email, OnSuccessListener<List<Integer>> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        DocumentSnapshot doc = querySnapshots.getDocuments().get(0);
                        List<Long> favRaw = (List<Long>) doc.get("favourites");
                        List<Integer> result = new ArrayList<>();
                        if (favRaw != null) {
                            for (Long l : favRaw) result.add(l.intValue());
                        }
                        onSuccess.onSuccess(result);
                    } else {
                        onSuccess.onSuccess(new ArrayList<>()); // No user found
                    }
                })
                .addOnFailureListener(onFailure);
    }

}

