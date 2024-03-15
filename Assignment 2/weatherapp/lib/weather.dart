import 'package:location/location.dart';

class WeatherData {
  WeatherData(LocationData locationData, List<dynamic> decodedCity,
      Map<String, dynamic> decodedWeather)
      : lat = locationData.latitude!,
        long = locationData.longitude!,
        cityName = decodedCity[0]['name'],
        countryCode = decodedCity[0]['country'],
        currentWeather = CurrentWeather(decodedWeather['current']),
        hourlyWeather = [],
        dailyWeather = [] {
    for (int i = 1; i < 25; i++) {
      hourlyWeather.add(HourlyWeather(decodedWeather['hourly'][i]));
    }
    for (int i = 0; i < 8; i++) {
      dailyWeather.add(DailyWeather(decodedWeather['daily'][i]));
    }
  }

  WeatherData.fromJson(Map<String, dynamic> decodedWeather)
      : lat = decodedWeather['lat'],
        long = decodedWeather['long'],
        cityName = decodedWeather['cityName'],
        countryCode = decodedWeather['countryCode'],
        currentWeather =
            CurrentWeather.fromJson(decodedWeather['currentWeather']),
        hourlyWeather = [],
        dailyWeather = [] {
    List houW = decodedWeather['hourlyWeather'] as List;
    List daiW = decodedWeather['dailyWeather'] as List;

    for (int j = 0; j < houW.length; j++) {
      Map<String, dynamic> h = houW[j] as Map<String, dynamic>;
      hourlyWeather.add(HourlyWeather.fromJson(h));
    }
    for (int j = 0; j < daiW.length; j++) {
      Map<String, dynamic> d = daiW[j] as Map<String, dynamic>;
      dailyWeather.add(DailyWeather.fromJson(d));
    }
  }

  Map toJson() {
    Map curW = currentWeather.toJson();
    List<Map> houW = hourlyWeather.map((e) => e.toJson()).toList();
    List<Map> daiW = dailyWeather.map((e) => e.toJson()).toList();

    return {
      "lat": lat,
      "long": long,
      "cityName": cityName,
      "countryCode": countryCode,
      "currentWeather": curW,
      "hourlyWeather": houW,
      "dailyWeather": daiW
    };
  }

  double lat;
  double long;

  String cityName;
  String countryCode;

  CurrentWeather currentWeather;
  List<HourlyWeather> hourlyWeather;
  List<DailyWeather> dailyWeather;
}

class CurrentWeather {
  CurrentWeather(Map<String, dynamic> decodedWeather)
      : dateTime =
            DateTime.fromMillisecondsSinceEpoch(decodedWeather['dt'] * 1000),
        temperature = int.parse((decodedWeather['temp']).truncate().toString()),
        feelsLike = int.parse((decodedWeather['feels_like']).truncate().toString()),
        description = decodedWeather['weather'][0]['description'],
        iconUri =
            Uri.parse("assets/img/${decodedWeather['weather'][0]['icon']}.png")
                .toString() {
    description =
        description.replaceFirst(description[0], description[0].toUpperCase());
  }

  CurrentWeather.fromJson(Map<String, dynamic> decodedWeather)
      : dateTime =
            DateTime.fromMillisecondsSinceEpoch(decodedWeather['dateTime']),
        temperature = decodedWeather['temperature'],
        feelsLike = decodedWeather['feelsLike'],
        description = decodedWeather['description'],
        iconUri = decodedWeather['iconUri'];

  Map toJson() => {
        "dateTime": dateTime.millisecondsSinceEpoch,
        "temperature": temperature,
        "feelsLike": feelsLike,
        "description": description,
        "iconUri": iconUri
      };

  DateTime dateTime;
  int temperature;
  int feelsLike;
  String description;
  String iconUri;
}

class HourlyWeather {
  HourlyWeather(Map<String, dynamic> decodedWeather)
      : dateTime =
            DateTime.fromMillisecondsSinceEpoch(decodedWeather['dt'] * 1000),
        temperature = int.parse((decodedWeather['temp']).truncate().toString()),
        iconUri =
            Uri.parse("assets/img/${decodedWeather['weather'][0]['icon']}.png")
                .toString();

  HourlyWeather.fromJson(Map<String, dynamic> decodedWeather)
      : dateTime =
            DateTime.fromMillisecondsSinceEpoch(decodedWeather['dateTime']),
        temperature = decodedWeather['temperature'],
        iconUri = decodedWeather['iconUri'];

  Map toJson() => {
        "dateTime": dateTime.millisecondsSinceEpoch,
        "temperature": temperature,
        "iconUri": iconUri
      };

  DateTime dateTime;
  int temperature;
  String iconUri;
}

class DailyWeather {
  DailyWeather(Map<String, dynamic> decodedWeather)
      : sunrise = DateTime.fromMillisecondsSinceEpoch(
            decodedWeather['sunrise'] * 1000),
        sunset = DateTime.fromMillisecondsSinceEpoch(
            decodedWeather['sunset'] * 1000),
        max = int.parse((decodedWeather['temp']['max']).truncate().toString()),
        min = int.parse((decodedWeather['temp']['min']).truncate().toString()),
        pressure = decodedWeather['pressure'],
        humidity = decodedWeather['humidity'],
        uvi = decodedWeather['uvi'],
        windSpeed = decodedWeather['wind_speed'],
        description = decodedWeather['weather'][0]['description'],
        iconUri =
            Uri.parse("assets/img/${decodedWeather['weather'][0]['icon']}.png")
                .toString() {
    description =
        description.replaceFirst(description[0], description[0].toUpperCase());
  }

  DailyWeather.fromJson(Map<String, dynamic> decodedWeather)
      : sunrise =
            DateTime.fromMillisecondsSinceEpoch(decodedWeather['sunrise']),
        sunset = DateTime.fromMillisecondsSinceEpoch(decodedWeather['sunset']),
        max = decodedWeather['max'],
        min = decodedWeather['min'],
        pressure = decodedWeather['pressure'],
        humidity = decodedWeather['humidity'],
        uvi = decodedWeather['uvi'],
        windSpeed = decodedWeather['wind_speed'],
        description = decodedWeather['description'],
        iconUri = decodedWeather['iconUri'];

  Map toJson() => {
        "sunrise": sunrise.millisecondsSinceEpoch,
        "sunset": sunset.millisecondsSinceEpoch,
        "max": max,
        "min": min,
        "pressure": pressure,
        "humidity": humidity,
        "uvi": uvi,
        "windSpeed": windSpeed,
        "description": description,
        "iconUri": iconUri
      };

  DateTime sunrise;
  DateTime sunset;
  int max;
  int min;
  dynamic pressure;
  dynamic humidity;
  dynamic uvi;
  dynamic windSpeed;
  String description;
  String iconUri;
}
