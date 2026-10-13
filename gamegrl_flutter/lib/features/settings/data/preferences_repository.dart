import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

class PreferencesRepository {
  final SharedPreferences _prefs;

  PreferencesRepository(this._prefs);

  static const String _keySelectedPalette = 'selected_palette';
  static const String _keyFastForwardRatio = 'fast_forward_ratio';
  static const String _keyLowLatencyAudio = 'low_latency_audio';
  static const String _keyHardcoreMode = 'hardcore_mode';

  String? getSelectedPalette() => _prefs.getString(_keySelectedPalette);

  Future<bool> setSelectedPalette(String value) =>
      _prefs.setString(_keySelectedPalette, value);

  double getFastForwardRatio() =>
      _prefs.getDouble(_keyFastForwardRatio) ?? 2.0;

  Future<bool> setFastForwardRatio(double value) =>
      _prefs.setDouble(_keyFastForwardRatio, value);

  bool getLowLatencyAudio() => _prefs.getBool(_keyLowLatencyAudio) ?? true;

  Future<bool> setLowLatencyAudio(bool value) =>
      _prefs.setBool(_keyLowLatencyAudio, value);

  bool getHardcoreMode() => _prefs.getBool(_keyHardcoreMode) ?? false;

  Future<bool> setHardcoreMode(bool value) =>
      _prefs.setBool(_keyHardcoreMode, value);
}

final preferencesRepositoryProvider = Provider<PreferencesRepository>((ref) {
  throw UnimplementedError(
      'preferencesRepositoryProvider must be initialized with SharedPreferences instance');
});
