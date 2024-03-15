import 'dart:math';

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:weatherapp/weather.dart';

import 'generated/l10n.dart';

class Details extends StatefulWidget {
  const Details({Key? key}) : super(key: key);

  static const String routeName = "detailsScreen";

  @override
  _DetailsState createState() => _DetailsState();
}

class _DetailsState extends State<Details> {
  final Random rand = Random();

  late final DailyWeather _dailyWeather =
      ModalRoute.of(context)!.settings.arguments as DailyWeather;

  final int _animationDuration = 700;
  final int _animationPauseDuration = 700 + 300;

  double _opacity = 1.0;
  double _turns1 = 0.0;
  double _scale1 = 1.0;
  double _elevation1 = 0.0;
  double _turns2 = 0.0;
  double _scale2 = 1.0;
  double _elevation2 = 0.0;

  Widget getTempImage() {
    return Hero(
      tag: "${_dailyWeather.hashCode}",
      child: Image.asset(
        _dailyWeather.iconUri,
        scale: 2,
      ),
    );
  }

  Widget getMainTempWidget() {
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
            padding: const EdgeInsets.symmetric(vertical: 5, horizontal: 30),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  children: [
                    AnimatedOpacity(
                        duration: Duration(milliseconds: _animationDuration),
                        opacity: _opacity,
                        child: GestureDetector(
                          onTap: () async {
                            setState(() {
                              _opacity = 0.0;
                            });
                            await Future.delayed(Duration(
                                milliseconds: _animationPauseDuration));
                            setState(() {
                              _opacity = 1.0;
                            });
                          },
                          child: getTempImage(),
                        )),
                    Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Text(
                        _dailyWeather.description,
                        style: const TextStyle(color: Colors.grey),
                      ),
                    )
                  ],
                ),
                Column(
                  children: [
                    Text(
                      "${_dailyWeather.max}º / ${_dailyWeather.min}º",
                      style:
                          const TextStyle(color: Colors.black, fontSize: 32.0),
                    ),
                    Text(
                      S.of(context).maxMin,
                      style: const TextStyle(color: Colors.grey),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget getSecondaryTempWidget() {
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
      child: Container(
        decoration: const BoxDecoration(
          borderRadius: BorderRadius.all(Radius.circular(22)),
        ),
        padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 30),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    AnimatedPhysicalModel(
                      shape: BoxShape.rectangle,
                      shadowColor: Colors.red,
                      color: Colors.white,
                      duration: Duration(milliseconds: _animationDuration),
                      elevation: _elevation2,
                      child: GestureDetector(
                        onTap: () async {
                          setState(() {
                            _elevation2 = 100.0;
                          });
                          await Future.delayed(
                              Duration(milliseconds: _animationPauseDuration));
                          setState(() {
                            _elevation2 = 0.0;
                          });
                        },
                        child: Image.asset(
                          "assets/img/sunrise.png",
                          scale: 5,
                        ),
                      ),
                    ),
                    Text(
                      S.of(context).sunrise,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                Text(
                  DateFormat.Hm().format(_dailyWeather.sunrise),
                  style: const TextStyle(
                      fontSize: 14, fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const Divider(
              color: Colors.grey,
              thickness: 1,
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    AnimatedScale(
                      duration: Duration(milliseconds: _animationDuration),
                      scale: _scale2,
                      child: GestureDetector(
                        onTap: () async {
                          setState(() {
                            _scale2 = rand.nextDouble() % 5.0;
                          });
                          await Future.delayed(
                              Duration(milliseconds: _animationPauseDuration));
                          setState(() {
                            _scale2 = 1;
                          });
                        },
                        child: Image.asset(
                          "assets/img/sunset.png",
                          scale: 5,
                        ),
                      ),
                    ),
                    Text(
                      S.of(context).sunset,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                Text(
                  DateFormat.Hm().format(_dailyWeather.sunset),
                  style: const TextStyle(
                      fontSize: 14, fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const Divider(
              color: Colors.grey,
              thickness: 1,
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    AnimatedPhysicalModel(
                      shape: BoxShape.circle,
                      shadowColor: Colors.grey,
                      color: Colors.white,
                      duration: Duration(milliseconds: _animationDuration),
                      elevation: _elevation1,
                      child: GestureDetector(
                        onTap: () async {
                          setState(() {
                            _elevation1 = 100.0;
                          });
                          await Future.delayed(
                              Duration(milliseconds: _animationPauseDuration));
                          setState(() {
                            _elevation1 = 0.0;
                          });
                        },
                        child: Image.asset(
                          "assets/img/uvi.png",
                          scale: 5,
                        ),
                      ),
                    ),
                    Text(
                      S.of(context).uvIndex,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                Row(
                  children: [
                    Text(
                      "${_dailyWeather.uvi}",
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                    Text(
                      S.of(context).of10,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ],
            ),
            const Divider(
              color: Colors.grey,
              thickness: 1,
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    AnimatedRotation(
                      duration: Duration(milliseconds: _animationDuration),
                      turns: _turns1,
                      child: GestureDetector(
                        onTap: () {
                          setState(() {
                            _turns1 += 1.0 / rand.nextDouble();
                          });
                        },
                        child: Image.asset(
                          "assets/img/windspeed.png",
                          scale: 5,
                        ),
                      ),
                    ),
                    Text(
                      S.of(context).windSpeed,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                Row(
                  children: [
                    Text(
                      "${_dailyWeather.windSpeed}",
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                    Text(
                      S.of(context).ms,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ],
            ),
            const Divider(
              color: Colors.grey,
              thickness: 1,
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    AnimatedScale(
                      duration: Duration(milliseconds: _animationDuration),
                      scale: _scale1,
                      child: GestureDetector(
                        onTap: () async {
                          setState(() {
                            _scale1 = rand.nextDouble() % 5.0;
                          });
                          await Future.delayed(
                              Duration(milliseconds: _animationPauseDuration));
                          setState(() {
                            _scale1 = 1;
                          });
                        },
                        child: Image.asset(
                          "assets/img/humidity.png",
                          scale: 5,
                        ),
                      ),
                    ),
                    Text(
                      S.of(context).humidity,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                Row(
                  children: [
                    Text(
                      "${_dailyWeather.humidity}",
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                    Text(
                      S.of(context).percentage,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ],
            ),
            const Divider(
              color: Colors.grey,
              thickness: 1,
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    AnimatedRotation(
                      duration: Duration(milliseconds: _animationDuration),
                      turns: _turns2,
                      child: GestureDetector(
                        onTap: () {
                          setState(() {
                            _turns2 += 1.0 / rand.nextDouble();
                          });
                        },
                        child: Image.asset(
                          "assets/img/pressure.png",
                          scale: 5,
                        ),
                      ),
                    ),
                    Text(
                      S.of(context).pressure,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                Row(
                  children: [
                    Text(
                      "${_dailyWeather.pressure}",
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                    Text(
                      S.of(context).hpa,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Text(S.of(context).detailsFor),
            Text(DateFormat.MEd().format(_dailyWeather.sunrise)),
          ],
        ),
      ),
      backgroundColor: Colors.white,
      body: Container(
        height: double.infinity,
        width: double.infinity,
        decoration: const BoxDecoration(
            image: DecorationImage(
                image: AssetImage("assets/img/app_bg.png"), fit: BoxFit.cover)),
        child: SingleChildScrollView(
          child: Container(
            margin: const EdgeInsets.symmetric(vertical: 10, horizontal: 22),
            child: Column(
              children: [
                getMainTempWidget(),
                getSecondaryTempWidget(),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
