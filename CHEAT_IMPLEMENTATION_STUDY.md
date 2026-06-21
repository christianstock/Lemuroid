# Lemuroid Cheat System Implementation Study
## Based on PPSSPP Architecture Analysis

**Date:** June 21, 2026  
**Branch:** `feature/cheat-support`  
**Target Systems:** GB, GBC, GBA

---

## I. PPSSPP CHEAT SYSTEM ARCHITECTURE

### A. File Format Overview

PPSSPP uses "CwCheat" format (loosely based on Codebreaker):

```
_S GAME_ID
_G Game Title
_C0 Cheat Name (disabled by default, 0 = disabled, 1 = enabled)
_L XXXX XXXX  (hex code lines, can be multiple)
_C1 Another Cheat (enabled by default)
_L YYYY YYYY
_L ZZZZ ZZZZ
```

**Format Details:**
- `_S` = Section (Game ID) - required
- `_G` = Game title/comment - optional
- `_C0` / `_C1` = Cheat header (0=disabled, 1=enabled) + cheat name
- `_L` = Cheat data line (two 32-bit hex values)
- Supports multi-line cheats (multiple `_L` entries per cheat)
- Comments: `//` or `#` (ignored)
- UTF-8 BOM support (auto-trimmed)

**Example from PPSSPP source:**
```cpp
// PPSSPP libretro.cpp#L1920-L1942
fprintf(outFile, "_S %s\n", g_paramSFO.GetDiscID().c_str());
for (const std::string &cheat : cheats) {
    fprintf(outFile, "_C%s\n", cheat.c_str());  // cheat includes "0 name" or "1 name"
}
```

### B. Core Components (C++ Implementation)

#### 1. **Data Structures** (`Core/CwCheat.h`)

```cpp
struct CheatLine {
    uint32_t part1;  // First hex value
    uint32_t part2;  // Second hex value
};

struct CheatCode {
    std::string name;
    std::vector<CheatLine> lines;  // Multi-line support
};

struct CheatFileInfo {
    int lineNum;                // Line number in file
    std::string name;           // Cheat name
    bool enabled;               // Enable state
};
```

#### 2. **File Parser** (`Core/CwCheat.cpp` - CheatFileParser class)

```cpp
class CheatFileParser {
public:
    CheatFileParser(const Path &filename, std::string_view gameID);
    bool Parse();
    
    const std::vector<CheatCode> &GetCheats() const;
    const std::vector<CheatFileInfo> &GetFileInfo() const;
    const std::vector<std::string> &GetErrors() const;
    
private:
    void ParseLine(const std::string &line, int lineNumber);
    void ParseDataLine(const std::string &line, int lineNumber);
    void Flush();
    void FlushCheatInfo();
    bool ValidateGameID(std::string_view gameID);
    
    FILE *fp_;
    std::string validGameID_;
    std::vector<CheatCode> cheats_;
    std::vector<CheatFileInfo> cheatInfo_;
    std::vector<CheatLine> pendingLines_;
};
```

**Key Logic:**
- Parses line-by-line (handles large files)
- Validates game IDs against current game
- Tracks line numbers for UI (enable/disable specific cheats)
- Collects errors for user reporting
- Supports multi-line cheats via `pendingLines_` buffer

#### 3. **Cheat Engine** (`Core/CwCheat.h` - CWCheatEngine class)

```cpp
class CWCheatEngine {
public:
    CWCheatEngine(std::string_view gameID);
    void ParseCheats();
    void CreateCheatFile();
    void Run();
    bool HasCheats();
    const Path &CheatFilename() const;
    std::vector<CheatFileInfo> FileInfo() const;
    
private:
    CheatOperation InterpretNextCwCheat(const CheatCode &cheat, size_t &i);
    void ExecuteOp(const CheatOperation &op, const CheatCode &cheat, size_t &i);
    
    std::vector<CheatCode> cheats_;
    std::string gameID_;
    Path filename_;
};
```

**Execution:**
- Called periodically via `hleCheat()` function
- Parses cheat codes into `CheatOperation` structs
- Executes operations per frame (memory reads/writes at addresses)
- Supports conditional cheats (if statements)

#### 4. **UI Integration** (`UI/CwCheatScreen.cpp`)

