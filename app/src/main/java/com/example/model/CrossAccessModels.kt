package com.example.model

enum class RiskLevel(val label: String) {
    LOW("LOW RISK"),
    MEDIUM("MODERATE RISK"),
    HIGH("HIGH RISK"),
    CRITICAL("CRITICAL SECURITY IMPACT")
}

data class TopologyNode(
    val id: String,
    val name: String,
    val zone: String,
    val xRatio: Float, // 0.0 to 1.0 on canvas
    val yRatio: Float, // 0.0 to 1.0 on canvas
    val status: NodeStatus = NodeStatus.ACTIVE,
    val trafficKbps: Int = 450,
    val latencyMs: Int = 12,
    val connectedToIds: List<String> = emptyList()
)

enum class NodeStatus {
    ACTIVE,
    WARNING,
    LOCKED,
    ISOLATED
}

data class SimulationParams(
    val threatLevel: String = "Medium",
    val attackVector: String = "Distributed DDoS / Buffer Injection",
    val packetsPerSec: Int = 12500,
    val latencyMs: Int = 24,
    val anomalyThreshold: Float = 0.85f,
    val autoIsolateBreaches: Boolean = true,
    val quantumEncryption: Boolean = true
)

data class TelemetryLog(
    val id: String,
    val timestamp: String,
    val source: String,
    val message: String,
    val severity: LogSeverity
)

enum class LogSeverity {
    INFO,
    WARNING,
    CRITICAL,
    SUCCESS
}

data class SecurityBarrierRequest(
    val title: String,
    val targetScope: String,
    val description: String,
    val riskLevel: RiskLevel,
    val affectedNodes: List<String>,
    val actionType: SecurityActionType,
    val pendingActionPayload: () -> Unit
)

enum class SecurityActionType {
    TOPOLOGY_CANVAS_COMMIT,
    SIMULATION_PARAM_COMMIT,
    NODE_LOCKDOWN,
    EMERGENCY_ISOLATION
}
