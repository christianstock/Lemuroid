// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_database.dart';

// ignore_for_file: type=lint
class $GamesTable extends Games with TableInfo<$GamesTable, Game> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $GamesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    hasAutoIncrement: true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'PRIMARY KEY AUTOINCREMENT',
    ),
  );
  static const VerificationMeta _titleMeta = const VerificationMeta('title');
  @override
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
    'title',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _pathMeta = const VerificationMeta('path');
  @override
  late final GeneratedColumn<String> path = GeneratedColumn<String>(
    'path',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _systemIdMeta = const VerificationMeta(
    'systemId',
  );
  @override
  late final GeneratedColumn<String> systemId = GeneratedColumn<String>(
    'system_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _coverUrlMeta = const VerificationMeta(
    'coverUrl',
  );
  @override
  late final GeneratedColumn<String> coverUrl = GeneratedColumn<String>(
    'cover_url',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _backCoverUrlMeta = const VerificationMeta(
    'backCoverUrl',
  );
  @override
  late final GeneratedColumn<String> backCoverUrl = GeneratedColumn<String>(
    'back_cover_url',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _manualUrlMeta = const VerificationMeta(
    'manualUrl',
  );
  @override
  late final GeneratedColumn<String> manualUrl = GeneratedColumn<String>(
    'manual_url',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _isFavoriteMeta = const VerificationMeta(
    'isFavorite',
  );
  @override
  late final GeneratedColumn<bool> isFavorite = GeneratedColumn<bool>(
    'is_favorite',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("is_favorite" IN (0, 1))',
    ),
    defaultValue: const Constant(false),
  );
  static const VerificationMeta _lastPlayedTimestampMeta =
      const VerificationMeta('lastPlayedTimestamp');
  @override
  late final GeneratedColumn<int> lastPlayedTimestamp = GeneratedColumn<int>(
    'last_played_timestamp',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    title,
    path,
    systemId,
    coverUrl,
    backCoverUrl,
    manualUrl,
    isFavorite,
    lastPlayedTimestamp,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'games';
  @override
  VerificationContext validateIntegrity(
    Insertable<Game> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('path')) {
      context.handle(
        _pathMeta,
        path.isAcceptableOrUnknown(data['path']!, _pathMeta),
      );
    } else if (isInserting) {
      context.missing(_pathMeta);
    }
    if (data.containsKey('system_id')) {
      context.handle(
        _systemIdMeta,
        systemId.isAcceptableOrUnknown(data['system_id']!, _systemIdMeta),
      );
    } else if (isInserting) {
      context.missing(_systemIdMeta);
    }
    if (data.containsKey('cover_url')) {
      context.handle(
        _coverUrlMeta,
        coverUrl.isAcceptableOrUnknown(data['cover_url']!, _coverUrlMeta),
      );
    }
    if (data.containsKey('back_cover_url')) {
      context.handle(
        _backCoverUrlMeta,
        backCoverUrl.isAcceptableOrUnknown(
          data['back_cover_url']!,
          _backCoverUrlMeta,
        ),
      );
    }
    if (data.containsKey('manual_url')) {
      context.handle(
        _manualUrlMeta,
        manualUrl.isAcceptableOrUnknown(data['manual_url']!, _manualUrlMeta),
      );
    }
    if (data.containsKey('is_favorite')) {
      context.handle(
        _isFavoriteMeta,
        isFavorite.isAcceptableOrUnknown(data['is_favorite']!, _isFavoriteMeta),
      );
    }
    if (data.containsKey('last_played_timestamp')) {
      context.handle(
        _lastPlayedTimestampMeta,
        lastPlayedTimestamp.isAcceptableOrUnknown(
          data['last_played_timestamp']!,
          _lastPlayedTimestampMeta,
        ),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  Game map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Game(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      path: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}path'],
      )!,
      systemId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}system_id'],
      )!,
      coverUrl: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}cover_url'],
      ),
      backCoverUrl: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}back_cover_url'],
      ),
      manualUrl: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}manual_url'],
      ),
      isFavorite: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}is_favorite'],
      )!,
      lastPlayedTimestamp: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}last_played_timestamp'],
      ),
    );
  }

  @override
  $GamesTable createAlias(String alias) {
    return $GamesTable(attachedDatabase, alias);
  }
}

class Game extends DataClass implements Insertable<Game> {
  final int id;
  final String title;
  final String path;
  final String systemId;
  final String? coverUrl;
  final String? backCoverUrl;
  final String? manualUrl;
  final bool isFavorite;
  final int? lastPlayedTimestamp;
  const Game({
    required this.id,
    required this.title,
    required this.path,
    required this.systemId,
    this.coverUrl,
    this.backCoverUrl,
    this.manualUrl,
    required this.isFavorite,
    this.lastPlayedTimestamp,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['title'] = Variable<String>(title);
    map['path'] = Variable<String>(path);
    map['system_id'] = Variable<String>(systemId);
    if (!nullToAbsent || coverUrl != null) {
      map['cover_url'] = Variable<String>(coverUrl);
    }
    if (!nullToAbsent || backCoverUrl != null) {
      map['back_cover_url'] = Variable<String>(backCoverUrl);
    }
    if (!nullToAbsent || manualUrl != null) {
      map['manual_url'] = Variable<String>(manualUrl);
    }
    map['is_favorite'] = Variable<bool>(isFavorite);
    if (!nullToAbsent || lastPlayedTimestamp != null) {
      map['last_played_timestamp'] = Variable<int>(lastPlayedTimestamp);
    }
    return map;
  }

  GamesCompanion toCompanion(bool nullToAbsent) {
    return GamesCompanion(
      id: Value(id),
      title: Value(title),
      path: Value(path),
      systemId: Value(systemId),
      coverUrl: coverUrl == null && nullToAbsent
          ? const Value.absent()
          : Value(coverUrl),
      backCoverUrl: backCoverUrl == null && nullToAbsent
          ? const Value.absent()
          : Value(backCoverUrl),
      manualUrl: manualUrl == null && nullToAbsent
          ? const Value.absent()
          : Value(manualUrl),
      isFavorite: Value(isFavorite),
      lastPlayedTimestamp: lastPlayedTimestamp == null && nullToAbsent
          ? const Value.absent()
          : Value(lastPlayedTimestamp),
    );
  }

  factory Game.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return Game(
      id: serializer.fromJson<int>(json['id']),
      title: serializer.fromJson<String>(json['title']),
      path: serializer.fromJson<String>(json['path']),
      systemId: serializer.fromJson<String>(json['systemId']),
      coverUrl: serializer.fromJson<String?>(json['coverUrl']),
      backCoverUrl: serializer.fromJson<String?>(json['backCoverUrl']),
      manualUrl: serializer.fromJson<String?>(json['manualUrl']),
      isFavorite: serializer.fromJson<bool>(json['isFavorite']),
      lastPlayedTimestamp: serializer.fromJson<int?>(
        json['lastPlayedTimestamp'],
      ),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'title': serializer.toJson<String>(title),
      'path': serializer.toJson<String>(path),
      'systemId': serializer.toJson<String>(systemId),
      'coverUrl': serializer.toJson<String?>(coverUrl),
      'backCoverUrl': serializer.toJson<String?>(backCoverUrl),
      'manualUrl': serializer.toJson<String?>(manualUrl),
      'isFavorite': serializer.toJson<bool>(isFavorite),
      'lastPlayedTimestamp': serializer.toJson<int?>(lastPlayedTimestamp),
    };
  }

  Game copyWith({
    int? id,
    String? title,
    String? path,
    String? systemId,
    Value<String?> coverUrl = const Value.absent(),
    Value<String?> backCoverUrl = const Value.absent(),
    Value<String?> manualUrl = const Value.absent(),
    bool? isFavorite,
    Value<int?> lastPlayedTimestamp = const Value.absent(),
  }) => Game(
    id: id ?? this.id,
    title: title ?? this.title,
    path: path ?? this.path,
    systemId: systemId ?? this.systemId,
    coverUrl: coverUrl.present ? coverUrl.value : this.coverUrl,
    backCoverUrl: backCoverUrl.present ? backCoverUrl.value : this.backCoverUrl,
    manualUrl: manualUrl.present ? manualUrl.value : this.manualUrl,
    isFavorite: isFavorite ?? this.isFavorite,
    lastPlayedTimestamp: lastPlayedTimestamp.present
        ? lastPlayedTimestamp.value
        : this.lastPlayedTimestamp,
  );
  Game copyWithCompanion(GamesCompanion data) {
    return Game(
      id: data.id.present ? data.id.value : this.id,
      title: data.title.present ? data.title.value : this.title,
      path: data.path.present ? data.path.value : this.path,
      systemId: data.systemId.present ? data.systemId.value : this.systemId,
      coverUrl: data.coverUrl.present ? data.coverUrl.value : this.coverUrl,
      backCoverUrl: data.backCoverUrl.present
          ? data.backCoverUrl.value
          : this.backCoverUrl,
      manualUrl: data.manualUrl.present ? data.manualUrl.value : this.manualUrl,
      isFavorite: data.isFavorite.present
          ? data.isFavorite.value
          : this.isFavorite,
      lastPlayedTimestamp: data.lastPlayedTimestamp.present
          ? data.lastPlayedTimestamp.value
          : this.lastPlayedTimestamp,
    );
  }

