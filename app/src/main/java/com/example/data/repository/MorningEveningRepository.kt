package com.example.data.repository

import android.content.Context
import com.example.data.model.MorningEveningCategory
import com.example.data.model.MorningEveningDhikr
import com.example.data.model.MorningEveningItemState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MorningEveningRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("morning_evening_prefs", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    private val _morningAzkar = MutableStateFlow<List<MorningEveningItemState>>(emptyList())
    val morningAzkar: StateFlow<List<MorningEveningItemState>> = _morningAzkar.asStateFlow()

    private val _eveningAzkar = MutableStateFlow<List<MorningEveningItemState>>(emptyList())
    val eveningAzkar: StateFlow<List<MorningEveningItemState>> = _eveningAzkar.asStateFlow()

    init {
        loadData()
    }

    private fun getCurrentDate(): String = dateFormat.format(Date())

    fun loadData() {
        val today = getCurrentDate()
        val savedDate = prefs.getString("last_date", "") ?: ""

        val isNewDay = savedDate != today
        if (isNewDay) {
            prefs.edit().clear().putString("last_date", today).apply()
        }

        _morningAzkar.value = MORNING_ITEMS.map { dhikr ->
            val count = if (isNewDay) 0 else prefs.getInt("count_${dhikr.id}", 0)
            MorningEveningItemState(dhikr = dhikr, currentCount = count)
        }

        _eveningAzkar.value = EVENING_ITEMS.map { dhikr ->
            val count = if (isNewDay) 0 else prefs.getInt("count_${dhikr.id}", 0)
            MorningEveningItemState(dhikr = dhikr, currentCount = count)
        }
    }

    fun incrementDhikr(id: Int, category: MorningEveningCategory) {
        val currentList = if (category == MorningEveningCategory.MORNING) _morningAzkar.value else _eveningAzkar.value
        val updated = currentList.map { item ->
            if (item.dhikr.id == id) {
                val newCount = item.currentCount + 1
                prefs.edit().putInt("count_$id", newCount).apply()
                item.copy(currentCount = newCount)
            } else {
                item
            }
        }
        if (category == MorningEveningCategory.MORNING) {
            _morningAzkar.value = updated
        } else {
            _eveningAzkar.value = updated
        }
    }

    fun resetDhikr(id: Int, category: MorningEveningCategory) {
        prefs.edit().putInt("count_$id", 0).apply()
        val currentList = if (category == MorningEveningCategory.MORNING) _morningAzkar.value else _eveningAzkar.value
        val updated = currentList.map { item ->
            if (item.dhikr.id == id) {
                item.copy(currentCount = 0)
            } else {
                item
            }
        }
        if (category == MorningEveningCategory.MORNING) {
            _morningAzkar.value = updated
        } else {
            _eveningAzkar.value = updated
        }
    }

    fun resetCategory(category: MorningEveningCategory) {
        val currentList = if (category == MorningEveningCategory.MORNING) _morningAzkar.value else _eveningAzkar.value
        val editor = prefs.edit()
        currentList.forEach { editor.putInt("count_${it.dhikr.id}", 0) }
        editor.apply()

        val updated = currentList.map { it.copy(currentCount = 0) }
        if (category == MorningEveningCategory.MORNING) {
            _morningAzkar.value = updated
        } else {
            _eveningAzkar.value = updated
        }
    }

    companion object {
        val MORNING_ITEMS = listOf(
            MorningEveningDhikr(
                id = 101,
                category = MorningEveningCategory.MORNING,
                title = "آية الكرسي",
                arabicText = "﴿اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَوَاتِ وَالْأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾",
                targetCount = 1,
                virtue = "من قالها حين يصبح أُجير من الجن حتى يمسي.",
                takhrij = "أخرجه الحاكم وصححه الألباني",
                orderIndex = 1
            ),
            MorningEveningDhikr(
                id = 102,
                category = MorningEveningCategory.MORNING,
                title = "سورة الإخلاص والمعوذتان",
                arabicText = "﴿ قُلْ هُوَ اللَّهُ أَحَدٌ ۞ اللَّهُ الصَّمَدُ ۞ لَمْ يَلِدْ وَلَمْ يُولَدْ ۞ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ ﴾\n\n﴿ قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۞ مِنْ شَرِّ مَا خَلَقَ ۞ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۞ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۞ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴾\n\n﴿ قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۞ مَلِكِ النَّاسِ ۞ إِلَهِ النَّاسِ ۞ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۞ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۞ مِنَ الْجِنَّةِ وَالنَّاسِ ﴾",
                targetCount = 3,
                virtue = "من قرأها حين يصبح وحين يمسي ثلاث مرات كفته من كل شيء.",
                takhrij = "رواه أبو داود والترمذي وحسنه الألباني",
                orderIndex = 2
            ),
            MorningEveningDhikr(
                id = 103,
                category = MorningEveningCategory.MORNING,
                title = "أصبحنا وأصبح الملك لله",
                arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
                targetCount = 1,
                virtue = "إقرار بملك الله وحده، وسؤال خير اليوم واستعاذة من الشرور وعذاب القبر.",
                takhrij = "رواه مسلم",
                orderIndex = 3
            ),
            MorningEveningDhikr(
                id = 104,
                category = MorningEveningCategory.MORNING,
                title = "اللهم بك أصبحنا",
                arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
                targetCount = 1,
                virtue = "افتقار العبد الكامل لربه في حياته ومماته وحشره.",
                takhrij = "رواه الترمذي وأبو داود وصححه الألباني",
                orderIndex = 4
            ),
            MorningEveningDhikr(
                id = 105,
                category = MorningEveningCategory.MORNING,
                title = "سيد الاستغفار",
                arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنوبَ إِلاَّ أَنْتَ",
                targetCount = 1,
                virtue = "من قالها موقناً بها حين يصبح فمات من يومه دخل الجنة.",
                takhrij = "رواه البخاري",
                orderIndex = 5
            ),
            MorningEveningDhikr(
                id = 106,
                category = MorningEveningCategory.MORNING,
                title = "اللهم إني أصبحت أشهدك",
                arabicText = "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلاَئِكَتِكَ، وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلاَّ أَنْتَ وَحْدَكَ لاَ شَرِيكَ لَكَ، وَأَنَّ مُحَمَّداً عَبْدُكَ وَرَسُولُكَ",
                targetCount = 4,
                virtue = "من قالها حين يصبح أو يمسي أربع مرات أعتقه الله من النار.",
                takhrij = "رواه أبو داود وحسنه الألباني (حصن المسلم)",
                orderIndex = 6
            ),
            MorningEveningDhikr(
                id = 107,
                category = MorningEveningCategory.MORNING,
                title = "اللهم ما أصبح بي من نعمة",
                arabicText = "اللَّهُمَّ مَا أَصْبَحَ بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لاَ شَرِيكَ لَكَ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
                targetCount = 1,
                virtue = "من قالها حين يصبح فقد أدى شكر يومه.",
                takhrij = "رواه أبو داود والنسائي وحسنه الألباني (حصن المسلم)",
                orderIndex = 7
            ),
            MorningEveningDhikr(
                id = 108,
                category = MorningEveningCategory.MORNING,
                title = "اللهم عافني في بدني",
                arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ. اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ، وَالفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ القَبْرِ، لاَ إِلَهَ إِلاَّ أَنْتَ",
                targetCount = 3,
                virtue = "سؤال العافية في البدن والسمع والبصر والاستعاذة من الكفر والفقر وعذاب القبر.",
                takhrij = "رواه أبو داود وأحمد وحسنه الألباني",
                orderIndex = 8
            ),
            MorningEveningDhikr(
                id = 109,
                category = MorningEveningCategory.MORNING,
                title = "حسبي الله لا إله إلا هو",
                arabicText = "حَسْبِيَ اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ عَلَيهِ تَوَكَّلتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
                targetCount = 7,
                virtue = "من قالها حين يصبح وحين يمسي سبع مرات كفاه الله ما أهمه من أمر الدنيا والآخرة.",
                takhrij = "رواه أبو داود وصححه الألباني موقوفاً (حصن المسلم)",
                orderIndex = 9
            ),
            MorningEveningDhikr(
                id = 110,
                category = MorningEveningCategory.MORNING,
                title = "دعاء العافية والستر",
                arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ: فِي دِينِي وَدُنْيَايَ وَأَهْلِي، وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي، وَآمِنْ رَوْعَاتِي، اللَّهُمَّ احْفَظْنِي مِنْ بَينِ يَدَيَّ، وَمِنْ خَلْفِي، وَعَنْ يَمِينِي، وَعَنْ شِمَالِي، وَمِنْ فَوْقِي، وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي",
                targetCount = 1,
                virtue = "لم يكن رسول الله ﷺ يدع هؤلاء الكلمات حين يصبح وحين يمسي لحفظ العبد من كل الجهات.",
                takhrij = "رواه أبو داود والنسائي وصححه الألباني",
                orderIndex = 10
            ),
            MorningEveningDhikr(
                id = 111,
                category = MorningEveningCategory.MORNING,
                title = "اللهم عالم الغيب والشهادة",
                arabicText = "اللَّهُمَّ عَالِمَ الغَيْبِ وَالشَّهَادَةِ فَاطِرَ السَّمَوَاتِ وَالْأَرْضِ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ، أَشْهَدُ أَنْ لاَ إِلَهَ إِلاَّ أَنْتَ، أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي، وَمِنْ شَرِّ الشَّيْطانِ وَشَرَكِهِ، وَأَنْ أَقْتَرِفَ عَلَى نَفْسِي سُوءاً، أَوْ أَجُرَّهُ إِلَى مُسْلِمٍ",
                targetCount = 1,
                virtue = "التحصن من شر النفس ووساوس الشيطان ومكائده والوقوع في الإثم.",
                takhrij = "رواه الترمذي وأبو داود وصححه الألباني",
                orderIndex = 11
            ),
            MorningEveningDhikr(
                id = 112,
                category = MorningEveningCategory.MORNING,
                title = "بسم الله الذي لا يضر مع اسمه شيء",
                arabicText = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلاَ فِي السّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                targetCount = 3,
                virtue = "من قالها ثلاثاً إذا أصبح وثلاثاً إذا أمسى لم يضره شيء.",
                takhrij = "رواه أبو داود والترمذي وصححه الألباني",
                orderIndex = 12
            ),
            MorningEveningDhikr(
                id = 113,
                category = MorningEveningCategory.MORNING,
                title = "رضيت بالله رباً",
                arabicText = "رَضِيتُ بِاللَّهِ رَبَّاً، وَبِالْإِسْلاَمِ دِيناً، وَبِمُحَمَّدٍ صلى الله عليه وسلم نَبِيّاً",
                targetCount = 3,
                virtue = "من قالها حين يصبح وحين يمسي ثلاثاً كان حقاً على الله أن يرضيه يوم القيامة.",
                takhrij = "رواه أحمد وأبو داود والترمذي وحسنه الألباني",
                orderIndex = 13
            ),
            MorningEveningDhikr(
                id = 114,
                category = MorningEveningCategory.MORNING,
                title = "يا حي يا قيوم برحمتك أستغيث",
                arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغيثُ أَصْلِحْ لِي شَأْنِيَ كُلَّهُ وَلاَ تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
                targetCount = 1,
                virtue = "تفويض الأمر إلى الله والاستعانة برحمته في إصلاح كل الأحوال.",
                takhrij = "رواه الحاكم والنسائي وحسنه الألباني",
                orderIndex = 14
            ),
            MorningEveningDhikr(
                id = 115,
                category = MorningEveningCategory.MORNING,
                title = "أصبحنا وأصبح الملك لله رب العالمين",
                arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ رَبِّ الْعَالَمِينَ، اللَّهُـمَّ إِنِّي أَسْأَلُكَ خَيْرَ هَذَا الْيَوْمِ: فَتْحَهُ، وَنَصْرَهُ، وَنورَهُ، وَبَرَكَتَهُ، وَهُدَاهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِيهِ وَشَرِّ مَا بَعْدَهُ",
                targetCount = 1,
                virtue = "سؤال البركة والفتح والنور والهداية والاستعاذة من الشرور.",
                takhrij = "حصن المسلم - سعيد بن علي القحطاني",
                orderIndex = 15
            ),
            MorningEveningDhikr(
                id = 116,
                category = MorningEveningCategory.MORNING,
                title = "أصبحنا على فطرة الإسلام",
                arabicText = "أَصْبَحْنا عَلَى فِطْرَةِ الْإِسْلاَمِ، وَعَلَى كَلِمَةِ الْإِخْلاَصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صلى الله عليه وسلم، وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ، حَنِيفاً مُسْلِماً وَمَا كَانَ مِنَ الْمُشرِكِينَ",
                targetCount = 1,
                virtue = "تجديد العهد على ملة إبراهيم حنيفاً ودين محمد ﷺ والتوحيد الخالص.",
                takhrij = "رواه أحمد وصححه الألباني",
                orderIndex = 16
            ),
            MorningEveningDhikr(
                id = 117,
                category = MorningEveningCategory.MORNING,
                title = "سبحان الله وبحمده",
                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                targetCount = 100,
                virtue = "من قالها مائة مرة حين يصبح وحين يمسي لم يأت أحد يوم القيامة بأفضل مما جاء به وحُطّت خطاياه.",
                takhrij = "رواه مسلم",
                orderIndex = 17
            ),
            MorningEveningDhikr(
                id = 118,
                category = MorningEveningCategory.MORNING,
                title = "التهليل عشر مرات",
                arabicText = "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                targetCount = 10,
                virtue = "عشر مرات (أو مرة واحدة عند الكسل): كانت له عدل أربع رقاب من ولد إسماعيل.",
                takhrij = "رواه النسائي وأبو داود وصححه الألباني",
                orderIndex = 18
            ),
            MorningEveningDhikr(
                id = 119,
                category = MorningEveningCategory.MORNING,
                title = "التهليل مائة مرة",
                arabicText = "لاَ إِلَهَ إِلاَّ اللَّهُ، وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                targetCount = 100,
                virtue = "كانت له عدل عشر رقاب، وكتبت له مائة حسنة، ومحيت عنه مائة سيئة، وحرزاً من الشيطان.",
                takhrij = "رواه البخاري ومسلم",
                orderIndex = 19
            ),
            MorningEveningDhikr(
                id = 120,
                category = MorningEveningCategory.MORNING,
                title = "سبحان الله وبحمده عدد خلقه",
                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
                targetCount = 3,
                virtue = "أربع كلمات ثلاث مرات تعدل كل تسبيح طوال اليوم بأضعاف مضاعفة.",
                takhrij = "رواه مسلم",
                orderIndex = 20
            ),
            MorningEveningDhikr(
                id = 121,
                category = MorningEveningCategory.MORNING,
                title = "اللهم إني أسألك علماً نافعاً",
                arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْماً نَافِعاً، وَرِزْقاً طَيِّباً، وَعَمَلاً مُتَقَبَّلاً",
                targetCount = 1,
                virtue = "يقال في الصباح، وهو دعاء نبوي جامع لخير العلم والرزق الطيب والعمل الصالح المتقبل.",
                takhrij = "رواه ابن ماجه وصححه الألباني (حصن المسلم)",
                orderIndex = 21
            ),
            MorningEveningDhikr(
                id = 122,
                category = MorningEveningCategory.MORNING,
                title = "الاستغفار مائة مرة",
                arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                targetCount = 100,
                virtue = "مائة مرة في اليوم: مغفرة الذنوب، وسعة الرزق، وانشراح الصدر وتفريج الكروب.",
                takhrij = "رواه البخاري ومسلم",
                orderIndex = 22
            ),
            MorningEveningDhikr(
                id = 123,
                category = MorningEveningCategory.MORNING,
                title = "الصلاة على النبي ﷺ",
                arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبَيِّنَا مُحَمَّدٍ",
                targetCount = 10,
                virtue = "من صلى عليّ حين يصبح عشراً وحين يمسي عشراً أدركته شفاعتي يوم القيامة.",
                takhrij = "رواه الطبراني وحسنه الألباني (حصن المسلم)",
                orderIndex = 23
            )
        )

        val EVENING_ITEMS = listOf(
            MorningEveningDhikr(
                id = 201,
                category = MorningEveningCategory.EVENING,
                title = "آية الكرسي",
                arabicText = "﴿اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَوَاتِ وَالْأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾",
                targetCount = 1,
                virtue = "من قالها حين يمسي أُجير من الجن حتى يصبح.",
                takhrij = "أخرجه الحاكم وصححه الألباني",
                orderIndex = 1
            ),
            MorningEveningDhikr(
                id = 202,
                category = MorningEveningCategory.EVENING,
                title = "سورة الإخلاص والمعوذتان",
                arabicText = "﴿ قُلْ هُوَ اللَّهُ أَحَدٌ ۞ اللَّهُ الصَّمَدُ ۞ لَمْ يَلِدْ وَلَمْ يُولَدْ ۞ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ ﴾\n\n﴿ قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۞ مِنْ شَرِّ مَا خَلَقَ ۞ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۞ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۞ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴾\n\n﴿ قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۞ مَلِكِ النَّاسِ ۞ إِلَهِ النَّاسِ ۞ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۞ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۞ مِنَ الْجِنَّةِ وَالنَّاسِ ﴾",
                targetCount = 3,
                virtue = "من قرأها حين يمسي ثلاث مرات كفته من كل شيء.",
                takhrij = "رواه أبو داود والترمذي وحسنه الألباني",
                orderIndex = 2
            ),
            MorningEveningDhikr(
                id = 203,
                category = MorningEveningCategory.EVENING,
                title = "أمسينا وأمسى الملك لله",
                arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
                targetCount = 1,
                virtue = "استسلام العبد لله وسؤال خير الليلة والاستعاذة من الشرور وعذاب القبر.",
                takhrij = "رواه مسلم",
                orderIndex = 3
            ),
            MorningEveningDhikr(
                id = 204,
                category = MorningEveningCategory.EVENING,
                title = "اللهم بك أمسينا",
                arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
                targetCount = 1,
                virtue = "إقرار بفضل الله وتدبيره للكون والمصير إليه.",
                takhrij = "رواه الترمذي وأبو داود وصححه الألباني",
                orderIndex = 4
            ),
            MorningEveningDhikr(
                id = 205,
                category = MorningEveningCategory.EVENING,
                title = "سيد الاستغفار",
                arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنوبَ إِلاَّ أَنْتَ",
                targetCount = 1,
                virtue = "من قالها موقناً بها حين يمسي فمات من ليلته دخل الجنة.",
                takhrij = "رواه البخاري",
                orderIndex = 5
            ),
            MorningEveningDhikr(
                id = 206,
                category = MorningEveningCategory.EVENING,
                title = "اللهم إني أمسيت أشهدك",
                arabicText = "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلاَئِكَتِكَ، وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلاَّ أَنْتَ وَحْدَكَ لاَ شَرِيكَ لَكَ، وَأَنَّ مُحَمَّداً عَبْدُكَ وَرَسُولُكَ",
                targetCount = 4,
                virtue = "من قالها حين يصبح أو يمسي أربع مرات أعتقه الله من النار.",
                takhrij = "رواه أبو داود وحسنه الألباني (حصن المسلم)",
                orderIndex = 6
            ),
            MorningEveningDhikr(
                id = 207,
                category = MorningEveningCategory.EVENING,
                title = "اللهم ما أمسى بي من نعمة",
                arabicText = "اللَّهُمَّ مَا أَمْسَى بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لاَ شَرِيكَ لَكَ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
                targetCount = 1,
                virtue = "من قالها حين يمسي فقد أدى شكر ليلته.",
                takhrij = "رواه أبو داود والنسائي وحسنه الألباني (حصن المسلم)",
                orderIndex = 7
            ),
            MorningEveningDhikr(
                id = 208,
                category = MorningEveningCategory.EVENING,
                title = "اللهم عافني في بدني",
                arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ. اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ، وَالفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ القَبْرِ، لاَ إِلَهَ إِلاَّ أَنْتَ",
                targetCount = 3,
                virtue = "سؤال العافية في البدن والسمع والبصر والاستعاذة من الكفر والفقر وعذاب القبر.",
                takhrij = "رواه أبو داود وأحمد وحسنه الألباني",
                orderIndex = 8
            ),
            MorningEveningDhikr(
                id = 209,
                category = MorningEveningCategory.EVENING,
                title = "حسبي الله لا إله إلا هو",
                arabicText = "حَسْبِيَ اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ عَلَيهِ تَوَكَّلتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
                targetCount = 7,
                virtue = "من قالها حين يصبح وحين يمسي سبع مرات كفاه الله ما أهمه من أمر الدنيا والآخرة.",
                takhrij = "رواه أبو داود وصححه الألباني موقوفاً (حصن المسلم)",
                orderIndex = 9
            ),
            MorningEveningDhikr(
                id = 210,
                category = MorningEveningCategory.EVENING,
                title = "دعاء العافية والستر",
                arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ: فِي دِينِي وَدُنْيَايَ وَأَهْلِي، وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي، وَآمِنْ رَوْعَاتِي، اللَّهُمَّ احْفَظْنِي مِنْ بَينِ يَدَيَّ، وَمِنْ خَلْفِي، وَعَنْ يَمِينِي، وَعَنْ شِمَالِي، وَمِنْ فَوْقِي، وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي",
                targetCount = 1,
                virtue = "حفظ تام من الفواجع والمهالك في النفس والأهل والدين والمال.",
                takhrij = "رواه أبو داود والنسائي وصححه الألباني",
                orderIndex = 10
            ),
            MorningEveningDhikr(
                id = 211,
                category = MorningEveningCategory.EVENING,
                title = "اللهم عالم الغيب والشهادة",
                arabicText = "اللَّهُمَّ عَالِمَ الغَيْبِ وَالشَّهَادَةِ فَاطِرَ السَّمَوَاتِ وَالْأَرْضِ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ، أَشْهَدُ أَنْ لاَ إِلَهَ إِلاَّ أَنْتَ، أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي، وَمِنْ شَرِّ الشَّيْطانِ وَشَرَكِهِ، وَأَنْ أَقْتَرِفَ عَلَى نَفْسِي سُوءاً، أَوْ أَجُرَّهُ إِلَى مُسْلِمٍ",
                targetCount = 1,
                virtue = "التحصن من شر النفس ووساوس الشيطان ومكائده والوقوع في الإثم.",
                takhrij = "رواه الترمذي وأبو داود وصححه الألباني",
                orderIndex = 11
            ),
            MorningEveningDhikr(
                id = 212,
                category = MorningEveningCategory.EVENING,
                title = "بسم الله الذي لا يضر مع اسمه شيء",
                arabicText = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلاَ فِي السّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                targetCount = 3,
                virtue = "من قالها ثلاثاً لم تصبه فجأة بلاء حتى يصبح.",
                takhrij = "رواه أبو داود والترمذي وصححه الألباني",
                orderIndex = 12
            ),
            MorningEveningDhikr(
                id = 213,
                category = MorningEveningCategory.EVENING,
                title = "رضيت بالله رباً",
                arabicText = "رَضِيتُ بِاللَّهِ رَبَّاً، وَبِالْإِسْلاَمِ دِيناً، وَبِمُحَمَّدٍ صلى الله عليه وسلم نَبِيّاً",
                targetCount = 3,
                virtue = "كان حقاً على الله أن يرضيه يوم القيامة.",
                takhrij = "رواه أحمد وأبو داود والترمذي وحسنه الألباني",
                orderIndex = 13
            ),
            MorningEveningDhikr(
                id = 214,
                category = MorningEveningCategory.EVENING,
                title = "يا حي يا قيوم برحمتك أستغيث",
                arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغيثُ أَصْلِحْ لِي شَأْنِيَ كُلَّهُ وَلاَ تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
                targetCount = 1,
                virtue = "تفويض الأمر إلى الله والاستعانة برحمته في إصلاح كل الأحوال.",
                takhrij = "رواه الحاكم والنسائي وحسنه الألباني",
                orderIndex = 14
            ),
            MorningEveningDhikr(
                id = 215,
                category = MorningEveningCategory.EVENING,
                title = "أمسينا وأمسى الملك لله رب العالمين",
                arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ رَبِّ الْعَالَمِينَ، اللَّهُـمَّ إِنِّي أَسْأَلُكَ خَيْرَ هَذِهِ اللَّيْلَةِ: فَتْحَهَا، وَنَصْرَهَا، وَنُورَهَا، وَبَرَكَتَهَا، وَهُدَاهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِيهَا وَشَرِّ مَا بَعْدَهَا",
                targetCount = 1,
                virtue = "سؤال البركة والفتح والنور والهداية والاستعاذة من الشرور.",
                takhrij = "حصن المسلم - سعيد بن علي القحطاني",
                orderIndex = 15
            ),
            MorningEveningDhikr(
                id = 216,
                category = MorningEveningCategory.EVENING,
                title = "أمسينا على فطرة الإسلام",
                arabicText = "أَمْسَيْنَا عَلَى فِطْرَةِ الْإِسْلاَمِ، وَعَلَى كَلِمَةِ الْإِخْلاَصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صلى الله عليه وسلم، وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ، حَنِيفاً مُسْلِماً وَمَا كَانَ مِنَ الْمُشرِكِينَ",
                targetCount = 1,
                virtue = "الاعتصام بالفطرة وملة التوحيد الخالص.",
                takhrij = "رواه أحمد وصححه الألباني",
                orderIndex = 16
            ),
            MorningEveningDhikr(
                id = 217,
                category = MorningEveningCategory.EVENING,
                title = "سبحان الله وبحمده",
                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                targetCount = 100,
                virtue = "من قالها مائة مرة حين يصبح وحين يمسي لم يأت أحد يوم القيامة بأفضل مما جاء به وحُطّت خطاياه.",
                takhrij = "رواه مسلم",
                orderIndex = 17
            ),
            MorningEveningDhikr(
                id = 218,
                category = MorningEveningCategory.EVENING,
                title = "التهليل عشر مرات",
                arabicText = "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                targetCount = 10,
                virtue = "عشر مرات (أو مرة واحدة عند الكسل): كانت له عدل أربع رقاب من ولد إسماعيل.",
                takhrij = "رواه النسائي وأبو داود وصححه الألباني",
                orderIndex = 18
            ),
            MorningEveningDhikr(
                id = 219,
                category = MorningEveningCategory.EVENING,
                title = "أعوذ بكلمات الله التامات",
                arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                targetCount = 3,
                virtue = "من قالها حين يمسي ثلاث مرات لم تضره حُمة تلك الليلة.",
                takhrij = "رواه مسلم والترمذي",
                orderIndex = 19
            ),
            MorningEveningDhikr(
                id = 220,
                category = MorningEveningCategory.EVENING,
                title = "الاستغفار مائة مرة",
                arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                targetCount = 100,
                virtue = "محو الذنوب وسعة الرزق وتفريج الهموم.",
                takhrij = "رواه البخاري ومسلم",
                orderIndex = 20
            ),
            MorningEveningDhikr(
                id = 221,
                category = MorningEveningCategory.EVENING,
                title = "الصلاة على النبي ﷺ",
                arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبَيِّنَا مُحَمَّدٍ",
                targetCount = 10,
                virtue = "من صلى عليّ حين يمسي عشراً أدركته شفاعتي يوم القيامة.",
                takhrij = "رواه الطبراني وحسنه الألباني (حصن المسلم)",
                orderIndex = 21
            )
        )
    }
}