**Screen provides:**
- List of cheats for current game
- Enable/disable toggles
- Import from file (flat files or zips)
- Download database
- Edit cheat file directly
- Show cheat names and descriptions

**Key functions:**
```cpp
class CwCheatScreen {
    bool ImportCheats(const Path &cheatFile, int *cheatsFound);
    void ImportAndReport(const Path &cheatFile);
    bool RebuildCheatFile(int index);  // Update file after enable/disable
    void OnCheckBox(int index);        // Handle toggle
};
```

**Import Logic:**
1. Open file
2. Search for `_S GAME_ID` matching current game
3. Copy all `_C` and `_L` lines for that game
4. Append to game's cheat file
5. Parse and reload

#### 5. **Zip File Support**

PPSSPP doesn't have special zip cheat support. It:
- Handles normal file browse (Windows dialog, platform-specific)
- Allows `.db` extension files
- No built-in zip extraction for cheats

**But** PPSSPP has `ZipContainer` and zip utilities for other features:
```cpp
// Loaders.cpp
ZipContainer ZipOpenPath(const Path &fileName);
void ZipClose(ZipContainer &z);
bool DetectZipFileContents(zip_t *z, ZipFileInfo *info);
```

---

## II. LEMUROID ARCHITECTURE ANALYSIS

### Current State

**Database:**
- Room database: `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/library/db/RetrogradeDatabase.kt`
- Already has Game table with system info
- No cheat tables

**Game System:**
- `GameSystem.kt` defines GB, GBC, GBA with core configs
- LibRetro cores: Gambatte (GB/GBC), mGBA (GBA)

**LibretroDroid:**
- Currently no exposed cheat API
- Internal LibRetro core integration (via JNI)

**Storage:**
- `DirectoriesManager.kt` - manages save/state paths
- No cheat directory defined

---

## III. CHEAT SYSTEM IMPLEMENTATION PLAN FOR LEMUROID

### Phase 1: Core Data Model (Week 1)

#### 1.1 Database Schema

**File:** `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/entity/Cheat.kt`

```kotlin
@Entity(tableName = "cheats")
data class Cheat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameId: String,              // Foreign key to Game.id
    val code: String,                // Full cheat code (hex pair)
    val description: String,         // Cheat name
    val enabled: Boolean = false,
    val codeType: String = "cwcheat", // "cwcheat", "gameshark", etc
    val orderIndex: Int = 0,         // For ordering in UI
    val lineNumber: Int = 0,         // File line number (for rebuild)
)

@Entity(tableName = "cheat_imports")
data class CheatImport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceFile: String,          // Original import file path
    val gameId: String,              // Which game was imported
    val timestamp: Long = System.currentTimeMillis(),
    val cheatCount: Int = 0,
)
```

**File:** `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/dao/CheatDao.kt`

```kotlin
@Dao
interface CheatDao {
    @Query("SELECT * FROM cheats WHERE gameId = :gameId ORDER BY orderIndex")
    suspend fun getCheatsForGame(gameId: String): List<Cheat>
    
    @Query("SELECT * FROM cheats WHERE gameId = :gameId AND enabled = 1")
    suspend fun getEnabledCheatsForGame(gameId: String): List<Cheat>
    
    @Insert
    suspend fun insertCheat(cheat: Cheat): Long
    
    @Insert
    suspend fun insertCheats(cheats: List<Cheat>)
    
    @Update
    suspend fun updateCheat(cheat: Cheat)
    
    @Delete
    suspend fun deleteCheat(cheat: Cheat)
    
    @Query("DELETE FROM cheats WHERE gameId = :gameId")
    suspend fun deleteCheatsForGame(gameId: String)
    
    @Query("UPDATE cheats SET enabled = :enabled WHERE id = :id")
    suspend fun setCheatEnabled(id: Int, enabled: Boolean)
}
```

**Update:** `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/library/db/RetrogradeDatabase.kt`

```kotlin
@Database(
    entities = [
        Game::class,
        DataFile::class,
        Cheat::class,           // ADD THIS
        CheatImport::class,     // ADD THIS
    ],
    version = 10,  // Increment from current
    exportSchema = true,
)
abstract class RetrogradeDatabase : RoomDatabase() {
    abstract fun cheatDao(): CheatDao  // ADD THIS
}
```

