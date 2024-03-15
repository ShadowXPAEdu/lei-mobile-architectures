import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:location/location.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:weatherapp/weather.dart';

import 'details.dart';
import 'generated/l10n.dart';

class Home extends StatefulWidget {
  const Home({Key? key}) : super(key: key);

  static const String routeName = "homeScreen";

  @override
  State<Home> createState() => _HomeState();
}

class _HomeState extends State<Home> {
  Location location = Location();
  bool _serviceEnabled = false;
  PermissionStatus _permissionGranted = PermissionStatus.denied;
  LocationData? _locationData;

  WeatherData? _weatherData;
  bool _fetchingData = false;

  bool _fetchedData = false;

  @override
  initState() {
    super.initState();
    _fetchData();
  }

  Future<void> _fetchData() async {
    SharedPreferences sp = await SharedPreferences.getInstance();
    String? data = sp.getString("weather");

    if (data == null) {
      await _fetchLocation();
    } else {
      _weatherData = WeatherData.fromJson(json.decode(data));
      setState(() {});
    }
  }

  Future<void> _fetchLocation() async {
    try {
      setState(() {
        _fetchingData = true;
      });

      _serviceEnabled = await location.serviceEnabled();
      if (!_serviceEnabled) {
        _serviceEnabled = await location.requestService();
        if (!_serviceEnabled) {
          return;
        }
      }

      _permissionGranted = await location.hasPermission();
      if (_permissionGranted == PermissionStatus.denied) {
        _permissionGranted = await location.requestPermission();
        if (_permissionGranted != PermissionStatus.granted) {
          return;
        }
      }

      location.changeSettings(distanceFilter: 10000.0);

      if (!_fetchedData) {
        _fetchedData = true;
        location.onLocationChanged.listen((locationData) async {
          try {
            setState(() {
              _fetchingData = true;
            });
            _locationData = locationData;

            await _fetchWeatherData();
          } finally {
            setState(() {
              _fetchingData = false;
            });
          }
        });
      }

      // _locationData = await location.getLocation();
      //
      // await _fetchWeatherData();
    } finally {
      setState(() {
        _fetchingData = false;
      });
    }
  }

  Future<WeatherData?> _fetchWeatherData() async {
    try {
      http.Response response = await http.get(Uri.parse(
          "http://api.openweathermap.org/geo/1.0/reverse?lang=${Intl.getCurrentLocale()}&lat=${_locationData!.latitude}&lon=${_locationData!.longitude}&appid=[API_KEY]&limit=1"));

      if (response.statusCode == HttpStatus.ok) {
        final List<dynamic> decodedCityData = json.decode(response.body);
        response = await http.get(Uri.parse(
            "https://api.openweathermap.org/data/2.5/onecall?lang=${Intl.getCurrentLocale()}&lat=${_locationData!.latitude}&lon=${_locationData!.longitude}&exclude=minutely,alerts&units=metric&appid=[API_KEY]"));

        if (response.statusCode == HttpStatus.ok) {
          final Map<String, dynamic> decodedWeatherData =
              json.decode(response.body);
          _weatherData =
              WeatherData(_locationData!, decodedCityData, decodedWeatherData);

          SharedPreferences prefs = await SharedPreferences.getInstance();
          prefs.setString("weather", json.encode(_weatherData!));

          return _weatherData;
        }
      }
    } catch (ex) {
      debugPrint('Something went wrong: $ex');
    }
  }

