import 'dart:io';

class ApiConstants {
  // Option dynamique : 10.0.2.2 pour émulateur Android, localhost pour iOS/Desktop
  static String get baseUrl {
    if (Platform.isAndroid) {
      return 'http://10.0.2.2:8080/api';
    }
    return 'http://localhost:8080/api';
  }

  static const String register = '/auth/register';
  static const String login = '/auth/login';
  static const String tasks = '/tasks';
}
