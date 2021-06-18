package mx.tecnm.tepic.ladm_u5_p1_mapas

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {
    var baseFirebase =FirebaseFirestore.getInstance()
    private  lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var  lastLocation: Location
    private lateinit var mMap: GoogleMap
    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE=1

    }
    override fun onMarkerClick(p0: Marker?)=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.uiSettings.isZoomControlsEnabled=true
        Map()
        // Add a marker in Sydney and move the camera\
    }
    private fun obtenerPuntos(){
        baseFirebase.collection("Ubicaciones").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException!=null){
                return@addSnapshotListener
            }
            for (document in querySnapshot!!){
                if(document!=null){
                    var pos=document.getGeoPoint("pos")!!.latitude
                    var pos2=document.getGeoPoint("pos")!!.longitude
                    var lugar=document.getString("nombre")!!
                    var desc=document.getString("descripcion")!!
                    Marker(LatLng(pos,pos2),lugar,desc)
                }

            }
        }
    }
    private fun Marker(location: LatLng,title:String,desc:String){
        val ubi=MarkerOptions().position(location).title(title).snippet(desc)
        ubi.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        mMap.addMarker(ubi)
    }
    private fun Map(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return }

        mMap.isMyLocationEnabled=true
        mMap.mapType= GoogleMap.MAP_TYPE_NORMAL
        fusedLocationClient.lastLocation.addOnSuccessListener(this){ location ->

            if(location!= null){
                lastLocation=location
                val currentLatLong=LatLng(location.latitude, location.longitude)
                Marker(currentLatLong,"Estas aqui","Esta es tu ubicacion actual")
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 19f))
                obtenerPuntos()
            }
        }
    }
}