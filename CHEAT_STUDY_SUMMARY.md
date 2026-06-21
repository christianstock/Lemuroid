# Lemuroid Cheat Integration - Executive Summary

## Key Findings

### 1. PPSSPP Uses CwCheat Format
```
_S GAME_ID
_G Game Title
_C0 Cheat Name (0=disabled, 1=enabled)
_L XXXX XXXX  (hex code, can be multi-line)
_C1 Another Cheat
_L YYYY YYYY
_L ZZZZ ZZZZ
```

**BUT** - Your requirement mentions this format with slight differences (cheats = count, cheat0_desc, cheat0_code). This is actually **LibRetro format**, not CwCheat.

### 2. Architecture Comparison

| Component | PPSSPP | Lemuroid Implementation |
|-----------|--------|------------------------|
| **Storage** | Text files (.txt) | Room Database + text backup |
| **Parser** | C++ class | Kotlin parser |
| **Format** | CwCheat | LibRetro (as you specified) |
| **Game ID** | Fixed PSP ID | Flexible (filename/hash/DB ID) |
| **Code Execution** | Direct memory ops | Via LibRetro API |
| **Multi-system** | N/A (PSP only) | GB/GBC/GBA |
| **Zip support** | File browser | Automatic extraction + detection |

### 3. Three Critical Blockers to Investigate

#### Blocker #1: LibRetroDroid Cheat API ⚠️ CRITICAL

**Question:** Does LibRetroDroid expose `retro_cheat_set()` and `retro_cheat_reset()` functions?

**Current Status:** Unknown - need to check
- Library version: 0.13.2
- If not exposed: must fork or use JNI

**Impact:** Blocks 50% of implementation

#### Blocker #2: Game Identification

**Challenge:** GB/GBC/GBA games don't have fixed ID format like PSP

**Options:**
1. Use game filename
2. Use CRC32 hash
3. Store UUID in database
4. Manual user mapping

**Recommendation:** Use combination of filename + CRC, with manual fallback

#### Blocker #3: Cheat Code Format Ambiguity

**Your requirement shows:**
```
cheats = 2
cheat0_desc = "Infinite Health"
cheat0_code = "820241A0 03E7"
cheat0_enable = false
```

**This is LibRetro format**, not CwCheat.

**For Lemuroid, we should:**
- Support both CwCheat and LibRetro formats
- Store as-is (don't parse hex codes)
- Let cores handle format translation

---

## Implementation Phases (4-6 weeks)

### Phase 1-2: Data & Parsing (Week 1-2)
- ✅ Create Room database tables
- ✅ Implement format parsers
- ✅ Zip extraction

### Phase 3: Integration (Week 2-4) ⚠️
- ⚠️ **INVESTIGATE LibRetroDroid API**
- Implement game ID mapping
- Connect to LibRetro core

### Phase 4-5: UI & Testing (Week 4-6)
- ✅ Import screen
- ✅ In-game cheat menu
- ✅ Enable/disable toggles

---

## What We Know (From PPSSPP Study)

### ✅ File Format
PPSSPP implementation shows:
- Simple text format
- Game ID matching
- Enable/disable tracking
- Multi-line cheat support
- Line number tracking (for updates)

### ✅ Parser Logic
```kotlin
for each line:
  if starts with "_S" -> parse game ID
  if starts with "_G" -> parse title
  if starts with "_C" -> parse cheat name + enabled state
  if starts with "_L" -> parse hex code
  track line numbers for later modification
```

### ✅ Execution Model
```kotlin
ParseCheats()     // Load from file
Run()             // Execute per frame
UpdateCheat()     // Enable/disable specific cheat
RebuildFile()     // Write changes back to disk
```

### ✅ Database Strategy
PPSSPP uses text files, but Lemuroid should use Room database for:
- Faster queries
- Better UI support
- Separation of concerns
- Easier enable/disable without file I/O

---

## What We DON'T Know (Need to Investigate)

### 1. LibRetroDroid Cheat Support

**Required investigation:**
```bash
# Check if these methods exist in GLRetroView
grep -r "resetCheats\|setCheat\|retro_cheat" LibretroDroid/
grep -r "CHEAT" LibretroDroid/src/main/jni/
```

**Alternative:** Look at how other emulators (RetroArch Android) handle this

### 2. Game ID Format for GB/GBC/GBA

**Required investigation:**
- How does Lemuroid currently identify games?
- Check Game.kt model
- Check game library database

### 3. Cheat Code Format Requirements

**Need clarification:**
- User specified LibRetro format - confirm?
- Multi-line codes - how are they joined?
- Support for conditionals/operations?

---

## Recommended Next Steps (Priority Order)

### IMMEDIATE (Today)
1. ✅ Read CHEAT_IMPLEMENTATION_STUDY.md fully
2. ⚠️ Investigate LibRetroDroid in the actual repo:
   ```bash
   cd /path/to/lemuroid-master
   grep -r "cheat\|CHEAT" .
   grep -r "retro_" build/
   ```
3. ⚠️ Check if mGBA/Gambatte cores have cheat support in their LibRetro wrappers

### SHORT TERM (This week)
4. Verify game identification strategy
5. Confirm exact cheat code format needed
6. Create detailed LibRetroDroid fork plan (if needed)

### START IMPLEMENTATION
7. Create database schema
8. Implement parsers
9. Build UI skeleton

---

## Open Questions for User

1. **Cheat Format Confirmation**
   - You specified the "cheats = " format - is this required?
   - Should we also support traditional CwCheat format for compatibility?

2. **Game Matching**
   - When importing cheats, if game ID doesn't match exactly, how should we handle it?
   - Should user manually select the game?
   - Or try fuzzy matching on game title?

3. **Code Behavior**
   - Do codes execute constantly?
   - Or only when enabled in menu?
   - Should they apply immediately or on next game load?

4. **Multi-system Zip Handling**
   - If user imports zip with GB + GBC + GBA cheats, show all and let them pick?
   - Or only show relevant ones?

5. **File Organization**
   - Where should imported cheats be stored?
   - One file per game?
   - One master file per system?

---

## Files Created

1. **[FEASIBILITY_STUDY.md](FEASIBILITY_STUDY.md)** - Overall feature study
2. **[CHEAT_IMPLEMENTATION_STUDY.md](CHEAT_IMPLEMENTATION_STUDY.md)** - Detailed implementation plan (this is the comprehensive one)

Both files contain:
- Architecture diagrams
- Code examples
- File structure
- Testing strategy
- Timeline estimates

---

## Branch Status

**Current branch:** `feature/cheat-support`

Ready to start implementation on any phase. Recommend starting with Phase 1 (database) while investigating LibRetroDroid API in parallel.

