package c.app.sociamediabackend.data.model

data class Gift(
    val id: String,
    val name: String,
    val slug: String,
    val iconUrl: String,
    val coinCost: Int,
    val category: String?
)

data class Wallet(
    val id: String,
    val userId: String,
    val coinBalance: Int,
    val diamondBalance: Int
)

data class WalletResponse(
    val success: Boolean,
    val message: String?,
    val wallet: Wallet?
)

data class GiftCatalogResponse(
    val success: Boolean,
    val gifts: List<Gift>?
)
