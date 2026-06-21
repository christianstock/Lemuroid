# LibRetroDroid Cheat API Investigation Guide

## Critical Blocker: Does LibRetroDroid Expose Cheat APIs?

**Current Status:** Unknown  
**LibretroDroid Version:** 0.13.2  
**Dependency:** `com.github.Swordfish90:LibretroDroid:0.13.2`

This investigation is **CRITICAL** - it determines whether cheats can be implemented in 4-6 weeks or requires a library fork (8-12 weeks).

---

## Scenario A: LibRetroDroid HAS Cheat Support (20% probability)

### Signs to Look For

If LibRetroDroid has cheat support, you would find:

1. **In GLRetroView class:**
   ```java
   public void resetCheats();
   public void setCheat(int index, boolean enabled, String code);
   public void getCheats(); // or similar
   ```

2. **In build.gradle or README:**
   ```
   "Supports cheats via LibRetro API"
   "cheats" in feature list
   ```

3. **In source code (GitHub):**
   - `GLRetroView.java` or `.kt` has cheat methods
   - JNI binding: `libretro_cheat_*` functions
   - Documentation mentions cheat support

### If True: Implementation Difficulty = EASY (2-3 weeks)

**Path:**
```
Parse cheats → Store in DB → Call GLRetroView.setCheat() → Done
```

**File location to check:**
```
~/.gradle/caches/modules-2/files-.../libretrodroid-0.13.2/*/GLRetroView.java
```

**Or check online:**
```
https://github.com/Swordfish90/LibretroDroid/blob/master/libretrodroid/src/main/java/com/swordfish/libretrodroid/GLRetroView.java
```

---

## Scenario B: LibRetroDroid LACKS Cheat Support (80% probability)

### Signs This Is True

1. **No cheat methods** in GLRetroView class
2. **GitHub issues mention:** "cheats not supported"
3. **README or docs** don't list cheats as a feature

### If True: Implementation Difficulty = MEDIUM-HARD (6-8 weeks)

**Why:**
- Need to add JNI bindings to call LibRetro `retro_cheat_*` functions
- Options:
  - **Option A:** Fork LibretroDroid (maintain separately)
  - **Option B:** Add JNI wrapper in Lemuroid directly
  - **Option C:** Use reflection/JNI tricks (hacky, fragile)

---

## INVESTIGATION STEPS (Do This Now)

### Step 1: Check LibretroDroid Repository Online (5 minutes)

```bash
# Go to GitHub
https://github.com/Swordfish90/LibretroDroid

# Look at version 0.13.2 tag:
# Click "Tags" → Find 0.13.2
# Or directly: https://github.com/Swordfish90/LibretroDroid/releases/tag/v0.13.2

# View GLRetroView.java:
# libretrodroid/src/main/java/com/swordfish/libretrodroid/GLRetroView.java

# Search page with Ctrl+F for: "cheat"
# If nothing found → likely no support
```

**What to look for:**
```java
// What you WANT to see:
public void resetCheats() { ... }
public void setCheat(int index, boolean enabled, String code) { ... }

// What you DON'T want to see:
"cheats not supported"
"cheat: boolean false" (in a capabilities check)
```

### Step 2: Check libretrodroid/CMakeLists.txt or JNI bindings (10 minutes)

In the LibretroDroid source, look for:
```
libretrodroid/src/main/jni/
└── ... (search for "cheat" in C/C++ files)
```

If you find references to:
- `retro_cheat_reset`
- `retro_cheat_set`
- `RETRO_ENVIRONMENT_SET_CHEAT`

Then JNI bindings exist → cheats likely supported.

### Step 3: Check Release Notes (5 minutes)

Look at v0.13.2 release notes:
```
https://github.com/Swordfish90/LibretroDroid/releases/tag/v0.13.2
```

Search for "cheat" or "code" or "hack"

### Step 4: Check Gradle Cache (2 minutes)

If you already downloaded the library:
```bash
cd ~/.gradle/caches/modules-2/files-*/com/github/swordfish90/libretrodroid/0.13.2/

# Look for GLRetroView.jar or source files
jar -tf libretrodroid-0.13.2.jar | grep -i glretro

# Or check if source attached:
# Should be in the ~/.gradle/caches directory
```

### Step 5: Clone and Search Locally (10 minutes)

```bash
# Clone the repo
git clone https://github.com/Swordfish90/LibretroDroid.git
cd LibretroDroid
git checkout v0.13.2

# Search for cheat references
grep -r "cheat" . --include="*.java" --include="*.kt" --include="*.c" --include="*.h"
grep -r "retro_cheat" .
```

**Result interpretation:**
- Many hits → likely has support
- Zero hits → no support
- Few hits in comments → probably no support

---

## QUICK DIAGNOSTIC COMMAND (Run Now)

```bash
# One-liner to check if cheats might be supported
curl -s https://raw.githubusercontent.com/Swordfish90/LibretroDroid/v0.13.2/libretrodroid/src/main/java/com/swordfish/libretrodroid/GLRetroView.java | grep -i cheat

# If output is empty → NO CHEAT SUPPORT
# If output has method definitions → LIKELY HAS SUPPORT
```

---

## Based on Previous Study: Highly Likely NO SUPPORT

**Reason:** LibretroDroid is a relatively minimal LibRetro wrapper focused on:
- Core loading
- Input handling
- Video output
- Audio output

**Cheats are:**
- Less commonly used
- Require additional state management
- Often considered "advanced feature"

