// GENERATED CODE - DO NOT MODIFY BY HAND
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'intl/messages_all.dart';

// **************************************************************************
// Generator: Flutter Intl IDE plugin
// Made by Localizely
// **************************************************************************

// ignore_for_file: non_constant_identifier_names, lines_longer_than_80_chars
// ignore_for_file: join_return_with_assignment, prefer_final_in_for_each
// ignore_for_file: avoid_redundant_argument_values, avoid_escaping_inner_quotes

class S {
  S();

  static S? _current;

  static S get current {
    assert(_current != null,
        'No instance of S was loaded. Try to initialize the S delegate before accessing S.current.');
    return _current!;
  }

  static const AppLocalizationDelegate delegate = AppLocalizationDelegate();

  static Future<S> load(Locale locale) {
    final name = (locale.countryCode?.isEmpty ?? false)
        ? locale.languageCode
        : locale.toString();
    final localeName = Intl.canonicalizedLocale(name);
    return initializeMessages(localeName).then((_) {
      Intl.defaultLocale = localeName;
      final instance = S();
      S._current = instance;

      return instance;
    });
  }

  static S of(BuildContext context) {
    final instance = S.maybeOf(context);
    assert(instance != null,
        'No instance of S present in the widget tree. Did you add S.delegate in localizationsDelegates?');
    return instance!;
  }

  static S? maybeOf(BuildContext context) {
    return Localizations.of<S>(context, S);
  }

  /// `Weather`
  String get weather {
    return Intl.message(
      'Weather',
      name: 'weather',
      desc: '',
      args: [],
    );
  }

  /// `Last updated: `
  String get lastUpdated {
    return Intl.message(
      'Last updated: ',
      name: 'lastUpdated',
      desc: '',
      args: [],
    );
  }

  /// `Weather has not been updated yet.`
  String get weatherHasNotBeenUpdatedYet {
    return Intl.message(
      'Weather has not been updated yet.',
      name: 'weatherHasNotBeenUpdatedYet',
      desc: '',
      args: [],
    );
  }

  /// `Feels like `
  String get feelsLike {
    return Intl.message(
      'Feels like ',
      name: 'feelsLike',
      desc: '',
      args: [],
    );
  }

  /// `Today`
  String get today {
    return Intl.message(
      'Today',
      name: 'today',
      desc: '',
      args: [],
    );
  }

  /// `Max / Min`
  String get maxMin {
    return Intl.message(
      'Max / Min',
      name: 'maxMin',
      desc: '',
      args: [],
    );
  }

  /// `Details for: `
  String get detailsFor {
    return Intl.message(
      'Details for: ',
      name: 'detailsFor',
      desc: '',
      args: [],
    );
  }

  /// `Sunrise`
  String get sunrise {
    return Intl.message(
      'Sunrise',
      name: 'sunrise',
      desc: '',
      args: [],
    );
  }

  /// `Sunset`
  String get sunset {
    return Intl.message(
      'Sunset',
      name: 'sunset',
      desc: '',
      args: [],
    );
  }

  /// `UV index`
  String get uvIndex {
    return Intl.message(
      'UV index',
      name: 'uvIndex',
      desc: '',
      args: [],
    );
  }

  /// `Wind speed`
  String get windSpeed {
    return Intl.message(
      'Wind speed',
      name: 'windSpeed',
      desc: '',
      args: [],
    );
  }

  /// `Humidity`
  String get humidity {
    return Intl.message(
      'Humidity',
      name: 'humidity',
      desc: '',
      args: [],
    );
  }

  /// `Pressure`
  String get pressure {
    return Intl.message(
      'Pressure',
      name: 'pressure',
      desc: '',
      args: [],
    );
  }

  /// ` of 10`
  String get of10 {
    return Intl.message(
      ' of 10',
      name: 'of10',
      desc: '',
      args: [],
    );
  }

  /// ` m/s`
  String get ms {
    return Intl.message(
      ' m/s',
      name: 'ms',
      desc: '',
      args: [],
    );
  }

  /// ` %`
  String get percentage {
    return Intl.message(
      ' %',
      name: 'percentage',
      desc: '',
      args: [],
    );
  }

  /// ` hPa`
  String get hpa {
    return Intl.message(
      ' hPa',
      name: 'hpa',
      desc: '',
      args: [],
    );
  }
}

class AppLocalizationDelegate extends LocalizationsDelegate<S> {
  const AppLocalizationDelegate();

  List<Locale> get supportedLocales {
    return const <Locale>[
      Locale.fromSubtags(languageCode: 'en'),
      Locale.fromSubtags(languageCode: 'pt'),
    ];
  }

  @override
  bool isSupported(Locale locale) => _isSupported(locale);
  @override
  Future<S> load(Locale locale) => S.load(locale);
  @override
  bool shouldReload(AppLocalizationDelegate old) => false;

  bool _isSupported(Locale locale) {
    for (var supportedLocale in supportedLocales) {
      if (supportedLocale.languageCode == locale.languageCode) {
        return true;
      }
    }
    return false;
  }
}
