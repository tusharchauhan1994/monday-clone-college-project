package com.example.mondaycloneapp.models

data class ContactUs(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val company: String = "",
    val website: String = "",
    val subject: String = "",
    val message: String = "",
    val loginGmail: String? = ""
)