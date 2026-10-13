import 'dart:developer' as developer;

class GameGrlLogger {
  static void info(String message, {String tag = 'GameGrl'}) {
    developer.log('[INFO] $message', name: tag);
  }

  static void warning(String message, {String tag = 'GameGrl'}) {
    developer.log('[WARN] $message', name: tag);
  }

  static void error(
    String message, {
    Object? error,
    StackTrace? stackTrace,
    String tag = 'GameGrl',
  }) {
    developer.log(
      '[ERROR] $message',
      name: tag,
      error: error,
      stackTrace: stackTrace,
    );
  }
}