  @override
  String toString() {
    return (StringBuffer('Game(')
          ..write('id: $id, ')
          ..write('title: $title, ')
          ..write('path: $path, ')
          ..write('systemId: $systemId, ')
          ..write('coverUrl: $coverUrl, ')
          ..write('backCoverUrl: $backCoverUrl, ')
          ..write('manualUrl: $manualUrl, ')
          ..write('isFavorite: $isFavorite, ')
          ..write('lastPlayedTimestamp: $lastPlayedTimestamp')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    title,
    path,
    systemId,
    coverUrl,
    backCoverUrl,
    manualUrl,
    isFavorite,
    lastPlayedTimestamp,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is Game &&
          other.id == this.id &&
          other.title == this.title &&
          other.path == this.path &&
          other.systemId == this.systemId &&
          other.coverUrl == this.coverUrl &&
          other.backCoverUrl == this.backCoverUrl &&
          other.manualUrl == this.manualUrl &&
          other.isFavorite == this.isFavorite &&
          other.lastPlayedTimestamp == this.lastPlayedTimestamp);
}

class GamesCompanion extends UpdateCompanion<Game> {
  final Value<int> id;
  final Value<String> title;
  final Value<String> path;
  final Value<String> systemId;
  final Value<String?> coverUrl;
  final Value<String?> backCoverUrl;
  final Value<String?> manualUrl;
  final Value<bool> isFavorite;
  final Value<int?> lastPlayedTimestamp;
  const GamesCompanion({
    this.id = const Value.absent(),
    this.title = const Value.absent(),
    this.path = const Value.absent(),
    this.systemId = const Value.absent(),
    this.coverUrl = const Value.absent(),
    this.backCoverUrl = const Value.absent(),
    this.manualUrl = const Value.absent(),
    this.isFavorite = const Value.absent(),
    this.lastPlayedTimestamp = const Value.absent(),
  });
  GamesCompanion.insert({
    this.id = const Value.absent(),
    required String title,
    required String path,
    required String systemId,
    this.coverUrl = const Value.absent(),
    this.backCoverUrl = const Value.absent(),
    this.manualUrl = const Value.absent(),
    this.isFavorite = const Value.absent(),
    this.lastPlayedTimestamp = const Value.absent(),
  }) : title = Value(title),
       path = Value(path),
       systemId = Value(systemId);
  static Insertable<Game> custom({
    Expression<int>? id,
    Expression<String>? title,
    Expression<String>? path,
    Expression<String>? systemId,
    Expression<String>? coverUrl,
    Expression<String>? backCoverUrl,
    Expression<String>? manualUrl,
    Expression<bool>? isFavorite,
    Expression<int>? lastPlayedTimestamp,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (title != null) 'title': title,
      if (path != null) 'path': path,
      if (systemId != null) 'system_id': systemId,
      if (coverUrl != null) 'cover_url': coverUrl,
      if (backCoverUrl != null) 'back_cover_url': backCoverUrl,
      if (manualUrl != null) 'manual_url': manualUrl,
      if (isFavorite != null) 'is_favorite': isFavorite,
      if (lastPlayedTimestamp != null)
        'last_played_timestamp': lastPlayedTimestamp,
    });
  }

  GamesCompanion copyWith({
    Value<int>? id,
    Value<String>? title,
    Value<String>? path,
    Value<String>? systemId,
    Value<String?>? coverUrl,
    Value<String?>? backCoverUrl,
    Value<String?>? manualUrl,
    Value<bool>? isFavorite,
    Value<int?>? lastPlayedTimestamp,
  }) {
    return GamesCompanion(
      id: id ?? this.id,
      title: title ?? this.title,
      path: path ?? this.path,
      systemId: systemId ?? this.systemId,
      coverUrl: coverUrl ?? this.coverUrl,
      backCoverUrl: backCoverUrl ?? this.backCoverUrl,
      manualUrl: manualUrl ?? this.manualUrl,
      isFavorite: isFavorite ?? this.isFavorite,
      lastPlayedTimestamp: lastPlayedTimestamp ?? this.lastPlayedTimestamp,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (title.present) {
      map['title'] = Variable<String>(title.value);
    }
    if (path.present) {
      map['path'] = Variable<String>(path.value);
    }
    if (systemId.present) {
      map['system_id'] = Variable<String>(systemId.value);
    }
    if (coverUrl.present) {
      map['cover_url'] = Variable<String>(coverUrl.value);
    }
    if (backCoverUrl.present) {
      map['back_cover_url'] = Variable<String>(backCoverUrl.value);
    }
    if (manualUrl.present) {
      map['manual_url'] = Variable<String>(manualUrl.value);
    }
    if (isFavorite.present) {
      map['is_favorite'] = Variable<bool>(isFavorite.value);
    }
    if (lastPlayedTimestamp.present) {
      map['last_played_timestamp'] = Variable<int>(lastPlayedTimestamp.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('GamesCompanion(')
          ..write('id: $id, ')
          ..write('title: $title, ')
          ..write('path: $path, ')
          ..write('systemId: $systemId, ')
          ..write('coverUrl: $coverUrl, ')
          ..write('backCoverUrl: $backCoverUrl, ')
          ..write('manualUrl: $manualUrl, ')
          ..write('isFavorite: $isFavorite, ')
          ..write('lastPlayedTimestamp: $lastPlayedTimestamp')
          ..write(')'))
        .toString();
  }
}

class $DataFilesTable extends DataFiles
    with TableInfo<$DataFilesTable, DataFile> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $DataFilesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    hasAutoIncrement: true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'PRIMARY KEY AUTOINCREMENT',
    ),
  );
  static const VerificationMeta _gameIdMeta = const VerificationMeta('gameId');
  @override
  late final GeneratedColumn<int> gameId = GeneratedColumn<int>(
    'game_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'REFERENCES games (id)',
    ),
  );
  static const VerificationMeta _uriMeta = const VerificationMeta('uri');
  @override
  late final GeneratedColumn<String> uri = GeneratedColumn<String>(
    'uri',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _checksumMeta = const VerificationMeta(
    'checksum',
  );
  @override
  late final GeneratedColumn<String> checksum = GeneratedColumn<String>(
    'checksum',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [id, gameId, uri, checksum];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'data_files';
  @override
  VerificationContext validateIntegrity(
    Insertable<DataFile> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('game_id')) {
      context.handle(
        _gameIdMeta,
        gameId.isAcceptableOrUnknown(data['game_id']!, _gameIdMeta),
      );
    } else if (isInserting) {
      context.missing(_gameIdMeta);
    }
    if (data.containsKey('uri')) {
      context.handle(
        _uriMeta,
        uri.isAcceptableOrUnknown(data['uri']!, _uriMeta),
      );
    } else if (isInserting) {
      context.missing(_uriMeta);
    }
    if (data.containsKey('checksum')) {
      context.handle(
        _checksumMeta,
        checksum.isAcceptableOrUnknown(data['checksum']!, _checksumMeta),
      );
    } else if (isInserting) {
      context.missing(_checksumMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  DataFile map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return DataFile(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      gameId: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}game_id'],
      )!,
      uri: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}uri'],
      )!,
      checksum: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}checksum'],
      )!,
    );
  }

  @override
  $DataFilesTable createAlias(String alias) {
    return $DataFilesTable(attachedDatabase, alias);
  }
}

