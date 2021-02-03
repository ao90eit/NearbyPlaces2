package com.aoinc.nearbyplaces2.view

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aoinc.nearbyplaces2.R
import com.aoinc.nearbyplaces2.util.NetworkConstants
import com.aoinc.nearbyplaces2.util.WorshipType
import com.aoinc.nearbyplaces2.view.custom.PermissionDeniedView
import com.aoinc.nearbyplaces2.viewmodel.MapViewModel
import com.google.android.material.slider.Slider
import java.util.*

class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    // View Model
    private val mapViewModel: MapViewModel by viewModels()

    // Permissions
    private val PERMISSION_REQUEST_CODE: Int = 555

    // Fragments
    private val mapFragment = MapFragment()

    // View Data
    private var worshipType: WorshipType = WorshipType.ALL

    // Connected Views
    private lateinit var permissionDeniedOverlay: PermissionDeniedView
    private lateinit var detailFragmentView: View
    private lateinit var radiusSlider: Slider
    private lateinit var worshipTypeButton: ImageButton
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionDeniedOverlay = findViewById(R.id.permission_denied_view)
        detailFragmentView = findViewById(R.id.detail_fragment_container)
        radiusSlider = findViewById(R.id.search_radius_slider)
        worshipTypeButton = findViewById(R.id.place_type_menu_button)
        searchButton = findViewById(R.id.search_button)

//        radiusSlider.setLabelFormatter { value: Float ->
//            val format = NumberFormat.getNumberInstance()
//            format.maximumFractionDigits = 1
//            format.format(value/1000.0)
//        }

        //default
        worshipTypeButton.setImageResource(R.drawable.ic_infinity)

        worshipTypeButton.setOnClickListener {
            val popupMenu = PopupMenu(this, worshipTypeButton)
            popupMenu.menuInflater.inflate(R.menu.worship_selector_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.show()
        }

        searchButton.setOnClickListener {
            mapViewModel.curLocation?.let {
                mapViewModel.updateDetailsEnabled(false)
                mapViewModel.getNearbyPlaces(radiusSlider.value.toString().trim(),
                    worshipType.toString().toLowerCase(Locale.ROOT))
            }
        }

        mapViewModel.shouldDisplayDetails.observe(this, { enabled ->
            if (enabled)
                detailFragmentView.visibility = View.VISIBLE
            else
                detailFragmentView.visibility = View.GONE
        })
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

    private fun loadMapFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.main_fragment_containerView, mapFragment)
            .commit()
    }

    private fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() =
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)

    private fun onLocationPermissionsGranted() {
        permissionDeniedOverlay.visibility = View.INVISIBLE
        loadMapFragment()   // continue loading program
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

    override fun onMenuItemClick(item: MenuItem): Boolean {
        worshipType = when (item.title.toString().toUpperCase()) {
            WorshipType.CHURCH.toString() -> WorshipType.CHURCH
            WorshipType.MOSQUE.toString() -> WorshipType.MOSQUE
            WorshipType.HINDU_TEMPLE.toString()
                .replace("_", " ") -> WorshipType.HINDU_TEMPLE
            WorshipType.SYNAGOGUE.toString() -> WorshipType.SYNAGOGUE
            WorshipType.OTHERS.toString() -> WorshipType.OTHERS
            WorshipType.BUDDHIST_TEMPLE.toString()
                .replace("_", " ") -> WorshipType.BUDDHIST_TEMPLE
            WorshipType.JAIN_TEMPLE.toString()
                .replace("_", " ") -> WorshipType.JAIN_TEMPLE
            else -> WorshipType.ALL
        }

        worshipTypeButton.setImageResource(when (worshipType) {
            WorshipType.CHURCH -> R.drawable.ic_christian
            WorshipType.MOSQUE -> R.drawable.ic_islam
            WorshipType.HINDU_TEMPLE -> R.drawable.ic_hindu
            WorshipType.SYNAGOGUE -> R.drawable.ic_judaism
            WorshipType.OTHERS -> R.drawable.worship_general_71
            WorshipType.BUDDHIST_TEMPLE -> R.drawable.ic_buddhist
            WorshipType.JAIN_TEMPLE -> R.drawable.ic_jain2
            else -> R.drawable.ic_infinity
        })

        return true
    }









    // TEST PLACE DETAILS REQUEST
    private fun getPlaceDetails(placeID: String) {
        val queryMap = mapOf(
            NetworkConstants.KEY_KEY to NetworkConstants.KEY_VALUE,
            NetworkConstants.PLACE_ID_KEY to placeID
        )

        mapViewModel.requestPlaceDetails(queryMap)
    }
}