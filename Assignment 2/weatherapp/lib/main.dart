import 'package:flutter/material.dart';

import 'package:flutter_localizations/flutter_localizations.dart';

import 'details.dart';
import 'generated/l10n.dart';
import 'home.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "JCK Weather",
      localizationsDelegates: const [
        S.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate
      ],
      supportedLocales: S.delegate.supportedLocales,
      theme: ThemeData(
        primarySwatch: Colors.lightBlue,
      ),
      initialRoute: Home.routeName,
      routes: {
        Home.routeName: (_) => const Home(),
        Details.routeName: (_) => const Details(),
      },
    );
  }
}
