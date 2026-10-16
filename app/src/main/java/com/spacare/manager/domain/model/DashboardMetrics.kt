package com.spacare.manager.domain.model

data class TopServiceMetric(
    val serviceName: String,
    val bookingCount: Int,
    val totalRevenue: Long,
    val percentage: Float
)

data class RevenueDistribution(
    val treatmentPackages: Long, // Gói liệu trình
    val singleServices: Long,    // Dịch vụ lẻ
    val retailProducts: Long     // Sản phẩm mỹ phẩm bán lẻ
)

data class StaffPerformance(
    val staffId: String,
    val staffName: String,
    val role: String,
    val totalCustomersServed: Int,
    val revenueGenerated: Long,
    val rating: Float
)

data class DashboardMetrics(
    val todayRevenue: Long,
    val todayGrowthPercent: Float, // +15.4%
    val weekRevenue: Long,
    val monthRevenue: Long,
    val totalBookingsToday: Int,
    val roomOccupancyRate: Float, // 85%
    val topServices: List<TopServiceMetric>,
    val revenueDistribution: RevenueDistribution,
    val topPerformers: List<StaffPerformance>
)