class DataFile extends DataClass implements Insertable<DataFile> {
  final int id;
  final int gameId;
  final String uri;
  final String checksum;
  const DataFile({
    required this.id,
    required this.gameId,
    required this.uri,
    required this.checksum,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['game_id'] = Variable<int>(gameId);
    map['uri'] = Variable<String>(uri);
    map['checksum'] = Variable<String>(checksum);
    return map;
  }

  DataFilesCompanion toCompanion(bool nullToAbsent) {
    return DataFilesCompanion(
      id: Value(id),
      gameId: Value(gameId),
      uri: Value(uri),
      checksum: Value(checksum),
    );
  }

  factory DataFile.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return DataFile(
      id: serializer.fromJson<int>(json['id']),
      gameId: serializer.fromJson<int>(json['gameId']),
      uri: serializer.fromJson<String>(json['uri']),
      checksum: serializer.fromJson<String>(json['checksum']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'gameId': serializer.toJson<int>(gameId),
      'uri': serializer.toJson<String>(uri),
      'checksum': serializer.toJson<String>(checksum),
    };
  }

  DataFile copyWith({int? id, int? gameId, String? uri, String? checksum}) =>
      DataFile(
        id: id ?? this.id,
        gameId: gameId ?? this.gameId,
        uri: uri ?? this.uri,
        checksum: checksum ?? this.checksum,
      );
  DataFile copyWithCompanion(DataFilesCompanion data) {
    return DataFile(
      id: data.id.present ? data.id.value : this.id,
      gameId: data.gameId.present ? data.gameId.value : this.gameId,
      uri: data.uri.present ? data.uri.value : this.uri,
      checksum: data.checksum.present ? data.checksum.value : this.checksum,
    );
  }

  @override
  String toString() {
    return (StringBuffer('DataFile(')
          ..write('id: $id, ')
          ..write('gameId: $gameId, ')
          ..write('uri: $uri, ')
          ..write('checksum: $checksum')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, gameId, uri, checksum);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is DataFile &&
          other.id == this.id &&
          other.gameId == this.gameId &&
          other.uri == this.uri &&
          other.checksum == this.checksum);
}

class DataFilesCompanion extends UpdateCompanion<DataFile> {
  final Value<int> id;
  final Value<int> gameId;
  final Value<String> uri;
  final Value<String> checksum;
  const DataFilesCompanion({
    this.id = const Value.absent(),
    this.gameId = const Value.absent(),
    this.uri = const Value.absent(),
    this.checksum = const Value.absent(),
  });
  DataFilesCompanion.insert({
    this.id = const Value.absent(),
    required int gameId,
    required String uri,
    required String checksum,
  }) : gameId = Value(gameId),
       uri = Value(uri),
       checksum = Value(checksum);
  static Insertable<DataFile> custom({
    Expression<int>? id,
    Expression<int>? gameId,
    Expression<String>? uri,
    Expression<String>? checksum,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (gameId != null) 'game_id': gameId,
      if (uri != null) 'uri': uri,
      if (checksum != null) 'checksum': checksum,
    });
  }

  DataFilesCompanion copyWith({
    Value<int>? id,
    Value<int>? gameId,
    Value<String>? uri,
    Value<String>? checksum,
  }) {
    return DataFilesCompanion(
      id: id ?? this.id,
      gameId: gameId ?? this.gameId,
      uri: uri ?? this.uri,
      checksum: checksum ?? this.checksum,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (gameId.present) {
      map['game_id'] = Variable<int>(gameId.value);
    }
    if (uri.present) {
      map['uri'] = Variable<String>(uri.value);
    }
    if (checksum.present) {
      map['checksum'] = Variable<String>(checksum.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('DataFilesCompanion(')
          ..write('id: $id, ')
          ..write('gameId: $gameId, ')
          ..write('uri: $uri, ')
          ..write('checksum: $checksum')
          ..write(')'))
        .toString();
  }
}

class $GameCheatsTable extends GameCheats
    with TableInfo<$GameCheatsTable, GameCheat> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $GameCheatsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    hasAutoIncrement: true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'PRIMARY KEY AUTOINCREMENT',
    ),
  );
  static const VerificationMeta _gameIdMeta = const VerificationMeta('gameId');
  @override
  late final GeneratedColumn<int> gameId = GeneratedColumn<int>(
    'game_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'REFERENCES games (id)',
    ),
  );
  static const VerificationMeta _cheatIndexMeta = const VerificationMeta(
    'cheatIndex',
  );
  @override
  late final GeneratedColumn<int> cheatIndex = GeneratedColumn<int>(
    'cheat_index',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _descriptionMeta = const VerificationMeta(
    'description',
  );
  @override
  late final GeneratedColumn<String> description = GeneratedColumn<String>(
    'description',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _codeMeta = const VerificationMeta('code');
  @override
  late final GeneratedColumn<String> code = GeneratedColumn<String>(
    'code',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _enabledMeta = const VerificationMeta(
    'enabled',
  );
  @override
  late final GeneratedColumn<bool> enabled = GeneratedColumn<bool>(
    'enabled',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("enabled" IN (0, 1))',
    ),
    defaultValue: const Constant(false),
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    gameId,
    cheatIndex,
    description,
    code,
    enabled,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'game_cheats';
  @override
  VerificationContext validateIntegrity(
    Insertable<GameCheat> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('game_id')) {
      context.handle(
        _gameIdMeta,
        gameId.isAcceptableOrUnknown(data['game_id']!, _gameIdMeta),
      );
    } else if (isInserting) {
      context.missing(_gameIdMeta);
    }
    if (data.containsKey('cheat_index')) {
      context.handle(
        _cheatIndexMeta,
        cheatIndex.isAcceptableOrUnknown(data['cheat_index']!, _cheatIndexMeta),
      );
    } else if (isInserting) {
      context.missing(_cheatIndexMeta);
    }
    if (data.containsKey('description')) {
      context.handle(
        _descriptionMeta,
        description.isAcceptableOrUnknown(
          data['description']!,
          _descriptionMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_descriptionMeta);
    }
    if (data.containsKey('code')) {
      context.handle(
        _codeMeta,
        code.isAcceptableOrUnknown(data['code']!, _codeMeta),
      );
    } else if (isInserting) {
      context.missing(_codeMeta);
    }
    if (data.containsKey('enabled')) {
      context.handle(
        _enabledMeta,
        enabled.isAcceptableOrUnknown(data['enabled']!, _enabledMeta),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  GameCheat map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return GameCheat(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      gameId: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}game_id'],
      )!,
      cheatIndex: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}cheat_index'],
      )!,
      description: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}description'],
      )!,
      code: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}code'],
      )!,
      enabled: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}enabled'],
      )!,
    );
  }

  @override
  $GameCheatsTable createAlias(String alias) {
    return $GameCheatsTable(attachedDatabase, alias);
  }
}

class GameCheat extends DataClass implements Insertable<GameCheat> {
  final int id;
  final int gameId;
  final int cheatIndex;
  final String description;
  final String code;
  final bool enabled;
  const GameCheat({
    required this.id,
    required this.gameId,
    required this.cheatIndex,
    required this.description,
    required this.code,
    required this.enabled,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['game_id'] = Variable<int>(gameId);
    map['cheat_index'] = Variable<int>(cheatIndex);
    map['description'] = Variable<String>(description);
    map['code'] = Variable<String>(code);
    map['enabled'] = Variable<bool>(enabled);
    return map;
  }

  GameCheatsCompanion toCompanion(bool nullToAbsent) {
    return GameCheatsCompanion(
      id: Value(id),
      gameId: Value(gameId),
      cheatIndex: Value(cheatIndex),
      description: Value(description),
      code: Value(code),
      enabled: Value(enabled),
    );
  }

  factory GameCheat.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return GameCheat(
      id: serializer.fromJson<int>(json['id']),
      gameId: serializer.fromJson<int>(json['gameId']),
      cheatIndex: serializer.fromJson<int>(json['cheatIndex']),
      description: serializer.fromJson<String>(json['description']),
      code: serializer.fromJson<String>(json['code']),
      enabled: serializer.fromJson<bool>(json['enabled']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'gameId': serializer.toJson<int>(gameId),
      'cheatIndex': serializer.toJson<int>(cheatIndex),
      'description': serializer.toJson<String>(description),
      'code': serializer.toJson<String>(code),
      'enabled': serializer.toJson<bool>(enabled),
    };
  }

  GameCheat copyWith({
    int? id,
    int? gameId,
    int? cheatIndex,
    String? description,
    String? code,
    bool? enabled,
  }) => GameCheat(
    id: id ?? this.id,
    gameId: gameId ?? this.gameId,
    cheatIndex: cheatIndex ?? this.cheatIndex,
    description: description ?? this.description,
    code: code ?? this.code,
    enabled: enabled ?? this.enabled,
  );
  GameCheat copyWithCompanion(GameCheatsCompanion data) {
    return GameCheat(
      id: data.id.present ? data.id.value : this.id,
      gameId: data.gameId.present ? data.gameId.value : this.gameId,
      cheatIndex: data.cheatIndex.present
          ? data.cheatIndex.value
          : this.cheatIndex,
      description: data.description.present
          ? data.description.value
          : this.description,
      code: data.code.present ? data.code.value : this.code,
      enabled: data.enabled.present ? data.enabled.value : this.enabled,
    );
  }

  @override
  String toString() {
    return (StringBuffer('GameCheat(')
          ..write('id: $id, ')
          ..write('gameId: $gameId, ')
          ..write('cheatIndex: $cheatIndex, ')
          ..write('description: $description, ')
          ..write('code: $code, ')
          ..write('enabled: $enabled')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, gameId, cheatIndex, description, code, enabled);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is GameCheat &&
          other.id == this.id &&
          other.gameId == this.gameId &&
          other.cheatIndex == this.cheatIndex &&
          other.description == this.description &&
          other.code == this.code &&
          other.enabled == this.enabled);
}

class GameCheatsCompanion extends UpdateCompanion<GameCheat> {
  final Value<int> id;
  final Value<int> gameId;
  final Value<int> cheatIndex;
  final Value<String> description;
  final Value<String> code;
  final Value<bool> enabled;
  const GameCheatsCompanion({
    this.id = const Value.absent(),
    this.gameId = const Value.absent(),
    this.cheatIndex = const Value.absent(),
    this.description = const Value.absent(),
    this.code = const Value.absent(),
    this.enabled = const Value.absent(),
  });
  GameCheatsCompanion.insert({
    this.id = const Value.absent(),
    required int gameId,
    required int cheatIndex,
    required String description,
    required String code,
    this.enabled = const Value.absent(),
  }) : gameId = Value(gameId),
       cheatIndex = Value(cheatIndex),
       description = Value(description),
       code = Value(code);
  static Insertable<GameCheat> custom({
    Expression<int>? id,
    Expression<int>? gameId,
    Expression<int>? cheatIndex,
    Expression<String>? description,
    Expression<String>? code,
    Expression<bool>? enabled,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (gameId != null) 'game_id': gameId,
      if (cheatIndex != null) 'cheat_index': cheatIndex,
      if (description != null) 'description': description,
      if (code != null) 'code': code,
      if (enabled != null) 'enabled': enabled,
    });
  }

