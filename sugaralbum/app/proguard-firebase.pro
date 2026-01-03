# Begin: Proguard rules for Firebase

# Authentication
-keepattributes *Annotation*

# Realtime database
-keepattributes Signature

-dontwarn com.google.firebase.ktx.Firebase
-dontwarn com.google.firebase.ktx.FirebaseKt
# End: Proguard rules for Firebase