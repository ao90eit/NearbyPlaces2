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
import com.aoinc.nearbyplaces2.model.NearbyPlaces
import com.aoinc.nearbyplaces2.viewmodel.MapViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), LocationListener, OnMapReadyCallback {

    // View Model
    private val mapViewModel: MapViewModel by activityViewModels()

    // Location Polling
    private lateinit var locationManager: LocationManager
    private lateinit var curLocation: LatLng

    // Map
    private lateinit var gmap: GoogleMap
    private lateinit var userMarker: Marker
    private val markers: MutableList<Marker> = mutableListOf()

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

        // Observe data changes
        mapViewModel.nearbyPlaceResults.observe(viewLifecycleOwner, {
            if (this::gmap.isInitialized)
                it.searchResults?.let { results ->
                    Log.d("TAG_X", "result in")
                    placeNearbyMarkers(results)
                }
        })
    }

    @SuppressLint("MissingPermission")
    // Map will not even load without this permission, check not needed
    private fun enableLocationPolling(enabled: Boolean) {
        if (enabled)
            // TODO: make this long and wide to avoid constant updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 20f, this)
        else
            locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        Log.d("TAG_X", "current location -> LAT: ${location.latitude}, LONG: ${location.longitude}")
        curLocation = LatLng(location.latitude, location.longitude)
        mapViewModel.updateLocation(curLocation)

        if (this::gmap.isInitialized) {
            if (this::userMarker.isInitialized) userMarker.remove()
            userMarker = gmap.addMarker(MarkerOptions().position(curLocation).title("My Location"))
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 12f))
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
        gmap.setMinZoomPreference(7f)
//        gmap.uiSettings.isZoomControlsEnabled = true
        gmap.uiSettings.isCompassEnabled = true
    }

    private fun placeNearbyMarkers(placesList: List<NearbyPlaces.SearchResult>) {
        // clear first
        removeAllMarkers()

        Log.d("TAG_X", "in place markers")

        // add second
        for (place in placesList) {
            var lat = 0.0
            var lng = 0.0

            place.geometry?.location?.lat?.let { lat = it }
            place.geometry?.location?.lng?.let { lng = it }

            markers.add(gmap.addMarker(MarkerOptions()
                .position(LatLng(lat,lng))
                .title(place.name)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            ))
        }
    }

    private fun removeAllMarkers() {
        for (m in markers)
            m.remove()
        markers.clear()
    }
}