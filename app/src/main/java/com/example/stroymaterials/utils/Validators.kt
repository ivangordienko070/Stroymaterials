package com.example.stroymaterials.utils

object Validators {
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
        return emailRegex.matches(email)
    }

    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^[+]?[0-9]{10,15}\$".toRegex()
        return phoneRegex.matches(phone)
    }

    fun isValidQuantity(quantity: String): Boolean {
        return quantity.toDoubleOrNull() != null && quantity.toDouble() > 0
    }

    fun isValidPrice(price: String): Boolean {
        return price.toDoubleOrNull() != null && price.toDouble() >= 0
    }
}