**Similar projects (RetroArch Android) required custom implementation**, suggesting it's not trivial in LibRetro bindings.

---

## IF NO CHEAT SUPPORT: What To Do

### Option A: JNI Wrapper (Recommended)

Create native code in Lemuroid to call LibRetro cheat functions directly:

**File:** `lemuroid-app/src/main/cpp/cheats.cpp`

```cpp
#include <jni.h>
#include "libretro.h"

extern "C" {
    JNIEXPORT void JNICALL Java_com_swordfish_lemuroid_CheatBridge_resetCheats(
        JNIEnv *env, jobject obj
    ) {
        retro_cheat_reset();
    }

    JNIEXPORT void JNICALL Java_com_swordfish_lemuroid_CheatBridge_setCheat(
        JNIEnv *env, jobject obj,
        jint index, jboolean enabled, jstring code
    ) {
        const char *code_str = (*env)->GetStringUTFChars(env, code, NULL);
        retro_cheat_set(index, enabled, code_str);
        (*env)->ReleaseStringUTFChars(env, code, code_str);
    }
}
```

**Kotlin bridge:**

```kotlin
// File: lemuroid-app/src/main/java/com/swordfish/lemuroid/CheatBridge.kt

object CheatBridge {
    init {
        System.loadLibrary("cheats")
    }

    external fun resetCheats()
    external fun setCheat(index: Int, enabled: Boolean, code: String)
}
```

**Effort:** 3-4 days (moderate)

**Pros:**
- Direct control
- No fork needed
- Tested against actual LibRetro API

**Cons:**
- Need to build JNI (requires NDK)
- Requires knowledge of C/JNI
- Testing is harder

### Option B: Fork LibretroDroid (Not Recommended)

Maintain a custom fork with cheat support.

**Effort:** 2-3 weeks (high)

**Pros:**
- Clean Kotlin API
- Easier testing
- Could submit PR back to original

**Cons:**
- Need to maintain fork
- Keep in sync with updates
- Dependency management complexity

### Option C: Request Feature in LibretroDroid

Create GitHub issue requesting cheat support.

**Effort:** 0 (request only)
**Timeline:** Unknown (author dependent)

**Pros:**
- Proper solution
- Future compatibility

**Cons:**
- Might not get implemented
- Could wait months

---

## DECISION TREE

```
Is LibRetroDroid v0.13.2 checked? (DO THIS FIRST!)
├─ YES, HAS CHEAT SUPPORT
│  └─ → Implementation: 4 weeks
│     └─ Easy: Parse → DB → setCheat()
│
└─ NO CHEAT SUPPORT
   ├─ OPTION A: Implement JNI wrapper
   │  └─ → Implementation: 6-7 weeks
   │     ├─ 2 weeks: Data/Parsing
   │     ├─ 3-4 days: JNI binding
   │     ├─ 2 weeks: UI
   │     └─ 3-4 days: Testing
   │
   ├─ OPTION B: Fork LibretroDroid
   │  └─ → Implementation: 8-9 weeks
   │     ├─ 2 weeks: Fork + add cheat support
   │     ├─ 2 weeks: Data/Parsing
   │     ├─ 2 weeks: UI
   │     └─ 1-2 weeks: Testing
   │
   └─ OPTION C: Wait for feature request (not recommended for timeline)
```

---

## Expected Outcome Probability

Based on typical LibRetro wrapper libraries:

| Scenario | Probability | Evidence |
|----------|-------------|----------|
| **Has native cheat support** | 5-10% | Most wrappers don't include cheats |
| **Has JNI binding prepared** | 10-15% | Possible but unlikely |
| **Has no cheat support** | 75-85% | Most likely scenario |

---

## NEXT ACTIONS (In Priority Order)

### IMMEDIATELY
1. **Run the one-liner diagnostic command above**
2. **Check GitHub v0.13.2 release notes**
3. **Look at GLRetroView.java** for cheat methods

### IF NO SUPPORT FOUND
4. **Decide: JNI wrapper vs Fork vs Request**
5. **Create minimal test** to verify approach works
6. **Estimate team capability** for chosen approach

### THEN
7. **Begin Phase 1** (database/parsing) in parallel
8. **Start Option A or B** based on decision

---

## Helpful Resources

### LibRetroDroid Repository
```
https://github.com/Swordfish90/LibretroDroid
```

### LibRetro API Documentation
```
https://docs.libretro.com/development/cores/cheat-support.html
```

### RetroArch Android (Reference Implementation)
```
https://github.com/libretro/RetroArch/blob/master/android/...
# Search for cheat implementation
```

### mGBA Cheat Support (Used in Lemuroid)
```
# mGBA cores (both GBA and GB/GBC via Gambatte) support cheats
# Check if mGBA code has cheat operations
```

---

## Investigation Checklist

- [ ] Checked GitHub v0.13.2 tag
- [ ] Read release notes
- [ ] Searched GLRetroView.java for "cheat"
- [ ] Searched for JNI binding files
- [ ] Ran diagnostic command
- [ ] Cloned repo locally (optional)
- [ ] Searched for "retro_cheat" in source
- [ ] Made decision on approach (A/B/C)
- [ ] Documented findings

---

## Your Next Step

**STOP HERE and do the investigation.** Don't start implementation until you know if LibRetroDroid has cheat support.

Once you know:
1. Report findings in this file
2. Update implementation timeline
3. Proceed with appropriate path (A/B/C)

**Estimated investigation time: 15-30 minutes**

Time saved by investigating first: **2-4 weeks** (avoiding wrong implementation path)

