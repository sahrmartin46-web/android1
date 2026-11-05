package c.app.sociamediabackend.data.model

data class MonetizationProgram(
    val id: String,
    val icon: String,
    val title: String,
    val category: String,
    val payoutRate: String,
    val description: String,
    val requirement: String,
    val status: String,
    val isEnrolled: Boolean = false
)

data class MonetizationProgramsResponse(
    val success: Boolean,
    val programs: List<MonetizationProgram>?
)
