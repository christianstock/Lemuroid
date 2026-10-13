import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/gameboy_palette.dart';
import '../state/theme_palette_notifier.dart';

class PaletteSwapperDemoScreen extends ConsumerWidget {
  const PaletteSwapperDemoScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeState = ref.watch(themePaletteProvider);
    final theme = themeState.themeData;
    final palette = themeState.activePalette;

    return Scaffold(
      backgroundColor: theme.backgroundColor,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Custom GameGrl Handheld Header (Non-Material)
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Container(
                        width: 14,
                        height: 14,
                        decoration: BoxDecoration(
                          color: theme.accentColor,
                          shape: BoxShape.circle,
                          boxShadow: [
                            BoxShadow(
                              color: theme.accentColor.withAlpha(128),
                              blurRadius: 8,
                              spreadRadius: 2,
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(width: 10),
                      Text(
                        'GameGrl',
                        style: TextStyle(
                          fontSize: 28,
                          fontWeight: FontWeight.w900,
                          letterSpacing: 1.5,
                          color: theme.textColor,
                        ),
                      ),
                    ],
                  ),
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: theme.cardBackgroundColor,
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(
                        color: theme.accentColor.withAlpha(64),
                      ),
                    ),
                    child: Text(
                      'mGBA GB/GBC/GBA',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: theme.accentColor,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),

              // Game Boy LCD Screen Simulation Frame
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFF2B2D3E),
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: const [
                    BoxShadow(
                      color: Colors.black45,
                      blurRadius: 12,
                      offset: Offset(0, 6),
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    Row(
                      children: [
                        Container(
                          width: 8,
                          height: 8,
                          decoration: const BoxDecoration(
                            color: Colors.redAccent,
                            shape: BoxShape.circle,
                          ),
                        ),
                        const SizedBox(width: 6),
                        const Text(
                          'BATTERY',
                          style: TextStyle(
                            fontSize: 9,
                            fontWeight: FontWeight.bold,
                            color: Colors.white54,
                            letterSpacing: 1.0,
                          ),
                        ),
                        const Spacer(),
                        Text(
                          palette.name,
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: palette.lightest,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),

                    // LCD Display Matrix
                    Container(
                      height: 180,
                      width: double.infinity,
                      decoration: BoxDecoration(
                        color: palette.lightest,
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(
                          color: Colors.black.withAlpha(128),
                          width: 3,
                        ),
                      ),
                      child: Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.videogame_asset,
                              size: 56,
                              color: palette.darkest,
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'GAMEGRL LCD DISPLAY',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                                color: palette.darkest,
                                letterSpacing: 1.2,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              palette.description,
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontSize: 11,
                                color: palette.dark,
                              ),
                            ),
                            const SizedBox(height: 12),

                            // 4-Shade Palette Preview Bar
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                _buildPaletteShadeBox(palette.lightest, '0'),
                                _buildPaletteShadeBox(palette.light, '1'),
                                _buildPaletteShadeBox(palette.dark, '2'),
                                _buildPaletteShadeBox(palette.darkest, '3'),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 28),

              // Game Boy Palette Switcher Section Header
              Text(
                'Game Boy LCD Palette Presets',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: theme.subtitleColor,
                ),
              ),
              const SizedBox(height: 12),

              // Palette Selector Buttons Grid
              Expanded(
                child: ListView.builder(
                  itemCount: GameBoyPalette.values.length,
                  itemBuilder: (context, index) {
                    final item = GameBoyPalette.values[index];
                    final isSelected = item.type == palette.type;

                    return GestureDetector(
                      onTap: () {
                        ref
                            .read(themePaletteProvider.notifier)
                            .selectPalette(item);
                      },
                      child: Container(
                        margin: const EdgeInsets.only(bottom: 12),
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: isSelected
                              ? theme.cardBackgroundColor
                              : theme.cardBackgroundColor.withAlpha(128),
                          borderRadius: BorderRadius.circular(14),
                          border: Border.all(
                            color: isSelected
                                ? theme.accentColor
                                : Colors.transparent,
                            width: 2,
                          ),
                        ),
                        child: Row(
                          children: [
                            Container(
                              width: 36,
                              height: 36,
                              decoration: BoxDecoration(
                                color: item.lightest,
                                borderRadius: BorderRadius.circular(8),
                                border: Border.all(color: item.darkest, width: 2),
                              ),
                              child: Center(
                                child: Container(
                                  width: 16,
                                  height: 16,
                                  decoration: BoxDecoration(
                                    color: item.darkest,
                                    shape: BoxShape.circle,
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(width: 14),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    item.name,
                                    style: TextStyle(
                                      fontSize: 15,
                                      fontWeight: FontWeight.bold,
                                      color: isSelected
                                          ? theme.accentColor
                                          : theme.textColor,
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    item.description,
                                    style: TextStyle(
                                      fontSize: 12,
                                      color: theme.subtitleColor,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            if (isSelected)
                              Icon(
                                Icons.check_circle_rounded,
                                color: theme.accentColor,
                              ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPaletteShadeBox(Color color, String shadeLabel) {
    return Container(
      width: 28,
      height: 20,
      margin: const EdgeInsets.symmetric(horizontal: 3),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(4),
        border: Border.all(color: Colors.black26),
      ),
      child: Center(
        child: Text(
          shadeLabel,
          style: TextStyle(
            fontSize: 9,
            fontWeight: FontWeight.bold,
            color: color.computeLuminance() > 0.5 ? Colors.black : Colors.white,
          ),
        ),
      ),
    );
  }
}
