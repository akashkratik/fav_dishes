package com.example.dishes.utils

object Constants {

    const val DISH_TYPE: String = "DishType"
    const val DISH_CATEGORY: String = "DishCategory"
    const val DISH_COOKING_TIME: String = "DishCookingTime"

    const val DISH_IMAGE_SOURCE_LOCAL: String = "Local"
    const val DISH_IMAGE_SOURCE_ONLINE: String = "Online"

    fun dishTypes(): ArrayList<String>{
        val list = ArrayList<String>()
        list.add("Breakfast")
        list.add("Lunch")
        list.add("Dinner")
        list.add("Snacks")
        list.add("Side dish")
        list.add("Dessert")
        list.add("Salad")
        list.add("Other")
        return list
    }

    fun dishCategories(): ArrayList<String>{
        val list = ArrayList<String>()
        list.add("Pizza")
        list.add("BBQ")
        list.add("Bakery")
        list.add("Burger")
        list.add("Cafe")
        list.add("Chicken")
        list.add("Drinks")
        list.add("Hot Dogs")
        list.add("Juices")
        list.add("Sandwiches")
        list.add("Tea & Coffee")
        list.add("Wraps")
        list.add("Other")
        return list
    }

    fun dishCookTime(): ArrayList<String>{
        val list = ArrayList<String>()
        list.add("less than 30")
        list.add("30")
        list.add("45")
        list.add("60")
        list.add("90")
        list.add("120")
        list.add("150")
        list.add("180")
        list.add("more than 180")
        return list
    }

}