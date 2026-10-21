package com.mummyflix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mummyflix.R
import com.mummyflix.model.Channel
import com.mummyflix.model.FooterGroup

/**
 * Pixel geometry of the reference 16:9 receiver OSD.
 *
 * Every dimension is a fraction of the screen so the layout lands identically
 * on 720p, 1080p and 4K Fire TV devices.
 */
@Immutable
data class OsdGeometry(val width: Dp, val height: Dp) {
    val headerHeight: Dp = height * 0.105f
    val footerHeight: Dp = (height * 0.125f).coerceAtLeast(36.dp)

    /** Body = everything between the header and footer, minus the 5dp inset. */
    val bodyTop: Dp = headerHeight + 5.dp
    val bodyHeight: Dp = (height - headerHeight - footerHeight - 10.dp).coerceAtLeast(80.dp)

    val leftPanelX: Dp = 10.dp
    val leftPanelY: Dp = bodyTop
    /** 40.5% of the body content box, exactly like the reference `.left-panel`. */
    val leftPanelWidth: Dp = (width - 20.dp) * 0.405f
    val leftPanelHeight: Dp = bodyHeight

    val rowHeight: Dp = (bodyHeight / 10f).coerceAtLeast(24.dp)

    val previewLeft: Dp = width * 0.425f
    val previewTop: Dp = height * 0.13f
    val previewWidth: Dp = width * 0.555f
    val previewHeight: Dp = height * 0.52f
    val previewBottom: Dp = previewTop + previewHeight

    /** The green strip / transponder sits after the left panel + 12dp body gap. */
    val rightPanelLeft: Dp = leftPanelX + leftPanelWidth + 12.dp
    val rightPanelWidth: Dp = (width - rightPanelLeft - 10.dp).coerceAtLeast(80.dp)
    val rightPanelTop: Dp = bodyTop
    val rightPanelHeight: Dp = bodyHeight

    val optionLeft: Dp = width * 0.425f
    val optionTop: Dp = height * 0.13f
    val optionWidth: Dp = width * 0.555f
    val optionHeight: Dp = height * 0.71f
}

@Composable
fun OsdBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to colorResource(R.color.bg_dark_blue_1),
                        0.85f to colorResource(R.color.bg_dark_blue_2),
                        1f to colorResource(R.color.bg_dark_blue_3)
                    )
                )
            )
    )
}

/** Outer receiver bezel, drawn above the video window. */
@Composable
fun OsdFrameBorder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(3.dp, colorResource(R.color.border_cyan))
    )
}

@Composable
fun OsdHeaderBar(title: String, height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0f to colorResource(R.color.border_cyan_light),
                        0.58f to colorResource(R.color.border_blue),
                        0.60f to colorResource(R.color.bg_blue_12),
                        1f to colorResource(R.color.bg_blue_13)
                    )
                )
            )
            .drawBehind {
                drawRect(
                    color = Color(0xFFD4F0FC),
                    topLeft = Offset(0f, size.height - 2.dp.toPx()),
                    size = Size(size.width, 2.dp.toPx())
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(22.dp).background(colorResource(R.color.bg_blue_12), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "➜",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.border_cyan_light)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colorResource(R.color.text_dark_blue),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            BrandHeaderLockup()
        }
    }
}

@Composable
fun OsdFooterBar(
    groups: List<FooterGroup>,
    focusedIndex: Int?,
    height: Dp,
    modifier: Modifier = Modifier,
    onActivate: (Int) -> Unit,
    onInfoClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.border_cyan),
                        colorResource(R.color.footer_gradient_end)
                    )
                )
            )
            .drawBehind {
                drawRect(
                    color = Color(0xFFD4F0FC),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, 2.dp.toPx())
                )
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            groups.forEachIndexed { index, group ->
                FooterGroupView(
                    group = group,
                    focused = focusedIndex == index,
                    onClick = { onActivate(index) }
                )
            }
        }
        InfoCircle(focused = focusedIndex == groups.size, onClick = onInfoClick)
    }
}

