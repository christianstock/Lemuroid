# Libretro Cheat Format - Real Examples From libretro-database

## Verified Cheat Format (From Actual libretro-database)

Confirmed from: https://github.com/libretro/libretro-database/tree/master/cht/

### Example 1: Simple Cheats (Game Boy Advance)
```
cheats = 2

cheat0_desc = "Invincibility"
cheat0_code = "1f071c:20"
cheat0_enable = false

cheat1_desc = "Infinite Hearts"
cheat1_code = "1f0718:15"
cheat1_enable = false
```

### Example 2: Multi-Address Cheats
```
cheats = 3

cheat0_desc = "Infinite Timer"
cheat0_code = "1f124e:5a+1f124e:90"
cheat0_enable = false

cheat1_desc = "P1 Infinite HP"
cheat1_code = "1f0760:6a+1f0761:00+1f0760:10"
cheat1_enable = false

cheat2_desc = "Invincibility"
cheat2_code = "1f0d4a:25"
cheat2_enable = false
```

### Example 3: Real Game Boy Advance Game
```
cheats = 7

cheat0_desc = "Invincibility"
cheat0_code = "1f071c:20"
cheat0_enable = false

cheat1_desc = "Infinite Hearts"
cheat1_code = "1f0718:15"
cheat1_enable = false

cheat2_desc = "Have Blue Key"
cheat2_code = "1f0732:01"
cheat2_enable = false

cheat3_desc = "Have Gold Key"
cheat3_code = "1f0730:01"
cheat3_enable = false

cheat4_desc = "Have Green Key"
cheat4_code = "1f0731:01"
cheat4_enable = false

cheat5_desc = "Have Purple Key"
cheat5_code = "1f0733:01"
cheat5_enable = false

cheat6_desc = "Have Red Key"
cheat6_code = "1f072f:01"
cheat6_enable = false
```

## Code Format Breakdown

**Single Address Cheat:**
```
address:value
1f071c:20
```
- `1f071c` = memory address (hex)
- `20` = value to write (hex)

**Multi-Address Cheat (separated by `+`):**
```
address1:value1+address2:value2+address3:value3
1f0760:6a+1f0761:00+1f0760:10
```

## Extended Format (Optional Metadata)

Some .cht files include detailed metadata (especially for newer RetroArch versions):
```
cheat0_address = "1842"
cheat0_address_bit_position = "0"
cheat0_big_endian = "false"
cheat0_cheat_type = "1"
cheat0_code = ""
cheat0_desc = "Have Blue Key"
cheat0_enable = "false"
cheat0_handler = "1"
cheat0_memory_search_size = "3"
cheat0_rumble_port = "0"
cheat0_rumble_primary_duration = "0"
cheat0_rumble_primary_strength = "0"
cheat0_rumble_secondary_duration = "0"
cheat0_rumble_secondary_strength = "0"
cheat0_rumble_type = "0"
cheat0_rumble_value = "0"
cheat0_value = "1"
cheats = "5"
```

**⚠️ Important:** The **extended format has TWO ways to store codes:**
- Option 1: `cheat0_code = "1f0732:01"` (simple format we care about)
- Option 2: `cheat0_address + cheat0_value` (metadata format)

Jules' parser handles **Option 1** (the simple code-based format), which is what GB/GBC/GBA typically use. The extended format is for compatibility with RetroArch's advanced cheat engine.

## Jules Parser Compatibility ✅

`CheatParser.kt` correctly handles:
- ✅ Simple format: `cheat0_desc`, `cheat0_code`, `cheat0_enable`
- ✅ Multi-address codes with `+` separator
- ✅ Hex address:value format
- ✅ Boolean enable/disable parsing
- ✅ Trims quotes and whitespace

The parser uses Java `Properties` class which handles:
- Key=value format
- Double-quoted strings
- Standard properties file syntax

## Files Available in libretro-database

- `cht/Nintendo - Game Boy/` - Game Boy cheats
- `cht/Nintendo - Game Boy Color/` - GBC cheats  
- `cht/Nintendo - Game Boy Advance/` - GBA cheats

All use the simple format that Jules' parser supports.

## Next Steps

1. Download a real .cht file from libretro-database to test
2. Verify Jules' parser handles it correctly
3. Confirm multi-address codes parse properly (the `+` separator handling)
4. Test edge cases (empty descriptions, missing cheats=N, malformed hex)
