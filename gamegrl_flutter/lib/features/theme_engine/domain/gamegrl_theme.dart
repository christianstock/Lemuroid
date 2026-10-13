import 'package:flutter/material.dart';

/// Non-Material Custom GameGrl Handheld Theme
class GameGrlThemeData {
  final Color backgroundColor;
  final Color cardBackgroundColor;
  final Color accentColor;
  final Color secondaryAccentColor;
  final Color textColor;
  final Color subtitleColor;
  final Color dpadColor;
  final Color actionButtonAColor;
  final Color actionButtonBColor;
  final BorderRadius borderRadius;

  const GameGrlThemeData({
    required this.backgroundColor,
    required this.cardBackgroundColor,
    required this.accentColor,
    required this.secondaryAccentColor,
    required this.textColor,
    required this.subtitleColor,
    required this.dpadColor,
    required this.actionButtonAColor,
    required this.actionButtonBColor,
    this.borderRadius = const BorderRadius.all(Radius.circular(16)),
  });

  static const GameGrlThemeData darkHandheld = GameGrlThemeData(
    backgroundColor: Color(0xFF12131C),
    cardBackgroundColor: Color(0xFF1E202E),
    accentColor: Color(0xFF00FFCC), // Neon Aqua Accent
    secondaryAccentColor: Color(0xFFFF2A6D), // Neon Magenta
    textColor: Color(0xFFF0F4F8),
    subtitleColor: Color(0xFF8C9BAE),
    dpadColor: Color(0xFF2C2F42),
    actionButtonAColor: Color(0xFFFF2A6D),
    actionButtonBColor: Color(0xFF00C8FF),
  );
}