#### 1.2 Directory Management

**File:** `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/storage/DirectoriesManager.kt`

Add cheat directory (add to existing enum):
```kotlin
fun getCheatsDirectory(): File {
    return File(getAppDataDirectory(), "cheats")
}
```

### Phase 2: Cheat File Parsing (Week 2)

#### 2.1 CwCheat Format Parser

**File:** `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/CwCheatParser.kt`

```kotlin
data class CwCheatFile(
    val gameId: String,
    val cheats: List<ParsedCheat>,
    val errors: List<String>,
)

data class ParsedCheat(
    val name: String,
    val enabled: Boolean,
    val lines: List<String>,  // Raw hex lines as strings
    val lineNumber: Int,
)

class CwCheatParser {
    fun parseCheatFile(content: String, targetGameId: String? = null): CwCheatFile {
        val cheats = mutableListOf<ParsedCheat>()
        val errors = mutableListOf<String>()
        val lines = content.lines()
        
        var currentGameId: String? = null
        var currentCheat: MutableList<String>? = null
        var currentCheatName = ""
        var currentCheatEnabled = false
        var currentLineNum = 0
        
        for ((index, line) in lines.withIndex()) {
            val lineNum = index + 1
            val trimmed = line.trim()
            
            when {
                trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#") -> {
                    // Skip comments and blank lines
                }
                trimmed.startsWith("_S ") -> {
                    // Game section
                    flushCurrentCheat()
                    currentGameId = extractGameId(trimmed)
                }
                trimmed.startsWith("_G ") -> {
                    // Game title - ignore
                }
                trimmed.startsWith("_C") -> {
                    // Cheat header: _C0 name or _C1 name
                    flushCurrentCheat()
                    val enabled = trimmed[2] == '1'
                    currentCheatName = if (trimmed.length > 4) trimmed.substring(4) else "Unnamed"
                    currentCheatEnabled = enabled
                    currentCheat = mutableListOf()
                    currentLineNum = lineNum
                }
                trimmed.startsWith("_L ") -> {
                    // Cheat data line
                    if (currentCheat != null) {
                        currentCheat.add(trimmed.substring(3))
                    } else {
                        errors.add("Line $lineNum: _L without _C")
                    }
                }
                else -> {
                    errors.add("Line $lineNum: Unrecognized format")
                }
            }
        }
        flushCurrentCheat()
        
        return CwCheatFile(currentGameId ?: "", cheats, errors)
    }
    
    private fun flushCurrentCheat() {
        if (currentCheat != null && currentCheat.isNotEmpty()) {
            cheats.add(ParsedCheat(
                name = currentCheatName,
                enabled = currentCheatEnabled,
                lines = currentCheat.toList(),
                lineNumber = currentLineNum,
            ))
        }
        currentCheat = null
    }
    
    private fun extractGameId(line: String): String {
        // "_S ABCD-1234" or "_S ABCD1234"
        return line.substring(3).trim()
    }
}
```

#### 2.2 Multi-line Cheat Handling

The parser above already handles multi-line cheats by storing all `_L` lines between `_C` headers.

### Phase 3: Zip File Import (Week 2-3)

#### 3.1 Zip Extraction

**File:** `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/ZipCheatExtractor.kt`

```kotlin
class ZipCheatExtractor {
    suspend fun extractCheatFilesFromZip(
        zipPath: String,
        targetDirectory: File
    ): List<File> {
        val extractedFiles = mutableListOf<File>()
        
        ZipFile(File(zipPath)).use { zip ->
            for (entry in zip.entries()) {
                // Extract .cht, .txt files that look like cheats
                if (entry.isDirectory) continue
                if (!entry.name.endsWith(".cht") && 
                    !entry.name.endsWith(".txt") &&
                    !entry.name.endsWith(".db")) continue
                
                val outputFile = File(targetDirectory, entry.name)
                outputFile.parentFile?.mkdirs()
                
                zip.getInputStream(entry).use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                extractedFiles.add(outputFile)
            }
        }
        return extractedFiles
    }
}
```

#### 3.2 System Detection

