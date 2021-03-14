package com.marketfinance.app.utils.network.parser.responses

data class AssetProfile(
    val city: String?,
    val state: String?,
    val phone: String?,
    val website: String?,
    val industry: String?,
    val sector: String?,
    val longBusinessSummary: String?,
    val fullTimeEmployees: Int?,
    val companyOfficers: List<CompanyOfficer?>?,
    val auditRisk: Int?,
    val boardRisk: Int?,
    val compensationRisk: Int?,
    val shareHolderRightsRisk: Int?,
    val overallRisk: Int?,
    val governanceEpochDate: Long?,
    val compensationAsOfEpochDate: Long?,
    val maxAge: Long?
)
