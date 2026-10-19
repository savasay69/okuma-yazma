package com.ferhatozcelik.jetpackcomposetemplate

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = Color(0xFFF8F9FA)) {
                    MaarifOkumaEkrani()
                }
            }
        }
    }
}

@Composable
fun MaarifOkumaEkrani() {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // TTS (Sesli Okuma) Ayarları
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("tr", "TR")
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val sesliOku = { metin: String ->
        tts?.speak(metin, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // MEB Türkiye Yüzyılı Maarif Modeli Yeni Ses Grupları
    val maarifSesler = listOf(
        "A", "N", "E", "T", "İ", "L", // 1. Grup
        "O", "K", "U", "R", "I", "M", // 2. Grup
        "Ü", "S", "Ö", "Y", "D", "Z", // 3. Grup
        "Ç", "B", "G", "C", "Ş",      // 4. Grup
        "P", "H", "V", "Ğ", "F", "J"  // 5. Grup
    )

    // Uygulama içindeki hece ve kelime sözlüğü (Genişletebilirsiniz)
    val heceHavuzu = listOf(
        "AL", "LA", "AN", "NA", "AT", "TA", "EL", "LE", "EN", "NE", "ET", "TE", "İL", "Lİ", "İN", "Nİ", "İT", "Tİ",
        "AK", "KA", "OK", "KO", "UK", "KU", "IK", "KI", "EK", "KE", "İK", "Kİ", "AR", "RA", "ER", "RE", "İR", "Rİ",
        "OM", "MO", "UM", "MU", "IM", "MI", "ON", "NO", "UN", "NU", "OT", "TO"
    )

    val kelimeHavuzu = listOf(
        "ALİ", "AİLE", "ALET", "ANA", "ANNE", "ANT", "ATLET", "ELA", "ELTİ", "ELLİ", "ETLİ",
        "İL", "İLAN", "İNAT", "LALE", "NANE", "NENE", "NİL", "NİNE", "TAN", "TANE", "TAT", "TEL", "TELLİ",
        "ELMA", "KALEM", "OKUL", "OKU", "KOKU", "KOL", "KOLA", "KİLİM", "LİMON", "MOR", "NAR", "NUR", "ON",
        "ROMAN", "KOR", "KUM", "KURUM", "KURAL", "MAMA", "NİMET", "UMUT", "MUTLU", "TARTI", "RAKAM", "MAKİNE",
        "SÜT", "SÜS", "KEDİ", "YEDİ", "ÖZEL", "GÖZ", "SÖZ", "YAZ", "DEDEM", "DAYI", "DERE", "ZİL", "ZAMAN",
        "ÇAY", "ÇOCUK", "BABA", "BÖCEK", "GÜNEŞ", "GECE", "ŞEKER", "PASTA", "PARA", "HAVUÇ", "VAPUR", "AĞAÇ", "FİL"
    )

    // Çocuğun seçtiği sesin indeksi
    var seciliSesIndeksi by remember { mutableIntStateOf(0) }

    // Seçili ve önceki tüm sesleri bir listeye alıyoruz
    val ogrenilenSesler = maarifSesler.take(seciliSesIndeksi + 1)

    // FİLTRELEME MANTIĞI: Sadece öğrenilen harflerden oluşan hece ve kelimeleri getir
    val gecerliHeceler = heceHavuzu.filter { metin ->
        metin.all { harf -> ogrenilenSesler.contains(harf.toString()) }
    }

    val gecerliKelimeler = kelimeHavuzu.filter { metin ->
        metin.all { harf -> ogrenilenSesler.contains(harf.toString()) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        Text(
            text = "Maarif Modeli - İlk Okuma",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Text(
            text = "Harfleri seçerek hece ve kelimeleri keşfet",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Üst Kısım: Ses Seçme Çubuğu
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(maarifSesler) { index, ses ->
                val isSelected = index == seciliSesIndeksi
                val isLearned = index <= seciliSesIndeksi

                SesRozeti(
                    ses = ses,
                    isSelected = isSelected,
                    isLearned = isLearned,
                    onClick = {
                        seciliSesIndeksi = index
                        sesliOku(ses)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Orta Kısım: Öğrenilen Heceler (Ağırlıklı paylaşımlı liste)
        Text("Oluşan Heceler", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE67E22))
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(70.dp),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gecerliHeceler) { hece ->
                OkumaKarti(metin = hece, onClick = { sesliOku(hece) }, renk = Color(0xFFFFF9C4))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alt Kısım: Öğrenilen Kelimeler (Ağırlıklı paylaşımlı liste)
        Text("Oluşan Kelimeler", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF27AE60))
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gecerliKelimeler) { kelime ->
                OkumaKarti(metin = kelime, onClick = { sesliOku(kelime) }, renk = Color(0xFFD5F5E3))
            }
        }
    }
}

// Harflerin gösterildiği üstteki yuvarlak butonlar
@Composable
fun SesRozeti(ses: String, isSelected: Boolean, isLearned: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFF3498DB) else if (isLearned) Color(0xFFAED6F1) else Color(0xFFE5E7E9)
    val textColor = if (isLearned) Color(0xFF1B4F72) else Color(0xFFBDC3C7)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color(0xFF154360) else Color.Transparent,
                shape = CircleShape
            )
    ) {
        Text(text = ses, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// Hece ve Kelime Kartları
@Composable
fun OkumaKarti(metin: String, onClick: () -> Unit, renk: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(60.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = renk),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = metin,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }
    }
}