**File:** `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/SystemDetector.kt`

```kotlin
class SystemDetector {
    fun detectSystemFromCheatFile(
        gameId: String,
        cheatContent: String
    ): SystemID? {
        // Parse cheat file to extract game ID
        val parser = CwCheatParser()
        val parsed = parser.parseCheatFile(cheatContent)
        
        // Try to match game ID to known systems
        return when {
            gameId.matches(Regex(".*[Gg][Bb].*")) -> SystemID.GB
            gameId.matches(Regex(".*[Gg][Bb][Cc].*")) -> SystemID.GBC
            gameId.matches(Regex(".*[Gg][Bb][Aa].*")) -> SystemID.GBA
            // Check library database
            else -> libraryDatabase.getGameSystem(gameId)
        }
    }
}
```

### Phase 4: LibRetroDroid Integration (Week 3-4)

#### 4.1 Investigation Required

**Critical question:** Does LibretroDroid expose cheat APIs?

Need to check:
1. `GLRetroViewData` class - does it have cheat methods?
2. LibRetro standard: `retro_cheat_reset()`, `retro_cheat_set()`
3. If not exposed: implement via JNI or fork LibretroDroid

**Tentative approach (assuming APIs exist):**

```kotlin
// File: lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/game/viewmodel/GameViewModelCheats.kt

class GameViewModelCheats(
    private val retroGameView: GLRetroView?,
    private val cheatDao: CheatDao,
    private val gameId: String,
    private val scope: CoroutineScope,
) {
    fun loadAndApplyCheats() {
        scope.launch {
            val enabledCheats = cheatDao.getEnabledCheatsForGame(gameId)
            
            // Reset all cheats
            retroGameView?.resetCheats()
            
            // Apply enabled cheats
            enabledCheats.forEachIndexed { index, cheat ->
                retroGameView?.setCheat(index, true, cheat.code)
            }
        }
    }
    
    fun toggleCheat(cheatId: Int, enabled: Boolean) {
        scope.launch {
            cheatDao.setCheatEnabled(cheatId, enabled)
            loadAndApplyCheats()  // Reload
        }
    }
}
```

**Alternative (JNI if not exposed):**

```kotlin
// Create JNI binding to LibRetro core
// native/cheats.c
extern "C" {
    void Java_com_swordfish_lemuroid_LibretroCheats_resetCheats(JNIEnv *env, jobject obj) {
        retro_cheat_reset();
    }
    
    void Java_com_swordfish_lemuroid_LibretroCheats_setCheat(
        JNIEnv *env, jobject obj,
        jint index, jboolean enabled, jstring code
    ) {
        const char *code_str = (*env)->GetStringUTFChars(env, code, NULL);
        retro_cheat_set(index, enabled, code_str);
        (*env)->ReleaseStringUTFChars(env, code, code_str);
    }
}
```

### Phase 5: Persistence (Week 4)

#### 5.1 Database Persistence

When user enables/disables cheats in game menu:

```kotlin
suspend fun updateCheatState(cheat: Cheat, enabled: Boolean) {
    cheatDao.setCheatEnabled(cheat.id, enabled)
    // Immediately reload in LibRetro
    gameViewModel.toggleCheat(cheat.id, enabled)
}
```

#### 5.2 File Persistence (Optional)

To sync database back to cheat file:

```kotlin
suspend fun rebuildCheatFile(gameId: String) {
    val cheats = cheatDao.getCheatsForGame(gameId)
    val game = gameLibrary.getGame(gameId)
    
    val sb = StringBuilder()
    sb.append("_S $gameId\n")
    sb.append("_G ${game.name}\n\n")
    
    cheats.forEachIndexed { _, cheat ->
        val enabled = if (cheat.enabled) "1" else "0"
        sb.append("_C$enabled ${cheat.description}\n")
        
        cheat.lines.forEach { line ->
            sb.append("_L $line\n")
        }
    }
    
    File(cheatDirectory, "$gameId.cht").writeText(sb.toString())
}
```

### Phase 6: UI Integration (Week 4-5)

#### 6.1 Import Menu

**File:** `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/settings/SettingsScreen.kt`

