package com.aoinc.nearbyplaces2.network

import android.graphics.Bitmap
import com.aoinc.nearbyplaces2.model.Geocode
import com.aoinc.nearbyplaces2.model.NearbyPlaces
import com.aoinc.nearbyplaces2.model.PlaceDetails
import com.aoinc.nearbyplaces2.util.NetworkConstants
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap

object GooglePlacesRetrofit {
    private var googlePlacesAPI: GooglePlacesAPI

    init {
        googlePlacesAPI = createGooglePlacesAPI(createRetrofit())
    }

    private fun createRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private fun createGooglePlacesAPI(retrofit: Retrofit): GooglePlacesAPI =
        retrofit.create(GooglePlacesAPI::class.java)

    // TODO: function for each query here
    fun searchNearbyPlaces(queryMap: Map<String, String>) = googlePlacesAPI.searchNearbyPlaces(queryMap)
    fun getGeocodeData(queryMap: Map<String, String>) = googlePlacesAPI.getGeocodeData(queryMap)
    fun getfirstPhoto(queryMap: Map<String, String>) = googlePlacesAPI.getFirstPhoto(queryMap)
    fun getPlaceDetails(queryMap: Map<String, String>) = googlePlacesAPI.getPlaceDetails(queryMap)
}

// API requests interface
interface GooglePlacesAPI {

    // key, location, radius, type, keyword
    @GET(NetworkConstants.NEARBY_PLACES_REQUEST_PATH)
    fun searchNearbyPlaces(@QueryMap queryMap: Map<String, String>): Observable<NearbyPlaces>

    // key, place_id
    @GET(NetworkConstants.GEOCODE_REQUEST_PATH)
    fun getGeocodeData(@QueryMap queryMap: Map<String, String>): Observable<Geocode>

    // key, photoreference, maxwidth OR maxheight
    @GET(NetworkConstants.PLACE_PHOTO_REQUEST_PATH)
    fun getFirstPhoto(@QueryMap queryMap: Map<String, String>): Observable<Bitmap>

    // key, place_id
    @GET(NetworkConstants.PLACE_DETAILS_REQUEST_PATH)
    fun getPlaceDetails(@QueryMap queryMap: Map<String, String>): Observable<PlaceDetails>
}