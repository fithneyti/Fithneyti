package com.example.fithneyti.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.fithneyti.R;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapFragment;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.*;

import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgressState;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;


public class HomeFragment extends Fragment implements
        OnMapReadyCallback, PermissionsListener,MapboxMap.OnMapClickListener {
    private MapView mapView;
    private  MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS;
    private HomeViewModel homeViewModel;
    public HashMap<Integer,Point> path = new HashMap<>();

    /**********/
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    /***********/

    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";

    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    private Point origin;
    private Point destination;
    private Point mylocation;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.access_token));

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);





        return root;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;


        mapboxMap.setStyle(new Style.Builder().fromUrl("mapbox://styles/med07/ck9ukr0mb068i1ipohg27nw9m"), style -> {

            enableLocationComponent(style);
            initSearchFab();
            // Create an empty GeoJSON source using the empty feature collection
            setUpSource(style);

// Set up a new symbol layer for displaying the searched location's feature coordinates
            setupLayer(style);

            addSinglePoi(mapboxMap,"1",36.857428, 10.191857);
            addSinglePoi(mapboxMap,"2",36.857832, 10.191342);
            addSinglePoi(mapboxMap,"3",36.858173, 10.191883);

            Point p1=Point.fromLngLat(10.191857,36.857428);
            Point p2=Point.fromLngLat(10.191342,  36.857832);
            Point p3=Point.fromLngLat(10.191883,  36.858173);
            path.put(1,p1);
            path.put(2,p2);
            path.put(3,p3);


            mapboxMap.addOnMapClickListener(this::onMapClick);

        });



    }

    private void addSinglePoi(final MapboxMap mapboxMap, final String id, double lat, double longt) {
        GeoJsonSource geoJsonSource = new GeoJsonSource(id, Feature.fromGeometry(
                Point.fromLngLat(longt, lat)));

        mapboxMap.getStyle().addSource(geoJsonSource);
        final SymbolLayer symbolLayer = new SymbolLayer(id + "layer", id);
        Glide.with(getContext())
                .asBitmap()
                .load(R.drawable.station)
                .override(200, 200)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // you can do something with loaded bitmap here c
                        mapboxMap.getStyle().addImage("img"+id, resource);
                        symbolLayer.withProperties(PropertyFactory.iconImage("img"+id));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                   /*     mapboxMap.getStyle().addImage("" + id, myMapUtils.textAsBitmap(entite.getLibelles().get(Locale.getDefault().getLanguage()).getLibelle(), TEXT_SIZE_POI, Color.BLUE));
                        symbolLayer.withProperties(PropertyFactory.iconImage("" + entite.getId()));
                    */}
                });

        symbolLayer.setMinZoom(17.5f);
        mapboxMap.getStyle().addLayer(symbolLayer);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

// Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();


//            Point pUser=Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),locationComponent.getLastKnownLocation().getLatitude());
//            Point p1=Point.fromLngLat(10.191857,36.857428);
////            Point p2=Point.fromLngLat(36.857832, 10.191342);
////            Point p3=Point.fromLngLat(36.858173, 10.191883);
//            path.add(pUser);

            mylocation = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),locationComponent.getLastKnownLocation().getLatitude());
