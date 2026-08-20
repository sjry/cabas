# R8 est actif sur la build release.
#
# Compose, Room et kotlinx-coroutines embarquent leurs propres règles (consumer
# rules), il n'y a donc rien à déclarer pour eux. Les entités Room sont
# référencées par le code généré, qui est lui-même conservé.

# Conserve les noms des entités et énumérations persistées : le nom de la
# constante IngredientUnit est écrit tel quel en base par le TypeConverter,
# un renommage casserait la relecture des recettes déjà importées.
-keepclassmembers enum com.sjarry.cuisine.parser.IngredientUnit {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static ** *;
}

# Trace lisible en cas de plantage.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