  GameCheatsCompanion copyWith({
    Value<int>? id,
    Value<int>? gameId,
    Value<int>? cheatIndex,
    Value<String>? description,
    Value<String>? code,
    Value<bool>? enabled,
  }) {
    return GameCheatsCompanion(
      id: id ?? this.id,
      gameId: gameId ?? this.gameId,
      cheatIndex: cheatIndex ?? this.cheatIndex,
      description: description ?? this.description,
      code: code ?? this.code,
      enabled: enabled ?? this.enabled,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (gameId.present) {
      map['game_id'] = Variable<int>(gameId.value);
    }
    if (cheatIndex.present) {
      map['cheat_index'] = Variable<int>(cheatIndex.value);
    }
    if (description.present) {
      map['description'] = Variable<String>(description.value);
    }
    if (code.present) {
      map['code'] = Variable<String>(code.value);
    }
    if (enabled.present) {
      map['enabled'] = Variable<bool>(enabled.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('GameCheatsCompanion(')
          ..write('id: $id, ')
          ..write('gameId: $gameId, ')
          ..write('cheatIndex: $cheatIndex, ')
          ..write('description: $description, ')
          ..write('code: $code, ')
          ..write('enabled: $enabled')
          ..write(')'))
        .toString();
  }
}

class $AchievementsTable extends Achievements
    with TableInfo<$AchievementsTable, Achievement> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $AchievementsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _gameIdMeta = const VerificationMeta('gameId');
  @override
  late final GeneratedColumn<int> gameId = GeneratedColumn<int>(
    'game_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'REFERENCES games (id)',
    ),
  );
  static const VerificationMeta _titleMeta = const VerificationMeta('title');
  @override
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
    'title',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _descriptionMeta = const VerificationMeta(
    'description',
  );
  @override
  late final GeneratedColumn<String> description = GeneratedColumn<String>(
    'description',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _pointsMeta = const VerificationMeta('points');
  @override
  late final GeneratedColumn<int> points = GeneratedColumn<int>(
    'points',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _badgeUrlMeta = const VerificationMeta(
    'badgeUrl',
  );
  @override
  late final GeneratedColumn<String> badgeUrl = GeneratedColumn<String>(
    'badge_url',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _isUnlockedMeta = const VerificationMeta(
    'isUnlocked',
  );
  @override
  late final GeneratedColumn<bool> isUnlocked = GeneratedColumn<bool>(
    'is_unlocked',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("is_unlocked" IN (0, 1))',
    ),
    defaultValue: const Constant(false),
  );
  static const VerificationMeta _unlockTimestampMeta = const VerificationMeta(
    'unlockTimestamp',
  );
  @override
  late final GeneratedColumn<int> unlockTimestamp = GeneratedColumn<int>(
    'unlock_timestamp',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    gameId,
    title,
    description,
    points,
    badgeUrl,
    isUnlocked,
    unlockTimestamp,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'achievements';
  @override
  VerificationContext validateIntegrity(
    Insertable<Achievement> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('game_id')) {
      context.handle(
        _gameIdMeta,
        gameId.isAcceptableOrUnknown(data['game_id']!, _gameIdMeta),
      );
    } else if (isInserting) {
      context.missing(_gameIdMeta);
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('description')) {
      context.handle(
        _descriptionMeta,
        description.isAcceptableOrUnknown(
          data['description']!,
          _descriptionMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_descriptionMeta);
    }
    if (data.containsKey('points')) {
      context.handle(
        _pointsMeta,
        points.isAcceptableOrUnknown(data['points']!, _pointsMeta),
      );
    } else if (isInserting) {
      context.missing(_pointsMeta);
    }
    if (data.containsKey('badge_url')) {
      context.handle(
        _badgeUrlMeta,
        badgeUrl.isAcceptableOrUnknown(data['badge_url']!, _badgeUrlMeta),
      );
    } else if (isInserting) {
      context.missing(_badgeUrlMeta);
    }
    if (data.containsKey('is_unlocked')) {
      context.handle(
        _isUnlockedMeta,
        isUnlocked.isAcceptableOrUnknown(data['is_unlocked']!, _isUnlockedMeta),
      );
    }
    if (data.containsKey('unlock_timestamp')) {
      context.handle(
        _unlockTimestampMeta,
        unlockTimestamp.isAcceptableOrUnknown(
          data['unlock_timestamp']!,
          _unlockTimestampMeta,
        ),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  Achievement map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Achievement(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      gameId: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}game_id'],
      )!,
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      description: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}description'],
      )!,
      points: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}points'],
      )!,
      badgeUrl: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}badge_url'],
      )!,
      isUnlocked: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}is_unlocked'],
      )!,
      unlockTimestamp: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}unlock_timestamp'],
      ),
    );
  }

  @override
  $AchievementsTable createAlias(String alias) {
    return $AchievementsTable(attachedDatabase, alias);
  }
}