Add to settings:
```kotlin
LemuroidSettingsMenuLink(
    title = { Text("Import Cheats") },
    onClick = { navController.navigate(SettingsRoute.CHEAT_IMPORT) },
)
```

#### 6.2 Cheat Import Screen

**File:** `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/cheats/CheatImportScreen.kt`

```kotlin
@Composable
fun CheatImportScreen(viewModel: CheatImportViewModel) {
    Column(Modifier.fillMaxSize()) {
        // Button to pick zip file
        Button(onClick = { /* Launch file picker */ }) {
            Text("Select Cheat ZIP File")
        }
        
        // Show progress
        if (viewModel.isImporting.value) {
            CircularProgressIndicator()
        }
        
        // Show results
        LazyColumn {
            items(viewModel.importResults.value) { result ->
                ImportResultItem(result)
            }
        }
    }
}
```

#### 6.3 In-Game Cheat Menu

**File:** Modify `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/gamemenu/GameMenuHomeScreen.kt`

Add:
```kotlin
LemuroidSettingsMenuLink(
    title = { Text("Cheats") },
    onClick = { navController.navigateToRoute(GameMenuRoute.CHEATS) },
)
```

#### 6.4 Cheat Selection Screen

**File:** `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/gamemenu/GameMenuCheatsScreen.kt`

```kotlin
@Composable
fun GameMenuCheatsScreen(gameId: String, viewModel: GameMenuViewModel) {
    val cheats = viewModel.getCheatsForGame(gameId).collectAsState()
    
    LazyColumn {
        items(cheats.value) { cheat ->
            Row {
                Checkbox(
                    checked = cheat.enabled,
                    onCheckedChange = { enabled ->
                        viewModel.updateCheatState(cheat, enabled)
                    }
                )
                Text(cheat.description)
            }
        }
    }
}
```

---

## IV. DETAILED IMPLEMENTATION STEPS

### Step 1: Setup & Dependencies

```bash
# Add to lemuroid-app/build.gradle.kts
dependencies {
    implementation("org.apache.commons:commons-compress:1.21")  // For zip
}
```

### Step 2: Data Model Creation

1. Create `Cheat.kt` entity
2. Create `CheatDao.kt` interface
3. Update `RetrogradeDatabase.kt` (version 10)
4. Create migration if needed

### Step 3: Parser Implementation

1. Implement `CwCheatParser.kt` with comprehensive testing
2. Test with multiple cheat file formats
3. Add error handling

### Step 4: File Operations

1. Implement `ZipCheatExtractor.kt`
2. Implement `SystemDetector.kt`
3. Test with multi-system cheat archives

### Step 5: LibRetroDroid Investigation

1. Check if `GLRetroView` has cheat methods
2. If not, implement JNI wrapper
3. Test with mGBA core (GBA)
4. Test with Gambatte core (GB/GBC)

### Step 6: Game Integration

1. Create `GameViewModelCheats.kt`
2. Integrate into `GameViewModelRetroGameView.kt`
3. Add cheat loading on game start

### Step 7: UI Development

1. Create `CheatImportScreen.kt`
2. Create `GameMenuCheatsScreen.kt`
3. Add navigation routing
4. Add file picker integration

### Step 8: Testing

1. Unit tests for parser
2. Integration tests for database
3. E2E tests with actual games

---

## V. COMPARISON: PPSSPP vs LEMUROID APPROACH

| Aspect | PPSSPP | Lemuroid |
|--------|--------|----------|
| **Database** | File-based (.txt) | Room database + file |
| **Parser** | C++ CheatFileParser | Kotlin parser |
| **Game ID** | PSP disc ID (9 chars) | Game filename hash |
| **Execution** | Direct memory access | Via LibRetro API |
| **UI** | Custom C++ UI | Jetpack Compose |
| **Zip handling** | Manual (not built-in) | Apache Commons Compress |
| **Multi-system** | Single system (PSP) | Three systems (GB/GBC/GBA) |

---

## VI. KEY DIFFERENCES & CONSIDERATIONS

### 1. System Detection Challenge

**PPSSPP:** PSP games have fixed ID format (`ABCD-1234`)  
**Lemuroid:** GB/GBC/GBA games identified by filename/CRC

**Solution:** Store game ID in database, match when importing

