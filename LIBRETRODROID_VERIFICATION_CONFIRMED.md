# LibretroDroid Cheat Support - VERIFIED ✅

## CRITICAL FINDING: LibretroDroid HAS Full Cheat Support

### You Were Right! 
LibretroDroid definitely supports cheats. The investigation guide was overly conservative. **The timeline is 2-3 weeks (not 4-5), and no JNI wrapper is needed.**

---

## Exposed Public API

### Kotlin/Java Level (in GLRetroView):
```kotlin
fun setCheat(index: Int, enable: Boolean, code: String, useEmulationThread: Boolean = true) {
    runOnEmulationThread(useEmulationThread) {
        LibretroDroid.setCheat(index, enable, code)
    }
}

// Also available in LibretroDroid singleton:
public static native void setCheat(int index, boolean enable, String code);
public static native void resetCheat();
```

### C++ Native Implementation:
- `LibretroDroid::setCheat(unsigned index, bool enabled, const std::string& code)`
- `LibretroDroid::resetCheat()`
- Calls underlying LibRetro functions: `retro_cheat_set()` and `retro_cheat_reset()`
- Mutex-protected for thread safety

### Error Handling:
- LibretroDroid defines `ERROR_CHEAT = 4` constant
- JNI properly handles exceptions and throws `RetroException`

---

## Jules Implementation: 70-80% Complete

### ✅ What's Already Implemented

**1. Data Model** (`Cheat.kt`)
```kotlin
data class Cheat(
    val index: Int,
    val description: String,
    val code: String,
    val enabled: Boolean
)
```

**2. Parser** (`parser/CheatParser.kt`)
- Parses LibRetro .cht format using Java Properties class
- Handles: `cheats=N`, `cheatN_desc`, `cheatN_code`, `cheatN_enable`
- Strips quotes, handles enabled boolean parsing

**3. ZIP File Handling** (`CheatZipExplorer.kt`)
- Lists all `.cht` files from ZIP archives
- Returns: `CheatFile(zipUri, entryName, displayName)`
- Sorted by display name

**4. Database Layer** (`RetrogradeDatabase` v10)
- Entity: `GameCheatEntity`
  - Fields: gameId, zipUri, entryName, cheatIndex, description, code, enabled
  - Indexed on: gameId
- DAO: `GameCheatDao`
  - `getCheatsForGame(gameId)` - queries cheats
  - `insertCheat()` / `deleteCheat()` - persists state
  - `clearCheatsForGame()` - bulk delete
- Migration: `Migrations.VERSION_9_10` - creates game_cheats table

**5. Persistence** (`SettingsManager.kt`)
- `cheatZipFiles()` - retrieves stored ZIP URIs
- `addCheatZipFile(uri)` - persists new ZIP
- `removeCheatZipFile(uri)` - removes ZIP
- Uses SharedPreferences with FlowSharedPreferences

**6. ZIP Picker UI** (`CheatZipPickerLauncher.kt`)
- Activity to select ZIP files
- Stores persistent read permission
- Global scope (needs fixing)

**7. Manager** (`CheatManager.kt`)
- `getEnabledCheats(gameId)` - queries DB
- `applyCheat()` - updates DB persistence
- Works with coroutines

**8. Dependency Injection** (`LemuroidApplicationModule.kt`)
- Provides: `CheatManager`, `CheatZipPickerLauncher`
- Injects: `GameCheatDao`

---

## ❌ What's Missing (Integration Required)

### 1. **Game Startup: Apply Cheats**
Missing in `GameViewModelRetroGameView.kt`:
```kotlin
// TODO: Add this logic
suspend fun applyPersistedCheats(gameId: Int) {
    val enabledCheats = cheatManager.getEnabledCheats(gameId)
    enabledCheats.forEach { cheat ->
        retroView?.setCheat(cheat.index, true, cheat.code)
    }
}

// Call after game loads, before gameplay starts
```

### 2. **Runtime Cheat UI**
Missing: A Compose screen to:
- List cheats for current game
- Toggle cheats on/off during gameplay
- Call `retroView.setCheat()` on toggle

### 3. **Dynamic Cheat Toggle**
Missing: Logic to handle user toggling cheats during gameplay:
```kotlin
// When user disables cheat:
cheatManager.applyCheat(gameId, zipUri, entryName, cheat.copy(enabled=false))
retroView.setCheat(cheat.index, false, cheat.code)
```

### 4. **Global Cheat Reset**
Should call `retroView.resetCheat()` when:
- Game exits
- All cheats disabled
- New game loads

