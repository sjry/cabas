package com.sjarry.cabas.parser

import java.util.Locale

/**
 * Rayon de magasin d'un ingrédient. Sept rayons volontairement larges : il s'agit
 * de regrouper le parcours en magasin, pas de reproduire le plan d'un hypermarché.
 *
 * **L'ordre de déclaration est l'ordre d'affichage** de la liste de courses : il suit
 * le parcours habituel, du frais vers l'épicerie puis les surgelés. [OTHER] ferme la
 * marche — c'est le repli de ce que le lexique ne connaît pas encore.
 */
enum class IngredientCategory(val label: String) {
    PRODUCE("Fruits & légumes"),
    BUTCHER("Boucherie & poissonnerie"),
    DAIRY("Crémerie"),
    BAKERY("Boulangerie"),
    GROCERY("Épicerie"),
    FROZEN("Surgelés"),
    OTHER("Divers"),
}

/**
 * Devine le rayon à partir du **nom** de l'ingrédient, jamais de la recette : le sel
 * est au même endroit quel que soit le plat. C'est ce qui permet de classer sans rien
 * demander à l'utilisateur, et de reclasser les recettes déjà importées.
 */
internal object CategoryVocabulary {

    private data class Rule(val regex: Regex, val category: IngredientCategory)

    /**
     * Motif ancré sur des frontières de mot, pour que « ail » ne soit pas reconnu dans
     * « cocktail ». Le nom est déjà minusculé et désaccentué : ni casse ni accents ici.
     */
    private fun p(body: String) = Regex("""(?<![a-z0-9])(?:$body)(?![a-z0-9])""")

    /**
     * Un ingrédient annoncé surgelé est au rayon surgelés, quoi qu'il soit par ailleurs.
     * Testé **avant** le lexique, car « petits pois surgelés » contient un mot-clé de
     * légume plus long que « surgelés » et gagnerait la règle du plus long match.
     */
    private val frozenMarker = p("""surgelees?|surgeles?|congelees?|congeles?""")

    private fun of(category: IngredientCategory, vararg bodies: String) =
        bodies.map { Rule(p(it), category) }

