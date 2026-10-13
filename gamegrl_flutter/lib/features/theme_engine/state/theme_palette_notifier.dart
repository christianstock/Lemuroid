import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/gameboy_palette.dart';
import '../domain/gamegrl_theme.dart';

class ThemeState {
  final GameGrlThemeData themeData;
  final GameBoyPalette activePalette;

  const ThemeState({
    required this.themeData,
    required this.activePalette,
  });

  ThemeState copyWith({
    GameGrlThemeData? themeData,
    GameBoyPalette? activePalette,
  }) {
    return ThemeState(
      themeData: themeData ?? this.themeData,
      activePalette: activePalette ?? this.activePalette,
    );
  }
}

class ThemePaletteNotifier extends StateNotifier<ThemeState> {
  ThemePaletteNotifier()
      : super(const ThemeState(
          themeData: GameGrlThemeData.darkHandheld,
          activePalette: GameBoyPalette.dmgGreen,
        ));

  void selectPalette(GameBoyPalette palette) {
    state = state.copyWith(activePalette: palette);
  }
}

final themePaletteProvider =
    StateNotifierProvider<ThemePaletteNotifier, ThemeState>((ref) {
  return ThemePaletteNotifier();
});
