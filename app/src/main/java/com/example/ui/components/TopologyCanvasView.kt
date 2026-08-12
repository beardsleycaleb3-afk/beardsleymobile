package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NodeStatus
import com.example.model.TopologyNode
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralCritical
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecure
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopologyCanvasView(
    nodes: List<TopologyNode>,
    selectedNodeId: String?,
    hasUncommittedChanges: Boolean,
    onSelectNode: (String) -> Unit,
    onUpdateNodeStatus: (String, NodeStatus) -> Unit,
    onResetTopology: () -> Unit,
    onRequestCommitCanvas: () -> Unit
) {
    val selectedNode = nodes.find { it.id == selectedNodeId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Canvas Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CROSS ACCESS TOPOLOGY CANVAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Perimeter & Zone Connectivity Grid",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onResetTopology) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Canvas",
                        tint = TextSecondary
                    )
                }

                if (hasUncommittedChanges) {
                    Surface(
                        color = AmberWarning.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning)
                    ) {
                        Text(
                            text = "PENDING EDITS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Topology Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .testTag("topology_canvas_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberDarkBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Drawing Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(nodes) {
                            detectTapGestures { tapOffset ->
                                val width = size.width
                                val height = size.height
                                val clickedNode = nodes.minByOrNull { node ->
                                    val nx = node.xRatio * width
                                    val ny = node.yRatio * height
                                    val dx = nx - tapOffset.x
                                    val dy = ny - tapOffset.y
                                    dx * dx + dy * dy
                                }
                                clickedNode?.let { onSelectNode(it.id) }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Draw Grid Lines background
                    val gridSpacing = 40.dp.toPx()
                    var x = 0f
                    while (x < canvasWidth) {
                        drawLine(
                            color = CyberSurfaceVariant.copy(alpha = 0.3f),
                            start = Offset(x, 0f),
                            end = Offset(x, canvasHeight),
                            strokeWidth = 1f
                        )
                        x += gridSpacing
                    }
                    var y = 0f
                    while (y < canvasHeight) {
                        drawLine(
                            color = CyberSurfaceVariant.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f
                        )
                        y += gridSpacing
                    }

                    // Map of node IDs to Offsets
                    val nodePos = nodes.associate { node ->
                        node.id to Offset(node.xRatio * canvasWidth, node.yRatio * canvasHeight)
                    }

                    // Draw Connecting Lines
                    nodes.forEach { sourceNode ->
                        val sourcePos = nodePos[sourceNode.id] ?: return@forEach
                        sourceNode.connectedToIds.forEach { targetId ->
                            val targetPos = nodePos[targetId]
                            if (targetPos != null) {
                                val isHighRiskLink = sourceNode.status == NodeStatus.WARNING ||
                                        sourceNode.status == NodeStatus.LOCKED
                                val lineColor = if (isHighRiskLink) AmberWarning.copy(alpha = 0.6f) else CyanPrimary.copy(alpha = 0.5f)

                                drawLine(
                                    color = lineColor,
                                    start = sourcePos,
                                    end = targetPos,
                                    strokeWidth = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            }
                        }
                    }

                    // Draw Nodes
                    nodes.forEach { node ->
                        val pos = nodePos[node.id] ?: return@forEach
                        val isSelected = node.id == selectedNodeId

                        val nodeColor = when (node.status) {
                            NodeStatus.ACTIVE -> EmeraldSecure
                            NodeStatus.WARNING -> AmberWarning
                            NodeStatus.LOCKED -> CoralCritical
                            NodeStatus.ISOLATED -> PurpleAccent
                        }

                        // Selected Glow Ring
                        if (isSelected) {
                            drawCircle(
                                color = nodeColor.copy(alpha = 0.35f),
                                radius = 28.dp.toPx(),
                                center = pos
                            )
                        }

                        // Base Node Circle
                        drawCircle(
                            color = CyberSurface,
                            radius = 18.dp.toPx(),
                            center = pos
                        )
                        drawCircle(
                            color = nodeColor,
                            radius = 14.dp.toPx(),
                            center = pos
                        )
                        drawCircle(
                            color = CyberDarkBg,
                            radius = 6.dp.toPx(),
                            center = pos
                        )
                    }
                }

                // Overlay Canvas Controls Legend
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CanvasLegendItem("Active", EmeraldSecure)
                    CanvasLegendItem("Warning", AmberWarning)
                    CanvasLegendItem("Locked", CoralCritical)
                    CanvasLegendItem("Isolated", PurpleAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Node Quick Config Panel
        if (selectedNode != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NODE CONTROLS: ${selectedNode.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Zone: ${selectedNode.zone}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real-time animated status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE NODE HEALTH & LATENCY:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )

                        AnimatedNodeStatusBadge(
                            status = selectedNode.status,
                            latencyMs = selectedNode.latencyMs,
                            showLatency = true,
                            showHealthScore = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "OVERRIDE NODE SECURITY STATUS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NodeStatus.values().forEach { status ->
                            val isCurrent = selectedNode.status == status
                            val statusColor = when (status) {
                                NodeStatus.ACTIVE -> EmeraldSecure
                                NodeStatus.WARNING -> AmberWarning
                                NodeStatus.LOCKED -> CoralCritical
                                NodeStatus.ISOLATED -> PurpleAccent
                            }

                            FilterChip(
                                selected = isCurrent,
                                onClick = { onUpdateNodeStatus(selectedNode.id, status) },
                                label = {
                                    Text(
                                        text = status.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = statusColor.copy(alpha = 0.25f),
                                    selectedLabelColor = statusColor,
                                    containerColor = CyberDarkBg,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isCurrent,
                                    borderColor = CyberSurfaceVariant,
                                    selectedBorderColor = statusColor
                                )
                            )
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CyberSurface.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant)
            ) {
                Text(
                    text = "Tap any node on the topology canvas above to inspect or modify access parameters.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Commit Canvas Changes Button (Triggers Security Barrier Dialog)
        Button(
            onClick = onRequestCommitCanvas,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("commit_topology_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasUncommittedChanges) CoralCritical else CyanPrimary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasUncommittedChanges) "COMMIT CANVAS CHANGES (HIGH-STAKES)" else "COMMIT TOPOLOGY STATE",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun CanvasLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        ) { }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}
