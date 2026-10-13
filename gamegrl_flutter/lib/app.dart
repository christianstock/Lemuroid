import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'features/theme_engine/presentation/palette_swapper_demo.dart';
import 'features/theme_engine/state/theme_palette_notifier.dart';

class GameGrlApp extends ConsumerWidget {
  const GameGrlApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeState = ref.watch(themePaletteProvider);

    return MaterialApp(
      title: 'GameGrl',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: themeState.themeData.backgroundColor,
        useMaterial3: true,
      ),
      home: const PaletteSwapperDemoScreen(),
    );
  }
}
