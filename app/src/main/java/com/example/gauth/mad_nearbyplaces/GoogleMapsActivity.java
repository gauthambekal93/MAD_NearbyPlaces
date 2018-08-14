package com.example.gauth.mad_nearbyplaces;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private GoogleMap mMap;
private GoogleApiClient googleApiClient;
private LocationRequest locationRequest;
private Location lastLocation;
private Marker currentUserLocationMarker;
private static final int Reqest_User_Location_Code=99;
private double latitude,longitude;
private int ProximityRadius=10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {checkUserLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //THIS METHOD IS CALLED WHENEVER THE MAP IS READY TO BE USED
        mMap = googleMap;
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
 {
            //THIS IS BASICALLY TO CHECK IF PERMISSIONS HAVE BEEN GIVEN IN MANIFESTS FILE
//Toast.makeText(this,"onMap Ready",Toast.LENGTH_SHORT).show();
buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }
public  boolean checkUserLocationPermission()
{
if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
{

    if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
    {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Reqest_User_Location_Code);
    }else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Reqest_User_Location_Code);
        }
        return false;
}
else {
 //   Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
    return  true;
}
}


//THIS METHOD HANDLES THE RESPONSE OF ON REQUEST PERMISSION RESPONSE
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case Reqest_User_Location_Code:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        if(googleApiClient==null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else {
              //      Toast.makeText(this,"Permission denied..",Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    protected  synchronized void buildGoogleApiClient()
{
 //   Toast.makeText(this,"BuildGoogleApiClient Called",Toast.LENGTH_SHORT).show();
googleApiClient=new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
googleApiClient.connect();
}


    @Override
    public void onLocationChanged(Location location) {
//THIS METHOD IS CALLED WHEN LOCATION OF DEVICE CHANGES
      //  Toast.makeText(this,"Location Changed",Toast.LENGTH_LONG).show();
       latitude=location.getLatitude();
       longitude=location.getLongitude();
        lastLocation=location;
      //  Toast.makeText(this,location.toString(),Toast.LENGTH_LONG).show();
        Log.i("Location is",location.toString());
        if(currentUserLocationMarker!=null)
        {
            currentUserLocationMarker.remove();

        }

        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("user current location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        currentUserLocationMarker=mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
   //     mMap.animateCamera(CameraUpdateFactory.zoomBy(2));//if you reduce this value then map will be zoomed out

        if(googleApiClient!=null)
{
    //The below commented line essentially makes sure that the location is updated only once when the application
    //is started.For continuous location updates we have commented the below line.

    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);

}
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
//THE METHOD IS CALLED WHEN DEVICE IS CONNECTED
        Toast.makeText(this,"Connected",Toast.LENGTH_SHORT).show();
        locationRequest=new LocationRequest();
        locationRequest.setInterval(5000);
        //locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
            mMap.animateCamera(CameraUpdateFactory.zoomBy(2));//if you reduce this value then map will be zoomed out

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//THIS METHOD IS CALLED WHEN CONNECTION IS FAILED
    }


    //SEARCH LOCATIONS
    public void searchClicked(View view)
    {
        String hospital="hospital",school="school",restaurant="restaurant";
Object transferData[]=new Object[2];
GetNearbyPlaces getNearbyPlaces=new GetNearbyPlaces();
String url="";

        Log.i("Search","Clicked");
switch (view.getId())
{
    case R.id.search:
        EditText addressField=(EditText)findViewById(R.id.location_search);
        String address=addressField.getText().toString();

        Toast.makeText(this,address,Toast.LENGTH_SHORT).show();

        List<Address> addressList=null;
        MarkerOptions userMarkerOptions=new MarkerOptions();

        if(address.length()>0)
        {
            Geocoder geocoder=new Geocoder(this);
            try {
                addressList=geocoder.getFromLocationName(address,6);
                if(addressList!=null)
                {
                    for(int i=0;i<addressList.size();i++)
                    {
                        Address userAddress=addressList.get(i);
                        LatLng latLng=new LatLng(userAddress.getLatitude(),userAddress.getLongitude());
                      userMarkerOptions.position(latLng);
                        userMarkerOptions.title(address);
                        userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
         Toast.makeText(this,"LATLNG IS "+latLng.toString(),Toast.LENGTH_SHORT).show();
                                              mMap.addMarker(userMarkerOptions);
                         mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

                    }
                }
                else
                    {
                    Toast.makeText(this,"Location not found",Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else {
        Toast.makeText(this,"Please enter a text value",Toast.LENGTH_SHORT).show();
        }
        break;

    case R.id.hospital:
        mMap.clear();
        url=getUrl(latitude,longitude,hospital);
        transferData[0]=mMap;
        transferData[1]=url;
        getNearbyPlaces.execute(transferData);
        Toast.makeText(this,"Searching for nearby Hosptals...",Toast.LENGTH_SHORT).show();
        break;

    case R.id.schools:
        mMap.clear();
        url=getUrl(latitude,longitude,school);
        transferData[0]=mMap;
        transferData[1]=url;
        getNearbyPlaces.execute(transferData);
        Toast.makeText(this,"Searching for nearby Schools...",Toast.LENGTH_SHORT).show();
        break;

    case R.id.restaraunt:
        mMap.clear();
        url=getUrl(latitude,longitude,restaurant);
        transferData[0]=mMap;
        transferData[1]=url;
        getNearbyPlaces.execute(transferData);
        Toast.makeText(this,"Searching for nearby Restaurants...",Toast.LENGTH_SHORT).show();
        break;
}
    }

    private String getUrl(double latitude,double longitude,String nearbyPlace)
    {
StringBuilder googleURL=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
googleURL.append("location="+latitude+","+longitude);
googleURL.append("&radius="+ProximityRadius);
googleURL.append("&type="+nearbyPlace);
        googleURL.append("&sensor=true");
//AIzaSyA-K4UsF3LrswnBFbVtitUUkn5IPP92H9c
        googleURL.append("&key="+"AIzaSyCbfFeHpckwhnS7jLa_IzTql0lP8hRtro4");
        Log.i("URL IS",googleURL.toString());
        return googleURL.toString();
    }


}