  Widget getCurrentTempWidget() {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey,
            offset: Offset(0.0, 1.0),
            blurRadius: 6.0,
          ),
        ],
        borderRadius: BorderRadius.all(Radius.circular(22)),
      ),
      margin: const EdgeInsets.symmetric(vertical: 10, horizontal: 0),
      child: TextButton(
        onPressed: () {
          Navigator.pushNamed(context, Details.routeName,
              arguments: _weatherData!.dailyWeather[0]);
        },
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(
                      vertical: 8.0, horizontal: 24.0),
                  child: Text(
                    "${_weatherData!.cityName}, ${_weatherData!.countryCode}",
                    style: const TextStyle(
                        color: Colors.black,
                        fontSize: 20.0,
                        fontWeight: FontWeight.bold),
                  ),
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        Image.asset(
                          _weatherData!.currentWeather.iconUri,
                          scale: 2,
                        ),
                        Text(
                          "${_weatherData!.currentWeather.temperature}º",
                          style: const TextStyle(
                              color: Colors.black, fontSize: 32.0),
                        ),
                      ],
                    ),
                    Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          Text(
                            _weatherData!.currentWeather.description,
                            style: const TextStyle(color: Colors.grey),
                          ),
                          Text(
                            "${_weatherData!.dailyWeather[0].max}º / ${_weatherData!.dailyWeather[0].min}º",
                            style: const TextStyle(color: Colors.grey),
                          ),
                          Row(
                            children: [
                              Text(
                                S.of(context).feelsLike,
                                style: const TextStyle(color: Colors.grey),
                              ),
                              Text(
                                "${_weatherData!.currentWeather.feelsLike}º",
                                style: const TextStyle(color: Colors.grey),
                              ),
                            ],
                          ),
                        ],
                      ),
                    )
                  ],
                )
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget getHourlyTempWidget() {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey,
            offset: Offset(0.0, 1.0),
            blurRadius: 6.0,
          ),
        ],
        borderRadius: BorderRadius.all(Radius.circular(22)),
      ),
      margin: const EdgeInsets.symmetric(vertical: 10, horizontal: 0),
      child: Row(
        children: [
          Expanded(
            child: Container(
              height: 140,
              decoration: const BoxDecoration(
                borderRadius: BorderRadius.all(Radius.circular(22)),
              ),
              padding: const EdgeInsets.symmetric(vertical: 5, horizontal: 5),
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: _weatherData!.hourlyWeather.length,
                itemBuilder: (context, index) {
                  var hourTemp = _weatherData!.hourlyWeather[index];
                  return Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        DateFormat.Hm().format(hourTemp.dateTime),
                        style: const TextStyle(
                            fontSize: 14, fontWeight: FontWeight.bold),
                      ),
                      Image.asset(
                        hourTemp.iconUri,
                        scale: 3,
                      ),
                      Text(
                        "${hourTemp.temperature}º",
                        style: const TextStyle(
                            fontSize: 14, fontWeight: FontWeight.bold),
                      ),
                    ],
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget getDailyTempWidget() {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey,
            offset: Offset(0.0, 1.0),
            blurRadius: 6.0,
          ),
        ],
        borderRadius: BorderRadius.all(Radius.circular(22)),
      ),
      margin: const EdgeInsets.symmetric(vertical: 10, horizontal: 0),
      child: Column(
        children: [
          Container(
            decoration: const BoxDecoration(
              borderRadius: BorderRadius.all(Radius.circular(22)),
            ),
            padding: const EdgeInsets.symmetric(vertical: 5, horizontal: 5),
            child: Column(
              children: getDailyTemps(),
            ),
          ),
        ],
      ),
    );
  }

  List<Widget> getDailyTemps() {
    List<Widget> widgets = [];
    var dW = _weatherData!.dailyWeather;

    for (int i = 0; i < dW.length; i++) {
      var dayTemp = dW[i];
      widgets.add(Column(
        children: [
          TextButton(
            onPressed: () {
              Navigator.pushNamed(context, Details.routeName,
                  arguments: dayTemp);
            },
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Hero(
                      tag: "${dayTemp.hashCode}",
                      child: Image.asset(
                        dayTemp.iconUri,
                        scale: 5,
                      ),
                    ),
                    Text(
                      i == 0
                          ? S.of(context).today
                          : DateFormat.EEEE().format(dayTemp.sunrise),
                      style: const TextStyle(color: Colors.black, fontSize: 14),
                    ),
                  ],
                ),
                Text(
                  "${dayTemp.max}º / ${dayTemp.min}º",
                  style: const TextStyle(color: Colors.black, fontSize: 14),
                ),
              ],
            ),
          ),
          if (i != dW.length - 1)
            const Divider(
              color: Colors.grey,
              height: 1,
              thickness: 1,
            ),
        ],
      ));
    }

    return widgets;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        actions: [
          IconButton(onPressed: _fetchLocation, icon: const Icon(Icons.update))
        ],
        title: Text(S.of(context).weather),
      ),
      body: Container(
        height: double.infinity,
        width: double.infinity,
        decoration: const BoxDecoration(
            image: DecorationImage(
                image: AssetImage("assets/img/app_bg.png"), fit: BoxFit.cover)),
        child: Builder(
          builder: (context) {
            if (_fetchingData) {
              return const Center(child: CircularProgressIndicator());
            }
            if (_weatherData == null) {
              return Center(
                child: Text(
                  S.of(context).weatherHasNotBeenUpdatedYet,
                  style: const TextStyle(fontSize: 16.0),
                ),
              );
            }
            return SingleChildScrollView(
              child: Container(
                margin:
                    const EdgeInsets.symmetric(vertical: 10, horizontal: 22),
                child: Column(
                  children: [
                    getCurrentTempWidget(),
                    getHourlyTempWidget(),
                    getDailyTempWidget(),
                    Container(
                      margin: const EdgeInsets.symmetric(vertical: 10, horizontal: 0),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          Text(
                            S.of(context).lastUpdated,
                            style: const TextStyle(fontSize: 12.0, fontWeight: FontWeight.bold),
                          ),
                          Builder(builder: (context) {
                            if (_weatherData == null) {
                              return Text(
                                S.of(context).weatherHasNotBeenUpdatedYet,
                                style: const TextStyle(fontSize: 12.0, fontWeight: FontWeight.bold),
                              );
                            }
                            return Text(
                              "${DateFormat.yMMMMd().format(_weatherData!.currentWeather.dateTime.toLocal())} ${DateFormat.Hms().format(_weatherData!.currentWeather.dateTime.toLocal())}",
                              style: const TextStyle(fontSize: 12.0, fontWeight: FontWeight.bold),
                            );
                          })
                        ],
                      ),
                    )
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
