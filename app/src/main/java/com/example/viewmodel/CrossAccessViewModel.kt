package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.LogSeverity
import com.example.model.NodeStatus
import com.example.model.RiskLevel
import com.example.model.SecurityActionType
import com.example.model.SecurityBarrierRequest
import com.example.model.SimulationParams
import com.example.model.TelemetryLog
import com.example.model.TopologyNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class CrossAccessUiState(
    val selectedTab: Int = 0, // 0: Topology, 1: Simulation, 2: Telemetry, 3: Analytics
    val nodes: List<TopologyNode> = defaultNodes(),
    val selectedNodeId: String? = "node-1",
    val simulationParams: SimulationParams = SimulationParams(),
    val telemetryLogs: List<TelemetryLog> = initialLogs(),
    val barrierAuditLogs: List<String> = emptyList(),
    val pendingSecurityBarrier: SecurityBarrierRequest? = null,
    val hasTopologyPendingEdits: Boolean = false,
    val hasSimulationPendingEdits: Boolean = false,
    val snackbarMessage: String? = null
)

private fun defaultNodes() = listOf(
    TopologyNode("node-1", "Zone-Alpha Gateway", "Zone-Alpha", 0.2f, 0.3f, NodeStatus.ACTIVE, 1250, 8, listOf("node-2", "node-3")),
    TopologyNode("node-2", "Perimeter Bridge 01", "Perimeter", 0.5f, 0.2f, NodeStatus.WARNING, 2400, 22, listOf("node-4")),
    TopologyNode("node-3", "Perimeter Bridge 02", "Perimeter", 0.5f, 0.6f, NodeStatus.ACTIVE, 890, 12, listOf("node-4")),
    TopologyNode("node-4", "Core Telemetry Sink", "Core", 0.8f, 0.45f, NodeStatus.ACTIVE, 3100, 5, emptyList())
)

private fun initialLogs() = listOf(
    TelemetryLog("log-1", "19:01:05", "SYS_AUTH", "CrossAccess security kernel initialized.", LogSeverity.SUCCESS),
    TelemetryLog("log-2", "19:02:12", "PERIMETER_01", "High traffic burst on Perimeter Bridge 01.", LogSeverity.WARNING),
    TelemetryLog("log-3", "19:03:40", "CORE_SINK", "Lattice encryption tunnel active.", LogSeverity.INFO)
)

class CrossAccessViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CrossAccessUiState())
    val uiState: StateFlow<CrossAccessUiState> = _uiState.asStateFlow()

    init {
        startRealtimeTelemetryTicker()
    }

    private fun startRealtimeTelemetryTicker() {
        viewModelScope.launch {
            while (true) {
                delay(2000)
                _uiState.update { state ->
                    val updatedNodes = state.nodes.map { node ->
                        val latencyDelta = Random.nextInt(-2, 3)
                        val trafficDelta = Random.nextInt(-40, 50)
                        val baseLatency = when (node.status) {
                            NodeStatus.ACTIVE -> 8
                            NodeStatus.WARNING -> 28
                            NodeStatus.LOCKED -> 85
                            NodeStatus.ISOLATED -> 0
                        }
                        val newLatency = (node.latencyMs + latencyDelta).coerceIn(baseLatency - 4, baseLatency + 15)
                        val newTraffic = (node.trafficKbps + trafficDelta).coerceAtLeast(0)

                        node.copy(
                            latencyMs = if (node.status == NodeStatus.ISOLATED) 0 else newLatency,
                            trafficKbps = if (node.status == NodeStatus.ISOLATED) 0 else newTraffic
                        )
                    }
                    state.copy(nodes = updatedNodes)
                }
            }
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun selectNode(nodeId: String) {
        _uiState.update { it.copy(selectedNodeId = nodeId) }
    }

    fun updateNodeStatus(nodeId: String, status: NodeStatus) {
        _uiState.update { state ->
            val updatedNodes = state.nodes.map { node ->
                if (node.id == nodeId) node.copy(status = status) else node
            }
            state.copy(
                nodes = updatedNodes,
                hasTopologyPendingEdits = true
            )
        }
    }

    fun resetTopology() {
        _uiState.update {
            it.copy(
                nodes = defaultNodes(),
                hasTopologyPendingEdits = false
            )
        }
    }

    fun updateSimulationParams(params: SimulationParams) {
        _uiState.update {
            it.copy(
                simulationParams = params,
                hasSimulationPendingEdits = true
            )
        }
    }

    fun requestCommitTopology() {
        val currentNodeState = _uiState.value.nodes
        val affectedNames = currentNodeState.filter { it.status != NodeStatus.ACTIVE }.map { it.name }

        val request = SecurityBarrierRequest(
            title = "Commit Topology Canvas State",
            targetScope = "Perimeter & Zone Connectivity Grid",
            description = "High-stakes operation: Applying modified access policy overrides across core topology gateways.",
            riskLevel = if (affectedNames.isNotEmpty()) RiskLevel.HIGH else RiskLevel.MEDIUM,
            affectedNodes = if (affectedNames.isNotEmpty()) affectedNames else listOf("Zone-Alpha Gateway", "Perimeter Bridge 01"),
            actionType = SecurityActionType.TOPOLOGY_CANVAS_COMMIT,
            pendingActionPayload = {
                _uiState.update { state ->
                    val newAudit = "[${state.barrierAuditLogs.size + 1}] COMMITTED Topology Canvas changes (${affectedNames.size} node overrides applied)."
                    val newLog = TelemetryLog(
                        id = "log-${System.currentTimeMillis()}",
                        timestamp = "19:04:15",
                        source = "BARRIER_AUTH",
                        message = "Topology canvas state committed via Security Barrier authorization.",
                        severity = LogSeverity.SUCCESS
                    )
                    state.copy(
                        hasTopologyPendingEdits = false,
                        barrierAuditLogs = listOf(newAudit) + state.barrierAuditLogs,
                        telemetryLogs = listOf(newLog) + state.telemetryLogs,
                        snackbarMessage = "Topology Canvas state successfully committed!"
                    )
                }
            }
        )

        _uiState.update { it.copy(pendingSecurityBarrier = request) }
    }

    fun requestCommitSimulation() {
        val currentParams = _uiState.value.simulationParams
        val risk = when (currentParams.threatLevel) {
            "Critical" -> RiskLevel.CRITICAL
            "High" -> RiskLevel.HIGH
            else -> RiskLevel.MEDIUM
        }

        val request = SecurityBarrierRequest(
            title = "Commit Simulation Parameters",
            targetScope = "${currentParams.attackVector} (${currentParams.threatLevel})",
            description = "High-stakes operation: Injecting simulated threat load at ${currentParams.packetsPerSec} PKT/S into perimeter telemetry pipeline.",
            riskLevel = risk,
            affectedNodes = listOf("Zone-Alpha Gateway", "Perimeter Bridge 01", "Core Telemetry Sink"),
            actionType = SecurityActionType.SIMULATION_PARAM_COMMIT,
            pendingActionPayload = {
                _uiState.update { state ->
                    val newAudit = "[${state.barrierAuditLogs.size + 1}] COMMITTED Simulation params (${currentParams.threatLevel} threat / ${currentParams.packetsPerSec} PKT/S)."
                    val newLog = TelemetryLog(
                        id = "log-${System.currentTimeMillis()}",
                        timestamp = "19:04:30",
                        source = "SIM_ENGINE",
                        message = "Threat injection scenario [${currentParams.attackVector}] committed via Security Barrier.",
                        severity = LogSeverity.WARNING
                    )
                    state.copy(
                        hasSimulationPendingEdits = false,
                        barrierAuditLogs = listOf(newAudit) + state.barrierAuditLogs,
                        telemetryLogs = listOf(newLog) + state.telemetryLogs,
                        snackbarMessage = "Scenario Simulation parameters committed!"
                    )
                }
            }
        )

        _uiState.update { it.copy(pendingSecurityBarrier = request) }
    }

    fun confirmSecurityBarrier() {
        val request = _uiState.value.pendingSecurityBarrier
        request?.pendingActionPayload?.invoke()
        _uiState.update { it.copy(pendingSecurityBarrier = null) }
    }

    fun dismissSecurityBarrier() {
        _uiState.update { it.copy(pendingSecurityBarrier = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
