package com.example.ano.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ano.R
import com.example.ano.dataSource.InformationWordByNature
import com.example.readinggoals.ui.theme.Barlow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPackage(
   word:String,
   wordInfos: List<InformationWordByNature>,
   onWordClicked :(String)->Unit,
   wordToDisplayInAPackage : (Int)-> Unit,
) {

    var recto by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ElevatedCard(
            onClick = {
                    if(recto)
                        {recto = !recto}
                    else{

                    }
                      },
            modifier = Modifier
                .height(708.dp)
                .padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .clickable { onWordClicked(word) }
                ) {
                    Text(
                        text = word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        fontFamily = Barlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                )
                if (recto) {
                    Spacer(modifier = Modifier.weight(0.5F))
                    Image(
                        painter = painterResource(id = R.drawable.anopackage),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(0.75F)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.weight(0.5F))
                } else {
                    // Create element height in dp state
                    LazyColumn(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        items(wordInfos.size){
                            index ->
                            var elt = wordInfos[index]
                            if(elt.definitions.isNotEmpty()){
                                ListOfdef(
                                    wordInfos = elt,
                                )
                            }
                        }
                    }
                }
            }
        }
        var options = listOf("Encore","Difficile", "Bien", "Facile")
        var selectedIndex by remember { mutableIntStateOf(0) }
        if(!recto){
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 1f)
                    .padding(4.dp)
            ) {
                options.forEachIndexed { index, label ->
                    Button(
                        onClick = {
                            wordToDisplayInAPackage(index)
                            selectedIndex = index
                            recto = true
                        },
                        shape= MaterialTheme.shapes.small,
                        modifier = Modifier
                            .weight(1f)
                            .padding(0.5.dp)
                    ) {
                        Text(
                            text =label,
                            fontWeight = FontWeight.Light,
                            fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ListOfdef(
    wordInfos: InformationWordByNature,
) {
    var nature = wordInfos.nature
    Text(
        text = "$nature",
        fontFamily = Barlow,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.secondary,
    )
    var definitions = wordInfos.definitions //Map
    for (def in definitions.keys.toList()) {
        Element(lexique = definitions[def]?.lexique, def = def)
    }
}

    @Composable
    fun Element(
        lexique: String?,
        def: String
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .fillMaxWidth()
        ) {
            if (lexique != null) {
                Text(
                    text = "[$lexique]",
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Text(
                text = "- $def",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
            )
        }
    }




