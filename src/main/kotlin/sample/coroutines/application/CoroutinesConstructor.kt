package sample.sample.coroutines.application

import sample.IProgram

fun coroutineConstructorsProgram(version: Int, vararg args: String): IProgram = when (version) {
    1 -> PalakChickenRecipe()
    else -> throw IllegalArgumentException("Invalid version")
}

private class PalakBaseRecipe() : BaseRecipe() {
    override fun _cook(): Ingredient.CompoundIngredient {
        utensil.add(Ingredient.WATER)
        //Boil the water
        gas.modulateFlame(Gas.FLAME.HIGH, 5 * 60)
        //Add palak to  the  utensil
        utensil.add(Ingredient.PALAK)
        gas.modulateFlame(Gas.FLAME.MEDIUM, 5 * 60)
        //Add spices to the utensil
        utensil.add(Ingredient.SPICES)
        gas.modulateFlame(Gas.FLAME.LOW, 5 * 60)
        return utensil.empty("Palak Base")
    }
}

private class BoilChickenRecipe() : BaseRecipe() {
    override fun _cook(): Ingredient.CompoundIngredient {
        utensil.add(Ingredient.WATER)
        //Boil the water
        gas.modulateFlame(Gas.FLAME.HIGH, 5 * 60)
        //Add palak to  the  utensil
        utensil.add(Ingredient.CHICKEN)
        gas.modulateFlame(Gas.FLAME.MEDIUM, 5 * 60)
        //Add spices to the utensil
        utensil.add(Ingredient.SPICES)
        gas.modulateFlame(Gas.FLAME.LOW, 5 * 60)
        return utensil.empty("Boiled Chicken")
    }
}


class PalakChickenRecipe() : IProgram, BaseRecipe() {
    override fun execute() {
        println("Executing coroutineConstructorsProgram ...")
        println(cook())
    }

    override fun _cook(): Ingredient.CompoundIngredient =
        utensil.apply {
            add(PalakBaseRecipe().cook())
            add(BoilChickenRecipe().cook())
        }.empty("Palak Chicken")
}

abstract class BaseRecipe(
    val utensil: Utensil = Utensil(),
    val gas: Gas.CookingGas = Gas.CookingGas(utensilPlaced = utensil)
) : Recipe {
    abstract fun _cook(): Ingredient.CompoundIngredient
    override fun cook(): Ingredient.CompoundIngredient {
        gas.placeUtensil(utensil)
        val cookedFood = _cook()
        utensil.add(cookedFood)
        gas.off()
        return utensil.empty(cookedFood.name)
    }
}

sealed class Gas(private var on: Boolean = true, var flame: FLAME = FLAME.LOW, var utensilPlaced: Utensil? = null) {
    private val flameStats = mutableListOf<Pair<FLAME, Int>>()

    class CookingGas(on: Boolean = true, flame: FLAME = FLAME.LOW, utensilPlaced: Utensil) :
        Gas(on, flame, utensilPlaced)

    enum class FLAME { NONE, LOW, MEDIUM, HIGH }

    fun on() {
        on = true
    }

    fun off() {
        on = false
    }

    fun modulateFlame(flame: FLAME, seconds: Int = 60 * 2) {
        this.flame = flame
        flameStats.add(Pair(flame, seconds))
    }

    fun placeUtensil(utensil: Utensil?) {
        utensilPlaced = utensil
    }

    override fun toString(): String =
        "(on -> $on, flame -> $flame, utensilPlaced -> $utensilPlaced), flameStats -> $flameStats)"
}


class Utensil(val type: UtensilType = UtensilType.KADAI, initialIngredients: List<Ingredient> = emptyList()) {
    enum class UtensilType {
        PAN, KADAI, PRESSURE_COOKER
    }

    val ingredients = mutableListOf<Ingredient>()

    init {
        ingredients.addAll(initialIngredients)
    }

    fun add(ingredient: Ingredient) {
        ingredients.add(ingredient)
    }

    fun remove(vararg ingredientToBeRemoved: Ingredient): List<Ingredient> {
        return ingredients.filter { ingredientToBeRemoved.contains(it) }.apply { ingredients.removeAll(this) }
    }

    fun empty(dishName: String = "Food"): Ingredient.CompoundIngredient {
        val cookedFood = Ingredient.CompoundIngredient(name = dishName, ingredients = ingredients.toList())
        ingredients.clear()
        return cookedFood
    }
}

sealed class Ingredient {
    object WATER : Ingredient()
    object PALAK : Ingredient()
    object CHICKEN : Ingredient()
    object PANEEER : Ingredient()
    object SPICES : Ingredient()
    data class CompoundIngredient(val name: String, val ingredients: List<Ingredient>) : Ingredient()

    override fun toString(): String = this::class.simpleName ?: "Ingredient"
}

interface Recipe {
    fun cook(): Ingredient.CompoundIngredient
}


/**
 * Palak Chicken Recipe
 * 1. A program to cook the palak base
 * 2. A program to boil the chicken
 * 3. A program to add the boiled chicken to the palak base
 *
 * Cooking Program
 * 1. Capture the algorithm & reuse the algorithm i.e recipe
 * 1.1  Recipe -> cook() : CompoundIngredient
 * 2. Objects
 * 2.1 Utensil -> Container of ingredients/food
 * 2.2 Gas -> Different gases have different state/functionality i.e properties/behaivour e.g CookingGas, Oven, Tandoor
 * 2.3 Grinder -> Grinds the ingredients
 * 3. Data i.e primitives
 * 3.1 Ingredients are data
 */