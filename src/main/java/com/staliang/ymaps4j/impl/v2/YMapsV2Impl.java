package com.staliang.ymaps4j.impl.v2;

import com.staliang.ymaps4j.YMaps;
import com.staliang.ymaps4j.beans.Coordinate;
import com.staliang.ymaps4j.beans.Location;
import com.staliang.ymaps4j.beans.UserLocation;
import com.staliang.ymaps4j.beans.Route;
import com.staliang.ymaps4j.exception.YMapsException;
import com.staliang.ymaps4j.impl.v2.beans.CoordinatesOrder;
import com.staliang.ymaps4j.impl.v2.util.UserLocationConvert;
import com.staliang.ymaps4j.impl.v2.util.LocationConvert;
import com.staliang.ymaps4j.impl.v2.util.RouteConvert;
import com.staliang.ymaps4j.json.types.Geocode;
import com.staliang.ymaps4j.util.GZipHttpClient;
import com.staliang.ymaps4j.util.JsonUtil;
import com.staliang.ymaps4j.util.UrlBuilder;
import com.staliang.ymaps4j.util.UrlToken;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Alexandr_Badin on 11.08.2015.
 */
public class YMapsV2Impl implements YMaps {

    private static final Logger logger = Logger.getLogger(YMapsV2Impl.class);

    private final Locale locale;
    private final GZipHttpClient client;

    private String token;
    private UserLocation userLocation;
    private CoordinatesOrder coordinatesOrder;

    private void init() {
        try {
            UrlBuilder urlBuilder = new UrlBuilder("https://api-maps.yandex.ru/2.0-stable/")
                    .add(UrlToken.LANG, locale);
            String getResult = client.get(urlBuilder.build());

            Map<String, String> stringMap = new HashMap<>();
            String[] strings = getResult.split("project_data");
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].startsWith("[")) {
                    String[] split = strings[i].split("=");
                    stringMap.put(split[0].trim(), split[1].trim());
                }
            }

            String rawToken = stringMap.get("[\"token\"]");
            token = rawToken.substring(1, rawToken.length() - 2);

            String rawGeolocation = stringMap.get("[\"geolocation\"]");
            userLocation = UserLocationConvert.convert(JsonUtil.fromJson(rawGeolocation.substring(0, rawGeolocation.length() - 1), com.staliang.ymaps4j.json.types.Geolocation.class));

            String rawCoordinatesOrder = stringMap.get("[\"coordinatesOrder\"]");
            coordinatesOrder = CoordinatesOrder.getBySysName(rawCoordinatesOrder.substring(1, rawCoordinatesOrder.length() - 2));
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage(), e);
            throw new YMapsException(e);
        }
    }

    public YMapsV2Impl(Locale locale) {
        this.locale = locale;
        this.client = new GZipHttpClient();

        init();
    }

    public UserLocation geolocation() {
        return userLocation;
    }

    private Geocode getGeocode(String location, CoordinatesOrder order) {
        try {
            UrlBuilder urlBuilder = new UrlBuilder("https://api-maps.yandex.ru/services/search/v1/")
                    .add(UrlToken.TEXT, URLEncoder.encode(location, "UTF-8"))
                    .add(UrlToken.FORMAT, "json").add(UrlToken.RSPN, 0)
                    .add(UrlToken.LANG, locale).add(UrlToken.RESULTS, "geocode")
                    .add(UrlToken.TOKEN, token).add(UrlToken.TYPE, "geo")
                    .add(UrlToken.PROPERTIES, "addressdetails")
                    .add(UrlToken.GEOCODER_SCO, order.getSysName())
                    .add(UrlToken.ORIGIN, "jsapi2Geocoder");

            Geocode geocode = JsonUtil.fromJson(client.get(urlBuilder.build()), Geocode.class);
            if (geocode.getFeatures().isEmpty()) {
                throw new YMapsException("The location not found");
            }
            return geocode;
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage(), e);
            throw new YMapsException(e);
        }
    }

    private Geocode getGeocode(String location) {
        return getGeocode(location, coordinatesOrder);
    }

    public Coordinate geocode(String location) {
        Geocode geocode = getGeocode(location);
        List<Object> coordinates = geocode.getFeatures().get(0).getGeometry().getCoordinates();
        return new Coordinate(Double.valueOf(coordinates.get(0).toString()), Double.valueOf(coordinates.get(1).toString()));
    }

    public Location geocode(Coordinate coordinate) {
        String location = String.format("%s %s", coordinate.getLongitude(), coordinate.getLatitude());
        return LocationConvert.convert(getGeocode(location, CoordinatesOrder.LONGLAT));
    }

    public Route route(String... locations) {
        Coordinate[] coordinates = Stream.of(locations)
                .map(location -> geocode(location))
                .toArray(size -> new Coordinate[size]);
        return route(coordinates);
    }

    public Route route(Coordinate... coordinates) {
        try {
            String string = Stream.of(coordinates)
                    .map(point -> String.format("%s%%2C%s", point.getLongitude(), point.getLatitude()))
                    .collect(Collectors.joining("~"));
            UrlBuilder urlBuilder = new UrlBuilder("https://api-maps.yandex.ru/services/route/2.0/")
                    .add(UrlToken.RLL, string).add(UrlToken.LANG, locale)
                    .add(UrlToken.TOKEN, token).add(UrlToken.RESULTS, 1)
                    .add(UrlToken.RTM, "atm");
            return RouteConvert.convert(JsonUtil.fromJson(client.get(urlBuilder.build()), com.staliang.ymaps4j.json.types.Route.class));
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage(), e);
            throw new YMapsException(e);
        }
    }
}
