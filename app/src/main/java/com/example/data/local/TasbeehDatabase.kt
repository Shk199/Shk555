package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyStat
import com.example.data.model.DhikrItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DhikrItem::class, DailyStat::class],
    version = 1,
    exportSchema = false
)
abstract class TasbeehDatabase : RoomDatabase() {

    abstract fun tasbeehDao(): TasbeehDao

    companion object {
        @Volatile
        private var INSTANCE: TasbeehDatabase? = null

        fun getInstance(context: Context): TasbeehDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TasbeehDatabase::class.java,
                    "elegant_tasbeeh.db"
                ).addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val INITIAL_AZKAR = listOf(
            DhikrItem(
                id = 1,
                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
                virtue = "كلمتان خفيفتان على اللسان، ثقيلتان في الميزان، حبيبتان إلى الرحمن. (رواه البخاري ومسلم).",
                targetCount = 100,
                orderIndex = 1
            ),
            DhikrItem(
                id = 2,
                arabicText = "لا إلهَ إلاَّ اللَّهُ وحدَهُ لا شَرِيكُ لَهُ، لَهُ المُلْكُ ولَهُ الحَمْدُ، وهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                virtue = "كانت له عدل عشر رقاب، وكتبت له مائة حسنة، ومحيت عنه مائة سيئة، وكان حرزاً له من الشيطان يومه ذلك حتى يمسي. (رواه البخاري ومسلم).",
                targetCount = 100,
                orderIndex = 2
            ),
            DhikrItem(
                id = 3,
                arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                virtue = "قال النبي ﷺ: «إِنِّي لَأَسْتَغْفِرُ اللَّهَ فِي الْيَوْمِ مِائَةَ مَرَّةٍ»، وفيه تفريج الهموم وتكفير السيئات وسعة الرزق. (رواه مسلم).",
                targetCount = 100,
                orderIndex = 3
            ),
            DhikrItem(
                id = 4,
                arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
                virtue = "قال النبي ﷺ: «مَنْ صَلَّى عَلَيَّ صَلَاةً وَاحِدَةً صَلَّى اللَّهُ عَلَيْهِ عَشْرَ صَلَوَاتٍ، وَحُطَّتْ عَنْهُ عَشْرُ خَطِيئَاتٍ، وَرُفِعَتْ لَهُ عَشْرُ دَرَجَاتٍ». (رواه النسائي وأحمد).",
                targetCount = 0,
                orderIndex = 4
            ),
            DhikrItem(
                id = 5,
                arabicText = "لا حَوْلَ وَلا قُوَّةَ إِلاَّ بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
                virtue = "كنز من كنوز الجنة، وباب من أبوابها، وبها يُستعان على عظائم الأمور وتفريج الكروب. (رواه البخاري ومسلم).",
                targetCount = 0,
                orderIndex = 5
            ),
            DhikrItem(
                id = 6,
                arabicText = "سُبْحَانَ اللَّهِ",
                virtue = "تسبيح دبر كل صلاة مكتوبة ثلاثاً وثلاثين، قال رسول الله ﷺ: «مسبِّحات لا يَخِيبُ قائِلُهُنَّ دُبُرَ كُلِّ صَلاةٍ مَكْتُوبَةٍ: ثَلاثٌ وثَلاثُونَ تَسْبِيحَةً...». (رواه مسلم).",
                targetCount = 33,
                orderIndex = 6
            ),
            DhikrItem(
                id = 7,
                arabicText = "الْحَمْدُ لِلَّهِ",
                virtue = "تحميد دبر كل صلاة مكتوبة ثلاثاً وثلاثين، وأفضل الدعاء الحمد لله، وهي تملأ الميزان بالخيرات. (رواه مسلم).",
                targetCount = 33,
                orderIndex = 7
            ),
            DhikrItem(
                id = 8,
                arabicText = "اللَّهُ أَكْبَرُ",
                virtue = "تكبير دبر كل صلاة مكتوبة ثلاثاً وثلاثين، تكميلاً للمائة، وتكفير للخطايا والذنوب وإن كانت مثل زبد البحر. (رواه مسلم).",
                targetCount = 33,
                orderIndex = 8
            ),
            DhikrItem(
                id = 9,
                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
                virtue = "قال النبي ﷺ لأم المؤمنين جويرية رضي الله عنها: «لقد قلتُ بعدكِ أربعَ كلماتٍ ثلاثَ مراتٍ، لو وُزِنت بما قلتِ منذُ اليومِ لوزَنَتْهُنَّ». (رواه مسلم).",
                targetCount = 3,
                orderIndex = 9
            ),
            DhikrItem(
                id = 10,
                arabicText = "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                virtue = "دعوة ذي النون إذ دعا بها في بطن الحوت: لم يَدْعُ بها رجل مسلم في شيء قط إلا استجاب الله له وفرج همه. (رواه الترمذي وصححه الألباني).",
                targetCount = 0,
                orderIndex = 10
            ),
            DhikrItem(
                id = 11,
                arabicText = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                virtue = "قالها إبراهيم عليه السلام حين أُلقي في النار فكانت برداً وسلاماً، وقالها النبي ﷺ وصحابته فكفاهم الله مكر الأعداء. (رواه البخاري).",
                targetCount = 0,
                orderIndex = 11
            ),
            DhikrItem(
                id = 12,
                arabicText = "سُبْحَانَ رَبِّيَ الْعَظِيمِ وَبِحَمْدِهِ",
                virtue = "تسبيح الركوع في الصلاة (3 مرات)، تعظيم لله عز وجل في الركوع امتثالاً لأمره سبحانه ولهدي نبيه ﷺ. (رواه مسلم وأبو داود).",
                targetCount = 3,
                orderIndex = 12
            ),
            DhikrItem(
                id = 13,
                arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى وَبِحَمْدِهِ",
                virtue = "تسبيح السجود في الصلاة (3 مرات)، وأقرب ما يكون العبد من ربه وهو ساجد فأكثروا فيه من الدعاء والتسبيح. (رواه مسلم وأبو داود).",
                targetCount = 3,
                orderIndex = 13
            ),
            DhikrItem(
                id = 14,
                arabicText = "رَبِّ اغْفِرْ لِي، رَبِّ اغْفِرْ لِي",
                virtue = "دعاء الجلوس بين السجدتين في الصلاة، كان النبي ﷺ يكرره طلباً لمغفرة الذنوب والرحمة والعافية والرزق. (رواه أبو داود وابن ماجه).",
                targetCount = 0,
                orderIndex = 14
            )
        )
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.tasbeehDao()?.insertAllAzkar(INITIAL_AZKAR)
            }
        }
    }
}
