import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

part 'app_database.g.dart';

// Table: games (Mirrors Room 'Game' Entity with GameGrl additions)
class Games extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get title => text()();
  TextColumn get path => text()();
  TextColumn get systemId => text()();
  TextColumn get coverUrl => text().nullable()();
  TextColumn get backCoverUrl => text().nullable()();
  TextColumn get manualUrl => text().nullable()();
  BoolColumn get isFavorite => boolean().withDefault(const Constant(false))();
  IntColumn get lastPlayedTimestamp => integer().nullable()();
}

// Table: data_files (Mirrors Room 'DataFile' Entity)
class DataFiles extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get gameId => integer().references(Games, #id)();
  TextColumn get uri => text()();
  TextColumn get checksum => text()();
}

// Table: game_cheats (Mirrors Room 'GameCheatEntity')
class GameCheats extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get gameId => integer().references(Games, #id)();
  IntColumn get cheatIndex => integer()();
  TextColumn get description => text()();
  TextColumn get code => text()();
  BoolColumn get enabled => boolean().withDefault(const Constant(false))();
}

// Table: achievements (RetroAchievements Local Cache)
class Achievements extends Table {
  IntColumn get id => integer()();
  IntColumn get gameId => integer().references(Games, #id)();
  TextColumn get title => text()();
  TextColumn get description => text()();
  IntColumn get points => integer()();
  TextColumn get badgeUrl => text()();
  BoolColumn get isUnlocked => boolean().withDefault(const Constant(false))();
  IntColumn get unlockTimestamp => integer().nullable()();

  @override
  Set<Column> get primaryKey => {id};
}

@DriftDatabase(tables: [Games, DataFiles, GameCheats, Achievements])
class AppDatabase extends _$AppDatabase {
  AppDatabase([QueryExecutor? executor]) : super(executor ?? _openConnection());

  @override
  int get schemaVersion => 1;
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'retrograde.db'));
    return NativeDatabase.createInBackground(file);
  });
}