@Composable
private fun FooterGroupView(group: FooterGroup, focused: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .then(
                if (focused) Modifier.background(colorResource(R.color.secondary))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = if (focused) 6.dp else 0.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        group.keys.forEach { key ->
            KeyBadge(label = key, focused = focused)
        }
        Text(
            text = group.label,
            fontSize = 14.sp,
            fontWeight = if (focused) FontWeight.Black else FontWeight.Bold,
            color = colorResource(R.color.text_dark_blue),
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
fun KeyBadge(label: String, focused: Boolean = false) {
    Box(
        modifier = Modifier
            .background(
                if (focused) colorResource(R.color.text_dark_blue_2)
                else colorResource(R.color.bg_blue_11),
                RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                if (focused) colorResource(R.color.secondary) else colorResource(R.color.text_dark_blue_3),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (focused) colorResource(R.color.secondary)
            else colorResource(R.color.border_cyan_light)
        )
    }
}

@Composable
fun InfoCircle(focused: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                if (focused) colorResource(R.color.secondary)
                else colorResource(R.color.text_dark_blue_2),
                CircleShape
            )
            .then(
                if (focused) Modifier.border(2.dp, Color.White, CircleShape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "i",
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = if (focused) colorResource(R.color.text_dark_blue)
            else colorResource(R.color.border_cyan_light)
        )
    }
}

/** Channel list with the reference receiver's row styling and scrollbar. */
@Composable
fun ChannelListPanel(
    channels: List<Channel>,
    currentIndex: Int,
    rowHeight: Dp,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex, channels.size) {
        if (currentIndex !in channels.indices) return@LaunchedEffect
        val info = listState.layoutInfo
        val first = info.visibleItemsInfo.firstOrNull()?.index ?: 0
        val visible = info.visibleItemsInfo.size.coerceAtLeast(1)
        when {
            currentIndex < first -> listState.animateScrollToItem(currentIndex)
            currentIndex > first + visible - 1 ->
                listState.animateScrollToItem((currentIndex - visible + 1).coerceAtLeast(0))
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val panelHeight = maxHeight
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(channels, key = { index, _ -> index }) { index, channel ->
                ChannelRow(
                    index = index,
                    channel = channel,
                    isActive = index == currentIndex,
                    isPlaying = isPlaying && index == currentIndex,
                    height = rowHeight,
                    onClick = { onSelect(index) }
                )
            }
        }

        if (channels.size > 10) {
            RetroScrollbar(
                listState = listState,
                totalItems = channels.size,
                rowHeight = rowHeight,
                panelHeight = panelHeight
            )
        }
    }
}

@Composable
private fun BoxScope.RetroScrollbar(
    listState: androidx.compose.foundation.lazy.LazyListState,
    totalItems: Int,
    rowHeight: Dp,
    panelHeight: Dp
) {
    val visible = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
    if (visible >= totalItems) return

    val first = listState.layoutInfo.visibleItemsInfo.firstOrNull() ?: return
    val rowPx = with(LocalDensity.current) { rowHeight.toPx() }.coerceAtLeast(1f)
    val scrolled = first.index + (-first.offset / rowPx)
    val maxScroll = (totalItems - visible).coerceAtLeast(1)
    val progress = (scrolled / maxScroll).coerceIn(0f, 1f)

    val thumbHeight = (panelHeight * (visible.toFloat() / totalItems)).coerceAtLeast(18.dp)
    val thumbOffset = (panelHeight - thumbHeight) * progress

    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(5.dp)
            .fillMaxHeight()
            .background(colorResource(R.color.bg_dark_blue_3), RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .offset(y = thumbOffset)
                .fillMaxWidth()
                .height(thumbHeight)
                .background(colorResource(R.color.border_cyan), RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun ChannelRow(
    index: Int,
    channel: Channel,
    isActive: Boolean,
    isPlaying: Boolean,
    height: Dp,
    onClick: () -> Unit
) {
    val lineHeight = (height - 3.dp).coerceAtLeast(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(bottom = 2.dp)
            .background(
                if (isActive) {
                    Brush.horizontalGradient(
                        listOf(
                            colorResource(R.color.secondary),
                            colorResource(R.color.secondary_variant)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            colorResource(R.color.bg_blue_1),
                            colorResource(R.color.bg_blue_2)
                        )
                    )
                },
                RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                if (isActive) Color.White else colorResource(R.color.bg_blue_3),
                RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 6.dp)
            .height(lineHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%03d".format(index + 1),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.Black else colorResource(R.color.text_cyan_light),
            modifier = Modifier.width(42.dp)
        )
        if (channel.fav) {
            Text(
                text = "♥",
                fontSize = 13.sp,
                color = if (isActive) colorResource(R.color.border_red)
                else colorResource(R.color.secondary)
            )
            Spacer(Modifier.width(4.dp))
        }
        if (channel.locked) {
            Box(
                modifier = Modifier
                    .background(
                        if (isActive) Color.Black else colorResource(R.color.border_red),
                        RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "L",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isActive) colorResource(R.color.secondary) else Color.White
                )
            }
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = channel.displayName,
            fontSize = 16.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.Black else colorResource(R.color.text_cyan),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isPlaying) {
            Text(
                text = "▶",
                fontSize = 11.sp,
                color = if (isActive) Color.Black else colorResource(R.color.gradient_green_end)
            )
        }
    }
}

/** Main-menu / sub-menu list: items stretch to fill the panel exactly. */
@Composable
fun MenuListPanel(
    items: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    showNumbers: Boolean = true,
    onClick: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEachIndexed { index, item ->
            val active = index == selectedIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        if (active) {
                            Brush.horizontalGradient(
                                listOf(
                                    colorResource(R.color.secondary),
                                    colorResource(R.color.secondary_variant)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    colorResource(R.color.bg_blue_1),
                                    colorResource(R.color.bg_blue_2)
                                )
                            )
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        if (active) Color.White else colorResource(R.color.bg_blue_3),
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { onClick(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showNumbers) "${index + 1}. $item" else item,
                    fontSize = 16.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) Color.Black else colorResource(R.color.text_cyan),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

/**
 * Receiver chrome wrapped around the live preview: the video bezel plus the
 * green signal-lock strip and transponder read-out that sit at the bottom of
 * the right-hand panel (exactly where the reference OSD puts them).
 */
@Composable
fun PreviewChrome(
    geo: OsdGeometry,
    modifier: Modifier = Modifier,
    signal: Boolean = true,
    menuMode: Boolean = false,
    lnb: String = "09750",
    transponder: String = "12020"
) {
    // Video bezel.
    Box(
        modifier = modifier
            .offset(geo.previewLeft, geo.previewTop)
            .size(geo.previewWidth, geo.previewHeight)
            .border(3.dp, colorResource(R.color.border_cyan_light))
    )

    Column(
        modifier = modifier
            .offset(geo.rightPanelLeft, geo.rightPanelTop)
            .width(geo.rightPanelWidth)
            .height(geo.rightPanelHeight),
        verticalArrangement = Arrangement.Bottom
    ) {
        // LED strip at the bottom of the body, matching the web OSD.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(colorResource(R.color.border_cyan_light))
                .border(2.dp, Color.White)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 10.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (signal) colorResource(R.color.border_green)
                                else colorResource(R.color.border_red),
                                colorResource(R.color.bg_blue_15)
                            )
                        ),
                        CircleShape
                    )
                    .border(1.dp, Color(0xFF081C15), CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.bg_blue_9).copy(alpha = 0.90f))
                .drawBehind {
                    drawRect(
                        color = Color(0xFF5AA9E6),
                        size = Size(size.width, 2.dp.toPx())
                    )
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (menuMode) "11090F/V/29500S" else "$transponder/V/30000",
                    fontSize = 16.sp,
                    color = colorResource(R.color.text_cyan),
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = if (menuMode) "DiSEqC:OFF" else "DiSEqC:Off",
                    fontSize = 16.sp,
                    color = colorResource(R.color.text_cyan),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                Text(
                    text = if (menuMode) "5010V/6010A/5010P" else "5064/6064",
                    fontSize = 16.sp,
                    color = colorResource(R.color.text_cyan),
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = if (menuMode) "22K:OFF" else "22K:Off",
                    fontSize = 16.sp,
                    color = colorResource(R.color.text_cyan),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Right-hand option modal used by sub-menus. Mirrors the web OSD's
 * `#option-panel`: blue gradient, yellow section strap, yellow highlight row.
 */
@Composable
fun OptionPanelShell(
    title: String,
    geo: OsdGeometry,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .offset(geo.optionLeft, geo.optionTop)
            .size(geo.optionWidth, geo.optionHeight)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.bg_dark_blue_4),
                        colorResource(R.color.bg_dark_blue_5)
                    )
                )
            )
            .border(3.dp, colorResource(R.color.border_cyan_light))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = "[ $title ]",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.secondary),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
    }
}

@Composable
fun OptionRow(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .then(
                if (active) Modifier.background(
                    colorResource(R.color.secondary),
                    RoundedCornerShape(3.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick)
            .drawBehind {
                if (!active) {
                    drawRect(
                        color = Color(0x4D8ECAE6),
                        topLeft = Offset(0f, size.height - 1.dp.toPx()),
                        size = Size(size.width, 1.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) Color.Black else colorResource(R.color.text_cyan),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Shared empty-state text used inside the OSD. */
@Composable
fun OsdEmptyText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = colorResource(R.color.text_cyan),
            textAlign = TextAlign.Center
        )
    }
}
