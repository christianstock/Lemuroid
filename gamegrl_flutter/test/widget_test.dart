import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:gamegrl_flutter/app.dart';

void main() {
  testWidgets('GameGrlApp launches and displays LCD Palette Presets',
      (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: GameGrlApp(),
      ),
    );

    expect(find.text('GameGrl'), findsOneWidget);
    expect(find.text('mGBA GB/GBC/GBA'), findsOneWidget);
    expect(find.text('Game Boy LCD Palette Presets'), findsOneWidget);
    expect(find.text('Game Boy Pocket Gray'), findsOneWidget);
  });
}
