package com.marketfinance.app.utils.network.parser.responses

import com.marketfinance.app.utils.network.parser.RawLongFMT

data class CompanyOfficer(
    val maxAge: Long?,
    val name: String?,
    val age: Int?,
    val title: String?,
    val yearBorn: Int?,
    val fiscalYear: Int?,
    val totalPay: RawLongFMT?,
    val exercisedValue: RawLongFMT?,
    val unexercisedValue: RawLongFMT?
)
