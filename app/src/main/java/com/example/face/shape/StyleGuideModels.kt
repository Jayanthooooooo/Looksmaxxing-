package com.example.face.shape

data class ShapeStyleGuide(
    val id: String,
    val name: String,
    val description: String,
    val barber_summary: String,
    val haircuts: Haircuts,
    val beard_options: List<BeardOption>,
    val eyebrow_tips: String,
    val glasses_frames: List<String>,
    val what_to_avoid: List<String>
)

data class Haircuts(
    val top: String,
    val sides: String,
    val fringe: String,
    val parting: String
)

data class BeardOption(
    val style: String,
    val effect: String
)
