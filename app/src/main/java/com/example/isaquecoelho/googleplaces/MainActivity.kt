package com.example.isaquecoelho.googleplaces

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.*
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder
import java.util.*
import kotlin.Comparator

class MainActivity : AppCompatActivity() {

    private val LOG_TAG = "MainActivity"

    lateinit var placesClient: PlacesClient

    private var placesFieldList: List<Place.Field> =
        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)
    private lateinit var placesFragment: AutocompleteSupportFragment

    private var mPlaceId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPlaces()
        setupPlaceAutoComplete()

        listeningView()

        requestPermission()
    }

    private fun initPlaces() {
        Places.initialize(applicationContext, getString(R.string.places_api_key))
        placesClient = Places.createClient(this)
    }

    private fun setupPlaceAutoComplete() {
        placesFragment = getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment) as AutocompleteSupportFragment
        placesFragment.setPlaceFields(placesFieldList)
        placesFragment.setOnPlaceSelectedListener( object : PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                Toast.makeText(this@MainActivity, place.name, Toast.LENGTH_LONG).show()
                Log.e(LOG_TAG, "id: ${place.id}, name: ${place.name}; \naddress: ${place.address};")
            }

            override fun onError(status: Status) {
                Toast.makeText(this@MainActivity, status.statusMessage, Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun listeningView() {
        button.setOnClickListener { getCurrentPlace() }
        button_getphoto.setOnClickListener {
            if(TextUtils.isEmpty(mPlaceId)){
                Toast.makeText(this@MainActivity, "Places Id mus not be null: ${mPlaceId}", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                getPhotoAndDetail()
            }
        }
    }

    private fun getCurrentPlace() {
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.builder(placesFieldList).build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            val placeResponse: Task<FindCurrentPlaceResponse> = placesClient.findCurrentPlace(request)

            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful){

                    val response = task.result

                    val stringBuilder = StringBuilder()

                    for (placeLikelihood in response!!.getPlaceLikelihoods()) {

                        stringBuilder.append(placeLikelihood.place.name)
                            .append(" - Likelihood value: ")
                            .append(placeLikelihood.likelihood)
                            .append("\n")
                    }

                    mPlaceId = response.placeLikelihoods[10].place.id.toString()
                    editext_likehood.setText(stringBuilder.toString())
                }
            }

        }
    }

    private fun requestPermission() {
        Dexter.withActivity(this)
            .withPermissions(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Toast.makeText(this@MainActivity, "You must enable permissions", Toast.LENGTH_LONG).show()
                }
            }).check()
    }

    private fun getPhotoAndDetail() {
        val request: FetchPlaceRequest =
            FetchPlaceRequest.builder(mPlaceId, Arrays.asList(Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG)).build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { fetchPlaceResponse ->
                val place = fetchPlaceResponse.place
                val photoMetadata = place.photoMetadatas!![0]
                val photoRequest = FetchPhotoRequest.builder(photoMetadata).build()

                placesClient.fetchPhoto(photoRequest)
                    .addOnSuccessListener {fetchPhotoResponse ->
                        val bitmap = fetchPhotoResponse.bitmap
                        imageview_place_photo.setImageBitmap(bitmap)
                    }
            }
            .addOnFailureListener { Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show() }

        val requestDetail: FetchPlaceRequest =
                FetchPlaceRequest.builder(mPlaceId, Arrays.asList(Place.Field.LAT_LNG)).build()
        placesClient.fetchPlace(requestDetail)
            .addOnCompleteListener {fetchPlaceResponse ->
                val place = fetchPlaceResponse.result!!.place
                textview_place_detail.text = StringBuilder( place.latLng!!.latitude.toString() )
                    .append("/")
                    .append( place.latLng!!.longitude.toString() )
            }
            .addOnFailureListener { Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show() }
    }

}
