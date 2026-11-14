package lab.neurotrino.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// One corner language: tight instrument edges for chips (small),
// generous but not bubbly radii for panels and wells.
val ChipShape = RoundedCornerShape(9.dp)
val SmallPanelShape = RoundedCornerShape(14.dp)
val PanelShape = RoundedCornerShape(20.dp)
val WellShape = RoundedCornerShape(24.dp)
val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val CircleShape = RoundedCornerShape(percent = 50)