class Achievement extends DataClass implements Insertable<Achievement> {
  final int id;
  final int gameId;
  final String title;
  final String description;
  final int points;
  final String badgeUrl;
  final bool isUnlocked;
  final int? unlockTimestamp;
  const Achievement({
    required this.id,
    required this.gameId,
    required this.title,
    required this.description,
    required this.points,
    required this.badgeUrl,
    required this.isUnlocked,
    this.unlockTimestamp,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['game_id'] = Variable<int>(gameId);
    map['title'] = Variable<String>(title);
    map['description'] = Variable<String>(description);
    map['points'] = Variable<int>(points);
    map['badge_url'] = Variable<String>(badgeUrl);
    map['is_unlocked'] = Variable<bool>(isUnlocked);
    if (!nullToAbsent || unlockTimestamp != null) {
      map['unlock_timestamp'] = Variable<int>(unlockTimestamp);
    }
    return map;
  }

  AchievementsCompanion toCompanion(bool nullToAbsent) {
    return AchievementsCompanion(
      id: Value(id),
      gameId: Value(gameId),
      title: Value(title),
      description: Value(description),
      points: Value(points),
      badgeUrl: Value(badgeUrl),
      isUnlocked: Value(isUnlocked),
      unlockTimestamp: unlockTimestamp == null && nullToAbsent
          ? const Value.absent()
          : Value(unlockTimestamp),
    );
  }

  factory Achievement.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return Achievement(
      id: serializer.fromJson<int>(json['id']),
      gameId: serializer.fromJson<int>(json['gameId']),
      title: serializer.fromJson<String>(json['title']),
      description: serializer.fromJson<String>(json['description']),
      points: serializer.fromJson<int>(json['points']),
      badgeUrl: serializer.fromJson<String>(json['badgeUrl']),
      isUnlocked: serializer.fromJson<bool>(json['isUnlocked']),
      unlockTimestamp: serializer.fromJson<int?>(json['unlockTimestamp']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'gameId': serializer.toJson<int>(gameId),
      'title': serializer.toJson<String>(title),
      'description': serializer.toJson<String>(description),
      'points': serializer.toJson<int>(points),
      'badgeUrl': serializer.toJson<String>(badgeUrl),
      'isUnlocked': serializer.toJson<bool>(isUnlocked),
      'unlockTimestamp': serializer.toJson<int?>(unlockTimestamp),
    };
  }

  Achievement copyWith({
    int? id,
    int? gameId,
    String? title,
    String? description,
    int? points,
    String? badgeUrl,
    bool? isUnlocked,
    Value<int?> unlockTimestamp = const Value.absent(),
  }) => Achievement(
    id: id ?? this.id,
    gameId: gameId ?? this.gameId,
    title: title ?? this.title,
    description: description ?? this.description,
    points: points ?? this.points,
    badgeUrl: badgeUrl ?? this.badgeUrl,
    isUnlocked: isUnlocked ?? this.isUnlocked,
    unlockTimestamp: unlockTimestamp.present
        ? unlockTimestamp.value
        : this.unlockTimestamp,
  );
  Achievement copyWithCompanion(AchievementsCompanion data) {
    return Achievement(
      id: data.id.present ? data.id.value : this.id,
      gameId: data.gameId.present ? data.gameId.value : this.gameId,
      title: data.title.present ? data.title.value : this.title,
      description: data.description.present
          ? data.description.value
          : this.description,
      points: data.points.present ? data.points.value : this.points,
      badgeUrl: data.badgeUrl.present ? data.badgeUrl.value : this.badgeUrl,
      isUnlocked: data.isUnlocked.present
          ? data.isUnlocked.value
          : this.isUnlocked,
      unlockTimestamp: data.unlockTimestamp.present
          ? data.unlockTimestamp.value
          : this.unlockTimestamp,
    );
  }

  @override
  String toString() {
    return (StringBuffer('Achievement(')
          ..write('id: $id, ')
          ..write('gameId: $gameId, ')
          ..write('title: $title, ')
          ..write('description: $description, ')
          ..write('points: $points, ')
          ..write('badgeUrl: $badgeUrl, ')
          ..write('isUnlocked: $isUnlocked, ')
          ..write('unlockTimestamp: $unlockTimestamp')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    gameId,
    title,
    description,
    points,
    badgeUrl,
    isUnlocked,
    unlockTimestamp,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is Achievement &&
          other.id == this.id &&
          other.gameId == this.gameId &&
          other.title == this.title &&
          other.description == this.description &&
          other.points == this.points &&
          other.badgeUrl == this.badgeUrl &&
          other.isUnlocked == this.isUnlocked &&
          other.unlockTimestamp == this.unlockTimestamp);
}

class AchievementsCompanion extends UpdateCompanion<Achievement> {
  final Value<int> id;
  final Value<int> gameId;
  final Value<String> title;
  final Value<String> description;
  final Value<int> points;
  final Value<String> badgeUrl;
  final Value<bool> isUnlocked;
  final Value<int?> unlockTimestamp;
  const AchievementsCompanion({
    this.id = const Value.absent(),
    this.gameId = const Value.absent(),
    this.title = const Value.absent(),
    this.description = const Value.absent(),
    this.points = const Value.absent(),
    this.badgeUrl = const Value.absent(),
    this.isUnlocked = const Value.absent(),
    this.unlockTimestamp = const Value.absent(),
  });
  AchievementsCompanion.insert({
    this.id = const Value.absent(),
    required int gameId,
    required String title,
    required String description,
    required int points,
    required String badgeUrl,
    this.isUnlocked = const Value.absent(),
    this.unlockTimestamp = const Value.absent(),
  }) : gameId = Value(gameId),
       title = Value(title),
       description = Value(description),
       points = Value(points),
       badgeUrl = Value(badgeUrl);
  static Insertable<Achievement> custom({
    Expression<int>? id,
    Expression<int>? gameId,
    Expression<String>? title,
    Expression<String>? description,
    Expression<int>? points,
    Expression<String>? badgeUrl,
    Expression<bool>? isUnlocked,
    Expression<int>? unlockTimestamp,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (gameId != null) 'game_id': gameId,
      if (title != null) 'title': title,
      if (description != null) 'description': description,
      if (points != null) 'points': points,
      if (badgeUrl != null) 'badge_url': badgeUrl,
      if (isUnlocked != null) 'is_unlocked': isUnlocked,
      if (unlockTimestamp != null) 'unlock_timestamp': unlockTimestamp,
    });
  }

  AchievementsCompanion copyWith({
    Value<int>? id,
    Value<int>? gameId,
    Value<String>? title,
    Value<String>? description,
    Value<int>? points,
    Value<String>? badgeUrl,
    Value<bool>? isUnlocked,
    Value<int?>? unlockTimestamp,
  }) {
    return AchievementsCompanion(
      id: id ?? this.id,
      gameId: gameId ?? this.gameId,
      title: title ?? this.title,
      description: description ?? this.description,
      points: points ?? this.points,
      badgeUrl: badgeUrl ?? this.badgeUrl,
      isUnlocked: isUnlocked ?? this.isUnlocked,
      unlockTimestamp: unlockTimestamp ?? this.unlockTimestamp,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (gameId.present) {
      map['game_id'] = Variable<int>(gameId.value);
    }
    if (title.present) {
      map['title'] = Variable<String>(title.value);
    }
    if (description.present) {
      map['description'] = Variable<String>(description.value);
    }
    if (points.present) {
      map['points'] = Variable<int>(points.value);
    }
    if (badgeUrl.present) {
      map['badge_url'] = Variable<String>(badgeUrl.value);
    }
    if (isUnlocked.present) {
      map['is_unlocked'] = Variable<bool>(isUnlocked.value);
    }
    if (unlockTimestamp.present) {
      map['unlock_timestamp'] = Variable<int>(unlockTimestamp.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('AchievementsCompanion(')
          ..write('id: $id, ')
          ..write('gameId: $gameId, ')
          ..write('title: $title, ')
          ..write('description: $description, ')
          ..write('points: $points, ')
          ..write('badgeUrl: $badgeUrl, ')
          ..write('isUnlocked: $isUnlocked, ')
          ..write('unlockTimestamp: $unlockTimestamp')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $GamesTable games = $GamesTable(this);
  late final $DataFilesTable dataFiles = $DataFilesTable(this);
  late final $GameCheatsTable gameCheats = $GameCheatsTable(this);
  late final $AchievementsTable achievements = $AchievementsTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
    games,
    dataFiles,
    gameCheats,
    achievements,
  ];
}

typedef $$GamesTableCreateCompanionBuilder =
    GamesCompanion Function({
      Value<int> id,
      required String title,
      required String path,
      required String systemId,
      Value<String?> coverUrl,
      Value<String?> backCoverUrl,
      Value<String?> manualUrl,
      Value<bool> isFavorite,
      Value<int?> lastPlayedTimestamp,
    });
typedef $$GamesTableUpdateCompanionBuilder =
    GamesCompanion Function({
      Value<int> id,
      Value<String> title,
      Value<String> path,
      Value<String> systemId,
      Value<String?> coverUrl,
      Value<String?> backCoverUrl,
      Value<String?> manualUrl,
      Value<bool> isFavorite,
      Value<int?> lastPlayedTimestamp,
    });

final class $$GamesTableReferences
    extends BaseReferences<_$AppDatabase, $GamesTable, Game> {
  $$GamesTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static MultiTypedResultKey<$DataFilesTable, List<DataFile>>
  _dataFilesRefsTable(_$AppDatabase db) => MultiTypedResultKey.fromTable(
    db.dataFiles,
    aliasName: 'games__id__data_files__game_id',
  );

  $$DataFilesTableProcessedTableManager get dataFilesRefs {
    final manager = $$DataFilesTableTableManager(
      $_db,
      $_db.dataFiles,
    ).filter((f) => f.gameId.id.sqlEquals($_itemColumn<int>('id')!));

    final cache = $_typedResult.readTableOrNull(_dataFilesRefsTable($_db));
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }

  static MultiTypedResultKey<$GameCheatsTable, List<GameCheat>>
  _gameCheatsRefsTable(_$AppDatabase db) => MultiTypedResultKey.fromTable(
    db.gameCheats,
    aliasName: 'games__id__game_cheats__game_id',
  );

  $$GameCheatsTableProcessedTableManager get gameCheatsRefs {
    final manager = $$GameCheatsTableTableManager(
      $_db,
      $_db.gameCheats,
    ).filter((f) => f.gameId.id.sqlEquals($_itemColumn<int>('id')!));

    final cache = $_typedResult.readTableOrNull(_gameCheatsRefsTable($_db));
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }

  static MultiTypedResultKey<$AchievementsTable, List<Achievement>>
  _achievementsRefsTable(_$AppDatabase db) => MultiTypedResultKey.fromTable(
    db.achievements,
    aliasName: 'games__id__achievements__game_id',
  );

  $$AchievementsTableProcessedTableManager get achievementsRefs {
    final manager = $$AchievementsTableTableManager(
      $_db,
      $_db.achievements,
    ).filter((f) => f.gameId.id.sqlEquals($_itemColumn<int>('id')!));

    final cache = $_typedResult.readTableOrNull(_achievementsRefsTable($_db));
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }
}

class $$GamesTableFilterComposer extends Composer<_$AppDatabase, $GamesTable> {
  $$GamesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get path => $composableBuilder(
    column: $table.path,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get systemId => $composableBuilder(
    column: $table.systemId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get coverUrl => $composableBuilder(
    column: $table.coverUrl,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get backCoverUrl => $composableBuilder(
    column: $table.backCoverUrl,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get manualUrl => $composableBuilder(
    column: $table.manualUrl,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get isFavorite => $composableBuilder(
    column: $table.isFavorite,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get lastPlayedTimestamp => $composableBuilder(
    column: $table.lastPlayedTimestamp,
    builder: (column) => ColumnFilters(column),
  );

  Expression<bool> dataFilesRefs(
    Expression<bool> Function($$DataFilesTableFilterComposer f) f,
  ) {
    final $$DataFilesTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.dataFiles,
      getReferencedColumn: (t) => t.gameId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$DataFilesTableFilterComposer(
            $db: $db,
            $table: $db.dataFiles,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }

  Expression<bool> gameCheatsRefs(
    Expression<bool> Function($$GameCheatsTableFilterComposer f) f,
  ) {
    final $$GameCheatsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.gameCheats,
      getReferencedColumn: (t) => t.gameId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GameCheatsTableFilterComposer(
            $db: $db,
            $table: $db.gameCheats,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }

  Expression<bool> achievementsRefs(
    Expression<bool> Function($$AchievementsTableFilterComposer f) f,
  ) {
    final $$AchievementsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.achievements,
      getReferencedColumn: (t) => t.gameId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$AchievementsTableFilterComposer(
            $db: $db,
            $table: $db.achievements,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$GamesTableOrderingComposer
    extends Composer<_$AppDatabase, $GamesTable> {
  $$GamesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get path => $composableBuilder(
    column: $table.path,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get systemId => $composableBuilder(
    column: $table.systemId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get coverUrl => $composableBuilder(
    column: $table.coverUrl,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get backCoverUrl => $composableBuilder(
    column: $table.backCoverUrl,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get manualUrl => $composableBuilder(
    column: $table.manualUrl,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get isFavorite => $composableBuilder(
    column: $table.isFavorite,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get lastPlayedTimestamp => $composableBuilder(
    column: $table.lastPlayedTimestamp,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$GamesTableAnnotationComposer
    extends Composer<_$AppDatabase, $GamesTable> {
  $$GamesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  GeneratedColumn<String> get path =>
      $composableBuilder(column: $table.path, builder: (column) => column);

  GeneratedColumn<String> get systemId =>
      $composableBuilder(column: $table.systemId, builder: (column) => column);

  GeneratedColumn<String> get coverUrl =>
      $composableBuilder(column: $table.coverUrl, builder: (column) => column);

  GeneratedColumn<String> get backCoverUrl => $composableBuilder(
    column: $table.backCoverUrl,
    builder: (column) => column,
  );

  GeneratedColumn<String> get manualUrl =>
      $composableBuilder(column: $table.manualUrl, builder: (column) => column);

  GeneratedColumn<bool> get isFavorite => $composableBuilder(
    column: $table.isFavorite,
    builder: (column) => column,
  );

  GeneratedColumn<int> get lastPlayedTimestamp => $composableBuilder(
    column: $table.lastPlayedTimestamp,
    builder: (column) => column,
  );

  Expression<T> dataFilesRefs<T extends Object>(
    Expression<T> Function($$DataFilesTableAnnotationComposer a) f,
  ) {
    final $$DataFilesTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.dataFiles,
      getReferencedColumn: (t) => t.gameId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$DataFilesTableAnnotationComposer(
            $db: $db,
            $table: $db.dataFiles,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }

  Expression<T> gameCheatsRefs<T extends Object>(
    Expression<T> Function($$GameCheatsTableAnnotationComposer a) f,
  ) {
    final $$GameCheatsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.gameCheats,
      getReferencedColumn: (t) => t.gameId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GameCheatsTableAnnotationComposer(
            $db: $db,
            $table: $db.gameCheats,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }

  Expression<T> achievementsRefs<T extends Object>(
    Expression<T> Function($$AchievementsTableAnnotationComposer a) f,
  ) {
    final $$AchievementsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.achievements,
      getReferencedColumn: (t) => t.gameId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$AchievementsTableAnnotationComposer(
            $db: $db,
            $table: $db.achievements,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$GamesTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $GamesTable,
          Game,
          $$GamesTableFilterComposer,
          $$GamesTableOrderingComposer,
          $$GamesTableAnnotationComposer,
          $$GamesTableCreateCompanionBuilder,
          $$GamesTableUpdateCompanionBuilder,
          (Game, $$GamesTableReferences),
          Game,
          PrefetchHooks Function({
            bool dataFilesRefs,
            bool gameCheatsRefs,
            bool achievementsRefs,
          })
        > {
  $$GamesTableTableManager(_$AppDatabase db, $GamesTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$GamesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$GamesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$GamesTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<String> title = const Value.absent(),
                Value<String> path = const Value.absent(),
                Value<String> systemId = const Value.absent(),
                Value<String?> coverUrl = const Value.absent(),
                Value<String?> backCoverUrl = const Value.absent(),
                Value<String?> manualUrl = const Value.absent(),
                Value<bool> isFavorite = const Value.absent(),
                Value<int?> lastPlayedTimestamp = const Value.absent(),
              }) => GamesCompanion(
                id: id,
                title: title,
                path: path,
                systemId: systemId,
                coverUrl: coverUrl,
                backCoverUrl: backCoverUrl,
                manualUrl: manualUrl,
                isFavorite: isFavorite,
                lastPlayedTimestamp: lastPlayedTimestamp,
              ),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                required String title,
                required String path,
                required String systemId,
                Value<String?> coverUrl = const Value.absent(),
                Value<String?> backCoverUrl = const Value.absent(),
                Value<String?> manualUrl = const Value.absent(),
                Value<bool> isFavorite = const Value.absent(),
                Value<int?> lastPlayedTimestamp = const Value.absent(),
              }) => GamesCompanion.insert(
                id: id,
                title: title,
                path: path,
                systemId: systemId,
                coverUrl: coverUrl,
                backCoverUrl: backCoverUrl,
                manualUrl: manualUrl,
                isFavorite: isFavorite,
                lastPlayedTimestamp: lastPlayedTimestamp,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) =>
                    (e.readTable(table), $$GamesTableReferences(db, table, e)),
              )
              .toList(),
          prefetchHooksCallback:
              ({
                dataFilesRefs = false,
                gameCheatsRefs = false,
                achievementsRefs = false,
              }) {
                return PrefetchHooks(
                  db: db,
                  explicitlyWatchedTables: [
                    if (dataFilesRefs) db.dataFiles,
                    if (gameCheatsRefs) db.gameCheats,
                    if (achievementsRefs) db.achievements,
                  ],
                  addJoins: null,
                  getPrefetchedDataCallback: (items) async {
                    return [
                      if (dataFilesRefs)
                        await $_getPrefetchedData<Game, $GamesTable, DataFile>(
                          currentTable: table,
                          referencedTable: $$GamesTableReferences
                              ._dataFilesRefsTable(db),
                          managerFromTypedResult: (p0) =>
                              $$GamesTableReferences(
                                db,
                                table,
                                p0,
                              ).dataFilesRefs,
                          referencedItemsForCurrentItem:
                              (item, referencedItems) => referencedItems.where(
                                (e) => e.gameId == item.id,
                              ),
                          typedResults: items,
                        ),
                      if (gameCheatsRefs)
                        await $_getPrefetchedData<Game, $GamesTable, GameCheat>(
                          currentTable: table,
                          referencedTable: $$GamesTableReferences
                              ._gameCheatsRefsTable(db),
                          managerFromTypedResult: (p0) =>
                              $$GamesTableReferences(
                                db,
                                table,
                                p0,
                              ).gameCheatsRefs,
                          referencedItemsForCurrentItem:
                              (item, referencedItems) => referencedItems.where(
                                (e) => e.gameId == item.id,
                              ),
                          typedResults: items,
                        ),
                      if (achievementsRefs)
                        await $_getPrefetchedData<
                          Game,
                          $GamesTable,
                          Achievement
                        >(
                          currentTable: table,
                          referencedTable: $$GamesTableReferences
                              ._achievementsRefsTable(db),
                          managerFromTypedResult: (p0) =>
                              $$GamesTableReferences(
                                db,
                                table,
                                p0,
                              ).achievementsRefs,
                          referencedItemsForCurrentItem:
                              (item, referencedItems) => referencedItems.where(
                                (e) => e.gameId == item.id,
                              ),
                          typedResults: items,
                        ),
                    ];
                  },
                );
              },
        ),
      );
}

typedef $$GamesTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $GamesTable,
      Game,
      $$GamesTableFilterComposer,
      $$GamesTableOrderingComposer,
      $$GamesTableAnnotationComposer,
      $$GamesTableCreateCompanionBuilder,
      $$GamesTableUpdateCompanionBuilder,
      (Game, $$GamesTableReferences),
      Game,
      PrefetchHooks Function({
        bool dataFilesRefs,
        bool gameCheatsRefs,
        bool achievementsRefs,
      })
    >;
typedef $$DataFilesTableCreateCompanionBuilder =
    DataFilesCompanion Function({
      Value<int> id,
      required int gameId,
      required String uri,
      required String checksum,
    });
typedef $$DataFilesTableUpdateCompanionBuilder =
    DataFilesCompanion Function({
      Value<int> id,
      Value<int> gameId,
      Value<String> uri,
      Value<String> checksum,
    });

final class $$DataFilesTableReferences
    extends BaseReferences<_$AppDatabase, $DataFilesTable, DataFile> {
  $$DataFilesTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static $GamesTable _gameIdTable(_$AppDatabase db) =>
      db.games.createAlias('data_files__game_id__games__id');

  $$GamesTableProcessedTableManager get gameId {
    final $_column = $_itemColumn<int>('game_id')!;

    final manager = $$GamesTableTableManager(
      $_db,
      $_db.games,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_gameIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }
}

class $$DataFilesTableFilterComposer
    extends Composer<_$AppDatabase, $DataFilesTable> {
  $$DataFilesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get uri => $composableBuilder(
    column: $table.uri,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get checksum => $composableBuilder(
    column: $table.checksum,
    builder: (column) => ColumnFilters(column),
  );

  $$GamesTableFilterComposer get gameId {
    final $$GamesTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableFilterComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$DataFilesTableOrderingComposer
    extends Composer<_$AppDatabase, $DataFilesTable> {
  $$DataFilesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get uri => $composableBuilder(
    column: $table.uri,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get checksum => $composableBuilder(
    column: $table.checksum,
    builder: (column) => ColumnOrderings(column),
  );

  $$GamesTableOrderingComposer get gameId {
    final $$GamesTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableOrderingComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$DataFilesTableAnnotationComposer
    extends Composer<_$AppDatabase, $DataFilesTable> {
  $$DataFilesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get uri =>
      $composableBuilder(column: $table.uri, builder: (column) => column);

  GeneratedColumn<String> get checksum =>
      $composableBuilder(column: $table.checksum, builder: (column) => column);

  $$GamesTableAnnotationComposer get gameId {
    final $$GamesTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableAnnotationComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$DataFilesTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $DataFilesTable,
          DataFile,
          $$DataFilesTableFilterComposer,
          $$DataFilesTableOrderingComposer,
          $$DataFilesTableAnnotationComposer,
          $$DataFilesTableCreateCompanionBuilder,
          $$DataFilesTableUpdateCompanionBuilder,
          (DataFile, $$DataFilesTableReferences),
          DataFile,
          PrefetchHooks Function({bool gameId})
        > {
  $$DataFilesTableTableManager(_$AppDatabase db, $DataFilesTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$DataFilesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$DataFilesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$DataFilesTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<int> gameId = const Value.absent(),
                Value<String> uri = const Value.absent(),
                Value<String> checksum = const Value.absent(),
              }) => DataFilesCompanion(
                id: id,
                gameId: gameId,
                uri: uri,
                checksum: checksum,
              ),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                required int gameId,
                required String uri,
                required String checksum,
              }) => DataFilesCompanion.insert(
                id: id,
                gameId: gameId,
                uri: uri,
                checksum: checksum,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$DataFilesTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: ({gameId = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [],
              addJoins:
                  <
                    T extends TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic
                    >
                  >(state) {
                    if (gameId) {
                      state =
                          state.withJoin(
                                currentTable: table,
                                currentColumn: table.gameId,
                                referencedTable: $$DataFilesTableReferences
                                    ._gameIdTable(db),
                                referencedColumn: $$DataFilesTableReferences
                                    ._gameIdTable(db)
                                    .id,
                              )
                              as T;
                    }

                    return state;
                  },
              getPrefetchedDataCallback: (items) async {
                return [];
              },
            );
          },
        ),
      );
}

typedef $$DataFilesTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $DataFilesTable,
      DataFile,
      $$DataFilesTableFilterComposer,
      $$DataFilesTableOrderingComposer,
      $$DataFilesTableAnnotationComposer,
      $$DataFilesTableCreateCompanionBuilder,
      $$DataFilesTableUpdateCompanionBuilder,
      (DataFile, $$DataFilesTableReferences),
      DataFile,
      PrefetchHooks Function({bool gameId})
    >;
typedef $$GameCheatsTableCreateCompanionBuilder =
    GameCheatsCompanion Function({
      Value<int> id,
      required int gameId,
      required int cheatIndex,
      required String description,
      required String code,
      Value<bool> enabled,
    });
typedef $$GameCheatsTableUpdateCompanionBuilder =
    GameCheatsCompanion Function({
      Value<int> id,
      Value<int> gameId,
      Value<int> cheatIndex,
      Value<String> description,
      Value<String> code,
      Value<bool> enabled,
    });

final class $$GameCheatsTableReferences
    extends BaseReferences<_$AppDatabase, $GameCheatsTable, GameCheat> {
  $$GameCheatsTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static $GamesTable _gameIdTable(_$AppDatabase db) =>
      db.games.createAlias('game_cheats__game_id__games__id');

  $$GamesTableProcessedTableManager get gameId {
    final $_column = $_itemColumn<int>('game_id')!;

    final manager = $$GamesTableTableManager(
      $_db,
      $_db.games,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_gameIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }
}

class $$GameCheatsTableFilterComposer
    extends Composer<_$AppDatabase, $GameCheatsTable> {
  $$GameCheatsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get cheatIndex => $composableBuilder(
    column: $table.cheatIndex,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get code => $composableBuilder(
    column: $table.code,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get enabled => $composableBuilder(
    column: $table.enabled,
    builder: (column) => ColumnFilters(column),
  );

  $$GamesTableFilterComposer get gameId {
    final $$GamesTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableFilterComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$GameCheatsTableOrderingComposer
    extends Composer<_$AppDatabase, $GameCheatsTable> {
  $$GameCheatsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get cheatIndex => $composableBuilder(
    column: $table.cheatIndex,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get code => $composableBuilder(
    column: $table.code,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get enabled => $composableBuilder(
    column: $table.enabled,
    builder: (column) => ColumnOrderings(column),
  );

  $$GamesTableOrderingComposer get gameId {
    final $$GamesTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableOrderingComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$GameCheatsTableAnnotationComposer
    extends Composer<_$AppDatabase, $GameCheatsTable> {
  $$GameCheatsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get cheatIndex => $composableBuilder(
    column: $table.cheatIndex,
    builder: (column) => column,
  );

  GeneratedColumn<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => column,
  );

  GeneratedColumn<String> get code =>
      $composableBuilder(column: $table.code, builder: (column) => column);

  GeneratedColumn<bool> get enabled =>
      $composableBuilder(column: $table.enabled, builder: (column) => column);

  $$GamesTableAnnotationComposer get gameId {
    final $$GamesTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableAnnotationComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$GameCheatsTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $GameCheatsTable,
          GameCheat,
          $$GameCheatsTableFilterComposer,
          $$GameCheatsTableOrderingComposer,
          $$GameCheatsTableAnnotationComposer,
          $$GameCheatsTableCreateCompanionBuilder,
          $$GameCheatsTableUpdateCompanionBuilder,
          (GameCheat, $$GameCheatsTableReferences),
          GameCheat,
          PrefetchHooks Function({bool gameId})
        > {
  $$GameCheatsTableTableManager(_$AppDatabase db, $GameCheatsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$GameCheatsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$GameCheatsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$GameCheatsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<int> gameId = const Value.absent(),
                Value<int> cheatIndex = const Value.absent(),
                Value<String> description = const Value.absent(),
                Value<String> code = const Value.absent(),
                Value<bool> enabled = const Value.absent(),
              }) => GameCheatsCompanion(
                id: id,
                gameId: gameId,
                cheatIndex: cheatIndex,
                description: description,
                code: code,
                enabled: enabled,
              ),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                required int gameId,
                required int cheatIndex,
                required String description,
                required String code,
                Value<bool> enabled = const Value.absent(),
              }) => GameCheatsCompanion.insert(
                id: id,
                gameId: gameId,
                cheatIndex: cheatIndex,
                description: description,
                code: code,
                enabled: enabled,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$GameCheatsTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: ({gameId = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [],
              addJoins:
                  <
                    T extends TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic
                    >
                  >(state) {
                    if (gameId) {
                      state =
                          state.withJoin(
                                currentTable: table,
                                currentColumn: table.gameId,
                                referencedTable: $$GameCheatsTableReferences
                                    ._gameIdTable(db),
                                referencedColumn: $$GameCheatsTableReferences
                                    ._gameIdTable(db)
                                    .id,
                              )
                              as T;
                    }

                    return state;
                  },
              getPrefetchedDataCallback: (items) async {
                return [];
              },
            );
          },
        ),
      );
}

typedef $$GameCheatsTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $GameCheatsTable,
      GameCheat,
      $$GameCheatsTableFilterComposer,
      $$GameCheatsTableOrderingComposer,
      $$GameCheatsTableAnnotationComposer,
      $$GameCheatsTableCreateCompanionBuilder,
      $$GameCheatsTableUpdateCompanionBuilder,
      (GameCheat, $$GameCheatsTableReferences),
      GameCheat,
      PrefetchHooks Function({bool gameId})
    >;
typedef $$AchievementsTableCreateCompanionBuilder =
    AchievementsCompanion Function({
      Value<int> id,
      required int gameId,
      required String title,
      required String description,
      required int points,
      required String badgeUrl,
      Value<bool> isUnlocked,
      Value<int?> unlockTimestamp,
    });
typedef $$AchievementsTableUpdateCompanionBuilder =
    AchievementsCompanion Function({
      Value<int> id,
      Value<int> gameId,
      Value<String> title,
      Value<String> description,
      Value<int> points,
      Value<String> badgeUrl,
      Value<bool> isUnlocked,
      Value<int?> unlockTimestamp,
    });

final class $$AchievementsTableReferences
    extends BaseReferences<_$AppDatabase, $AchievementsTable, Achievement> {
  $$AchievementsTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static $GamesTable _gameIdTable(_$AppDatabase db) =>
      db.games.createAlias('achievements__game_id__games__id');

  $$GamesTableProcessedTableManager get gameId {
    final $_column = $_itemColumn<int>('game_id')!;

    final manager = $$GamesTableTableManager(
      $_db,
      $_db.games,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_gameIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }
}

class $$AchievementsTableFilterComposer
    extends Composer<_$AppDatabase, $AchievementsTable> {
  $$AchievementsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get points => $composableBuilder(
    column: $table.points,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get badgeUrl => $composableBuilder(
    column: $table.badgeUrl,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get isUnlocked => $composableBuilder(
    column: $table.isUnlocked,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get unlockTimestamp => $composableBuilder(
    column: $table.unlockTimestamp,
    builder: (column) => ColumnFilters(column),
  );

  $$GamesTableFilterComposer get gameId {
    final $$GamesTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableFilterComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$AchievementsTableOrderingComposer
    extends Composer<_$AppDatabase, $AchievementsTable> {
  $$AchievementsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get points => $composableBuilder(
    column: $table.points,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get badgeUrl => $composableBuilder(
    column: $table.badgeUrl,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get isUnlocked => $composableBuilder(
    column: $table.isUnlocked,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get unlockTimestamp => $composableBuilder(
    column: $table.unlockTimestamp,
    builder: (column) => ColumnOrderings(column),
  );

  $$GamesTableOrderingComposer get gameId {
    final $$GamesTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableOrderingComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$AchievementsTableAnnotationComposer
    extends Composer<_$AppDatabase, $AchievementsTable> {
  $$AchievementsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  GeneratedColumn<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => column,
  );

  GeneratedColumn<int> get points =>
      $composableBuilder(column: $table.points, builder: (column) => column);

  GeneratedColumn<String> get badgeUrl =>
      $composableBuilder(column: $table.badgeUrl, builder: (column) => column);

  GeneratedColumn<bool> get isUnlocked => $composableBuilder(
    column: $table.isUnlocked,
    builder: (column) => column,
  );

  GeneratedColumn<int> get unlockTimestamp => $composableBuilder(
    column: $table.unlockTimestamp,
    builder: (column) => column,
  );

  $$GamesTableAnnotationComposer get gameId {
    final $$GamesTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.gameId,
      referencedTable: $db.games,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$GamesTableAnnotationComposer(
            $db: $db,
            $table: $db.games,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$AchievementsTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $AchievementsTable,
          Achievement,
          $$AchievementsTableFilterComposer,
          $$AchievementsTableOrderingComposer,
          $$AchievementsTableAnnotationComposer,
          $$AchievementsTableCreateCompanionBuilder,
          $$AchievementsTableUpdateCompanionBuilder,
          (Achievement, $$AchievementsTableReferences),
          Achievement,
          PrefetchHooks Function({bool gameId})
        > {
  $$AchievementsTableTableManager(_$AppDatabase db, $AchievementsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$AchievementsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$AchievementsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$AchievementsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<int> gameId = const Value.absent(),
                Value<String> title = const Value.absent(),
                Value<String> description = const Value.absent(),
                Value<int> points = const Value.absent(),
                Value<String> badgeUrl = const Value.absent(),
                Value<bool> isUnlocked = const Value.absent(),
                Value<int?> unlockTimestamp = const Value.absent(),
              }) => AchievementsCompanion(
                id: id,
                gameId: gameId,
                title: title,
                description: description,
                points: points,
                badgeUrl: badgeUrl,
                isUnlocked: isUnlocked,
                unlockTimestamp: unlockTimestamp,
              ),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                required int gameId,
                required String title,
                required String description,
                required int points,
                required String badgeUrl,
                Value<bool> isUnlocked = const Value.absent(),
                Value<int?> unlockTimestamp = const Value.absent(),
              }) => AchievementsCompanion.insert(
                id: id,
                gameId: gameId,
                title: title,
                description: description,
                points: points,
                badgeUrl: badgeUrl,
                isUnlocked: isUnlocked,
                unlockTimestamp: unlockTimestamp,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$AchievementsTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: ({gameId = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [],
              addJoins:
                  <
                    T extends TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic
                    >
                  >(state) {
                    if (gameId) {
                      state =
                          state.withJoin(
                                currentTable: table,
                                currentColumn: table.gameId,
                                referencedTable: $$AchievementsTableReferences
                                    ._gameIdTable(db),
                                referencedColumn: $$AchievementsTableReferences
                                    ._gameIdTable(db)
                                    .id,
                              )
                              as T;
                    }

                    return state;
                  },
              getPrefetchedDataCallback: (items) async {
                return [];
              },
            );
          },
        ),
      );
}

typedef $$AchievementsTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $AchievementsTable,
      Achievement,
      $$AchievementsTableFilterComposer,
      $$AchievementsTableOrderingComposer,
      $$AchievementsTableAnnotationComposer,
      $$AchievementsTableCreateCompanionBuilder,
      $$AchievementsTableUpdateCompanionBuilder,
      (Achievement, $$AchievementsTableReferences),
      Achievement,
      PrefetchHooks Function({bool gameId})
    >;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$GamesTableTableManager get games =>
      $$GamesTableTableManager(_db, _db.games);
  $$DataFilesTableTableManager get dataFiles =>
      $$DataFilesTableTableManager(_db, _db.dataFiles);
  $$GameCheatsTableTableManager get gameCheats =>
      $$GameCheatsTableTableManager(_db, _db.gameCheats);
  $$AchievementsTableTableManager get achievements =>
      $$AchievementsTableTableManager(_db, _db.achievements);
}
