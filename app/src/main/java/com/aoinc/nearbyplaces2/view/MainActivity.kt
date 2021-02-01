package com.aoinc.nearbyplaces2.view

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aoinc.nearbyplaces2.R
import com.aoinc.nearbyplaces2.util.NetworkConstants
import com.aoinc.nearbyplaces2.view.custom.PermissionDeniedView
import com.aoinc.nearbyplaces2.viewmodel.MapViewModel
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    // view model
    private val mapViewModel: MapViewModel by viewModels()

    // permissions
    private val PERMISSION_REQUEST_CODE: Int = 555

    // layout views
    private lateinit var testImage: ImageView
    private lateinit var permissionDeniedOverlay: PermissionDeniedView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionDeniedOverlay = findViewById(R.id.permission_denied_view)

        /*******  TESTS  *******/
//        testImage = findViewById(R.id.test_image)
//        mapViewModel.nearbyPlaceResults.observe(this, {
//            Log.d("TAG_X", "nearby places -> ${it.searchResults?.size}")
////            it.searchResults?.get(0)?.place_id?.let { id ->
////                getGeocodeData(id)
////            }
//
////            it.searchResults?.get(0)?.photos?.get(0)?.photo_reference?.let { id ->
////                getFirstPhoto(id, "400", "400")
////            }
//        })
//        mapViewModel.geocodeResults.observe(this, {
//            Log.d("TAG_X", "geocode -> ${it.searchResults?.get(0)?.formatted_address}")
////            it.searchResults?.get(0)?.place_id?.let { id ->
////                getPlaceDetails(id)
////            }
//        })
//        mapViewModel.placeDetailsResults.observe(this, {
//            Log.d("TAG_X", "place details -> ${it.searchResult?.photos?.get(0)?.photo_reference}")
//        })
//        getNearbyPlaces("33.908951,-84.4789859", "10000", "mosque", "")
        /*******  TESTS  *******/
    }

    override fun onStart() {
        super.onStart()

        // Get location permissions and run app
        // TODO: do I need to skip this if the app is already running??
        if (hasLocationPermission())
            onLocationPermissionsGranted()
        else
            requestLocationPermissions()
    }

    private fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() =
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)

    private fun onLocationPermissionsGranted() {
        permissionDeniedOverlay.visibility = View.INVISIBLE
//        loadMainFragments()   // continue loading program
    }

    private fun onLocationPermissionsDenied() {
        permissionDeniedOverlay.message = resources.getString(R.string.permission_denied_text,
            resources.getString(R.string.location))
        permissionDeniedOverlay.visibility = View.VISIBLE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onLocationPermissionsGranted()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                    requestLocationPermissions()
                else
                    onLocationPermissionsDenied()
            }
    }








    // TEST NEARBY PLACES REQUEST
    private fun getNearbyPlaces(location: String, radius: String, placeType: String = "", keywords: String = "") {
        val queryMap = mapOf(
            NetworkConstants.KEY_KEY to NetworkConstants.KEY_VALUE,
            NetworkConstants.LOCATION_KEY to location,
            NetworkConstants.RADIUS_KEY to radius,
            NetworkConstants.TYPE_KEY to placeType,
            NetworkConstants.KEYWORD_KEY to keywords
        )

        mapViewModel.getNearbyPlaces(queryMap)
    }

    // TEST GEOCODE REQUEST
    private fun getGeocodeData(placeID: String) {
        val queryMap = mapOf(
            NetworkConstants.KEY_KEY to NetworkConstants.KEY_VALUE,
            NetworkConstants.PLACE_ID_KEY to placeID
        )

        mapViewModel.getGeocodeData(queryMap)
    }

    // TEST FIRST PHOTO REQUEST
    private fun getFirstPhoto(photoRefID: String, maxWidth: String = "", maxHeight: String = "") {
        val firstPhotoURL: String = NetworkConstants.BASE_URL + NetworkConstants.PLACE_PHOTO_REQUEST_PATH +
                String.format("?%s=%s&%s=%s&%s=%s&%s=%s",
                    NetworkConstants.KEY_KEY, NetworkConstants.KEY_VALUE,
                    NetworkConstants.PHOTO_REFERENCE_KEY, photoRefID,
                    NetworkConstants.MAX_WIDTH_KEY, maxWidth,
                    NetworkConstants.MAX_HEIGHT_KEY, maxHeight)

        Log.d("TAG_X", "first photo URL -> $firstPhotoURL")
        Glide.with(this).load(firstPhotoURL).into(testImage)
    }

    // TEST PLACE DETAILS REQUEST
    private fun getPlaceDetails(placeID: String) {
        val queryMap = mapOf(
            NetworkConstants.KEY_KEY to NetworkConstants.KEY_VALUE,
            NetworkConstants.PLACE_ID_KEY to placeID
        )

        mapViewModel.getPlaceDetails(queryMap)
    }
}