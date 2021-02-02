package com.aoinc.nearbyplaces2.view

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aoinc.nearbyplaces2.R
import com.aoinc.nearbyplaces2.viewmodel.MapViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), LocationListener, OnMapReadyCallback {

    // View Model
    private val mapViewModel: MapViewModel by activityViewModels()

    // Location Polling
    private lateinit var locationManager: LocationManager

    // Map
    private lateinit var gmap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.map_fragment_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationManager = view.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Load map
//        val mapOptions = customizeMap()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    // Map will not even load without this permission, check not needed
    private fun enableLocationPolling(enabled: Boolean) {
        if (enabled)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 20f, this)
        else
            locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        Log.d("TAG_X", "current location -> LAT: ${location.latitude}, LONG: ${location.longitude}")

        if (this::gmap.isInitialized) {
            val curLocation = LatLng(location.latitude, location.longitude)
            gmap.addMarker(MarkerOptions().position(curLocation).title("My Location"))
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 14f))
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.let {
            gmap = it
            customizeMap()
            enableLocationPolling(true)
        }
    }

    override fun onStop() {
        super.onStop()
        enableLocationPolling(false)
    }

//    private fun customizeMap(): GoogleMapOptions =
//        GoogleMapOptions()
//            .zoomControlsEnabled(true)
//            .compassEnabled(true)
//            .mapToolbarEnabled(true)
//            .maxZoomPreference(20f)
//            .minZoomPreference(10f)

    private fun customizeMap() {
        gmap.setMaxZoomPreference(17f)
        gmap.setMinZoomPreference(12f)
        gmap.uiSettings.isZoomControlsEnabled = true
        gmap.uiSettings.isCompassEnabled = true
    }
}