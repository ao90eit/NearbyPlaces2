package com.aoinc.nearbyplaces2.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import com.aoinc.nearbyplaces2.util.NetworkConstants
import com.aoinc.nearbyplaces2.util.SearchConstants
import com.aoinc.nearbyplaces2.util.WorshipType
import com.aoinc.nearbyplaces2.viewmodel.MapViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import java.util.*

class MapFragment : Fragment(), LocationListener, OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    // View Model
    private val mapViewModel: MapViewModel by activityViewModels()

    // Location Polling
    private lateinit var locationManager: LocationManager
    private lateinit var curLocation: LatLng

    // Map
    private lateinit var gmap: GoogleMap
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 100f, this)
        else
            locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
//        Log.d("TAG_X", "current location -> LAT: ${location.latitude}, LONG: ${location.longitude}")
        val newLocation = LatLng(location.latitude, location.longitude)
        if (this::curLocation.isInitialized)
            if (newLocation == curLocation)
                return

        curLocation = newLocation
        mapViewModel.updateLocation(curLocation)

        if (this::gmap.isInitialized) {
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 13f))
        }
    }

//    mMap.addCircle(CircleOptions()
//    .center(LatLng(currentLat, currentLong))
//    .radius(40.0)
//    .strokeColor(Color.parseColor("#168CCC"))
//    .fillColor(Color.BLUE))

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

    @SuppressLint("MissingPermission")
    private fun customizeMap() {
        gmap.setMaxZoomPreference(17f)
        gmap.setMinZoomPreference(7f)
//        gmap.uiSettings.isZoomControlsEnabled = true
        gmap.uiSettings.isCompassEnabled = true
        gmap.isMyLocationEnabled = true
        gmap.uiSettings.isMyLocationButtonEnabled = true
        gmap.setOnMarkerClickListener(this)
    }

    private fun placeNearbyMarkers(placesList: List<NearbyPlaces.SearchResult>) {
        // clear first
        removeAllMarkers()

//        Log.d("TAG_X", "in place markers")

        // add second
        for (place in placesList) {
            var lat = 0.0
            var lng = 0.0

            place.geometry?.location?.lat?.let { lat = it }
            place.geometry?.location?.lng?.let { lng = it }

            val mark = gmap.addMarker(MarkerOptions()
                .position(LatLng(lat,lng))
                .title(place.name)
            )

            var iconId: Int = R.drawable.worship_general_71
            // TODO: switch icon based on types data
            place.types?.let { type ->
                place.name?.let { name ->
                    iconId = when {
                        type.contains(WorshipType.CHURCH.toString().toLowerCase(Locale.ROOT)) -> R.drawable.christian
                        type.contains(WorshipType.MOSQUE.toString().toLowerCase(Locale.ROOT)) -> R.drawable.islam
                        type.contains(WorshipType.HINDU_TEMPLE.toString().toLowerCase(Locale.ROOT)) -> R.drawable.hindu
                        type.contains(WorshipType.SYNAGOGUE.toString().toLowerCase(Locale.ROOT)) -> R.drawable.judaism
                        type.any { s -> s in SearchConstants.BUDDHIST_LIST_KEYWORDS } ||
                                name.toLowerCase(Locale.ROOT).contains("buddh") -> R.drawable.buddhist
                        type.any { s -> s in SearchConstants.JAIN_LIST_KEYWORDS } ||
                                name.toLowerCase(Locale.ROOT).contains("jain") -> R.drawable.jain2
                        type.any { s -> s in SearchConstants.SHINTO_LIST_KEYWORDS } ||
                                name.toLowerCase(Locale.ROOT).contains("shinto") -> R.drawable.shinto
                        type.any { s -> s in SearchConstants.BAHAI_LIST_KEYWORDS } ||
                                name.toLowerCase(Locale.ROOT).contains("bahai") -> R.drawable.bahai
                        type.any { s -> s in SearchConstants.SIKH_LIST_KEYWORDS } ||
                                name.toLowerCase(Locale.ROOT).contains("sikh") -> R.drawable.sikh
                        type.any { s -> s in SearchConstants.TAO_LIST_KEYWORDS } ||
                                name.toLowerCase(Locale.ROOT).contains("tao") -> R.drawable.tao
                        type.any { s -> s in SearchConstants.PAGAN_LIST_KEYWORDS } ||
                                name.toLowerCase(Locale.ROOT).contains("pagan") -> R.drawable.pagan
                        type.any { s -> s in SearchConstants.CAO_DAI_LIST_KEYWORDS } ||
                                name.toLowerCase(Locale.ROOT).contains("cao dai") -> R.drawable.cao_dai
                        else -> R.drawable.worship_general_71
                    }
                }
            }

            mark.setIcon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(iconId, 100, 100)))
            mark.tag = place.place_id + ":" + iconId
            markers.add(mark)
        }
    }

    fun resizeBitmap(iconId: Int, width: Int, height: Int): Bitmap {
        val imageBitamp = BitmapFactory.decodeResource(resources, iconId)
        val resizedBitmap = Bitmap.createScaledBitmap(imageBitamp, width, height, false)
        return resizedBitmap
    }

    private fun removeAllMarkers() {
        for (m in markers)
            m.remove()
        markers.clear()
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
//        Log.d("TAG_X", marker?.tag.toString())

        val tagData = marker?.tag.toString().split(":")

        val queryMap = mapOf(
            NetworkConstants.KEY_KEY to NetworkConstants.KEY_VALUE,
            NetworkConstants.PLACE_ID_KEY to tagData[0]
        )

        marker?.title?.let { mapViewModel.selectedPlaceName = it }
        mapViewModel.selectedPlaceIconId = tagData[1].toInt()
        mapViewModel.requestGeocodeData(queryMap)

        // does not consume this function
        // allows default behavior to run after this
        return false
    }
}