    /**
     * Le **plus long mot-clé trouvé l'emporte**. C'est toute la subtilité du lexique :
     * « lait de coco » est en épicerie alors que « lait » est en crémerie, « tomates
     * pelées » en conserve alors que « tomates » sont au frais. Il suffit d'ajouter le
     * cas particulier, sans toucher au cas général.
     */
    private val rules: List<Rule> = listOf(
        of(
            IngredientCategory.PRODUCE,
            "ail", "oignons?", "echalotes?", "carottes?",
            "pommes? de terre", "patates? douces?",
            "courgettes?", "aubergines?", "poivrons?", "tomates?",
            "salades?", "laitues?", "roquette", "mache", "cresson", "epinards?",
            "concombres?", "radis", "navets?", "poireaux?", "poireau", "celeri",
            "choux?", "chou-?fleur", "brocolis?", "haricots? verts?",
            "champignons?", "courges?", "potirons?", "potimarrons?", "butternut",
            "fenouil", "betteraves?", "artichauts?", "asperges?", "endives?",
            "blettes?", "panais", "topinambours?", "petits? pois", "feves?",
            "persil", "coriandre", "basilic", "ciboulette", "menthe", "aneth",
            "estragon", "cerfeuil", "gingembre",
            "citrons?", "citrons? verts?", "oranges?", "pommes?", "poires?",
            "bananes?", "fraises?", "framboises?", "myrtilles?", "groseilles?",
            "cassis", "cerises?", "raisins?", "peches?", "nectarines?", "abricots?",
            "prunes?", "melons?", "pasteques?", "ananas", "mangues?", "avocats?",
            "kiwis?", "figues?", "grenades?", "clementines?", "mandarines?",
            "pamplemousses?", "rhubarbe",
        ),
        of(
            IngredientCategory.BUTCHER,
            "poulets?", "blancs? de poulet", "cuisses? de poulet", "ailes? de poulet",
            "escalopes?", "dindes?", "canards?", "magrets?", "lapins?", "pintades?",
            "boeufs?", "steaks?", "bavettes?", "entrecotes?", "faux-filet", "rumsteck",
            "rotis?", "paleron", "bourguignon", "veau", "agneau", "gigots?",
            "porc", "filets? mignons?", "echine", "travers de porc",
            "viandes? hachees?", "hachis",
            "saucisses?", "saucissons?", "chipolatas?", "merguez", "lardons?",
            "jambons?", "bacon", "chorizo", "pancetta", "poitrine fumee", "coppa",
            "rillettes", "boudins?", "andouilles?", "andouillettes?", "terrines?",
            "foie gras", "cordons? bleus?",
            "poissons?", "saumons?", "saumons? fumes?", "cabillauds?", "colin",
            "lieu noir", "merlu", "dorades?", "bars?", "truites?", "maquereaux?",
            "soles?", "lottes?", "raies?", "espadons?",
            "crevettes?", "gambas", "moules?", "huitres?", "coquilles? saint-?jacques",
            "noix de saint-?jacques", "calamars?", "encornets?", "poulpes?", "crabes?",
            "homards?", "langoustines?", "surimi",
        ),
        of(
            IngredientCategory.DAIRY,
            "laits?", "cremes?", "cremes? fraiches?", "cremes? liquides?",
            "cremes? epaisses?", "mascarpone", "beurres?", "beurres? demi-sel",
            "oeufs?", "yaourts?", "fromages?", "fromages? blancs?", "fromages? rapes?",
            "gruyere", "emmental", "comte", "parmesan", "mozzarella", "feta", "chevre",
            "roquefort", "camembert", "ricotta", "cheddar", "raclette", "reblochon",
            "gorgonzola", "boursin", "faisselle", "skyr", "petits?-suisses?", "tofu",
            "pates? feuilletees?", "pates? brisees?", "pates? sablees?",
            "pates? a pizza", "pates? a tarte",
        ),
        of(
            IngredientCategory.BAKERY,
            "pains?", "baguettes?", "pains? de mie", "brioches?", "pains? complets?",
            "pains? burger", "buns", "croissants?", "pains? pita", "wraps?", "tortillas?",
            "pains? au lait",
        ),
        of(
            IngredientCategory.GROCERY,
            // Féculents et farines. « pâtes » au pluriel seulement : « pâte » au singulier
            // est une pâte à tarte, pas des spaghettis — la crémerie s'en charge.
            "riz", "pates", "spaghettis?", "tagliatelles?", "penne", "macaronis?",
            "coquillettes", "lasagnes?", "raviolis?", "nouilles", "vermicelles?",
            "semoule", "couscous", "boulgour", "quinoa", "lentilles?", "pois chiches?",
            "haricots? rouges?", "haricots? blancs?", "flageolets", "polenta",
            "farines?", "maizena", "fecule", "chapelure", "croutons",
            // Sucré
            "sucres?", "sucres? glace", "sucres? vanilles?", "cassonade", "vergeoise",
            "miel", "confitures?", "sirops?", "sirops? d'erable", "compotes?",
            "chocolats?", "cacao", "pepites de chocolat", "pates? a tartiner",
            "levures?", "levures? chimiques?", "levures? de boulanger", "bicarbonate",
            "vanille", "extrait de vanille", "gelatine", "agar-agar", "cremes? de marron",
            "biscuits?", "speculoos", "genoise", "pains? d'epices", "colorant",
            // Épices et condiments
            "sel", "gros sel", "fleur de sel", "poivres?", "epices?", "curry", "curcuma",
            "paprika", "cumin", "cannelle", "muscade", "noix de muscade", "safran",
            "herbes de provence", "thym", "lauriers?", "romarin", "origan", "sauge",
            "piments?", "piments? d'espelette", "harissa", "ras el hanout",
            "quatre epices", "sesame", "pavot",
            // Huiles, sauces, conserves
            "huiles?", "huiles? d'olive", "huiles? de tournesol", "huiles? de colza",
            "vinaigres?", "vinaigres? balsamiques?", "moutardes?", "ketchup",
            "mayonnaise", "sauces? soja", "sauces? tomates?", "coulis de tomates?",
            "concentres? de tomates?", "tomates? pelees?", "passata", "pulpe de tomates?",
            "bouillons?", "cubes? de bouillon", "fond de veau", "fond de volaille",
            "olives?", "cornichons?", "capres", "anchois", "thons?", "thons? en boite",
            "sardines?", "mais",
            // Coco, fruits secs et oléagineux
            "laits? de coco", "cremes? de coco", "noix de coco", "noix de coco rapee",
            "noix", "noisettes?", "amandes?", "poudres? d'amandes?", "pignons",
            "cacahuetes?", "beurres? de cacahuetes?", "pistaches?", "noix de cajou",
            "graines?", "graines? de sesame", "raisins? secs", "abricots? secs",
            "pruneaux", "dattes?", "figues? seches?",
            // Boissons
            "eaux?", "vins?", "vins? blancs?", "vins? rouges?", "bieres?", "cidres?",
            "rhum", "cognac", "porto", "kirsch", "jus", "jus d'orange", "jus de citron",
            "the", "cafes?", "laits? d'amande", "laits? de soja", "laits? d'avoine",
            "sodas?", "limonade",
        ),
        of(
            IngredientCategory.FROZEN,
            "glaces?", "cremes? glacees?", "sorbets?", "glacons?", "frites",
            "poissons? panes?", "nuggets",
        ),
    ).flatten()

    fun categorize(name: String): IngredientCategory {
        val plain = normalize(name)
        if (frozenMarker.containsMatchIn(plain)) return IngredientCategory.FROZEN

        var best: IngredientCategory? = null
        var bestLength = 0
        for (rule in rules) {
            val found = rule.regex.find(plain) ?: continue
            if (found.value.length > bestLength) {
                best = rule.category
                bestLength = found.value.length
            }
        }
        return best ?: IngredientCategory.OTHER
    }

    /**
     * « œuf » et « Œuf » doivent tomber sur le même mot-clé que « oeuf ». La
     * désaccentuation générale remplace « œ » par un seul « o » — elle préserve la
     * longueur des chaînes, ce dont le parseur d'unités a besoin — donc les ligatures
     * sont développées ici, avant elle. L'apostrophe typographique est ramenée à
     * l'apostrophe droite écrite dans les motifs.
     */
    private fun normalize(name: String): String = name
        .replace("œ", "oe").replace("Œ", "Oe")
        .replace("æ", "ae").replace("Æ", "Ae")
        .replace('’', '\'')
        .deaccent()
        .lowercase(Locale.ROOT)
}