//
//            origin = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),locationComponent.getLastKnownLocation().getLatitude());
//
//// Set the destination location to the Plaza del Triunfo in Granada, Spain.
//            destination = Point.fromLngLat(10.191857,36.857428);
//
//            initSource(loadedMapStyle);
//
//            initLayers(loadedMapStyle);
//
//// Get the directions route from the Mapbox Directions API
//            getRoute(mapboxMap, origin, destination);
//
//            origin = Point.fromLngLat(10.191857,36.857428);
//
//// Set the destination location to the Plaza del Triunfo in Granada, Spain.
//            destination = Point.fromLngLat( 10.191342,36.857832 );
//
//            initSource(loadedMapStyle);
//
//            initLayers(loadedMapStyle);
//
//// Get the directions route from the Mapbox Directions API
//            getRoute(mapboxMap, origin, destination);


        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }
    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(getActivity());

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    public void createRoute(Style style){

//            GeoJsonSource source = style.getSourceAs("line-source" + "routeid");
//            if (source != null) {
//                source.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(
//                        LineString.fromLngLats(path)
//                )}));
//            } else {
//
//                style.addSource(new GeoJsonSource("line-source" + "routeid",
//                        FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(
//                                LineString.fromLngLats(path)
//                        )})));
//                // The layer properties for our line. This is where we make the line dotted, set the
//                // color, etc.
//
//                style.addLayerBelow(new LineLayer("linelayer" + "routeid", "line-source" + "routeid").withProperties(
//                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
//                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
//                        PropertyFactory.lineWidth(7f),
//                        PropertyFactory.lineColor(Color.parseColor("#8A2BE2"))
//                ), "LocationComponent");
//
//            }




    }


    /**
     * Add the route and marker sources to the map
     */
    private void initSource(@NonNull Style loadedMapStyle,int id) {



        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID+id));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID+id, FeatureCollection.fromFeatures(new Feature[] {
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }
    /**
     * Add the route and marker icon layers to the map
     */
    private void initLayers(@NonNull Style loadedMapStyle,int id) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID+id, ROUTE_SOURCE_ID+id);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#FF8C00"))
        );
        loadedMapStyle.addLayer(routeLayer);





// Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID+id, ICON_SOURCE_ID+id).withProperties(
                iconImage(RED_PIN_ICON_ID+id),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[] {0f, -9f})));
    }
    /**
     * Add the route and marker icon layers to the map
     */
    private void initLayers2(@NonNull Style loadedMapStyle,int id) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID+id, ROUTE_SOURCE_ID+id);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_SQUARE),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#FFD700"))
        );
        loadedMapStyle.addLayer(routeLayer);





// Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID+id, ICON_SOURCE_ID+id).withProperties(
                iconImage(RED_PIN_ICON_ID+id),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[] {0f, -9f})));
    }
    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     * @param mapboxMap the Mapbox map object that the route will be drawn on
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination,int id) {
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
// You can get the generic HTTP info about the response
                Timber.d("Response code: " + response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }

// Get the directions route
                currentRoute = response.body().routes().get(0);

                if (mapboxMap != null) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

// Retrieve and update the source designated for showing the directions route
                            GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID+id);

// Create a LineString with the directions route's geometry and
// reset the GeoJSON source for the route LineLayer source
                            if (source != null) {
                                source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), Constants.PRECISION_6));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: " + throwable.getMessage());

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getActivity(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(getActivity(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    public void finish() {
        this.onDestroy();
    }

    public void  remove(){
        mapboxMap.getStyle().removeLayer(ROUTE_LAYER_ID + 0);
        mapboxMap.getStyle().removeSource(ROUTE_SOURCE_ID + 0);
        mapboxMap.getStyle().removeLayer(ICON_LAYER_ID + 0);

        mapboxMap.getStyle().removeSource(ICON_SOURCE_ID + 0);

        for(Map.Entry<Integer, Point> entry : path.entrySet()) {
            mapboxMap.getStyle().removeLayer(ROUTE_LAYER_ID + entry.getKey());
            mapboxMap.getStyle().removeSource(ROUTE_SOURCE_ID + entry.getKey());
            mapboxMap.getStyle().removeLayer(ICON_LAYER_ID + entry.getKey());
            mapboxMap.getStyle().removeSource(ICON_SOURCE_ID + entry.getKey());

        }
    }
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        if (mapboxMap != null) {
//            remove();

            PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
            for(Map.Entry<Integer, Point> entry : path.entrySet()) {
                if (!mapboxMap.queryRenderedFeatures(screenPoint, entry.getKey() + "layer").isEmpty()) {

//                    origin=mylocation;
//// Set the destination location to the Plaza del Triunfo in Granada, Spain.
//                    destination = Point.fromLngLat(entry.getValue().longitude(),entry.getValue().latitude());
//
//                    initSource(mapboxMap.getStyle(),0);
//
//                    initLayers2(mapboxMap.getStyle(),0);
//
//// Get the directions route from the Mapbox Directions API
//                    getRoute(mapboxMap, mylocation, destination,0);
//
//                    for(int i =entry.getKey();i<path.size();i++){
//                    origin = Point.fromLngLat(path.get(i).longitude(),path.get(i).latitude());
//
//// Set the destination location to the Plaza del Triunfo in Granada, Spain.
//                    destination = Point.fromLngLat(path.get(i+1).longitude(),path.get(i+1).latitude());
//
//                    initSource(mapboxMap.getStyle(),i);
//
//                    initLayers(mapboxMap.getStyle(),i);
//
//// Get the directions route from the Mapbox Directions API
//                    getRoute(mapboxMap, origin, destination,i);}

                }
            }




        }
        return false;
    }



    private void initSearchFab() {

        getView().findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(getActivity());
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        final SymbolLayer symbolLayer = new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        );
        Glide.with(getContext())
                .asBitmap()
                .load(R.drawable.destination)
                .override(200, 200)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // you can do something with loaded bitmap here c
                        mapboxMap.getStyle().addImage("img", resource);
                        symbolLayer.withProperties(PropertyFactory.iconImage("img"));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                   /*     mapboxMap.getStyle().addImage("" + id, myMapUtils.textAsBitmap(entite.getLibelles().get(Locale.getDefault().getLanguage()).getLibelle(), TEXT_SIZE_POI, Color.BLUE));
                        symbolLayer.withProperties(PropertyFactory.iconImage("" + entite.getId()));
                    */}
                });
        loadedMapStyle.addLayer(symbolLayer);
    }
    @SuppressLint("ResourceAsColor")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

// Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

// Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
// Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    Toast.makeText(getContext(), ""+selectedCarmenFeature.placeName(), Toast.LENGTH_LONG).show();
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(17)
                                    .build()), 4000);
                }
            }

            new MaterialDialog.Builder(getActivity())
                    .cancelable(false)
                    .title("Lancer l'iteneraire")
                    .titleGravity(GravityEnum.CENTER)
                    .buttonsGravity(GravityEnum.START)
                    .titleColor(Color.WHITE)
                    .backgroundColor(R.color.colorPrimaryDark)
                    .positiveText("FiThneyti Map")
                    .neutralText("Simple Map")
                    .negativeText("Pas Maintenant")

                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            remove();
                            origin = mylocation;
// Set the destination location to the Plaza del Triunfo in Granada, Spain.
                            destination = selectedCarmenFeature.center();

                            initSource(mapboxMap.getStyle(), 0);

                            initLayers2(mapboxMap.getStyle(), 0);

// Get the directions route from the Mapbox Directions API
                            getRoute(mapboxMap, mylocation, destination, 0);
                        }
                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                    })
                    .onPositive((dialog, which) -> {
                        remove();
           path.put(4,selectedCarmenFeature.center());
                        origin = mylocation;
// Set the destination location to the Plaza del Triunfo in Granada, Spain.
                        destination = Point.fromLngLat(path.get(1).longitude(), path.get(1).latitude());

                        initSource(mapboxMap.getStyle(), 0);

                        initLayers2(mapboxMap.getStyle(), 0);

// Get the directions route from the Mapbox Directions API
                        getRoute(mapboxMap, mylocation, destination, 0);


                                for (int i = 1; i < path.size(); i++) {
                                    origin = Point.fromLngLat(path.get(i).longitude(), path.get(i).latitude());

// Set the destination location to the Plaza del Triunfo in Granada, Spain.
                                    destination = Point.fromLngLat(path.get(i + 1).longitude(), path.get(i + 1).latitude());

                                    initSource(mapboxMap.getStyle(), i);

                                    initLayers(mapboxMap.getStyle(), i);

// Get the directions route from the Mapbox Directions API
                                    getRoute(mapboxMap, origin, destination, i);
                                }




                       })
                    .show();
        }
    }
    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<HomeFragment> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(HomeFragment activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            HomeFragment activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }
// Create a Toast which displays the new location's coordinates


// Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            HomeFragment activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity.getContext(), exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}