package com.aoinc.nearbyplaces2.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.aoinc.nearbyplaces2.R
import com.aoinc.nearbyplaces2.viewmodel.MapViewModel
import com.google.android.gms.maps.model.LatLng

class PlaceDetailFragment : Fragment() {

    // View Model
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var nameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var worshipIconImageView: ImageView
    private lateinit var mapRouteImageView: ImageView

    private lateinit var photoRecyclerView: RecyclerView

    private lateinit var geoLocation: LatLng

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.place_detail_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTextView = view.findViewById(R.id.place_name)
        addressTextView = view.findViewById(R.id.place_address)
        worshipIconImageView = view.findViewById(R.id.place_worship_icon)
        mapRouteImageView = view.findViewById(R.id.place_route_button)

        photoRecyclerView = view.findViewById(R.id.photo_recyclerView)

        mapViewModel.geocodeResults.observe(viewLifecycleOwner, {
            it.searchResults?.get(0)?.let { result ->
                // set address in view
                result.formatted_address?.let { address ->
                    addressTextView.text = address
                }

                // set directions uri string
                result.geometry?.location?.let { loc ->
                    var lat = 0.0
                    var lng = 0.0
                    loc.lat?.let { lat = it }
                    loc.lng?.let { lng = it }
                    geoLocation = LatLng(lat, lng)
                }
            }

            // set title in view
            nameTextView.text = mapViewModel.selectedPlaceName

            // set icon in view
            worshipIconImageView.setImageResource(mapViewModel.selectedPlaceIconId)

            // TODO: update photos recycler list

            mapViewModel.updateDetailsEnabled(true)
        })

        mapRouteImageView.setOnClickListener{
            if (this::geoLocation.isInitialized) {
                val intentUri = Uri.parse("google.navigation:q=" +
                        geoLocation.latitude.toString() + "," + geoLocation.longitude.toString())

                val mapIntent = Intent(Intent.ACTION_VIEW, intentUri).also {
                    it.setPackage("com.google.android.apps.maps")
                }

                context?.packageManager?.let {
                    mapIntent.resolveActivity(it).also {
                        startActivity(mapIntent)
                    }
                }
            }
        }
    }
}