import 'package:flutter_test/flutter_test.dart';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:gamegrl_flutter/shared/database/app_database.dart';

void main() {
  late AppDatabase db;

  setUp(() {
    db = AppDatabase(NativeDatabase.memory());
  });

  tearDown(() async {
    await db.close();
  });

  test('AppDatabase inserts and queries Games, DataFiles and Achievements',
      () async {
    final gameId = await db.into(db.games).insert(
          GamesCompanion.insert(
            title: 'Pokemon Emerald',
            path: '/storage/emulated/0/GBA/PokemonEmerald.gba',
            systemId: 'gba',
            coverUrl: const Value('https://covers.org/emerald_front.png'),
            backCoverUrl: const Value('https://covers.org/emerald_back.png'),
            manualUrl: const Value('/storage/emulated/0/GBA/PokemonEmerald.pdf'),
            isFavorite: const Value(true),
          ),
        );

    expect(gameId, greaterThan(0));

    final games = await db.select(db.games).get();
    expect(games.length, equals(1));
    expect(games.first.title, equals('Pokemon Emerald'));
    expect(games.first.systemId, equals('gba'));
    expect(games.first.isFavorite, isTrue);
    expect(games.first.backCoverUrl, equals('https://covers.org/emerald_back.png'));

    await db.into(db.achievements).insert(
          AchievementsCompanion.insert(
            id: const Value(101),
            gameId: gameId,
            title: 'Champion of Hoenn',
            description: 'Defeat the Elite Four and Champion',
            points: 25,
            badgeUrl: 'https://retroachievements.org/badge/101.png',
            isUnlocked: const Value(true),
          ),
        );

    final achievements = await db.select(db.achievements).get();
    expect(achievements.length, equals(1));
    expect(achievements.first.title, equals('Champion of Hoenn'));
    expect(achievements.first.points, equals(25));
  });
}