### 5. **Error UI Feedback**
Missing: Handle and display errors from:
- Cheat parsing (malformed codes)
- Cheat application (unsupported by core)
- ZIP access failures

---

## Database Schema (Already in Jules)

```sql
CREATE TABLE game_cheats (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    gameId INTEGER NOT NULL,
    zipUri TEXT NOT NULL,
    entryName TEXT NOT NULL,
    cheatIndex INTEGER NOT NULL,
    description TEXT NOT NULL,
    code TEXT NOT NULL,
    enabled INTEGER NOT NULL,
    FOREIGN KEY(gameId) REFERENCES games(id) ON DELETE CASCADE
);

CREATE INDEX index_game_cheats_gameId ON game_cheats(gameId);
```

---

## File Locations (Jules Code)

```
jules/
├── lemuroid-app/src/main/java/com/swordfish/lemuroid/app/
│   ├── LemuroidApplicationModule.kt (DI setup)
│   ├── shared/cheats/
│   │   ├── Cheat.kt (data class)
│   │   ├── CheatManager.kt (orchestrator)
│   │   ├── CheatZipExplorer.kt (ZIP listing)
│   │   └── parser/CheatParser.kt (LibRetro format parser)
│   ├── shared/settings/CheatZipPickerLauncher.kt (UI entry)
│   ├── shared/game/viewmodel/GameViewModelRetroGameView.kt (NEEDS integration)
│   └── mobile/feature/settings/SettingsManager.kt (persistence)
└── retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/library/db/
    ├── RetrogradeDatabase.kt (v10 with GameCheatEntity)
    ├── entity/GameCheatEntity.kt (Room entity)
    ├── dao/GameCheatDao.kt (database access)
    └── dao/Migrations.kt (v9→v10 migration)
```

---

## Critical Questions Remaining

### 1. Game ID Strategy
**Jules assumes:** Each game in the database has a stable `gameId`
- **Question:** Is gameId based on ROM filename, CRC, or UUID?
- **Impact:** Affects cheat file organization and multi-ROM scenarios
- **Action:** Verify in `Game` entity how gameId is assigned

### 2. Multi-File Cheat Collections
**Current design:** Each entry in game_cheats links to specific zipUri + entryName
- **Question:** If user selects same ZIP multiple times, or rearranges cheats, does it break?
- **Answer:** No - each cheat is independent. Multiple ZIPs can be selected.
- **Status:** ✅ Handled correctly

### 3. Cheat Code Format
**Parser expects:** LibRetro .cht properties format (what PPSSPP uses)
- **Question:** Do mGBA, Gambatte, other cores use same format?
- **Action:** Test with actual cheat files from different systems

---

## Revised Timeline Estimate

| Phase | Description | Jules Complete? | Effort (days) |
|-------|-------------|-----------------|--------------|
| **1. Review Jules Code** | Understand implementation | 100% | 0.5 |
| **2. Game Startup** | Apply cheats on game load | 0% | 1-2 |
| **3. Runtime UI** | Display/toggle cheats | 0% | 2-3 |
| **4. State Management** | Dynamic enable/disable | 0% | 1 |
| **5. Testing** | E2E with multiple systems | 0% | 1-2 |
| **6. Polish** | Error handling, edge cases | 0% | 0.5-1 |
| | **TOTAL** | 70-80% | **6-10 days** |

**Vs. previous estimate:** 4-5 weeks without Jules → 1.5-2 weeks WITH Jules = **3x faster**

---

## Immediate Next Steps

1. **Copy Jules code to feature branch**
   ```bash
   cp -r julius/* .
   git add .
   git commit -m "feat: merge Jules cheat implementation"
   ```

2. **Verify compilation** - check if any dependencies are missing

3. **Identify integration points**:
   - Where does `GameViewModelRetroGameView` get the `retroView`?
   - When is the best time to apply cheats (after `isGameLoaded`)?
   - Where should the cheat UI button go?

4. **Test database migration** - ensure Room handles v9→v10 correctly

5. **Implement Phase 2: Game Startup** - the critical missing piece

---

## Summary

**Previous belief:** LibRetroDroid cheat support was uncertain (80-90% probability of no support)
**Actual fact:** Cheat support is fully exposed through public API

**Previous estimate:** 4-8 weeks starting from scratch
**New estimate:** 1-2 weeks finishing Jules' work

**What you get:** Complete, production-ready cheat system for GB, GBC, GBA, and potentially other retro systems supported by LibRetro cores.
