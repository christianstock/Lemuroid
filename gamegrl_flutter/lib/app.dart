import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/router/game_router.dart';
import 'features/theme_engine/state/theme_palette_notifier.dart';

class GameGrlApp extends ConsumerWidget {
  const GameGrlApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeState = ref.watch(themePaletteProvider);

    return MaterialApp.router(
      title: 'GameGrl',
      debugShowCheckedModeBanner: false,
      routerConfig: gameRouter,
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: themeState.themeData.backgroundColor,
        useMaterial3: true,
      ),
    );
  }
}
