package com.example.giftshop.data

/**
 * In-memory catalogue. Swap this for a network or database backed source
 * without touching the UI layer, which only depends on [products].
 */
object ProductRepository {

    val products: List<Product> = listOf(
        Product(
            id = "candle-fig",
            name = "Duftkerze Feige",
            tagline = "Handgegossen, 45 Std. Brenndauer",
            description = "Sojawachs mit einer warmen Note aus Feige, Zeder und " +
                "einem Hauch Vanille. Im wiederverwendbaren Glas mit Holzdeckel.",
            priceCents = 2490,
            emoji = "🕯️",
            category = Category.HOME,
            accent = 0xFFF3E2D0,
        ),
        Product(
            id = "mug-sunrise",
            name = "Becher Sunrise",
            tagline = "Steingut, 350 ml",
            description = "Von Hand glasierter Steingutbecher mit Verlauf von " +
                "Apricot zu Sand. Spülmaschinen- und mikrowellenfest.",
            priceCents = 1890,
            emoji = "☕",
            category = Category.HOME,
            accent = 0xFFFAD9C1,
        ),
        Product(
            id = "choco-box",
            name = "Pralinen-Box",
            tagline = "16 Stück, dunkle Kuvertüre",
            description = "Sechzehn gefüllte Pralinen aus 70 % Kakao: Salzkaramell, " +
                "Haselnuss, Himbeere und Espresso. In der Geschenkschachtel.",
            priceCents = 1650,
            emoji = "🍫",
            category = Category.SWEETS,
            accent = 0xFFE8D5C4,
        ),
        Product(
            id = "tea-sampler",
            name = "Tee-Sortiment",
            tagline = "8 Sorten, lose Blätter",
            description = "Acht lose Tees in wiederverschließbaren Dosen, von " +
                "Earl Grey bis Rooibos-Vanille. Mit Sieb und Aufbewahrungsbox.",
            priceCents = 2290,
            emoji = "🍵",
            category = Category.SWEETS,
            accent = 0xFFD6E5D2,
        ),
        Product(
            id = "scarf-wool",
            name = "Schal Merino",
            tagline = "100 % Merinowolle",
            description = "Feinstrick aus mulesingfreier Merinowolle, 180 × 35 cm. " +
                "Leicht, warm und angenehm auf der Haut.",
            priceCents = 3900,
            emoji = "🧣",
            category = Category.ACCESSORIES,
            accent = 0xFFD9DCEA,
        ),
        Product(
            id = "tote-canvas",
            name = "Stofftasche",
            tagline = "Bio-Baumwolle, 12 l",
            description = "Robuste Canvas-Tasche mit Innenfach und verstärkten " +
                "Henkeln. Bedruckt mit wasserbasierter Farbe.",
            priceCents = 1490,
            emoji = "👜",
            category = Category.ACCESSORIES,
            accent = 0xFFE3E0D4,
        ),
        Product(
            id = "notebook-linen",
            name = "Notizbuch Leinen",
            tagline = "A5, 192 Seiten, dotted",
            description = "Fadengebundenes Notizbuch mit Leineneinband, " +
                "100 g/m² Papier, Lesebändchen und Gummiverschluss.",
            priceCents = 1790,
            emoji = "📓",
            category = Category.STATIONERY,
            accent = 0xFFDCE6EC,
        ),
        Product(
            id = "pen-brass",
            name = "Kugelschreiber Messing",
            tagline = "Nachfüllbar, mit Etui",
            description = "Massives Messing, das mit der Zeit Patina ansetzt. " +
                "Standardmine, nachfüllbar, im Filzetui.",
            priceCents = 2790,
            emoji = "🖊️",
            category = Category.STATIONERY,
            accent = 0xFFF0E4C8,
        ),
        Product(
            id = "cards-set",
            name = "Grußkarten-Set",
            tagline = "10 Karten mit Umschlägen",
            description = "Zehn blanko Klappkarten auf Naturkarton, im " +
                "Risographie-Druck, mit passenden Umschlägen.",
            priceCents = 1290,
            emoji = "💌",
            category = Category.STATIONERY,
            accent = 0xFFF2DCE0,
        ),
        Product(
            id = "plant-mini",
            name = "Mini-Monstera",
            tagline = "Im Keramiktopf, 15 cm",
            description = "Junge Monstera deliciosa im handgetöpferten " +
                "Keramikübertopf. Pflegeanleitung liegt bei.",
            priceCents = 2190,
            emoji = "🪴",
            category = Category.HOME,
            accent = 0xFFD2E4D5,
        ),
    )

    fun byId(id: String): Product? = products.firstOrNull { it.id == id }
}
