# Vosk usa JNA para llamar a su biblioteca nativa (D-081); R8 no debe eliminar ni renombrar estas clases.
-keep class com.sun.jna.* { *; }
-keepclassmembers class * extends com.sun.jna.* { public *; }
