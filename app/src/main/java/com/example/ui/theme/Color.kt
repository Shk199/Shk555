package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// 1. خلفية التطبيق الرئيسية (Background): كحلي داكن جداً مائل للسواد (#111222)
val AppBackground = Color(0xFF111222)

// 2. خلفية البطاقات والقوائم (Cards & Panels): رمادي داكن مائل للبنفسجي (#22212A إلى #292735)
val CardBackground = Color(0xFF22212A)
val CardBackgroundVariant = Color(0xFF292735)

// 3. اللون البنفسجي الأساسي (Primary Purple): بنفسجي حيوي لإبراز العناصر كالبطاقات والأزرار المحددة (#8B42E8 أو #7C3AED)
val PrimaryPurple = Color(0xFF8B42E8)
val PrimaryPurpleVibrant = Color(0xFF7C3AED)
val PrimaryPurpleLight = Color(0xFFA76BFA)

// 4. اللون البنفسجي الهادئ (Muted Purple): بنفسجي خافت للتحديدات الفرعية (#372E54)
val MutedPurple = Color(0xFF372E54)
val MutedPurpleLight = Color(0xFF4C3F73)
val CardBorder = Color(0xFF372E54)

// 5. النصوص والأيقونات (Text & Icons): أبيض عاجي للنصوص الرئيسية (#FFFFFF) ورمادي فاتح للنصوص الفرعية (#A0A0AB)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA0A0AB)

// 6. اللمسات التفاعلية (Accents): أصفر ذهبي خفيف لأيقونات الشمس والرموز الحية (#F59E0B)
val AccentGold = Color(0xFFF59E0B)
val AccentGoldLight = Color(0xFFFBBF24)
val AccentGoldDark = Color(0xFFD97706)

// أحمر هادئ لزر تصفير العداد
val DarkRedReset = Color(0xFFE11D48)
val DarkRedResetHover = Color(0xFFF43F5E)
val DarkRedResetContainer = Color(0xFF3B1219)

// Aliases for seamless integration across existing UI screens
val EmeraldDark = CardBackground              // #22212A
val EmeraldDeep = CardBackgroundVariant       // #292735
val EmeraldSurface = MutedPurple              // #372E54
val EmeraldSurfaceVariant = MutedPurpleLight  // #4C3F73
val EmeraldGlow = PrimaryPurple               // #8B42E8
val EmeraldCardBorder = CardBorder            // #372E54

val ClassicGold = PrimaryPurple               // Primary interactive elements
val ClassicGoldLight = TextPrimary            // High contrast text #FFFFFF
val ClassicGoldDark = MutedPurple             // Subtle backgrounds
val GoldGlow = PrimaryPurpleLight             // Soft purple glow

// Theme Canvas bindings
val BackgroundDark = AppBackground            // #111222
val SurfaceDark = CardBackground              // #22212A
val SurfaceVariantDark = CardBackgroundVariant // #292735
val TextPrimaryDark = TextPrimary             // #FFFFFF
val TextSecondaryDark = TextSecondary         // #A0A0AB

val BackgroundLight = AppBackground           // #111222
val SurfaceLight = CardBackground             // #22212A
val SurfaceVariantLight = CardBackgroundVariant // #292735
val TextPrimaryLight = TextPrimary            // #FFFFFF
val TextSecondaryLight = TextSecondary        // #A0A0AB


