# Debug/release without minify for now. Keep rules ready for serialization.
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.serialization.**
-keep class lab.neurotrino.app.data.export.** { *; }
