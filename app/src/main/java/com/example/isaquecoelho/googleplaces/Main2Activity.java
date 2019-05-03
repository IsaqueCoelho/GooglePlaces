package com.example.isaquecoelho.googleplaces;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private static final String LOG_TAG = "Main2Activity";
    PlacesClient placesClient;
    List<Place.Field> placeFieldList = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS);
    AutocompleteSupportFragment placesFragment;

    private Button mButtonCurrentPlace;
    private EditText mEditTextAddress;
    private EditText mEditTextLikehood;
    private String mPlaceId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPlaces();

        requestPermission();

        settingViews();
        listeningViews();
        setupPlaceAutocomplete();
    }

    private void settingViews() {
        mButtonCurrentPlace = findViewById(R.id.button);
        mEditTextAddress = findViewById(R.id.editext_address);
        mEditTextLikehood = findViewById(R.id.editext_likehood);
    }

    private void listeningViews() {
        mButtonCurrentPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentPlaces();
            }
        });
    }

    private void setupPlaceAutocomplete() {
        placesFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        placesFragment.setPlaceFields(placeFieldList);
        placesFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Toast.makeText(Main2Activity.this, place.getName(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Main2Activity.this, status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getCurrentPlaces() {

        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFieldList).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);

            placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if(task.isSuccessful()){

                        FindCurrentPlaceResponse response = task.getResult();

                        StringBuilder stringBuilder = new StringBuilder();

                        for (PlaceLikelihood placeLikelihood: response.getPlaceLikelihoods()) {

                            stringBuilder.append(placeLikelihood.getPlace().getName())
                                    .append(" - Likelihood value: ")
                                    .append(placeLikelihood.getLikelihood())
                                    .append("\n");
                        }

                        mEditTextLikehood.setText(stringBuilder.toString());

                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException){
                            ApiException apiException = (ApiException) exception;
                            Log.e(LOG_TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                }
            });
        } else {
            requestPermission();
        }
    }

    private void initPlaces() {
        Places.initialize(getApplicationContext(), getString(R.string.places_api_key));
        placesClient = Places.createClient(this);
    }

    private void requestPermission() {
        Dexter.withActivity(this)
                .withPermissions(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Toast.makeText(Main2Activity.this, "You must enable this permission to continue", Toast.LENGTH_LONG).show();
                    }
                }).check();
    }

}
