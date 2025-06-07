package com.example.sage.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private static final String COLLECTION_NAME = "plants";
    private static FirebaseFirestore db = null;

    /**
     * Constructor initialises Firestore instance.
     */
    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * This method was used to upload plants to our firestore database
     *
     * @param plant
     */
    public void uploadPlant(Plant plant) {
        db.collection(COLLECTION_NAME)
                .document(String.valueOf(plant.getPlantid()))  // Use plant ID as document ID
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

    /**
     * Retrieves a Plant from Firestore by its ID.
     *
     * @param plantId   The ID of the plant to retrieve.
     * @param onSuccess Listener called when the plant is successfully retrieved.
     * @param onFailure Listener called when the retrieval fails.
     */
    public void retrievePlantById(int plantId, OnSuccessListener<Plant> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(String.valueOf(plantId))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Plant plant = documentSnapshot.toObject(Plant.class);// Convert to Plant object
                        onSuccess.onSuccess(plant);
                    } else {
                        onFailure.onFailure(new Exception("Plant with ID " + plantId + " not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Retrieves all plants from Firestore and returns them in a list.
     * @param onSuccess Listener called when the plants are successfully retrieved.
     * @param onFailure Listener called when the retrieval fails.
     */
    public static void retrieveAllPlants(OnSuccessListener<List<Plant>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Plant> plantList = new ArrayList<>();// List to store plants
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Plant plant = document.toObject(Plant.class);// Convert to Plant object
                        if (plant != null) {
                            plantList.add(plant);
                        }
                    }
                    onSuccess.onSuccess(plantList);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Increments the views count of a Plant document by 1.
     * Uses a query to find the document by plantId, then increments
     *
     * @param plantId The plant ID to increment views for.
     */
    public void incrementPlantViews(int plantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("plants")
                .whereEqualTo("plantid", plantId)
                .limit(1) // in case there's more than one with the same plantid
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        // Increment the views count
                        doc.getReference().update("views", FieldValue.increment(1));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreManager", "Failed to increment views: ", e);
                });
    }

    /**
     * Listener interface for loading all plants
     */

    public interface OnPlantDataLoadedListener {
        void onDataLoaded(List<Plant> plants);
    }


    /**
     * Retrieves all plants from Firestore and notifies the listener.
     *
     * @param listener Callback interface to receive loaded plant data.
     */
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

    /**
     * Creates a new user in Firebase Authentication and sets up
     * an empty profile document with email and empty favourites list in Firestore.
     *
     * @param email     User email.
     * @param password  User password.
     * @param onSuccess Callback on successful user creation.
     * @param onFailure Callback on failure.
     */
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

                    // We store the integer ID of each favorite plant
                    userData.put("favourites", new ArrayList<Integer>());

                    // Store user profile in Firestore under the users collection
                    db.collection("users")
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }
    /**
     * Adds a plant ID to the user's favourites array in Firestore.
     *
     * @param plantId    The plant ID to add to favourites.
     * @param email    The email of the user.
     * @param onSuccess Callback on successful update.
     * @param onFailure Callback on failure.
     */
    public void addIdToFavourite(int plantId, String email, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String userDocId = querySnapshot.getDocuments().get(0).getId();// Get the first document
                        db.collection("users")// Update the favourites array
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
    /**
     * Retrieves the list of favourite plant IDs for a user by their email.
     *
     * @param email     User email.
     * @param onSuccess Callback with list of favourite IDs, empty if none.
     * @param onFailure Callback on failure.
     */
    public void getFavouriteIdsByEmail(String email, OnSuccessListener<List<Integer>> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        // Get the first document
                        DocumentSnapshot doc = querySnapshots.getDocuments().get(0);
                        // Get the list of favourite plant IDs
                        List<Long> favRaw = (List<Long>) doc.get("favourites");
                        // Convert to integer list
                        List<Integer> result = new ArrayList<>();
                        if (favRaw != null) {
                            for (Long l : favRaw) result.add(l.intValue());
                        }
                        onSuccess.onSuccess(result); // Return the list of IDs
                    } else {
                        onSuccess.onSuccess(new ArrayList<>()); // No user found
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Deletes a plant ID from the user's favourites array in Firestore.
     *
     * @param plantId  The plant ID to remove.
     * @param email    User's email.
     * @param onSuccess Callback on successful update.
     * @param onFailure Callback on failure.
     */
    public void deleteFavourite(int plantId, String email, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String userDocId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("users")
                                .document(userDocId)
                                .update("favourites", FieldValue.arrayRemove(plantId))// Remove the plant ID
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("User with email " + email + " not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Checks if a plant ID is in the user's favourites list.
     *
     * @param plantId  Plant ID to check.
     * @param email    User email.
     * @param onSuccess Callback with true if favourite, false otherwise.
     * @param onFailure Callback on failure.
     */
    public void checkFavourite(int plantId, String email, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        List<Long> rawFavourites = (List<Long>) doc.get("favourites");// Get the list of favourite plant IDs
                        boolean isFavourite = false;// Boolean to store result
                        // Iterate through the list
                        if (rawFavourites != null) {
                            for (Long id : rawFavourites) {
                                if (id != null && id.intValue() == plantId) { // Found a match
                                    isFavourite = true;
                                    break;
                                }
                            }
                        }

                        onSuccess.onSuccess(isFavourite);
                    } else {
                        onFailure.onFailure(new Exception("User with email " + email + " not found."));
                    }
                })
                .addOnFailureListener(onFailure);
    }



}

