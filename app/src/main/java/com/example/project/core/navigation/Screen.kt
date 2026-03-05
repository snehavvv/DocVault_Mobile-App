package com.example.project.core.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object ImageToPdf : Screen("image_to_pdf")
    object PdfToImage : Screen("pdf_to_image")
    object Scanner : Screen("scanner")
    object FileOrganizer : Screen("file_organizer")
    object SignaturePad : Screen("signature_pad")
    object SecureNotes : Screen("secure_notes")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
    object MergePdf : Screen("merge_pdf")
}