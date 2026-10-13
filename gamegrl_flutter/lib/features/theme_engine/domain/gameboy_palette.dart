import 'package:flutter/material.dart';

/// Game Boy LCD Color Palette Presets
enum GameBoyPaletteType {
  dmgGreen,
  pocketGray,
  lightBlue,
  atomicPurple,
}

class GameBoyPalette {
  final GameBoyPaletteType type;
  final String name;
  final String description;
  final Color lightest; // Shade 0 (Background)
  final Color light;    // Shade 1
  final Color dark;     // Shade 2
  final Color darkest;  // Shade 3 (Foreground)

  const GameBoyPalette({
    required this.type,
    required this.name,
    required this.description,
    required this.lightest,
    required this.light,
    required this.dark,
    required this.darkest,
  });

  static const GameBoyPalette dmgGreen = GameBoyPalette(
    type: GameBoyPaletteType.dmgGreen,
    name: 'DMG Original Green',
    description: 'Classic 1989 Original Game Boy Dot Matrix display.',
    lightest: Color(0xFF8B956D),
    light: Color(0xFF4C6B50),
    dark: Color(0xFF234B36),
    darkest: Color(0xFF0F380F),
  );

  static const GameBoyPalette pocketGray = GameBoyPalette(
    type: GameBoyPaletteType.pocketGray,
    name: 'Game Boy Pocket Gray',
    description: 'High-contrast monochrome Game Boy Pocket LCD.',
    lightest: Color(0xFFC4CFA1),
    light: Color(0xFF8B956D),
    dark: Color(0xFF4D5832),
    darkest: Color(0xFF1F240A),
  );

  static const GameBoyPalette lightBlue = GameBoyPalette(
    type: GameBoyPaletteType.lightBlue,
    name: 'Game Boy Light Blue',
    description: 'Electroluminescent backlit Game Boy Light screen.',
    lightest: Color(0xFF00E3D1),
    light: Color(0xFF00A2A5),
    dark: Color(0xFF00616D),
    darkest: Color(0xFF002A36),
  );

  static const GameBoyPalette atomicPurple = GameBoyPalette(
    type: GameBoyPaletteType.atomicPurple,
    name: 'Atomic Purple Neon',
    description: 'Translucent Atomic Purple handheld edition styling.',
    lightest: Color(0xFFE2C4FF),
    light: Color(0xFFA862EA),
    dark: Color(0xFF6B22A8),
    darkest: Color(0xFF2E0054),
  );

  static List<GameBoyPalette> get values => [
        dmgGreen,
        pocketGray,
        lightBlue,
        atomicPurple,
      ];
}
