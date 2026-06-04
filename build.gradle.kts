plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// android.applicationVariants.all { variant ->

//     def outputName = "OnlyJesus-latest.apk"
// 	def path_to_filename = "${buildDir}/outputs/apk/${variant.name}/${outputName}"

//     variant.outputFile = file(path_to_filename)

// }