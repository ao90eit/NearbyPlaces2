package com.aoinc.nearbyplaces2.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aoinc.nearbyplaces2.R
import com.aoinc.nearbyplaces2.model.Geocode
import com.aoinc.nearbyplaces2.model.NearbyPlaces
import com.aoinc.nearbyplaces2.model.PlaceDetails
import com.aoinc.nearbyplaces2.network.GooglePlacesRetrofit
import com.aoinc.nearbyplaces2.util.NetworkConstants
import com.aoinc.nearbyplaces2.util.SearchConstants
import com.aoinc.nearbyplaces2.util.WorshipType
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

    val shouldDisplayDetails: MutableLiveData<Boolean> = MutableLiveData(false)
    var selectedPlaceName: String = ""
    var selectedPlaceIconId: Int = R.drawable.worship_general_71

//    private lateinit var nextPageToken: String
//    val placeIndexMap: HashMap<String, MutableList<Int>> = hashMapOf()

    fun requestNearbyPlaces(queryMap: Map<String, String>) {
//        Log.d("TAG_X", "requesting nearby places")
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

    fun requestGeocodeData(queryMap: Map<String, String>) {
//        Log.d("TAG_X", "requesting geocode data")
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
//        Log.d("TAG_X", "requesting place details")
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
//        Log.d("TAG_X", "radius: $radius, place type: $placeType")

        var type = placeType
        var keywords = ""

        when (placeType) {
            WorshipType.BUDDHIST_TEMPLE.toString().toLowerCase() -> {
                type = ""
                keywords = SearchConstants.BUDDHIST_SEARCH_KEYWORDS
            }
            WorshipType.JAIN_TEMPLE.toString().toLowerCase() -> {
                type = ""
                keywords = SearchConstants.JAIN_SEARCH_KEYWORDS
            }
            WorshipType.OTHERS.toString().toLowerCase() -> {
                type = ""
                keywords = SearchConstants.OTHER_SEARCH_KEYWORDS
            }
            WorshipType.ALL.toString().toLowerCase() -> {
                type = ""
                keywords = SearchConstants.ALL_SEARCH_PREFIX + "|" +
                        SearchConstants.BUDDHIST_SEARCH_KEYWORDS + "|" +
                        SearchConstants.JAIN_SEARCH_KEYWORDS + "|" +
                        SearchConstants.OTHER_SEARCH_KEYWORDS
            }
        }

        val queryMap = mapOf(
            NetworkConstants.KEY_KEY to NetworkConstants.KEY_VALUE,
            NetworkConstants.LOCATION_KEY to "${curLocation.latitude},${curLocation.longitude}",
            NetworkConstants.RADIUS_KEY to radius,
            NetworkConstants.TYPE_KEY to type,
            NetworkConstants.KEYWORD_KEY to keywords
        )

        requestNearbyPlaces(queryMap)
        Log.d("TAG_X", "$queryMap")
    }

    fun updateDetailsEnabled(enabled: Boolean) {
        shouldDisplayDetails.postValue(enabled)
    }
}
