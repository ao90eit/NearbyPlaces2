package com.aoinc.nearbyplaces2.viewmodel

import android.net.Network
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aoinc.nearbyplaces2.model.Geocode
import com.aoinc.nearbyplaces2.model.NearbyPlaces
import com.aoinc.nearbyplaces2.model.PlaceDetails
import com.aoinc.nearbyplaces2.network.GooglePlacesRetrofit
import com.aoinc.nearbyplaces2.util.NetworkConstants
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MapViewModel : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    val nearbyPlaceResults: MutableLiveData<NearbyPlaces> = MutableLiveData()
    val geocodeResults: MutableLiveData<Geocode> = MutableLiveData()
    val placeDetailsResults: MutableLiveData<PlaceDetails> = MutableLiveData()
    lateinit var curLocation: LatLng

//    private lateinit var nextPageToken: String
//    val placeIndexMap: HashMap<String, MutableList<Int>> = hashMapOf()

    fun requestNearbyPlaces(queryMap: Map<String, String>) {
        compositeDisposable.add(
            GooglePlacesRetrofit.searchNearbyPlaces(queryMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    if (it.status == "OK") {
//                        mapNewPlaces(it)
//                        nextPageToken = it.next_page_token ?: ""
                        nearbyPlaceResults.postValue(it)
                        // TODO: (bonus) send it.searchResults to db for offline use
                    } else {
                        Log.e("TAG_X", "nearby places request status -> ${it.status}")
                        Log.e("TAG_X", "nearby places request error -> ${it.error}")
                    }

                    compositeDisposable.clear()

                }, {    // Throwable Exception...
                    Log.e("TAG_X", "nearby places exception -> ${it.localizedMessage}")
                })
        )
    }

    // Adds any new nearby place results to the index map
//    private fun mapNewPlaces(places: NearbyPlaces) {
//        places.searchResults?.let { result ->
//            for (r in result) {
//                r.place_id?.let { id ->
//                    if (!placeIndexMap.containsKey(id))
//                        placeIndexMap.put(id, mutableListOf(result.indexOf(r)))
//                }
//            }
//        }
//    }

    fun requestGeocodeData(queryMap: Map<String, String>) {
        compositeDisposable.add(
            GooglePlacesRetrofit.getGeocodeData(queryMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    if (it.status == "OK") {
                        geocodeResults.postValue(it)
                        // TODO: (bonus) send it.searchResults to db for offline use
                    } else {
                        Log.e("TAG_X", "geocode request status -> ${it.status}")
                        Log.e("TAG_X", "geocode request error -> ${it.error}")
                    }

                    compositeDisposable.clear()

                }, {    // Throwable Exception...
                    Log.e("TAG_X", "geocode exception -> ${it.localizedMessage}")
                })
        )
    }

    fun requestPlaceDetails(queryMap: Map<String, String>) {
        compositeDisposable.add(
            GooglePlacesRetrofit.getPlaceDetails(queryMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    if (it.status == "OK") {
                        placeDetailsResults.postValue(it)
                        // TODO: (bonus) send it.searchResults to db for offline use
                    } else {
                        Log.e("TAG_X", "place details request status -> ${it.status}")
                        Log.e("TAG_X", "place details request error -> ${it.error}")
                    }

                    compositeDisposable.clear()

                }, {    // Throwable Exception...
                    Log.e("TAG_X", "place details exception -> ${it.localizedMessage}")
                })
        )
    }

    fun updateLocation(location: LatLng) {
        curLocation = location
    }

    fun getNearbyPlaces(radius: String, placeType: String = "") {

        var keywords = ""

        // TODO: update keywords based on place type for relevant queries
//        when (placeType) {
//            "" -> keywords = ""
//        }

        val queryMap = mapOf(
            NetworkConstants.KEY_KEY to NetworkConstants.KEY_VALUE,
            NetworkConstants.LOCATION_KEY to "${curLocation.latitude},${curLocation.longitude}",
            NetworkConstants.RADIUS_KEY to radius,
            NetworkConstants.TYPE_KEY to placeType,
            NetworkConstants.KEYWORD_KEY to keywords
        )

        requestNearbyPlaces(queryMap)
    }
}
