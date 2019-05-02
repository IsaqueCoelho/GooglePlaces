package com.example.isaquecoelho.googleplaces

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
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

    private lateinit var placesClient: PlacesClient
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
        Places.initialize(this, getString(R.string.places_api_key))
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
    }

    private fun getCurrentPlace() {
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.builder(placesFieldList).build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

        }

        val placeResponseTask: Task<FindCurrentPlaceResponse> = placesClient.findCurrentPlace(request)

        placeResponseTask
            .addOnCompleteListener {

                if( it.isSuccessful ){

                    val response = it.result

                    if (response != null) {
                        response.placeLikelihoods.sortWith(object : Comparator<PlaceLikelihood>{
                            override fun compare(placeLikelihood: PlaceLikelihood?, placeLikelihood2: PlaceLikelihood?): Int {
                                return placeLikelihood!!.likelihood.compareTo(placeLikelihood2!!.likelihood)
                            }
                        })

                        response.placeLikelihoods.reverse()

                        mPlaceId = response.placeLikelihoods[0].place.id.toString()

                        editext_address.text = StringBuilder(response.placeLikelihoods[0].place.address)
                    }

                }

            }
            .addOnFailureListener { Toast.makeText(this, it.message, Toast.LENGTH_LONG).show() }
    }

    private fun requestPermission() {
        Dexter.withActivity(this)
            .withPermissions(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Toast.makeText(this@MainActivity, "You must enable permissions", Toast.LENGTH_LONG).show()
                }
            }).check()
    }


}