### 2. Multi-System Cheats

**Challenge:** One zip might have cheats for GB + GBC + GBA

**Solution:** 
- Extract all files
- For each file, parse to find game IDs
- Show user which systems each cheat is for
- Let user select which to import

Example UI:
```
┌─────────────────────────────────────┐
│ Import Results                      │
├─────────────────────────────────────┤
│ ✓ Pokemon Red (GB) - 15 cheats     │
│ ✓ Pokemon Gold (GBC) - 12 cheats   │
│ ✓ Pokemon Ruby (GBA) - 18 cheats   │
├─────────────────────────────────────┤
│ [Import] [Cancel]                   │
└─────────────────────────────────────┘
```

### 3. LibRetro Code Format

**Standard LibRetro cheat format:**
```
cheats = 2

cheat0_desc = "Infinite Health"
cheat0_code = "820241A0 03E7"
cheat0_enable = false

cheat1_desc = "Max Gold"
cheat1_code = "2800412A 000000FF"
cheat1_enable = false
```

**Note:** Different from PPSSPP's CwCheat format!

**Your requirement:** This exact format (with multi-line support)

**Parser update needed:**
```kotlin
// Also support LibRetro format
class LibRetroCheatParser {
    fun parseCheatFile(content: String): Map<String, List<ParsedCheat>> {
        // cheats = 2
        // cheat0_desc = "name"
        // cheat0_code = "code"
        // cheat0_enable = false
    }
}
```

### 4. Game ID Matching Problem

**PPSSPP:** Cheats have hardcoded game ID in header  
**Lemuroid:** Games identified by filename, not ID in cheat file

**Solution Needed:**
1. When importing zip with cheats:
   - Parse cheat file game ID
   - Search library for matching game
   - Show user list of possible matches
   - Let user confirm

```kotlin
// Example
val cheatGameId = "POKEMON_BLUE"  // From cheat file
val matchingGames = gameLibrary.searchByTitle("Pokemon Blue")
// Show UI for user to select correct game
```

### 5. Persistence Strategy

**PPSSPP:** Modifies `.txt` file directly (append new cheats)  
**Lemuroid:** Use database

**Benefits of DB approach:**
- Faster queries
- Better structure
- Separation of concerns
- Can rebuild files if needed

---

## VII. POTENTIAL BLOCKERS & SOLUTIONS

### Blocker 1: LibRetroDroid Cheat API

**Problem:** LibRetroDroid might not expose `retro_cheat_*` functions

**Investigation steps:**
1. Clone LibretroDroid repo
2. Search for "cheat" in source
3. Check GLRetroView class methods
4. Check if JNI bindings exist

**Solutions if not exposed:**
- Fork LibretroDroid (high effort)
- Use JNI to call libretro directly
- Patch binary at runtime (not recommended)

### Blocker 2: Game Identification

**Problem:** GB/GBC/GBA games don't have fixed format IDs

**Solutions:**
1. Use game filename as ID
2. Use CRC32 if available
3. Store game UUID in database
4. Manual user mapping UI

### Blocker 3: Different Cheat Code Formats

**Problem:** GB/GBC/GBA use different cheat systems
- GB/GBC: Game Genie, Game Boy cheats
- GBA: Game Boy Advance codes, Action Replay

**Solution:**
- Parse and store as raw hex codes
- Let LibRetro cores handle format translation
- Store `codeType` field for future expansion

---

## VIII. ARCHITECTURE DIAGRAM

