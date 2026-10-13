import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../features/theme_engine/presentation/palette_swapper_demo.dart';

class GameGrlRoutePaths {
  static const String home = '/';
  static const String carousel = '/carousel';
  static const String games = '/games';
  static const String settings = '/settings';
  static const String retroAchievements = '/retro_achievements';
  static const String manualViewer = '/manual_viewer';
  static const String emulationSession = '/emulation_session';
}

final GoRouter gameRouter = GoRouter(
  initialLocation: GameGrlRoutePaths.home,
  routes: [
    GoRoute(
      path: GameGrlRoutePaths.home,
      builder: (context, state) => const PaletteSwapperDemoScreen(),
    ),
    GoRoute(
      path: GameGrlRoutePaths.carousel,
      builder: (context, state) => const PaletteSwapperDemoScreen(),
    ),
    GoRoute(
      path: GameGrlRoutePaths.settings,
      builder: (context, state) => const Scaffold(
        body: Center(child: Text('Settings Screen Placeholder')),
      ),
    ),
    GoRoute(
      path: GameGrlRoutePaths.retroAchievements,
      builder: (context, state) => const Scaffold(
        body: Center(child: Text('RetroAchievements Placeholder')),
      ),
    ),
    GoRoute(
      path: GameGrlRoutePaths.manualViewer,
      builder: (context, state) => const Scaffold(
        body: Center(child: Text('PDF Manual Viewer Placeholder')),
      ),
    ),
    GoRoute(
      path: GameGrlRoutePaths.emulationSession,
      builder: (context, state) => const Scaffold(
        body: Center(child: Text('Emulation Session Placeholder')),
      ),
    ),
  ],
);