```
┌─────────────────────────────────────────────────────────────┐
│                     User                                     │
└────────────┬────────────────────────────────────────────────┘
             │
    ┌────────▼────────────────────────────────────────┐
    │        UI Layer (Compose)                       │
    ├──────────────────────────────────────────────────┤
    │ • CheatImportScreen (zip selection)             │
    │ • GameMenuCheatsScreen (enable/disable)         │
    │ • CheatImportProgressScreen                     │
    └────────┬────────────────────────────────────────┘
             │
    ┌────────▼────────────────────────────────────────┐
    │   ViewModel / Repository Layer                  │
    ├──────────────────────────────────────────────────┤
    │ • CheatRepository                               │
    │   - importFromZip()                             │
    │   - getCheatsForGame()                          │
    │   - updateCheatState()                          │
    │ • GameViewModelCheats                           │
    └────────┬────────────────────────────────────────┘
             │
    ┌────────┴───────┬────────────────┬───────────────┐
    │                │                │               │
┌───▼────────┐  ┌───▼───────┐  ┌──────▼───┐  ┌──────▼──┐
│  Room DB   │  │  Parsers  │  │ File I/O │  │ LibRetro│
├────────────┤  ├───────────┤  ├──────────┤  ├─────────┤
│ CheatDao   │  │ CwCheat   │  │ZipExtract│  │setCheat │
│CheatImport │  │ LibRetro  │  │FileOps   │  │resetCheat
└────────────┘  │ System    │  │Detector  │  └─────────┘
                │Detector   │  │          │
                └───────────┘  └──────────┘
```

---

## IX. FILE STRUCTURE TO CREATE

```
retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/
├── entity/
│   ├── Cheat.kt
│   └── CheatImport.kt
├── dao/
│   └── CheatDao.kt
├── parser/
│   ├── CwCheatParser.kt
│   └── LibRetroCheatParser.kt
├── importer/
│   ├── ZipCheatExtractor.kt
│   ├── SystemDetector.kt
│   └── CheatImporter.kt
└── repository/
    └── CheatRepository.kt

lemuroid-app/src/main/java/com/swordfish/lemuroid/app/
├── shared/game/viewmodel/
│   └── GameViewModelCheats.kt
└── mobile/feature/
    ├── cheats/
    │   ├── CheatImportScreen.kt
    │   ├── CheatImportViewModel.kt
    │   └── GameMenuCheatsScreen.kt
    └── gamemenu/
        └── GameMenuRoute.kt (update)
```

---

## X. TESTING STRATEGY

### Unit Tests
```kotlin
// tests/cheats/CwCheatParserTest.kt
class CwCheatParserTest {
    @Test
    fun `parse simple single-line cheat`()
    
    @Test
    fun `parse multi-line cheat`()
    
    @Test
    fun `parse cheat file with multiple games`()
    
    @Test
    fun `handle missing _S section gracefully`()
    
    @Test
    fun `detect enabled vs disabled cheats`()
}
```

### Integration Tests
```kotlin
// tests/cheats/CheatDatabaseTest.kt
class CheatDatabaseTest {
    @Test
    fun `insert cheat and retrieve`()
    
    @Test
    fun `update cheat enabled state`()
    
    @Test
    fun `get enabled cheats only`()
}
```

### E2E Tests
```kotlin
// tests/cheats/CheatIntegrationTest.kt
class CheatIntegrationTest {
    @Test
    fun `import zip file with multi-system cheats`()
    
    @Test
    fun `apply cheats to running game and verify execution`()
}
```

---

## XI. ESTIMATED TIMELINE

| Phase | Task | Days | Notes |
|-------|------|------|-------|
| 1 | Database schema + migrations | 2 | May require version bump |
| 2 | Parser implementation | 3 | Comprehensive testing needed |
| 3 | Zip extraction | 2 | Handle edge cases |
| 4 | LibRetroDroid investigation | 2-5 | **Major dependency** |
| 5 | Persistence layer | 2 | Update cheat states in DB |
| 6 | UI screens | 5 | Two screens + file picker |
| 7 | Integration testing | 4 | Test with real games |
| 8 | Bugfixes & polish | 3 | |
| **TOTAL** | | **23-28 days** | **~4-6 weeks** |

**Critical Path:** LibRetroDroid investigation (can block Phase 4-6)

---

## XII. NEXT STEPS

1. **Verify LibRetroDroid API availability**
   - Clone: https://github.com/Swordfish90/LibretroDroid
   - Search for cheat methods
   - Check version 0.13.2 (current in Lemuroid)

2. **Finalize game ID strategy**
   - Decide: filename vs CRC vs UUID
   - Plan UI for manual mapping if needed

3. **Confirm cheat code format**
   - Verify: user wants LibRetro format (not CwCheat)
   - Test with real cheat files

4. **Begin implementation**
   - Start with database layer
   - Parser implementation
   - Can proceed in parallel while investigating LibRetroDroid

